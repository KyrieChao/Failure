package com.chao.failfast.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class I18nTest {

    private StaticMessageSource messageSource;

    private I18n i18n;

    @BeforeEach
    void setUp() {
        messageSource = new StaticMessageSource();
        i18n = new I18n(messageSource);
        i18n.init();
        LocaleContextHolder.setLocale(Locale.US);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clear static instance via reflection
        java.lang.reflect.Field instanceField = I18n.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void testGet_NullCode() {
        assertNull(I18n.get(null));
    }

    @Test
    void testGet_EmptyCode() {
        assertEquals("", I18n.get(""));
    }

    @Test
    void testGet_SimpleCode() {
        String code = "test.code";
        String expectedMessage = "Test Message";
        messageSource.addMessage(code, Locale.US, expectedMessage);

        String result = I18n.get(code);
        assertEquals(expectedMessage, result);
    }

    @Test
    void testGet_WrappedCode() {
        String code = "{test.code}";
        String key = "test.code";
        String expectedMessage = "Test Message";
        messageSource.addMessage(key, Locale.US, expectedMessage);

        String result = I18n.get(code);
        assertEquals(expectedMessage, result);
    }

    @Test
    void testGet_StartsWithBraceOnly() {
        String code = "{test.code";
        messageSource.addMessage(code, Locale.US, "Test Message");
        
        String result = I18n.get(code);
        assertEquals("Test Message", result);
    }

    @Test
    void testGet_EndsWithBraceOnly() {
        String code = "test.code}";
        messageSource.addMessage(code, Locale.US, "Test Message");

        String result = I18n.get(code);
        assertEquals("Test Message", result);
    }

    @Test
    void testGet_WithArgs() {
        String code = "test.code";
        Object[] args = new Object[]{"arg1", 123};
        String expectedMessage = "Test Message with arg1 and 123";
        // StaticMessageSource supports MessageFormat
        messageSource.addMessage(code, Locale.US, "Test Message with {0} and {1}");

        String result = I18n.get(code, args);
        assertEquals(expectedMessage, result);
    }

    @Test
    void testGet_Exception() {
        // StaticMessageSource doesn't easily throw exception, but we can rely on missing code behavior
        // If code is missing, it returns the code itself if useCodeAsDefaultMessage is true (default false for StaticMessageSource?)
        // Wait, I18n catches Exception and returns code.
        // Let's pass null message source to I18n?
        // Or mock it ONLY for this test?
        // Or force an exception by passing bad args?
        
        // Let's try to mock for this specific test, or just skip it if we trust I18n.
        // Actually, we can use a subclass of StaticMessageSource that throws exception.
        
        I18n i18nError = new I18n(new StaticMessageSource() {
            @Override
            protected String getMessageInternal(String code, Object[] args, Locale locale) {
                throw new RuntimeException("Message source error");
            }
        });
        i18nError.init();
        
        String code = "test.code";
        String result = I18n.get(code);
        assertEquals(code, result);
        
        // Restore normal i18n
        i18n.init();
    }
    
    @Test
    void testGet_InstanceNull() {
        // 通过反射将 instance 设置为 null 来模拟未初始化的情况
        try {
            java.lang.reflect.Field instanceField = I18n.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String code = "test.code";
        assertEquals(code, I18n.get(code));
        
        // 恢复 instance
        i18n.init();
    }

    @Test
    void testGetLocale() {
        Locale expectedLocale = Locale.UK;
        LocaleContextHolder.setLocale(expectedLocale);
        
        assertEquals(expectedLocale, I18n.getLocale());
        
        LocaleContextHolder.resetLocaleContext();
    }
}
