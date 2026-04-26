package com.chao.failfast.internal.validation;

import com.chao.failfast.validator.FastValidator.ValidationContext;
import com.chao.failfast.constant.Scenario;
import com.chao.failfast.internal.core.Ex;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.util.ReflectionCache;
import com.chao.failfast.validator.TypedValidator;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Object graph walker for recursive traversal and validation of all objects in the object graph
 */
public class ObjectGraphWalker {

    /**
     * Recursively traverse object graph
     * @param object Current object to traverse
     * @param path Current object path for identifying position in object graph
     * @param typedValidator Type validator for validating specific types of objects
     * @param context Validation context containing validation state and information
     * @param options Recursive options controlling traversal behavior and limits
     * @param depth Current recursion depth
     * @param visited Visited object mapping to prevent infinite recursion from circular references
     */
    public static void walk(Object object, String path, TypedValidator typedValidator,
                            ValidationContext context, RecursiveOption options,
                            int depth, IdentityHashMap<Object, Boolean> visited) {
        // Check if traversal should stop: context stopped, exceeded max depth, or error count exceeded limit
        if (context.isStopped() || depth > options.getMaxDepth() ||
                (context.errorSize() >= options.getMaxErrors())) {
            return;
        }
        // Check if object has been visited to prevent circular references
        if (object != null && visited.containsKey(object)) {
            return;
        }
        // Record visited object
        if (object != null) {
            visited.put(object, Boolean.TRUE);
        }

        try {
            // Handle null object
            if (object == null) {
                return;
            }
            // Check if path is in exclude list
            if (isExcluded(path, options.getExclude())) {
                return;
            }
            // Check if path is in include list
            if (!isIncluded(path, options.getInclude())) {
                return;
            }
            // If object has registered type validator, validate it
            boolean validated = typedValidator.validateIfRegistered(object, context);
            if (validated) {
                return;
            }

            // Check if this type should be skipped
            if (Ex.getSkipTypeRegistry() != null && Ex.getSkipTypeRegistry().shouldSkip(object.getClass())) {
                return;
            }
            // Handle collection types
            if (object instanceof Collection<?> collection) {
                int index = 0;
                for (Object item : collection) {
                    // Check if collection size exceeds limit
                    if (index >= options.getMaxItems()) {
                        context.reportError(ResponseCode.VALIDATION_ERROR_400, "Collection size exceeds limit");
                        break;
                    }
                    // Build collection item path
                    String itemPath = path.isEmpty() ? "[" + index + "]" : path + "[" + index + "]";
                    // Recursively traverse collection item
                    walk(item, itemPath, typedValidator, context, options, depth + 1, visited);
                    index++;
                }
            }
            // Handle Map types
            else if (object instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    // Build Map entry path
                    String entryPath = path.isEmpty() ? "[" + key + "]" : path + "[" + key + "]";
                    // Recursively traverse Map value
                    walk(value, entryPath, typedValidator, context, options, depth + 1, visited);
                }
            }
            // Handle array types
            else if (object.getClass().isArray()) {
                if (object instanceof Object[] array) {
                    for (int i = 0; i < array.length; i++) {
                        // Check if array size exceeds limit
                        if (i >= options.getMaxItems()) {
                            context.reportError(ResponseCode.VALIDATION_ERROR_400, "Array size exceeds limit");
                            break;
                        }
                        // Build array element path
                        String itemPath = path.isEmpty() ? "[" + i + "]" : path + "[" + i + "]";
                        // Recursively traverse array element
                        walk(array[i], itemPath, typedValidator, context, options, depth + 1, visited);
                    }
                }
            }
            // Handle custom object types
            else if (!isPrimitiveOrWrapper(object.getClass()) && !isStringOrEnum(object.getClass())) {
                // Get all fields of the object
                List<Field> fields = com.chao.failfast.util.ReflectionCache.getFields(object.getClass());
                // Get current scenes
                Scenario[] currentScenes = context.getScenes();

                // Traverse all fields
                for (Field field : fields) {
                    // Get field scene requirements
                    Set<Scenario> fieldScenes = ReflectionCache.getSceneValues(field);
                    // If field has scene requirements, check if they match current scenes
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
                        // If no match with current scenes, skip this field
                        if (!match) {
                            continue;
                        }
                    }

                    // Get field value
                    Object fieldValue;
                    try {
                        fieldValue = field.get(object);
                    } catch (Exception e) {
                        continue;
                    }

                    // Build field path
                    String fieldName = field.getName();
                    String fieldPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                    // Recursively traverse field value
                    walk(fieldValue, fieldPath, typedValidator, context, options, depth + 1, visited);
                }
            }
        } finally {
            // If global deduplication is not disabled, remove current object from visited set
            if (!options.isDedupeGlobal() && object != null) {
                visited.remove(object);
            }
        }
    }

    /**
     * Check if class is primitive or its wrapper
     * @param clazz Class to check
     * @return true if class is primitive or wrapper, false otherwise
     */
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

    /**
     * Check if class is String or enum type
     * @param clazz Class to check
     * @return true if class is String or enum, false otherwise
     */
    private static boolean isStringOrEnum(Class<?> clazz) {
        return clazz == String.class || clazz.isEnum();
    }

    /**
     * Check if path is in exclude list
     * @param path Path to check
     * @param exclude Exclude path list
     * @return true if path is in exclude list, false otherwise
     */
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

    /**
     * Check if path is in include list
     * @param path Path to check
     * @param include Include path list
     * @return true if path is in include list, false otherwise
     */
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
