package com.chao.failure.validator;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 针对 TemplateValidator 的完整单元测试
 * 目标：100% 类/方法/行/指令/分支/条件/路径覆盖
 */
@DisplayName("TemplateValidator 完整覆盖率测试")
class TemplateValidatorTest {

    @Nested
    @DisplayName("基础验证流程测试")
    class BasicValidationTests {

        @Test
        @DisplayName("正常验证流程：validateCommon -> validateSpecific")
        void testBasicValidationFlow() {
            TestTemplateValidator validator = new TestTemplateValidator();

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate("test", context);

            assertTrue(validator.isValidateCommonCalled());
            assertTrue(validator.isValidateSpecificCalled());
        }

        @Test
        @DisplayName("验证过程中验证被停止")
        void testValidationStopped() {
            TestTemplateValidator validator = new TestTemplateValidator();
            validator.setStopOnCommon(true);

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate("test", context);

            assertTrue(validator.isValidateCommonCalled());
            assertFalse(validator.isValidateSpecificCalled());
        }

        @Test
        @DisplayName("验证 null 上下文")
        void testNullContext() {
            TestTemplateValidator validator = new TestTemplateValidator();

            assertDoesNotThrow(() -> validator.validate("test", null));
        }
    }

    @Nested
    @DisplayName("泛型类型解析测试")
    class GenericTypeResolutionTests {

        @Test
        @DisplayName("继承 TemplateValidator<String> 应返回 String.class")
        void testStringTypeArgument() {
            StringTemplateValidator validator = new StringTemplateValidator();
            assertEquals(String.class, validator.getSupportedType());
        }

        @Test
        @DisplayName("继承 TemplateValidator<Integer> 应返回 Integer.class")
        void testIntegerTypeArgument() {
            IntegerTemplateValidator validator = new IntegerTemplateValidator();
            assertEquals(Integer.class, validator.getSupportedType());
        }

        @Test
        @DisplayName("继承 TemplateValidator 无类型参数应返回 Object.class")
        void testNoTypeArgument() {
            NoTypeArgumentValidator validator = new NoTypeArgumentValidator();
            assertEquals(Object.class, validator.getSupportedType());
        }

        @Test
        @DisplayName("多层继承链类型解析")
        void testMultiLevelInheritance() {
            DeepStringTemplateValidator validator = new DeepStringTemplateValidator();
            assertEquals(String.class, validator.getSupportedType());
        }

        @Test
        @DisplayName("参数化类型解析应返回原始类型")
        void testParameterizedTypeResolution() {
            ParameterizedTemplateValidator validator = new ParameterizedTemplateValidator();
            // 应该返回原始类型 List，而不是 Object
            assertEquals(java.util.List.class, validator.getSupportedType());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("验证 null 对象")
        void testValidateNullObject() {
            TestTemplateValidator validator = new TestTemplateValidator();

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate(null, context);

            // TemplateValidator 本身不会对 null 进行特殊处理，
            // 只有当 validateCommon 或 validateSpecific 报告错误时才会失败
            assertTrue(context.isValid());
        }

        @Test
        @DisplayName("泛型类型变量边界情况")
        void testTypeVariableBoundary() {
            BoundaryTypeValidator validator = new BoundaryTypeValidator();
            assertEquals(Object.class, validator.getSupportedType());
        }

        @Test
        @DisplayName("默认 validateCommon 空实现也应被调用")
        void testDefaultValidateCommonBranch() {
            DefaultCommonValidator validator = new DefaultCommonValidator();

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate("x", context);

            assertTrue(validator.validateSpecificCalled);
        }

        @Test
        @DisplayName("未解析的类型变量应回退为 Object")
        void testUnresolvedTypeVariableFallsBackToObject() {
            UnresolvedValidator validator = new UnresolvedValidator();
            assertEquals(Object.class, validator.getSupportedType());
        }
    }

    static class TestTemplateValidator extends TemplateValidator<String> {
        @Getter
        private boolean validateCommonCalled = false;
        @Getter
        private boolean validateSpecificCalled = false;
        @Setter
        private boolean stopOnCommon = false;

        @Override
        protected void validateCommon(String target, FastValidator.ValidationContext context) {
            validateCommonCalled = true;
            if (stopOnCommon) {
                context.stop();
            }
        }

        @Override
        protected void validateSpecific(String target, FastValidator.ValidationContext context) {
            validateSpecificCalled = true;
        }
    }

    static class StringTemplateValidator extends TemplateValidator<String> {
        @Override
        protected void validateSpecific(String target, FastValidator.ValidationContext context) {
        }
    }

    static class IntegerTemplateValidator extends TemplateValidator<Integer> {
        @Override
        protected void validateSpecific(Integer target, FastValidator.ValidationContext context) {
        }
    }

    static class NoTypeArgumentValidator extends TemplateValidator {
        @Override
        protected void validateSpecific(Object target, FastValidator.ValidationContext context) {
        }
    }

    static class DeepStringTemplateValidator extends StringTemplateValidator {
    }

    static class ParameterizedTemplateValidator extends TemplateValidator<java.util.List<String>> {
        @Override
        protected void validateSpecific(java.util.List<String> target, FastValidator.ValidationContext context) {
        }
    }

    static class BoundaryTypeValidator extends TemplateValidator {
        @Override
        protected void validateSpecific(Object target, FastValidator.ValidationContext context) {
        }
    }

    static class DefaultCommonValidator extends TemplateValidator<String> {
        private boolean validateSpecificCalled;

        @Override
        protected void validateSpecific(String target, FastValidator.ValidationContext context) {
            validateSpecificCalled = true;
        }
    }

    @SuppressWarnings("rawtypes")
    static class GenericBase<T> extends TemplateValidator<T> {
        @Override
        protected void validateSpecific(Object target, FastValidator.ValidationContext context) {
        }
    }

    @SuppressWarnings("rawtypes")
    static class UnresolvedValidator extends GenericBase {
    }

    @Test
    @DisplayName("resolveTypeArgument 遇到接口类型时应回退为 Object")
    void should_returnObject_when_resolveTypeArgumentReceivesInterfaceClass() throws Exception {
        Method method = TemplateValidator.class.getDeclaredMethod("resolveTypeArgument", Class.class);
        method.setAccessible(true);

        Object result = method.invoke(null, Runnable.class);

        assertEquals(Object.class, result);
    }

    @Test
    @DisplayName("toClass 遇到原始类型不是 Class 的参数化类型时应回退为 Object")
    void should_returnObject_when_toClassReceivesParameterizedTypeWithNonClassRawType() throws Exception {
        Method method = TemplateValidator.class.getDeclaredMethod("toClass", Type.class);
        method.setAccessible(true);

        ParameterizedType weirdType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[0];
            }

            @Override
            public Type getRawType() {
                return new Type() {
                };
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };

        Object result = method.invoke(null, weirdType);

        assertEquals(Object.class, result);
    }

    @Test
    @DisplayName("resolveTypeArgument 遇到 null 类时应回退为 Object")
    void should_returnObject_when_resolveTypeArgumentReceivesNullLeafClass() throws Exception {
        Method method = TemplateValidator.class.getDeclaredMethod("resolveTypeArgument", Class.class);
        method.setAccessible(true);

        Object result = method.invoke(null, new Object[]{null});

        assertEquals(Object.class, result);
    }
}
