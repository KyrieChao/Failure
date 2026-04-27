package com.chao.failfast.test;

import com.chao.failfast.Failure;
import com.chao.failfast.exception.Business;
import com.chao.failfast.internal.core.Chain;
import com.chao.failfast.reactive.StrictProcessor;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class FailureTest {

    @Test
    void testBeginReturnsChain() {
        Chain chain = Failure.begin();
        assertNotNull(chain);
    }

    @Test
    void testStrictReturnsChain() {
        Chain chain = Failure.strict();
        assertNotNull(chain);
    }

    @Test
    void testStrictStreamWithValidItems() {
        List<String> items = Arrays.asList("a", "b", "c");

        Flux<Business> flux = StrictProcessor.strictStream(items, scope -> {
        });

        List<Business> result = flux.collectList().block();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testStrictStreamWithEmptyItems() {
        List<String> items = Collections.emptyList();

        Flux<Business> flux = StrictProcessor.strictStream(items, scope -> {
        });

        List<Business> result = flux.collectList().block();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testStrictStreamWithNullItems() {
        List<String> items = null;

        Flux<Business> flux = StrictProcessor.strictStream(items, scope -> {
        });

        List<Business> result = flux.collectList().block();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testStrictStreamWithPathPrefix() {
        List<String> items = Arrays.asList("a", "b");

        Flux<Business> flux = StrictProcessor.strictStream(items, "prefix", scope -> {
        });

        List<Business> result = flux.collectList().block();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testStrictStreamWithNullPathPrefix() {
        List<String> items = Arrays.asList("a");

        Flux<Business> flux = StrictProcessor.strictStream(items, null, scope -> {
        });

        List<Business> result = flux.collectList().block();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testStrictStreamCancelBehavior() {
        List<String> items = Arrays.asList("a", "b", "c");

        Flux<Business> flux = StrictProcessor.strictStream(items, scope -> {
        });

        Flux<Business> cancelledFlux = flux.take(1);

        List<Business> result = cancelledFlux.collectList().block();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testStrictStreamErrorPropagation() {
        List<String> items = Arrays.asList("error-item");

        Flux<Business> flux = StrictProcessor.strictStream(items, scope -> {
            throw new RuntimeException("Test error");
        });

        assertThrows(RuntimeException.class, () -> flux.collectList().block());
    }

    @Test
    void testStrictStreamMultipleItemsProcessing() {
        List<Integer> items = Arrays.asList(1, 2, 3);

        Flux<Business> flux = StrictProcessor.strictStream(items, scope -> {
        });

        List<Business> result = flux.collectList().block();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testStrictStreamWithBusinessError() {
        List<String> items = Arrays.asList("valid", "invalid");

        AtomicBoolean errorHandled = new AtomicBoolean(false);

        Flux<Business> flux = StrictProcessor.strictStream(items, scope -> {
            if ("invalid".equals(scope.it().value())) {
                errorHandled.set(true);
            }
        });

        List<Business> result = flux.collectList().block();
        assertNotNull(result);
    }
}
