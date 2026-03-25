package com.chao.failfast.internal.core;

import com.chao.failfast.config.CodeMappingConfig;
import com.chao.failfast.internal.policy.DefaultErrorPolicy;
import com.chao.failfast.internal.policy.ErrorPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * FailureContext 100% 覆盖率测试
 */
@DisplayName("FailureContext 完整覆盖测试")
public class FailureContextTest {

    private FailureProperties mockProperties;
    private CodeMappingConfig mockCodeMappingConfig;
    private ErrorPolicy mockErrorPolicy;
    private FailureContext failureContext;

    @BeforeEach
    void setUp() {
        // 创建模拟对象
        mockProperties = mock(FailureProperties.class);
        mockCodeMappingConfig = mock(CodeMappingConfig.class);
        mockErrorPolicy = mock(ErrorPolicy.class);

        // 设置模拟对象的行为
        when(mockProperties.isShadowTrace()).thenReturn(true);
        when(mockProperties.isDebugSnapshot()).thenReturn(false);

        // 创建 FailureContext 实例
        failureContext = new FailureContext(mockProperties, mockCodeMappingConfig, mockErrorPolicy);
    }

    @Test
    @DisplayName("测试构造函数 - 传入非 null 的 ErrorPolicy")
    void testConstructorWithNonNullErrorPolicy() {
        // 验证构造函数是否正确初始化了字段
        assertEquals(mockCodeMappingConfig, failureContext.getCodeMappingConfig());
        assertEquals(mockErrorPolicy, failureContext.getErrorPolicy());
    }

    @Test
    @DisplayName("测试构造函数 - 传入 null 的 ErrorPolicy")
    void testConstructorWithNullErrorPolicy() {
        // 创建 FailureContext 实例，传入 null 的 ErrorPolicy
        FailureContext context = new FailureContext(mockProperties, mockCodeMappingConfig, null);
        // 验证是否使用了默认的 ErrorPolicy
        assertEquals(DefaultErrorPolicy.INSTANCE, context.getErrorPolicy());
    }

    @Test
    @DisplayName("测试 isShadowTrace 方法 - 有 override 值")
    void testIsShadowTraceWithOverride() {
        // 使用 withPrintMethod 方法设置 override 值
        Boolean result = failureContext.withPrintMethod(false, () -> {
            return failureContext.isShadowTrace();
        });
        // 验证结果是否为 override 值
        assertFalse(result);
    }

    @Test
    @DisplayName("测试 isShadowTrace 方法 - 无 override 值")
    void testIsShadowTraceWithoutOverride() {
        // 验证结果是否为 properties 的值
        assertTrue(failureContext.isShadowTrace());
    }

    @Test
    @DisplayName("测试 isDebugSnapshot 方法")
    void testIsDebugSnapshot() {
        // 验证结果是否为 properties 的值
        assertFalse(failureContext.isDebugSnapshot());
    }

    @Test
    @DisplayName("测试 traceId 相关方法")
    void testTraceIdMethods() {
        // 测试 setTraceId 和 getTraceId 方法
        String testTraceId = "test-trace-id";
        failureContext.setTraceId(testTraceId);
        assertEquals(testTraceId, failureContext.getTraceId());

        // 测试 clearThreadContext 方法
        failureContext.clearThreadContext();
        assertNull(failureContext.getTraceId());
    }

    @Test
    @DisplayName("测试 scene 相关方法")
    void testSceneMethods() {
        // 测试 setScene 和 getScene 方法
        String testScene = "test-scene";
        failureContext.setScene(testScene);
        assertEquals(testScene, failureContext.getScene());

        // 测试 clearThreadContext 方法
        failureContext.clearThreadContext();
        assertEquals("DEFAULT", failureContext.getScene());
    }

    @Test
    @DisplayName("测试 withPrintMethod 方法 - Supplier 版本")
    void testWithPrintMethodSupplier() {
        // 测试 withPrintMethod 方法
        String expectedResult = "test-result";
        String result = failureContext.withPrintMethod(true, () -> expectedResult);
        assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("测试 withPrintMethod 方法 - Runnable 版本")
    void testWithPrintMethodRunnable() {
        // 测试 withPrintMethod 方法
        final boolean[] runnableExecuted = {false};
        failureContext.withPrintMethod(true, () -> {
            runnableExecuted[0] = true;
        });
        assertTrue(runnableExecuted[0]);
    }

    @Test
    @DisplayName("测试 clearThreadContext 方法")
    void testClearThreadContext() {
        // 设置一些值
        failureContext.setTraceId("test-trace-id");
        failureContext.setScene("test-scene");

        // 执行 clearThreadContext 方法
        failureContext.clearThreadContext();

        // 验证所有线程本地变量都被清除
        assertNull(failureContext.getTraceId());
        assertEquals("DEFAULT", failureContext.getScene());
    }

    @Test
    @DisplayName("测试 methodEnabledOverride ThreadLocal 初始值")
    void testMethodEnabledOverrideThreadLocalInitialValue() throws Exception {
        var field = FailureContext.class.getDeclaredField("methodEnabledOverride");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        ThreadLocal<Boolean> tl = (ThreadLocal<Boolean>) field.get(failureContext);
        assertNull(tl.get());
    }
}
