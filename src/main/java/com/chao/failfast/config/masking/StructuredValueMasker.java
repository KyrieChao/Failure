package com.chao.failfast.config.masking;

import com.chao.failfast.annotation.Sensitive;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.spi.security.ValueMasker;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * StructuredValueMasker is a structured value masker that implements ValueMasker interface,
 * used for recursive value masking of complex objects.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public class StructuredValueMasker implements ValueMasker {
    // Fallback masker for handling primitive types
    private final ValueMasker fallback;
    // Masking configuration properties
    private final FailureProperties.Masking masking;

    /**
     * Constructor, initializes fallback masker and masking configuration
     * @param fallback Fallback masker
     * @param masking Masking configuration, uses default if null
     */
    public StructuredValueMasker(ValueMasker fallback, FailureProperties.Masking masking) {
        this.fallback = fallback;
        this.masking = masking != null ? masking : new FailureProperties.Masking();
    }

    /**
     * Mask the value
     * @param value Value to mask
     * @param fieldPath Field path
     * @return Masked value
     */
    @Override
    public Object mask(Object value, String fieldPath) {
        if (value == null) return null;
        // First try to use fallback masker
        Object direct = fallback.mask(value, fieldPath);
        // If value is not a string or not a masked value, and not a complex object, and not Map, Iterable, or array, return direct result
        if (!(direct instanceof String directText) || !"***[MASKED]***".equals(directText)) {
            if (!isComplexObject(value) && !(value instanceof Map) && !(value instanceof Iterable) && !value.getClass().isArray()) {
                return direct;
            }
        } else {
            return direct;
        }
        // Recursively mask complex objects
        return maskObject(value, 0, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    /**
     * Recursively mask object
     * @param value Object to mask
     * @param depth Current recursion depth
     * @param visited Set of visited objects to detect circular references
     * @return Masked object
     */
    private Object maskObject(Object value, int depth, Set<Object> visited) {
        if (value == null) return null;
        // Check if maximum recursion depth is reached
        if (depth >= Math.max(1, masking.getMaxDepth())) return "[MAX_DEPTH]";
        // Check for circular references
        if (visited.contains(value)) return "[CYCLE]";

        // Handle primitive types
        if (value instanceof CharSequence || value.getClass().isEnum() || value instanceof Number || value instanceof Boolean) {
            return fallback.mask(value, null);
        }
        // Handle Map types
        if (value instanceof Map<?, ?> map) {
            return maskMap(map, depth, visited);
        }
        // Handle Iterable types
        if (value instanceof Iterable<?> iterable) {
            return maskIterable(iterable, depth, visited);
        }
        // Handle array types
        if (value.getClass().isArray()) {
            return maskArray(value, depth, visited);
        }
        // Handle non-complex objects
        if (!isComplexObject(value)) {
            return fallback.mask(value, null);
        }

        // Handle complex objects
        visited.add(value);
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            int count = 0;
            // Collect and process all fields
            for (Field field : collectFields(value.getClass())) {
                // Check if maximum field count limit is exceeded
                if (count >= Math.max(1, masking.getMaxFields())) {
                    result.put("_truncated", true);
                    break;
                }
                field.setAccessible(true);
                Object fieldValue;
                try {
                    fieldValue = field.get(value);
                } catch (IllegalAccessException e) {
                    fieldValue = "[INACCESSIBLE]";
                }
                // Check if field has Sensitive annotation
                Sensitive sensitive = field.getAnnotation(Sensitive.class);
                if (sensitive != null) {
                    result.put(field.getName(), sensitive.maskedValue());
                } else {
                    result.put(field.getName(), maskObject(fieldValue, depth + 1, visited));
                }
                count++;
            }
            return result;
        } finally {
            visited.remove(value);
        }
    }

    /**
     * Mask Map type
     * @param map Map to process
     * @param depth Current recursion depth
     * @param visited Set of visited objects
     * @return Processed Map
     */
    private Object maskMap(Map<?, ?> map, int depth, Set<Object> visited) {
        Map<String, Object> result = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            // Check if maximum collection size limit is exceeded
            if (count >= Math.max(1, masking.getMaxCollectionSize())) {
                result.put("_truncated", true);
                break;
            }
            String key = String.valueOf(entry.getKey());
            result.put(key, maskObject(entry.getValue(), depth + 1, visited));
            count++;
        }
        return result;
    }

    /**
     * Mask Iterable type
     * @param iterable Iterable to process
     * @param depth Current recursion depth
     * @param visited Set of visited objects
     * @return Processed List
     */
    private Object maskIterable(Iterable<?> iterable, int depth, Set<Object> visited) {
        List<Object> result = new ArrayList<>();
        int count = 0;
        for (Object item : iterable) {
            // Check if maximum collection size limit is exceeded
            if (count >= Math.max(1, masking.getMaxCollectionSize())) {
                result.add("[TRUNCATED]");
                break;
            }
            result.add(maskObject(item, depth + 1, visited));
            count++;
        }
        return result;
    }

    /**
     * Mask array type
     * @param value Array to process
     * @param depth Current recursion depth
     * @param visited Set of visited objects
     * @return Processed List
     */
    private Object maskArray(Object value, int depth, Set<Object> visited) {
        List<Object> result = new ArrayList<>();
        int len = Math.min(Array.getLength(value), Math.max(1, masking.getMaxCollectionSize()));
        for (int i = 0; i < len; i++) {
            result.add(maskObject(Array.get(value, i), depth + 1, visited));
        }
        // If array length exceeds limit, add truncation marker
        if (Array.getLength(value) > len) {
            result.add("[TRUNCATED]");
        }
        return result;
    }

    /**
     * Determine if object is complex
     * @param value Object to check
     * @return true if complex object, false otherwise
     */
    private boolean isComplexObject(Object value) {
        Package pkg = value.getClass().getPackage();
        if (pkg == null) return true;
        String pkgName = pkg.getName();
        // Check if it's a class from Java standard library or Jakarta standard library
        return !pkgName.startsWith("java.") && !pkgName.startsWith("javax.") && !pkgName.startsWith("jakarta.");
    }

    /**
     * Collect all fields of the class (including parent class fields)
     * @param type Class to collect fields from
     * @return Field list
     */
    private List<Field> collectFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        // Traverse all fields of the class and its parent classes
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                // Skip static fields and synthetic fields
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (field.isSynthetic()) continue;
                fields.add(field);
            }
            current = current.getSuperclass();
        }
        return fields;
    }
}
