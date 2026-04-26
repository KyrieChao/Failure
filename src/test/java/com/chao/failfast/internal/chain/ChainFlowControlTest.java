package com.chao.failfast.internal.chain;

import com.chao.failfast.condition.Predicate;
import com.chao.failfast.internal.core.Chain;
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
    @DisplayName("when(ValidationCondition) 应支持组合条件 (如 (A||B)&&(C||D))")
    void whenConditionShouldSupportComplexLogic() {
        Chain chain = Chain.begin(true);

        boolean a = false;
        boolean b = true;
        boolean c = false;
        boolean d = false;

        Predicate condition = Predicate
                .anyOf(a, b)
                .and(Predicate.anyOf(c, d));

        chain.when(condition)
                .isTrue(false, ResponseCode.of(400, "Should be skipped"));

        assertThat(chain.isValid()).isTrue();
        assertThat(chain.getCauses()).isEmpty();
    }

    @Test
    @DisplayName("when(ValidationCondition) 条件为 true 时应执行后续校验")
    void whenConditionTrueShouldExecuteChecks() {
        Chain chain = Chain.begin(true);

        Predicate condition = Predicate.anyOf(false, true).and(true);

        chain.when(condition)
                .isTrue(false, ResponseCode.of(400, "Should fail"));

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

    @Test
    @DisplayName("strict模式下 stopOnFail 应该避免后续 NPE (配合 defer)")
    void stopOnFailShouldPreventNPEInStrictMode() {
        Chain chain = Chain.begin(false); // failFast = false

        String role = null;

        // 模拟用户场景：
        // 1. role != null (Fail)
        // 2. stopOnFail() (Should stop)
        // 3. defer(() -> role.equals("ADMIN")) (Should be skipped, avoiding NPE)

        chain.notNull(role, ResponseCode.of(400, "Role is null"))
                .stopOnFail()
                .defer(() -> role.equals("ADMIN"), ResponseCode.of(403, "Not Admin"));

        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses()).hasSize(1);
        assertThat(chain.getCauses().get(0).getDetail()).isEqualTo("Role is null");
    }

    @Test
    @DisplayName("stopOnFail 在没有错误时应该继续")
    void stopOnFailShouldContinueWhenValid() {
        Chain chain = Chain.begin(false);

        chain.isTrue(true)
                .stopOnFail()
                .isTrue(true);

        assertThat(chain.isValid()).isTrue();
    }

    @Test
    @DisplayName("resume 应该能恢复被 stopOnFail 停止的链")
    void resumeShouldRecoverFromStopOnFail() {
        Chain chain = Chain.begin(false);

        chain.isTrue(false, ResponseCode.of(400, "Error 1"))
                .stopOnFail()
                .isTrue(false, ResponseCode.of(400, "Error 2 (Skipped)"))
                .resume()
                .isTrue(false, ResponseCode.of(400, "Error 3"));

        assertThat(chain.getCauses()).hasSize(2);
        assertThat(chain.getCauses().get(0).getMessage()).isEqualTo("Error 1");
        assertThat(chain.getCauses().get(1).getMessage()).isEqualTo("Error 3");
    }
}
