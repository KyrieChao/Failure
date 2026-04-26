package com.chao.failfast.internal.check;

/**
 * Utility class for boolean validation.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class BooleanChecks {

    private BooleanChecks() {}

    /**
     * Checks the state of a boolean condition.
     *
     * @param condition the condition to check
     * @return the condition value
     */
    public static boolean state(boolean condition) {
        return condition;
    }

    /**
     * Checks if the condition is true.
     *
     * @param cond the condition to check
     * @return true if the condition is true, false otherwise
     */
    public static boolean isTrue(boolean cond) {
        return cond;
    }

    /**
     * Checks if the condition is false.
     *
     * @param cond the condition to check
     * @return true if the condition is false, false otherwise
     */
    public static boolean isFalse(boolean cond) {
        return !cond;
    }
}
