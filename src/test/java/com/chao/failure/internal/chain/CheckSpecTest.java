package com.chao.failure.internal.chain;

import com.chao.failure.exception.Business;
import com.chao.failure.internal.core.Chain;
import com.chao.failure.internal.chain.pipeline.CheckSpec;
import com.chao.failure.internal.core.ResponseCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckSpecTest {

    @Test
    void shouldUseCheckSpecOverload() {
        Business ex = assertThrows(Business.class, () -> Chain.begin(true)
                .check(false, CheckSpec.of(ResponseCode.of(400, "m"), "detail", "v"))
                .fail());
        assertThat(ex.getDetail()).isEqualTo("detail");
    }

    @Test
    void shouldEvaluateSupplierCheckSpec() {
        Chain c = Chain.begin(true);
        c.check(() -> true, CheckSpec.of(ResponseCode.of(400, "m"), "detail"));
        assertThat(c.isValid()).isTrue();
    }
}

