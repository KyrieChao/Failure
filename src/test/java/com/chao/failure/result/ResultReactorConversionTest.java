package com.chao.failure.result;

import com.chao.failure.internal.core.ResponseCode;
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
        Result<List<String>> r = Result.success(null);
        List<String> out = r.<String>toFluxElements().collectList().block();
        assertThat(out).isEmpty();
    }

    @Test
    void toFluxElementsOnNonCollectionValueEmitsError() {
        Result<Object> r = Result.success(new Object());
        Flux<Object> flux = r.toFluxElements();
        assertThrows(IllegalStateException.class, () -> flux.collectList().block());
    }
}

