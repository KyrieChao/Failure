package com.chao.failure.validator;

import com.chao.failure.internal.core.Ex;

public final class ValidatorPackageValidator {

    private ValidatorPackageValidator() {
    }

    public static String capture() {
        return Ex.captureMethodName();
    }
}

