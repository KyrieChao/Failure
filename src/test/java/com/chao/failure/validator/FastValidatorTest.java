package com.chao.failure.validator;

import com.chao.failure.Failure;
import com.chao.failure.constant.Scenario;
import com.chao.failure.exception.Business;
import com.chao.failure.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FastValidator 接口测试")
class FastValidatorTest {

    // 测试用的验证器实现
    static class StringValidator implements FastValidator<String> {
        @Override
        public void validate(String target, ValidationContext context) {
            if (target == null) {
                context.reportError(ResponseCode.of(40000, "不能为空"));
                return;
            }

            if (!context.isValid() || context.isStopped()) {
                return;
            }

            if (target.isEmpty()) {
                context.reportError(ResponseCode.of(40001, "不能为空字符串"));
            }
            if (target.length() > 10) {
                context.reportError(ResponseCode.of(40002, "长度不能超过10"));
            }
        }
    }

    static class IntegerValidator implements FastValidator<Integer> {
        @Override
        public void validate(Integer target, ValidationContext context) {
            if (target == null) {
                context.reportError(ResponseCode.of(40010, "数字不能为空"));
                return;
            }
            if (target < 0) {
                context.reportError(ResponseCode.of(40011, "数字不能为负数"));
            }
        }

        @Override
        public Class<?> getSupportedType() {
            return Integer.class;
        }
    }

    static abstract class BaseUserDTO {
        abstract String getId();
    }

    static final class AdminUserDTO extends BaseUserDTO {
        private final String id;
        private final Integer adminLevel;

        AdminUserDTO(String id, Integer adminLevel) {
            this.id = id;
            this.adminLevel = adminLevel;
        }

        @Override
        String getId() {
            return id;
        }

        Integer getAdminLevel() {
            return adminLevel;
        }
    }

    static class BaseUserValidator<T extends BaseUserDTO> extends TemplateValidator<T> {
        @Override
        protected void validateCommon(T target, ValidationContext context) {
            Failure.with(context).notNull(target.getId()).verify();
        }

        @Override
        protected void validateSpecific(T target, ValidationContext context) {
        }
    }

    static final class AdminUserValidator extends BaseUserValidator<AdminUserDTO> {
        private final AtomicInteger called = new AtomicInteger();

        @Override
        protected void validateSpecific(AdminUserDTO user, ValidationContext context) {
            called.incrementAndGet();
            Failure.with(context).notNull(user.getAdminLevel()).verify();
        }

        int called() {
            return called.get();
        }
    }

    @Nested
    @DisplayName("FastValidator 接口方法测试")
    class InterfaceTest {

        @Test
        @DisplayName("validate 方法应正确执行验证")
        void shouldValidateCorrectly() {
            StringValidator validator = new StringValidator();
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);

            validator.validate("valid", ctx);
            assertThat(ctx.isValid()).isTrue();
        }

        @Test
        @DisplayName("getSupportedType 默认实现应返回 Object.class")
        void shouldReturnObjectClassAsDefault() {
            StringValidator validator = new StringValidator();
            assertThat(validator.getSupportedType()).isEqualTo(Object.class);
        }

        @Test
        @DisplayName("自定义 getSupportedType 应返回指定类型")
        void shouldReturnCustomType() {
            IntegerValidator validator = new IntegerValidator();
            assertThat(validator.getSupportedType()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("TemplateValidator 应能从泛型推断 supportedType")
        void templateValidatorShouldResolveSupportedType() {
            AdminUserValidator validator = new AdminUserValidator();
            assertThat(validator.getSupportedType()).isEqualTo(AdminUserDTO.class);
        }
    }

    @Nested
    @DisplayName("ValidationContext - 构造和基本属性测试")
    class ValidationContextBasicTest {

        @Test
        @DisplayName("fast 模式应为 true")
        void shouldBeFastMode() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(true);
            assertThat(ctx.isFast()).isTrue();
        }

        @Test
        @DisplayName("非 fast 模式应为 false")
        void shouldNotBeFastMode() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
            assertThat(ctx.isFast()).isFalse();
        }

        @Test
        @DisplayName("初始状态group")
        void shouldHaveDefaultGroups() {

            interface Create{

            }
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false, Scenario.DEFAULT, new Class<?>[]{Create.class});
            assertThat(ctx.isFast()).isFalse();
            assertThat(ctx.getScenes()).containsExactly(Scenario.DEFAULT);
            assertThat(ctx.getGroups()).containsExactly(Create.class);
        }
        @Test
        @DisplayName("初始状态应为有效")
        void shouldBeValidInitially() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
            assertThat(ctx.isValid()).isTrue();
            assertThat(ctx.isStopped()).isFalse();
        }
    }

    @Nested
    @DisplayName("ValidationContext - reportError 测试")
    class ReportErrorTest {

        @Test
        @DisplayName("reportError(ResponseCode) 应添加错误")
        void shouldAddErrorWithCode() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);

            ctx.reportError(ResponseCode.of(40000, "错误"));

            assertThat(ctx.isValid()).isFalse();
            assertThat(ctx.hasCauses()).hasSize(1);
        }

        @Test
        @DisplayName("reportError(ResponseCode, detail) 应添加带详情的错误")
        void shouldAddErrorWithCodeAndDetail() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);

            ctx.reportError(ResponseCode.of(40000, "错误"), "详细错误信息");

            assertThat(ctx.isValid()).isFalse();
            List<Business> errors = ctx.hasCauses();
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).getDetail()).isEqualTo("详细错误信息");
        }

        @Test
        @DisplayName("reportError(Business) 应添加 Business 错误")
        void shouldAddBusinessError() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
            Business error = Business.of(ResponseCode.of(40000, "错误"));

            ctx.reportError(error);

            assertThat(ctx.hasCauses()).hasSize(1);
            assertThat(ctx.getFirstError()).isEqualTo(error);
        }

        @Test
        @DisplayName("fast 模式下第一个错误后应停止")
        void shouldStopAfterFirstErrorInFastMode() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(true);

            ctx.reportError(ResponseCode.of(40000, "第一个错误"));
            ctx.reportError(ResponseCode.of(40001, "第二个错误")); // 应该被忽略

            assertThat(ctx.isStopped()).isTrue();
            assertThat(ctx.hasCauses()).hasSize(1);
        }

        @Test
        @DisplayName("非 fast 模式下应收集所有错误")
        void shouldCollectAllErrorsInNonFastMode() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);

            ctx.reportError(ResponseCode.of(40000, "第一个错误"));
            ctx.reportError(ResponseCode.of(40001, "第二个错误"));

            assertThat(ctx.isStopped()).isFalse();
            assertThat(ctx.hasCauses()).hasSize(2);
        }

        @Test
        @DisplayName("stopped 状态下不应添加错误")
        void shouldNotAddErrorWhenStopped() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
            ctx.stop();

            ctx.reportError(ResponseCode.of(40000, "错误"));

            assertThat(ctx.hasCauses()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ValidationContext - stop 方法测试")
    class StopTest {

        @Test
        @DisplayName("stop 应设置 stopped 为 true")
        void shouldSetStoppedToTrue() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);

            ctx.stop();

            assertThat(ctx.isStopped()).isTrue();
        }

        @Test
        @DisplayName("手动 stop 后 reportError 应被忽略")
        void shouldIgnoreErrorAfterManualStop() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
            ctx.stop();

            ctx.reportError(ResponseCode.of(40000, "错误"));

            assertThat(ctx.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("ValidationContext - isValid 方法测试")
    class IsValidTest {

        @Test
        @DisplayName("无错误时应返回 true")
        void shouldReturnTrueWhenNoErrors() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
            assertThat(ctx.isValid()).isTrue();
        }

        @Test
        @DisplayName("有错误时应返回 false")
        void shouldReturnFalseWhenHasErrors() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
            ctx.reportError(ResponseCode.of(40000, "错误"));
            assertThat(ctx.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("ValidationContext - getErrors 方法测试")
    class GetErrorsTest {

        @Test
        @DisplayName("应返回不可修改的错误列表")
        void shouldReturnUnmodifiableList() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
            ctx.reportError(ResponseCode.of(40000, "错误"));

            List<Business> errors = ctx.hasCauses();

            assertThat(errors).hasSize(1);
            // 验证返回的是不可修改列表
            org.junit.jupiter.api.Assertions.assertThrows(
                    UnsupportedOperationException.class,
                    () -> errors.add(Business.of(ResponseCode.of(40001, "新错误")))
            );
        }

        @Test
        @DisplayName("无错误时应返回空列表")
        void shouldReturnEmptyListWhenNoErrors() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
            assertThat(ctx.hasCauses()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ValidationContext - getFirstError 方法测试")
    class GetFirstErrorTest {

        @Test
        @DisplayName("无错误时应返回 null")
        void shouldReturnNullWhenNoErrors() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
            assertThat(ctx.getFirstError()).isNull();
        }

        @Test
        @DisplayName("有错误时应返回第一个错误")
        void shouldReturnFirstError() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
            Business first = Business.of(ResponseCode.of(40000, "第一个"));
            Business second = Business.of(ResponseCode.of(40001, "第二个"));

            ctx.reportError(first);
            ctx.reportError(second);

            assertThat(ctx.getFirstError()).isEqualTo(first);
            assertThat(ctx.getFirstError().getResponseCode().getCode()).isEqualTo(40000);
        }

        @Test
        @DisplayName("fast 模式下应返回第一个错误")
        void shouldReturnFirstErrorInFastMode() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(true);
            ctx.reportError(ResponseCode.of(40000, "第一个错误"));
            ctx.reportError(ResponseCode.of(40001, "第二个错误")); // 被忽略

            assertThat(ctx.getFirstError().getResponseCode().getCode()).isEqualTo(40000);
        }
    }

    @Nested
    @DisplayName("ValidationContext - reset 方法测试")
    class ResetTest {

        @Test
        @DisplayName("reset 应重置上下文状态")
        void shouldResetContext() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
            ctx.reportError(ResponseCode.of(40000, "错误"));
            ctx.stop();

            assertThat(ctx.isValid()).isFalse();
            assertThat(ctx.isStopped()).isTrue();
            assertThat(ctx.hasCauses()).hasSize(1);

            // 执行重置
            ctx.reset();

            assertThat(ctx.isValid()).isTrue();
            assertThat(ctx.isStopped()).isFalse();
            assertThat(ctx.hasCauses()).isEmpty();
            assertThat(ctx.getFirstError()).isNull();
        }

        @Test
        @DisplayName("reset 后应能重新添加错误")
        void shouldAllowAddingErrorsAfterReset() {
            FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
            ctx.reportError(ResponseCode.of(40000, "错误1"));
            ctx.reset();

            // 重置后应能添加新错误
            ctx.reportError(ResponseCode.of(40001, "错误2"));
            assertThat(ctx.isValid()).isFalse();
            assertThat(ctx.hasCauses()).hasSize(1);
            assertThat(ctx.getFirstError().getResponseCode().getCode()).isEqualTo(40001);
        }
    }

    @Nested
    @DisplayName("集成测试 - 完整验证流程")
    class IntegrationTest {

        @Test
        @DisplayName("StringValidator 应正确验证")
        void shouldValidateStringCorrectly() {
            StringValidator validator = new StringValidator();

            // 验证通过
            FastValidator.ValidationContext ctx1 = new FastValidator.ValidationContext(false);
            validator.validate("valid", ctx1);
            assertThat(ctx1.isValid()).isTrue();

            // null 值
            FastValidator.ValidationContext ctx2 = new FastValidator.ValidationContext(false);
            validator.validate(null, ctx2);
            assertThat(ctx2.isValid()).isFalse();
            assertThat(ctx2.hasCauses()).hasSize(1);
            assertThat(ctx2.getFirstError().getResponseCode().getCode()).isEqualTo(40000);

            // 空字符串
            FastValidator.ValidationContext ctx3 = new FastValidator.ValidationContext(false);
            validator.validate("", ctx3);
            assertThat(ctx3.isValid()).isFalse();
            assertThat(ctx3.hasCauses()).hasSize(1);

            // 长度超过10
            FastValidator.ValidationContext ctx4 = new FastValidator.ValidationContext(false);
            validator.validate("12345678901", ctx4);
            assertThat(ctx4.isValid()).isFalse();

            // 多个错误（非 fast 模式）
            FastValidator.ValidationContext ctx5 = new FastValidator.ValidationContext(false);
            validator.validate("", ctx5);
            validator.validate(null, ctx5); // 不会执行，因为上面已经 return 了
            // 注意：实际验证器中 return 会阻止后续执行，这里只是演示

            // fast 模式
            FastValidator.ValidationContext ctx6 = new FastValidator.ValidationContext(true);
            validator.validate(null, ctx6);
            assertThat(ctx6.isStopped()).isTrue();
        }

        @Test
        @DisplayName("多个验证器应独立工作")
        void shouldWorkIndependently() {
            StringValidator strValidator = new StringValidator();
            IntegerValidator intValidator = new IntegerValidator();

            FastValidator.ValidationContext ctx1 = new FastValidator.ValidationContext(false);
            strValidator.validate("test", ctx1);
            assertThat(ctx1.isValid()).isTrue();

            FastValidator.ValidationContext ctx2 = new FastValidator.ValidationContext(false);
            intValidator.validate(-5, ctx2);
            assertThat(ctx2.isValid()).isFalse();
        }
    }
}
