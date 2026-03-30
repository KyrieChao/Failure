package com.chao.failfast.config.registry;

import com.chao.failfast.spi.SkipPrefixRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultSkipPrefixRegistryTest {

    @Test
    void testAddWithNullPrefixes() {
        DefaultSkipPrefixRegistry registry = new DefaultSkipPrefixRegistry();
        SkipPrefixRegistry result = registry.add(null);
        assertSame(registry, result);
    }

    @Test
    void testAddWithEmptyPrefixes() {
        DefaultSkipPrefixRegistry registry = new DefaultSkipPrefixRegistry();
        SkipPrefixRegistry result = registry.add();
        assertSame(registry, result);
    }

    @Test
    void testAddWithMultiplePrefixes() {
        DefaultSkipPrefixRegistry registry = new DefaultSkipPrefixRegistry();
        SkipPrefixRegistry result = registry.add("com.example", "org.springframework");
        assertSame(registry, result);
        assertTrue(registry.shouldSkip("com.example.Test"));
        assertTrue(registry.shouldSkip("org.springframework.Test"));
        assertFalse(registry.shouldSkip("com.chao.Test"));
    }

    @Test
    void testShouldSkipWithNullClassName() {
        DefaultSkipPrefixRegistry registry = new DefaultSkipPrefixRegistry();
        assertFalse(registry.shouldSkip(null));
    }

    @Test
    void testShouldSkipWithNoPrefixes() {
        DefaultSkipPrefixRegistry registry = new DefaultSkipPrefixRegistry();
        assertFalse(registry.shouldSkip("com.example.Test"));
    }

    @Test
    void testShouldSkipWithMatchingPrefix() {
        DefaultSkipPrefixRegistry registry = new DefaultSkipPrefixRegistry();
        registry.add("com.example");
        assertTrue(registry.shouldSkip("com.example.Test"));
        assertFalse(registry.shouldSkip("com.chao.Test"));
    }

    @Test
    void testShouldSkipWithCache() {
        DefaultSkipPrefixRegistry registry = new DefaultSkipPrefixRegistry();
        registry.add("com.example");
        // First call to populate cache
        assertTrue(registry.shouldSkip("com.example.Test"));
        // Second call should use cache
        assertTrue(registry.shouldSkip("com.example.Test"));
    }

    @Test
    void testAddClearsCache() {
        DefaultSkipPrefixRegistry registry = new DefaultSkipPrefixRegistry();
        registry.add("com.example");
        // Populate cache
        assertTrue(registry.shouldSkip("com.example.Test"));
        // Add new prefix
        registry.add("org.springframework");
        // Cache should be cleared, so shouldSkip should still work
        assertTrue(registry.shouldSkip("org.springframework.Test"));
    }
}
