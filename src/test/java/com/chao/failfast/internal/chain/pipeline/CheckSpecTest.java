package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckSpecTest {

    @Test
    void testOfWithoutInvalidValue() {
        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        String detail = "Test error";
        CheckSpec spec = CheckSpec.of(code, detail);
        assertSame(code, spec.code());
        assertEquals(detail, spec.detail());
        assertNull(spec.invalidValue());
    }

    @Test
    void testOfWithInvalidValue() {
        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        String detail = "Test error";
        Object invalidValue = "invalid";
        CheckSpec spec = CheckSpec.of(code, detail, invalidValue);
        assertSame(code, spec.code());
        assertEquals(detail, spec.detail());
        assertSame(invalidValue, spec.invalidValue());
    }

    @Test
    void testRecordProperties() {
        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        String detail = "Test error";
        Object invalidValue = "invalid";
        CheckSpec spec = new CheckSpec(code, detail, invalidValue);
        assertSame(code, spec.code());
        assertEquals(detail, spec.detail());
        assertSame(invalidValue, spec.invalidValue());
    }
}
