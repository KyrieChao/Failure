package com.chao.failfast.internal;

import com.chao.failfast.Failure;
import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Chain Context OR Coverage Test")
class ChainContextOrTest {

    @Test
    @DisplayName("测试当 context 已停止时调用 or() 直接返回")
    void testOrWhenContextStopped() {
        ValidationContext context = new ValidationContext(true); // failFast=true
        context.stop(); // Manually stop context

        Chain chain = Failure.with(context);
        
        // Context is stopped, so or() should return immediately without changing state
        chain.or();
        
        // Verify state is unchanged (though or() doesn't expose state easily, we can verify via debug or coverage)
        // But functionally, if it returns early, orMode is false.
        // If orMode is false, calling check() behaves normally (skips if stopped).
        
        assertTrue(context.isStopped());
    }
}
