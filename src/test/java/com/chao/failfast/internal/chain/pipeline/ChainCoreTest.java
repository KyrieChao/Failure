package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.constant.Scenario;
import com.chao.failfast.exception.Business;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.internal.validation.RecursiveOptions;
import com.chao.failfast.validator.TypedValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChainCoreTest {

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
    void testErrorSize() {
        TestChainCore chain = new TestChainCore(true, null);
        assertEquals(0, chain.errorSize());
    }

    @Test
    void testCheckAsyncWithNullStage() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.checkAsync(null, ResponseCode.VALIDATION_ERROR_400);
        assertSame(chain, result);
    }

    @Test
    void testCheckAsyncWithValidStage() {
        TestChainCore chain = new TestChainCore(true, null);
        CompletionStage<Boolean> stage = CompletableFuture.completedFuture(true);
        TestChainCore result = chain.checkAsync(stage, ResponseCode.VALIDATION_ERROR_400);
        assertSame(chain, result);
    }

    @Test
    void testApplyAsyncChecks() {
        TestChainCore chain = new TestChainCore(true, null);
        CompletableFuture<Void> future = chain.applyAsyncChecks();
        assertNotNull(future);
    }

    @Test
    void testWhen() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.when(true);
        assertSame(chain, result);
        assertTrue(chain.isConditionState());
    }

    @Test
    void testIfTrue() {
        TestChainCore chain = new TestChainCore(true, null);
        boolean[] executed = {false};
        TestChainCore result = chain.ifTrue(true, c -> { executed[0] = true; });
        assertSame(chain, result);
        assertTrue(executed[0]);
    }

    @Test
    void testOr() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.or();
        assertSame(chain, result);
    }

    @Test
    void testShouldSkip() {
        TestChainCore chain = new TestChainCore(true, null);
        assertFalse(chain.shouldSkip());
    }

    @Test
    void testCheckWithSupplier() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.check(() -> true, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertSame(chain, result);
    }

    @Test
    void testCheckWithSupplierAndResponseCode() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.check(() -> true, ResponseCode.VALIDATION_ERROR_400);
        assertSame(chain, result);
    }

    @Test
    void testCheckWithCondition() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.check(true, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertSame(chain, result);
    }

    @Test
    void testCheckRef() {
        TestChainCore chain = new TestChainCore(true, null);
        PathEntry<String> pathEntry = new PathEntry<>("test", "value");
        TestChainCore result = chain.checkRef(true, ResponseCode.VALIDATION_ERROR_400, pathEntry);
        assertSame(chain, result);
    }

    @Test
    void testCheckWithPathAndConstraint() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.checkWithPathAndConstraint(true, ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint");
        assertSame(chain, result);
    }

    @Test
    void testCheck() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.check(true);
        assertSame(chain, result);
    }

    @Test
    void testGetCauses() {
        TestChainCore chain = new TestChainCore(true, null);
        List<Business> causes = chain.getCauses();
        assertNotNull(causes);
        assertTrue(causes.isEmpty());
    }

    @Test
    void testIsValid() {
        TestChainCore chain = new TestChainCore(true, null);
        assertTrue(chain.isValid());
    }

    @Test
    void testStopOnFail() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.stopOnFail();
        assertSame(chain, result);
    }

    @Test
    void testResume() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.resume();
        assertSame(chain, result);
    }

    @Test
    void testWhenScene() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.whenScene(Scenario.DEFAULT);
        assertSame(chain, result);
    }

    @Test
    void testInScene() {
        TestChainCore chain = new TestChainCore(true, null);
        boolean[] executed = {false};
        TestChainCore result = chain.inScene(Scenario.DEFAULT, c -> { executed[0] = true; });
        assertSame(chain, result);
        assertTrue(executed[0]);
    }

    @Test
    void testWhenGroup() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.whenGroup(Object.class);
        assertSame(chain, result);
    }

    @Test
    void testInGroup() {
        TestChainCore chain = new TestChainCore(true, null);
        boolean[] executed = {false};
        TestChainCore result = chain.inGroup(Object.class, c -> { executed[0] = true; });
        assertSame(chain, result);
        assertTrue(executed[0]);
    }

    @Test
    void testRecursive() {
        TestChainCore chain = new TestChainCore(true, null);
        TypedValidator validator = Mockito.mock(TypedValidator.class);
        RecursiveOptions options = RecursiveOptions.builder().build();
        TestChainCore result = chain.recursive(new Object(), validator, options);
        assertSame(chain, result);
    }

    @Test
    void testConsole() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.console("Test");
        assertSame(chain, result);
    }

    @Test
    void testPrint() {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.print(System.out::println);
        assertSame(chain, result);
    }
}
