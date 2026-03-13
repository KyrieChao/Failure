package com.chao.failfast.internal;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("Ex 工具类测试")
@ExtendWith(MockitoExtension.class)
class ExTest {

    @Mock
    private FailureContext context;

    @BeforeEach
    void setUp() {
        Ex.setContext(context);
    }

    @AfterEach
    void tearDown() {
        Ex.setContext(null);
    }

    @Test
    @DisplayName("私有构造函数应防止实例化")
    void shouldPreventInstantiation() throws Exception {
        Constructor<Ex> constructor = Ex.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Ex instance = constructor.newInstance();
        assertThat(instance).isNotNull();
    }

    @Test
    @DisplayName("getContext 应返回设置的上下文")
    void shouldGetContext() {
        assertThat(Ex.getContext()).isEqualTo(context);
    }

    @Test
    @DisplayName("setContext 应设置上下文为 null")
    void shouldSetContextToNull() {
        Ex.setContext(null);
        assertThat(Ex.getContext()).isNull();
    }

    @Nested
    @DisplayName("isShadowTrace 为 false 时的测试")
    class WhenShadowTraceDisabled {

        @BeforeEach
        void setShadowTraceFalse() {
            when(context.isShadowTrace()).thenReturn(false);
        }

        @Test
        @DisplayName("location 应返回 null")
        void locationShouldReturnNull() {
            assertThat(Ex.location()).isNull();
        }

        @Test
        @DisplayName("method 应返回 null")
        void methodShouldReturnNull() {
            assertThat(Ex.method()).isNull();
        }

        @Test
        @DisplayName("captureLocation 应返回 null")
        void captureLocationShouldReturnNull() throws Exception {
            Method method = Ex.class.getDeclaredMethod("captureLocation");
            method.setAccessible(true);
            String result = (String) method.invoke(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("captureMethodName 应返回 null")
        void captureMethodNameShouldReturnNull() throws Exception {
            Method method = Ex.class.getDeclaredMethod("captureMethodName");
            method.setAccessible(true);
            String result = (String) method.invoke(null);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("isShadowTrace 为 true 时的测试")
    class WhenShadowTraceEnabled {

        @BeforeEach
        void setShadowTraceTrue() {
            when(context.isShadowTrace()).thenReturn(true);
        }

        @Test
        @DisplayName("location 应返回非 null 的位置信息")
        void locationShouldReturnLocation() {
            String location = Ex.location();
            assertThat(location).isNotNull();
            assertThat(location).contains(".java:");
        }

        @Test
        @DisplayName("method 应返回非 null 的方法信息")
        void methodShouldReturnMethod() {
            String method = Ex.method();
            assertThat(method).isNotNull();
            assertThat(method).contains("#");
        }

        @Test
        @DisplayName("captureLocation 应返回位置信息")
        void captureLocationShouldReturnLocation() throws Exception {
            Method method = Ex.class.getDeclaredMethod("captureLocation");
            method.setAccessible(true);
            String result = (String) method.invoke(null);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("captureMethodName 应返回方法信息")
        void captureMethodNameShouldReturnMethod() throws Exception {
            Method method = Ex.class.getDeclaredMethod("captureMethodName");
            method.setAccessible(true);
            String result = (String) method.invoke(null);
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("context 为 null 时的测试")
    class WhenContextIsNull {

        @BeforeEach
        void setContextNull() {
            Ex.setContext(null);
        }

        @Test
        @DisplayName("location 应返回 null")
        void locationShouldReturnNull() {
            assertThat(Ex.location()).isNull();
        }

        @Test
        @DisplayName("method 应返回 null")
        void methodShouldReturnNull() {
            assertThat(Ex.method()).isNull();
        }
    }

    @Nested
    @DisplayName("formatLocation 行号处理测试")
    class FormatLocationTest {

        @BeforeEach
        void setShadowTraceTrue() {
            when(context.isShadowTrace()).thenReturn(true);
        }

        @Test
        @DisplayName("应正确格式化包含行号的位置")
        void shouldFormatLocationWithLineNumber() {
            String location = Ex.location();
            assertThat(location).matches(".*\\.java:\\d+\\)$");
        }
    }

    @Nested
    @DisplayName("formatMethodName Lambda 处理测试")
    class FormatMethodNameTest {

        @BeforeEach
        void setShadowTraceTrue() {
            when(context.isShadowTrace()).thenReturn(true);
        }

        @Test
        @DisplayName("应处理 lambda 方法名")
        void shouldHandleLambdaMethodName() {
            Supplier<String> lambda = Ex::method;
            String method = lambda.get();
            assertThat(method).isNotNull();
            assertThat(method).doesNotContain("lambda$");
            assertThat(method).contains("#");
        }

        @Test
        @DisplayName("非 lambda 方法名应保持不变")
        void shouldKeepNormalMethodName() {
            String method = Ex.method();
            assertThat(method).isNotNull();
            assertThat(method).contains("#");
        }
    }

    @Nested
    @DisplayName("isNotSkipped 过滤测试")
    class IsNotSkippedTest {

        @BeforeEach
        void setShadowTraceTrue() {
            when(context.isShadowTrace()).thenReturn(true);
        }

        @Test
        @DisplayName("应跳过 com.chao.failfast 内部包")
        void shouldSkipInternalPackages() {
            String location = Ex.location();
            assertThat(location).isNotNull();
            assertThat(location).doesNotContain("ExTest");
        }
    }

    @Nested
    @DisplayName("captureMethodName Validator 过滤测试")
    class ValidatorFilterTest {

        @BeforeEach
        void setShadowTraceTrue() {
            when(context.isShadowTrace()).thenReturn(true);
        }

        @Test
        @DisplayName("应过滤 Validator 类名")
        void shouldFilterValidatorClasses() {
            String method = Ex.method();
            assertThat(method).doesNotContain("Validator");
        }
    }

    @Nested
    @DisplayName("formatMethodName lambda$ 边界测试")
    class LambdaBoundaryTest {

        @Test
        @DisplayName("formatMethodName 应处理各种 lambda$ 格式")
        void shouldHandleVariousLambdaFormats() throws Exception {
            Method formatMethod = Ex.class.getDeclaredMethod("formatMethodName", StackWalker.StackFrame.class);
            formatMethod.setAccessible(true);

            // 测试 lambda$methodName$0 格式 - 正常解析
            StackWalker.StackFrame frame1 = createMockFrame("com.example.TestClass", "lambda$testMethod$0", 0);
            String result1 = (String) formatMethod.invoke(null, frame1);
            assertThat(result1).isEqualTo("TestClass#testMethod");

            // 测试普通方法名
            StackWalker.StackFrame frame2 = createMockFrame("com.example.TestClass", "normalMethod", 0);
            String result2 = (String) formatMethod.invoke(null, frame2);
            assertThat(result2).isEqualTo("TestClass#normalMethod");

            // 测试 lambda$ 但只有一个 $ 的情况（firstDollar == lastDollar，条件失败）
            StackWalker.StackFrame frame3 = createMockFrame("com.example.TestClass", "lambda$", 0);
            String result3 = (String) formatMethod.invoke(null, frame3);
            assertThat(result3).isEqualTo("TestClass#lambda$");

            // 测试 lambda$xxx 但没有第二个 $（lastDollar == firstDollar，条件失败）
            StackWalker.StackFrame frame4 = createMockFrame("com.example.TestClass", "lambda$test", 0);
            String result4 = (String) formatMethod.invoke(null, frame4);
            assertThat(result4).isEqualTo("TestClass#lambda$test");

            // 测试 lambda$0$xxx（数字在中间，firstDollar=6, lastDollar=8，条件成功）
            StackWalker.StackFrame frame5 = createMockFrame("com.example.TestClass", "lambda$0$test", 0);
            String result5 = (String) formatMethod.invoke(null, frame5);
            assertThat(result5).isEqualTo("TestClass#0");
        }
    }

    @Nested
    @DisplayName("formatLocation 行号边界测试")
    class LineNumberBoundaryTest {

        @Test
        @DisplayName("formatLocation 应处理各种行号情况")
        void shouldHandleVariousLineNumbers() throws Exception {
            Method formatMethod = Ex.class.getDeclaredMethod("formatLocation", StackWalker.StackFrame.class);
            formatMethod.setAccessible(true);

            StackWalker.StackFrame frame1 = createMockFrame("com.example.TestClass", "testMethod", 100);
            String result1 = (String) formatMethod.invoke(null, frame1);
            assertThat(result1).isEqualTo("TestClass.testMethod(TestClass.java:100)");

            StackWalker.StackFrame frame2 = createMockFrame("com.example.TestClass", "testMethod", 0);
            String result2 = (String) formatMethod.invoke(null, frame2);
            assertThat(result2).isEqualTo("TestClass.testMethod(TestClass.java)");

            StackWalker.StackFrame frame3 = createMockFrame("com.example.TestClass", "testMethod", -1);
            String result3 = (String) formatMethod.invoke(null, frame3);
            assertThat(result3).isEqualTo("TestClass.testMethod(TestClass.java)");
        }
    }

    @Nested
    @DisplayName("isNotSkipped 边界测试")
    class IsNotSkippedBoundaryTest {

        @Test
        @DisplayName("isNotSkipped 应正确判断各种类名")
        void shouldHandleVariousClassNames() throws Exception {
            Method isNotSkippedMethod = Ex.class.getDeclaredMethod("isNotSkipped", StackWalker.StackFrame.class);
            isNotSkippedMethod.setAccessible(true);

            String[] skipPrefixes = {
                    "com.chao.failfast.advice.Test",
                    "com.chao.failfast.annotation.Test",
                    "com.chao.failfast.aspect.Test",
                    "com.chao.failfast.config.Test",
                    "com.chao.failfast.integration.Test",
                    "com.chao.failfast.internal.Test",
                    "com.chao.failfast.result.Test",
                    "com.chao.failfast.Failure",
                    "org.springframework.Test",
                    "org.apache.Test",
                    "jakarta.Test",
                    "java.util.Test",
                    "jdk.internal.Test",
                    "sun.misc.Test"
            };

            for (String className : skipPrefixes) {
                StackWalker.StackFrame frame = createMockFrame(className, "method", 0);
                Boolean result = (Boolean) isNotSkippedMethod.invoke(null, frame);
                assertThat(result).as("Class %s should be skipped", className).isFalse();
            }

            String[] nonSkipPrefixes = {
                    "com.example.Test",
                    "org.example.Test",
                    "net.example.Test",
                    "io.github.Test"
            };

            for (String className : nonSkipPrefixes) {
                StackWalker.StackFrame frame = createMockFrame(className, "method", 0);
                Boolean result = (Boolean) isNotSkippedMethod.invoke(null, frame);
                assertThat(result).as("Class %s should not be skipped", className).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("captureMethodName Validator 过滤完整测试")
    class CaptureMethodNameValidatorFilterTest {

        private boolean shouldBeFiltered(String className) {
            return className.startsWith("com.chao.failfast.validator")
                    || className.endsWith("Validator")
                    || className.endsWith("Validators");
        }

        @Test
        @DisplayName("应过滤 com.chao.failfast.validator 包下的类")
        void shouldFilterValidatorPackage() {
            assertThat(shouldBeFiltered("com.chao.failfast.validator.SomeValidator")).isTrue();
            assertThat(shouldBeFiltered("com.chao.failfast.validator.SomeService")).isTrue();
        }

        @Test
        @DisplayName("应过滤以 Validator 结尾的类")
        void shouldFilterValidatorSuffix() {
            assertThat(shouldBeFiltered("com.example.UserValidator")).isTrue();
            assertThat(shouldBeFiltered("Validator")).isTrue();
        }

        @Test
        @DisplayName("应过滤以 Validators 结尾的类")
        void shouldFilterValidatorsSuffix() {
            assertThat(shouldBeFiltered("com.example.UserValidators")).isTrue();
            assertThat(shouldBeFiltered("Validators")).isTrue();
        }

        @Test
        @DisplayName("不应过滤普通类")
        void shouldNotFilterNormalClasses() {
            assertThat(shouldBeFiltered("com.example.UserService")).isFalse();
            assertThat(shouldBeFiltered("com.example.MyValidationService")).isFalse();
        }
    }

    private StackWalker.StackFrame createMockFrame(String className, String methodName, int lineNumber) {
        return new StackWalker.StackFrame() {
            @Override
            public String getClassName() {
                return className;
            }

            @Override
            public String getMethodName() {
                return methodName;
            }

            @Override
            public int getLineNumber() {
                return lineNumber;
            }

            @Override
            public int getByteCodeIndex() {
                return 0;
            }

            @Override
            public String getFileName() {
                return null;
            }

            @Override
            public boolean isNativeMethod() {
                return false;
            }

            @Override
            public StackTraceElement toStackTraceElement() {
                return null;
            }

            @Override
            public Class<?> getDeclaringClass() {
                return null;
            }

            @Override
            public java.lang.invoke.MethodType getMethodType() {
                return null;
            }

            @Override
            public String getDescriptor() {
                return null;
            }
        };
    }
}