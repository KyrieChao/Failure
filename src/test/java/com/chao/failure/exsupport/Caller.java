package com.chao.failure.exsupport;

import java.lang.invoke.MethodHandle;

public final class Caller {
    private Caller() {
    }

    public static String call(MethodHandle handle) {
        return FooValidators.call(handle);
    }
}

