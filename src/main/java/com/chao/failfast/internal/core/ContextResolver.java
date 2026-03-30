package com.chao.failfast.internal.core;

import com.chao.failfast.integration.webflux.ReactiveTrace;
import reactor.util.context.ContextView;

public final class ContextResolver {

    private ContextResolver() {
    }

    public static String traceId(FailureContext ctx, ContextView view) {
        if (view != null) {
            Object v = view.getOrDefault(ReactiveTrace.TRACE_ID_KEY, null);
            if (v instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return ctx != null ? ctx.getTraceId() : null;
    }

    public static String scene(FailureContext ctx, ContextView view) {
        if (view != null) {
            Object v = view.getOrDefault(ReactiveTrace.SCENE_KEY, null);
            if (v instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return ctx != null ? ctx.getScene() : null;
    }

    public static boolean shadowTrace(FailureContext ctx, ContextView view) {
        if (view != null) {
            Object v = view.getOrDefault(ReactiveTrace.SHADOW_TRACE_KEY, null);
            if (v instanceof Boolean b) {
                return b;
            }
        }
        return ctx != null && ctx.isShadowTrace();
    }

    public static boolean methodEnabled(FailureContext ctx, ContextView view) {
        if (view != null) {
            Object v = view.getOrDefault(ReactiveTrace.METHOD_ENABLED_KEY, null);
            if (v instanceof Boolean b) {
                return b;
            }
        }
        return ctx != null && ctx.isMethodEnabled();
    }
}
