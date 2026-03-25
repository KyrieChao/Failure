package com.chao.failfast.internal.chain;

import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.Chain;
import com.chao.failfast.internal.chain.pipeline.CheckSpec;
import com.chao.failfast.internal.core.ResponseCode;
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

