package com.chao.failfast.internal.core;

final class SomeValidator {

    private SomeValidator() {
    }

    static String capture() {
        return Ex.captureMethodName();
    }
}

