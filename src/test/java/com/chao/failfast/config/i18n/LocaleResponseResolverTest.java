package com.chao.failfast.config.i18n;

import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.spi.i18n.LocalizedResponseResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LocaleResponseResolverTest {

    private static final ResponseCode CODE_1001 = ResponseCode.of(1001, "error.1001");
    private static final ResponseCode CODE_1002 = ResponseCode.of(1002, "error.1002");
    private static final ResponseCode CODE_1003 = ResponseCode.of(1003, "error.1003");

    @Nested
    @DisplayName("putMessage")
    class PutMessageTests {

        @Test
        @DisplayName("should return this when locale is null")
        void shouldReturnThisWhenLocaleIsNull() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            LocaleResponseResolver result = resolver.putMessage(null, 1001, "message");
            assertSame(resolver, result);
        }

        @Test
        @DisplayName("should return this when message is null")
        void shouldReturnThisWhenMessageIsNull() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            LocaleResponseResolver result = resolver.putMessage(Locale.CHINA, 1001, null);
            assertSame(resolver, result);
        }

        @Test
        @DisplayName("should return this when message is blank")
        void shouldReturnThisWhenMessageIsBlank() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            LocaleResponseResolver result = resolver.putMessage(Locale.CHINA, 1001, "   ");
            assertSame(resolver, result);
        }

        @Test
        @DisplayName("should store message for valid input")
        void shouldStoreMessageForValidInput() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINA, 1001, "错误消息");

            String message = resolver.resolveMessage(CODE_1001, Locale.CHINA);
            assertEquals("错误消息", message);
        }

        @Test
        @DisplayName("should support fluent chaining")
        void shouldSupportFluentChaining() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINA, 1001, "msg1")
                    .putMessage(Locale.US, 1002, "msg2")
                    .putMessage(Locale.JAPAN, 1003, "msg3");

            assertEquals("msg1", resolver.resolveMessage(CODE_1001, Locale.CHINA));
            assertEquals("msg2", resolver.resolveMessage(CODE_1002, Locale.US));
            assertEquals("msg3", resolver.resolveMessage(CODE_1003, Locale.JAPAN));
        }
    }

    @Nested
    @DisplayName("putDetail")
    class PutDetailTests {

        @Test
        @DisplayName("should return this when locale is null")
        void shouldReturnThisWhenLocaleIsNull() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            LocaleResponseResolver result = resolver.putDetail(null, 1001, "detail");
            assertSame(resolver, result);
        }

        @Test
        @DisplayName("should return this when detail is null")
        void shouldReturnThisWhenDetailIsNull() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            LocaleResponseResolver result = resolver.putDetail(Locale.CHINA, 1001, null);
            assertSame(resolver, result);
        }

        @Test
        @DisplayName("should return this when detail is blank")
        void shouldReturnThisWhenDetailIsBlank() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            LocaleResponseResolver result = resolver.putDetail(Locale.CHINA, 1001, "   ");
            assertSame(resolver, result);
        }

        @Test
        @DisplayName("should store detail for valid input")
        void shouldStoreDetailForValidInput() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putDetail(Locale.CHINA, 1001, "错误详情");

            String detail = resolver.resolveDetail(CODE_1001, "default", Locale.CHINA);
            assertEquals("错误详情", detail);
        }

        @Test
        @DisplayName("should support fluent chaining")
        void shouldSupportFluentChaining() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putDetail(Locale.CHINA, 1001, "detail1")
                    .putDetail(Locale.US, 1002, "detail2")
                    .putDetail(Locale.JAPAN, 1003, "detail3");

            assertEquals("detail1", resolver.resolveDetail(CODE_1001, "default", Locale.CHINA));
            assertEquals("detail2", resolver.resolveDetail(CODE_1002, "default", Locale.US));
            assertEquals("detail3", resolver.resolveDetail(CODE_1003, "default", Locale.JAPAN));
        }
    }

    @Nested
    @DisplayName("resolveMessage")
    class ResolveMessageTests {

        @Test
        @DisplayName("should return null when code is null")
        void shouldReturnNullWhenCodeIsNull() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINA, 1001, "message");
            assertNull(resolver.resolveMessage(null, Locale.CHINA));
        }

        @Test
        @DisplayName("should return null when locale is null")
        void shouldReturnNullWhenLocaleIsNull() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINA, 1001, "message");
            assertNull(resolver.resolveMessage(CODE_1001, null));
        }

        @Test
        @DisplayName("should return null when both code and locale are null")
        void shouldReturnNullWhenBothCodeAndLocaleAreNull() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            assertNull(resolver.resolveMessage(null, null));
        }

        @Test
        @DisplayName("should return exact match when found")
        void shouldReturnExactMatchWhenFound() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINA, 1001, "精确消息");

            assertEquals("精确消息", resolver.resolveMessage(CODE_1001, Locale.CHINA));
        }

        @Test
        @DisplayName("should return language match when exact match not found")
        void shouldReturnLanguageMatchWhenExactMatchNotFound() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINESE, 1001, "中文消息");

            assertEquals("中文消息", resolver.resolveMessage(CODE_1001, Locale.CHINA));
        }

        @Test
        @DisplayName("should return null when no match found")
        void shouldReturnNullWhenNoMatchFound() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINA, 1001, "message");

            assertNull(resolver.resolveMessage(CODE_1002, Locale.CHINA));
        }

        @Test
        @DisplayName("should return exact match over language match")
        void shouldReturnExactMatchOverLanguageMatch() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINESE, 1001, "语言匹配");
            resolver.putMessage(Locale.CHINA, 1001, "精确匹配");

            assertEquals("精确匹配", resolver.resolveMessage(CODE_1001, Locale.CHINA));
        }

        @Test
        @DisplayName("should return language match when exact is blank")
        void shouldReturnLanguageMatchWhenExactIsBlank() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINESE, 1001, "语言消息");
            resolver.putMessage(Locale.CHINA, 1001, "   ");

            assertEquals("语言消息", resolver.resolveMessage(CODE_1001, Locale.CHINA));
        }

        @Test
        @DisplayName("should return null when both exact and language match are blank")
        void shouldReturnNullWhenBothExactAndLanguageMatchAreBlank() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINESE, 1001, "   ");
            resolver.putMessage(Locale.CHINA, 1001, "   ");

            assertNull(resolver.resolveMessage(CODE_1001, Locale.CHINA));
        }
    }

    @Nested
    @DisplayName("resolveDetail")
    class ResolveDetailTests {

        @Test
        @DisplayName("should return null when code is null")
        void shouldReturnNullWhenCodeIsNull() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putDetail(Locale.CHINA, 1001, "detail");
            assertNull(resolver.resolveDetail(null, "default", Locale.CHINA));
        }

        @Test
        @DisplayName("should return null when locale is null")
        void shouldReturnNullWhenLocaleIsNull() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putDetail(Locale.CHINA, 1001, "detail");
            assertNull(resolver.resolveDetail(CODE_1001, "default", null));
        }

        @Test
        @DisplayName("should return null when both code and locale are null")
        void shouldReturnNullWhenBothCodeAndLocaleAreNull() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            assertNull(resolver.resolveDetail(null, "default", null));
        }

        @Test
        @DisplayName("should return exact match when found")
        void shouldReturnExactMatchWhenFound() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putDetail(Locale.CHINA, 1001, "精确详情");

            assertEquals("精确详情", resolver.resolveDetail(CODE_1001, "default", Locale.CHINA));
        }

        @Test
        @DisplayName("should return language match when exact match not found")
        void shouldReturnLanguageMatchWhenExactMatchNotFound() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putDetail(Locale.CHINESE, 1001, "中文详情");

            assertEquals("中文详情", resolver.resolveDetail(CODE_1001, "default", Locale.CHINA));
        }

        @Test
        @DisplayName("should return null when no match found")
        void shouldReturnNullWhenNoMatchFound() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putDetail(Locale.CHINA, 1001, "detail");

            assertNull(resolver.resolveDetail(CODE_1002, "default", Locale.CHINA));
        }

        @Test
        @DisplayName("should return exact match over language match")
        void shouldReturnExactMatchOverLanguageMatch() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putDetail(Locale.CHINESE, 1001, "语言匹配");
            resolver.putDetail(Locale.CHINA, 1001, "精确匹配");

            assertEquals("精确匹配", resolver.resolveDetail(CODE_1001, "default", Locale.CHINA));
        }

        @Test
        @DisplayName("should return language match when exact is blank")
        void shouldReturnLanguageMatchWhenExactIsBlank() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putDetail(Locale.CHINESE, 1001, "语言详情");
            resolver.putDetail(Locale.CHINA, 1001, "   ");

            assertEquals("语言详情", resolver.resolveDetail(CODE_1001, "default", Locale.CHINA));
        }

        @Test
        @DisplayName("should return null when both exact and language match are blank")
        void shouldReturnNullWhenBothExactAndLanguageMatchAreBlank() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putDetail(Locale.CHINESE, 1001, "   ");
            resolver.putDetail(Locale.CHINA, 1001, "   ");

            assertNull(resolver.resolveDetail(CODE_1001, "default", Locale.CHINA));
        }
    }

    @Nested
    @DisplayName("localeKey via reflection")
    class LocaleKeyTests {

        @Test
        @DisplayName("should return empty string when locale is null")
        void shouldReturnEmptyStringWhenLocaleIsNull() throws Exception {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            String result = invokeLocaleKey(resolver, null);
            assertEquals("", result);
        }

        @Test
        @DisplayName("should return language when country is null")
        void shouldReturnLanguageWhenCountryIsNull() throws Exception {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            Locale locale = new Locale("zh");
            String result = invokeLocaleKey(resolver, locale);
            assertEquals("zh", result);
        }

        @Test
        @DisplayName("should return language when country is blank")
        void shouldReturnLanguageWhenCountryIsBlank() throws Exception {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            Locale locale = new Locale("zh", "");
            String result = invokeLocaleKey(resolver, locale);
            assertEquals("zh", result);
        }

        @Test
        @DisplayName("should return empty string when language and country are blank")
        void shouldReturnEmptyStringWhenLanguageAndCountryAreBlank() throws Exception {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            Locale locale = new Locale("", "");
            String result = invokeLocaleKey(resolver, locale);
            assertEquals("", result);
        }

        @Test
        @DisplayName("should return language_country when both are present")
        void shouldReturnLanguageCountryWhenBothPresent() throws Exception {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            String result = invokeLocaleKey(resolver, Locale.CHINA);
            assertEquals("zh_CN", result);
        }

        @Test
        @DisplayName("should prefix underscore when country exists but language is blank")
        void shouldPrefixUnderscoreWhenCountryExistsButLanguageIsBlank() throws Exception {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            Locale locale = new Locale("", "CN");
            String result = invokeLocaleKey(resolver, locale);
            assertEquals("_CN", result);
        }

        @Test
        @DisplayName("should return language_country for US locale")
        void shouldReturnLanguageCountryForUSLocale() throws Exception {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            String result = invokeLocaleKey(resolver, Locale.US);
            assertEquals("en_US", result);
        }

        @Test
        @DisplayName("should return language_country for UK locale")
        void shouldReturnLanguageCountryForUKLocale() throws Exception {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            String result = invokeLocaleKey(resolver, Locale.UK);
            assertEquals("en_GB", result);
        }

        @Test
        @DisplayName("should handle locale with only language")
        void shouldHandleLocaleWithOnlyLanguage() throws Exception {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            String result = invokeLocaleKey(resolver, Locale.ENGLISH);
            assertEquals("en", result);
        }

        @Test
        @DisplayName("should handle locale with variant")
        void shouldHandleLocaleWithVariant() throws Exception {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            Locale locale = new Locale("no", "NO", "NY");
            String result = invokeLocaleKey(resolver, locale);
            assertEquals("no_NO", result);
        }

        private String invokeLocaleKey(LocaleResponseResolver resolver, Locale locale) {
            try {
                java.lang.reflect.Method method = LocaleResponseResolver.class.getDeclaredMethod("localeKey", Locale.class);
                method.setAccessible(true);
                return (String) method.invoke(resolver, locale);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    @DisplayName("Integration tests")
    class IntegrationTests {

        @Test
        @DisplayName("should handle multiple locales with same code")
        void shouldHandleMultipleLocalesWithSameCode() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINA, 1001, "中文消息")
                    .putMessage(Locale.US, 1001, "English message")
                    .putMessage(Locale.JAPAN, 1001, "日本語メッセージ");

            assertEquals("中文消息", resolver.resolveMessage(CODE_1001, Locale.CHINA));
            assertEquals("English message", resolver.resolveMessage(CODE_1001, Locale.US));
            assertEquals("日本語メッセージ", resolver.resolveMessage(CODE_1001, Locale.JAPAN));
        }

        @Test
        @DisplayName("should handle same locale with multiple codes")
        void shouldHandleSameLocaleWithMultipleCodes() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINA, 1001, "消息1")
                    .putMessage(Locale.CHINA, 1002, "消息2")
                    .putMessage(Locale.CHINA, 1003, "消息3");

            assertEquals("消息1", resolver.resolveMessage(CODE_1001, Locale.CHINA));
            assertEquals("消息2", resolver.resolveMessage(CODE_1002, Locale.CHINA));
            assertEquals("消息3", resolver.resolveMessage(CODE_1003, Locale.CHINA));
        }

        @Test
        @DisplayName("should handle message and detail separately")
        void shouldHandleMessageAndDetailSeparately() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINA, 1001, "错误消息")
                    .putDetail(Locale.CHINA, 1001, "错误详情");

            assertEquals("错误消息", resolver.resolveMessage(CODE_1001, Locale.CHINA));
            assertEquals("错误详情", resolver.resolveDetail(CODE_1001, "default", Locale.CHINA));
        }

        @Test
        @DisplayName("should handle overwriting existing entries")
        void shouldHandleOverwritingExistingEntries() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.CHINA, 1001, "旧消息");
            resolver.putMessage(Locale.CHINA, 1001, "新消息");

            assertEquals("新消息", resolver.resolveMessage(CODE_1001, Locale.CHINA));
        }

        @Test
        @DisplayName("should implement LocalizedResponseResolver interface")
        void shouldImplementLocalizedResponseResolverInterface() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            assertTrue(resolver instanceof LocalizedResponseResolver);
        }

        @Test
        @DisplayName("should use fallback when no exact or language match")
        void shouldUseFallbackWhenNoExactOrLanguageMatch() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putMessage(Locale.FRANCE, 1001, "消息");

            assertNull(resolver.resolveMessage(CODE_1001, Locale.CHINA));
        }

        @Test
        @DisplayName("should handle detail fallback when no match")
        void shouldHandleDetailFallbackWhenNoMatch() {
            LocaleResponseResolver resolver = new LocaleResponseResolver();
            resolver.putDetail(Locale.FRANCE, 1001, "详情");

            assertNull(resolver.resolveDetail(CODE_1001, "default", Locale.CHINA));
        }
    }
}
