package com.chao.failure.exsupport;

import java.lang.invoke.MethodHandle;

public final class FooValidators {
    private FooValidators() {
    }

    public static String call(MethodHandle handle) {
        try {
            return (String) handle.invokeExact();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}

