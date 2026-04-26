package com.chao.failfast.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeverityTest {

    @Test
    void testFromWithNull() {
        Severity fallback = Severity.INFO;
        Severity result = Severity.from(null, fallback);
        assertSame(fallback, result);
    }

    @Test
    void testFromWithBlankString() {
        Severity fallback = Severity.WARNING;
        Severity result = Severity.from("   ", fallback);
        assertSame(fallback, result);
    }

    @Test
    void testFromWithEmptyString() {
        Severity fallback = Severity.ERROR;
        Severity result = Severity.from("", fallback);
        assertSame(fallback, result);
    }

    @Test
    void testFromWithValidNameUppercase() {
        Severity fallback = Severity.INFO;
        Severity result = Severity.from("DEBUG", fallback);
        assertSame(Severity.DEBUG, result);
    }

    @Test
    void testFromWithValidNameLowercase() {
        Severity fallback = Severity.INFO;
        Severity result = Severity.from("info", fallback);
        assertSame(Severity.INFO, result);
    }

    @Test
    void testFromWithValidNameMixedCase() {
        Severity fallback = Severity.INFO;
        Severity result = Severity.from("WarNing", fallback);
        assertSame(Severity.WARNING, result);
    }

    @Test
    void testFromWithValidNameWithWhitespace() {
        Severity fallback = Severity.INFO;
        Severity result = Severity.from("  ERROR  ", fallback);
        assertSame(Severity.ERROR, result);
    }

    @Test
    void testFromWithInvalidName() {
        Severity fallback = Severity.CRITICAL;
        Severity result = Severity.from("INVALID_NAME", fallback);
        assertSame(fallback, result);
    }

    @Test
    void testFromWithAllEnumValues() {
        for (Severity severity : Severity.values()) {
            Severity result = Severity.from(severity.name(), Severity.DEBUG);
            assertSame(severity, result);
        }
    }
}
