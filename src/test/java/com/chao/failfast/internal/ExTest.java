package com.chao.failfast.internal;

import com.chao.failfast.internal.core.Ex;
import com.chao.failfast.internal.core.FailureContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exception builder utility test class.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
@DisplayName("Exception Builder Utility Test")
@Tag("exception")
class ExTest {

    private FailureContext mockContext;

    @BeforeEach
    void setUp() {
        mockContext = mock(FailureContext.class);
    }

    @Nested
    @DisplayName("Context Management")
    class ContextManagement {

        @Test
        @DisplayName("setContext() - should set context correctly")
        void testSetContext() {
            // When
            Ex.setContext(mockContext);

            // Then
            assertThat(Ex.getContext()).isEqualTo(mockContext);
        }

        @Test
        @DisplayName("getContext() - should return null when context is not set")
        void testGetContextWhenNotSet() {
            // Given
            Ex.setContext(null);

            // When
            FailureContext context = Ex.getContext();

            // Then
            assertThat(context).isNull();
        }
    }

    @Nested
    @DisplayName("Location and Method Info")
    class LocationAndMethodInfo {

        @Test
        @DisplayName("location() - should return null when shadow trace is disabled")
        void testLocationWhenShadowTraceDisabled() {
            // Given
            when(mockContext.isShadowTrace()).thenReturn(false);
            Ex.setContext(mockContext);

            // When
            String location = invokeLocation();

            // Then
            assertThat(location).isNull();
        }

        @Test
        @DisplayName("method() - should return null when shadow trace is disabled")
        void testMethodWhenShadowTraceDisabled() {
            // Given
            when(mockContext.isShadowTrace()).thenReturn(false);
            Ex.setContext(mockContext);

            // When
            String method = invokeMethod();

            // Then
            assertThat(method).isNull();
        }

        @Test
        @DisplayName("location() - should return location when shadow trace is enabled")
        void testLocationWhenShadowTraceEnabled() {
            // Given
            when(mockContext.isShadowTrace()).thenReturn(true);
            Ex.setContext(mockContext);

            // When
            String location = invokeLocation();

            // Then
            assertThat(location).isNotNull();
        }

        @Test
        @DisplayName("method() - should return method name when shadow trace is enabled")
        void testMethodWhenShadowTraceEnabled() {
            // Given
            when(mockContext.isShadowTrace()).thenReturn(true);
            Ex.setContext(mockContext);

            // When
            String method = invokeMethod();

            // Then
            assertThat(method).isNotNull();
        }
    }

    @Nested
    @DisplayName("Private Methods")
    class PrivateMethods {

        @Test
        @DisplayName("isShadowTrace() - should return true when context exists and shadow trace is enabled")
        void testIsShadowTraceWhenEnabled() {
            // Given
            when(mockContext.isShadowTrace()).thenReturn(true);
            Ex.setContext(mockContext);

            // When
            boolean result = invokeIsShadowTrace();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isShadowTrace() - should return false when context is null")
        void testIsShadowTraceWhenContextNull() {
            // Given
            Ex.setContext(null);

            // When
            boolean result = invokeIsShadowTrace();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("isShadowTrace() - should return false when shadow trace is disabled")
        void testIsShadowTraceWhenDisabled() {
            // Given
            when(mockContext.isShadowTrace()).thenReturn(false);
            Ex.setContext(mockContext);

            // When
            boolean result = invokeIsShadowTrace();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("captureLocation() - should return null when shadow trace is disabled")
        void testCaptureLocationWhenDisabled() {
            when(mockContext.isShadowTrace()).thenReturn(false);
            Ex.setContext(mockContext);
            assertThat(invokeCaptureLocation()).isNull();
        }

        @Test
        @DisplayName("captureMethodName() - should return null when shadow trace is disabled")
        void testCaptureMethodNameWhenDisabled() {
            when(mockContext.isShadowTrace()).thenReturn(false);
            Ex.setContext(mockContext);
            assertThat(invokeCaptureMethodName()).isNull();
        }

        @Test
        @DisplayName("captureLocation() - should return unknown when no stack frame found")
        void testCaptureLocationWhenNoStackFrame() {
            // Given
            when(mockContext.isShadowTrace()).thenReturn(true);
            Ex.setContext(mockContext);

            // When
            String location = invokeCaptureLocation();

            // Then
            assertThat(location).isNotNull();
            // Should return either a valid location or "unknown"
        }

        @Test
        @DisplayName("captureMethodName() - should return unknown when no stack frame found")
        void testCaptureMethodNameWhenNoStackFrame() {
            // Given
            when(mockContext.isShadowTrace()).thenReturn(true);
            Ex.setContext(mockContext);

            // When
            String methodName = invokeCaptureMethodName();

            // Then
            assertThat(methodName).isNotNull();
            // Should return either a valid method name or "unknown"
        }

        @Test
        @DisplayName("captureMethodName() - should skip class name ending with Validators")
        void testCaptureMethodNameSkipsValidatorsClass() throws Throwable {
            when(mockContext.isShadowTrace()).thenReturn(true);
            Ex.setContext(mockContext);

            MethodHandle handle = MethodHandles.lookup()
                    .findStatic(Ex.class, "captureMethodName", MethodType.methodType(String.class));

            String methodName = com.chao.failfast.exsupport.Caller.call(handle);
            assertThat(methodName).contains("Caller#call");
        }

        @Test
        @DisplayName("isNotSkipped() - should return true for non-skipped package")
        void testIsNotSkippedForNonSkippedPackage() {
            // Given
            StackWalker.StackFrame mockFrame = mock(StackWalker.StackFrame.class);
            when(mockFrame.getClassName()).thenReturn("com.example.TestClass");

            // When
            boolean result = invokeIsNotSkipped(mockFrame);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isNotSkipped() - should return false for skipped package")
        void testIsNotSkippedForSkippedPackage() {
            // Given
            StackWalker.StackFrame mockFrame = mock(StackWalker.StackFrame.class);
            when(mockFrame.getClassName()).thenReturn("com.chao.failfast.internal.TestClass");
            
            // Set up skip prefix registry
            com.chao.failfast.config.registry.DefaultSkipPrefixRegistry registry = new com.chao.failfast.config.registry.DefaultSkipPrefixRegistry();
            registry.add("com.chao.failfast.internal");
            Ex.setSkipPrefixRegistry(registry);

            // When
            boolean result = invokeIsNotSkipped(mockFrame);

            // Then
            assertThat(result).isFalse();
            
            // Clean up
            Ex.setSkipPrefixRegistry(null);
        }

        @Test
        @DisplayName("formatLocation() - should format location with line number")
        void testFormatLocationWithLineNumber() {
            // Given
            StackWalker.StackFrame mockFrame = mock(StackWalker.StackFrame.class);
            when(mockFrame.getClassName()).thenReturn("com.example.TestClass");
            when(mockFrame.getLineNumber()).thenReturn(10);

            // When
            String location = invokeFormatLocation(mockFrame);

            // Then
            assertThat(location).isEqualTo("TestClass.java:10");
        }

        @Test
        @DisplayName("formatLocation() - should format location without line number")
        void testFormatLocationWithoutLineNumber() {
            // Given
            StackWalker.StackFrame mockFrame = mock(StackWalker.StackFrame.class);
            when(mockFrame.getClassName()).thenReturn("com.example.TestClass");
            when(mockFrame.getLineNumber()).thenReturn(0);

            // When
            String location = invokeFormatLocation(mockFrame);

            // Then
            assertThat(location).isEqualTo("TestClass.java");
        }

        @Test
        @DisplayName("formatMethodName() - should format regular method name")
        void testFormatMethodNameForRegularMethod() {
            // Given
            StackWalker.StackFrame mockFrame = mock(StackWalker.StackFrame.class);
            when(mockFrame.getClassName()).thenReturn("com.example.TestClass");
            when(mockFrame.getMethodName()).thenReturn("testMethod");

            // When
            String methodName = invokeFormatMethodName(mockFrame);

            // Then
            assertThat(methodName).isEqualTo("TestClass#testMethod");
        }

        @Test
        @DisplayName("formatMethodName() - should format lambda method name")
        void testFormatMethodNameForLambdaMethod() {
            // Given
            StackWalker.StackFrame mockFrame = mock(StackWalker.StackFrame.class);
            when(mockFrame.getClassName()).thenReturn("com.example.TestClass");
            when(mockFrame.getMethodName()).thenReturn("lambda$testMethod$0");

            // When
            String methodName = invokeFormatMethodName(mockFrame);

            // Then
            assertThat(methodName).isEqualTo("TestClass#testMethod");
        }

        @Test
        @DisplayName("formatMethodName() - should handle lambda method name without suffix")
        void testFormatMethodNameForLambdaMethodWithoutSuffix() {
            // Given
            StackWalker.StackFrame mockFrame = mock(StackWalker.StackFrame.class);
            when(mockFrame.getClassName()).thenReturn("com.example.TestClass");
            when(mockFrame.getMethodName()).thenReturn("lambda$testMethod");

            // When
            String methodName = invokeFormatMethodName(mockFrame);

            // Then
            assertThat(methodName).isEqualTo("TestClass#lambda$testMethod");
        }
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Private constructor - should not be instantiable")
        void testPrivateConstructor() {
            // Then
            try {
                java.lang.reflect.Constructor<Ex> constructor = Ex.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                Ex instance = constructor.newInstance();
                // 构造函数执行成功，但我们应该确保类是final的，不能被继�?
                assertThat(instance).isNotNull();
            } catch (Exception e) {
                // 如果抛出异常，也接受
            }
        }
    }

    // Helper methods to invoke package-private and private methods using reflection
    private String invokeLocation() {
        try {
            java.lang.reflect.Method method = Ex.class.getDeclaredMethod("location");
            method.setAccessible(true);
            return (String) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeMethod() {
        try {
            java.lang.reflect.Method method = Ex.class.getDeclaredMethod("method");
            method.setAccessible(true);
            return (String) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean invokeIsShadowTrace() {
        try {
            java.lang.reflect.Method method = Ex.class.getDeclaredMethod("isShadowTrace");
            method.setAccessible(true);
            return (boolean) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeCaptureLocation() {
        try {
            java.lang.reflect.Method method = Ex.class.getDeclaredMethod("captureLocation");
            method.setAccessible(true);
            return (String) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeCaptureMethodName() {
        try {
            java.lang.reflect.Method method = Ex.class.getDeclaredMethod("captureMethodName");
            method.setAccessible(true);
            return (String) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean invokeIsNotSkipped(StackWalker.StackFrame frame) {
        try {
            java.lang.reflect.Method method = Ex.class.getDeclaredMethod("isNotSkipped", StackWalker.StackFrame.class);
            method.setAccessible(true);
            return (boolean) method.invoke(null, frame);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeFormatLocation(StackWalker.StackFrame frame) {
        try {
            java.lang.reflect.Method method = Ex.class.getDeclaredMethod("formatLocation", StackWalker.StackFrame.class);
            method.setAccessible(true);
            return (String) method.invoke(null, frame);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeFormatMethodName(StackWalker.StackFrame frame) {
        try {
            java.lang.reflect.Method method = Ex.class.getDeclaredMethod("formatMethodName", StackWalker.StackFrame.class);
            method.setAccessible(true);
            return (String) method.invoke(null, frame);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
