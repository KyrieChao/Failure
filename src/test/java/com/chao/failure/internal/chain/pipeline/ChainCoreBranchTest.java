package com.chao.failure.internal.chain.pipeline;

import com.chao.failure.validator.FastValidator.ValidationContext;
import com.chao.failure.internal.core.ResponseCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class ChainCoreBranchTest {

    // 测试实现类
    private static class TestChainCore extends ChainCore<TestChainCore> {
        public TestChainCore(boolean failFast, ValidationContext context) {
            super(failFast, context);
        }

        @Override
        public TestChainCore core() {
            return self();
        }
    }

    @Test
    void testCheckWithSupplierAndResponseCodeWhenSkipped() {
        TestChainCore chain = new TestChainCore(true, null);
        
        // 测试shouldSkip为true的情况
        // 这里需要通过反射设置conditionState为false，因为shouldSkip会检查这个字段
        try {
            java.lang.reflect.Field conditionStateField = ChainCore.class.getDeclaredField("conditionState");
            conditionStateField.setAccessible(true);
            conditionStateField.set(chain, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 测试check(Supplier, ResponseCode)方法在shouldSkip为true时的行为
        TestChainCore result = chain.check(() -> true, ResponseCode.VALIDATION_ERROR_400);
        assertSame(chain, result);
    }

    @Test
    void testCheckWithSupplierAndResponseCodeWhenFailed() {
        TestChainCore chain = new TestChainCore(true, null);
        
        // 测试check(Supplier, ResponseCode)方法在条件为false时的行为
        TestChainCore result = chain.check(() -> false, ResponseCode.VALIDATION_ERROR_400);
        assertSame(chain, result);
    }

    @Test
    void testCheckWithSupplierAndResponseCodeInOrMode() {
        TestChainCore chain = new TestChainCore(true, null);
        
        // 测试OR模式下的行为
        // 这里需要通过反射设置orMode为true
        try {
            java.lang.reflect.Field orModeField = ChainCore.class.getDeclaredField("orMode");
            orModeField.setAccessible(true);
            orModeField.set(chain, true);
            
            java.lang.reflect.Field orHasSuccessField = ChainCore.class.getDeclaredField("orHasSuccess");
            orHasSuccessField.setAccessible(true);
            orHasSuccessField.set(chain, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 测试OR模式下条件为false的情况
        TestChainCore result = chain.check(() -> false, ResponseCode.VALIDATION_ERROR_400);
        assertSame(chain, result);
    }

    @Test
    void testCheckWithSupplierAndResponseCodeInOrModeWithSuccess() {
        TestChainCore chain = new TestChainCore(true, null);
        
        // 测试OR模式下的行为
        // 这里需要通过反射设置orMode为true和orHasSuccess为true
        try {
            java.lang.reflect.Field orModeField = ChainCore.class.getDeclaredField("orMode");
            orModeField.setAccessible(true);
            orModeField.set(chain, true);
            
            java.lang.reflect.Field orHasSuccessField = ChainCore.class.getDeclaredField("orHasSuccess");
            orHasSuccessField.setAccessible(true);
            orHasSuccessField.set(chain, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 测试OR模式下条件为false但之前有成功的情况
        TestChainCore result = chain.check(() -> false, ResponseCode.VALIDATION_ERROR_400);
        assertSame(chain, result);
    }
}
