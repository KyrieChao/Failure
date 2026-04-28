package com.chao.failure.test;

import com.chao.failure.Failure;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.exception.Business;
import com.chao.failure.exception.MultiBusiness;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;

class AsyncChainTest {
    @Test
    void failAsyncShouldCompleteExceptionallyOnFailure() {
        var chain = Failure.begin()
                .checkAsync(CompletableFuture.completedFuture(false), ResponseCode.of(500, "async", "async"), "bad");

        CompletionException e = assertThrows(CompletionException.class, () -> chain.failAsync().join());
        assertTrue(e.getCause() instanceof Business);
    }

    @Test
    void failAllAsyncShouldAggregateInNonFailFastMode() {
        var chain = Failure.strict()
                .checkAsync(CompletableFuture.completedFuture(false), ResponseCode.of(500, "a", "a"))
                .checkAsync(CompletableFuture.completedFuture(false), ResponseCode.of(500, "b", "b"));

        CompletionException e = assertThrows(CompletionException.class, () -> chain.failAllAsync().join());
        assertTrue(e.getCause() instanceof MultiBusiness);
    }

    @Test
    void failMonoShouldErrorOnFailure() {
        var chain = Failure.begin()
                .checkAsync(Mono.just(false), ResponseCode.of(500, "async", "async"), "bad");
        assertThrows(CompletionException.class, () -> chain.failMono().toFuture().join());
    }
}

