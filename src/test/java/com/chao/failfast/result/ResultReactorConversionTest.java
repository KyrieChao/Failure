package com.chao.failfast.result;

import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResultReactorConversionTest {

    @Test
    void toFluxOnFailureEmitsError() {
        Result<String> r = Result.fail(ResponseCode.VALIDATION_ERROR_400, "x");
        assertThrows(RuntimeException.class, () -> r.toFlux().collectList().block());
    }

    @Test
    void toFluxElementsOnFailureEmitsError() {
        Result<List<String>> r = Result.fail(ResponseCode.VALIDATION_ERROR_400, "x");
        assertThrows(RuntimeException.class, () -> r.<String>toFluxElements().collectList().block());
    }

    @Test
    void toFluxElementsOnNullValueIsEmpty() {
        Result<List<String>> r = Result.ok(null);
        List<String> out = r.<String>toFluxElements().collectList().block();
        assertThat(out).isEmpty();
    }

    @Test
    void toFluxElementsOnNonCollectionValueEmitsError() {
        Result<Object> r = Result.ok(new Object());
        Flux<Object> flux = r.toFluxElements();
        assertThrows(IllegalStateException.class, () -> flux.collectList().block());
    }
}

