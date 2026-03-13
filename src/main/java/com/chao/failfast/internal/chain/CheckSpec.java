package com.chao.failfast.internal.chain;

import com.chao.failfast.internal.core.ResponseCode;

public record CheckSpec(ResponseCode code, String detail, Object invalidValue) {
    public static CheckSpec of(ResponseCode code, String detail) {
        return new CheckSpec(code, detail, null);
    }

    public static CheckSpec of(ResponseCode code, String detail, Object invalidValue) {
        return new CheckSpec(code, detail, invalidValue);
    }
}

