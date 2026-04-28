package com.chao.failure.result;

import com.chao.failure.exception.Business;
import com.chao.failure.model.TestResponseCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReactorConversionTest {
    @Test
    void toMonoShouldEmitValueOnSuccess() {
        String v = Result.ok("ok").toMono().block();
        assertEquals("ok", v);
    }

    @Test
    void toMonoShouldErrorOnFailure() {
        Result<String> r = Result.fail(TestResponseCode.PARAM_ERROR);
        assertThrows(Business.class, () -> r.toMono().block());
    }

    @Test
    void toFluxShouldEmitCollectionElementsOnSuccess() {
        List<Integer> out = Result.ok(List.of(1, 2)).<Integer>toFluxElements().collectList().block();
        assertEquals(List.of(1, 2), out);
    }

    @Test
    void toFluxShouldEmitSingleValueOnSuccess() {
        List<String> out = Result.ok("x").toFlux().collectList().block();
        assertEquals(List.of("x"), out);
    }

    @Test
    void toFluxShouldErrorOnFailure() {
        Result<String> r = Result.fail(TestResponseCode.PARAM_ERROR);
        assertThrows(Business.class, () -> r.toFlux().collectList().block());
    }
}

