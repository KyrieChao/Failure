package com.chao.failfast;

import com.chao.failfast.exception.Business;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.reactive.StrictProcessor;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.BaseSubscriber;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class FailureStrictStreamTest {

    @Test
    void strictStreamShouldEmitEveryError() {
        Flux<Business> flux = StrictProcessor.strictStream(List.of("a", "b"), "p", scope -> {
            scope.check(scope.it(), v -> false, ResponseCode.VALIDATION_ERROR_400, "e1");
            scope.check(scope.it(), v -> false, ResponseCode.VALIDATION_ERROR_400, "e2");
        });

        List<Business> out = flux.collectList().block();
        assertThat(out).hasSize(4);
    }

    @Test
    void strictStreamShouldStopEmittingAfterCancel() {
        AtomicInteger processed = new AtomicInteger();
        Flux<Business> flux = StrictProcessor.strictStream(List.of(1, 2, 3, 4, 5), "p", scope -> {
            processed.incrementAndGet();
            scope.check(scope.it(), v -> false, ResponseCode.VALIDATION_ERROR_400, "e");
        });

        List<Business> out = flux.take(1).collectList().block();
        assertThat(out).hasSize(1);

        assertThat(processed.get()).isEqualTo(1);
    }

    @Test
    void strictStreamShouldSkipSecondEmissionWhenSubscriberCancelsImmediately() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger received = new AtomicInteger();

        StrictProcessor.strictStream(List.of("x"), "p", scope -> {
            scope.check(scope.it(), v -> false, ResponseCode.VALIDATION_ERROR_400, "e1");
            scope.check(scope.it(), v -> false, ResponseCode.VALIDATION_ERROR_400, "e2");
        }).subscribe(new BaseSubscriber<>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(1);
            }

            @Override
            protected void hookOnNext(Business value) {
                received.incrementAndGet();
                cancel();
                latch.countDown();
            }
        });

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(received.get()).isEqualTo(1);
    }
}

