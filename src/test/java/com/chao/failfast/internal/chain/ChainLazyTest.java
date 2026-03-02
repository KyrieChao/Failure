package com.chao.failfast.internal.chain;

import com.chao.failfast.internal.Chain;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Chain Lazy Check Test (satisfies/check(Supplier))")
class ChainLazyTest {

    @Test
    @DisplayName("satisfies: 应执行并验证条件")
    void shouldExecuteLazyCheck() {
        Chain chain = Chain.begin(true);
        AtomicBoolean executed = new AtomicBoolean(false);

        chain.defer(() -> {
            executed.set(true);
            return true;
        });

        assertThat(executed).isTrue();
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    @DisplayName("satisfies: 失败时应记录错误")
    void shouldReportFailure() {
        Chain chain = Chain.begin(true);

        chain.defer(() -> false, ResponseCode.of(400, "Lazy Fail"),"demo");
        chain.defer(() -> false, ResponseCode.of(400, "Lazy Fail"));

        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses()).hasSize(1);
        assertThat(chain.getCauses().get(0).getResponseCode().getMessage()).isEqualTo("Lazy Fail");
    }

    @Test
    @DisplayName("satisfies: 当链已经 Fail-Fast 时，Supplier 不应被执行")
    void shouldNotExecuteWhenChainFailed() {
        Chain chain = Chain.begin(true);
        AtomicBoolean executed = new AtomicBoolean(false);

        // 第一次失败
        chain.isTrue(false);
        assertThat(chain.isAlive()).isFalse();

        // 这里的 Supplier 不应该被执行
        chain.defer(() -> {
            executed.set(true);
            return true;
        });

        assertThat(executed).isFalse();
    }

    @Test
    @DisplayName("satisfies: 当被 when(false) 跳过时，Supplier 不应被执行")
    void shouldNotExecuteWhenSkipped() {
        Chain chain = Chain.begin(true);
        AtomicBoolean executed = new AtomicBoolean(false);

        chain.when(false)
             .defer(() -> {
                 executed.set(true);
                 return true;
             });

        assertThat(executed).isFalse();
    }
}
