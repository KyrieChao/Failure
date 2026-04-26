package com.chao.failfast.internal.core;

import com.chao.failfast.integration.webflux.ReactiveTrace;
import com.chao.failfast.internal.core.observability.TraceInfoExtractor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraceInfoExtractorTest {

    @Test
    void testTraceIdWithNullContexts() {
        String traceId = TraceInfoExtractor.traceId(null, null);
        assertNull(traceId);
    }

    @Test
    void testTraceIdWithViewOnly() {
        ContextView view = Context.of(ReactiveTrace.TRACE_ID_KEY, "test-trace-id");
        String traceId = TraceInfoExtractor.traceId(null, view);
        assertEquals("test-trace-id", traceId);
    }

    @Test
    void testTraceIdWithBlankTraceId() {
        ContextView view = Context.of(ReactiveTrace.TRACE_ID_KEY, "");
        FailureContext ctx = Mockito.mock(FailureContext.class);
        when(ctx.getTraceId()).thenReturn("context-trace-id");
        String traceId = TraceInfoExtractor.traceId(ctx, view);
        assertEquals("context-trace-id", traceId);
    }

    @Test
    void testTraceIdWithNonStringTraceId() {
        ContextView view = Context.of(ReactiveTrace.TRACE_ID_KEY, 123);
        FailureContext ctx = Mockito.mock(FailureContext.class);
        when(ctx.getTraceId()).thenReturn("context-trace-id");
        String traceId = TraceInfoExtractor.traceId(ctx, view);
        assertEquals("context-trace-id", traceId);
    }

    @Test
    void testTraceIdWithContextOnly() {
        FailureContext ctx = Mockito.mock(FailureContext.class);
        when(ctx.getTraceId()).thenReturn("context-trace-id");
        String traceId = TraceInfoExtractor.traceId(ctx, null);
        assertEquals("context-trace-id", traceId);
    }

    @Test
    void testSceneWithNullContexts() {
        String scene = TraceInfoExtractor.scene(null, null);
        assertNull(scene);
    }

    @Test
    void testSceneWithViewOnly() {
        ContextView view = Context.of(ReactiveTrace.SCENE_KEY, "test-scene");
        String scene = TraceInfoExtractor.scene(null, view);
        assertEquals("test-scene", scene);
    }

    @Test
    void testSceneWithBlankScene() {
        ContextView view = Context.of(ReactiveTrace.SCENE_KEY, "");
        FailureContext ctx = Mockito.mock(FailureContext.class);
        when(ctx.getScene()).thenReturn("context-scene");
        String scene = TraceInfoExtractor.scene(ctx, view);
        assertEquals("context-scene", scene);
    }

    @Test
    void testSceneWithNonStringScene() {
        ContextView view = Context.of(ReactiveTrace.SCENE_KEY, 123);
        FailureContext ctx = Mockito.mock(FailureContext.class);
        when(ctx.getScene()).thenReturn("context-scene");
        String scene = TraceInfoExtractor.scene(ctx, view);
        assertEquals("context-scene", scene);
    }

    @Test
    void testSceneWithContextOnly() {
        FailureContext ctx = Mockito.mock(FailureContext.class);
        when(ctx.getScene()).thenReturn("context-scene");
        String scene = TraceInfoExtractor.scene(ctx, null);
        assertEquals("context-scene", scene);
    }

    @Test
    void testShadowTraceWithNullContexts() {
        boolean shadowTrace = TraceInfoExtractor.shadowTrace(null, null);
        assertFalse(shadowTrace);
    }

    @Test
    void testShadowTraceWithViewOnly() {
        ContextView view = Context.of(ReactiveTrace.SHADOW_TRACE_KEY, true);
        boolean shadowTrace = TraceInfoExtractor.shadowTrace(null, view);
        assertTrue(shadowTrace);
    }

    @Test
    void testShadowTraceWithNonBooleanShadowTrace() {
        ContextView view = Context.of(ReactiveTrace.SHADOW_TRACE_KEY, "true");
        FailureContext ctx = Mockito.mock(FailureContext.class);
        when(ctx.isShadowTrace()).thenReturn(true);
        boolean shadowTrace = TraceInfoExtractor.shadowTrace(ctx, view);
        assertTrue(shadowTrace);
    }

    @Test
    void testShadowTraceWithContextOnly() {
        FailureContext ctx = Mockito.mock(FailureContext.class);
        when(ctx.isShadowTrace()).thenReturn(true);
        boolean shadowTrace = TraceInfoExtractor.shadowTrace(ctx, null);
        assertTrue(shadowTrace);
    }

    @Test
    void testMethodEnabledWithNullContexts() {
        boolean methodEnabled = TraceInfoExtractor.methodEnabled(null, null);
        assertFalse(methodEnabled);
    }

    @Test
    void testMethodEnabledWithViewOnly() {
        ContextView view = Context.of(ReactiveTrace.METHOD_ENABLED_KEY, true);
        boolean methodEnabled = TraceInfoExtractor.methodEnabled(null, view);
        assertTrue(methodEnabled);
    }

    @Test
    void testMethodEnabledWithNonBooleanMethodEnabled() {
        ContextView view = Context.of(ReactiveTrace.METHOD_ENABLED_KEY, "true");
        FailureContext ctx = Mockito.mock(FailureContext.class);
        when(ctx.isMethodEnabled()).thenReturn(true);
        boolean methodEnabled = TraceInfoExtractor.methodEnabled(ctx, view);
        assertTrue(methodEnabled);
    }

    @Test
    void testMethodEnabledWithContextOnly() {
        FailureContext ctx = Mockito.mock(FailureContext.class);
        when(ctx.isMethodEnabled()).thenReturn(true);
        boolean methodEnabled = TraceInfoExtractor.methodEnabled(ctx, null);
        assertTrue(methodEnabled);
    }

    @Test
    void testMethodEnabledWithContextOnlyFalse() {
        FailureContext ctx = Mockito.mock(FailureContext.class);
        when(ctx.isMethodEnabled()).thenReturn(false);
        boolean methodEnabled = TraceInfoExtractor.methodEnabled(ctx, null);
        assertFalse(methodEnabled);
    }
}
