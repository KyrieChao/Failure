package com.chao.failure.internal.chain.pipeline;

import com.chao.failure.validator.FastValidator.ValidationContext;
import com.chao.failure.constant.Scenario;
import com.chao.failure.exception.Business;
import com.chao.failure.internal.core.Ex;
import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.internal.validation.RecursiveOption;
import com.chao.failure.validator.TypedValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;

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
        RecursiveOption options = RecursiveOption.builder().build();
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

    @Test
    void testAtWithNonNullPath() throws Exception {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.at("user.name");

        assertSame(chain, result);
        
        java.lang.reflect.Field currentPathField = ChainCore.class.getDeclaredField("currentPath");
        currentPathField.setAccessible(true);
        assertEquals("user.name", currentPathField.get(chain));
    }

    @Test
    void testAtWithNullPath() throws Exception {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.at(null);

        assertSame(chain, result);
        
        java.lang.reflect.Field currentPathField = ChainCore.class.getDeclaredField("currentPath");
        currentPathField.setAccessible(true);
        assertNull(currentPathField.get(chain));
    }

    @Test
    void testAtWithEmptyStringPath() throws Exception {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.at("");

        assertSame(chain, result);
        
        java.lang.reflect.Field currentPathField = ChainCore.class.getDeclaredField("currentPath");
        currentPathField.setAccessible(true);
        assertEquals("", currentPathField.get(chain));
    }

    @Test
    void testAtWithBlankStringPath() throws Exception {
        TestChainCore chain = new TestChainCore(true, null);
        TestChainCore result = chain.at("  ");

        assertSame(chain, result);
        
        java.lang.reflect.Field currentPathField = ChainCore.class.getDeclaredField("currentPath");
        currentPathField.setAccessible(true);
        assertEquals("  ", currentPathField.get(chain));
    }

    @Test
    void testAtPathUsedInErrorReporting() {
        TestChainCore chain = new TestChainCore(false, null);
        
        chain.at("user.email").check(false, ResponseCode.VALIDATION_ERROR_400, "Invalid email");
        
        List<Business> causes = chain.getCauses();
        assertEquals(1, causes.size());
        assertEquals("user.email", causes.get(0).getPath());
    }

    @Test
    void testAtWithPathClearsInvalidValue() throws Exception {
        TestChainCore chain = new TestChainCore(false, null);
        
        java.lang.reflect.Field invalidValueField = ChainCore.class.getDeclaredField("currentInvalidValueSupplier");
        invalidValueField.setAccessible(true);
        
        chain.at("user.name", "invalid-value");
        assertNotNull(invalidValueField.get(chain));
        
        chain.at("user.email");
        assertNull(invalidValueField.get(chain));
    }

    @Test
    void testAtWithPathAndNonNullInvalidValue() throws Exception {
        TestChainCore chain = new TestChainCore(false, null);
        
        java.lang.reflect.Field currentPathField = ChainCore.class.getDeclaredField("currentPath");
        currentPathField.setAccessible(true);
        java.lang.reflect.Field invalidValueField = ChainCore.class.getDeclaredField("currentInvalidValueSupplier");
        invalidValueField.setAccessible(true);
        
        TestChainCore result = chain.at("user.name", "test-value");
        
        assertSame(chain, result);
        assertEquals("user.name", currentPathField.get(chain));
        assertNotNull(invalidValueField.get(chain));
    }

    @Test
    void testAtWithPathAndNullInvalidValue() throws Exception {
        TestChainCore chain = new TestChainCore(false, null);
        
        java.lang.reflect.Field currentPathField = ChainCore.class.getDeclaredField("currentPath");
        currentPathField.setAccessible(true);
        java.lang.reflect.Field invalidValueField = ChainCore.class.getDeclaredField("currentInvalidValueSupplier");
        invalidValueField.setAccessible(true);
        
        chain.at("user.name", (Object) null);
        
        assertEquals("user.name", currentPathField.get(chain));
        assertNull(invalidValueField.get(chain));
    }

    @Test
    void testAtWithPathAndNonNullSupplier() throws Exception {
        TestChainCore chain = new TestChainCore(false, null);
        
        java.lang.reflect.Field currentPathField = ChainCore.class.getDeclaredField("currentPath");
        currentPathField.setAccessible(true);
        java.lang.reflect.Field invalidValueField = ChainCore.class.getDeclaredField("currentInvalidValueSupplier");
        invalidValueField.setAccessible(true);
        
        TestChainCore result = chain.at("user.name", () -> "supplied-value");
        
        assertSame(chain, result);
        assertEquals("user.name", currentPathField.get(chain));
        assertNotNull(invalidValueField.get(chain));
    }

    @Test
    void testAtWithPathAndNullSupplier() throws Exception {
        TestChainCore chain = new TestChainCore(false, null);
        
        java.lang.reflect.Field currentPathField = ChainCore.class.getDeclaredField("currentPath");
        currentPathField.setAccessible(true);
        java.lang.reflect.Field invalidValueField = ChainCore.class.getDeclaredField("currentInvalidValueSupplier");
        invalidValueField.setAccessible(true);
        
        chain.at("user.name", (java.util.function.Supplier<Object>) null);
        
        assertEquals("user.name", currentPathField.get(chain));
        assertNull(invalidValueField.get(chain));
    }

    @Test
    void testAtWithInvalidValueUsedInErrorReporting() {
        TestChainCore chain = new TestChainCore(false, null);
        
        chain.at("user.age", "18").check(false, ResponseCode.VALIDATION_ERROR_400, "Age must be greater than 18");
        
        List<Business> causes = chain.getCauses();
        assertEquals(1, causes.size());
        assertEquals("user.age", causes.get(0).getPath());
    }

    @Test
    void testAtWithSupplierInvalidValueUsedInErrorReporting() {
        TestChainCore chain = new TestChainCore(false, null);
        
        chain.at("user.age", () -> "18").check(false, ResponseCode.VALIDATION_ERROR_400, "Age must be greater than 18");
        
        List<Business> causes = chain.getCauses();
        assertEquals(1, causes.size());
        assertEquals("user.age", causes.get(0).getPath());
    }

    @Test
    void should_returnTrue_when_hasReachedErrorLimitHitsConfiguredCap() throws Exception {
        TestChainCore chain = new TestChainCore(false, null);
        FailureContext failureContext = Mockito.mock(FailureContext.class);
        try {
            Ex.setContext(failureContext);
            Mockito.when(failureContext.getStrictMaxErrors()).thenReturn(1);
            chain.check(false, ResponseCode.VALIDATION_ERROR_400, "boom");

            var method = ChainCore.class.getDeclaredMethod("hasReachedErrorLimit");
            method.setAccessible(true);

            assertTrue((boolean) method.invoke(chain));
        } finally {
            Ex.setContext(null);
        }
    }

    @Test
    void should_returnFalse_when_hasReachedErrorLimitSeesNonPositiveLimit() throws Exception {
        TestChainCore chain = new TestChainCore(false, null);
        FailureContext failureContext = Mockito.mock(FailureContext.class);
        try {
            Ex.setContext(failureContext);
            Mockito.when(failureContext.getStrictMaxErrors()).thenReturn(0);
            chain.check(false, ResponseCode.VALIDATION_ERROR_400, "boom");

            var method = ChainCore.class.getDeclaredMethod("hasReachedErrorLimit");
            method.setAccessible(true);

            assertFalse((boolean) method.invoke(chain));
        } finally {
            Ex.setContext(null);
        }
    }
}
