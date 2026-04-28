package com.chao.failure.internal.core.i18n;

import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.spi.i18n.LocalizedResponseResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocaleRouterTest {

    @AfterEach
    void tearDown() {
        LocaleRouter.setDefault(null);
    }

    @Test
    void testResolveMessageWithDefaultResolver() {
        String result = LocaleRouter.resolveMessage(ResponseCode.VALIDATION_ERROR, Locale.CHINA);
        assertNull(result);
    }

    @Test
    void testResolveDetailWithDefaultResolver() {
        String result = LocaleRouter.resolveDetail(ResponseCode.VALIDATION_ERROR, "detail", Locale.CHINA);
        assertNull(result);
    }

    @Test
    void testSetDefaultWithNonNullResolver() {
        LocalizedResponseResolver customResolver = mock(LocalizedResponseResolver.class);
        when(customResolver.resolveMessage(any(), any())).thenReturn("custom_message");
        when(customResolver.resolveDetail(any(), any(), any())).thenReturn("custom_detail");

        LocaleRouter.setDefault(customResolver);

        String message = LocaleRouter.resolveMessage(ResponseCode.VALIDATION_ERROR, Locale.CHINA);
        assertEquals("custom_message", message);

        String detail = LocaleRouter.resolveDetail(ResponseCode.VALIDATION_ERROR, "detail", Locale.CHINA);
        assertEquals("custom_detail", detail);
    }

    @Test
    void testSetDefaultWithNullResolver() {
        LocalizedResponseResolver customResolver = mock(LocalizedResponseResolver.class);
        when(customResolver.resolveMessage(any(), any())).thenReturn("custom_message");

        LocaleRouter.setDefault(customResolver);
        LocaleRouter.setDefault(null);

        String result = LocaleRouter.resolveMessage(ResponseCode.VALIDATION_ERROR, Locale.CHINA);
        assertNull(result);
    }

    @Test
    void testResolveMessageWithCustomResolverReturningNull() {
        LocalizedResponseResolver customResolver = mock(LocalizedResponseResolver.class);
        when(customResolver.resolveMessage(any(), any())).thenReturn(null);

        LocaleRouter.setDefault(customResolver);

        String result = LocaleRouter.resolveMessage(ResponseCode.VALIDATION_ERROR, Locale.CHINA);
        assertNull(result);
    }

    @Test
    void testResolveDetailWithCustomResolverReturningNull() {
        LocalizedResponseResolver customResolver = mock(LocalizedResponseResolver.class);
        when(customResolver.resolveDetail(any(), any(), any())).thenReturn(null);

        LocaleRouter.setDefault(customResolver);

        String result = LocaleRouter.resolveDetail(ResponseCode.VALIDATION_ERROR, "detail", Locale.CHINA);
        assertNull(result);
    }

    @Test
    void testResolveMessageWithDifferentLocales() {
        LocalizedResponseResolver customResolver = mock(LocalizedResponseResolver.class);
        when(customResolver.resolveMessage(eq(ResponseCode.VALIDATION_ERROR), eq(Locale.CHINA))).thenReturn("中文消息");
        when(customResolver.resolveMessage(eq(ResponseCode.VALIDATION_ERROR), eq(Locale.US))).thenReturn("English message");

        LocaleRouter.setDefault(customResolver);

        assertEquals("中文消息", LocaleRouter.resolveMessage(ResponseCode.VALIDATION_ERROR, Locale.CHINA));
        assertEquals("English message", LocaleRouter.resolveMessage(ResponseCode.VALIDATION_ERROR, Locale.US));
    }

    @Test
    void testResolveDetailWithDifferentLocales() {
        LocalizedResponseResolver customResolver = mock(LocalizedResponseResolver.class);
        when(customResolver.resolveDetail(eq(ResponseCode.VALIDATION_ERROR), eq("detail"), eq(Locale.CHINA))).thenReturn("中文详情");
        when(customResolver.resolveDetail(eq(ResponseCode.VALIDATION_ERROR), eq("detail"), eq(Locale.US))).thenReturn("English detail");

        LocaleRouter.setDefault(customResolver);

        assertEquals("中文详情", LocaleRouter.resolveDetail(ResponseCode.VALIDATION_ERROR, "detail", Locale.CHINA));
        assertEquals("English detail", LocaleRouter.resolveDetail(ResponseCode.VALIDATION_ERROR, "detail", Locale.US));
    }

    @Test
    void testSetDefaultMultipleTimes() {
        LocalizedResponseResolver resolver1 = mock(LocalizedResponseResolver.class);
        when(resolver1.resolveMessage(any(), any())).thenReturn("resolver1_message");

        LocalizedResponseResolver resolver2 = mock(LocalizedResponseResolver.class);
        when(resolver2.resolveMessage(any(), any())).thenReturn("resolver2_message");

        LocaleRouter.setDefault(resolver1);
        assertEquals("resolver1_message", LocaleRouter.resolveMessage(ResponseCode.VALIDATION_ERROR, Locale.CHINA));

        LocaleRouter.setDefault(resolver2);
        assertEquals("resolver2_message", LocaleRouter.resolveMessage(ResponseCode.VALIDATION_ERROR, Locale.CHINA));
    }

    @Test
    void testResolveMessageWithDifferentResponseCodes() {
        LocalizedResponseResolver customResolver = mock(LocalizedResponseResolver.class);
        when(customResolver.resolveMessage(eq(ResponseCode.VALIDATION_ERROR), any())).thenReturn("validation_error");
        when(customResolver.resolveMessage(eq(ResponseCode.INTERRUPTED_ERROR), any())).thenReturn("interrupted_error");

        LocaleRouter.setDefault(customResolver);

        assertEquals("validation_error", LocaleRouter.resolveMessage(ResponseCode.VALIDATION_ERROR, Locale.CHINA));
        assertEquals("interrupted_error", LocaleRouter.resolveMessage(ResponseCode.INTERRUPTED_ERROR, Locale.CHINA));
    }

    @Test
    void testGetDefaultWithNullResolverViaReflection() throws Exception {
        Field resolverField = LocaleRouter.class.getDeclaredField("resolver");
        resolverField.setAccessible(true);
        resolverField.set(null, null);

        try {
            String result = LocaleRouter.resolveMessage(ResponseCode.VALIDATION_ERROR, Locale.CHINA);
            assertNull(result);
        } finally {
            resolverField.set(null, new LocalizedResponseResolver() {});
        }
    }
}
