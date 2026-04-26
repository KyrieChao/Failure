package com.chao.failfast.integration.webflux;

import org.junit.jupiter.api.Test;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import static org.junit.jupiter.api.Assertions.*;

class ReactiveTraceUtilTest {

    @Test
    void testGetTraceIdWithNullContext() {
        String traceId = ReactiveTrace.getTraceId(null);
        assertNull(traceId);
    }

    @Test
    void testGetTraceIdWithEmptyContext() {
        ContextView contextView = Context.empty();
        String traceId = ReactiveTrace.getTraceId(contextView);
        assertNull(traceId);
    }

    @Test
    void testGetTraceIdWithTraceIdInContext() {
        String expectedTraceId = "test-trace-id";
        ContextView contextView = Context.of(ReactiveTrace.TRACE_ID_KEY, expectedTraceId);
        String traceId = ReactiveTrace.getTraceId(contextView);
        assertEquals(expectedTraceId, traceId);
    }

    @Test
    void testGetTraceIdWithNonStringTraceId() {
        ContextView contextView = Context.of(ReactiveTrace.TRACE_ID_KEY, 123);
        String traceId = ReactiveTrace.getTraceId(contextView);
        assertNull(traceId);
    }
}
