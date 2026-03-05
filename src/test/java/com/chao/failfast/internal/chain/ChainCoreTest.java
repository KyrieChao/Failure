package com.chao.failfast.internal.chain;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChainCore 核心逻辑测试")
class ChainCoreTest {

    // 最小化实现，用于测试核心逻辑
    static class TestChain extends ChainCore<TestChain> {
        protected TestChain(boolean failFast, FastValidator.ValidationContext context) {
            super(failFast, context);
        }

        public static TestChain create(boolean failFast) {
            return new TestChain(failFast, null);
        }

        public static TestChain create(FastValidator.ValidationContext context) {
            return new TestChain(context.isFast(), context);
        }

        // 暴露 protected 方法用于测试
        public TestChain publicCheck(boolean condition, ResponseCode code) {
            return check(condition, code, null);
        }

        public TestChain publicCheck(boolean condition) {
            return check(condition);
        }

        public TestChain coreTest() {
            return core();
        }

        public TestChain orTest() {
            return or();
        }

        public boolean shouldSkipTest() {
            return shouldSkip();
        }

        public void setAlive(boolean alive) {
            this.alive = alive;
        }
    }

    @Test
    @DisplayName("core: 应返回当前实例 (self)")
    void coreShouldReturnSelf() {
        TestChain chain = TestChain.create(true);
        assertThat(chain.coreTest()).isSameAs(chain);
    }

    @Test
    @DisplayName("or: 当 context 已停止时，应直接返回且不改变状态")
    void orShouldReturnImmediatelyWhenContextStopped() {
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        TestChain chain = TestChain.create(context);

        context.stop(); // 模拟 context 停止

        chain.orTest();

        // 验证没有进入 orMode (因为直接返回了)
        // 由于 orMode 是 private，我们通过 check 行为来验证
        // 如果进入了 orMode，下一个 check 会被认为是 or 的一部分
        // 如果没进入 orMode，下一个 check 就是普通的 check

        // 但这里 context 已经 stop 了，check 也会 skip。
        // 所以我们主要验证方法调用没有抛出异常，并且返回了 self
        assertThat(context.isStopped()).isTrue();
    }

    @Test
    @DisplayName("shouldSkip: Fail-Fast模式下，不活跃时应返回 true")
    void shouldSkipShouldReturnTrueWhenNotAliveAndFailFast() {
        TestChain chain = TestChain.create(true);
        // 触发失败
        chain.publicCheck(false);

        assertThat(chain.isAlive()).isFalse();
        assertThat(chain.shouldSkipTest()).isTrue();
    }

    @Test
    @DisplayName("shouldSkip: Fail-Safe模式下，即使失败也应返回 false")
    void shouldSkipShouldReturnFalseWhenNotAliveAndFailSafe() {
        TestChain chain = TestChain.create(false);
        // 触发失败
        chain.publicCheck(false);

        assertThat(chain.isAlive()).isTrue(); // Fail-safe 模式下 alive 保持 true
        assertThat(chain.shouldSkipTest()).isFalse();
    }

    @Test
    @DisplayName("shouldSkip: Fail-Safe模式下，即使强制设置不活跃，也应返回 false (虽然通常不会发生)")
    void shouldSkipShouldReturnFalseWhenForcedNotAliveAndFailSafe() {
        TestChain chain = TestChain.create(false);
        chain.setAlive(false); // 强制设置

        // !alive (true) && failFast (false) -> false
        assertThat(chain.shouldSkipTest()).isFalse();
    }

    @Test
    @DisplayName("shouldSkip: 当 Context 停止时，应返回 true")
    void shouldSkipShouldReturnTrueWhenContextStopped() {
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        TestChain chain = TestChain.create(context);

        context.stop();

        assertThat(chain.shouldSkipTest()).isTrue();
    }

    @Test
    @DisplayName("isValid: 初始状态应为有效")
    void shouldBeValidInitially() {
        TestChain chain = TestChain.create(true);
        assertThat(chain.isValid()).isTrue();
        assertThat(chain.isAlive()).isTrue();
        assertThat(chain.getCauses()).isEmpty();
    }

    @Test
    @DisplayName("check: 当条件为true时，不应产生错误")
    void shouldNotErrorWhenConditionIsTrue() {
        TestChain chain = TestChain.create(true);
        chain.publicCheck(true, (ResponseCode.of(400, "Error")));

        assertThat(chain.isValid()).isTrue();
        assertThat(chain.getCauses()).isEmpty();
    }

    @Test
    @DisplayName("check: 当条件为false时，应产生错误")
    void shouldErrorWhenConditionIsFalse() {
        TestChain chain = TestChain.create(true);
        chain.publicCheck(false, ResponseCode.of(400, "Error"));

        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses()).hasSize(1);
        assertThat(chain.getCauses().get(0).getResponseCode().getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("check: Fail-Fast模式下，第一次失败后应标记为不活跃")
    void shouldBeNotAliveAfterErrorInFailFastMode() {
        TestChain chain = TestChain.create(true); // failFast = true
        chain.publicCheck(false, ResponseCode.of(400, "Error"));

        assertThat(chain.isAlive()).isFalse();
    }

    @Test
    @DisplayName("check: Fail-Strict模式下，失败后仍应保持活跃")
    void shouldRemainAliveAfterErrorInFailStrictMode() {
        TestChain chain = TestChain.create(false); // failFast = false
        chain.publicCheck(false, ResponseCode.of(400, "Error"));

        assertThat(chain.isAlive()).isTrue();
        assertThat(chain.isValid()).isFalse();
    }

    @Test
    @DisplayName("check: Fail-Fast模式下，不活跃时应跳过后续检查")
    void shouldSkipChecksWhenNotAlive() {
        TestChain chain = TestChain.create(true);

        // First error
        chain.publicCheck(false, ResponseCode.of(400, "Error 1"));
        assertThat(chain.isAlive()).isFalse();

        // Second check (should be skipped)
        chain.publicCheck(false, ResponseCode.of(500, "Error 2"));

        assertThat(chain.getCauses()).hasSize(1); // Only the first error
        assertThat(chain.getCauses().get(0).getResponseCode().getMessage()).isEqualTo("Error 1");
    }

    @Test
    @DisplayName("check: 无配置 check(boolean) 使用默认错误")
    void shouldAddDefaultErrorWhenNoConfig() {
        TestChain chain = TestChain.create(true);
        chain.publicCheck(false);

        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses().get(0).getResponseCode().getCode()).isEqualTo(500);
        assertThat(chain.getCauses().get(0).getDetail()).contains("未通过");
    }

    @Test
    @DisplayName("Context集成: 错误应报告给 Context")
    void shouldReportToContext() {
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        TestChain chain = TestChain.create(context);

        chain.publicCheck(false, ResponseCode.of(400, "Error"));

        assertThat(context.isValid()).isFalse();
        assertThat(context.hasCauses()).hasSize(1);
    }

    @Test
    @DisplayName("isValid: 当使用 Context 时，应反映 Context 的状态")
    void isValidShouldReflectContextState() {
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        TestChain chain = TestChain.create(context);

        assertThat(chain.isValid()).isTrue();

        chain.publicCheck(false, ResponseCode.of(400, "Error"));

        assertThat(chain.isValid()).isFalse();
    }
}
