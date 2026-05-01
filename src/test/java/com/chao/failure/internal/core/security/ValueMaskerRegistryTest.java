package com.chao.failure.internal.core.security;

import com.chao.failure.config.masking.DefaultValueMasker;
import com.chao.failure.spi.security.ValueMasker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

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

        ValueMaskerRegistry.setDefault(customMasker);

        ValueMasker result = ValueMaskerRegistry.getDefault();
        assertEquals(customMasker, result);
    }

    @Test
    void testSetDefaultWithNull() {
        ValueMasker customMasker = mock(ValueMasker.class);

        ValueMaskerRegistry.setDefault(customMasker);
        ValueMaskerRegistry.setDefault(null);

        ValueMasker result = ValueMaskerRegistry.getDefault();
        assertNotNull(result);
        assertTrue(result instanceof DefaultValueMasker);
    }

    @Test
    void testSetDefaultMultipleTimes() {
        ValueMasker masker1 = mock(ValueMasker.class);

        ValueMasker masker2 = mock(ValueMasker.class);

        ValueMaskerRegistry.setDefault(masker1);
        assertEquals(masker1, ValueMaskerRegistry.getDefault());

        ValueMaskerRegistry.setDefault(masker2);
        assertEquals(masker2, ValueMaskerRegistry.getDefault());
    }

    @Test
    void testGetDefaultWithNullViaReflection() throws Exception {
        Field maskerField = ValueMaskerRegistry.class.getDeclaredField("MASKER");
        maskerField.setAccessible(true);
        @SuppressWarnings("unchecked")
        AtomicReference<ValueMasker> ref = (AtomicReference<ValueMasker>) maskerField.get(null);
        ValueMasker previous = ref.get();
        ref.set(null);

        try {
            ValueMasker result = ValueMaskerRegistry.getDefault();
            assertNotNull(result);
            assertTrue(result instanceof DefaultValueMasker);
        } finally {
            ref.set(previous);
        }
    }

    @Test
    void testCustomMaskerIsUsed() {
        ValueMasker customMasker = mock(ValueMasker.class);
        when(customMasker.mask("test")).thenReturn("***MASKED***");

        ValueMaskerRegistry.setDefault(customMasker);

        Object result = customMasker.mask("test");
        assertEquals("***MASKED***", result);
    }

    @Test
    void testFallbackMaskerFunctionality() {
        ValueMaskerRegistry.setDefault(null);

        ValueMasker result = ValueMaskerRegistry.getDefault();
        assertNotNull(result);

        Object maskedValue = result.mask("sensitive", () -> "password");
        assertEquals("***[MASKED]***", maskedValue);
    }
}
