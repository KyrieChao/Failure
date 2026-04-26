package com.chao.failfast.config.registry;

import com.chao.failfast.spi.filter.SkipTypeRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultSkipTypeRegistryTest {

    @Test
    void should_returnSameRegistry_when_typeArrayIsNull() {
        DefaultSkipTypeRegistry registry = new DefaultSkipTypeRegistry();

        SkipTypeRegistry result = registry.add((Class<?>[]) null);

        assertSame(registry, result);
    }

    @Test
    void testAddWithNullTypes() {
        DefaultSkipTypeRegistry registry = new DefaultSkipTypeRegistry();
        SkipTypeRegistry result = registry.add((Class<?>) null);
        assertSame(registry, result);
    }

    @Test
    void testAddWithEmptyTypes() {
        DefaultSkipTypeRegistry registry = new DefaultSkipTypeRegistry();
        SkipTypeRegistry result = registry.add();
        assertSame(registry, result);
    }

    @Test
    void testAddWithMultipleTypes() {
        DefaultSkipTypeRegistry registry = new DefaultSkipTypeRegistry();
        SkipTypeRegistry result = registry.add(String.class, Integer.class);
        assertSame(registry, result);
        assertTrue(registry.shouldSkip(String.class));
        assertTrue(registry.shouldSkip(Integer.class));
        assertFalse(registry.shouldSkip(Double.class));
    }

    @Test
    void testShouldSkipWithNullType() {
        DefaultSkipTypeRegistry registry = new DefaultSkipTypeRegistry();
        assertTrue(registry.shouldSkip(null));
    }

    @Test
    void testShouldSkipWithNoTypes() {
        DefaultSkipTypeRegistry registry = new DefaultSkipTypeRegistry();
        assertFalse(registry.shouldSkip(String.class));
    }

    @Test
    void testShouldSkipWithExactType() {
        DefaultSkipTypeRegistry registry = new DefaultSkipTypeRegistry();
        registry.add(String.class);
        assertTrue(registry.shouldSkip(String.class));
        assertFalse(registry.shouldSkip(Integer.class));
    }

    @Test
    void testShouldSkipWithAssignableType() {
        DefaultSkipTypeRegistry registry = new DefaultSkipTypeRegistry();
        registry.add(Number.class);
        assertTrue(registry.shouldSkip(Integer.class));
        assertTrue(registry.shouldSkip(Double.class));
        assertFalse(registry.shouldSkip(String.class));
    }

    @Test
    void testShouldSkipWithCache() {
        DefaultSkipTypeRegistry registry = new DefaultSkipTypeRegistry();
        registry.add(String.class);
        // First call to populate cache
        assertTrue(registry.shouldSkip(String.class));
        // Second call should use cache
        assertTrue(registry.shouldSkip(String.class));
    }

    @Test
    void testAddClearsCache() {
        DefaultSkipTypeRegistry registry = new DefaultSkipTypeRegistry();
        registry.add(String.class);
        // Populate cache
        assertTrue(registry.shouldSkip(String.class));
        // Add new type
        registry.add(Integer.class);
        // Cache should be cleared, so shouldSkip should still work
        assertTrue(registry.shouldSkip(Integer.class));
    }
}
