package com.chao.failfast.internal.check;

/**
 * Utility class for number validation.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class NumberChecks {

    private NumberChecks() {
    }

    /**
     * Checks if the number is positive.
     *
     * @param value the number to check
     * @return true if the number is positive, false otherwise
     */
    public static boolean positive(Number value) {
        return value != null && value.doubleValue() > 0;
    }

    /**
     * Checks if the number is within the specified range.
     *
     * @param value the number to check
     * @param min   the minimum value (inclusive)
     * @param max   the maximum value (inclusive)
     * @return true if the number is within [min, max], false otherwise
     */
    public static <T extends Number & Comparable<T>> boolean inRange(T value, T min, T max) {
        return value != null && min != null && max != null
                && value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    /**
     * Checks if the number is within the specified range using double values.
     *
     * @param v   the number to check
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return true if the number is within [min, max], false otherwise
     */
    public static boolean inRangeNumber(Number v, Number min, Number max) {
        return v != null && min != null && max != null
                && v.doubleValue() >= min.doubleValue()
                && v.doubleValue() <= max.doubleValue();
    }

    /**
     * Checks if the number is non-negative.
     *
     * @param value the number to check
     * @return true if the number is non-negative, false otherwise
     */
    public static boolean nonNegative(Number value) {
        return value != null && value.doubleValue() >= 0;
    }

    /**
     * Checks if the number is greater than the threshold.
     *
     * @param value     the number to check
     * @param threshold the threshold value
     * @return true if the number is greater than the threshold, false otherwise
     */
    public static <T extends Number & Comparable<T>> boolean greaterThan(T value, T threshold) {
        return value != null && threshold != null && value.compareTo(threshold) > 0;
    }

    /**
     * Checks if the number is greater than or equal to the threshold.
     *
     * @param value     the number to check
     * @param threshold the threshold value
     * @return true if the number is greater than or equal to the threshold, false otherwise
     */
    public static <T extends Number & Comparable<T>> boolean greaterOrEqual(T value, T threshold) {
        return value != null && threshold != null && value.compareTo(threshold) >= 0;
    }

    /**
     * Checks if the number is less than the threshold.
     *
     * @param value     the number to check
     * @param threshold the threshold value
     * @return true if the number is less than the threshold, false otherwise
     */
    public static <T extends Number & Comparable<T>> boolean lessThan(T value, T threshold) {
        return value != null && threshold != null && value.compareTo(threshold) < 0;
    }

    /**
     * Checks if the number is less than or equal to the threshold.
     *
     * @param value     the number to check
     * @param threshold the threshold value
     * @return true if the number is less than or equal to the threshold, false otherwise
     */
    public static <T extends Number & Comparable<T>> boolean lessOrEqual(T value, T threshold) {
        return value != null && threshold != null && value.compareTo(threshold) <= 0;
    }

    /**
     * Checks if the number is not zero.
     *
     * @param value the number to check
     * @return true if the number is not zero, false otherwise
     */
    public static boolean notZero(Number value) {
        return value != null && value.doubleValue() != 0.0;
    }

    /**
     * Checks if the number is zero.
     *
     * @param value the number to check
     * @return true if the number is zero, false otherwise
     */
    public static boolean isZero(Number value) {
        return value != null && value.doubleValue() == 0.0;
    }

    /**
     * Checks if the number is negative.
     *
     * @param value the number to check
     * @return true if the number is negative, false otherwise
     */
    public static boolean negative(Number value) {
        return value != null && value.doubleValue() < 0;
    }

    /**
     * Checks if the number is a multiple of the divisor.
     *
     * @param value   the number to check
     * @param divisor the divisor
     * @return true if the number is a multiple of the divisor, false otherwise
     */
    public static boolean multipleOf(Number value, Number divisor) {
        if (value == null || divisor == null || divisor.doubleValue() == 0) {
            return false;
        }
        return value.doubleValue() % divisor.doubleValue() == 0;
    }

    /**
     * Checks if the BigDecimal has the specified scale.
     *
     * @param value the BigDecimal to check
     * @param scale the expected scale
     * @return true if the BigDecimal has the specified scale, false otherwise
     */
    public static boolean decimalScale(java.math.BigDecimal value, int scale) {
        return value != null && value.scale() == scale;
    }
}
