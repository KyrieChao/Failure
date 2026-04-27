package com.chao.failfast.integration.webflux;

/**
 * Reactive trace utility for context propagation.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */

import reactor.util.context.ContextView;

public final class ReactiveTrace {

    public static final String TRACE_ID_KEY = "fail-fast.traceId";
    public static final String SCENE_KEY = "fail-fast.scene";
    public static final String SHADOW_TRACE_KEY = "fail-fast.shadowTrace";
    public static final String METHOD_ENABLED_KEY = "fail-fast.methodEnabled";

    private ReactiveTrace() {
    }

    public static String getTraceId(ContextView contextView) {
        if (contextView == null) {
            return null;
        }
        Object v = contextView.getOrDefault(ReactiveTrace.TRACE_ID_KEY, null);
        return v instanceof String s ? s : null;
    }
}
