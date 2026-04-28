package com.chao.failure.internal.check;

import java.util.Objects;

/**
 * Utility class for object identity and equality checks.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class IdentityChecks {

    private IdentityChecks() {}

    /**
     * Checks if two objects are the same instance.
     *
     * @param obj1 the first object
     * @param obj2 the second object
     * @return true if both objects refer to the same instance, false otherwise
     */
    public static boolean same(Object obj1, Object obj2) {
        return obj1 == obj2;
    }

    /**
     * Checks if two objects are not the same instance.
     *
     * @param obj1 the first object
     * @param obj2 the second object
     * @return true if objects refer to different instances, false otherwise
     */
    public static boolean notSame(Object obj1, Object obj2) {
        return obj1 != obj2;
    }

    /**
     * Checks if two objects are equal.
     *
     * @param obj1 the first object
     * @param obj2 the second object
     * @return true if objects are equal, false otherwise
     */
    public static boolean equals(Object obj1, Object obj2) {
        return Objects.equals(obj1, obj2);
    }

    /**
     * Checks if two objects are not equal.
     *
     * @param obj1 the first object
     * @param obj2 the second object
     * @return true if objects are not equal, false otherwise
     */
    public static boolean notEquals(Object obj1, Object obj2) {
        return !Objects.equals(obj1, obj2);
    }
}
