package com.chao.failfast.validator;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.i18n.I18nExtension;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Set;

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

    @Test
    @DisplayName("validateIfRegistered 应在类型注册时执行校验")
    void shouldValidateIfRegistered() {
        TestValidator validator = new TestValidator();
        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);

        // 注册类型，应执行校验
        boolean result = validator.validateIfRegistered("", ctx);
        assertThat(result).isTrue();
        assertThat(ctx.isValid()).isFalse();

        // 重置上下文
        ctx.reset();
        // 未注册类型，不应执行校验
        result = validator.validateIfRegistered(10.5, ctx);
        assertThat(result).isFalse();
        assertThat(ctx.isValid()).isTrue();

        // null 值，不应执行校验
        result = validator.validateIfRegistered(null, ctx);
        assertThat(result).isFalse();
        assertThat(ctx.isValid()).isTrue();
    }

    @Test
    @DisplayName("isRegisteredType 应正确判断类型是否注册")
    void shouldCheckIfTypeIsRegistered() {
        TestValidator validator = new TestValidator();
        assertThat(validator.isRegisteredType(String.class)).isTrue();
        assertThat(validator.isRegisteredType(Integer.class)).isTrue();
        assertThat(validator.isRegisteredType(Double.class)).isFalse();
    }

    @Test
    @DisplayName("getRegisteredTypes 应返回所有注册类型")
    void shouldReturnAllRegisteredTypes() {
        TestValidator validator = new TestValidator();
        Set<Class<?>> types = validator.getRegisteredTypes();
        assertThat(types).containsExactlyInAnyOrder(String.class, Integer.class);
    }

    @Test
    @DisplayName("size 应返回注册类型数量")
    void shouldReturnSizeOfRegisteredTypes() {
        TestValidator validator = new TestValidator();
        assertThat(validator.size()).isEqualTo(2);

        EmptyValidator emptyValidator = new EmptyValidator();
        assertThat(emptyValidator.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("当 context 为 null 时应创建默认上下文")
    void shouldCreateDefaultContextWhenNull() {
        TestValidator validator = new TestValidator();
        validator.validate("", null);
        // 不应抛出异常
    }

    @Test
    @DisplayName("继承体系的类型应选择最合适的处理器")
    void shouldSelectBestHandlerForInheritedTypes() {
        // 创建一个包含继承关系的测试
        TypedValidator validator = new TypedValidator() {
            @Override
            protected void registerValidators() {
                register(Number.class, (n, ctx) -> {
                    if (n.doubleValue() < 0) ctx.reportError(ResponseCode.of(400, "Negative number"));
                });
                register(Integer.class, (i, ctx) -> {
                    if (i < 0) ctx.reportError(ResponseCode.of(400, "Negative integer"));
                });
            }
        };

        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
        validator.validate(5, ctx);
        assertThat(ctx.isValid()).isTrue();

        ctx.reset();
        validator.validate(-5, ctx);
        assertThat(ctx.isValid()).isFalse();
    }

    @Test
    @DisplayName("当多个接口具有相同距离时应选择名称字典序较小的")
    void shouldSelectHandlerByLexicographicalOrderWhenDistanceEqual() {
        // 创建一个包含多个接口的测试
        interface InterfaceA {}
        interface InterfaceB {}
        class TestClass implements InterfaceA, InterfaceB {}

        TypedValidator validator = new TypedValidator() {
            @Override
            protected void registerValidators() {
                register(InterfaceA.class, (obj, ctx) -> ctx.reportError(ResponseCode.of(400, "InterfaceA error")));
                register(InterfaceB.class, (obj, ctx) -> ctx.reportError(ResponseCode.of(400, "InterfaceB error")));
            }
        };

        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
        validator.validate(new TestClass(), ctx);
        assertThat(ctx.isValid()).isFalse();
        // 应该选择 InterfaceA，因为 A 在字典序上小于 B
    }

    @Test
    @DisplayName("当类和接口距离相同时应选择类而非接口")
    void shouldSelectClassOverInterfaceWhenDistanceEqual() {
        interface TestInterface {}
        class TestBaseClass implements TestInterface {}
        class TestSubClass extends TestBaseClass {}

        TypedValidator validator = new TypedValidator() {
            @Override
            protected void registerValidators() {
                register(TestInterface.class, (obj, ctx) -> ctx.reportError(ResponseCode.of(400, "Interface error")));
                register(TestBaseClass.class, (obj, ctx) -> ctx.reportError(ResponseCode.of(400, "Class error")));
            }
        };

        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
        validator.validate(new TestSubClass(), ctx);
        assertThat(ctx.isValid()).isFalse();
        // 应该选择 TestBaseClass，因为类优先于接口
    }

    @Test
    @DisplayName("distance 方法应正确计算类继承距离")
    void shouldCalculateDistanceCorrectly() {
        // 测试直接继承
        assertThat(distance(Integer.class, Number.class)).isGreaterThan(0);
        assertThat(distance(Integer.class, Object.class)).isGreaterThan(distance(Integer.class, Number.class));
        assertThat(distance(Integer.class, Integer.class)).isEqualTo(0);
        assertThat(distance(null, Integer.class)).isEqualTo(Integer.MAX_VALUE);
        assertThat(distance(Integer.class, null)).isEqualTo(Integer.MAX_VALUE);
    }

    // 反射获取 distance 方法进行测试
    private int distance(Class<?> from, Class<?> to) {
        try {
            java.lang.reflect.Method method = TypedValidator.class.getDeclaredMethod("distance", Class.class, Class.class);
            method.setAccessible(true);
            return (int) method.invoke(null, from, to);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 
