package com.chao.failure.internal.core.i18n;

import com.chao.failure.constant.FailureConst;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.spi.i18n.LocalizedResponseResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalizedTextsTest {

    private static Method getLocaleMethod;
    private static Method getMessageMethod;

    @BeforeEach
    void setUp() throws Exception {
        getLocaleMethod = com.chao.failure.util.I18n.class.getDeclaredMethod("getLocale");
        getLocaleMethod.setAccessible(true);

        getMessageMethod = com.chao.failure.util.I18n.class.getDeclaredMethod("get", String.class);
        getMessageMethod.setAccessible(true);
    }

    @AfterEach
    void tearDown() {
        LocaleRouter.setDefault(null);
    }

    @Test
    void testMessageWithNullCode() throws Exception {
        String result = LocalizedTexts.message(null);
        assertTrue(FailureConst.UNKNOWN_ERROR.equals(result) || com.chao.failure.util.I18n.get(FailureConst.UNKNOWN_ERROR).equals(result));
    }

    @Test
    void testMessageWithLocalizedMessageAvailable() throws Exception {
        LocalizedResponseResolver customResolver = mock(LocalizedResponseResolver.class);
        when(customResolver.resolveMessage(any(), any())).thenReturn("localized_message");
        LocaleRouter.setDefault(customResolver);

        ResponseCode code = ResponseCode.VALIDATION_ERROR;

        String result = LocalizedTexts.message(code);
        assertEquals("localized_message", result);
    }

    @Test
    void testMessageWithLocalizedMessageBlank() throws Exception {
        LocalizedResponseResolver customResolver = mock(LocalizedResponseResolver.class);
        when(customResolver.resolveMessage(any(), any())).thenReturn("   ");
        LocaleRouter.setDefault(customResolver);

        ResponseCode code = ResponseCode.VALIDATION_ERROR;

        String result = LocalizedTexts.message(code);
        assertNotNull(result);
    }

    @Test
    void testMessageWithLocalizedMessageNull() throws Exception {
        LocalizedResponseResolver customResolver = mock(LocalizedResponseResolver.class);
        when(customResolver.resolveMessage(any(), any())).thenReturn(null);
        LocaleRouter.setDefault(customResolver);

        ResponseCode code = ResponseCode.VALIDATION_ERROR;

        String result = LocalizedTexts.message(code);
        assertNotNull(result);
    }

    @Test
    void testDetailWithLocalizedDetailAvailable() throws Exception {
        LocalizedResponseResolver customResolver = mock(LocalizedResponseResolver.class);
        when(customResolver.resolveDetail(any(), any(), any())).thenReturn("localized_detail");
        LocaleRouter.setDefault(customResolver);

        ResponseCode code = ResponseCode.VALIDATION_ERROR;

        String result = LocalizedTexts.detail(code, "original_detail");
        assertEquals("localized_detail", result);
    }

    @Test
    void testDetailWithLocalizedDetailBlank() throws Exception {
        LocalizedResponseResolver customResolver = mock(LocalizedResponseResolver.class);
        when(customResolver.resolveDetail(any(), any(), any())).thenReturn("   ");
        LocaleRouter.setDefault(customResolver);

        ResponseCode code = ResponseCode.VALIDATION_ERROR;

        String result = LocalizedTexts.detail(code, "original_detail");
        assertNotNull(result);
    }

    @Test
    void testDetailWithLocalizedDetailNull() throws Exception {
        LocalizedResponseResolver customResolver = mock(LocalizedResponseResolver.class);
        when(customResolver.resolveDetail(any(), any(), any())).thenReturn(null);
        LocaleRouter.setDefault(customResolver);

        ResponseCode code = ResponseCode.VALIDATION_ERROR;

        String result = LocalizedTexts.detail(code, "original_detail");
        assertNotNull(result);
    }
}
