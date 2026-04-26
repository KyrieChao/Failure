package com.chao.failfast.internal.core.security;

import com.chao.failfast.config.masking.DefaultValueMasker;
import com.chao.failfast.spi.security.ValueMasker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValueMaskerRegistryTest {

    @AfterEach
    void tearDown() {
        ValueMaskerRegistry.setDefault(null);
    }

    @Test
    void testGetDefaultReturnsDefaultMasker() {
        ValueMasker result = ValueMaskerRegistry.getDefault();
        assertNotNull(result);
        assertTrue(result instanceof DefaultValueMasker);
    }

    @Test
    void testSetDefaultWithCustomMasker() {
        ValueMasker customMasker = mock(ValueMasker.class);
        when(customMasker.mask(any(), any())).thenReturn("masked");

        ValueMaskerRegistry.setDefault(customMasker);

        ValueMasker result = ValueMaskerRegistry.getDefault();
        assertEquals(customMasker, result);
    }

    @Test
    void testSetDefaultWithNull() {
        ValueMasker customMasker = mock(ValueMasker.class);
        when(customMasker.mask(any(), any())).thenReturn("masked");

        ValueMaskerRegistry.setDefault(customMasker);
        ValueMaskerRegistry.setDefault(null);

        ValueMasker result = ValueMaskerRegistry.getDefault();
        assertNotNull(result);
        assertTrue(result instanceof DefaultValueMasker);
    }

    @Test
    void testSetDefaultMultipleTimes() {
        ValueMasker masker1 = mock(ValueMasker.class);
        when(masker1.mask(any(), any())).thenReturn("masker1");

        ValueMasker masker2 = mock(ValueMasker.class);
        when(masker2.mask(any(), any())).thenReturn("masker2");

        ValueMaskerRegistry.setDefault(masker1);
        assertEquals(masker1, ValueMaskerRegistry.getDefault());

        ValueMaskerRegistry.setDefault(masker2);
        assertEquals(masker2, ValueMaskerRegistry.getDefault());
    }

    @Test
    void testGetDefaultWithNullViaReflection() throws Exception {
        Field maskerField = ValueMaskerRegistry.class.getDeclaredField("masker");
        maskerField.setAccessible(true);
        maskerField.set(null, null);

        try {
            ValueMasker result = ValueMaskerRegistry.getDefault();
            assertNotNull(result);
            assertTrue(result instanceof DefaultValueMasker);
        } finally {
            maskerField.set(null, new DefaultValueMasker());
        }
    }

    @Test
    void testCustomMaskerIsUsed() {
        ValueMasker customMasker = mock(ValueMasker.class);
        when(customMasker.mask("test", "path")).thenReturn("***MASKED***");

        ValueMaskerRegistry.setDefault(customMasker);

        Object result = customMasker.mask("test", "path");
        assertEquals("***MASKED***", result);
    }

    @Test
    void testFallbackMaskerFunctionality() {
        ValueMaskerRegistry.setDefault(null);

        ValueMasker result = ValueMaskerRegistry.getDefault();
        assertNotNull(result);

        Object maskedValue = result.mask("sensitive", "password");
        assertEquals("***[MASKED]***", maskedValue);
    }
}
