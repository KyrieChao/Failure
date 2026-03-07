package com.chao.failfast.internal.check;

/**
 * Utility class for object validation.
 *
 * @author Kyrie Chao
 * @version 1.0.0
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
}
