package com.chao.failfast.validator;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.i18n.I18nExtension;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TypedValidator 类型校验器测试")
@ExtendWith(I18nExtension.class)
class TypedValidatorTest {

    static class TestValidator extends TypedValidator {
        @Override
        protected void registerValidators() {
            register(String.class, (s, ctx) -> {
                if (s.isEmpty()) ctx.reportError(ResponseCode.of(400, "Empty string"));
            });
            register(Integer.class, (i, ctx) -> {
                if (i < 0) ctx.reportError(ResponseCode.of(400, "Negative integer"));
            });
        }
    }
    static class EmptyValidator extends TypedValidator {
    }

    @Test
    @DisplayName("应当根据类型分发校验")
    void shouldDispatchByType() {
        TestValidator validator = new TestValidator();
        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);

        validator.validate("test", ctx);
        assertThat(ctx.isValid()).isTrue();

        validator.validate("", ctx);
        assertThat(ctx.isValid()).isFalse();
        assertThat(ctx.hasCauses()).hasSize(1);
        
        // Reset
        ctx = new FastValidator.ValidationContext(false);
        validator.validate(10, ctx);
        assertThat(ctx.isValid()).isTrue();
        
        validator.validate(-1, ctx);
        assertThat(ctx.isValid()).isFalse();
    }

    @Test
    @DisplayName("当对象为 null 时应报错")
    void shouldReportErrorWhenNull() {
        TestValidator validator = new TestValidator();
        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);

        validator.validate(null, ctx);
        assertThat(ctx.isValid()).isFalse();
        Business error = ctx.hasCauses().get(0);
        assertThat(error.getResponseCode().getCode()).isEqualTo(500);
        assertThat(error.getResponseCode().getMessage()).isIn("参数校验失败", "{response.code.validation.error}");
    }

    @Test
    @DisplayName("当类型不支持时应报错")
    void shouldReportErrorWhenTypeNotSupported() {
        TestValidator validator = new TestValidator();
        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);

        validator.validate(10.5, ctx); // Double not registered
        assertThat(ctx.isValid()).isFalse();
        Business error = ctx.hasCauses().get(0);
        assertThat(error.getResponseCode().getCode()).isEqualTo(400);
        assertThat(error.getDetail()).satisfiesAnyOf(
            s -> assertThat(s).contains("不支持的校验类型"),
            s -> assertThat(s).contains("{failure.const.unsupported.validation.type}")
        );
    }
    @Test
    @DisplayName("getSupportedType: 单一类型返回该类型，多类型返回 Object")
    void shouldReturnCorrectSupportedType() {
        // 单一类型
        TestValidator single = new TestValidator();
        // TestValidator 构造函数里注册了 String 和 Integer，所以是多类型
        assertThat(single.getSupportedType()).isEqualTo(Object.class);

        // 创建一个只注册了 String 的校验器
        TypedValidator stringValidator = new TypedValidator() {
            @Override
            protected void registerValidators() {
                register(String.class, (s, ctx) -> {});
            }
        };
        assertThat(stringValidator.getSupportedType()).isEqualTo(String.class);

        // 创建一个空校验器
        TypedValidator emptyValidator = new TypedValidator() {};
        assertThat(emptyValidator.getSupportedType()).isEqualTo(Object.class);
    }

    @Test
    @DisplayName("当未注册类型时，应当不进行校验")
    void shouldNotValidateWhenTypeNotRegistered() {
        EmptyValidator validator = new EmptyValidator();
        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);

        validator.validate("test", ctx);
        assertThat(ctx.isValid()).isFalse();
        assertThat(ctx.hasCauses().get(0).getResponseCode().getCode()).isEqualTo(400);
    }
}
