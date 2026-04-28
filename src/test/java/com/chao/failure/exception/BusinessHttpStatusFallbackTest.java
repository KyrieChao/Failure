package com.chao.failure.exception;

import com.chao.failure.internal.core.Ex;
import com.chao.failure.internal.core.ResponseCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessHttpStatusFallbackTest {

    @Test
    void shouldFallbackTo400WhenContextMissingAndResponseCodeIs400() {
        var old = Ex.getContext();
        Ex.setContext(null);
        try {
            Business b = Business.of(ResponseCode.VALIDATION_ERROR_400, "invalid");
            assertEquals(HttpStatus.BAD_REQUEST, b.getHttpStatus());
        } finally {
            Ex.setContext(old);
        }
    }
}
