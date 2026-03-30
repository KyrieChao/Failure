package com.chao.failfast.internal.core;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.exception.Business;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChainAsyncFailureWithContextTest {

    @Test
    void failAsyncThrows500WhenInvalidButNoLocalCauses() {
        ValidationContext ctx = new ValidationContext(true);
        Chain chain = Chain.begin(ctx);
        chain.check(false, ResponseCode.VALIDATION_ERROR_400, "x");

        CompletionException ex = assertThrows(CompletionException.class, () -> chain.failAsync().join());
        Business b = (Business) ex.getCause();
        assertEquals(ResponseCode.VALIDATION_ERROR_500.getCode(), b.getResponseCode().getCode());
    }

    @Test
    void failAllAsyncThrows500WhenInvalidButNoLocalCauses() {
        ValidationContext ctx = new ValidationContext(true);
        Chain chain = Chain.begin(ctx);
        chain.check(false, ResponseCode.VALIDATION_ERROR_400, "x");

        CompletionException ex = assertThrows(CompletionException.class, () -> chain.failAllAsync().join());
        Business b = (Business) ex.getCause();
        assertEquals(ResponseCode.VALIDATION_ERROR_500.getCode(), b.getResponseCode().getCode());
    }

    @Test
    void verifyAsyncReportsFailureWhenInvalid() {
        ValidationContext ctx = new ValidationContext(true);
        Chain chain = Chain.begin(ctx);
        chain.check(false, ResponseCode.VALIDATION_ERROR_400, "x");

        Boolean ok = chain.verifyAsync().join();
        assertEquals(false, ok);
    }

    @Test
    void failAllAsyncThrowsSingleCauseWhenOnlyOneLocalCause() {
        Chain chain = Chain.begin(true);
        chain.check(false, ResponseCode.VALIDATION_ERROR_400, "x");

        CompletionException ex = assertThrows(CompletionException.class, () -> chain.failAllAsync().join());
        Business b = (Business) ex.getCause();
        assertEquals(ResponseCode.VALIDATION_ERROR_400.getCode(), b.getResponseCode().getCode());
    }

    @Test
    void verifyAsyncThrowsRuntimeCauseWhenAsyncStageFails() {
        Chain chain = Chain.begin(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.CompletionStage<Boolean> stage = org.mockito.Mockito.mock(java.util.concurrent.CompletionStage.class);
        org.mockito.Mockito.when(stage.toCompletableFuture()).thenThrow(new CompletionException(new IllegalArgumentException("x")));
        chain.checkAsync(stage, ResponseCode.VALIDATION_ERROR_400);

        CompletionException ex = assertThrows(CompletionException.class, () -> chain.verifyAsync().join());
        assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
    }

    @Test
    void verifyAsyncWrapsNonRuntimeCause() {
        Chain chain = Chain.begin(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.CompletionStage<Boolean> stage = org.mockito.Mockito.mock(java.util.concurrent.CompletionStage.class);
        org.mockito.Mockito.when(stage.toCompletableFuture()).thenThrow(new AssertionError("x"));
        chain.checkAsync(stage, ResponseCode.VALIDATION_ERROR_400);

        CompletionException ex = assertThrows(CompletionException.class, () -> chain.verifyAsync().join());
        assertEquals(RuntimeException.class, ex.getCause().getClass());
        assertEquals(AssertionError.class, ex.getCause().getCause().getClass());
    }

    @Test
    void verifyAsyncUsesNonCompletionExceptionBranch() {
        Chain chain = Chain.begin(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.CompletionStage<Boolean> stage = org.mockito.Mockito.mock(java.util.concurrent.CompletionStage.class);
        org.mockito.Mockito.when(stage.toCompletableFuture()).thenThrow(new IllegalStateException("x"));
        chain.checkAsync(stage, ResponseCode.VALIDATION_ERROR_400);

        CompletionException ex = assertThrows(CompletionException.class, () -> chain.verifyAsync().join());
        assertEquals(IllegalStateException.class, ex.getCause().getClass());
    }

    @Test
    void verifyAsyncUsesExWhenCauseNull() {
        Chain chain = Chain.begin(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.CompletionStage<Boolean> stage = org.mockito.Mockito.mock(java.util.concurrent.CompletionStage.class);
        org.mockito.Mockito.when(stage.toCompletableFuture()).thenThrow(new CompletionException("x", null));
        chain.checkAsync(stage, ResponseCode.VALIDATION_ERROR_400);

        CompletionException ex = assertThrows(CompletionException.class, () -> chain.verifyAsync().join());
        assertEquals(null, ex.getCause());
    }
}

