package com.chao.failfast.internal.validation;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.constant.Scenario;
import com.chao.failfast.internal.core.Ex;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.validator.TypedValidator;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A utility class to recursively walk through an object graph and validate its fields.
 * Extracted from ChainCore to reduce its responsibilities.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
public class ObjectGraphWalker {

    /**
     * Recursive validation entry point.
     */
    public static void walk(Object object, String path, TypedValidator typedValidator,
                            ValidationContext context, RecursiveOptions options,
                            int depth, IdentityHashMap<Object, Boolean> visited) {
        // Check if validation should stop
        if (context.isStopped() || depth > options.getMaxDepth() ||
                (context.errorSize() >= options.getMaxErrors())) {
            return;
        }

        // Check for circular reference
        if (object != null && visited.containsKey(object)) {
            return;
        }

        // Mark as visited
        if (object != null) {
            visited.put(object, Boolean.TRUE);
        }

        try {
            // Check if object is null
            if (object == null) {
                return;
            }

            // Check if path is excluded
            if (isExcluded(path, options.getExclude())) {
                return;
            }

            // Check if path is included (if include list is specified)
            if (!isIncluded(path, options.getInclude())) {
                return;
            }

            // Validate current object if there's a validator for it
            boolean validated = typedValidator.validateIfRegistered(object, context);

            // If object was validated, don't recurse further
            if (validated) {
                return;
            }

            if (Ex.getSkipTypeRegistry() != null && Ex.getSkipTypeRegistry().shouldSkip(object.getClass())) {
                return;
            }

            // Recurse into collections
            if (object instanceof Collection<?> collection) {
                int index = 0;
                for (Object item : collection) {
                    if (index >= options.getMaxItems()) {
                        context.reportError(ResponseCode.VALIDATION_ERROR_400, "Collection size exceeds limit");
                        break;
                    }
                    String itemPath = path.isEmpty() ? "[" + index + "]" : path + "[" + index + "]";
                    walk(item, itemPath, typedValidator, context, options, depth + 1, visited);
                    index++;
                }
            }
            // Recurse into maps
            else if (object instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    String entryPath = path.isEmpty() ? "[" + key + "]" : path + "[" + key + "]";
                    walk(value, entryPath, typedValidator, context, options, depth + 1, visited);
                }
            }
            // Recurse into arrays
            else if (object.getClass().isArray()) {
                if (object instanceof Object[] array) {
                    // Object array
                    for (int i = 0; i < array.length; i++) {
                        if (i >= options.getMaxItems()) {
                            context.reportError(ResponseCode.VALIDATION_ERROR_400, "Array size exceeds limit");
                            break;
                        }
                        String itemPath = path.isEmpty() ? "[" + i + "]" : path + "[" + i + "]";
                        walk(array[i], itemPath, typedValidator, context, options, depth + 1, visited);
                    }
                }
            }
            // Recurse into POJOs using reflection
            else if (!isPrimitiveOrWrapper(object.getClass()) && !isStringOrEnum(object.getClass())) {
                List<java.lang.reflect.Field> fields = com.chao.failfast.util.ReflectionCache.getFields(object.getClass());
                Scenario[] currentScenes = context.getScenes();

                for (java.lang.reflect.Field field : fields) {
                    // Scene pruning
                    Set<Scenario> fieldScenes = com.chao.failfast.util.ReflectionCache.getSceneValues(field);
                    if (!fieldScenes.isEmpty()) {
                        boolean match = false;
                        if (currentScenes != null) {
                            for (Scenario currentScene : currentScenes) {
                                if (fieldScenes.contains(currentScene)) {
                                    match = true;
                                    break;
                                }
                            }
                        }
                        if (!match) {
                            continue;
                        }
                    }

                    Object fieldValue;
                    try {
                        fieldValue = field.get(object);
                    } catch (Exception e) {
                        continue;
                    }

                    String fieldName = field.getName();
                    String fieldPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                    walk(fieldValue, fieldPath, typedValidator, context, options, depth + 1, visited);
                }
            }
        } finally {
            if (!options.isDedupeGlobal() && object != null) {
                visited.remove(object);
            }
        }
    }

    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Boolean.class ||
                clazz == Byte.class ||
                clazz == Character.class ||
                clazz == Double.class ||
                clazz == Float.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Short.class ||
                clazz == Void.class;
    }

    private static boolean isStringOrEnum(Class<?> clazz) {
        return clazz == String.class || clazz.isEnum();
    }

    private static boolean isExcluded(String path, List<String> exclude) {
        if (exclude == null || exclude.isEmpty()) {
            return false;
        }
        for (String excluded : exclude) {
            if (path.startsWith(excluded)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isIncluded(String path, List<String> include) {
        if (path == null || path.isEmpty()) {
            return true;
        }
        if (include == null || include.isEmpty()) {
            return true;
        }
        for (String included : include) {
            if (path.equals(included) || path.startsWith(included + ".")) {
                return true;
            }
        }
        return false;
    }
}
