package com.chao.failure.internal.chain.pipeline;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.chao.failure.validator.FastValidator.ValidationContext;
import com.chao.failure.annotation.Scene;
import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.constant.Scenario;
import com.chao.failure.exception.Business;
import com.chao.failure.internal.core.Ex;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.internal.validation.ObjectGraphWalker;
import com.chao.failure.internal.validation.RecursiveOption;
import com.chao.failure.spi.filter.SkipTypeRegistry;
import com.chao.failure.util.ReflectionCache;
import com.chao.failure.validator.TypedValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ChainCoreEdgeCoverageTest {

    static class TestChainCore extends ChainCore<TestChainCore> {
        TestChainCore(boolean failFast, ValidationContext context) {
            super(failFast, context);
        }

        @Override
        public TestChainCore core() {
            return self();
        }

        void callAddError(ResponseCode code, String detail, Object value, String path, String constraint) {
            addError(code, detail, value, path, constraint);
        }
    }

    @AfterEach
    void tearDown() {
        Ex.setSkipTypeRegistry(null);
    }

    @Test
    void checkAsyncReturnsSelfWhenShouldSkip() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.when(false);
        TestChainCore out = chain.checkAsync(CompletableFuture.completedFuture(true), ResponseCode.VALIDATION_ERROR_400);
        assertSame(chain, out);
    }

    @Test
    void applyAsyncChecksSkipsWhenConditionBecomesFalse() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.checkAsync(CompletableFuture.completedFuture(false), ResponseCode.VALIDATION_ERROR_400, "x");
        chain.when(false);
        chain.applyAsyncChecks().join();
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void checkWithPathAndConstraintCoversOrModeFailAndPassBranches() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.check(false, ResponseCode.VALIDATION_ERROR_400, "l");
        chain.or();
        chain.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "r", "v", "p", "c", "s");
        assertThat(chain.isValid()).isFalse();

        chain = new TestChainCore(true, null);
        chain.check(false, ResponseCode.VALIDATION_ERROR_400, "l");
        chain.or();
        chain.checkWithPathAndConstraint(true, ResponseCode.VALIDATION_ERROR_400, "r", "v", "p", "c", "s");
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void orReturnsEarlyWhenContextStopped() {
        ValidationContext ctx = new ValidationContext(true);
        ctx.stop();
        TestChainCore chain = new TestChainCore(true, ctx);
        assertSame(chain, chain.or());
    }

    @Test
    void orContinuesWhenContextNotStopped() {
        ValidationContext ctx = new ValidationContext(true);
        TestChainCore chain = new TestChainCore(true, ctx);
        assertSame(chain, chain.or());
    }

    @Test
    void supplierCheckSkipsWhenConditionFalse() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.when(false);
        TestChainCore out = chain.check(() -> false, CheckSpec.of(ResponseCode.VALIDATION_ERROR_400, "x"));
        assertSame(chain, out);
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void checkSupplierExecutesWhenNotSkipped() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.check(() -> false, CheckSpec.of(ResponseCode.VALIDATION_ERROR_400, "x"));
        assertThat(chain.isValid()).isFalse();
    }

    @Test
    void checkRefSkipsWhenConditionFalse() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.when(false);
        assertSame(chain, chain.checkRef(false, ResponseCode.VALIDATION_ERROR_400, null));
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void checkRefWithNullValueRefCoversNullBranches() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.checkRef(false, ResponseCode.VALIDATION_ERROR_400, null);
        assertThat(chain.isValid()).isFalse();
    }

    @Test
    void checkWithPathAndConstraintSkipsWhenConditionFalse() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.when(false);
        assertSame(chain, chain.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "x", "v", "p", "c", "s"));
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void failFastFlagControlsAliveOnFailure() throws Exception {
        TestChainCore fast = new TestChainCore(true, null);
        fast.check(false, CheckSpec.of(ResponseCode.VALIDATION_ERROR_400, "x"));
        assertThat(readAlive(fast)).isFalse();

        TestChainCore slow = new TestChainCore(false, null);
        slow.check(false, CheckSpec.of(ResponseCode.VALIDATION_ERROR_400, "x"));
        assertThat(readAlive(slow)).isTrue();
    }

    @Test
    void failFastFlagControlsAliveOnFailureWithPathAndConstraint() throws Exception {
        TestChainCore fast = new TestChainCore(true, null);
        fast.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "x", "v", "p", "c", "s");
        assertThat(readAlive(fast)).isFalse();

        TestChainCore slow = new TestChainCore(false, null);
        slow.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "x", "v", "p", "c", "s");
        assertThat(readAlive(slow)).isTrue();
    }

    @Test
    void orModeFailureControlsAliveInCheckBooleanOverload() throws Exception {
        TestChainCore fast = new TestChainCore(true, null);
        fast.check(false, ResponseCode.VALIDATION_ERROR_400, "l", "v");
        fast.or();
        fast.check(false, ResponseCode.VALIDATION_ERROR_400, "r", "v");
        assertThat(readAlive(fast)).isFalse();

        TestChainCore slow = new TestChainCore(false, null);
        slow.check(false, ResponseCode.VALIDATION_ERROR_400, "l", "v");
        slow.or();
        slow.check(false, ResponseCode.VALIDATION_ERROR_400, "r", "v");
        assertThat(readAlive(slow)).isTrue();
    }

    @Test
    void orModeFailureControlsAliveInCheckWithPathAndConstraint() throws Exception {
        TestChainCore fast = new TestChainCore(true, null);
        fast.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "l", "v", "p", "c", "s");
        fast.or();
        fast.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "r", "v", "p", "c", "s");
        assertThat(readAlive(fast)).isFalse();

        TestChainCore slow = new TestChainCore(false, null);
        slow.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "l", "v", "p", "c", "s");
        slow.or();
        slow.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "r", "v", "p", "c", "s");
        assertThat(readAlive(slow)).isTrue();
    }

    @Test
    void orModeFinalSuccessTrueWhenLeftSideAlreadySucceeded() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.check(true, ResponseCode.VALIDATION_ERROR_400, "l");
        chain.or();
        chain.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "r", "v", "p", "c", "s");
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void getSceneNamePrivateMethodCoveredByReflection() throws Exception {
        TestChainCore chainDefault = new TestChainCore(true, null);
        assertThat(invokeGetSceneName(chainDefault)).isEqualTo(Scenario.DEFAULT.name());

        ValidationContext one = new ValidationContext(true, new Scenario[]{Scenario.CREATE}, new Class<?>[0]);
        TestChainCore chainOne = new TestChainCore(true, one);
        assertThat(invokeGetSceneName(chainOne)).isEqualTo(Scenario.CREATE.name());

        ValidationContext multi = new ValidationContext(true, new Scenario[]{Scenario.CREATE, Scenario.UPDATE}, new Class<?>[0]);
        TestChainCore chainMulti = new TestChainCore(true, multi);
        assertThat(invokeGetSceneName(chainMulti)).isEqualTo("MULTI");
    }

    @Test
    void buildBusinessOverloadWithoutConstraintCoveredByReflection() throws Exception {
        TestChainCore chain = new TestChainCore(true, null);
        Method m = ChainCore.class.getDeclaredMethod("buildBusiness", ResponseCode.class, String.class, Object.class, String.class);
        m.setAccessible(true);
        Object out = m.invoke(chain, ResponseCode.VALIDATION_ERROR_400, "d", "v", "p");
        assertThat(out).isInstanceOf(Business.class);
    }

    @Test
    void addErrorOverloadWithConstraintCovered() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.callAddError(ResponseCode.VALIDATION_ERROR_400, "d", "v", "p", "c");
        assertThat(chain.isValid()).isFalse();
    }
    @Test
    void addErrorOverloadWithConstraintCovered2() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.callAddError(ResponseCode.VALIDATION_ERROR_400, "d", "v", null, "c");
        assertThat(chain.isValid()).isFalse();
    }

    @Test
    void addErrorShouldReturnEarlyWhenStrictErrorLimitAlreadyReached() {
        com.chao.failure.internal.core.FailureContext original = Ex.getContext();
        Ex.setContext(null);
        TestChainCore chain = new TestChainCore(false, null);

        try {
            for (int i = 0; i < 50; i++) {
                chain.callAddError(ResponseCode.VALIDATION_ERROR_400, "d", "v", "p", "c");
            }
            int before = chain.errorSize();

            chain.callAddError(ResponseCode.VALIDATION_ERROR_400, "d2", "v2", "p2", "c2");

            assertThat(chain.errorSize()).isEqualTo(before);
            assertThat(chain.isErrorsTruncated()).isTrue();
        } finally {
            Ex.setContext(original);
        }
    }

    @Test
    void hasReachedErrorLimitShouldReturnFalseWhenConfiguredLimitIsNonPositive() throws Exception {
        TestChainCore chain = new TestChainCore(false, null);
        chain.callAddError(ResponseCode.VALIDATION_ERROR_400, "d", "v", "p", "c");

        Method method = ChainCore.class.getDeclaredMethod("hasReachedErrorLimit");
        method.setAccessible(true);

        com.chao.failure.internal.core.FailureContext ctx = mock(com.chao.failure.internal.core.FailureContext.class);
        when(ctx.getStrictMaxErrors()).thenReturn(0);
        try (MockedStatic<Ex> ex = mockStatic(Ex.class)) {
            ex.when(Ex::getContext).thenReturn(ctx);
            assertThat(method.invoke(chain)).isEqualTo(false);
        }
    }

    @Test
    void whenSceneAndGroupBranchesCoveredWithContext() {
        ValidationContext ctx = new ValidationContext(true, new Scenario[]{Scenario.CREATE}, new Class<?>[]{String.class});
        TestChainCore chain = new TestChainCore(true, ctx);

        chain.whenScene(Scenario.CREATE);
        assertThat(chain.isConditionState()).isTrue();
        chain.whenScene(Scenario.UPDATE);
        assertThat(chain.isConditionState()).isFalse();

        chain.resume();
        chain.whenScene(Scenario.UPDATE, Scenario.DELETE);
        assertThat(chain.isConditionState()).isFalse();
        chain.resume();
        chain.whenScene(Scenario.UPDATE, Scenario.CREATE);
        assertThat(chain.isConditionState()).isTrue();

        chain.whenGroup(String.class);
        assertThat(chain.isConditionState()).isTrue();
        chain.whenGroup(Integer.class);
        assertThat(chain.isConditionState()).isFalse();

        chain.resume();
        chain.whenGroup(Integer.class, Long.class);
        assertThat(chain.isConditionState()).isFalse();
        chain.resume();
        chain.whenGroup(Integer.class, String.class);
        assertThat(chain.isConditionState()).isTrue();
    }

    @Test
    void recursiveAddsErrorsToChainWhenContextNull() {
        TestChainCore chain = new TestChainCore(true, null);
        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenAnswer(inv -> {
            ValidationContext vc = inv.getArgument(1, ValidationContext.class);
            vc.reportError(ResponseCode.VALIDATION_ERROR_400, "x");
            return true;
        });
        chain.recursive(new Object(), typed, RecursiveOption.builder().build());
        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses()).isNotEmpty();
    }

    @Test
    void recursiveFailureWhenContextProvidedDoesNotCopyErrorsToLocalList() {
        ValidationContext ctx = new ValidationContext(true);
        TestChainCore chain = new TestChainCore(true, ctx);
        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenAnswer(inv -> {
            ValidationContext vc = inv.getArgument(1, ValidationContext.class);
            vc.reportError(ResponseCode.VALIDATION_ERROR_400, "x");
            return true;
        });
        chain.recursive(new Object(), typed, RecursiveOption.builder().build());
        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses()).isEmpty();
    }

    @Test
    void recursiveCoversEarlyReturnsAndCollectionMapArrayBranches() {
        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenReturn(false);

        TestChainCore chain = new TestChainCore(true, null);
        chain.recursive(null, typed, RecursiveOption.builder().build());

        chain = new TestChainCore(true, null);
        chain.recursive(new Object(), typed, RecursiveOption.builder().maxErrors(0).build());

        chain = new TestChainCore(true, null);
        chain.recursive(List.of(1, 2), typed, RecursiveOption.builder().maxItems(1).build());

        chain = new TestChainCore(true, null);
        chain.recursive(Map.of("k", 1), typed, RecursiveOption.builder().build());

        chain = new TestChainCore(true, null);
        chain.recursive(new Object[]{"a", "b"}, typed, RecursiveOption.builder().maxItems(1).build());

        chain = new TestChainCore(true, null);
        chain.recursive(new int[]{1, 2}, typed, RecursiveOption.builder().build());
    }

    @Test
    void recursiveSkipsWhenNotAlive() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.check(false, CheckSpec.of(ResponseCode.VALIDATION_ERROR_400, "x"));
        assertThat(chain.isValid()).isFalse();

        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenReturn(false);
        chain.recursive(new Object(), typed, RecursiveOption.builder().build());
    }

    @Test
    void recursiveFailureWithFailFastFalseDoesNotFlipAlive() throws Exception {
        TestChainCore chain = new TestChainCore(false, null);
        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenAnswer(inv -> {
            ValidationContext vc = inv.getArgument(1, ValidationContext.class);
            vc.reportError(ResponseCode.VALIDATION_ERROR_400, "x");
            return true;
        });
        chain.recursive(new Object(), typed, RecursiveOption.builder().build());
        assertThat(readAlive(chain)).isTrue();
    }

    @Test
    void recursiveFailureWithFailFastTrueFlipsAlive() throws Exception {
        TestChainCore chain = new TestChainCore(true, null);
        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenAnswer(inv -> {
            ValidationContext vc = inv.getArgument(1, ValidationContext.class);
            vc.reportError(ResponseCode.VALIDATION_ERROR_400, "x");
            return true;
        });
        chain.recursive(new Object(), typed, RecursiveOption.builder().build());
        assertThat(readAlive(chain)).isFalse();
    }

    @Test
    void recursiveFailFastTrueButNoErrorsDoesNotFlipAlive() throws Exception {
        TestChainCore chain = new TestChainCore(true, null);
        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenAnswer(inv -> {
            ValidationContext vc = inv.getArgument(1, ValidationContext.class);
            vc.stop();
            return true;
        });
        chain.recursive(new Object(), typed, RecursiveOption.builder().build());
        assertThat(readAlive(chain)).isTrue();
    }

    @Test
    void recursiveBuildsNestedItemPathsForCollectionMapArray() {
        TestChainCore chain = new TestChainCore(true, null);
        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenReturn(false);

        chain.recursive(new NestedHolder(), typed, RecursiveOption.builder().maxItems(2).build());
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void recursiveSkipsWhenSkipTypeRegistryMatches() {
        TestChainCore chain = new TestChainCore(true, null);
        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenReturn(false);

        SkipTypeRegistry skip = mock(SkipTypeRegistry.class);
        when(skip.shouldSkip(eq(SkippedPojo.class))).thenReturn(true);
        Ex.setSkipTypeRegistry(skip);

        chain.recursive(new SkippedPojo(), typed, RecursiveOption.builder().build());
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void recursiveCoversScenePruningAndFieldGetException() throws Exception {
        ValidationContext ctx = new ValidationContext(true, new Scenario[]{Scenario.CREATE}, new Class<?>[0]);
        TestChainCore chain = new TestChainCore(true, ctx);

        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenReturn(false);

        try (MockedStatic<ReflectionCache> cache = mockStatic(ReflectionCache.class)) {
            var badField = OtherPojo.class.getDeclaredField("x");
            cache.when(() -> ReflectionCache.getFields(eq(ScenePojo.class))).thenReturn(List.of(
                    ScenePojo.class.getDeclaredField("createOnly"),
                    ScenePojo.class.getDeclaredField("updateOnly"),
                    badField
            ));
            cache.when(() -> ReflectionCache.getSceneValues(org.mockito.ArgumentMatchers.any(java.lang.reflect.Field.class))).thenCallRealMethod();

            chain.recursive(new ScenePojo(), typed, RecursiveOption.builder().dedupeGlobal(false).include(List.of("createOnly", "other")).build());
        }
    }

    @Test
    void recursiveCoversIncludeExcludeAndCircularReferenceReturns() {
        TestChainCore chain = new TestChainCore(true, null);
        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenReturn(false);

        chain.recursive(new SimplePojo(), typed, RecursiveOption.builder().exclude(List.of("")).build());

        chain.recursive(new SimplePojo(), typed, RecursiveOption.builder().include(List.of("other")).build());

        CircularPojo c = new CircularPojo();
        c.self = c;
        chain.recursive(c, typed, RecursiveOption.builder().build());
    }

    @Test
    void recursiveNullObjectWithDedupeGlobalFalseCoversFinallyBranch() {
        TestChainCore chain = new TestChainCore(true, null);
        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenReturn(false);
        chain.recursive(null, typed, RecursiveOption.builder().dedupeGlobal(false).build());
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void whenSceneAndGroupCoverNullContextArrays() {
        ValidationContext ctx = new ValidationContext(true, (Scenario[]) null, null);
        TestChainCore chain = new TestChainCore(true, ctx);

        chain.whenScene(Scenario.DEFAULT, Scenario.UPDATE);
        assertThat(chain.isConditionState()).isTrue();

        chain.resume();
        chain.whenScene(Scenario.UPDATE, Scenario.DELETE);
        assertThat(chain.isConditionState()).isFalse();

        chain.whenGroup(String.class);
        assertThat(chain.isConditionState()).isFalse();

        chain.resume();
        chain.whenGroup(String.class, Integer.class);
        assertThat(chain.isConditionState()).isFalse();
    }

    @Test
    void recursiveCoversExcludeLoopNoMatchAndIncludeMatch() {
        TestChainCore chain = new TestChainCore(true, null);
        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenReturn(false);

        chain.recursive(new IncludePojo(), typed, RecursiveOption.builder().exclude(List.of("x")).include(List.of("includeMe")).build());
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void consoleCoversDebugLoggingBranch() {
        Logger logger = (Logger) LoggerFactory.getLogger(ChainCore.class);
        Level old = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        try {
            TestChainCore chain = new TestChainCore(true, null);
            chain.check(false, ResponseCode.VALIDATION_ERROR_400, "x");
            chain.console("m");
        } finally {
            logger.setLevel(old);
        }
    }

    @Test
    void printCoversDetailFallbackToMessageBranch() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.check(false, new CheckSpec(ResponseCode.VALIDATION_ERROR_400, null, null));
        AtomicReference<String> out = new AtomicReference<>();
        chain.print(out::set);
        assertThat(out.get()).contains("Error count: 1");
    }

    @Test
    void printCoversNullDetailBranch() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.errors.add(new Business(ResponseCode.VALIDATION_ERROR_400, null, null, null, HttpStatus.BAD_REQUEST, null));
        AtomicReference<String> out = new AtomicReference<>();
        chain.print(out::set);
        assertThat(out.get()).contains("Chain status:");
    }

    @Test
    void printCoversDetailPresentBranch() {
        TestChainCore chain = new TestChainCore(true, null);
        chain.check(false, new CheckSpec(ResponseCode.VALIDATION_ERROR_400, "d", null));
        AtomicReference<String> out = new AtomicReference<>();
        chain.print(out::set);
        assertThat(out.get()).contains("d");
    }

    @Test
    void sceneAndGroupPrivateHelpersCoveredByReflection() throws Exception {
        ValidationContext ctx = new ValidationContext(true);
        setFinalField(ctx, "scenes", new Scenario[0]);
        setFinalField(ctx, "groups", new Class<?>[0]);
        TestChainCore chain = new TestChainCore(true, ctx);

        Method hasScene = ChainCore.class.getDeclaredMethod("hasScene", Scenario.class);
        hasScene.setAccessible(true);
        assertThat((boolean) hasScene.invoke(chain, Scenario.DEFAULT)).isTrue();
        assertThat((boolean) hasScene.invoke(chain, Scenario.UPDATE)).isFalse();

        setFinalField(ctx, "scenes", null);
        assertThat((boolean) hasScene.invoke(chain, Scenario.DEFAULT)).isTrue();
        assertThat((boolean) hasScene.invoke(chain, Scenario.UPDATE)).isFalse();

        Method hasAnyScene = ChainCore.class.getDeclaredMethod("hasAnyScene", Scenario[].class);
        hasAnyScene.setAccessible(true);
        assertThat((boolean) hasAnyScene.invoke(chain, new Object[]{null})).isTrue();
        assertThat((boolean) hasAnyScene.invoke(chain, (Object) new Scenario[0])).isTrue();

        setFinalField(ctx, "scenes", new Scenario[0]);
        assertThat((boolean) hasAnyScene.invoke(chain, (Object) new Scenario[]{Scenario.UPDATE})).isFalse();
        assertThat((boolean) hasAnyScene.invoke(chain, (Object) new Scenario[]{Scenario.DEFAULT})).isTrue();

        setFinalField(ctx, "scenes", null);
        assertThat((boolean) hasAnyScene.invoke(chain, (Object) new Scenario[]{Scenario.UPDATE})).isFalse();
        assertThat((boolean) hasAnyScene.invoke(chain, (Object) new Scenario[]{Scenario.DEFAULT})).isTrue();

        Method hasGroup = ChainCore.class.getDeclaredMethod("hasGroup", Class.class);
        hasGroup.setAccessible(true);
        assertThat((boolean) hasGroup.invoke(chain, String.class)).isFalse();

        Method hasAnyGroup = ChainCore.class.getDeclaredMethod("hasAnyGroup", Class[].class);
        hasAnyGroup.setAccessible(true);
        assertThat((boolean) hasAnyGroup.invoke(chain, new Object[]{null})).isFalse();
        assertThat((boolean) hasAnyGroup.invoke(chain, (Object) new Class<?>[0])).isFalse();
        assertThat((boolean) hasAnyGroup.invoke(chain, (Object) new Class<?>[]{String.class})).isFalse();

        TestChainCore nullCtxChain = new TestChainCore(true, null);
        assertThat((boolean) hasScene.invoke(nullCtxChain, Scenario.DEFAULT)).isTrue();
        assertThat((boolean) hasScene.invoke(nullCtxChain, Scenario.UPDATE)).isFalse();
        assertThat((boolean) hasAnyScene.invoke(nullCtxChain, (Object) new Scenario[]{Scenario.DEFAULT})).isTrue();
        assertThat((boolean) hasAnyGroup.invoke(nullCtxChain, (Object) new Class<?>[]{String.class})).isFalse();
    }

    @Test
    void pathHelperMethodsCoveredByReflection() throws Exception {
        Class<?> walkerClass = Class.forName("com.chao.failure.internal.validation.ObjectGraphWalker");

        Method isPrimitiveOrWrapper = walkerClass.getDeclaredMethod("isPrimitiveOrWrapper", Class.class);
        isPrimitiveOrWrapper.setAccessible(true);
        assertThat((boolean) isPrimitiveOrWrapper.invoke(null, int.class)).isTrue();
        assertThat((boolean) isPrimitiveOrWrapper.invoke(null, boolean.class)).isTrue();
        assertThat((boolean) isPrimitiveOrWrapper.invoke(null, Integer.class)).isTrue();
        assertThat((boolean) isPrimitiveOrWrapper.invoke(null, Boolean.class)).isTrue();
        assertThat((boolean) isPrimitiveOrWrapper.invoke(null, Byte.class)).isTrue();
        assertThat((boolean) isPrimitiveOrWrapper.invoke(null, Short.class)).isTrue();
        assertThat((boolean) isPrimitiveOrWrapper.invoke(null, Long.class)).isTrue();
        assertThat((boolean) isPrimitiveOrWrapper.invoke(null, Float.class)).isTrue();
        assertThat((boolean) isPrimitiveOrWrapper.invoke(null, Double.class)).isTrue();
        assertThat((boolean) isPrimitiveOrWrapper.invoke(null, Character.class)).isTrue();
        assertThat((boolean) isPrimitiveOrWrapper.invoke(null, Void.class)).isTrue();
        assertThat((boolean) isPrimitiveOrWrapper.invoke(null, String.class)).isFalse();

        Method isStringOrEnum = walkerClass.getDeclaredMethod("isStringOrEnum", Class.class);
        isStringOrEnum.setAccessible(true);
        assertThat((boolean) isStringOrEnum.invoke(null, String.class)).isTrue();
        assertThat((boolean) isStringOrEnum.invoke(null, Scenario.class)).isTrue();
        assertThat((boolean) isStringOrEnum.invoke(null, Integer.class)).isFalse();

        Method isExcluded = walkerClass.getDeclaredMethod("isExcluded", String.class, List.class);
        isExcluded.setAccessible(true);
        assertThat((boolean) isExcluded.invoke(null, "a.b", List.of("a"))).isTrue();
        assertThat((boolean) isExcluded.invoke(null, "a.b", List.of("x"))).isFalse();
        assertThat((boolean) isExcluded.invoke(null, "a.b", null)).isFalse();
        assertThat((boolean) isExcluded.invoke(null, "a.b", List.of())).isFalse();

        Method isIncluded = walkerClass.getDeclaredMethod("isIncluded", String.class, List.class);
        isIncluded.setAccessible(true);
        assertThat((boolean) isIncluded.invoke(null, null, List.of("x"))).isTrue();
        assertThat((boolean) isIncluded.invoke(null, "", List.of("x"))).isTrue();
        assertThat((boolean) isIncluded.invoke(null, "a.b", null)).isTrue();
        assertThat((boolean) isIncluded.invoke(null, "a.b", List.of())).isTrue();
        assertThat((boolean) isIncluded.invoke(null, "a.b", List.of("a"))).isTrue();
        assertThat((boolean) isIncluded.invoke(null, "a.b", List.of("x"))).isFalse();
    }

    @Test
    void getSceneNameCoversNullAndEmptyScenesBranches() throws Exception {
        ValidationContext ctx = new ValidationContext(true);
        TestChainCore chain = new TestChainCore(true, ctx);
        assertThat(invokeGetSceneName(chain)).isEqualTo(Scenario.DEFAULT.name());

        setFinalField(ctx, "scenes", new Scenario[0]);
        assertThat(invokeGetSceneName(chain)).isEqualTo(Scenario.DEFAULT.name());

        setFinalField(ctx, "scenes", null);
        assertThat(invokeGetSceneName(chain)).isEqualTo(Scenario.DEFAULT.name());
    }

    @Test
    void isValidCoversContextAndAliveCombinations() throws Exception {
        ValidationContext ctx = new ValidationContext(true);
        TestChainCore chain = new TestChainCore(true, ctx);
        assertThat(chain.isValid()).isTrue();

        ctx.reportError(ResponseCode.VALIDATION_ERROR_400, "x");
        assertThat(chain.isValid()).isFalse();

        setAlive(chain, false);
        ValidationContext ctx2 = new ValidationContext(true);
        TestChainCore chain2 = new TestChainCore(true, ctx2);
        setAlive(chain2, false);
        assertThat(chain2.isValid()).isFalse();
    }

    @Test
    void recursiveValidateEarlyReturnBranchesCoveredByReflection() throws Exception {
        Class<?> walkerClass = Class.forName("com.chao.failure.internal.validation.ObjectGraphWalker");
        TypedValidator typed = mock(TypedValidator.class);
        RecursiveOption options = RecursiveOption.builder().maxDepth(0).maxErrors(Integer.MAX_VALUE).build();

        Method m = walkerClass.getDeclaredMethod("walk", Object.class, String.class, TypedValidator.class, ValidationContext.class, RecursiveOption.class, int.class, IdentityHashMap.class);
        m.setAccessible(true);

        ValidationContext stopped = new ValidationContext(true);
        stopped.stop();
        m.invoke(null, new Object(), "", typed, stopped, options, 0, new IdentityHashMap<>());

        ValidationContext depthCtx = new ValidationContext(true);
        m.invoke(null, new Object(), "", typed, depthCtx, options, 1, new IdentityHashMap<>());
    }

    @Test
    void recursiveValidateCoversNonEmptyPathForFieldPath() throws Exception {
        Class<?> walkerClass = Class.forName("com.chao.failure.internal.validation.ObjectGraphWalker");
        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenReturn(false);
        RecursiveOption options = RecursiveOption.builder().maxDepth(2).build();

        Method m = walkerClass.getDeclaredMethod("walk", Object.class, String.class, TypedValidator.class, ValidationContext.class, RecursiveOption.class, int.class, IdentityHashMap.class);
        m.setAccessible(true);

        ValidationContext ctx = new ValidationContext(true);
        m.invoke(null, new FieldHolder(), "root", typed, ctx, options, 0, new IdentityHashMap<>());
    }

    @Test
    void recursiveValidateCoversDedupeGlobalTrueBranch() {
        TestChainCore chain = new TestChainCore(true, null);
        TypedValidator typed = mock(TypedValidator.class);
        when(typed.validateIfRegistered(any(), any())).thenReturn(false);
        chain.recursive(new Object(), typed, RecursiveOption.builder().dedupeGlobal(true).build());
    }

    @Test
    void should_treatNullPredicateAsTrue_when_whenPredicateReceivesNull() {
        TestChainCore chain = new TestChainCore(true, null);

        assertThat(chain.when((com.chao.failure.condition.Predicate) null)).isSameAs(chain);
        assertThat(chain.isConditionState()).isTrue();
    }

    @Test
    void should_returnNullLatestCause_when_contextExistsWithoutErrors() {
        ValidationContext ctx = new ValidationContext(false);
        TestChainCore chain = new TestChainCore(false, ctx);

        assertThat(chain.latestCause()).isNull();
    }

    @Test
    void should_returnLastContextCause_when_contextContainsErrors() {
        ValidationContext ctx = new ValidationContext(false);
        ctx.reportError(ResponseCode.VALIDATION_ERROR_400, "first");
        ctx.reportError(ResponseCode.VALIDATION_ERROR_400, "second");
        TestChainCore chain = new TestChainCore(false, ctx);

        assertThat(chain.latestCause()).isNotNull();
        assertThat(chain.latestCause().getDetail()).isEqualTo("second");
    }

    @Test
    void should_markChainAsTruncatedAndStopContext_when_errorLimitIsReachedBeforeAddingAnotherError() throws Exception {
        FailureProperties properties = new FailureProperties();
        properties.getChain().setMaxErrors(1);
        com.chao.failure.internal.core.FailureContext exContext =
                new com.chao.failure.internal.core.FailureContext(properties, new com.chao.failure.config.mapping.CodeMappingConfig(properties), null);
        Ex.setContext(exContext);
        try {
            ValidationContext validationContext = new ValidationContext(false);
            TestChainCore chain = new TestChainCore(false, validationContext);

            chain.check(false, ResponseCode.VALIDATION_ERROR_400, "first");
            chain.check(false, ResponseCode.VALIDATION_ERROR_400, "second");

            Field truncated = ChainCore.class.getDeclaredField("errorsTruncated");
            truncated.setAccessible(true);

            assertThat((boolean) truncated.get(chain)).isTrue();
            assertThat(validationContext.isStopped()).isTrue();
            assertThat(readAlive(chain)).isFalse();
            assertThat(chain.isConditionState()).isFalse();
        } finally {
            Ex.setContext(null);
        }
    }

    @Test
    void should_notReachErrorLimit_when_strictLimitIsNonPositive() throws Exception {
        FailureProperties properties = new FailureProperties();
        properties.getChain().setMaxErrors(0);
        com.chao.failure.internal.core.FailureContext exContext =
                new com.chao.failure.internal.core.FailureContext(properties, new com.chao.failure.config.mapping.CodeMappingConfig(properties), null);
        Ex.setContext(exContext);
        try {
            TestChainCore chain = new TestChainCore(false, null);
            Method method = ChainCore.class.getDeclaredMethod("hasReachedErrorLimit");
            method.setAccessible(true);

            boolean reached = (boolean) method.invoke(chain);

            assertThat(reached).isFalse();
        } finally {
            Ex.setContext(null);
        }
    }


    @Test
    void ObjectGraphWalker() {
        ObjectGraphWalker o = new ObjectGraphWalker();
    }

    private static String invokeGetSceneName(ChainCore<?> chain) throws Exception {
        Method m = ChainCore.class.getDeclaredMethod("getSceneName");
        m.setAccessible(true);
        return (String) m.invoke(chain);
    }

    private static void setFinalField(Object target, String name, Object value) throws Exception {
        Field f = ValidationContext.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static boolean readAlive(ChainCore<?> chain) throws Exception {
        Field f = ChainCore.class.getDeclaredField("alive");
        f.setAccessible(true);
        return (boolean) f.get(chain);
    }

    private static void setAlive(ChainCore<?> chain, boolean value) throws Exception {
        Field f = ChainCore.class.getDeclaredField("alive");
        f.setAccessible(true);
        f.set(chain, value);
    }

    static class SkippedPojo {
    }

    static class SimplePojo {
        @SuppressWarnings("unused")
        String name = "n";
    }

    static class CircularPojo {
        CircularPojo self;
    }

    static class OtherPojo {
        @SuppressWarnings("unused")
        private String x = "x";
    }

    static class ScenePojo {
        @Scene({Scenario.CREATE})
        @SuppressWarnings("unused")
        private String createOnly = "a";

        @Scene({Scenario.UPDATE})
        @SuppressWarnings("unused")
        private String updateOnly = "b";
    }

    static class IncludePojo {
        @SuppressWarnings("unused")
        private String includeMe = "x";
    }

    static class NestedHolder {
        @SuppressWarnings("unused")
        private List<String> list = List.of("a");
        @SuppressWarnings("unused")
        private Map<String, String> map = Map.of("k", "v");
        @SuppressWarnings("unused")
        private String[] arr = new String[]{"a"};
    }

    static class FieldHolder {
        @SuppressWarnings("unused")
        private Object child = new Object();
    }
}

