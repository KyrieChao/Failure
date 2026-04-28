package com.chao.failure.internal.core;

final class SomeValidator {

    private SomeValidator() {
    }

    static String capture() {
        return Ex.captureMethodName();
    }
}

