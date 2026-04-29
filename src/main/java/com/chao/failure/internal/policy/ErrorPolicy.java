package com.chao.failure.internal.policy;

/**
 * Error policy interface for error handling strategies.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */

import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.internal.core.ResponseCode;

public interface ErrorPolicy {

    ResponseCode defaultCode();

    String defaultDetail(ResponseCode code);

    boolean captureInvalidValue(FailureContext context);
}

