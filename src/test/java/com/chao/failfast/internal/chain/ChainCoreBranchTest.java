package com.chao.failfast.internal.chain;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.config.CodeMappingConfig;
import com.chao.failfast.i18n.I18nExtension;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.Ex;
import com.chao.failfast.internal.FailureContext;
import com.chao.failfast.internal.core.FailureProperties;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.internal.policy.ErrorPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(I18nExtension.class)
class ChainCoreBranchTest {

    static class TestChain extends ChainCore<TestChain> {
        TestChain(boolean failFast, ValidationContext context) {
            super(failFast, context);
        }
    }

    private FailureContext original;

    @BeforeEach
    void setUp() {
        original = Ex.getContext();
        Ex.setContext(null);
    }

    @AfterEach
    void tearDown() {
        Ex.setContext(original);
    }

    @Test
    void shouldSkipWhenConditionStateFalseForSupplierCheck() {
        TestChain chain = new TestChain(true, null);
        chain.when(false);

        AtomicInteger calls = new AtomicInteger();
        chain.check(() -> {
            calls.incrementAndGet();
            return false;
        }, ResponseCode.of(400, "m"), "d");

        assertThat(calls.get()).isEqualTo(0);
        assertThat(chain.getCauses()).isEmpty();
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void shouldSkipWhenContextStoppedForSupplierCheck() {
        ValidationContext ctx = new ValidationContext(true);
        ctx.stop();
        TestChain chain = new TestChain(true, ctx);

        AtomicInteger calls = new AtomicInteger();
        chain.check(() -> {
            calls.incrementAndGet();
            return false;
        }, ResponseCode.of(400, "m"), "d");

        assertThat(calls.get()).isEqualTo(0);
        assertThat(chain.getCauses()).isEmpty();
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void shouldSkipWhenNotAliveAndFailFast() {
        TestChain chain = new TestChain(true, null);
        chain.check(false, ResponseCode.of(400, "m"), "d", "v");
        assertThat(chain.isAlive()).isFalse();

        AtomicInteger calls = new AtomicInteger();
        chain.check(() -> {
            calls.incrementAndGet();
            return true;
        }, ResponseCode.of(400, "m2"), "d2");

        assertThat(calls.get()).isEqualTo(0);
    }

    @Test
    void shouldFallbackWhenSpecNull() {
        TestChain chain = new TestChain(true, null);
        chain.check(false, (CheckSpec) null);

        assertThat(chain.getCauses()).hasSize(1);
        Business ex = chain.getCauses().get(0);
        assertThat(ex.getResponseCode()).isEqualTo(ResponseCode.VALIDATION_ERROR_500_DYNAMIC);
    }

    @Test
    void shouldHandleOrModeBothFailAndReportRightErrorOnly() {
        TestChain chain = new TestChain(true, null);
        chain.check(false, ResponseCode.of(400, "left"), "L", "lv");
        assertThat(chain.isAlive()).isFalse();

        chain.or();
        chain.check(false, ResponseCode.of(401, "right"), "R", "rv");

        assertThat(chain.getCauses()).hasSize(1);
        Business ex = chain.getCauses().get(0);
        assertThat(ex.getResponseCode().getCode()).isEqualTo(401);
        assertThat(ex.getDetail()).isEqualTo("R");
        assertThat(ex.getInvalidValue()).isEqualTo("rv");
    }

    @Test
    void shouldHandleOrModeLeftFailRightSuccessAndClearErrors() {
        TestChain chain = new TestChain(true, null);
        chain.check(false, ResponseCode.of(400, "left"), "L", "lv");
        chain.or();
        chain.check(true, ResponseCode.of(401, "right"), "R", "rv");

        assertThat(chain.isAlive()).isTrue();
        assertThat(chain.getCauses()).isEmpty();
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void shouldHandleOrModeLeftSuccessRightFailAndPass() {
        TestChain chain = new TestChain(true, null);
        chain.check(true, ResponseCode.of(400, "left"), "L", "lv");
        chain.or();
        chain.check(false, ResponseCode.of(401, "right"), "R", "rv");

        assertThat(chain.getCauses()).isEmpty();
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void shouldNotCaptureInvalidValueWhenPolicyDisablesIt() {
        FailureContext ctx = getFailureContext();
        Ex.setContext(ctx);

        AtomicInteger calls = new AtomicInteger();
        TestChain chain = new TestChain(true, null);
        chain.check(false, ResponseCode.of(400, "m"), "d", () -> {
            calls.incrementAndGet();
            return "secret";
        });

        assertThat(calls.get()).isEqualTo(0);
        assertThat(chain.getCauses()).hasSize(1);
        assertThat(chain.getCauses().get(0).getInvalidValue()).isNull();
    }

    private static FailureContext getFailureContext() {
        FailureProperties props = new FailureProperties();
        props.setDebugSnapshot(true);
        ErrorPolicy policy = new ErrorPolicy() {
            @Override
            public ResponseCode defaultCode() {
                return ResponseCode.VALIDATION_ERROR_500_DYNAMIC;
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
        return new FailureContext(props, new CodeMappingConfig(props), policy);
    }

    @Test
    void shouldCaptureInvalidValueWhenContextIsNullWithSupplier() {
        Ex.setContext(null);
        AtomicInteger calls = new AtomicInteger();

        TestChain chain = new TestChain(true, null);
        chain.check(false, ResponseCode.of(400, "m"), "d", () -> {
            calls.incrementAndGet();
            return "secret";
        });

        assertThat(calls.get()).isEqualTo(1);
        assertThat(chain.getCauses()).hasSize(1);
        assertThat(chain.getCauses().get(0).getInvalidValue()).isEqualTo("secret");
    }

    @Test
    void shouldIncludeAliveFlagInContextualIsValid() {
        ValidationContext vctx = new ValidationContext(true);
        TestChain chain = new TestChain(true, vctx);
        assertThat(chain.isValid()).isTrue();

        chain.alive = false;
        assertThat(chain.isValid()).isFalse();
    }

    @Test
    void stopOnFailShouldReturnSelfWhenConditionStateAlreadyFalse() {
        TestChain chain = new TestChain(true, null);
        chain.when(false);
        TestChain returned = chain.stopOnFail();

        AtomicInteger calls = new AtomicInteger();
        returned.check(() -> {
            calls.incrementAndGet();
            return true;
        }, ResponseCode.of(400, "m"), "d");

        assertThat(returned).isSameAs(chain);
        assertThat(calls.get()).isEqualTo(0);
    }

    @Test
    void stopOnFailShouldDisableFurtherChecksWhenInvalid() {
        TestChain chain = new TestChain(false, null);
        chain.check(false, ResponseCode.of(400, "m"), "d");
        chain.stopOnFail();

        AtomicInteger calls = new AtomicInteger();
        chain.check(() -> {
            calls.incrementAndGet();
            return true;
        }, ResponseCode.of(400, "m2"), "d2");

        assertThat(calls.get()).isEqualTo(0);
    }
}

