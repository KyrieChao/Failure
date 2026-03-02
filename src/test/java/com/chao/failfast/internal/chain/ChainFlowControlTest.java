package com.chao.failfast.internal.chain;

import com.chao.failfast.internal.Chain;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Chain Flow Control Test (when/skip)")
class ChainFlowControlTest {

    @Test
    @DisplayName("when(false) 应该跳过后续校验")
    void whenFalseShouldSkipChecks() {
        Chain chain = Chain.begin(true);

        chain.when(false)
                .isTrue(false) // 应该被跳过
                .notNull(null); // 应该被跳过

        assertThat(chain.isValid()).isTrue();
        assertThat(chain.getCauses()).isEmpty();
    }

    @Test
    @DisplayName("when(true) 应该恢复后续校验")
    void whenTrueShouldEnableChecks() {
        Chain chain = Chain.begin(true);

        chain.when(false)
                .isTrue(false) // 跳过
                .when(true)
                .isTrue(true); // 执行且通过

        assertThat(chain.isValid()).isTrue();
    }

    @Test
    @DisplayName("when(true) 恢复后，失败应该被捕获")
    void whenTrueShouldCatchFailure() {
        Chain chain = Chain.begin(true);

        chain.when(false)
                .isTrue(false) // 跳过
                .when(true)
                .isTrue(false, ResponseCode.of(400, "Should fail")); // 执行且失败

        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses()).hasSize(1);
        assertThat(chain.getCauses().get(0).getResponseCode().getMessage()).isEqualTo("Should fail");
    }

    @Test
    @DisplayName("when(false) 也应该跳过 or()")
    void whenFalseShouldSkipOr() {
        Chain chain = Chain.begin(true);

        // A (fail) -> when(false) -> or() -> B (fail)
        // 如果 or() 被跳过，那么 B 只是一个普通的 check。
        // 由于 when(false)，B 也被跳过。
        // 所以结果应该是 isValid=false (A 导致的)。
        
        // Wait: 
        // .isTrue(false) -> alive=false
        // .when(false)
        // .or() -> skipped
        // .isTrue(false) -> skipped
        
        chain.isTrue(false, ResponseCode.of(400, "Error A"))
                .when(false)
                .or()
                .isTrue(false, ResponseCode.of(400, "Error B"));
        
        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses()).hasSize(1);
        assertThat(chain.getCauses().get(0).getResponseCode().getMessage()).isEqualTo("Error A");
    }

    @Test
    @DisplayName("when(true) 结合 or() 正常工作")
    void whenTrueShouldWorkWithOr() {
        Chain chain = Chain.begin(true);

        // A (fail) -> or() -> B (success) -> Valid
        chain.isTrue(false)
             .or()
             .when(true) // Default is true, but explicit shouldn't hurt
             .isTrue(true);

        assertThat(chain.isValid()).isTrue();
    }
}
