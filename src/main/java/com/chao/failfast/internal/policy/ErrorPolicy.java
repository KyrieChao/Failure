package com.chao.failfast.internal.policy;

import com.chao.failfast.internal.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;

public interface ErrorPolicy {

    ResponseCode defaultCode();

    String defaultDetail(ResponseCode code);

    boolean captureInvalidValue(FailureContext context);
}

