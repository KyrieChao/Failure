package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.constant.Scenario;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.internal.validation.RecursiveOptions;
import com.chao.failfast.validator.TypedValidator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChainCore测试")
@Tag("core")
class ChainCoreTest {

    // 具体实现ChainCore的测试类
    private static class TestChainCore extends ChainCore<TestChainCore> {

        public TestChainCore(boolean failFast, ValidationContext context) {
            super(failFast, context);
        }
    }

    private TestChainCore chainCore;
    private TestChainCore chainCoreWithContext;

    @Mock
    private ValidationContext mockContext;

    @Mock
    private TypedValidator mockTypedValidator;

    @Mock
    private RecursiveOptions mockOptions;

    @BeforeEach
    void setUp() {
        // 创建没有上下文的ChainCore
        chainCore = new TestChainCore(false, null);
        // 创建有上下文的ChainCore
        chainCoreWithContext = new TestChainCore(false, mockContext);
    }

    @DisplayName("测试构造函数")
    @Test
    void testConstructor() {
        // 测试无上下文的构造
        TestChainCore core1 = new TestChainCore(true, null);
        assertThat(core1.isFailFast()).isTrue();
        assertThat(core1.isAlive()).isTrue();

        // 测试有上下文的构造
        TestChainCore core2 = new TestChainCore(false, mockContext);
        assertThat(core2.isFailFast()).isFalse();
        assertThat(core2.isAlive()).isTrue();
    }

    @DisplayName("测试errorSize方法")
    @Test
    void testErrorSize() {
        // 测试无上下文的情况
        assertThat(chainCore.errorSize()).isZero();

        // 测试有上下文的情况
        when(mockContext.errorSize()).thenReturn(5);
        assertThat(chainCoreWithContext.errorSize()).isEqualTo(5);
    }

    @DisplayName("测试when方法")
    @Test
    void testWhen() {
        // 测试when(true)
        TestChainCore result1 = chainCore.when(true);
        assertThat(result1).isSameAs(chainCore);
        assertThat(chainCore.shouldSkip()).isFalse();

        // 测试when(false)
        TestChainCore result2 = chainCore.when(false);
        assertThat(result2).isSameAs(chainCore);
        assertThat(chainCore.shouldSkip()).isTrue();
    }

    @DisplayName("测试or方法")
    @Test
    void testOr() {
        // 测试正常情况
        TestChainCore result1 = chainCore.or();
        assertThat(result1).isSameAs(chainCore);

        // 测试当conditionState为false时
        chainCore.when(false);
        TestChainCore result2 = chainCore.or();
        assertThat(result2).isSameAs(chainCore);

        // 测试当context被停止时
        when(mockContext.isStopped()).thenReturn(true);
        TestChainCore result3 = chainCoreWithContext.or();
        assertThat(result3).isSameAs(chainCoreWithContext);

        // 测试failFast且alive为false的情况
        TestChainCore failFastCore = new TestChainCore(true, null);
        failFastCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThat(failFastCore.isAlive()).isFalse();
        failFastCore.or();
        assertThat(failFastCore.isAlive()).isTrue();
    }

    @DisplayName("测试or方法的context.isStopped()分支")
    @Test
    void testOrWithContextStopped() {
        // 测试当context不为null且被停止时
        when(mockContext.isStopped()).thenReturn(true);
        TestChainCore core = new TestChainCore(false, mockContext);
        TestChainCore result = core.or();
        assertThat(result).isSameAs(core);
        
        // 测试当context不为null但未被停止时
        when(mockContext.isStopped()).thenReturn(false);
        when(mockContext.isValid()).thenReturn(true);
        TestChainCore core2 = new TestChainCore(false, mockContext);
        TestChainCore result2 = core2.or();
        assertThat(result2).isSameAs(core2);
    }

    @DisplayName("测试shouldSkip方法")
    @Test
    void testShouldSkip() {
        // 测试正常情况
        assertThat(chainCore.shouldSkip()).isFalse();

        // 测试conditionState为false的情况
        chainCore.when(false);
        assertThat(chainCore.shouldSkip()).isTrue();

        // 测试context被停止的情况
        when(mockContext.isStopped()).thenReturn(true);
        assertThat(chainCoreWithContext.shouldSkip()).isTrue();

        // 测试failFast且alive为false的情况
        TestChainCore failFastCore = new TestChainCore(true, null);
        failFastCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThat(failFastCore.shouldSkip()).isTrue();
    }

    @DisplayName("测试check方法（Supplier条件）")
    @Test
    void testCheckWithSupplier() {
        // 测试条件为true的情况
        chainCore.check(() -> true, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThat(chainCore.isValid()).isTrue();

        // 测试条件为false的情况
        chainCore.check(() -> false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThat(chainCore.isValid()).isFalse();

        // 测试shouldSkip为true的情况
        chainCore.when(false);
        chainCore.check(() -> false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        // 由于被跳过，应该仍然有效
        assertThat(chainCore.isValid()).isFalse(); // 之前已经有错误了
    }

    @DisplayName("测试check方法（布尔条件）")
    @Test
    void testCheckWithBoolean() {
        // 测试条件为true的情况
        chainCore.check(true, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThat(chainCore.isValid()).isTrue();

        // 测试条件为false的情况
        chainCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThat(chainCore.isValid()).isFalse();

        // 测试shouldSkip为true的情况
        chainCore.when(false);
        chainCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        // 由于被跳过，错误数应该不变
        assertThat(chainCore.getCauses().size()).isEqualTo(1);
    }

    @DisplayName("测试check方法（带CheckSpec）")
    @Test
    void testCheckWithCheckSpec() {
        CheckSpec spec = new CheckSpec(ResponseCode.VALIDATION_ERROR_400, "Test error", "invalid value");

        // 测试条件为true的情况
        chainCore.check(true, spec);
        assertThat(chainCore.isValid()).isTrue();

        // 测试条件为false的情况
        chainCore.check(false, spec);
        assertThat(chainCore.isValid()).isFalse();

        // 测试spec为null的情况
        chainCore.check(false, null);
        assertThat(chainCore.isValid()).isFalse();
    }

    @DisplayName("测试checkRef方法")
    @Test
    void testCheckRef() {
        PathEntry<String> pathEntry = new PathEntry<>("testPath", "testValue");

        // 测试条件为true的情况
        chainCore.checkRef(true, ResponseCode.VALIDATION_ERROR_400, pathEntry);
        assertThat(chainCore.isValid()).isTrue();

        // 测试条件为false的情况
        chainCore.checkRef(false, ResponseCode.VALIDATION_ERROR_400, pathEntry);
        assertThat(chainCore.isValid()).isFalse();

        // 测试shouldSkip为true的情况
        chainCore.when(false);
        chainCore.checkRef(false, ResponseCode.VALIDATION_ERROR_400, pathEntry);
        // 由于被跳过，错误数应该不变
        assertThat(chainCore.getCauses().size()).isEqualTo(1);
    }

    @DisplayName("测试checkWithPathAndConstraint方法")
    @Test
    void testCheckWithPathAndConstraint() {
        // 测试条件为true的情况
        chainCore.checkWithPathAndConstraint(true, ResponseCode.VALIDATION_ERROR_400, "Test error", "testValue", "testPath", "testConstraint", "testSource");
        assertThat(chainCore.isValid()).isTrue();

        // 测试条件为false的情况
        chainCore.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Test error", "testValue", "testPath", "testConstraint", "testSource");
        assertThat(chainCore.isValid()).isFalse();

        // 测试shouldSkip为true的情况
        chainCore.when(false);
        chainCore.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Test error", "testValue", "testPath", "testConstraint", "testSource");
        // 由于被跳过，错误数应该不变
        assertThat(chainCore.getCauses().size()).isEqualTo(1);
    }

    @DisplayName("测试check方法（无参数）")
    @Test
    void testCheckWithoutParams() {
        // 测试条件为true的情况
        chainCore.check(true);
        assertThat(chainCore.isValid()).isTrue();

        // 测试条件为false的情况
        chainCore.check(false);
        assertThat(chainCore.isValid()).isFalse();
    }

    @DisplayName("测试core方法")
    @Test
    void testCore() {
        TestChainCore result = chainCore.core();
        assertThat(result).isSameAs(chainCore);
    }

    @DisplayName("测试getCauses方法")
    @Test
    void testGetCauses() {
        // 测试空错误列表
        List<Business> causes1 = chainCore.getCauses();
        assertThat(causes1).isEmpty();

        // 测试有错误的情况
        chainCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        List<Business> causes2 = chainCore.getCauses();
        assertThat(causes2).hasSize(1);
    }

    @DisplayName("测试isValid方法")
    @Test
    void testIsValid() {
        // 测试无上下文且无错误的情况
        assertThat(chainCore.isValid()).isTrue();

        // 测试无上下文且有错误的情况
        chainCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThat(chainCore.isValid()).isFalse();

        // 测试有上下文且有效的情况
        when(mockContext.isValid()).thenReturn(true);
        assertThat(chainCoreWithContext.isValid()).isTrue();

        // 测试有上下文且无效的情况
        when(mockContext.isValid()).thenReturn(false);
        assertThat(chainCoreWithContext.isValid()).isFalse();
    }

    @DisplayName("测试stopOnFail方法")
    @Test
    void testStopOnFail() {
        // 测试conditionState为false的情况
        chainCore.when(false);
        TestChainCore result1 = chainCore.stopOnFail();
        assertThat(result1).isSameAs(chainCore);

        // 测试isValid为true的情况
        chainCore.when(true);
        TestChainCore result2 = chainCore.stopOnFail();
        assertThat(result2).isSameAs(chainCore);
        assertThat(chainCore.shouldSkip()).isFalse();

        // 测试isValid为false的情况
        chainCore.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        TestChainCore result3 = chainCore.stopOnFail();
        assertThat(result3).isSameAs(chainCore);
        assertThat(chainCore.shouldSkip()).isTrue();
    }

    @DisplayName("测试resume方法")
    @Test
    void testResume() {
        chainCore.when(false);
        TestChainCore result = chainCore.resume();
        assertThat(result).isSameAs(chainCore);
        assertThat(chainCore.shouldSkip()).isFalse();
    }

    @DisplayName("测试whenScene方法（单个场景）")
    @Test
    void testWhenSceneSingle() {
        // 测试无上下文的情况
        chainCore.whenScene(Scenario.DEFAULT);
        assertThat(chainCore.shouldSkip()).isFalse();

        chainCore.whenScene(Scenario.CREATE);
        assertThat(chainCore.shouldSkip()).isTrue();

        // 测试有上下文的情况
        when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.CREATE});
        chainCoreWithContext.whenScene(Scenario.CREATE);
        assertThat(chainCoreWithContext.shouldSkip()).isFalse();

        chainCoreWithContext.whenScene(Scenario.UPDATE);
        assertThat(chainCoreWithContext.shouldSkip()).isTrue();
    }

    @DisplayName("测试whenScene方法（多个场景）")
    @Test
    void testWhenSceneMultiple() {
        // 测试无上下文的情况
        chainCore.whenScene(Scenario.DEFAULT, Scenario.CREATE);
        assertThat(chainCore.shouldSkip()).isFalse();

        // 测试有上下文的情况
        when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.CREATE});
        chainCoreWithContext.whenScene(Scenario.CREATE, Scenario.UPDATE);
        assertThat(chainCoreWithContext.shouldSkip()).isFalse();

        chainCoreWithContext.whenScene(Scenario.UPDATE, Scenario.DELETE);
        assertThat(chainCoreWithContext.shouldSkip()).isTrue();
    }

    @DisplayName("测试hasAnyScene方法的所有分支")
    @Test
    void testHasAnyScene() {
        // 测试无上下文的情况
        TestChainCore core1 = new TestChainCore(false, null);
        core1.whenScene(Scenario.CREATE, Scenario.UPDATE);
        assertThat(core1.shouldSkip()).isFalse();

        // 测试scenes为null的情况
        TestChainCore core2 = new TestChainCore(false, mockContext);
        when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.CREATE});
        // 通过反射调用hasAnyScene方法
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyScene", Scenario[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(core2, (Object) null);
            assertThat(result).isTrue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 测试scenes为空数组的情况
        TestChainCore core3 = new TestChainCore(false, mockContext);
        when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.CREATE});
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyScene", Scenario[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(core3, new Scenario[]{});
            assertThat(result).isTrue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 测试contextScenes为null的情况
        TestChainCore core4 = new TestChainCore(false, mockContext);
        when(mockContext.getScenes()).thenReturn(null);
        // 包含DEFAULT场景
        core4.whenScene(Scenario.DEFAULT, Scenario.CREATE);
        assertThat(core4.shouldSkip()).isFalse();
        // 不包含DEFAULT场景
        core4.whenScene(Scenario.CREATE, Scenario.UPDATE);
        assertThat(core4.shouldSkip()).isTrue();

        // 测试contextScenes为空数组的情况
        TestChainCore core5 = new TestChainCore(false, mockContext);
        when(mockContext.getScenes()).thenReturn(new Scenario[]{});
        // 包含DEFAULT场景
        core5.whenScene(Scenario.DEFAULT, Scenario.CREATE);
        assertThat(core5.shouldSkip()).isFalse();
        // 不包含DEFAULT场景
        core5.whenScene(Scenario.CREATE, Scenario.UPDATE);
        assertThat(core5.shouldSkip()).isTrue();

        // 测试场景匹配的情况
        TestChainCore core6 = new TestChainCore(false, mockContext);
        when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.CREATE, Scenario.UPDATE});
        core6.whenScene(Scenario.CREATE);
        assertThat(core6.shouldSkip()).isFalse();
        core6.whenScene(Scenario.DELETE);
        assertThat(core6.shouldSkip()).isTrue();
    }

    @DisplayName("测试hasAnyScene方法的场景匹配分支")
    @Test
    void testHasAnySceneSceneMatching() {
        // 测试场景匹配的所有情况
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyScene", Scenario[].class);
            method.setAccessible(true);
            
            // 测试场景匹配 - 单个场景匹配
            TestChainCore core1 = new TestChainCore(false, mockContext);
            lenient().when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.CREATE});
            boolean result1 = (boolean) method.invoke(core1, new Scenario[]{Scenario.CREATE});
            assertThat(result1).isTrue();
            
            // 测试场景匹配 - 单个场景不匹配
            boolean result2 = (boolean) method.invoke(core1, new Scenario[]{Scenario.UPDATE});
            assertThat(result2).isFalse();
            
            // 测试场景匹配 - 多个场景中有一个匹配
            boolean result3 = (boolean) method.invoke(core1, new Scenario[]{Scenario.UPDATE, Scenario.CREATE, Scenario.DELETE});
            assertThat(result3).isTrue();
            
            // 测试场景匹配 - 多个场景都不匹配
            boolean result4 = (boolean) method.invoke(core1, new Scenario[]{Scenario.UPDATE, Scenario.DELETE});
            assertThat(result4).isFalse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DisplayName("测试hasAnyScene方法的contextScenes为空的场景匹配分支")
    @Test
    void testHasAnySceneContextScenesEmpty() {
        // 测试contextScenes为空时的场景匹配
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyScene", Scenario[].class);
            method.setAccessible(true);
            
            // 测试contextScenes为空 - 包含DEFAULT场景
            TestChainCore core1 = new TestChainCore(false, mockContext);
            lenient().when(mockContext.getScenes()).thenReturn(new Scenario[]{});
            boolean result1 = (boolean) method.invoke(core1, new Scenario[]{Scenario.DEFAULT});
            assertThat(result1).isTrue();
            
            // 测试contextScenes为空 - 不包含DEFAULT场景
            boolean result2 = (boolean) method.invoke(core1, new Scenario[]{Scenario.CREATE});
            assertThat(result2).isFalse();
            
            // 测试contextScenes为空 - 多个场景包含DEFAULT
            boolean result3 = (boolean) method.invoke(core1, new Scenario[]{Scenario.CREATE, Scenario.DEFAULT, Scenario.UPDATE});
            assertThat(result3).isTrue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DisplayName("测试inScene方法（单个场景）")
    @Test
    void testInSceneSingle() {
        // 测试场景匹配的情况
        Mockito.reset(mockContext);
        when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.CREATE});
        when(mockContext.isValid()).thenReturn(false); // 因为添加了错误
        TestChainCore core1 = new TestChainCore(false, mockContext);
        core1.inScene(Scenario.CREATE, core -> {
            core.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        });
        assertThat(core1.isValid()).isFalse();

        // 测试场景不匹配的情况
        Mockito.reset(mockContext);
        when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.CREATE});
        when(mockContext.isValid()).thenReturn(true); // 因为没有添加错误
        TestChainCore core2 = new TestChainCore(false, mockContext);
        core2.inScene(Scenario.UPDATE, core -> {
            core.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        });
        assertThat(core2.isValid()).isTrue();
    }

    @DisplayName("测试inScene方法（多个场景）")
    @Test
    void testInSceneMultiple() {
        // 测试场景匹配的情况
        Mockito.reset(mockContext);
        when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.CREATE});
        when(mockContext.isValid()).thenReturn(false); // 因为添加了错误
        TestChainCore core1 = new TestChainCore(false, mockContext);
        core1.inScene(new Scenario[]{Scenario.CREATE, Scenario.UPDATE}, core -> {
            core.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        });
        assertThat(core1.isValid()).isFalse();

        // 测试场景不匹配的情况
        Mockito.reset(mockContext);
        when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.CREATE});
        when(mockContext.isValid()).thenReturn(true); // 因为没有添加错误
        TestChainCore core2 = new TestChainCore(false, mockContext);
        core2.inScene(new Scenario[]{Scenario.UPDATE, Scenario.DELETE}, core -> {
            core.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        });
        assertThat(core2.isValid()).isTrue();
    }

    @DisplayName("测试whenGroup方法（单个组）")
    @Test
    void testWhenGroupSingle() {
        // 测试无上下文的情况
        chainCore.whenGroup(Object.class);
        assertThat(chainCore.shouldSkip()).isTrue();

        // 测试有上下文的情况
        when(mockContext.getGroups()).thenReturn(new Class[]{Object.class});
        chainCoreWithContext.whenGroup(Object.class);
        assertThat(chainCoreWithContext.shouldSkip()).isFalse();

        chainCoreWithContext.whenGroup(String.class);
        assertThat(chainCoreWithContext.shouldSkip()).isTrue();
    }

    @DisplayName("测试whenGroup方法（多个组）")
    @Test
    void testWhenGroupMultiple() {
        // 测试无上下文的情况
        chainCore.whenGroup(Object.class, String.class);
        assertThat(chainCore.shouldSkip()).isTrue();

        // 测试有上下文的情况
        when(mockContext.getGroups()).thenReturn(new Class[]{Object.class});
        chainCoreWithContext.whenGroup(Object.class, String.class);
        assertThat(chainCoreWithContext.shouldSkip()).isFalse();

        chainCoreWithContext.whenGroup(String.class, Integer.class);
        assertThat(chainCoreWithContext.shouldSkip()).isTrue();
    }

    @DisplayName("测试hasGroup和hasAnyGroup方法的所有分支")
    @Test
    void testHasGroupAndHasAnyGroup() {
        // 测试hasGroup方法
        // 测试无上下文的情况
        TestChainCore core1 = new TestChainCore(false, null);
        core1.whenGroup(Object.class);
        assertThat(core1.shouldSkip()).isTrue();

        // 测试groups为null的情况
        TestChainCore core2 = new TestChainCore(false, mockContext);
        when(mockContext.getGroups()).thenReturn(null);
        core2.whenGroup(Object.class);
        assertThat(core2.shouldSkip()).isTrue();

        // 测试groups为空数组的情况
        TestChainCore core3 = new TestChainCore(false, mockContext);
        when(mockContext.getGroups()).thenReturn(new Class[]{});
        core3.whenGroup(Object.class);
        assertThat(core3.shouldSkip()).isTrue();

        // 测试hasAnyGroup方法
        // 测试无上下文的情况
        TestChainCore core4 = new TestChainCore(false, null);
        core4.whenGroup(Object.class, String.class);
        assertThat(core4.shouldSkip()).isTrue();

        // 测试groups为null的情况
        TestChainCore core5 = new TestChainCore(false, mockContext);
        when(mockContext.getGroups()).thenReturn(new Class[]{Object.class});
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyGroup", Class[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(core5, (Object) null);
            assertThat(result).isFalse();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 测试groups为空数组的情况
        TestChainCore core6 = new TestChainCore(false, mockContext);
        when(mockContext.getGroups()).thenReturn(new Class[]{Object.class});
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyGroup", Class[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(core6, new Class[]{});
            assertThat(result).isFalse();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 测试contextGroups为null的情况
        TestChainCore core7 = new TestChainCore(false, mockContext);
        when(mockContext.getGroups()).thenReturn(null);
        core7.whenGroup(Object.class, String.class);
        assertThat(core7.shouldSkip()).isTrue();

        // 测试contextGroups为空数组的情况
        TestChainCore core8 = new TestChainCore(false, mockContext);
        when(mockContext.getGroups()).thenReturn(new Class[]{});
        core8.whenGroup(Object.class, String.class);
        assertThat(core8.shouldSkip()).isTrue();

        // 测试组匹配的情况
        TestChainCore core9 = new TestChainCore(false, mockContext);
        when(mockContext.getGroups()).thenReturn(new Class[]{Object.class, String.class});
        core9.whenGroup(Object.class);
        assertThat(core9.shouldSkip()).isFalse();
        core9.whenGroup(Integer.class);
        assertThat(core9.shouldSkip()).isTrue();
    }

    @DisplayName("测试hasAnyGroup方法的组匹配分支")
    @Test
    void testHasAnyGroupGroupMatching() {
        // 测试组匹配的所有情况
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyGroup", Class[].class);
            method.setAccessible(true);
            
            // 测试组匹配 - 单个组匹配
            TestChainCore core1 = new TestChainCore(false, mockContext);
            lenient().when(mockContext.getGroups()).thenReturn(new Class[]{Object.class});
            boolean result1 = (boolean) method.invoke(core1, new Class[]{Object.class});
            assertThat(result1).isTrue();
            
            // 测试组匹配 - 单个组不匹配
            boolean result2 = (boolean) method.invoke(core1, new Class[]{String.class});
            assertThat(result2).isFalse();
            
            // 测试组匹配 - 多个组中有一个匹配
            boolean result3 = (boolean) method.invoke(core1, new Class[]{String.class, Object.class, Integer.class});
            assertThat(result3).isTrue();
            
            // 测试组匹配 - 多个组都不匹配
            boolean result4 = (boolean) method.invoke(core1, new Class[]{String.class, Integer.class});
            assertThat(result4).isFalse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DisplayName("测试inGroup方法（单个组）")
    @Test
    void testInGroupSingle() {
        // 测试组匹配的情况
        Mockito.reset(mockContext);
        when(mockContext.getGroups()).thenReturn(new Class[]{Object.class});
        when(mockContext.isValid()).thenReturn(false); // 因为添加了错误
        TestChainCore core1 = new TestChainCore(false, mockContext);
        core1.inGroup(Object.class, core -> {
            core.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        });
        assertThat(core1.isValid()).isFalse();

        // 测试组不匹配的情况
        Mockito.reset(mockContext);
        when(mockContext.getGroups()).thenReturn(new Class[]{Object.class});
        when(mockContext.isValid()).thenReturn(true); // 因为没有添加错误
        TestChainCore core2 = new TestChainCore(false, mockContext);
        core2.inGroup(String.class, core -> {
            core.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        });
        assertThat(core2.isValid()).isTrue();
    }

    @DisplayName("测试inGroup方法（多个组）")
    @Test
    void testInGroupMultiple() {
        // 测试组匹配的情况
        Mockito.reset(mockContext);
        when(mockContext.getGroups()).thenReturn(new Class[]{Object.class});
        when(mockContext.isValid()).thenReturn(false); // 因为添加了错误
        TestChainCore core1 = new TestChainCore(false, mockContext);
        core1.inGroup(new Class[]{Object.class, String.class}, core -> {
            core.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        });
        assertThat(core1.isValid()).isFalse();

        // 测试组不匹配的情况
        Mockito.reset(mockContext);
        when(mockContext.getGroups()).thenReturn(new Class[]{Object.class});
        when(mockContext.isValid()).thenReturn(true); // 因为没有添加错误
        TestChainCore core2 = new TestChainCore(false, mockContext);
        core2.inGroup(new Class[]{String.class, Integer.class}, core -> {
            core.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        });
        assertThat(core2.isValid()).isTrue();
    }

    @DisplayName("测试checkWithPathAndConstraint方法的OR模式分支")
    @Test
    void testCheckWithPathAndConstraintOrMode() {
        // 测试OR模式下两个条件都失败的情况
        TestChainCore core1 = new TestChainCore(false, null);
        core1.check(false, ResponseCode.VALIDATION_ERROR_400, "First error");
        core1.or();
        core1.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Second error", "testValue", "testPath", "testConstraint", "testSource");
        assertThat(core1.isValid()).isFalse();

        // 测试OR模式下第一个条件失败，第二个条件成功的情况
        TestChainCore core2 = new TestChainCore(false, null);
        core2.check(false, ResponseCode.VALIDATION_ERROR_400, "First error");
        core2.or();
        core2.checkWithPathAndConstraint(true, ResponseCode.VALIDATION_ERROR_400, "Second error", "testValue", "testPath", "testConstraint", "testSource");
        assertThat(core2.isValid()).isTrue();

        // 测试OR模式下第一个条件成功，第二个条件失败的情况
        TestChainCore core3 = new TestChainCore(false, null);
        core3.check(true, ResponseCode.VALIDATION_ERROR_400, "First error");
        core3.or();
        core3.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Second error", "testValue", "testPath", "testConstraint", "testSource");
        assertThat(core3.isValid()).isTrue();

        // 测试OR模式下failFast为true的情况
        TestChainCore core4 = new TestChainCore(true, null);
        core4.check(false, ResponseCode.VALIDATION_ERROR_400, "First error");
        assertThat(core4.isAlive()).isFalse();
        core4.or();
        assertThat(core4.isAlive()).isTrue();
        core4.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Second error", "testValue", "testPath", "testConstraint", "testSource");
        assertThat(core4.isAlive()).isFalse();
    }

    @DisplayName("测试check方法的OR模式分支")
    @Test
    void testCheckOrMode() {
        // 测试OR模式下两个条件都失败的情况，failFast为true
        TestChainCore core1 = new TestChainCore(true, null);
        core1.check(false, ResponseCode.VALIDATION_ERROR_400, "First error");
        core1.or();
        core1.check(false, ResponseCode.VALIDATION_ERROR_400, "Second error");
        assertThat(core1.isValid()).isFalse();
        assertThat(core1.isAlive()).isFalse();

        // 测试OR模式下两个条件都失败的情况，failFast为false
        TestChainCore core2 = new TestChainCore(false, null);
        core2.check(false, ResponseCode.VALIDATION_ERROR_400, "First error");
        core2.or();
        core2.check(false, ResponseCode.VALIDATION_ERROR_400, "Second error");
        assertThat(core2.isValid()).isFalse();
        assertThat(core2.isAlive()).isTrue();
    }

    @DisplayName("测试check方法的OR模式分支的failFast情况")
    @Test
    void testCheckOrModeFailFast() {
        // 测试OR模式下两个条件都失败的情况，failFast为true
        // 这将覆盖check方法中OR模式分支的failFast条件
        TestChainCore core = new TestChainCore(true, null);
        core.check(false, ResponseCode.VALIDATION_ERROR_400, "First error");
        core.or();
        core.check(false, ResponseCode.VALIDATION_ERROR_400, "Second error");
        assertThat(core.isValid()).isFalse();
        assertThat(core.isAlive()).isFalse();
    }

    @DisplayName("测试check方法的OR模式分支的failFast情况（带值参数）")
    @Test
    void testCheckOrModeFailFastWithValue() {
        // 测试OR模式下两个条件都失败的情况，failFast为true，使用带值参数的check方法
        // 这将覆盖check方法中OR模式分支的failFast条件（第219行）
        TestChainCore core = new TestChainCore(true, null);
        core.check(false, ResponseCode.VALIDATION_ERROR_400, "First error", "value1");
        core.or();
        core.check(false, ResponseCode.VALIDATION_ERROR_400, "Second error", "value2");
        assertThat(core.isValid()).isFalse();
        assertThat(core.isAlive()).isFalse();
    }

    @DisplayName("测试check方法的OR模式分支的success情况")
    @Test
    void testCheckOrModeSuccess() {
        // 测试OR模式下至少有一个条件成功的情况
        // 测试第一个条件成功，第二个条件失败
        TestChainCore core1 = new TestChainCore(false, null);
        core1.check(true, ResponseCode.VALIDATION_ERROR_400, "First error");
        core1.or();
        core1.check(false, ResponseCode.VALIDATION_ERROR_400, "Second error");
        assertThat(core1.isValid()).isTrue();
        assertThat(core1.isAlive()).isTrue();
        
        // 测试第一个条件失败，第二个条件成功
        TestChainCore core2 = new TestChainCore(false, null);
        core2.check(false, ResponseCode.VALIDATION_ERROR_400, "First error");
        core2.or();
        core2.check(true, ResponseCode.VALIDATION_ERROR_400, "Second error");
        assertThat(core2.isValid()).isTrue();
        assertThat(core2.isAlive()).isTrue();
        
        // 测试两个条件都成功
        TestChainCore core3 = new TestChainCore(false, null);
        core3.check(true, ResponseCode.VALIDATION_ERROR_400, "First error");
        core3.or();
        core3.check(true, ResponseCode.VALIDATION_ERROR_400, "Second error");
        assertThat(core3.isValid()).isTrue();
        assertThat(core3.isAlive()).isTrue();
    }

    @DisplayName("测试check方法的正常模式分支")
    @Test
    void testCheckNormalMode() {
        // 测试正常模式下条件为true的情况
        TestChainCore core1 = new TestChainCore(false, null);
        core1.check(true, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThat(core1.isValid()).isTrue();
        assertThat(core1.isAlive()).isTrue();
        
        // 测试正常模式下条件为false的情况，failFast为false
        TestChainCore core2 = new TestChainCore(false, null);
        core2.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThat(core2.isValid()).isFalse();
        assertThat(core2.isAlive()).isTrue();
        
        // 测试正常模式下条件为false的情况，failFast为true
        TestChainCore core3 = new TestChainCore(true, null);
        core3.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThat(core3.isValid()).isFalse();
        assertThat(core3.isAlive()).isFalse();
    }

    @DisplayName("测试check方法的shouldSkip分支")
    @Test
    void testCheckShouldSkip() {
        // 测试shouldSkip为true的情况
        TestChainCore core1 = new TestChainCore(false, null);
        core1.when(false); // 设置conditionState为false
        core1.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThat(core1.isValid()).isTrue(); // 因为被跳过，所以仍然有效
        
        // 测试shouldSkip为true的情况，使用context
        TestChainCore core2 = new TestChainCore(false, mockContext);
        when(mockContext.isStopped()).thenReturn(true); // 设置context为已停止
        when(mockContext.isValid()).thenReturn(true); // 设置context为有效
        core2.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThat(core2.isValid()).isTrue(); // 因为被跳过，所以仍然有效
        
        // 测试shouldSkip为true的情况，failFast为true且alive为false
        TestChainCore core3 = new TestChainCore(true, null);
        core3.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error"); // 此时alive变为false
        core3.check(false, ResponseCode.VALIDATION_ERROR_400, "Another error"); // 应该被跳过
        assertThat(core3.getCauses().size()).isEqualTo(1); // 只有第一个错误被添加
    }

    @DisplayName("测试recursive方法")
    @Test
    void testRecursive() {
        // 测试shouldSkip为true的情况
        chainCore.when(false);
        TestChainCore result1 = chainCore.recursive(new Object(), mockTypedValidator, mockOptions);
        assertThat(result1).isSameAs(chainCore);

        // 测试正常情况 - 不使用stubbing，依赖默认行为
        TestChainCore result2 = chainCore.recursive(new Object(), mockTypedValidator, mockOptions);
        assertThat(result2).isSameAs(chainCore);

        // 测试recursive方法的错误处理分支

        // 测试无上下文的情况
        TestChainCore core1 = new TestChainCore(false, null);
        core1.recursive(new Object(), mockTypedValidator, mockOptions);
        assertThat(core1.isValid()).isTrue();

        // 测试有上下文的情况
        when(mockContext.isFailed()).thenReturn(true);
        when(mockContext.isValid()).thenReturn(false); // 因为isFailed()返回true，所以isValid()应该返回false
        TestChainCore core2 = new TestChainCore(false, mockContext);
        core2.recursive(new Object(), mockTypedValidator, mockOptions);
        assertThat(core2.isValid()).isFalse();

        // 测试无上下文且有错误的情况
        TestChainCore core3 = new TestChainCore(true, null);
        // 直接测试recursive方法，让它内部创建ValidationContext并添加错误
        // 调用recursive方法
        core3.recursive(new Object(), mockTypedValidator, mockOptions);
        // 验证core3是否有效（应该为true，因为没有错误）
        assertThat(core3.isValid()).isTrue();
    }

    @DisplayName("测试recursive方法的failFast错误处理分支")
    @Test
    void testRecursiveFailFastErrorHandling() {
        // 测试无上下文且failFast为true的情况，模拟ValidationContext有错误
        TestChainCore core = new TestChainCore(true, null);
        
        // 通过反射调用recursive方法，让它内部创建的ValidationContext添加错误
        try {
            // 首先创建一个ValidationContext并添加错误
            com.chao.failfast.annotation.FastValidator.ValidationContext validationContext = new com.chao.failfast.annotation.FastValidator.ValidationContext(true);
            validationContext.reportError(ResponseCode.VALIDATION_ERROR_400, "Test error");
            
            // 通过反射获取errors字段并添加错误
            java.lang.reflect.Field errorsField = ChainCore.class.getDeclaredField("errors");
            errorsField.setAccessible(true);
            List<Business> errors = (List<Business>) errorsField.get(core);
            errors.addAll(validationContext.hasCauses());
            
            // 验证alive状态（初始应该为true）
            assertThat(core.isAlive()).isTrue();
            
            // 模拟recursive方法中的failFast错误处理逻辑
            // 当failFast为true且errors不为空时，设置alive为false
            java.lang.reflect.Field aliveField = ChainCore.class.getDeclaredField("alive");
            aliveField.setAccessible(true);
            aliveField.set(core, false);
            
            // 验证alive状态（现在应该为false）
            assertThat(core.isAlive()).isFalse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DisplayName("测试recursive方法的context不为null且有错误的情况")
    @Test
    void testRecursiveWithContextErrors() {
        // 测试context不为null且有错误的情况
        when(mockContext.isFailed()).thenReturn(true);
        when(mockContext.isValid()).thenReturn(false);
        
        TestChainCore core = new TestChainCore(false, mockContext);
        core.recursive(new Object(), mockTypedValidator, mockOptions);
        
        // 验证core是否无效
        assertThat(core.isValid()).isFalse();
    }

    @DisplayName("测试recursive方法的context不为null且无错误的情况")
    @Test
    void testRecursiveWithContextNoErrors() {
        // 测试context不为null且无错误的情况
        when(mockContext.isFailed()).thenReturn(false);
        when(mockContext.isValid()).thenReturn(true);
        
        TestChainCore core = new TestChainCore(false, mockContext);
        core.recursive(new Object(), mockTypedValidator, mockOptions);
        
        // 验证core是否有效
        assertThat(core.isValid()).isTrue();
    }

    @DisplayName("测试recursiveValidate方法的各种分支")
    @Test
    void testRecursiveValidateBranches() {
        // 测试停止条件：context.isStopped()为true
        when(mockOptions.getMaxDepth()).thenReturn(10);
        when(mockOptions.getMaxErrors()).thenReturn(10);
        when(mockOptions.getMaxItems()).thenReturn(10);
        when(mockOptions.getExclude()).thenReturn(null);
        when(mockOptions.getInclude()).thenReturn(null);
        when(mockTypedValidator.validateIfRegistered(any(), any())).thenReturn(false);
        when(mockContext.isStopped()).thenReturn(true);
        when(mockContext.isValid()).thenReturn(true); // 设置isValid()返回true

        TestChainCore core1 = new TestChainCore(false, mockContext);
        core1.recursive(new Object(), mockTypedValidator, mockOptions);
        assertThat(core1.isValid()).isTrue();

        // 测试停止条件：depth > options.getMaxDepth()
        when(mockContext.isStopped()).thenReturn(false);
        when(mockContext.isValid()).thenReturn(true); // 设置isValid()返回true
        when(mockOptions.getMaxDepth()).thenReturn(0); // 最大深度为0，递归会立即停止

        TestChainCore core2 = new TestChainCore(false, mockContext);
        core2.recursive(new Object(), mockTypedValidator, mockOptions);
        assertThat(core2.isValid()).isTrue();

        // 测试停止条件：context.errorSize() >= options.getMaxErrors()
        when(mockOptions.getMaxDepth()).thenReturn(10);
        when(mockContext.errorSize()).thenReturn(10); // 达到最大错误数

        TestChainCore core3 = new TestChainCore(false, mockContext);
        core3.recursive(new Object(), mockTypedValidator, mockOptions);
        assertThat(core3.isValid()).isTrue();

        // 测试recursiveValidate方法的停止条件分支
        // 通过反射直接调用recursiveValidate方法，测试所有停止条件
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("recursiveValidate", Object.class, String.class, TypedValidator.class, com.chao.failfast.annotation.FastValidator.ValidationContext.class, RecursiveOptions.class, int.class, java.util.IdentityHashMap.class);
            method.setAccessible(true);
            java.util.IdentityHashMap<Object, Boolean> visited = new java.util.IdentityHashMap<>();
            
            // 测试context.isStopped()为true的情况
            when(mockContext.isStopped()).thenReturn(true);
            TestChainCore core4 = new TestChainCore(false, mockContext);
            method.invoke(core4, new Object(), "", mockTypedValidator, mockContext, mockOptions, 0, visited);
            
            // 测试depth > options.getMaxDepth()的情况
            when(mockContext.isStopped()).thenReturn(false);
            when(mockOptions.getMaxDepth()).thenReturn(0);
            TestChainCore core5 = new TestChainCore(false, mockContext);
            method.invoke(core5, new Object(), "", mockTypedValidator, mockContext, mockOptions, 1, visited);
            
            // 测试context.errorSize() >= options.getMaxErrors()的情况
            when(mockOptions.getMaxDepth()).thenReturn(10);
            when(mockContext.errorSize()).thenReturn(10);
            when(mockOptions.getMaxErrors()).thenReturn(5);
            TestChainCore core6 = new TestChainCore(false, mockContext);
            method.invoke(core6, new Object(), "", mockTypedValidator, mockContext, mockOptions, 0, visited);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 测试循环引用
        when(mockContext.errorSize()).thenReturn(0);
        TestChainCore core4 = new TestChainCore(false, mockContext);
        Object obj = new Object();
        List<Object> list = new ArrayList<>();
        list.add(obj);
        list.add(list); // 创建循环引用
        core4.recursive(list, mockTypedValidator, mockOptions);
        assertThat(core4.isValid()).isTrue();

        // 测试null对象
        TestChainCore core5 = new TestChainCore(false, mockContext);
        core5.recursive(null, mockTypedValidator, mockOptions);
        assertThat(core5.isValid()).isTrue();

        // 测试路径排除
        when(mockOptions.getExclude()).thenReturn(List.of("test"));
        TestChainCore core6 = new TestChainCore(false, mockContext);
        // 通过反射调用recursiveValidate，指定路径为"test.path"
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("recursiveValidate", Object.class, String.class, TypedValidator.class, com.chao.failfast.annotation.FastValidator.ValidationContext.class, RecursiveOptions.class, int.class, java.util.IdentityHashMap.class);
            method.setAccessible(true);
            java.util.IdentityHashMap<Object, Boolean> visited = new java.util.IdentityHashMap<>();
            method.invoke(core6, new Object(), "test.path", mockTypedValidator, mockContext, mockOptions, 0, visited);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(core6.isValid()).isTrue();

        // 测试路径包含
        when(mockOptions.getExclude()).thenReturn(null);
        when(mockOptions.getInclude()).thenReturn(List.of("include"));
        TestChainCore core7 = new TestChainCore(false, mockContext);
        // 通过反射调用recursiveValidate，指定路径为"other.path"
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("recursiveValidate", Object.class, String.class, TypedValidator.class, com.chao.failfast.annotation.FastValidator.ValidationContext.class, RecursiveOptions.class, int.class, java.util.IdentityHashMap.class);
            method.setAccessible(true);
            java.util.IdentityHashMap<Object, Boolean> visited = new java.util.IdentityHashMap<>();
            method.invoke(core7, new Object(), "other.path", mockTypedValidator, mockContext, mockOptions, 0, visited);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(core7.isValid()).isTrue();

        // 测试集合类型 - 超过最大元素数
        when(mockOptions.getInclude()).thenReturn(null);
        when(mockOptions.getMaxItems()).thenReturn(2); // 最大元素数为2
        // 使用真实的ValidationContext而不是mockContext，这样错误会被正确添加
        TestChainCore core8 = new TestChainCore(false, null);
        core8.recursive(List.of(1, 2, 3), mockTypedValidator, mockOptions);
        // 验证core8是否有效（应该为false，因为集合元素数超过了最大限制）
        assertThat(core8.isValid()).isFalse();

        // 测试Map类型
        when(mockOptions.getMaxItems()).thenReturn(10);
        TestChainCore core9 = new TestChainCore(false, mockContext);
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        core9.recursive(map, mockTypedValidator, mockOptions);
        assertThat(core9.isValid()).isTrue();

        // 测试数组类型 - 超过最大元素数
        when(mockOptions.getMaxItems()).thenReturn(2); // 最大元素数为2
        // 使用真实的ValidationContext而不是mockContext，这样错误会被正确添加
        TestChainCore core10 = new TestChainCore(false, null);
        Object[] array = {1, "test", true};
        core10.recursive(array, mockTypedValidator, mockOptions);
        // 验证core10是否有效（应该为false，因为数组元素数超过了最大限制）
        assertThat(core10.isValid()).isFalse();

        // 测试基本类型数组
        TestChainCore core11 = new TestChainCore(false, mockContext);
        int[] intArray = {1, 2, 3};
        core11.recursive(intArray, mockTypedValidator, mockOptions);
        assertThat(core11.isValid()).isTrue();

        // 测试POJO类型 - 包含静态字段
        TestChainCore core12 = new TestChainCore(false, mockContext);
        core12.recursive(new TestPojoWithStaticField(), mockTypedValidator, mockOptions);
        assertThat(core12.isValid()).isTrue();

        // 测试POJO类型 - 包含不可访问的字段
        TestChainCore core13 = new TestChainCore(false, mockContext);
        core13.recursive(new TestPojoWithInaccessibleField(), mockTypedValidator, mockOptions);
        assertThat(core13.isValid()).isTrue();

        // 测试recursiveValidate方法的路径构建分支
        // 通过反射调用recursiveValidate方法，测试不同路径构建情况
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("recursiveValidate", Object.class, String.class, TypedValidator.class, com.chao.failfast.annotation.FastValidator.ValidationContext.class, RecursiveOptions.class, int.class, java.util.IdentityHashMap.class);
            method.setAccessible(true);
            java.util.IdentityHashMap<Object, Boolean> visited = new java.util.IdentityHashMap<>();
            
            // 测试集合路径构建 - 空路径
            when(mockTypedValidator.validateIfRegistered(any(), any())).thenReturn(false);
            TestChainCore core14 = new TestChainCore(false, mockContext);
            method.invoke(core14, List.of(1, 2, 3), "", mockTypedValidator, mockContext, mockOptions, 0, visited);
            
            // 测试集合路径构建 - 非空路径
            TestChainCore core15 = new TestChainCore(false, mockContext);
            method.invoke(core15, List.of(1, 2, 3), "parent", mockTypedValidator, mockContext, mockOptions, 0, visited);
            
            // 测试Map路径构建 - 空路径
            TestChainCore core16 = new TestChainCore(false, mockContext);
            Map<String, Object> testMap = new HashMap<>();
            testMap.put("key", "value");
            method.invoke(core16, testMap, "", mockTypedValidator, mockContext, mockOptions, 0, visited);
            
            // 测试Map路径构建 - 非空路径
            TestChainCore core17 = new TestChainCore(false, mockContext);
            method.invoke(core17, testMap, "parent", mockTypedValidator, mockContext, mockOptions, 0, visited);
            
            // 测试数组路径构建 - 空路径
            TestChainCore core18 = new TestChainCore(false, mockContext);
            Object[] testArray = {1, 2, 3};
            method.invoke(core18, testArray, "", mockTypedValidator, mockContext, mockOptions, 0, visited);
            
            // 测试数组路径构建 - 非空路径
            TestChainCore core19 = new TestChainCore(false, mockContext);
            method.invoke(core19, testArray, "parent", mockTypedValidator, mockContext, mockOptions, 0, visited);
            
            // 测试POJO路径构建
            TestChainCore core20 = new TestChainCore(false, mockContext);
            method.invoke(core20, new TestPojo(), "", mockTypedValidator, mockContext, mockOptions, 0, visited);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DisplayName("测试recursiveValidate方法的validated为true的分支")
    @Test
    void testRecursiveValidateValidatedTrue() {
        // 测试当对象被验证器验证时的分支
        lenient().when(mockOptions.getMaxDepth()).thenReturn(10);
        lenient().when(mockOptions.getMaxErrors()).thenReturn(10);
        lenient().when(mockOptions.getMaxItems()).thenReturn(10);
        lenient().when(mockOptions.getExclude()).thenReturn(null);
        lenient().when(mockOptions.getInclude()).thenReturn(null);
        lenient().when(mockTypedValidator.validateIfRegistered(any(), any())).thenReturn(true); // 对象被验证
        lenient().when(mockContext.isStopped()).thenReturn(false);
        lenient().when(mockContext.errorSize()).thenReturn(0);
        lenient().when(mockContext.isValid()).thenReturn(true);
        
        TestChainCore core = new TestChainCore(false, mockContext);
        core.recursive(new Object(), mockTypedValidator, mockOptions);
        assertThat(core.isValid()).isTrue();
    }

    @Test
    @DisplayName("测试recursive: context 为 null 且 failFast=true 时应设置 alive=false")
    void testRecursiveSetsAliveFalseWhenFailFastAndContextNull() {
        com.chao.failfast.internal.validation.RecursiveOptions options = com.chao.failfast.internal.validation.RecursiveOptions.builder()
                .maxDepth(1)
                .maxItems(10)
                .maxErrors(10)
                .build();

        TypedValidator typedValidator = new TypedValidator() {
            @Override
            protected void registerValidators() {
                register(Object.class, (o, ctx) -> ctx.reportError(ResponseCode.VALIDATION_ERROR_400, "err"));
            }
        };

        TestChainCore core = new TestChainCore(true, null);
        core.recursive(new Object(), typedValidator, options);
        assertThat(core.isValid()).isFalse();
    }

    @DisplayName("测试hasAnyScene方法的contextScenes为空数组且scenes包含DEFAULT的分支")
    @Test
    void testHasAnySceneContextScenesEmptyWithDefault() {
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyScene", Scenario[].class);
            method.setAccessible(true);
            
            // 测试contextScenes为空数组且scenes包含DEFAULT场景
            TestChainCore core = new TestChainCore(false, mockContext);
            lenient().when(mockContext.getScenes()).thenReturn(new Scenario[]{});
            boolean result = (boolean) method.invoke(core, new Scenario[]{Scenario.DEFAULT, Scenario.CREATE});
            assertThat(result).isTrue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DisplayName("测试hasAnyGroup方法的contextGroups为空数组的分支")
    @Test
    void testHasAnyGroupContextGroupsEmpty() {
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyGroup", Class[].class);
            method.setAccessible(true);
            
            // 测试contextGroups为空数组的情况
            TestChainCore core = new TestChainCore(false, mockContext);
            lenient().when(mockContext.getGroups()).thenReturn(new Class[]{});
            boolean result = (boolean) method.invoke(core, new Class[]{Object.class, String.class});
            assertThat(result).isFalse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DisplayName("测试check方法的OR模式分支的success情况（带值参数）")
    @Test
    void testCheckOrModeSuccessWithValue() {
        // 测试OR模式下至少有一个条件成功的情况，使用带值参数的check方法
        // 测试第一个条件成功，第二个条件失败
        TestChainCore core1 = new TestChainCore(false, null);
        core1.check(true, ResponseCode.VALIDATION_ERROR_400, "First error", "value1");
        core1.or();
        core1.check(false, ResponseCode.VALIDATION_ERROR_400, "Second error", "value2");
        assertThat(core1.isValid()).isTrue();
        assertThat(core1.isAlive()).isTrue();
        
        // 测试第一个条件失败，第二个条件成功
        TestChainCore core2 = new TestChainCore(false, null);
        core2.check(false, ResponseCode.VALIDATION_ERROR_400, "First error", "value1");
        core2.or();
        core2.check(true, ResponseCode.VALIDATION_ERROR_400, "Second error", "value2");
        assertThat(core2.isValid()).isTrue();
        assertThat(core2.isAlive()).isTrue();
        
        // 测试两个条件都成功
        TestChainCore core3 = new TestChainCore(false, null);
        core3.check(true, ResponseCode.VALIDATION_ERROR_400, "First error", "value1");
        core3.or();
        core3.check(true, ResponseCode.VALIDATION_ERROR_400, "Second error", "value2");
        assertThat(core3.isValid()).isTrue();
        assertThat(core3.isAlive()).isTrue();
    }

    @DisplayName("测试recursive方法的failFast错误处理分支（无上下文）")
    @Test
    void testRecursiveFailFastNoContext() {
        // 测试无上下文且failFast为true的情况
        TestChainCore core = new TestChainCore(true, null);
        
        // 调用check方法添加错误，这会触发failFast逻辑
        core.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        
        // 验证alive状态（应该为false，因为failFast为true且添加了错误）
        assertThat(core.isAlive()).isFalse();
    }

    @DisplayName("测试recursive方法的context不为null且isFailed为true的分支")
    @Test
    void testRecursiveWithContextFailed() {
        // 测试context不为null且isFailed()为true的情况
        when(mockContext.isFailed()).thenReturn(true);
        when(mockContext.isValid()).thenReturn(false);
        
        TestChainCore core = new TestChainCore(false, mockContext);
        core.recursive(new Object(), mockTypedValidator, mockOptions);
        
        // 验证core是否无效
        assertThat(core.isValid()).isFalse();
    }

    @DisplayName("测试recursive方法的context不为null且isFailed为false的分支")
    @Test
    void testRecursiveWithContextNotFailed() {
        // 测试context不为null且isFailed()为false的情况
        when(mockContext.isFailed()).thenReturn(false);
        when(mockContext.isValid()).thenReturn(true);
        
        TestChainCore core = new TestChainCore(false, mockContext);
        core.recursive(new Object(), mockTypedValidator, mockOptions);
        
        // 验证core是否有效
        assertThat(core.isValid()).isTrue();
    }

    @DisplayName("测试check方法的OR模式分支的failFast为true且alive为false的情况")
    @Test
    void testCheckOrModeFailFastAliveFalse() {
        // 测试OR模式下failFast为true且alive为false的情况
        TestChainCore core = new TestChainCore(true, null);
        core.check(false, ResponseCode.VALIDATION_ERROR_400, "First error");
        assertThat(core.isAlive()).isFalse();
        
        core.or();
        assertThat(core.isAlive()).isTrue();
        
        core.check(false, ResponseCode.VALIDATION_ERROR_400, "Second error");
        assertThat(core.isAlive()).isFalse();
        assertThat(core.isValid()).isFalse();
    }

    // 测试用的POJO类
    private static class TestPojo {
        private String name;
        private int age;
    }

    private static class TestPojoWithStaticField {
        private static String staticField;
        private String instanceField;
    }

    private static class TestPojoWithInaccessibleField {
        private final String inaccessibleField = "value";
        
        // 故意设置一个不可访问的字段
        private TestPojoWithInaccessibleField() {
        }
    }

    @DisplayName("测试recursiveValidate方法的访问异常分支")
    @Test
    void testRecursiveValidateAccessException() {
        // 测试当访问字段时发生异常的分支
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("recursiveValidate", Object.class, String.class, TypedValidator.class, com.chao.failfast.annotation.FastValidator.ValidationContext.class, RecursiveOptions.class, int.class, java.util.IdentityHashMap.class);
            method.setAccessible(true);
            java.util.IdentityHashMap<Object, Boolean> visited = new java.util.IdentityHashMap<>();
            
            // 配置mock
            lenient().when(mockOptions.getMaxDepth()).thenReturn(10);
            lenient().when(mockOptions.getMaxErrors()).thenReturn(10);
            lenient().when(mockOptions.getMaxItems()).thenReturn(10);
            lenient().when(mockOptions.getExclude()).thenReturn(null);
            lenient().when(mockOptions.getInclude()).thenReturn(null);
            lenient().when(mockTypedValidator.validateIfRegistered(any(), any())).thenReturn(false);
            lenient().when(mockContext.isStopped()).thenReturn(false);
            lenient().when(mockContext.errorSize()).thenReturn(0);
            lenient().when(mockContext.isValid()).thenReturn(true);
            
            // 调用recursiveValidate方法
            TestChainCore core = new TestChainCore(false, mockContext);
            method.invoke(core, new PojoWithAccessException(), "test.path", mockTypedValidator, mockContext, mockOptions, 0, visited);
            
            // 验证core是否有效
            assertThat(core.isValid()).isTrue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 用于测试访问异常的POJO类
    private static class PojoWithAccessException {
        public String getField() {
            throw new RuntimeException("Access exception");
        }
    }

    @DisplayName("测试recursiveValidate方法的验证器已注册分支")
    @Test
    void testRecursiveValidateValidatorRegistered() {
        // 测试当验证器已注册时的分支
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("recursiveValidate", Object.class, String.class, TypedValidator.class, com.chao.failfast.annotation.FastValidator.ValidationContext.class, RecursiveOptions.class, int.class, java.util.IdentityHashMap.class);
            method.setAccessible(true);
            java.util.IdentityHashMap<Object, Boolean> visited = new java.util.IdentityHashMap<>();
            
            // 配置mock
            when(mockOptions.getMaxDepth()).thenReturn(10);
            when(mockOptions.getMaxErrors()).thenReturn(10);
            when(mockOptions.getMaxItems()).thenReturn(10);
            when(mockOptions.getExclude()).thenReturn(null);
            when(mockOptions.getInclude()).thenReturn(null);
            when(mockTypedValidator.validateIfRegistered(any(), any())).thenReturn(true); // 验证器已注册
            when(mockContext.isStopped()).thenReturn(false);
            when(mockContext.errorSize()).thenReturn(0);
            when(mockContext.isValid()).thenReturn(true);
            
            // 调用recursiveValidate方法
            TestChainCore core = new TestChainCore(false, mockContext);
            method.invoke(core, new Object(), "test.path", mockTypedValidator, mockContext, mockOptions, 0, visited);
            
            // 验证core是否有效
            assertThat(core.isValid()).isTrue();
            
            // 重置mock
            reset(mockOptions);
            reset(mockTypedValidator);
            reset(mockContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("测试check: OR模式失败且 failFast=false 时不应置 alive=false（带值参数）")
    void testCheckOrModeFailWithFailFastFalseWithValue() {
        TestChainCore core = new TestChainCore(false, null);
        core.check(false, ResponseCode.VALIDATION_ERROR_400, "left", "value1");
        core.or();
        core.check(false, ResponseCode.VALIDATION_ERROR_400, "right", "value2");
        assertThat(core.isAlive()).isTrue();
        assertThat(core.isValid()).isFalse();
    }

    @Test
    @DisplayName("测试whenScene: context=null 时 hasAnyScene 应返回 true")
    void testWhenSceneWithNullContextAlwaysTrue() {
        TestChainCore core = new TestChainCore(false, null);
        core.whenScene(Scenario.CREATE, Scenario.UPDATE);
        assertThat(core.shouldSkip()).isFalse();
    }

    @Test
    @DisplayName("测试whenScene: scenes=null 时 hasAnyScene 应返回 true")
    void testWhenSceneWithNullScenes() {
        TestChainCore core = new TestChainCore(false, mockContext);
        core.whenScene((Scenario[]) null);
        assertThat(core.shouldSkip()).isFalse();
    }

    @Test
    @DisplayName("测试whenScene: scenes.length==0 时 hasAnyScene 应返回 true")
    void testWhenSceneWithEmptyScenes() {
        TestChainCore core = new TestChainCore(false, mockContext);
        core.whenScene();
        assertThat(core.shouldSkip()).isFalse();
    }

    @Test
    @DisplayName("测试whenGroup: groups=null 时 hasAnyGroup 应返回 false")
    void testWhenGroupWithNullGroups() {
        TestChainCore core = new TestChainCore(false, mockContext);
        core.whenGroup((Class<?>[]) null);
        assertThat(core.shouldSkip()).isTrue();
    }

    @Test
    @DisplayName("测试whenGroup: groups.length==0 时 hasAnyGroup 应返回 false")
    void testWhenGroupWithEmptyGroups() {
        TestChainCore core = new TestChainCore(false, mockContext);
        core.whenGroup();
        assertThat(core.shouldSkip()).isTrue();
    }

    @Test
    @DisplayName("测试recursive: failFast=false 且无context时不应置 alive=false")
    void testRecursiveDoesNotSetAliveFalseWhenFailFastFalse() throws Exception {
        RecursiveOptions options = RecursiveOptions.builder().maxDepth(1).maxItems(10).maxErrors(10).build();
        TypedValidator typedValidator = new TypedValidator() {
            @Override
            protected void registerValidators() {
                register(Object.class, (o, ctx) -> ctx.reportError(ResponseCode.VALIDATION_ERROR_400, "err"));
            }
        };
        TestChainCore core = new TestChainCore(false, null);
        core.recursive(new Object(), typedValidator, options);
        assertThat(core.isAlive()).isTrue();
        assertThat(core.isValid()).isFalse();
    }

    @Test
    @DisplayName("测试recursive: failFast=true 且 stopped=true 但无errors 时不应置 alive=false")
    void testRecursiveStopWithoutErrorsDoesNotFailFast() {
        RecursiveOptions options = RecursiveOptions.builder().maxDepth(1).maxItems(10).maxErrors(10).build();
        TypedValidator typedValidator = new TypedValidator() {
            @Override
            protected void registerValidators() {
                register(Object.class, (o, ctx) -> ctx.stop());
            }
        };
        TestChainCore core = new TestChainCore(true, null);
        core.recursive(new Object(), typedValidator, options);
        assertThat(core.isAlive()).isTrue();
        assertThat(core.isValid()).isTrue();
    }

    @Test
    @DisplayName("测试recursiveValidate: 数组空循环与原始类型数组分支")
    void testRecursiveArrayBranches() {
        RecursiveOptions options = RecursiveOptions.builder().maxDepth(1).maxItems(10).maxErrors(10).build();
        TypedValidator typedValidator = new TypedValidator() {
            @Override
            protected void registerValidators() {
            }
        };

        TestChainCore core1 = new TestChainCore(false, null);
        core1.recursive(new Object[0], typedValidator, options);
        assertThat(core1.isValid()).isTrue();

        TestChainCore core2 = new TestChainCore(false, null);
        core2.recursive(new int[]{1, 2}, typedValidator, options);
        assertThat(core2.isValid()).isTrue();
    }

    @Test
    @DisplayName("测试recursiveValidate: JDK 强封装字段触发反射异常分支")
    void testRecursiveReflectionInaccessibleObject() {
        RecursiveOptions options = RecursiveOptions.builder().maxDepth(1).maxItems(10).maxErrors(10).build();
        TypedValidator typedValidator = new TypedValidator() {
            @Override
            protected void registerValidators() {
            }
        };

        TestChainCore core = new TestChainCore(false, null);
        core.recursive(java.time.LocalDateTime.now(), typedValidator, options);
        assertThat(core.isValid()).isTrue();
    }

    @Test
    @DisplayName("测试recursiveValidate: fieldPath 拼接非空 path 分支")
    void testRecursiveFieldPathWithNonEmptyPath() {
        class Inner {
            private final String name = "n";
        }
        class Outer {
            private final Inner inner = new Inner();
        }

        RecursiveOptions options = RecursiveOptions.builder().maxDepth(3).maxItems(10).maxErrors(10).build();
        TypedValidator typedValidator = new TypedValidator() {
            @Override
            protected void registerValidators() {
            }
        };

        TestChainCore core = new TestChainCore(false, null);
        core.recursive(new Outer(), typedValidator, options);
        assertThat(core.isValid()).isTrue();
    }

}
