package com.chao.failure.internal.check;

/**
 * Utility class for object validation.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class ObjectChecks {

    private ObjectChecks() {}

    /**
     * Checks if the object exists (is not null).
     *
     * @param obj the object to check
     * @return true if the object is not null, false otherwise
     */
    public static boolean exists(Object obj) {
        return obj != null;
    }

    /**
     * Checks if the object is not null.
     *
     * @param obj the object to check
     * @return true if the object is not null, false otherwise
     */
    public static boolean notNull(Object obj) {
        return obj != null;
    }

    /**
     * Checks if the object is null.
     *
     * @param obj the object to check
     * @return true if the object is null, false otherwise
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * Checks if the object is an instance of the specified type.
     *
     * @param obj  the object to check
     * @param type the target type
     * @return true if the object is an instance of the type, false otherwise
     */
    public static boolean instanceOf(Object obj, Class<?> type) {
        return type != null && type.isInstance(obj);
    }

    /**
     * Checks if the object is not an instance of the specified type.
     *
     * @param obj  the object to check
     * @param type the target type
     * @return true if the object is not an instance of the type, false otherwise
     */
    public static boolean notInstanceOf(Object obj, Class<?> type) {
        return type != null && !type.isInstance(obj);
    }

    /**
     * Checks if all objects are not null.
     *
     * @param objs the objects to check
     * @return true if all objects are not null, false otherwise
     */
    public static boolean allNotNull(Object... objs) {
        if (objs == null) {
            return false;
        }
        for (Object obj : objs) {
            if (obj == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the collection is not empty.
     *
     * @param collection the collection to check
     * @return true if the collection is not null and not empty, false otherwise
     */
    public static boolean notEmpty(java.util.Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * Checks if the map is not empty.
     *
     * @param map the map to check
     * @return true if the map is not null and not empty, false otherwise
     */
    public static boolean notEmpty(java.util.Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }
}
