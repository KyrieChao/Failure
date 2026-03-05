package com.chao.failfast.internal.chain;

import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.MultiBusiness;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ChainTerminator 终结操作测试")
class ChainTerminatorTest {

    // 辅助类，用于测试接口默认方法
    static class TestTerminator extends ChainCore<TestTerminator> implements ChainTerminator<TestTerminator> {
        protected TestTerminator(boolean failFast) {
            super(failFast, null);
        }

        public static TestTerminator create(boolean failFast) {
            return new TestTerminator(failFast);
        }

        @Override
        public TestTerminator core() {
            return this;
        }

        // 辅助方法：手动添加错误
        public TestTerminator addError(ResponseCode code) {
            return check(false, code, null);
        }

        // 辅助方法：手动设置状态
        public TestTerminator setAlive(boolean alive) {
            this.alive = alive;
            return this;
        }
    }

    @Test
    @DisplayName("fail: 验证通过时不应抛出异常")
    void failShouldPassWhenValid() {
        TestTerminator chain = TestTerminator.create(true);
        chain.fail(); // Should pass
    }

    @Test
    @DisplayName("fail: 验证失败时应抛出 Business 异常")
    void failShouldThrowWhenInvalid() {
        TestTerminator chain = TestTerminator.create(true);
        chain.addError(ResponseCode.of(400, "Error"));

        assertThatThrownBy(chain::fail)
                .isInstanceOf(Business.class)
                .extracting(e -> ((Business) e).getResponseCode().getCode())
                .isEqualTo(400);
    }

    @Test
    @DisplayName("fail: 验证失败但无原因时应抛出通用异常")
    void failShouldThrowGenericWhenNoCauses() {
        TestTerminator chain = TestTerminator.create(true);
        chain.setAlive(false); // 强制设置为失败，但没有添加错误

        assertThatThrownBy(chain::fail)
                .isInstanceOf(Business.class)
                .extracting(e -> ((Business) e).getResponseCode().getCode())
                .isEqualTo(500);
    }

    @Test
    @DisplayName("failAll: 多个错误时应抛出 MultiBusiness")
    void failAllShouldThrowMultiBusiness() {
        TestTerminator chain = TestTerminator.create(false); // Strict mode
        chain.addError(ResponseCode.of(400, "Error 1"));
        chain.addError(ResponseCode.of(401, "Error 2"));

        assertThatThrownBy(chain::failAll)
                .isInstanceOf(MultiBusiness.class)
                .extracting(e -> ((MultiBusiness) e).getErrors().size())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("failAll: 单个错误时应抛出 Business")
    void failAllShouldThrowBusinessForSingleError() {
        TestTerminator chain = TestTerminator.create(false);
        chain.addError(ResponseCode.of(400, "Error 1"));

        assertThatThrownBy(chain::failAll)
                .isInstanceOf(Business.class)
                .isNotInstanceOf(MultiBusiness.class);
    }

    @Test
    @DisplayName("failNow(Code): 当活跃时返回自身，不活跃时抛出")
    void failNowCode() {
        TestTerminator chain = TestTerminator.create(true);

        // Alive -> returns self
        assertThat(chain.failNow(ResponseCode.of(500, "Error"))).isSameAs(chain);

        // Not alive -> throws
        chain.setAlive(false);
        assertThatThrownBy(() -> chain.failNow(ResponseCode.of(500, "Error")))
                .isInstanceOf(Business.class);
    }

    @Test
    @DisplayName("onFail: 仅在失败时执行")
    void onFail() {
        TestTerminator chain = TestTerminator.create(true);
        AtomicBoolean executed = new AtomicBoolean(false);

        // Valid -> not executed
        chain.onFail(() -> executed.set(true));
        assertThat(executed.get()).isFalse();

        // Invalid -> executed
        chain.setAlive(false);
        chain.onFail(() -> executed.set(true));
        assertThat(executed.get()).isTrue();
    }

    @Test
    @DisplayName("onFailGet: 仅在失败时返回值")
    void onFailGet() {
        TestTerminator chain = TestTerminator.create(true);

        // Valid -> empty
        assertThat(chain.onFailGet(() -> "Value")).isEmpty();

        // Invalid -> present
        chain.setAlive(false);
        Optional<String> result = chain.onFailGet(() -> "Value");
        assertThat(result).isPresent().contains("Value");
    }
}
