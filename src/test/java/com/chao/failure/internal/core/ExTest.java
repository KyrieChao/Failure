package com.chao.failure.internal.core;

import com.chao.failure.spi.filter.SkipPrefixRegistry;
import com.chao.failure.spi.filter.SkipTypeRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExTest {

    @Test
    void testSetContext() {
        FailureContext context = Mockito.mock(FailureContext.class);
        Ex.setContext(context);
        assertSame(context, Ex.getContext());
    }

    @Test
    void testSetSkipPrefixRegistry() {
        SkipPrefixRegistry registry = Mockito.mock(SkipPrefixRegistry.class);
        Ex.setSkipPrefixRegistry(registry);
        assertSame(registry, Ex.getSkipPrefixRegistry());
    }

    @Test
    void testSetSkipTypeRegistry() {
        SkipTypeRegistry registry = Mockito.mock(SkipTypeRegistry.class);
        Ex.setSkipTypeRegistry(registry);
        assertSame(registry, Ex.getSkipTypeRegistry());
    }

    @Test
    void testLocationWithShadowTraceDisabled() {
        FailureContext context = Mockito.mock(FailureContext.class);
        when(context.isShadowTrace()).thenReturn(false);
        Ex.setContext(context);
        assertNull(Ex.location());
    }

    @Test
    void testMethodWithShadowTraceDisabled() {
        FailureContext context = Mockito.mock(FailureContext.class);
        when(context.isShadowTrace()).thenReturn(false);
        Ex.setContext(context);
        assertNull(Ex.method());
    }

    @Test
    void testCaptureLocationWithShadowTraceDisabled() {
        FailureContext context = Mockito.mock(FailureContext.class);
        when(context.isShadowTrace()).thenReturn(false);
        Ex.setContext(context);
        assertNull(Ex.captureLocation());
    }

    @Test
    void testCaptureMethodNameWithShadowTraceDisabled() {
        FailureContext context = Mockito.mock(FailureContext.class);
        when(context.isShadowTrace()).thenReturn(false);
        Ex.setContext(context);
        assertNull(Ex.captureMethodName());
    }

    @Test
    void testLocation() {
        FailureContext context = Mockito.mock(FailureContext.class);
        when(context.isShadowTrace()).thenReturn(true);
        Ex.setContext(context);
        String location = Ex.location();
        // 可能返回null，因为stack frame可能被跳过
        assertNotNull(location);
    }

    @Test
    void testMethod() {
        FailureContext context = Mockito.mock(FailureContext.class);
        when(context.isShadowTrace()).thenReturn(true);
        Ex.setContext(context);
        String method = Ex.method();
        // 可能返回null，因为stack frame可能被跳过
        assertNotNull(method);
    }

    @Test
    void testCaptureLocation() {
        FailureContext context = Mockito.mock(FailureContext.class);
        when(context.isShadowTrace()).thenReturn(true);
        Ex.setContext(context);
        String location = Ex.captureLocation();
        // 可能返回null，因为stack frame可能被跳过
        assertNotNull(location);
    }

    @Test
    void testCaptureMethodName() {
        FailureContext context = Mockito.mock(FailureContext.class);
        when(context.isShadowTrace()).thenReturn(true);
        Ex.setContext(context);
        String methodName = Ex.captureMethodName();
        // 可能返回null，因为stack frame可能被跳过
        assertNotNull(methodName);
    }
}
