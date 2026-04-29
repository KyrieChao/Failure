package com.chao.failure.config.masking;

import com.chao.failure.spi.security.Mask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultValueMaskerTest {

    private final DefaultValueMasker masker = new DefaultValueMasker();

    @Test
    void maskWithNullValue() {
        assertNull(masker.mask(null));
    }

    @Test
    void maskWithNullValueAndMask() {
        assertNull(masker.mask(null, () -> "phone"));
    }

    @Test
    void mask2() {
        assertEquals("", masker.mask("", () -> "phone"));
    }

    @Test
    void maskWithNullMask() {
        assertEquals("test", masker.mask("test", null));
    }

    @Test
    void maskWithBlankMaskType() {
        Mask blankMask = () -> "  ";
        assertEquals("test", masker.mask("test", blankMask));
    }

    @Test
    void maskWithEmptyString() {
        assertEquals("", masker.mask(""));
    }

    @Test
    void maskPhone() {
        Mask phoneMask = () -> "phone";
        assertEquals("138****5678", masker.mask("13812345678", phoneMask));
    }

    @Test
    void maskPhoneWithInvalidFormat() {
        Mask phoneMask = () -> "phone";
        assertEquals("***[MASKED]***", masker.mask("12345678", phoneMask));
    }

    @Test
    void maskEmail() {
        Mask emailMask = () -> "email";
        assertEquals("a****@example.com", masker.mask("admin@example.com", emailMask));
    }

    @Test
    void maskEmailWithInvalidFormat() {
        Mask emailMask = () -> "email";
        assertEquals("***[MASKED]***", masker.mask("invalid-email", emailMask));
    }

    @Test
    void maskBankCard() {
        Mask cardMask = () -> "bankcard";
        assertEquals("6222****1234", masker.mask("622202123456781234", cardMask));
    }

    @Test
    void maskCreditCard() {
        Mask cardMask = () -> "creditcard";
        assertEquals("6222****1234", masker.mask("622202123456781234", cardMask));
    }

    @Test
    void maskCard() {
        Mask cardMask = () -> "card";
        assertEquals("6222****1234", masker.mask("622202123456781234", cardMask));
    }

    @Test
    void maskCardWithInvalidFormat() {
        Mask cardMask = () -> "card";
        assertEquals("***[MASKED]***", masker.mask("123456", cardMask));
    }

    @Test
    void maskUnknownType() {
        Mask unknownMask = () -> "unknown";
        assertEquals("***[MASKED]***", masker.mask("test", unknownMask));
    }

    @Test
    void truncateAndFormatMobile() {
        assertEquals("138****5678", masker.mask("13812345678"));
    }

    @Test
    void truncateAndFormatEmail() {
        assertEquals("a****@example.com", masker.mask("admin@example.com"));
    }

    @Test
    void truncateAndFormatCard() {
        assertEquals("6222****1234", masker.mask("622202123456781234"));
    }

    @Test
    void truncateAndFormatLongString() {
        String longStr = "a".repeat(60);
        String result = (String) masker.mask(longStr);
        assertTrue(result.contains("...(60char)..."));
        assertTrue(result.startsWith("aaaaa"));
        assertTrue(result.endsWith("aaaaa"));
    }

    @Test
    void truncateAndFormatNormalString() {
        assertEquals("hello", masker.mask("hello"));
    }

    @Test
    void truncateAndFormatEmptyString() {
        assertEquals("", masker.mask(""));
    }

    @Test
    void maskWithUpperCaseType() {
        Mask phoneMask = () -> "PHONE";
        assertEquals("138****5678", masker.mask("13812345678", phoneMask));
    }

    @Test
    void maskWithMixedCaseType() {
        Mask phoneMask = () -> "Phone";
        assertEquals("138****5678", masker.mask("13812345678", phoneMask));
    }
}
