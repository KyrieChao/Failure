package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Scope测试")
class ScopeTest {

    @Mock
    private ChainCore<?> chainCore;

    private Scope<TestObject> scope;
    private TestObject testObject;

    @BeforeEach
    void setUp() {
        testObject = new TestObject("Test", 18, "test@example.com", true);
        scope = new Scope<>(chainCore, testObject, "testObject");
    }

    @Test
    @DisplayName("测试构造函数")
    void testConstructor() {
        assertThat(scope).isNotNull();
    }

    @Test
    @DisplayName("测试it方法")
    void testIt() {
        PathEntry<TestObject> pathEntry = scope.it();
        assertThat(pathEntry).isNotNull();
        assertThat(pathEntry.value()).isEqualTo(testObject);
        assertThat(pathEntry.path()).isEqualTo("testObject");
    }

    @Test
    @DisplayName("测试field方法 - 使用Function")
    void testFieldWithFunction() {
        Scope.FieldRef<String> fieldRef = scope.field(TestObject::getName);
        assertThat(fieldRef).isNotNull();
        assertThat(fieldRef.value()).isEqualTo("Test");
    }

    @Test
    @DisplayName("测试field方法 - 使用字段名和Function")
    void testFieldWithNameAndFunction() {
        Scope.FieldRef<String> fieldRef = scope.field("name", TestObject::getName);
        assertThat(fieldRef).isNotNull();
        assertThat(fieldRef.value()).isEqualTo("Test");
    }

    @Test
    @DisplayName("测试fieldEntry方法")
    void testFieldEntry() {
        PathEntry<String> pathEntry = scope.fieldEntry(TestObject::getName);
        assertThat(pathEntry).isNotNull();
        assertThat(pathEntry.value()).isEqualTo("Test");
    }

    @Test
    @DisplayName("测试FieldRef的as方法")
    void testFieldRefAs() {
        Scope.FieldRef<String> fieldRef = scope.field("name", TestObject::getName);
        PathEntry<String> pathEntry = fieldRef.as("customName");
        assertThat(pathEntry).isNotNull();
        assertThat(pathEntry.value()).isEqualTo("Test");
    }

    @Test
    @DisplayName("测试FieldRef的ref方法")
    void testFieldRefRef() {
        Scope.FieldRef<String> fieldRef = scope.field("name", TestObject::getName);
        PathEntry<String> pathEntry = fieldRef.ref();
        assertThat(pathEntry).isNotNull();
        assertThat(pathEntry.value()).isEqualTo("Test");
    }

    @Test
    @DisplayName("测试FieldRef的value方法")
    void testFieldRefValue() {
        Scope.FieldRef<String> fieldRef = scope.field("name", TestObject::getName);
        assertThat(fieldRef.value()).isEqualTo("Test");
    }

    @Test
    @DisplayName("测试notNull方法")
    void testNotNull() {
        Scope<TestObject> result = scope.notNull(ResponseCode.VALIDATION_ERROR_NULL);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_NULL), any(PathEntry.class));
    }

    @Test
    @DisplayName("测试notBlank方法 - PathEntry版本")
    void testNotBlankWithPathEntry() {
        PathEntry<String> pathEntry = scope.fieldEntry(TestObject::getName);
        Scope<TestObject> result = scope.notBlank(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), eq(pathEntry));
    }

    @Test
    @DisplayName("测试notBlank方法 - FieldRef版本")
    void testNotBlankWithFieldRef() {
        Scope.FieldRef<String> fieldRef = scope.field("name", TestObject::getName);
        Scope<TestObject> result = scope.notBlank(fieldRef, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), any(PathEntry.class));
    }

    @Test
    @DisplayName("测试positive方法 - PathEntry版本")
    void testPositiveWithPathEntry() {
        PathEntry<Integer> pathEntry = scope.fieldEntry(TestObject::getAge);
        Scope<TestObject> result = scope.positive(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), eq(pathEntry));
    }

    @Test
    @DisplayName("测试positive方法 - FieldRef版本")
    void testPositiveWithFieldRef() {
        Scope.FieldRef<Integer> fieldRef = scope.field("age", TestObject::getAge);
        Scope<TestObject> result = scope.positive(fieldRef, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), any(PathEntry.class));
    }

    @Test
    @DisplayName("测试email方法 - PathEntry版本")
    void testEmailWithPathEntry() {
        PathEntry<String> pathEntry = scope.fieldEntry(TestObject::getEmail);
        Scope<TestObject> result = scope.email(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), eq(pathEntry));
    }

    @Test
    @DisplayName("测试email方法 - FieldRef版本")
    void testEmailWithFieldRef() {
        Scope.FieldRef<String> fieldRef = scope.field("email", TestObject::getEmail);
        Scope<TestObject> result = scope.email(fieldRef, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), any(PathEntry.class));
    }

    @Test
    @DisplayName("测试mobile方法 - PathEntry版本")
    void testMobileWithPathEntry() {
        testObject.setMobile("13800138000");
        PathEntry<String> pathEntry = scope.fieldEntry(TestObject::getMobile);
        Scope<TestObject> result = scope.mobile(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), eq(pathEntry));
    }

    @Test
    @DisplayName("测试mobile方法 - FieldRef版本")
    void testMobileWithFieldRef() {
        testObject.setMobile("13800138000");
        Scope.FieldRef<String> fieldRef = scope.field("mobile", TestObject::getMobile);
        Scope<TestObject> result = scope.mobile(fieldRef, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), any(PathEntry.class));
    }

    @Test
    @DisplayName("测试isTrue方法")
    void testIsTrue() {
        PathEntry<Boolean> pathEntry = scope.fieldEntry(TestObject::isActive);
        Scope<TestObject> result = scope.isTrue(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), eq(pathEntry));
    }

    @Test
    @DisplayName("测试isFalse方法")
    void testIsFalse() {
        PathEntry<Boolean> pathEntry = scope.fieldEntry(TestObject::isActive);
        Scope<TestObject> result = scope.isFalse(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), eq(pathEntry));
    }

    @Test
    @DisplayName("测试notEmptyCollection方法 - PathEntry版本")
    void testNotEmptyCollectionWithPathEntry() {
        testObject.setHobbies(new ArrayList<>());
        PathEntry<List<String>> pathEntry = scope.fieldEntry(TestObject::getHobbies);
        Scope<TestObject> result = scope.notEmptyCollection(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), eq(pathEntry));
    }

    @Test
    @DisplayName("测试notEmptyCollection方法 - FieldRef版本")
    void testNotEmptyCollectionWithFieldRef() {
        testObject.setHobbies(new ArrayList<>());
        Scope.FieldRef<List<String>> fieldRef = scope.field("hobbies", TestObject::getHobbies);
        Scope<TestObject> result = scope.notEmptyCollection(fieldRef, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), any(PathEntry.class));
    }

    @Test
    @DisplayName("测试notEmptyMap方法 - PathEntry版本")
    void testNotEmptyMapWithPathEntry() {
        testObject.setProperties(new HashMap<>());
        PathEntry<Map<String, String>> pathEntry = scope.fieldEntry(TestObject::getProperties);
        Scope<TestObject> result = scope.notEmptyMap(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), eq(pathEntry));
    }

    @Test
    @DisplayName("测试notEmptyMap方法 - FieldRef版本")
    void testNotEmptyMapWithFieldRef() {
        testObject.setProperties(new HashMap<>());
        Scope.FieldRef<Map<String, String>> fieldRef = scope.field("properties", TestObject::getProperties);
        Scope<TestObject> result = scope.notEmptyMap(fieldRef, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), any(PathEntry.class));
    }

    @Test
    @DisplayName("测试length方法 - PathEntry版本")
    void testLengthWithPathEntry() {
        PathEntry<String> pathEntry = scope.fieldEntry(TestObject::getName);
        Scope<TestObject> result = scope.length(pathEntry, 1, 10, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), eq(pathEntry));
    }

    @Test
    @DisplayName("测试length方法 - FieldRef版本")
    void testLengthWithFieldRef() {
        Scope.FieldRef<String> fieldRef = scope.field("name", TestObject::getName);
        Scope<TestObject> result = scope.length(fieldRef, 1, 10, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), any(PathEntry.class));
    }

    @Test
    @DisplayName("测试between方法 - PathEntry版本")
    void testBetweenWithPathEntry() {
        PathEntry<Integer> pathEntry = scope.fieldEntry(TestObject::getAge);
        Scope<TestObject> result = scope.between(pathEntry, 1, 100, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), eq(pathEntry));
    }

    @Test
    @DisplayName("测试between方法 - FieldRef版本")
    void testBetweenWithFieldRef() {
        Scope.FieldRef<Integer> fieldRef = scope.field("age", TestObject::getAge);
        Scope<TestObject> result = scope.between(fieldRef, 1, 100, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), any(PathEntry.class));
    }

    @Test
    @DisplayName("测试matches方法 - PathEntry版本")
    void testMatchesWithPathEntry() {
        PathEntry<String> pathEntry = scope.fieldEntry(TestObject::getName);
        Scope<TestObject> result = scope.matches(pathEntry, "^[A-Za-z]+$", ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), eq(pathEntry));
    }

    @Test
    @DisplayName("测试matches方法 - FieldRef版本")
    void testMatchesWithFieldRef() {
        Scope.FieldRef<String> fieldRef = scope.field("name", TestObject::getName);
        Scope<TestObject> result = scope.matches(fieldRef, "^[A-Za-z]+$", ResponseCode.VALIDATION_ERROR_400);
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), any(PathEntry.class));
    }

    @Test
    @DisplayName("测试check方法 - PathEntry版本")
    void testCheckWithPathEntry() {
        PathEntry<String> pathEntry = scope.fieldEntry(TestObject::getName);
        Scope<TestObject> result = scope.check(pathEntry, s -> s.length() > 0, ResponseCode.VALIDATION_ERROR_400, "Name should not be empty");
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), eq(pathEntry));
    }

    @Test
    @DisplayName("测试check方法 - FieldRef版本")
    void testCheckWithFieldRef() {
        Scope.FieldRef<String> fieldRef = scope.field("name", TestObject::getName);
        Scope<TestObject> result = scope.check(fieldRef, s -> s.length() > 0, ResponseCode.VALIDATION_ERROR_400, "Name should not be empty");
        assertThat(result).isSameAs(scope);
        verify(chainCore).checkRef(anyBoolean(), eq(ResponseCode.VALIDATION_ERROR_400), any(PathEntry.class));
    }

    @Test
    @DisplayName("测试when方法 - 布尔条件")
    void testWhenWithBoolean() {
        boolean[] executed = {false};
        Scope<TestObject> result = scope.when(true, () -> executed[0] = true);
        assertThat(result).isSameAs(scope);
        assertThat(executed[0]).isTrue();

        executed[0] = false;
        result = scope.when(false, () -> executed[0] = true);
        assertThat(result).isSameAs(scope);
        assertThat(executed[0]).isFalse();
    }

    @Test
    @DisplayName("测试when方法 - Predicate")
    void testWhenWithPredicate() {
        boolean[] executed = {false};
        Scope<TestObject> result = scope.when(obj -> obj.getAge() > 10, () -> executed[0] = true);
        assertThat(result).isSameAs(scope);
        assertThat(executed[0]).isTrue();

        executed[0] = false;
        result = scope.when(obj -> obj.getAge() > 20, () -> executed[0] = true);
        assertThat(result).isSameAs(scope);
        assertThat(executed[0]).isFalse();
    }

    @Test
    @DisplayName("测试unless方法 - 布尔条件")
    void testUnlessWithBoolean() {
        boolean[] executed = {false};
        Scope<TestObject> result = scope.unless(false, () -> executed[0] = true);
        assertThat(result).isSameAs(scope);
        assertThat(executed[0]).isTrue();

        executed[0] = false;
        result = scope.unless(true, () -> executed[0] = true);
        assertThat(result).isSameAs(scope);
        assertThat(executed[0]).isFalse();
    }

    @Test
    @DisplayName("测试unless方法 - Predicate")
    void testUnlessWithPredicate() {
        boolean[] executed = {false};
        Scope<TestObject> result = scope.unless(obj -> obj.getAge() > 20, () -> executed[0] = true);
        assertThat(result).isSameAs(scope);
        assertThat(executed[0]).isTrue();

        executed[0] = false;
        result = scope.unless(obj -> obj.getAge() > 10, () -> executed[0] = true);
        assertThat(result).isSameAs(scope);
        assertThat(executed[0]).isFalse();
    }

    @Test
    @DisplayName("测试nested方法 - 使用Function")
    void testNestedWithFunction() {
        TestObject nestedObject = new TestObject("Nested", 5, null, false);
        testObject.setChild(nestedObject);

        boolean[] executed = {false};
        Scope<TestObject> result = scope.nested(TestObject::getChild, childScope -> {
            executed[0] = true;
            assertThat(childScope).isNotNull();
        });
        assertThat(result).isSameAs(scope);
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("测试nested方法 - 使用字段名和Function")
    void testNestedWithNameAndFunction() {
        TestObject nestedObject = new TestObject("Nested", 5, null, false);
        testObject.setChild(nestedObject);

        boolean[] executed = {false};
        Scope<TestObject> result = scope.nested("child", TestObject::getChild, childScope -> {
            executed[0] = true;
            assertThat(childScope).isNotNull();
        });
        assertThat(result).isSameAs(scope);
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("测试forEach方法 - 使用Function")
    void testForEachWithFunction() {
        List<String> hobbies = new ArrayList<>();
        hobbies.add("Reading");
        hobbies.add("Sports");
        testObject.setHobbies(hobbies);

        int[] count = {0};
        Scope<TestObject> result = scope.forEach(TestObject::getHobbies, hobbyScope -> {
            count[0]++;
            assertThat(hobbyScope).isNotNull();
        });
        assertThat(result).isSameAs(scope);
        assertThat(count[0]).isEqualTo(2);
    }

    @Test
    @DisplayName("测试forEach方法 - 使用字段名和Function")
    void testForEachWithNameAndFunction() {
        List<String> hobbies = new ArrayList<>();
        hobbies.add("Reading");
        hobbies.add("Sports");
        testObject.setHobbies(hobbies);

        int[] count = {0};
        Scope<TestObject> result = scope.forEach("hobbies", TestObject::getHobbies, hobbyScope -> {
            count[0]++;
            assertThat(hobbyScope).isNotNull();
        });
        assertThat(result).isSameAs(scope);
        assertThat(count[0]).isEqualTo(2);
    }

    @Test
    @DisplayName("测试forEachEntry方法 - 使用Function")
    void testForEachEntryWithFunction() {
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        testObject.setProperties(properties);

        int[] count = {0};
        Scope<TestObject> result = scope.forEachEntry(TestObject::getProperties, (key, valueScope) -> {
            count[0]++;
            assertThat(key).isNotNull();
            assertThat(valueScope).isNotNull();
        });
        assertThat(result).isSameAs(scope);
        assertThat(count[0]).isEqualTo(2);
    }

    @Test
    @DisplayName("测试forEachEntry方法 - 使用字段名和Function")
    void testForEachEntryWithNameAndFunction() {
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        testObject.setProperties(properties);

        int[] count = {0};
        Scope<TestObject> result = scope.forEachEntry("properties", TestObject::getProperties, (key, valueScope) -> {
            count[0]++;
            assertThat(key).isNotNull();
            assertThat(valueScope).isNotNull();
        });
        assertThat(result).isSameAs(scope);
        assertThat(count[0]).isEqualTo(2);
    }

    @Test
    @DisplayName("测试stopItemOnFail方法")
    void testStopItemOnFail() {
        Scope<TestObject> result = scope.stopItemOnFail();
        assertThat(result).isSameAs(scope);
    }

    @Test
    @DisplayName("测试done方法")
    void testDone() {
        scope.done();
        // 无异常抛出即可
    }

    @Test
    @DisplayName("测试getFieldNameFromGetter方法 - 解析失败的情况")
    void testGetFieldNameFromGetterWithParseFailure() {
        // 测试getFieldNameFromGetter方法的catch块
        // 通过反射调用私有方法
        try {
            java.lang.reflect.Method method = Scope.class.getDeclaredMethod("getFieldNameFromGetter", Function.class);
            method.setAccessible(true);
            // 传入一个会导致toString()抛出异常的Function
            Function<TestObject, String> invalidGetter = new Function<TestObject, String>() {
                @Override
                public String apply(TestObject testObject) {
                    return testObject.getName();
                }
                @Override
                public String toString() {
                    throw new RuntimeException("Test exception");
                }
            };
            String fieldName = (String) method.invoke(scope, invalidGetter);
            assertThat(fieldName).isEqualTo("field");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("测试joinPath方法 - 边界情况")
    void testJoinPath() {
        // 测试joinPath方法的边界情况
        try {
            java.lang.reflect.Method method = Scope.class.getDeclaredMethod("joinPath", String.class, String.class);
            method.setAccessible(true);
            
            // 测试parent为null
            String result1 = (String) method.invoke(null, null, "child");
            assertThat(result1).isEqualTo("child");
            
            // 测试parent为空白字符串
            String result2 = (String) method.invoke(null, "   ", "child");
            assertThat(result2).isEqualTo("child");
            
            // 测试正常情况
            String result3 = (String) method.invoke(null, "parent", "child");
            assertThat(result3).isEqualTo("parent.child");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("测试endOnFail方法 - 有错误的情况")
    void testEndOnFailWithError() {
        // 测试endOnFail方法
        // 首先设置stopItemOnFail为true
        scope.stopItemOnFail();
        
        // 模拟chainCore.errorSize()返回大于baseErrorSize的值
        when(chainCore.errorSize()).thenReturn(1); // 假设baseErrorSize为0
        
        // 通过反射调用endOnFail方法
        try {
            java.lang.reflect.Method method = Scope.class.getDeclaredMethod("endOnFail");
            method.setAccessible(true);
            method.invoke(scope);
            
            // 验证ended是否被设置为true
            // 通过反射获取ended字段
            java.lang.reflect.Field endedField = Scope.class.getDeclaredField("ended");
            endedField.setAccessible(true);
            boolean ended = (boolean) endedField.get(scope);
            assertThat(ended).isTrue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("测试验证方法在ended为true时的行为")
    void testValidationMethodsWhenEnded() {
        // 首先设置ended为true
        try {
            java.lang.reflect.Field endedField = Scope.class.getDeclaredField("ended");
            endedField.setAccessible(true);
            endedField.set(scope, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // 测试各种验证方法在ended为true时的行为
        PathEntry<String> nameEntry = scope.fieldEntry(TestObject::getName);
        PathEntry<Integer> ageEntry = scope.fieldEntry(TestObject::getAge);
        PathEntry<Boolean> activeEntry = scope.fieldEntry(TestObject::isActive);
        
        // 测试notNull方法
        Scope<TestObject> result1 = scope.notNull(ResponseCode.VALIDATION_ERROR_NULL);
        assertThat(result1).isSameAs(scope);
        
        // 测试notBlank方法
        Scope<TestObject> result2 = scope.notBlank(nameEntry, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result2).isSameAs(scope);
        
        // 测试positive方法
        Scope<TestObject> result3 = scope.positive(ageEntry, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result3).isSameAs(scope);
        
        // 测试isTrue方法
        Scope<TestObject> result4 = scope.isTrue(activeEntry, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result4).isSameAs(scope);
        
        // 测试isFalse方法
        Scope<TestObject> result5 = scope.isFalse(activeEntry, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result5).isSameAs(scope);
        
        // 测试条件验证方法
        boolean[] executed = {false};
        Scope<TestObject> result6 = scope.when(true, () -> executed[0] = true);
        assertThat(result6).isSameAs(scope);
        assertThat(executed[0]).isFalse(); // 应该不执行
        
        // 测试nested方法
        Scope<TestObject> result7 = scope.nested(TestObject::getChild, childScope -> {
            executed[0] = true;
        });
        assertThat(result7).isSameAs(scope);
        assertThat(executed[0]).isFalse(); // 应该不执行
    }

    // 测试用的POJO
    private static class TestObject {
        private String name;
        private int age;
        private String email;
        private boolean active;
        private String mobile;
        private List<String> hobbies;
        private Map<String, String> properties;
        private TestObject child;

        public TestObject(String name, int age, String email, boolean active) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.active = active;
            this.hobbies = new ArrayList<>();
            this.properties = new HashMap<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public List<String> getHobbies() {
            return hobbies;
        }

        public void setHobbies(List<String> hobbies) {
            this.hobbies = hobbies;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }

        public TestObject getChild() {
            return child;
        }

        public void setChild(TestObject child) {
            this.child = child;
        }
    }
}
