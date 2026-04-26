package com.chao.failfast.config.masking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultValueMaskerTest {

    private final DefaultValueMasker masker = new DefaultValueMasker();

    @Test
    void should_return_null_when_value_is_null() {
        assertNull(masker.mask(null, "anyPath"));
    }

    @Test
    void should_return_masked_value_when_field_path_contains_sensitive_pattern() {
        String result = (String) masker.mask("sensitiveValue", "user.password");
        assertEquals("***[MASKED]***", result);

        result = (String) masker.mask("tokenValue", "auth.token");
        assertEquals("***[MASKED]***", result);

        result = (String) masker.mask("secretValue", "config.secret");
        assertEquals("***[MASKED]***", result);
    }

    @Test
    void should_return_original_value_when_field_path_is_not_sensitive() {
        Object result = masker.mask("normalValue", "user.name");
        assertEquals("normalValue", result);
    }

    @Test
    void should_return_false_when_field_path_is_null() {
        // 测试isSensitive方法的fieldPath == null分支
        // 通过反射调用私有方法
        try {
            java.lang.reflect.Method isSensitiveMethod = DefaultValueMasker.class.getDeclaredMethod("isSensitive", String.class);
            isSensitiveMethod.setAccessible(true);
            boolean result = (boolean) isSensitiveMethod.invoke(masker, (Object) null);
            assertFalse(result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    void should_return_false_when_field_path_is_blank() {
        // 测试isSensitive方法的fieldPath.isBlank()分支
        // 通过反射调用私有方法
        try {
            java.lang.reflect.Method isSensitiveMethod = DefaultValueMasker.class.getDeclaredMethod("isSensitive", String.class);
            isSensitiveMethod.setAccessible(true);
            boolean result = (boolean) isSensitiveMethod.invoke(masker, "");
            assertFalse(result);

            result = (boolean) isSensitiveMethod.invoke(masker, "   ");
            assertFalse(result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    void should_mask_mobile_number() {
        String result = (String) masker.mask("13812345678", "user.phone");
        assertEquals("138****5678", result);
    }

    @Test
    void should_mask_bank_card() {
        // 使用不包含敏感模式的字段路径
        String result = (String) masker.mask("6222021234567890123", "user.cardNumber");
        assertEquals("6222****0123", result);
    }

    @Test
    void should_mask_email() {
        String result = (String) masker.mask("test@example.com", "user.email");
        assertEquals("t****@example.com", result);
    }

    @Test
    void should_truncate_long_string() {
        String longString = "a".repeat(60);
        String result = (String) masker.mask(longString, "user.description");
        assertTrue(result.contains("...(60char)..."));
    }

    @Test
    void should_return_empty_string_when_value_is_empty() {
        String result = (String) masker.mask("", "user.name");
        assertEquals("", result);
    }

    @Test
    void should_return_original_string_when_length_is_less_than_50() {
        String normalString = "This is a normal string";
        String result = (String) masker.mask(normalString, "user.name");
        assertEquals(normalString, result);
    }

}
