package com.chao.failure.spi;

import com.chao.failure.spi.validation.CancelToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CancelTokenTest {

    @Test
    void testCancelAndIsCancelled() {
        CancelToken token = new CancelToken();
        
        // 初始状态应该是未取消
        assertFalse(token.isCancelled());
        
        // 取消令牌
        token.cancel();
        
        // 状态应该变为已取消
        assertTrue(token.isCancelled());
        
        // 再次取消应该仍然是已取消状态
        token.cancel();
        assertTrue(token.isCancelled());
    }
}
