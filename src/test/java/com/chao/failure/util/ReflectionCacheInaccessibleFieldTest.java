package com.chao.failure.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ReflectionCacheInaccessibleFieldTest {

    @Test
    void getFieldsSkipsInaccessibleFieldsWithoutThrowing() {
        ReflectionCache.clearCache();
        List<Field> fields = assertDoesNotThrow(() -> ReflectionCache.getFields(String.class));
        assertThat(fields).isNotNull();
    }
}

