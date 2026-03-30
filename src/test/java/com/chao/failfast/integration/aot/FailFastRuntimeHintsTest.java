package com.chao.failfast.integration.aot;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

import static org.junit.jupiter.api.Assertions.fail;

class FailFastRuntimeHintsTest {

    @Test
    void testRegisterHints() {
        // 准备
        FailFastRuntimeHints hintsRegistrar = new FailFastRuntimeHints();
        RuntimeHints hints = new RuntimeHints();
        
        // 执行 - 验证方法不会抛出异常
        try {
            hintsRegistrar.registerHints(hints, null);
        } catch (Exception e) {
            // 不应该抛出异常
            fail("registerHints should not throw exception");
        }
    }
}

