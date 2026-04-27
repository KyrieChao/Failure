package com.chao.failfast.internal.policy;

import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;


/**
 * Default error policy implementation class
 * This class provides default error handling policies, including default error code, default error details, and logic for capturing invalid values
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class DefaultErrorPolicy implements ErrorPolicy {
    // Singleton pattern instance
    public static final DefaultErrorPolicy INSTANCE = new DefaultErrorPolicy();

    /**
     * Private constructor to prevent external instantiation
     */
    private DefaultErrorPolicy() {
    }

    /**
     * Get default error code
     * @return Returns validation error code: VALIDATION_ERROR_500_DYNAMIC
     */
    @Override
    public ResponseCode defaultCode() {
        return ResponseCode.VALIDATION_ERROR_500_DYNAMIC;
    }

    /**
     * Get default error details
     * @param code Error code
     * @return Returns error code description, returns error code message if description is empty
     */
    @Override
    public String defaultDetail(ResponseCode code) {
        String detail = code.getDescription();
        if (detail == null) detail = code.getMessage();
        return detail;
    }

    /**
     * Determine whether to capture invalid value
     * @param context Failure context
     * @return Returns true if context is null or debug snapshot is enabled, otherwise returns false
     */
    @Override
    public boolean captureInvalidValue(FailureContext context) {
        return context == null || context.isDebugSnapshot();
    }
}

