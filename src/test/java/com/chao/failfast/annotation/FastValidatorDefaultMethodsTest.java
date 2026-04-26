package com.chao.failfast.annotation;

import com.chao.failfast.validator.FastValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class FastValidatorDefaultMethodsTest {

    @Test
    void allowObjectSupportedTypeDefaultsToFalse() {
        FastValidator<Object> v = (target, ctx) -> {
        };
        assertFalse(v.allowObjectSupportedType());
    }

    @Test
    void getSupportedTypeDefaultsToObject() {
        FastValidator<Object> v = (target, ctx) -> {
        };
        assertSame(Object.class, v.getSupportedType());
    }
}

