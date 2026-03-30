package com.chao.failfast.integration.webflux;

import reactor.util.context.ContextView;

public final class ReactiveTraceUtil {

    private ReactiveTraceUtil() {
    }

    public static String getTraceId(ContextView contextView) {
        if (contextView == null) {
            return null;
        }
        Object v = contextView.getOrDefault(ReactiveTrace.TRACE_ID_KEY, null);
        return v instanceof String s ? s : null;
    }
}

