package com.chao.failfast.validator;

import com.chao.failfast.internal.core.Ex;

public final class ValidatorPackageValidator {

    private ValidatorPackageValidator() {
    }

    public static String capture() {
        return Ex.captureMethodName();
    }
}

