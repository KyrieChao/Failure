package com.chao.failfast.util;

import com.chao.failfast.annotation.Scene;
import com.chao.failfast.constant.Scenario;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionCacheTest {

    @Test
    void testGetFieldsWithNullClass() {
        List<Field> fields = ReflectionCache.getFields(null);
        assertNotNull(fields);
        assertTrue(fields.isEmpty());
    }

    @Test
    void testGetFieldsWithConcreteClass() {
        List<Field> fields = ReflectionCache.getFields(TestClass.class);
        assertNotNull(fields);
        assertEquals(2, fields.size());
        // Check that both fields are present
        boolean hasNameField = false;
        boolean hasAgeField = false;
        for (Field field : fields) {
            if ("name".equals(field.getName())) {
                hasNameField = true;
            } else if ("age".equals(field.getName())) {
                hasAgeField = true;
            }
        }
        assertTrue(hasNameField);
        assertTrue(hasAgeField);
    }

    @Test
    void testFindFieldWithNullClass() {
        Field field = ReflectionCache.findField(null, "name");
        assertNull(field);
    }

    @Test
    void testFindFieldWithNullFieldName() {
        Field field = ReflectionCache.findField(TestClass.class, null);
        assertNull(field);
    }

    @Test
    void testFindFieldWithEmptyFieldName() {
        Field field = ReflectionCache.findField(TestClass.class, "");
        assertNull(field);
    }

    @Test
    void testFindFieldWithExistingField() {
        Field field = ReflectionCache.findField(TestClass.class, "name");
        assertNotNull(field);
        assertEquals("name", field.getName());
    }

    @Test
    void testFindFieldWithNonExistingField() {
        Field field = ReflectionCache.findField(TestClass.class, "nonExistingField");
        assertNull(field);
    }

    @Test
    void testGetSceneValuesWithNullField() {
        Set<Scenario> scenarios = ReflectionCache.getSceneValues(null);
        assertNotNull(scenarios);
        assertTrue(scenarios.isEmpty());
    }

    @Test
    void testGetSceneValuesWithFieldWithoutSceneAnnotation() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("name");
        Set<Scenario> scenarios = ReflectionCache.getSceneValues(field);
        assertNotNull(scenarios);
        assertTrue(scenarios.isEmpty());
    }

    @Test
    void testGetSceneValuesWithFieldWithSceneAnnotation() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("age");
        Set<Scenario> scenarios = ReflectionCache.getSceneValues(field);
        assertNotNull(scenarios);
        assertEquals(2, scenarios.size());
        assertTrue(scenarios.contains(Scenario.CREATE));
        assertTrue(scenarios.contains(Scenario.UPDATE));
    }

    @Test
    void testClearCache() {
        // First call to populate cache
        ReflectionCache.getFields(TestClass.class);
        ReflectionCache.findField(TestClass.class, "name");
        try {
            Field field = TestClass.class.getDeclaredField("age");
            ReflectionCache.getSceneValues(field);
        } catch (NoSuchFieldException e) {
            fail("Field not found", e);
        }

        // Clear cache
        ReflectionCache.clearCache();

        // Call again to ensure cache is repopulated
        List<Field> fields = ReflectionCache.getFields(TestClass.class);
        assertNotNull(fields);
        assertFalse(fields.isEmpty());
    }

    // tryMakeAccessible is a private method, so we can't test it directly
    // We test its functionality indirectly through getFields and findField methods


    // Test class with fields for testing
    private static class TestClass {
        private String name;
        @Scene({Scenario.CREATE, Scenario.UPDATE})
        private int age;
        // Static field should be excluded
        public static String staticField = "static";
    }
}
