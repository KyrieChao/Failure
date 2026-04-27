package com.chao.failfast.internal.policy;

/**
 * Error policy interface for error handling strategies.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */

import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;

public interface ErrorPolicy {

    ResponseCode defaultCode();

    String defaultDetail(ResponseCode code);

    boolean captureInvalidValue(FailureContext context);
}

