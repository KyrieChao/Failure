package com.chao.failfast.internal;

import com.chao.failfast.config.CodeMappingConfig;
import com.chao.failfast.internal.core.FailureProperties;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvalidValueCaptureTest {

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
    void shouldNotCaptureInvalidValueWhenDebugSnapshotDisabled() {
        FailureProperties props = new FailureProperties();
        props.setDebugSnapshot(false);
        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(ctx);

        Business ex = assertThrows(Business.class, () -> Chain.begin(true)
                .check(false, ResponseCode.of(400, "m"), "d", "secret")
                .fail());

        assertThat(ex.getInvalidValue()).isNull();
        assertThat(ex.toString()).doesNotContain("val=");
    }

    @Test
    void shouldCaptureInvalidValueWhenDebugSnapshotEnabled() {
        FailureProperties props = new FailureProperties();
        props.setDebugSnapshot(true);
        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(ctx);

        Business ex = assertThrows(Business.class, () -> Chain.begin(true)
                .check(false, ResponseCode.of(400, "m"), "d", "secret")
                .fail());

        assertThat(ex.getInvalidValue()).isEqualTo("secret");
        assertThat(ex.toString()).contains("val=");
    }

    @Test
    void shouldCaptureInvalidValueWhenNoContext() {
        Ex.setContext(null);

        Business ex = assertThrows(Business.class, () -> Chain.begin(true)
                .check(false, ResponseCode.of(400, "m"), "d", "secret")
                .fail());

        assertThat(ex.getInvalidValue()).isEqualTo("secret");
    }

    @Test
    void shouldOnlyComputeInvalidValueOnFailureWhenSupplierUsed() {
        FailureProperties props = new FailureProperties();
        props.setDebugSnapshot(true);
        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(ctx);

        AtomicInteger calls = new AtomicInteger();
        Chain.begin(true).check(true, ResponseCode.of(400, "m"), "d", calls::incrementAndGet);
        assertThat(calls.get()).isEqualTo(0);

        Business ex = assertThrows(Business.class, () -> Chain.begin(true)
                .check(false, ResponseCode.of(400, "m"), "d", calls::incrementAndGet)
                .fail());
        assertThat(calls.get()).isEqualTo(1);
        assertThat(ex.getInvalidValue()).isEqualTo(1);
    }
}

