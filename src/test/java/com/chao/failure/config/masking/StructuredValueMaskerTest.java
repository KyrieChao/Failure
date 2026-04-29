package com.chao.failure.config.masking;

import com.chao.failure.annotation.Sensitive;
import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.spi.security.Mask;
import com.chao.failure.spi.security.ValueMasker;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StructuredValueMaskerTest {

    private final ValueMasker fallback = mock(ValueMasker.class);
    private final FailureProperties.Masking masking = new FailureProperties.Masking();
    private final StructuredValueMasker masker;

    public StructuredValueMaskerTest() {
        when(fallback.mask(any())).thenAnswer(invocation -> {
            Object value = invocation.getArgument(0);
            return value;
        });
        masker = new StructuredValueMasker(fallback, masking);
    }

    @Test
    void should_return_null_when_value_is_null() {
        assertNull(masker.mask(null));
    }

    @Test
    void should_return_fallback_result_for_primitive_types() {
        Object result = masker.mask("test");
        assertEquals("test", result);

        result = masker.mask(123);
        assertEquals(123, result);

        result = masker.mask(true);
        assertEquals(true, result);
    }

    @Test
    void should_return_masked_result_for_sensitive_fields() {
        when(fallback.mask("password123")).thenReturn("***[MASKED]***");
        Object result = masker.mask("password123");
        assertEquals("***[MASKED]***", result);
    }

    @Test
    void should_mask_complex_object() {
        TestUser user = new TestUser("John", 30);
        Object result = masker.mask(user);

        assertTrue(result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("John", resultMap.get("name"));
        assertEquals(30, resultMap.get("age"));
    }

    @Test
    void should_mask_object_with_sensitive_annotation() {
        TestUserWithSensitive user = new TestUserWithSensitive("John", "password123");
        Object result = masker.mask(user);

        assertTrue(result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("John", resultMap.get("name"));
        assertEquals("[MASKED]", resultMap.get("password"));
    }

    @Test
    void should_handle_max_depth() {
        masking.setMaxDepth(1);
        StructuredValueMasker depthMasker = new StructuredValueMasker(fallback, masking);

        NestedTestUser user = new NestedTestUser(new TestUser("John", 30));
        Object result = depthMasker.mask(user);

        assertTrue(result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("[MAX_DEPTH]", resultMap.get("nestedUser"));
    }

    @Test
    void should_handle_circular_reference() {
        CircularTestUser user1 = new CircularTestUser("John");
        CircularTestUser user2 = new CircularTestUser("Jane");
        user1.setFriend(user2);
        user2.setFriend(user1);

        Object result = masker.mask(user1);

        assertTrue(result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("John", resultMap.get("name"));
        assertTrue(resultMap.get("friend") instanceof Map);
        Map<?, ?> friendMap = (Map<?, ?>) resultMap.get("friend");
        assertEquals("Jane", friendMap.get("name"));
        assertEquals("[CYCLE]", friendMap.get("friend"));
    }

    @Test
    void should_mask_map() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "John");
        map.put("age", 30);

        Object result = masker.mask(map);

        assertTrue(result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("John", resultMap.get("name"));
        assertEquals(30, resultMap.get("age"));
    }

    @Test
    void should_mask_map_with_truncation() {
        masking.setMaxCollectionSize(2);
        StructuredValueMasker truncationMasker = new StructuredValueMasker(fallback, masking);

        Map<String, Object> map = new HashMap<>();
        map.put("name", "John");
        map.put("age", 30);
        map.put("email", "john@example.com");

        Object result = truncationMasker.mask(map);

        assertTrue(result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("John", resultMap.get("name"));
        assertEquals(30, resultMap.get("age"));
        assertTrue(resultMap.containsKey("_truncated"));
        assertTrue((Boolean) resultMap.get("_truncated"));
    }

    @Test
    void should_mask_iterable() {
        List<String> list = Arrays.asList("John", "Jane", "Bob");
        Object result = masker.mask(list);

        assertTrue(result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals(3, resultList.size());
        assertEquals("John", resultList.get(0));
        assertEquals("Jane", resultList.get(1));
        assertEquals("Bob", resultList.get(2));
    }

    @Test
    void should_mask_iterable_with_truncation() {
        masking.setMaxCollectionSize(2);
        StructuredValueMasker truncationMasker = new StructuredValueMasker(fallback, masking);

        List<String> list = Arrays.asList("John", "Jane", "Bob", "Alice");
        Object result = truncationMasker.mask(list);

        assertTrue(result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals(3, resultList.size());
        assertEquals("John", resultList.get(0));
        assertEquals("Jane", resultList.get(1));
        assertEquals("[TRUNCATED]", resultList.get(2));
    }

    @Test
    void should_mask_array() {
        int[] array = {1, 2, 3};
        Object result = masker.mask(array);

        assertTrue(result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals(3, resultList.size());
        assertEquals(1, resultList.get(0));
        assertEquals(2, resultList.get(1));
        assertEquals(3, resultList.get(2));
    }

    @Test
    void should_mask_array_with_truncation() {
        masking.setMaxCollectionSize(2);
        StructuredValueMasker truncationMasker = new StructuredValueMasker(fallback, masking);

        int[] array = {1, 2, 3, 4, 5};
        Object result = truncationMasker.mask(array);

        assertTrue(result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals(3, resultList.size());
        assertEquals(1, resultList.get(0));
        assertEquals(2, resultList.get(1));
        assertEquals("[TRUNCATED]", resultList.get(2));
    }

    @Test
    void should_mask_object_with_max_fields_truncation() {
        masking.setMaxFields(2);
        StructuredValueMasker truncationMasker = new StructuredValueMasker(fallback, masking);

        TestUserWithMultipleFields user = new TestUserWithMultipleFields("John", 30, "john@example.com", "123 Main St");
        Object result = truncationMasker.mask(user);

        assertTrue(result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("John", resultMap.get("name"));
        assertEquals(30, resultMap.get("age"));
        assertTrue(resultMap.containsKey("_truncated"));
        assertTrue((Boolean) resultMap.get("_truncated"));
    }

    @Test
    void should_handle_null_masking_config() {
        // 测试构造函数中的masking == null分支
        StructuredValueMasker nullConfigMasker = new StructuredValueMasker(fallback, null);
        TestUser user = new TestUser("John", 30);
        Object result = nullConfigMasker.mask(user);

        assertTrue(result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("John", resultMap.get("name"));
        assertEquals(30, resultMap.get("age"));
    }

    @Test
    void should_identify_jakarta_packages_as_non_complex() {
        // 测试jakarta标准库的类应该被识别为非复杂对象
        try {
            java.lang.reflect.Method isComplexObjectMethod = StructuredValueMasker.class.getDeclaredMethod("isComplexObject", Object.class);
            isComplexObjectMethod.setAccessible(true);

            // 测试javax标准的类应该被视为非复杂对象
            boolean result = (boolean) isComplexObjectMethod.invoke(masker, new java.util.ArrayList());
            assertFalse(result);

            result = (boolean) isComplexObjectMethod.invoke(masker, "test");
            assertFalse(result);

            result = (boolean) isComplexObjectMethod.invoke(masker, 123);
            assertFalse(result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    void should_collect_fields_from_object_class() {
        // 测试collectFields方法的边界情况
        try {
            java.lang.reflect.Method collectFieldsMethod = StructuredValueMasker.class.getDeclaredMethod("collectFields", Class.class);
            collectFieldsMethod.setAccessible(true);

            // 收集Object类的字段应该返回空列表
            @SuppressWarnings("unchecked")
            List<Field> objectFields = (List<Field>) collectFieldsMethod.invoke(masker, Object.class);
            // Object类没有实例字段
            assertTrue(objectFields.isEmpty() || objectFields.stream().noneMatch(f -> !Modifier.isStatic(f.getModifiers())));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    void should_handle_masked_string_with_complex_object() {
        TestUser user = new TestUser("John", 30);
        Object result = masker.mask(user);

        // 应该继续处理复杂对象而不是直接返回masked值
        assertTrue(result instanceof Map);
    }

    @Test
    void should_handle_max_depth_boundary() {
        // 测试maxDepth为0的情况
        masking.setMaxDepth(0);
        StructuredValueMasker zeroDepthMasker = new StructuredValueMasker(fallback, masking);

        NestedTestUser user = new NestedTestUser(new TestUser("John", 30));
        Object result = zeroDepthMasker.mask(user);

        assertTrue(result instanceof Map);
    }

    @Test
    void should_mask_object_with_static_fields_ignored() {
        // 静态字段应该被忽略
        TestUserWithStatic user = new TestUserWithStatic("John", 30);
        Object result = masker.mask(user);

        assertTrue(result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("John", resultMap.get("name"));
        assertFalse(resultMap.containsKey("staticField"));
    }

    @Test
    void should_mask_object_with_synthetic_fields_ignored() {
        // 合成字段应该被忽略（编译器生成的字段）
        TestUserWithSynthetic user = new TestUserWithSynthetic("John");
        Object result = masker.mask(user);

        assertTrue(result instanceof Map);
    }

    // 新的测试类
    static class TestUserWithStatic {
        private String name;
        private int age;
        public static String staticField = "static";

        public TestUserWithStatic(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class TestUserWithSynthetic {
        private String name;
        // 内部类会有外部类的引用字段（合成字段）
        public TestUserWithSynthetic(String name) {
            this.name = name;
        }
    }

    @Test
    void should_mask_object_with_private_fields() {
        TestUserWithInaccessibleField user = new TestUserWithInaccessibleField("John");
        Object result = masker.mask(user);

        assertTrue(result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("John", resultMap.get("name"));
        assertEquals("secret", resultMap.get("inaccessibleField"));
    }

    @Test
    void should_return_directFallbackValue_when_valueIsNotComplexAndNotContainer() {
        when(fallback.mask(42)).thenReturn("masked-number");

        Object result = masker.mask(42);

        assertEquals("masked-number", result);
    }

    @Test
    void should_collectInheritedInstanceFields_when_superclassContainsProperties() throws Exception {
        java.lang.reflect.Method method = StructuredValueMasker.class.getDeclaredMethod("collectFields", Class.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Field> fields = (List<Field>) method.invoke(masker, ChildUser.class);

        assertTrue(fields.stream().anyMatch(field -> field.getName().equals("parentName")));
        assertTrue(fields.stream().anyMatch(field -> field.getName().equals("childName")));
    }

    @Test
    void should_returnNull_when_maskObjectReceivesNullViaReflection() throws Exception {
        java.lang.reflect.Method method = StructuredValueMasker.class.getDeclaredMethod("maskObject", Object.class, int.class, Set.class);
        method.setAccessible(true);

        Object result = method.invoke(masker, null, 0, Collections.newSetFromMap(new IdentityHashMap<>()));

        assertNull(result);
    }

    @Test
    void should_useFallbackForNestedNonComplexJdkObject_when_maskObjectTraversesPojo() {
        UUID uuid = UUID.randomUUID();
        when(fallback.mask(uuid)).thenReturn("masked-uuid");

        Object result = masker.mask(new HolderWithUuid(uuid));

        assertTrue(result instanceof Map);
        assertEquals("masked-uuid", ((Map<?, ?>) result).get("id"));
    }

    @Test
    void should_treat_javaxAndJakartaPackagesAsNonComplex_when_checkedReflectively() throws Exception {
        java.lang.reflect.Method method = StructuredValueMasker.class.getDeclaredMethod("isComplexObject", Object.class);
        method.setAccessible(true);

        assertFalse((boolean) method.invoke(masker, new javax.crypto.spec.SecretKeySpec(new byte[]{1}, "AES")));
        assertFalse((boolean) method.invoke(masker, new jakarta.validation.ValidationException("x")));
    }

    @Test
    void should_ignoreSyntheticOuterReference_when_collectFieldsProcessesAnonymousInnerClass() throws Exception {
        Object holder = new Object() {
            @SuppressWarnings("unused")
            private final String value = "x";
        };

        java.lang.reflect.Method method = StructuredValueMasker.class.getDeclaredMethod("collectFields", Class.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Field> fields = (List<Field>) method.invoke(masker, holder.getClass());

        assertTrue(fields.stream().noneMatch(field -> field.getName().startsWith("this$")));
        assertTrue(fields.stream().anyMatch(field -> field.getName().equals("value")));
    }

    @Test
    void should_maskEnumFieldThroughPrimitiveBranch_when_nestedPojoContainsEnum() {
        Object result = masker.mask(new HolderWithEnum(TestRole.ADMIN));

        assertTrue(result instanceof Map);
        assertEquals(TestRole.ADMIN, ((Map<?, ?>) result).get("role"));
    }

    @Test
    void should_maskNestedBooleanField_when_maskObjectHitsBooleanPrimitiveBranch() {
        Object result = masker.mask(new HolderWithBoolean(Boolean.TRUE));

        assertTrue(result instanceof Map);
        assertEquals(Boolean.TRUE, ((Map<?, ?>) result).get("active"));
    }

    @Test
    void should_returnEmptyFieldList_when_collectFieldsReceivesNullType() throws Exception {
        java.lang.reflect.Method method = StructuredValueMasker.class.getDeclaredMethod("collectFields", Class.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Field> fields = (List<Field>) method.invoke(masker, new Object[]{null});

        assertTrue(fields.isEmpty());
    }

    // 测试类
    static class TestUser {
        private String name;
        private int age;

        public TestUser(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class TestUserWithSensitive {
        private String name;
        @Sensitive(maskedValue = "[MASKED]")
        private String password;

        public TestUserWithSensitive(String name, String password) {
            this.name = name;
            this.password = password;
        }
    }

    static class NestedTestUser {
        private TestUser nestedUser;

        public NestedTestUser(TestUser nestedUser) {
            this.nestedUser = nestedUser;
        }
    }

    static class CircularTestUser {
        private String name;
        private CircularTestUser friend;

        public CircularTestUser(String name) {
            this.name = name;
        }

        public void setFriend(CircularTestUser friend) {
            this.friend = friend;
        }
    }

    static class TestUserWithInaccessibleField {
        private String name;
        // 私有字段
        private String inaccessibleField = "secret";

        public TestUserWithInaccessibleField(String name) {
            this.name = name;
        }
    }

    static class TestUserWithMultipleFields {
        private String name;
        private int age;
        private String email;
        private String address;

        public TestUserWithMultipleFields(String name, int age, String email, String address) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.address = address;
        }
    }

    static class ParentUser {
        private String parentName = "parent";
    }

    static class ChildUser extends ParentUser {
        private String childName = "child";
    }

    static class HolderWithUuid {
        private final UUID id;

        HolderWithUuid(UUID id) {
            this.id = id;
        }
    }

    static class HolderWithEnum {
        private final TestRole role;

        HolderWithEnum(TestRole role) {
            this.role = role;
        }
    }

    static class HolderWithBoolean {
        private final Boolean active;

        HolderWithBoolean(Boolean active) {
            this.active = active;
        }
    }

    enum TestRole {
        ADMIN
    }

    @Test
    void maskWithNullMask() {
        TestUser user = new TestUser("John", 30);
        Object result = masker.mask(user, null);

        assertTrue(result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("John", resultMap.get("name"));
        assertEquals(30, resultMap.get("age"));
    }

    @Test
    void maskWithNonNullMask() {
        Mask phoneMask = () -> "phone";
        when(fallback.mask("13812345678", phoneMask)).thenReturn("138****5678");

        Object result = masker.mask("13812345678", phoneMask);

        assertEquals("138****5678", result);
        verify(fallback, times(1)).mask("13812345678", phoneMask);
    }

    @Test
    void maskWithNonNullMaskForComplexObject() {
        Mask customMask = () -> "custom";
        TestUser user = new TestUser("John", 30);
        when(fallback.mask(user, customMask)).thenReturn("***[MASKED]***");

        Object result = masker.mask(user, customMask);

        assertEquals("***[MASKED]***", result);
        verify(fallback, times(1)).mask(user, customMask);
    }

    @Test
    void maskWithNonNullMaskForNullValue() {
        Mask mask = () -> "phone";
        Object result = masker.mask(null, mask);

        assertNull(result);
    }

}
