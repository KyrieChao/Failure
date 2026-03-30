package com.chao.failfast.util;

import com.chao.failfast.annotation.Scene;
import com.chao.failfast.constant.Scenario;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for caching reflection metadata to improve validation performance.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
public final class ReflectionCache {

    /**
     * Cache for all validatable fields of a class (including superclasses).
     * Excludes static fields. Fields are pre-set to be accessible.
     */
    private static final Map<Class<?>, List<Field>> FIELD_LIST_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for field lookup by name.
     */
    private static final Map<Class<?>, Map<String, Field>> FIELD_MAP_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for Scene values on fields.
     */
    private static final Map<Field, Set<Scenario>> SCENE_CACHE = new ConcurrentHashMap<>();

    private ReflectionCache() {
        // Prevent instantiation
    }

    /**
     * Get all validatable fields for a class, including inherited ones.
     *
     * @param clazz The class to inspect
     * @return List of accessible fields
     */
    public static List<Field> getFields(Class<?> clazz) {
        if (clazz == null) return Collections.emptyList();
        return FIELD_LIST_CACHE.computeIfAbsent(clazz, c -> {
            List<Field> fields = new ArrayList<>();
            ReflectionUtils.doWithFields(c, field -> {
                if (!Modifier.isStatic(field.getModifiers())) {
                    if (tryMakeAccessible(field)) {
                        fields.add(field);
                    }
                }
            });
            return Collections.unmodifiableList(fields);
        });
    }

    /**
     * Find a specific field by name, including inherited fields.
     *
     * @param clazz     The class to inspect
     * @param fieldName The name of the field
     * @return The field, or null if not found
     */
    public static Field findField(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null || fieldName.isEmpty()) return null;
        Map<String, Field> fieldMap = FIELD_MAP_CACHE.computeIfAbsent(clazz, c -> {
            Map<String, Field> map = new HashMap<>();
            ReflectionUtils.doWithFields(c, field -> {
                tryMakeAccessible(field);
                map.put(field.getName(), field);
            });
            return Collections.unmodifiableMap(map);
        });
        return fieldMap.get(fieldName);
    }

    /**
     * Get the Scenario values specified by the @Scene annotation on a field.
     *
     * @param field The field to check
     * @return Set of scenarios, empty set if no annotation is present
     */
    public static Set<Scenario> getSceneValues(Field field) {
        if (field == null) return Collections.emptySet();
        return SCENE_CACHE.computeIfAbsent(field, f -> {
            Scene sceneAnnotation = f.getAnnotation(Scene.class);
            if (sceneAnnotation == null) {
                return Collections.emptySet();
            }
            return Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(sceneAnnotation.value())));
        });
    }

    /**
     * Clear all caches. Useful for testing or when class reloading occurs.
     */
    public static void clearCache() {
        FIELD_LIST_CACHE.clear();
        FIELD_MAP_CACHE.clear();
        SCENE_CACHE.clear();
    }

    private static boolean tryMakeAccessible(Field field) {
        try {
            ReflectionUtils.makeAccessible(field);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
