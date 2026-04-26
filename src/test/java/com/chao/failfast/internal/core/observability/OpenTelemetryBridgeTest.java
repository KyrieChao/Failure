package com.chao.failfast.internal.core.observability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 对 OpenTelemetryBridge 的完整单元测试，覆盖所有方法、行、分支、条件和路径。
 * 使用 Mockito 来模拟反射调用，部分场景结合真实的 OpenTelemetry API。
 */
@ExtendWith(MockitoExtension.class)
class OpenTelemetryBridgeTest {

    // ---------- 辅助方法：通过反射访问私有成员 ----------
    private static void setStaticField(String fieldName, Object value) throws Exception {
        Field field = OpenTelemetryBridge.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    private static Object getStaticField(String fieldName) throws Exception {
        Field field = OpenTelemetryBridge.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    private static Object invokePrivateStatic(String methodName, Class<?>[] paramTypes, Object... args)
            throws Exception {
        Method method = OpenTelemetryBridge.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    private static void resetBridgeState() throws Exception {
        setStaticField("initialized", false);
        setStaticField("spanCurrent", null);
        setStaticField("spanGetContext", null);
        setStaticField("spanContextIsValid", null);
        setStaticField("spanContextGetTraceId", null);
        setStaticField("spanContextGetSpanId", null);
    }

    // ---------- 每个测试前重置状态 ----------
    @BeforeEach
    void setUp() throws Exception {
        resetBridgeState();
    }

    // ---------- 1. 私有构造器 ----------
    @Test
    @DisplayName("私有构造器覆盖")
    void testPrivateConstructor() throws Exception {
        Constructor<OpenTelemetryBridge> ctor = OpenTelemetryBridge.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        OpenTelemetryBridge instance = ctor.newInstance();
        assertNotNull(instance);
    }

    // ---------- 2. isBlank 方法 ----------
    @Test
    @DisplayName("isBlank 覆盖 null/空白/非空白")
    void testIsBlank() throws Exception {
        assertTrue((Boolean) invokePrivateStatic("isBlank", new Class[]{String.class}, (String) null));
        assertTrue((Boolean) invokePrivateStatic("isBlank", new Class[]{String.class}, ""));
        assertTrue((Boolean) invokePrivateStatic("isBlank", new Class[]{String.class}, "   "));
        assertFalse((Boolean) invokePrivateStatic("isBlank", new Class[]{String.class}, "abc"));
    }

    // ---------- 3. blankToNull 方法 ----------
    @Test
    @DisplayName("blankToNull 转换空白为 null")
    void testBlankToNull() throws Exception {
        assertNull(invokePrivateStatic("blankToNull", new Class[]{String.class}, (String) null));
        assertNull(invokePrivateStatic("blankToNull", new Class[]{String.class}, ""));
        assertNull(invokePrivateStatic("blankToNull", new Class[]{String.class}, "  "));
        assertEquals("abc", invokePrivateStatic("blankToNull", new Class[]{String.class}, "abc"));
    }

    // ---------- 4. 初始化场景：基于实际依赖状态 ----------
    @Test
    @DisplayName("初始化场景：基于实际依赖状态")
    void testInitBasedOnActualDependency() throws Exception {
        resetBridgeState();
        
        // 第一次调用触发初始化
        assertNull(OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());

        // 检查初始化状态（如果有 OpenTelemetry 依赖，初始化会成功）
        boolean initialized = (Boolean) getStaticField("initialized");
        Object spanCurrent = getStaticField("spanCurrent");
        
        if (spanCurrent != null) {
            // OpenTelemetry 依赖存在，初始化成功
            assertTrue(initialized);
            assertNotNull(getStaticField("spanGetContext"));
            assertNotNull(getStaticField("spanContextIsValid"));
            assertNotNull(getStaticField("spanContextGetTraceId"));
            assertNotNull(getStaticField("spanContextGetSpanId"));
        } else {
            // OpenTelemetry 依赖不存在，初始化失败
            assertFalse(initialized);
            assertNull(getStaticField("spanGetContext"));
            assertNull(getStaticField("spanContextIsValid"));
            assertNull(getStaticField("spanContextGetTraceId"));
            assertNull(getStaticField("spanContextGetSpanId"));
        }
    }

    // ---------- 5. 初始化成功后不再重复加载 ----------
    @Test
    @DisplayName("首次初始化成功后，后续调用不再重复初始化")
    void testInitOnlyOnceWhenSuccessful() throws Exception {
        resetBridgeState();
        // 利用真实 OpenTelemetry 类路径完成初始化
        OpenTelemetryBridge.currentTraceId(); // 触发加载，只要类路径中有 otel api 即成功

        // 验证初始化状态
        assertEquals(true, getStaticField("initialized"));
        assertNotNull(getStaticField("spanCurrent"));
        assertNotNull(getStaticField("spanGetContext"));
        assertNotNull(getStaticField("spanContextIsValid"));
        assertNotNull(getStaticField("spanContextGetTraceId"));
        assertNotNull(getStaticField("spanContextGetSpanId"));

        // 再次调用，应使用缓存的结果
        OpenTelemetryBridge.currentTraceId();
        OpenTelemetryBridge.currentSpanId();

        // 验证状态保持不变
        assertEquals(true, getStaticField("initialized"));
        assertNotNull(getStaticField("spanCurrent"));
    }

    // ---------- 6. 初始化状态验证 ----------
    @Test
    @DisplayName("初始化状态验证")
    void testInitState() throws Exception {
        resetBridgeState();
        // 触发初始化
        assertNull(OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());

        // 验证初始化状态（根据实际依赖情况）
        boolean initialized = (Boolean) getStaticField("initialized");
        Object spanCurrent = getStaticField("spanCurrent");
        
        if (spanCurrent != null) {
            // OpenTelemetry 依赖存在，初始化成功
            assertTrue(initialized);
            assertNotNull(getStaticField("spanGetContext"));
            assertNotNull(getStaticField("spanContextIsValid"));
            assertNotNull(getStaticField("spanContextGetTraceId"));
            assertNotNull(getStaticField("spanContextGetSpanId"));
        } else {
            // OpenTelemetry 依赖不存在，初始化失败
            assertFalse(initialized);
            assertNull(getStaticField("spanGetContext"));
            assertNull(getStaticField("spanContextIsValid"));
            assertNull(getStaticField("spanContextGetTraceId"));
            assertNull(getStaticField("spanContextGetSpanId"));
        }

        // 再次调用，验证行为一致性
        assertNull(OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());
    }

    // ---------- 7. 模拟 Method 为 null 的场景 ----------
    @Test
    @DisplayName("spanCurrent 为 null 时，返回 null")
    void testSpanCurrentNull() throws Exception {
        setStaticField("initialized", true);
        setStaticField("spanCurrent", null);

        assertNull(OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());
    }

    // ---------- 8. 模拟 spanGetContext 为 null 的场景 ----------
    @Test
    @DisplayName("spanGetContext 为 null 时，返回 null")
    void testSpanGetContextNull() throws Exception {
        setStaticField("initialized", true);
        setStaticField("spanCurrent", Object.class.getMethod("toString"));
        setStaticField("spanGetContext", null);

        assertNull(OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());
    }

    // ---------- 9. 模拟 spanContextIsValid 为 null 的场景 ----------
    @Test
    @DisplayName("spanContextIsValid 为 null 时，返回 null")
    void testSpanContextIsValidNull() throws Exception {
        setStaticField("initialized", true);
        setStaticField("spanCurrent", Object.class.getMethod("toString"));
        setStaticField("spanGetContext", Object.class.getMethod("toString"));
        setStaticField("spanContextIsValid", null);

        assertNull(OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());
    }

    // ---------- 10. 模拟 spanContextGetTraceId 为 null 的场景 ----------
    @Test
    @DisplayName("spanContextGetTraceId 为 null 时，返回 null")
    void testSpanContextGetTraceIdNull() throws Exception {
        setStaticField("initialized", true);
        setStaticField("spanCurrent", Object.class.getMethod("toString"));
        setStaticField("spanGetContext", Object.class.getMethod("toString"));
        setStaticField("spanContextIsValid", Object.class.getMethod("toString"));
        setStaticField("spanContextGetTraceId", null);

        assertNull(OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());
    }

    // ---------- 11. 模拟 spanContextGetSpanId 为 null 的场景 ----------
    @Test
    @DisplayName("spanContextGetSpanId 为 null 时，返回 null")
    void testSpanContextGetSpanIdNull() throws Exception {
        setStaticField("initialized", true);
        setStaticField("spanCurrent", Object.class.getMethod("toString"));
        setStaticField("spanGetContext", Object.class.getMethod("toString"));
        setStaticField("spanContextIsValid", Object.class.getMethod("toString"));
        setStaticField("spanContextGetTraceId", Object.class.getMethod("toString"));
        setStaticField("spanContextGetSpanId", null);

        assertNull(OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());
    }

    // ---------- 16. TracePair record 覆盖 ----------
    @Test
    @DisplayName("TracePair record 的构造和方法覆盖")
    void testTracePairRecord() throws Exception {
        // 通过 currentPair 的正常路径自然会创建 TracePair，但为行覆盖可以显式反射
        Class<?> tracePairClass = Class.forName(
                "com.chao.failfast.internal.core.observability.OpenTelemetryBridge$TracePair"
        );
        Constructor<?> ctor = tracePairClass.getDeclaredConstructor(String.class, String.class);
        ctor.setAccessible(true);
        Object pair = ctor.newInstance("tid", "sid");
        Method traceIdMethod = tracePairClass.getDeclaredMethod("traceId");
        Method spanIdMethod = tracePairClass.getDeclaredMethod("spanId");
        assertEquals("tid", traceIdMethod.invoke(pair));
        assertEquals("sid", spanIdMethod.invoke(pair));

        // 同时也触发 equals/hashCode/toString 的覆盖（自动生成）
        Object pair2 = ctor.newInstance("tid", "sid");
        assertEquals(pair, pair2);
        assertEquals(pair.hashCode(), pair2.hashCode());
        assertNotNull(pair.toString());
    }

    // ---------- 17. 真实 OpenTelemetry 无效 span 场景（不通过 mock） ----------
    @Test
    @DisplayName("真实 OTel 且上下文中无有效 span，应该返回 null")
    void testWithRealOpenTelemetryInvalidSpan() {
        // 因为当前没有设置有效 span，Span.current() 返回 invalid span
        // isValid 为 false，最终应返回 null
        assertNull(OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());
    }

    @Test
    void should_returnTraceAndSpan_when_fakeSpanContextIsValid() throws Exception {
        resetBridgeState();
        setStaticField("initialized", true);
        setStaticField("spanCurrent", FakeSpanProvider.class.getMethod("current"));
        setStaticField("spanGetContext", FakeSpan.class.getMethod("getSpanContext"));
        setStaticField("spanContextIsValid", FakeSpanContext.class.getMethod("isValid"));
        setStaticField("spanContextGetTraceId", FakeSpanContext.class.getMethod("getTraceId"));
        setStaticField("spanContextGetSpanId", FakeSpanContext.class.getMethod("getSpanId"));
        FakeSpanProvider.current = new FakeSpan(new FakeSpanContext(true, "trace-x", "span-y"));

        assertEquals("trace-x", OpenTelemetryBridge.currentTraceId());
        assertEquals("span-y", OpenTelemetryBridge.currentSpanId());
    }

    @Test
    void should_returnNull_when_fakeSpanContextHasOnlyBlankIds() throws Exception {
        resetBridgeState();
        setStaticField("initialized", true);
        setStaticField("spanCurrent", FakeSpanProvider.class.getMethod("current"));
        setStaticField("spanGetContext", FakeSpan.class.getMethod("getSpanContext"));
        setStaticField("spanContextIsValid", FakeSpanContext.class.getMethod("isValid"));
        setStaticField("spanContextGetTraceId", FakeSpanContext.class.getMethod("getTraceId"));
        setStaticField("spanContextGetSpanId", FakeSpanContext.class.getMethod("getSpanId"));
        FakeSpanProvider.current = new FakeSpan(new FakeSpanContext(true, " ", ""));

        assertNull(OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());
    }

    @Test
    void should_returnNull_when_validityMethodDoesNotReturnBoolean() throws Exception {
        resetBridgeState();
        setStaticField("initialized", true);
        setStaticField("spanCurrent", FakeSpanProvider.class.getMethod("current"));
        setStaticField("spanGetContext", FakeSpan.class.getMethod("getSpanContext"));
        setStaticField("spanContextIsValid", FakeSpanContext.class.getMethod("isValid"));
        setStaticField("spanContextGetTraceId", FakeSpanContext.class.getMethod("getTraceId"));
        setStaticField("spanContextGetSpanId", FakeSpanContext.class.getMethod("getSpanId"));
        FakeSpanProvider.current = new FakeSpan(new FakeSpanContext("not-boolean", "trace-x", "span-y"));

        assertNull(OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());
    }

    @Test
    void should_returnNull_when_reflectionInvocationThrows() throws Exception {
        resetBridgeState();
        setStaticField("initialized", true);
        setStaticField("spanCurrent", FakeSpanProvider.class.getMethod("current"));
        setStaticField("spanGetContext", FakeSpan.class.getMethod("getSpanContext"));
        setStaticField("spanContextIsValid", FakeSpanContext.class.getMethod("isValid"));
        setStaticField("spanContextGetTraceId", ThrowingSpanContext.class.getMethod("getTraceId"));
        setStaticField("spanContextGetSpanId", FakeSpanContext.class.getMethod("getSpanId"));
        FakeSpanProvider.current = new FakeSpan(new ThrowingSpanContext(true, "trace-x", "span-y"));

        assertNull(OpenTelemetryBridge.currentTraceId());
    }

    @Test
    void should_returnNull_when_currentSpanIsNull() throws Exception {
        resetBridgeState();
        setStaticField("initialized", true);
        setStaticField("spanCurrent", FakeSpanProvider.class.getMethod("current"));
        setStaticField("spanGetContext", FakeSpan.class.getMethod("getSpanContext"));
        setStaticField("spanContextIsValid", FakeSpanContext.class.getMethod("isValid"));
        setStaticField("spanContextGetTraceId", FakeSpanContext.class.getMethod("getTraceId"));
        setStaticField("spanContextGetSpanId", FakeSpanContext.class.getMethod("getSpanId"));
        FakeSpanProvider.current = null;

        assertNull(OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());
    }

    @Test
    void should_returnNull_when_spanContextIsNull() throws Exception {
        resetBridgeState();
        setStaticField("initialized", true);
        setStaticField("spanCurrent", FakeSpanProvider.class.getMethod("current"));
        setStaticField("spanGetContext", FakeSpan.class.getMethod("getSpanContext"));
        setStaticField("spanContextIsValid", FakeSpanContext.class.getMethod("isValid"));
        setStaticField("spanContextGetTraceId", FakeSpanContext.class.getMethod("getTraceId"));
        setStaticField("spanContextGetSpanId", FakeSpanContext.class.getMethod("getSpanId"));
        FakeSpanProvider.current = new FakeSpan(null);

        assertNull(OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());
    }

    @Test
    void should_returnPartialTracePair_when_only_one_identifierIsBlank() throws Exception {
        resetBridgeState();
        setStaticField("initialized", true);
        setStaticField("spanCurrent", FakeSpanProvider.class.getMethod("current"));
        setStaticField("spanGetContext", FakeSpan.class.getMethod("getSpanContext"));
        setStaticField("spanContextIsValid", FakeSpanContext.class.getMethod("isValid"));
        setStaticField("spanContextGetTraceId", FakeSpanContext.class.getMethod("getTraceId"));
        setStaticField("spanContextGetSpanId", FakeSpanContext.class.getMethod("getSpanId"));
        FakeSpanProvider.current = new FakeSpan(new FakeSpanContext(true, "trace-only", " "));

        assertEquals("trace-only", OpenTelemetryBridge.currentTraceId());
        assertNull(OpenTelemetryBridge.currentSpanId());
    }

    @Test
    void should_returnPartialTracePair_when_only_traceIdentifierIsBlank() throws Exception {
        resetBridgeState();
        setStaticField("initialized", true);
        setStaticField("spanCurrent", FakeSpanProvider.class.getMethod("current"));
        setStaticField("spanGetContext", FakeSpan.class.getMethod("getSpanContext"));
        setStaticField("spanContextIsValid", FakeSpanContext.class.getMethod("isValid"));
        setStaticField("spanContextGetTraceId", FakeSpanContext.class.getMethod("getTraceId"));
        setStaticField("spanContextGetSpanId", FakeSpanContext.class.getMethod("getSpanId"));
        FakeSpanProvider.current = new FakeSpan(new FakeSpanContext(true, " ", "span-only"));

        assertNull(OpenTelemetryBridge.currentTraceId());
        assertEquals("span-only", OpenTelemetryBridge.currentSpanId());
    }

    @Test
    void should_coverInitFailureBranch_when_bridgeLoadedWithoutOpenTelemetry() throws Exception {
        URL classesUrl = Path.of("target", "classes").toAbsolutePath().toUri().toURL();
        try (URLClassLoader loader = new URLClassLoader(new URL[]{classesUrl}, null)) {
            Class<?> isolatedBridge = Class.forName(
                    "com.chao.failfast.internal.core.observability.OpenTelemetryBridge", true, loader);

            Method currentTraceId = isolatedBridge.getDeclaredMethod("currentTraceId");
            Object traceId = currentTraceId.invoke(null);

            Field initialized = isolatedBridge.getDeclaredField("initialized");
            initialized.setAccessible(true);
            Field spanCurrent = isolatedBridge.getDeclaredField("spanCurrent");
            spanCurrent.setAccessible(true);

            assertNull(traceId);
            assertFalse((boolean) initialized.get(null));
            assertNull(spanCurrent.get(null));
        }
    }

    static class FakeSpanProvider {
        static Object current;

        public static Object current() {
            return current;
        }
    }

    static class FakeSpan {
        private final Object spanContext;

        FakeSpan(Object spanContext) {
            this.spanContext = spanContext;
        }

        public Object getSpanContext() {
            return spanContext;
        }
    }

    static class FakeSpanContext {
        private final Object valid;
        private final String traceId;
        private final String spanId;

        FakeSpanContext(Object valid, String traceId, String spanId) {
            this.valid = valid;
            this.traceId = traceId;
            this.spanId = spanId;
        }

        public Object isValid() {
            return valid;
        }

        public String getTraceId() {
            return traceId;
        }

        public String getSpanId() {
            return spanId;
        }
    }

    static class ThrowingSpanContext extends FakeSpanContext {
        ThrowingSpanContext(Object valid, String traceId, String spanId) {
            super(valid, traceId, spanId);
        }

        @Override
        public String getTraceId() {
            throw new IllegalStateException("boom");
        }
    }
}
