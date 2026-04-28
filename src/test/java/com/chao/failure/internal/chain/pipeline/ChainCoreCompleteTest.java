package com.chao.failure.internal.chain.pipeline;

import com.chao.failure.validator.FastValidator.ValidationContext;
import com.chao.failure.constant.Scenario;
import com.chao.failure.exception.Business;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.internal.validation.RecursiveOption;
import com.chao.failure.validator.TypedValidator;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChainCoreCompleteTest {

    private TestChainCore chainCore;
    private ValidationContext context;

    @BeforeEach
    void setUp() {
        context = mock(ValidationContext.class);
        when(context.isValid()).thenReturn(true);
        when(context.isStopped()).thenReturn(false);
        when(context.errorSize()).thenReturn(0);
        // 不使用 context，直接测试核心逻辑
        chainCore = new TestChainCore(false, null);
    }

    @Test
    void testConstructor() {
        TestChainCore core = new TestChainCore(true, null);
        assertTrue(core.isFailFast());
        assertTrue(core.isAlive());
    }

    @Test
    void testErrorSize() {
        // Test without context
        TestChainCore coreWithoutContext = new TestChainCore(false, null);
        assertEquals(0, coreWithoutContext.errorSize());

        // Test with context
        TestChainCore coreWithContext = new TestChainCore(false, context);
        when(context.errorSize()).thenReturn(5);
        assertEquals(5, coreWithContext.errorSize());
    }

    @Test
    void testCheckAsync() {
        // Test with null stage
        chainCore.checkAsync(null, ResponseCode.VALIDATION_ERROR_400);
        assertEquals(1, chainCore.getErrors().size());

        // Test with valid stage
        CompletionStage<Boolean> stage = CompletableFuture.completedFuture(true);
        chainCore.checkAsync(stage, ResponseCode.VALIDATION_ERROR_400);
        assertEquals(1, chainCore.getAsyncChecks().size());

        // Test with invalid stage
        stage = CompletableFuture.completedFuture(false);
        chainCore.checkAsync(stage, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertEquals(2, chainCore.getAsyncChecks().size());
    }

    @Test
    void testApplyAsyncChecks() {
        // Test with empty async checks
        chainCore.applyAsyncChecks().join();
        assertTrue(chainCore.getAsyncChecks().isEmpty());

        // Test with valid async check
        CompletionStage<Boolean> stage = CompletableFuture.completedFuture(true);
        chainCore.checkAsync(stage, ResponseCode.VALIDATION_ERROR_400);
        chainCore.applyAsyncChecks().join();
        assertTrue(chainCore.getAsyncChecks().isEmpty());

        // Test with invalid async check
        stage = CompletableFuture.completedFuture(false);
        chainCore.checkAsync(stage, ResponseCode.VALIDATION_ERROR_400, "Test error");
        chainCore.applyAsyncChecks().join();
        assertTrue(chainCore.getAsyncChecks().isEmpty());
        assertEquals(1, chainCore.getErrors().size());

        // Test with exception in async check
        stage = CompletableFuture.failedFuture(new RuntimeException("Async error"));
        chainCore.checkAsync(stage, ResponseCode.VALIDATION_ERROR_400);
        chainCore.applyAsyncChecks().join();
        assertTrue(chainCore.getAsyncChecks().isEmpty());
        assertEquals(2, chainCore.getErrors().size());
    }

    @Test
    void testWhen() {
        assertTrue(chainCore.isConditionState());
        chainCore.when(false);
        assertFalse(chainCore.isConditionState());
        chainCore.when(true);
        assertTrue(chainCore.isConditionState());
    }

    @Test
    void testIfTrue() {
        // Test with condition true
        final boolean[] executed = {false};
        chainCore.ifTrue(true, core -> executed[0] = true);
        assertTrue(executed[0]);

        // Test with condition false
        executed[0] = false;
        chainCore.ifTrue(false, core -> executed[0] = true);
        assertFalse(executed[0]);

        // Test with not alive
        chainCore.setAlive(false);
        executed[0] = false;
        chainCore.ifTrue(true, core -> executed[0] = true);
        assertFalse(executed[0]);
    }

    @Test
    void testOr() {
        // Test when condition state is false
        chainCore.when(false);
        chainCore.or();
        // Just ensure it doesn't throw exceptions

        // Test failFast case
        TestChainCore failFastCore = new TestChainCore(true, null);
        failFastCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertFalse(failFastCore.isAlive());
        failFastCore.or();
        assertTrue(failFastCore.isAlive());

        // Test normal case with OR logic
        TestChainCore core = new TestChainCore(false, null);
        core.check(false, ResponseCode.VALIDATION_ERROR_400, "First error");
        assertFalse(core.isValid());
        core.or();
        core.check(true, ResponseCode.VALIDATION_ERROR_400, "Second error");
        assertTrue(core.isValid());
    }

    @Test
    void testShouldSkip() {
        // Test when condition state is false
        chainCore.when(false);
        assertTrue(chainCore.shouldSkip());

        // Test when not alive and failFast
        TestChainCore failFastCore = new TestChainCore(true, null);
        failFastCore.setAlive(false);
        assertTrue(failFastCore.shouldSkip());

        // Test when should not skip
        chainCore.when(true);
        chainCore.setAlive(true);
        assertFalse(chainCore.shouldSkip());

        // Test when context is stopped
        TestChainCore coreWithContext = new TestChainCore(false, context);
        when(context.isStopped()).thenReturn(true);
        assertTrue(coreWithContext.shouldSkip());
    }

    @Test
    void testCheckWithSupplier() {
        // Test with valid condition
        chainCore.check(() -> true, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertTrue(chainCore.isValid());

        // Test with invalid condition
        chainCore.check(() -> false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertFalse(chainCore.isValid());
    }

    @Test
    void testCheckWithBoolean() {
        // Test with valid condition
        chainCore.check(true, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertTrue(chainCore.isValid());

        // Test with invalid condition
        chainCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertFalse(chainCore.isValid());
    }

    @Test
    void testCheckWithCheckSpec() {
        CheckSpec spec = new CheckSpec(ResponseCode.VALIDATION_ERROR_400, "Test error", null);
        chainCore.check(false, spec);
        assertFalse(chainCore.isValid());
    }

    @Test
    void testCheckWithInvalidValueSupplier() {
        chainCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error", () -> "invalid value");
        assertFalse(chainCore.isValid());
    }

    @Test
    void testCheckRef() {
        PathEntry<String> pathEntry = new PathEntry<>("testPath", "testValue");
        chainCore.checkRef(false, ResponseCode.VALIDATION_ERROR_400, pathEntry);
        assertFalse(chainCore.isValid());
    }

    @Test
    void testCheckWithPathAndConstraint() {
        chainCore.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint");
        assertFalse(chainCore.isValid());
    }

    @Test
    void testStopOnFail() {
        // Test when condition state is false
        chainCore.when(false);
        chainCore.stopOnFail();
        assertFalse(chainCore.isConditionState());

        // Test when valid
        chainCore.when(true);
        chainCore.stopOnFail();
        assertTrue(chainCore.isConditionState());

        // Test when invalid
        chainCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        chainCore.stopOnFail();
        assertFalse(chainCore.isConditionState());
    }

    @Test
    void testResume() {
        chainCore.when(false);
        assertFalse(chainCore.isConditionState());
        chainCore.resume();
        assertTrue(chainCore.isConditionState());
    }

    @Test
    void testWhenScene() {
        // Test with DEFAULT scene
        chainCore.whenScene(Scenario.DEFAULT);
        assertTrue(chainCore.isConditionState());

        // Test with non-DEFAULT scene
        chainCore.whenScene(Scenario.CREATE);
        assertFalse(chainCore.isConditionState()); // When context is null, only DEFAULT scene is considered active
    }

    @Test
    void testWhenSceneArray() {
        // Test with empty array
        chainCore.whenScene();
        assertTrue(chainCore.isConditionState());

        // Test with DEFAULT scene
        chainCore.whenScene(new Scenario[]{Scenario.DEFAULT});
        assertTrue(chainCore.isConditionState());

        // Test with non-DEFAULT scene
        chainCore.whenScene(new Scenario[]{Scenario.CREATE});
        assertTrue(chainCore.isConditionState()); // When context is null, all scenes are considered active
    }

    @Test
    void testInScene() {
        final boolean[] executed = {false};
        chainCore.inScene(Scenario.DEFAULT, core -> executed[0] = true);
        assertTrue(executed[0]);

        executed[0] = false;
        chainCore.inScene(Scenario.CREATE, core -> executed[0] = true);
        assertTrue(executed[0]); // When context is null, all scenes are considered active
    }

    @Test
    void testInSceneArray() {
        final boolean[] executed = {false};
        chainCore.inScene(new Scenario[]{Scenario.DEFAULT}, core -> executed[0] = true);
        assertTrue(executed[0]);

        executed[0] = false;
        chainCore.inScene(new Scenario[]{Scenario.CREATE}, core -> executed[0] = true);
        assertTrue(executed[0]); // When context is null, all scenes are considered active
    }

    @Test
    void testWhenGroup() {
        chainCore.whenGroup(Object.class);
        assertFalse(chainCore.isConditionState()); // When context is null, no groups are considered active
    }

    @Test
    void testWhenGroupArray() {
        chainCore.whenGroup(new Class[]{Object.class});
        assertFalse(chainCore.isConditionState()); // When context is null, no groups are considered active
    }

    @Test
    void testInGroup() {
        final boolean[] executed = {false};
        chainCore.inGroup(Object.class, core -> executed[0] = true);
        assertTrue(executed[0]); // inGroup always executes the block regardless of group status
    }

    @Test
    void testInGroupArray() {
        final boolean[] executed = {false};
        chainCore.inGroup(new Class[]{Object.class}, core -> executed[0] = true);
        assertTrue(executed[0]); // inGroup always executes the block regardless of group status
    }

    @Test
    void testRecursive() {
        TypedValidator typedValidator = mock(TypedValidator.class);
        when(typedValidator.validateIfRegistered(any(), any())).thenReturn(false);

        RecursiveOption options = RecursiveOption.builder()
                .maxDepth(5)
                .maxErrors(10)
                .maxItems(10)
                .build();

        // Test with null object
        chainCore.recursive(null, typedValidator, options);
        assertTrue(chainCore.isValid());

        // Test with simple object
        TestObject testObject = new TestObject();
        chainCore.recursive(testObject, typedValidator, options);
        assertTrue(chainCore.isValid());
    }

    @Test
    void testConsole() {
        chainCore.console("Test message");
        // Just ensure it doesn't throw exceptions
    }

    @Test
    void testPrint() {
        final StringBuilder output = new StringBuilder();
        chainCore.print(output::append);
        assertTrue(output.toString().contains("Chain status: VALID"));

        // Test with errors
        chainCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        output.setLength(0);
        chainCore.print(output::append);
        assertTrue(output.toString().contains("Chain status: INVALID"));
    }

    @Test
    void testGetCauses() {
        assertTrue(chainCore.getCauses().isEmpty());
        chainCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertEquals(1, chainCore.getCauses().size());
    }

    @Test
    void testIsValid() {
        assertTrue(chainCore.isValid());
        chainCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertFalse(chainCore.isValid());
    }

    // Test implementation of ChainCore
    @Getter
    private static class TestChainCore extends ChainCore<TestChainCore> {

        private final boolean orMode = false;

        public TestChainCore(boolean failFast, ValidationContext context) {
            super(failFast, context);
        }

        public List<Business> getErrors() {
            return errors;
        }

        public List<?> getAsyncChecks() {
            try {
                java.lang.reflect.Field field = ChainCore.class.getDeclaredField("asyncChecks");
                field.setAccessible(true);
                return (List<?>) field.get(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void setAlive(boolean alive) {
            this.alive = alive;
        }

        @Override
        protected void notifyValidationStart(String source, String scene) {
            // No-op for testing
        }

        @Override
        protected void notifyValidationEnd(String source, long durationNanos, boolean success) {
            // No-op for testing
        }

        @Override
        protected void notifyValidationFailure(String source, String errorCode) {
            // No-op for testing
        }

        @Override
        protected void notifyViolation(String source, String constraint) {
            // No-op for testing
        }
    }

    // Test object for recursive validation
    @Setter
    @Getter
    private static class TestObject {
        private String name;
        private int age;
        private List<String> hobbies;
    }
}
