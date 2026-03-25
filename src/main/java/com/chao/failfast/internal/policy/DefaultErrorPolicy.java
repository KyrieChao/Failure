package com.chao.failfast.internal.policy;

import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;


public final class DefaultErrorPolicy implements ErrorPolicy {
    public static final DefaultErrorPolicy INSTANCE = new DefaultErrorPolicy();

    private DefaultErrorPolicy() {
    }

    @Override
    public ResponseCode defaultCode() {
        return ResponseCode.VALIDATION_ERROR_500_DYNAMIC;
    }

    @Override
    public String defaultDetail(ResponseCode code) {
        String detail = code.getDescription();
        if (detail == null) detail = code.getMessage();
        return detail;
    }

    @Override
    public boolean captureInvalidValue(FailureContext context) {
        return context == null || context.isDebugSnapshot();
    }
}

