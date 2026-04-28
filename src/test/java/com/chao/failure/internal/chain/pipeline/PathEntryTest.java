package com.chao.failure.internal.chain.pipeline;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathEntryTest {

    @Test
    void testRecordProperties() {
        String value = "test value";
        String path = "test.path";
        PathEntry<String> entry = new PathEntry<>(value, path);
        assertSame(value, entry.value());
        assertEquals(path, entry.path());
    }

    @Test
    void testWithNullValues() {
        PathEntry<String> entry = new PathEntry<>(null, null);
        assertNull(entry.value());
        assertNull(entry.path());
    }
}
