package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Scope 类覆盖率补充测试
 * 覆盖未被测试的分支
 */
@DisplayName("Scope 覆盖率补充测试")
public class ScopeCoverageTest {

    private TestChainCore chainCore;

    @BeforeEach
    void setUp() {
        ValidationContext mockContext = mock(ValidationContext.class);
        chainCore = new TestChainCore(false, mockContext);
    }

    // 测试用的ChainCore实现
    static class TestChainCore extends ChainCore<TestChainCore> {
        public TestChainCore(boolean failFast, ValidationContext context) {
            super(failFast, context);
        }
    }

    // 测试用的POJO
    static class TestObject {
        private String name;
        private int age;
        private TestObject nestedObject;

        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public TestObject(String name, int age, TestObject nestedObject) {
            this.name = name;
            this.age = age;
            this.nestedObject = nestedObject;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public TestObject getNestedObject() {
            return nestedObject;
        }
    }

    @Test
    @DisplayName("测试 FieldRef.as 方法 - dot >= 0 分支")
    void testFieldRefAsWithDot() {
        Scope<TestObject> scope = new Scope<>(chainCore, new TestObject("test", 18), "parent");
        Scope.FieldRef<String> fieldRef = scope.field("child.name", TestObject::getName);
        
        // 测试带点的路径
        PathEntry<String> pathEntry = fieldRef.as("alias");
        assertNotNull(pathEntry);
    }

    @Test
    @DisplayName("测试断言方法 - ended 为 true 的情况")
    void testAssertionMethodsWithEnded() {
        TestObject testObject = new TestObject("test", 18);
        Scope<TestObject> scope = new Scope<>(chainCore, testObject, "test");

        try {
            // 设置 ended 为 true
            Field endedField = Scope.class.getDeclaredField("ended");
            endedField.setAccessible(true);
            endedField.set(scope, true);

            // 测试各种断言方法
            scope.notNull(ResponseCode.VALIDATION_ERROR_400);

            PathEntry<String> namePath = scope.fieldEntry(TestObject::getName);
            scope.notBlank(namePath, ResponseCode.VALIDATION_ERROR_400);

            PathEntry<Integer> agePath = new PathEntry<>(testObject.getAge(), "age");
            scope.positive(agePath, ResponseCode.VALIDATION_ERROR_400);

            scope.email(namePath, ResponseCode.VALIDATION_ERROR_400);
            scope.mobile(namePath, ResponseCode.VALIDATION_ERROR_400);

            PathEntry<Boolean> truePath = new PathEntry<>(true, "bool");
            scope.isTrue(truePath, ResponseCode.VALIDATION_ERROR_400);

            PathEntry<Boolean> falsePath = new PathEntry<>(false, "bool");
            scope.isFalse(falsePath, ResponseCode.VALIDATION_ERROR_400);

            PathEntry<java.util.List<String>> listPath = new PathEntry<>(Collections.singletonList("test"), "list");
            scope.notEmptyCollection(listPath, ResponseCode.VALIDATION_ERROR_400);

            PathEntry<Map<String, String>> mapPath = new PathEntry<>(Collections.singletonMap("key", "value"), "map");
            scope.notEmptyMap(mapPath, ResponseCode.VALIDATION_ERROR_400);

            scope.length(namePath, 1, 10, ResponseCode.VALIDATION_ERROR_400);
            scope.between(agePath, 1, 100, ResponseCode.VALIDATION_ERROR_400);
            scope.matches(namePath, ".*", ResponseCode.VALIDATION_ERROR_400);

            scope.check(namePath, s -> true, ResponseCode.VALIDATION_ERROR_400, "Test error");
            scope.check(namePath, () -> true, ResponseCode.VALIDATION_ERROR_400, "Test error");

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 check 方法 - ref.value() 为 null 的情况")
    void testCheckWithNullValue() {
        Scope<TestObject> scope = new Scope<>(chainCore, new TestObject("test", 18), "test");
        PathEntry<String> nullPath = new PathEntry<>(null, "field");
        scope.check(nullPath, s -> true, ResponseCode.VALIDATION_ERROR_400, "Test error");
    }

    @Test
    @DisplayName("测试 when 和 unless 方法 - ended 为 true 的情况")
    void testConditionalMethodsWithEnded() {
        TestObject testObject = new TestObject("test", 18);
        Scope<TestObject> scope = new Scope<>(chainCore, testObject, "test");

        try {
            // 设置 ended 为 true
            Field endedField = Scope.class.getDeclaredField("ended");
            endedField.setAccessible(true);
            endedField.set(scope, true);

            // 测试 when 方法
            scope.when(true, () -> {});
            scope.when(obj -> obj.getAge() > 10, () -> {});

            // 测试 unless 方法
            scope.unless(false, () -> {});
            scope.unless(obj -> obj.getAge() > 10, () -> {});

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 nested 方法 - nestedItem 为 null 的情况")
    void testNestedWithNullItem() {
        TestObject testObject = new TestObject("test", 18, null);
        Scope<TestObject> scope = new Scope<>(chainCore, testObject, "test");

        // 测试 nested 方法
        scope.nested(TestObject::getNestedObject, s -> {});
        scope.nested("nestedObject", TestObject::getNestedObject, s -> {});
    }

    @Test
    @DisplayName("测试 nested(带字段名) - ended 为 true 时应直接返回")
    void testNestedWithFieldNameAndEnded() throws Exception {
        TestObject testObject = new TestObject("test", 18, new TestObject("n", 1));
        Scope<TestObject> scope = new Scope<>(chainCore, testObject, "test");

        Field endedField = Scope.class.getDeclaredField("ended");
        endedField.setAccessible(true);
        endedField.set(scope, true);

        scope.nested("nestedObject", TestObject::getNestedObject, s -> fail());
    }

    @Test
    @DisplayName("测试 forEach 方法 - collection 为 null 的情况")
    void testForEachWithNullCollection() {
        TestObject testObject = new TestObject("test", 18);
        Scope<TestObject> scope = new Scope<>(chainCore, testObject, "test");

        // 测试 forEach 方法
        scope.forEach(obj -> null, s -> {});
        scope.forEach("collection", obj -> null, s -> {});
    }

    @Test
    @DisplayName("测试 forEachEntry 方法 - map 为 null 的情况")
    void testForEachEntryWithNullMap() {
        TestObject testObject = new TestObject("test", 18);
        Scope<TestObject> scope = new Scope<>(chainCore, testObject, "test");

        // 测试 forEachEntry 方法
        scope.forEachEntry(obj -> null, (k, s) -> {});
        scope.forEachEntry("map", obj -> null, (k, s) -> {});
    }

    @Test
    @DisplayName("测试 getFieldNameFromGetter 方法 - 异常情况")
    void testGetFieldNameFromGetterWithException() {
        Scope<TestObject> scope = new Scope<>(chainCore, new TestObject("test", 18), "test");

        Function<TestObject, String> invalidGetter = new Function<>() {
            @Override
            public String apply(TestObject testObject) {
                return testObject.getName();
            }

            @Override
            public String toString() {
                throw new RuntimeException("Test");
            }
        };

        try {
            java.lang.reflect.Method method = Scope.class.getDeclaredMethod("getFieldNameFromGetter", Function.class);
            method.setAccessible(true);
            String fieldName = (String) method.invoke(scope, invalidGetter);
            assertEquals("field", fieldName);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 getFieldNameFromGetter 方法 - dot/paren 分支")
    void testGetFieldNameFromGetterParsesSubstring() throws Exception {
        Scope<TestObject> scope = new Scope<>(chainCore, new TestObject("test", 18), "test");
        Function<TestObject, String> getter = new Function<>() {
            @Override
            public String apply(TestObject testObject) {
                return testObject.getName();
            }

            @Override
            public String toString() {
                return "x.getName()";
            }
        };

        java.lang.reflect.Method method = Scope.class.getDeclaredMethod("getFieldNameFromGetter", Function.class);
        method.setAccessible(true);
        String fieldName = (String) method.invoke(scope, getter);
        assertEquals("getName", fieldName);
    }

    @Test
    @DisplayName("测试 joinPath 方法 - parent 为 null 或 blank 的情况")
    void testJoinPathWithNullParent() {
        try {
            java.lang.reflect.Method method = Scope.class.getDeclaredMethod("joinPath", String.class, String.class);
            method.setAccessible(true);

            // 测试 parent 为 null
            String result1 = (String) method.invoke(null, null, "child");
            assertEquals("child", result1);

            // 测试 parent 为 blank
            String result2 = (String) method.invoke(null, "   ", "child");
            assertEquals("child", result2);

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 forEach 方法（带字段名）- ended 为 true 的情况")
    void testForEachWithFieldNameAndEnded() {
        TestObject testObject = new TestObject("test", 18);
        Scope<TestObject> scope = new Scope<>(chainCore, testObject, "test");

        try {
            // 设置 ended 为 true
            Field endedField = Scope.class.getDeclaredField("ended");
            endedField.setAccessible(true);
            endedField.set(scope, true);

            // 测试 forEach 方法（带字段名）
            scope.forEach("collection", obj -> java.util.Collections.singletonList(obj.getName()), s -> {});

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 forEachEntry 方法（带字段名）- ended 为 true 的情况")
    void testForEachEntryWithFieldNameAndEnded() {
        TestObject testObject = new TestObject("test", 18);
        Scope<TestObject> scope = new Scope<>(chainCore, testObject, "test");

        try {
            // 设置 ended 为 true
            Field endedField = Scope.class.getDeclaredField("ended");
            endedField.setAccessible(true);
            endedField.set(scope, true);

            // 测试 forEachEntry 方法（带字段名）
            scope.forEachEntry("map", obj -> java.util.Collections.singletonMap("name", obj.getName()), (k, s) -> {});

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DisplayName("测试 getFieldNameFromGetter 方法 - 正常解析情况")
    void testGetFieldNameFromGetterNormal() {
        Scope<TestObject> scope = new Scope<>(chainCore, new TestObject("test", 18), "test");

        // 测试正常的 getter
        Function<TestObject, String> validGetter = TestObject::getName;

        try {
            java.lang.reflect.Method method = Scope.class.getDeclaredMethod("getFieldNameFromGetter", Function.class);
            method.setAccessible(true);
            String fieldName = (String) method.invoke(scope, validGetter);
            assertNotNull(fieldName);

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
} 
