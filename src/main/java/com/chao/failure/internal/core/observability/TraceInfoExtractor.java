package com.chao.failure.internal.core.observability;

import com.chao.failure.integration.webflux.ReactiveTrace;
import com.chao.failure.internal.core.FailureContext;
import reactor.util.context.ContextView;

/**
 * TraceInfoExtractor class is a utility class for parsing and obtaining trace ID, scene, shadow trace, and method enabled status from context information.
 * This class provides static methods to extract these information from FailureContext or ContextView.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public final class TraceInfoExtractor {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private TraceInfoExtractor() {
    }

    /**
     * Get trace ID from FailureContext or ContextView.
     * First try to get from ContextView, if not exists or empty, get from FailureContext.
     *
     * @param ctx FailureContext object containing trace information
     * @param view ContextView object containing context view
     * @return Trace ID string, returns null if not exists
     */
    public static String traceId(FailureContext ctx, ContextView view) {
        if (view != null) {
            Object v = view.getOrDefault(ReactiveTrace.TRACE_ID_KEY, null);
            if (v instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return ctx != null ? ctx.getTraceId() : null;
    }

    /**
     * Get scene information from FailureContext or ContextView.
     * First try to get from ContextView, if not exists or empty, get from FailureContext.
     *
     * @param ctx FailureContext object containing scene information
     * @param view ContextView object containing context view
     * @return Scene string, returns null if not exists
     */
    public static String scene(FailureContext ctx, ContextView view) {
        if (view != null) {
            Object v = view.getOrDefault(ReactiveTrace.SCENE_KEY, null);
            if (v instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return ctx != null ? ctx.getScene() : null;
    }

    /**
     * Get whether shadow trace is enabled from FailureContext or ContextView.
     * First try to get from ContextView, if not exists, get from FailureContext.
     *
     * @param ctx FailureContext object containing shadow trace information
     * @param view ContextView object containing context view
     * @return true if shadow trace is enabled, false otherwise
     */
    public static boolean shadowTrace(FailureContext ctx, ContextView view) {
        if (view != null) {
            Object v = view.getOrDefault(ReactiveTrace.SHADOW_TRACE_KEY, null);
            if (v instanceof Boolean b) {
                return b;
            }
        }
        return ctx != null && ctx.isShadowTrace();
    }

    /**
     * Get whether method is enabled from FailureContext or ContextView.
     * First try to get from ContextView, if not exists, get from FailureContext.
     *
     * @param ctx FailureContext object containing method enabled information
     * @param view ContextView object containing context view
     * @return true if method is enabled, false otherwise
     */
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
