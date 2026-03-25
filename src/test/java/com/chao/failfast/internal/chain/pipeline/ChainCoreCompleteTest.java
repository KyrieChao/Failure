package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.constant.Scenario;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.internal.validation.RecursiveOptions;
import com.chao.failfast.validator.TypedValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ChainCore 100% 覆盖率测试
 */
@DisplayName("ChainCore 100% 覆盖率测试")
public class ChainCoreCompleteTest {

    private TestChainCore chainCore;
    private ValidationContext mockContext;
    private TypedValidator mockValidator;

    @BeforeEach
    void setUp() {
        mockContext = mock(ValidationContext.class);
        when(mockContext.isValid()).thenReturn(true);
        when(mockContext.errorSize()).thenReturn(0);
        when(mockContext.isStopped()).thenReturn(false);
        when(mockContext.getScenes()).thenReturn(new Scenario[0]);
        when(mockContext.getGroups()).thenReturn(new Class[0]);
        when(mockContext.hasCauses()).thenReturn(new ArrayList<>());
        
        mockValidator = mock(TypedValidator.class);
        when(mockValidator.validateIfRegistered(any(), any())).thenReturn(false);
    }

    // 测试用的ChainCore实现
    static class TestChainCore extends ChainCore<TestChainCore> {
        public TestChainCore(boolean failFast, ValidationContext context) {
            super(failFast, context);
        }
    }

    // 测试用的场景
    interface TestGroup {}

    @Test
    @DisplayName("测试 or 方法 - context.isStopped() 分支")
    void testOrWithContextStopped() {
        when(mockContext.isStopped()).thenReturn(true);
        chainCore = new TestChainCore(true, mockContext);
        chainCore.or();
        // 应该跳过
    }

    @Test
    @DisplayName("测试 check(Supplier<Boolean>, CheckSpec) - shouldSkip 分支")
    void testCheckSupplierCheckSpecWithSkip() {
        chainCore = new TestChainCore(true, null);
        chainCore.when(false); // 使shouldSkip()返回true
        CheckSpec spec = CheckSpec.of(ResponseCode.VALIDATION_ERROR_400, "Test error", "invalid value");
        chainCore.check(() -> true, spec);
        // 应该跳过
    }

    @Test
    @DisplayName("测试 checkRef 方法 - 完整覆盖")
    void testCheckRefComplete() {
        // 测试 shouldSkip 分支
        chainCore = new TestChainCore(true, null);
        chainCore.when(false); // 使 shouldSkip() 返回 true
        PathEntry<String> pathEntry = new PathEntry<>("test", "field");
        chainCore.checkRef(false, ResponseCode.VALIDATION_ERROR_400, pathEntry);
        
        // 测试 valueRef 为 null 的情况 - 成功
        chainCore = new TestChainCore(true, null);
        chainCore.checkRef(true, ResponseCode.VALIDATION_ERROR_400, null);
        
        // 测试 valueRef 为 null 的情况 - 失败
        chainCore = new TestChainCore(true, null);
        chainCore.checkRef(false, ResponseCode.VALIDATION_ERROR_400, null);
        
        // 测试 valueRef 不为 null 的情况 - 成功
        chainCore = new TestChainCore(true, null);
        pathEntry = new PathEntry<>("test", "field");
        chainCore.checkRef(true, ResponseCode.VALIDATION_ERROR_400, pathEntry);
        
        // 测试 valueRef 不为 null 的情况 - 失败（failFast=true）
        chainCore = new TestChainCore(true, null);
        pathEntry = new PathEntry<>("test", "field");
        chainCore.checkRef(false, ResponseCode.VALIDATION_ERROR_400, pathEntry);
        
        // 测试 valueRef 不为 null 的情况 - 失败（failFast=false）
        chainCore = new TestChainCore(false, null);
        pathEntry = new PathEntry<>("test", "field");
        chainCore.checkRef(false, ResponseCode.VALIDATION_ERROR_400, pathEntry);
    }

    @Test
    @DisplayName("测试 checkWithPathAndConstraint 方法 - 完整覆盖")
    void testCheckWithPathAndConstraintComplete() {
        // 测试 shouldSkip 分支
        chainCore = new TestChainCore(true, null);
        chainCore.when(false); // 使 shouldSkip() 返回 true
        chainCore.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint", "source");
        
        // 测试 OR模式 - 成功情况
        chainCore = new TestChainCore(true, null);
        chainCore.or(); // 激活 OR模式
        chainCore.checkWithPathAndConstraint(true, ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint", "source");
        
        // 测试 OR模式 - 失败情况（failFast=true）
        chainCore = new TestChainCore(true, null);
        chainCore.or(); // 激活 OR模式
        chainCore.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint", "source");
        
        // 测试 OR模式 - 失败情况（failFast=false）
        chainCore = new TestChainCore(false, null);
        chainCore.or(); // 激活 OR模式
        chainCore.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint", "source");
        
        // 测试 正常模式 - 成功情况
        chainCore = new TestChainCore(true, null);
        chainCore.checkWithPathAndConstraint(true, ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint", "source");
        
        // 测试 正常模式 - 失败情况（failFast=true）
        chainCore = new TestChainCore(true, null);
        chainCore.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint", "source");
        
        // 测试 正常模式 - 失败情况（failFast=false）
        chainCore = new TestChainCore(false, null);
        chainCore.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint", "source");
        
        // 测试 带context的情况
        chainCore = new TestChainCore(true, mockContext);
        chainCore.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint", "source");
    }

    @Test
    @DisplayName("测试 addError 方法 - 带constraint")
    void testAddErrorWithConstraint() {
        chainCore = new TestChainCore(false, null);
        chainCore.addError(ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint");
    }

    @Test
    @DisplayName("测试 getSceneName 方法 - 完整覆盖")
    void testGetSceneName() {
        // 测试context为null的情况
        chainCore = new TestChainCore(false, null);
        // 反射调用getSceneName方法
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("getSceneName");
            method.setAccessible(true);
            String result = (String) method.invoke(chainCore);
            assertEquals(Scenario.DEFAULT.name(), result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，scenes为null的情况
        when(mockContext.getScenes()).thenReturn(null);
        chainCore = new TestChainCore(false, mockContext);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("getSceneName");
            method.setAccessible(true);
            String result = (String) method.invoke(chainCore);
            assertEquals(Scenario.DEFAULT.name(), result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，scenes为空数组的情况
        when(mockContext.getScenes()).thenReturn(new Scenario[0]);
        chainCore = new TestChainCore(false, mockContext);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("getSceneName");
            method.setAccessible(true);
            String result = (String) method.invoke(chainCore);
            assertEquals(Scenario.DEFAULT.name(), result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，scenes有一个元素的情况
        when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.DEFAULT});
        chainCore = new TestChainCore(false, mockContext);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("getSceneName");
            method.setAccessible(true);
            String result = (String) method.invoke(chainCore);
            assertEquals(Scenario.DEFAULT.name(), result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，scenes有多个元素的情况
        when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.DEFAULT, Scenario.CREATE});
        chainCore = new TestChainCore(false, mockContext);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("getSceneName");
            method.setAccessible(true);
            String result = (String) method.invoke(chainCore);
            assertEquals("MULTI", result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 buildBusiness 方法 - 无detail")
    void testBuildBusinessWithoutDetail() {
        chainCore = new TestChainCore(false, null);
        // 反射调用buildBusiness方法
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("buildBusiness", ResponseCode.class, String.class, Object.class, String.class);
            method.setAccessible(true);
            Business business = (Business) method.invoke(chainCore, ResponseCode.VALIDATION_ERROR_400, null, "value", "path");
            assertNotNull(business);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 isValid 方法 - 完整覆盖")
    void testIsValidComplete() {
        // 测试context不为null，context.isValid()为false的情况
        when(mockContext.isValid()).thenReturn(false);
        chainCore = new TestChainCore(false, mockContext);
        assertFalse(chainCore.isValid());
        
        // 测试context不为null，context.isValid()为true，alive为true的情况
        when(mockContext.isValid()).thenReturn(true);
        chainCore = new TestChainCore(false, mockContext);
        assertTrue(chainCore.isValid());
        
        // 测试context不为null，context.isValid()为true，alive为false的情况
        chainCore = new TestChainCore(false, mockContext);
        // 通过反射设置alive为false
        try {
            java.lang.reflect.Field field = ChainCore.class.getDeclaredField("alive");
            field.setAccessible(true);
            field.set(chainCore, false);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        assertFalse(chainCore.isValid());
        
        // 测试context为null，errors为空，alive为true的情况
        chainCore = new TestChainCore(false, null);
        assertTrue(chainCore.isValid());
        
        // 测试context为null，errors不为空的情况
        chainCore = new TestChainCore(false, null);
        chainCore.addError(ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path");
        assertFalse(chainCore.isValid());
        
        // 测试context为null，errors为空，alive为false的情况
        chainCore = new TestChainCore(false, null);
        try {
            java.lang.reflect.Field field = ChainCore.class.getDeclaredField("alive");
            field.setAccessible(true);
            field.set(chainCore, false);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        assertFalse(chainCore.isValid());
    }

    @Test
    @DisplayName("测试 hasScene 方法 - 完整覆盖")
    void testHasScene() {
        // 测试context为null的情况
        chainCore = new TestChainCore(false, null);
        // 反射调用hasScene方法
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasScene", Scenario.class);
            method.setAccessible(true);
            // 测试DEFAULT场景
            boolean result1 = (boolean) method.invoke(chainCore, Scenario.DEFAULT);
            assertTrue(result1);
            // 测试非DEFAULT场景
            boolean result2 = (boolean) method.invoke(chainCore, Scenario.CREATE);
            assertFalse(result2);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，scenes为null的情况
        when(mockContext.getScenes()).thenReturn(null);
        chainCore = new TestChainCore(false, mockContext);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasScene", Scenario.class);
            method.setAccessible(true);
            // 测试DEFAULT场景
            boolean result1 = (boolean) method.invoke(chainCore, Scenario.DEFAULT);
            assertTrue(result1);
            // 测试非DEFAULT场景
            boolean result2 = (boolean) method.invoke(chainCore, Scenario.CREATE);
            assertFalse(result2);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，scenes为空数组的情况
        when(mockContext.getScenes()).thenReturn(new Scenario[0]);
        chainCore = new TestChainCore(false, mockContext);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasScene", Scenario.class);
            method.setAccessible(true);
            // 测试DEFAULT场景
            boolean result1 = (boolean) method.invoke(chainCore, Scenario.DEFAULT);
            assertTrue(result1);
            // 测试非DEFAULT场景
            boolean result2 = (boolean) method.invoke(chainCore, Scenario.CREATE);
            assertFalse(result2);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，scenes包含指定场景的情况
        when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.CREATE});
        chainCore = new TestChainCore(false, mockContext);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasScene", Scenario.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, Scenario.CREATE);
            assertTrue(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，scenes不包含指定场景的情况
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasScene", Scenario.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, Scenario.UPDATE);
            assertFalse(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 hasAnyScene 方法 - 完整覆盖")
    void testHasAnyScene() {
        // 测试context为null的情况
        chainCore = new TestChainCore(false, null);
        // 反射调用hasAnyScene方法
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyScene", Scenario[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, (Object) new Scenario[]{Scenario.CREATE});
            assertTrue(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试scenes为null的情况
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyScene", Scenario[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, (Object) null);
            assertTrue(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试scenes为空数组的情况
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyScene", Scenario[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, (Object) new Scenario[0]);
            assertTrue(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，contextScenes为null的情况
        when(mockContext.getScenes()).thenReturn(null);
        chainCore = new TestChainCore(false, mockContext);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyScene", Scenario[].class);
            method.setAccessible(true);
            // 测试包含DEFAULT场景
            boolean result1 = (boolean) method.invoke(chainCore, (Object) new Scenario[]{Scenario.DEFAULT});
            assertTrue(result1);
            // 测试不包含DEFAULT场景
            boolean result2 = (boolean) method.invoke(chainCore, (Object) new Scenario[]{Scenario.CREATE});
            assertFalse(result2);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，contextScenes包含指定场景的情况
        when(mockContext.getScenes()).thenReturn(new Scenario[]{Scenario.CREATE});
        chainCore = new TestChainCore(false, mockContext);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyScene", Scenario[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, (Object) new Scenario[]{Scenario.CREATE});
            assertTrue(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，contextScenes不包含指定场景的情况
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyScene", Scenario[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, (Object) new Scenario[]{Scenario.UPDATE});
            assertFalse(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 hasGroup 方法 - 完整覆盖")
    void testHasGroup() {
        // 测试context为null的情况
        chainCore = new TestChainCore(false, null);
        // 反射调用hasGroup方法
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasGroup", Class.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, TestGroup.class);
            assertFalse(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，groups为null的情况
        when(mockContext.getGroups()).thenReturn(null);
        chainCore = new TestChainCore(false, mockContext);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasGroup", Class.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, TestGroup.class);
            assertFalse(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，groups包含指定组的情况
        when(mockContext.getGroups()).thenReturn(new Class[]{TestGroup.class});
        chainCore = new TestChainCore(false, mockContext);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasGroup", Class.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, TestGroup.class);
            assertTrue(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，groups不包含指定组的情况
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasGroup", Class.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, Object.class);
            assertFalse(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 hasAnyGroup 方法 - 完整覆盖")
    void testHasAnyGroup() {
        // 测试context为null的情况
        chainCore = new TestChainCore(false, null);
        // 反射调用hasAnyGroup方法
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyGroup", Class[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, (Object) new Class[]{TestGroup.class});
            assertFalse(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试groups为null的情况
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyGroup", Class[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, (Object) null);
            assertFalse(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试groups为空数组的情况
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyGroup", Class[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, (Object) new Class[0]);
            assertFalse(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，contextGroups为null的情况
        when(mockContext.getGroups()).thenReturn(null);
        chainCore = new TestChainCore(false, mockContext);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyGroup", Class[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, (Object) new Class[]{TestGroup.class});
            assertFalse(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，contextGroups包含指定组的情况
        when(mockContext.getGroups()).thenReturn(new Class[]{TestGroup.class});
        chainCore = new TestChainCore(false, mockContext);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyGroup", Class[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, (Object) new Class[]{TestGroup.class});
            assertTrue(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试context不为null，contextGroups不包含指定组的情况
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyGroup", Class[].class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(chainCore, (Object) new Class[]{Object.class});
            assertFalse(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 recursive 方法 - 完整覆盖")
    void testRecursiveComplete() {
        RecursiveOptions options = RecursiveOptions.builder().maxDepth(5).build();
        
        // 测试 shouldSkip 分支
        chainCore = new TestChainCore(true, null);
        chainCore.when(false); // 使 shouldSkip() 返回 true
        chainCore.recursive("test", mockValidator, options);
        
        // 测试带context的情况 - 验证失败
        chainCore = new TestChainCore(true, mockContext);
        when(mockContext.isFailed()).thenReturn(true);
        when(mockContext.hasCauses()).thenReturn(java.util.Collections.emptyList());
        chainCore.recursive("test", mockValidator, options);
        
        // 测试带context的情况 - 验证成功
        chainCore = new TestChainCore(true, mockContext);
        when(mockContext.isFailed()).thenReturn(false);
        chainCore.recursive("test", mockValidator, options);
        
        // 测试不带context的情况 - 验证失败（failFast=true）
        chainCore = new TestChainCore(true, null);
        chainCore.recursive("test", mockValidator, options);
        
        // 测试不带context的情况 - 验证失败（failFast=false）
        chainCore = new TestChainCore(false, null);
        chainCore.recursive("test", mockValidator, options);
        
        // 测试不带context的情况 - 验证成功
        // 这里需要模拟 typedValidator.validateIfRegistered 不返回错误
        when(mockValidator.validateIfRegistered(any(), any())).thenReturn(true);
        chainCore = new TestChainCore(true, null);
        chainCore.recursive("test", mockValidator, options);
    }
    
    @Test
    @DisplayName("测试 checkWithPathAndConstraint 方法 - OR模式完整覆盖")
    void testCheckWithPathAndConstraintOrMode() {
        // 测试 OR 模式 - 最终成功
        chainCore = new TestChainCore(true, null);
        chainCore.or(); // 激活 OR 模式
        chainCore.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint");
        
        // 测试 OR 模式 - 最终失败（failFast=true）
        chainCore = new TestChainCore(true, null);
        chainCore.or(); // 激活 OR 模式
        chainCore.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint");
        
        // 测试 OR 模式 - 最终失败（failFast=false）
        chainCore = new TestChainCore(false, null);
        chainCore.or(); // 激活 OR 模式
        chainCore.checkWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Test error", "value", "path", "constraint");
    }
    
    @Test
    @DisplayName("测试 recursiveValidate 方法 - 完整覆盖")
    void testRecursiveValidateComplete() {
        chainCore = new TestChainCore(false, null);
        RecursiveOptions options = RecursiveOptions.builder()
                .maxDepth(1)
                .maxErrors(1)
                .maxItems(1)
                .exclude(java.util.Collections.singletonList("exclude"))
                .include(java.util.Collections.singletonList("include"))
                .build();
        
        // 测试集合大小超过限制
        java.util.List<String> largeList = java.util.Arrays.asList("item1", "item2");
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("recursiveValidate", 
                    Object.class, String.class, TypedValidator.class, ValidationContext.class, RecursiveOptions.class, int.class, java.util.IdentityHashMap.class);
            method.setAccessible(true);
            ValidationContext context = new ValidationContext(false);
            method.invoke(chainCore, largeList, "list", mockValidator, context, options, 0, new java.util.IdentityHashMap<>());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试映射处理
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("key", "value");
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("recursiveValidate", 
                    Object.class, String.class, TypedValidator.class, ValidationContext.class, RecursiveOptions.class, int.class, java.util.IdentityHashMap.class);
            method.setAccessible(true);
            ValidationContext context = new ValidationContext(false);
            method.invoke(chainCore, map, "map", mockValidator, context, options, 0, new java.util.IdentityHashMap<>());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试对象数组处理
        String[] array = new String[]{"item1", "item2"};
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("recursiveValidate", 
                    Object.class, String.class, TypedValidator.class, ValidationContext.class, RecursiveOptions.class, int.class, java.util.IdentityHashMap.class);
            method.setAccessible(true);
            ValidationContext context = new ValidationContext(false);
            method.invoke(chainCore, array, "array", mockValidator, context, options, 0, new java.util.IdentityHashMap<>());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试基本类型数组处理
        int[] intArray = new int[]{1, 2};
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("recursiveValidate", 
                    Object.class, String.class, TypedValidator.class, ValidationContext.class, RecursiveOptions.class, int.class, java.util.IdentityHashMap.class);
            method.setAccessible(true);
            ValidationContext context = new ValidationContext(false);
            method.invoke(chainCore, intArray, "intArray", mockValidator, context, options, 0, new java.util.IdentityHashMap<>());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试静态字段处理
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("recursiveValidate", 
                    Object.class, String.class, TypedValidator.class, ValidationContext.class, RecursiveOptions.class, int.class, java.util.IdentityHashMap.class);
            method.setAccessible(true);
            ValidationContext context = new ValidationContext(false);
            method.invoke(chainCore, new TestObjectWithStaticField(), "obj", mockValidator, context, options, 0, new java.util.IdentityHashMap<>());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    // 测试用的带静态字段的POJO
    static class TestObjectWithStaticField {
        private static String staticField = "static";
        private String instanceField = "instance";
        
        public String getInstanceField() {
            return instanceField;
        }
    }
    
    @Test
    @DisplayName("测试 hasAnyScene 和 hasAnyGroup 方法 - 完整覆盖")
    void testHasAnySceneAndGroup() {
        // 测试 hasAnyScene 方法
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyScene", Scenario[].class);
            method.setAccessible(true);
            
            // 测试 context 为 null 的情况
            chainCore = new TestChainCore(false, null);
            boolean result1 = (boolean) method.invoke(chainCore, (Object) null);
            assertTrue(result1);
            
            // 测试 scenes 为空数组的情况
            boolean result2 = (boolean) method.invoke(chainCore, (Object) new Scenario[0]);
            assertTrue(result2);
            
            // 测试 context 不为 null，scenes 包含 DEFAULT 的情况
            when(mockContext.getScenes()).thenReturn(null);
            chainCore = new TestChainCore(false, mockContext);
            boolean result3 = (boolean) method.invoke(chainCore, (Object) new Scenario[]{Scenario.DEFAULT});
            assertTrue(result3);
            
            // 测试 context 不为 null，scenes 不包含 DEFAULT 的情况
            boolean result4 = (boolean) method.invoke(chainCore, (Object) new Scenario[]{Scenario.CREATE});
            assertFalse(result4);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // 测试 hasAnyGroup 方法
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("hasAnyGroup", Class[].class);
            method.setAccessible(true);
            
            // 测试 context 为 null 的情况
            chainCore = new TestChainCore(false, null);
            boolean result1 = (boolean) method.invoke(chainCore, (Object) null);
            assertFalse(result1);
            
            // 测试 groups 为空数组的情况
            boolean result2 = (boolean) method.invoke(chainCore, (Object) new Class[0]);
            assertFalse(result2);
            
            // 测试 context 不为 null，groups 为 null 的情况
            when(mockContext.getGroups()).thenReturn(null);
            chainCore = new TestChainCore(false, mockContext);
            boolean result3 = (boolean) method.invoke(chainCore, (Object) new Class[]{TestGroup.class});
            assertFalse(result3);
            
            // 测试 context 不为 null，groups 为空数组的情况
            when(mockContext.getGroups()).thenReturn(new Class[0]);
            boolean result4 = (boolean) method.invoke(chainCore, (Object) new Class[]{TestGroup.class});
            assertFalse(result4);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    // 测试用的带异常的POJO
    static class TestObjectWithException {
        private String name;
        
        public TestObjectWithException() {
            this.name = "test";
        }
        
        public String getName() {
            return name;
        }
        
        // 模拟反射获取字段时的异常
        private void throwException() {
            throw new RuntimeException("Test exception");
        }
    }

    @Test
    @DisplayName("测试 isPrimitiveOrWrapper 方法 - 完整覆盖")
    void testIsPrimitiveOrWrapper() {
        chainCore = new TestChainCore(false, null);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("isPrimitiveOrWrapper", Class.class);
            method.setAccessible(true);
            
            // 测试基本类型
            assertTrue((boolean) method.invoke(chainCore, int.class));
            
            // 测试包装类型
            assertTrue((boolean) method.invoke(chainCore, Boolean.class));
            assertTrue((boolean) method.invoke(chainCore, Byte.class));
            assertTrue((boolean) method.invoke(chainCore, Character.class));
            assertTrue((boolean) method.invoke(chainCore, Double.class));
            assertTrue((boolean) method.invoke(chainCore, Float.class));
            assertTrue((boolean) method.invoke(chainCore, Integer.class));
            assertTrue((boolean) method.invoke(chainCore, Long.class));
            assertTrue((boolean) method.invoke(chainCore, Short.class));
            assertTrue((boolean) method.invoke(chainCore, Void.class));
            
            // 测试非基本类型和包装类型
            assertFalse((boolean) method.invoke(chainCore, String.class));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 isStringOrEnum 方法 - 完整覆盖")
    void testIsStringOrEnum() {
        chainCore = new TestChainCore(false, null);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("isStringOrEnum", Class.class);
            method.setAccessible(true);
            
            // 测试String
            assertTrue((boolean) method.invoke(chainCore, String.class));
            
            // 测试Enum
            assertTrue((boolean) method.invoke(chainCore, Scenario.class));
            
            // 测试非String和Enum
            assertFalse((boolean) method.invoke(chainCore, Integer.class));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 isExcluded 方法 - 完整覆盖")
    void testIsExcluded() {
        chainCore = new TestChainCore(false, null);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("isExcluded", String.class, List.class);
            method.setAccessible(true);
            
            // 测试exclude为null
            assertFalse((boolean) method.invoke(chainCore, "path", null));
            
            // 测试exclude为空列表
            assertFalse((boolean) method.invoke(chainCore, "path", new ArrayList<>()));
            
            // 测试路径被排除
            List<String> exclude = List.of("exclude");
            assertTrue((boolean) method.invoke(chainCore, "exclude.path", exclude));
            
            // 测试路径未被排除
            assertFalse((boolean) method.invoke(chainCore, "include.path", exclude));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 isIncluded 方法 - 完整覆盖")
    void testIsIncluded() {
        chainCore = new TestChainCore(false, null);
        try {
            java.lang.reflect.Method method = ChainCore.class.getDeclaredMethod("isIncluded", String.class, List.class);
            method.setAccessible(true);
            
            // 测试include为null
            assertTrue((boolean) method.invoke(chainCore, "path", null));
            
            // 测试include为空列表
            assertTrue((boolean) method.invoke(chainCore, "path", new ArrayList<>()));
            
            // 测试路径被包含
            List<String> include = List.of("include");
            assertTrue((boolean) method.invoke(chainCore, "include.path", include));
            
            // 测试路径未被包含
            assertFalse((boolean) method.invoke(chainCore, "exclude.path", include));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    // 测试用的POJO
    static class TestObject {
        private String name;
        private int age;
        private static String staticField = "static";

        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
}
