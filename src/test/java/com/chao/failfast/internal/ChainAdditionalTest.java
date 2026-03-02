package com.chao.failfast.internal;

import com.chao.failfast.Failure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Chain 额外补充测试")
class ChainAdditionalTest {

    @Test
    @DisplayName("verify() 方法测试")
    void testVerify() {
        Chain chain = Failure.begin();
        assertDoesNotThrow(chain::verify);
    }
}
