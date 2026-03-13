package com.chao.failfast.internal.policy;

import com.chao.failfast.config.CodeMappingConfig;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.Chain;
import com.chao.failfast.internal.Ex;
import com.chao.failfast.internal.FailureContext;
import com.chao.failfast.internal.core.FailureProperties;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErrorPolicyTest {

    private FailureContext original;

    @BeforeEach
    void setUp() {
        original = Ex.getContext();
    }

    @AfterEach
    void tearDown() {
        Ex.setContext(original);
    }

    @Test
    void shouldUseDefaultPolicyWhenNullProvided() {
        FailureProperties props = new FailureProperties();
        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        assertThat(ctx.getErrorPolicy()).isSameAs(DefaultErrorPolicy.INSTANCE);
    }

    @Test
    void shouldUsePolicyForDefaultDetail() {
        FailureProperties props = new FailureProperties();
        ErrorPolicy policy = new ErrorPolicy() {
            @Override
            public ResponseCode defaultCode() {
                return ResponseCode.of(500, "m", "d");
            }

            @Override
            public String defaultDetail(ResponseCode code) {
                return "custom-detail";
            }

            @Override
            public boolean captureInvalidValue(FailureContext context) {
                return true;
            }
        };

        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), policy);
        Ex.setContext(ctx);

        Business ex = Business.of(ResponseCode.of(400, "m"));
        assertThat(ex.getDetail()).isEqualTo("custom-detail");
    }

    @Test
    void shouldUsePolicyDefaultCodeInChainCore() {
        FailureProperties props = new FailureProperties();
        ErrorPolicy policy = new ErrorPolicy() {
            @Override
            public ResponseCode defaultCode() {
                return ResponseCode.of(400, "m", "d");
            }

            @Override
            public String defaultDetail(ResponseCode code) {
                return null;
            }

            @Override
            public boolean captureInvalidValue(FailureContext context) {
                return false;
            }
        };

        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), policy);
        Ex.setContext(ctx);

        Business ex = assertThrows(Business.class, () -> Chain.begin(true).check(false, null, null, "x").fail());
        assertThat(ex.getResponseCode().getCode()).isEqualTo(400);
        assertThat(ex.getInvalidValue()).isNull();
    }
}

