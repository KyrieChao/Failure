package com.chao.failure.internal.check;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Utility class for array validation.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class ArrayChecks {

    private ArrayChecks() {
    }

    /**
     * Checks if the array is not empty.
     *
     * @param array the array to check
     * @return true if the array is not null and has elements, false otherwise
     */
    public static <T> boolean notEmpty(T[] array) {
        return array != null && array.length > 0;
    }

    /**
     * Checks if the array is empty.
     *
     * @param array the array to check
     * @return true if the array is null or empty, false otherwise
     */
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the array size is within the specified range.
     *
     * @param array the array to check
     * @param min   the minimum size (inclusive)
     * @param max   the maximum size (inclusive)
     * @return true if the array size is within [min, max], false otherwise
     */
    public static <T> boolean sizeBetween(T[] array, int min, int max) {
        int len = (array == null) ? 0 : array.length;
        return len >= min && len <= max;
    }

    /**
     * Checks if the array size equals the expected size.
     *
     * @param array        the array to check
     * @param expectedSize the expected size
     * @return true if the array size equals the expected size, false otherwise
     */
    public static <T> boolean sizeEquals(T[] array, int expectedSize) {
        return array != null && array.length == expectedSize;
    }

    /**
     * Checks if the array contains the specified element.
     *
     * @param array the array to check
     * @param o     the element to find
     * @return true if the array contains the element, false otherwise
     */
    public static <T> boolean contains(T[] array, T o) {
        if (array == null) return false;
        for (T element : array) {
            if (Objects.equals(element, o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the array does not contain the specified element.
     *
     * @param array the array to check
     * @param o     the element to exclude
     * @return true if the array does not contain the element, false otherwise
     */
    public static <T> boolean notContains(T[] array, T o) {
        return !contains(array, o);
    }

    /**
     * Checks if the array contains no null elements.
     *
     * @param array the array to check
     * @return true if the array is null or contains no null elements, false otherwise
     */
    public static <T> boolean hasNoNullElements(T[] array) {
        if (array == null) return true;
        for (T element : array) {
            if (element == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all elements in the array match the predicate.
     *
     * @param array     the array to check
     * @param predicate the condition to match
     * @return true if all elements match the predicate, false otherwise
     */
    public static <T> boolean allMatch(T[] array, Predicate<T> predicate) {
        if (array == null || predicate == null) return false;
        for (T t : array) {
            if (!predicate.test(t)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if any element in the array matches the predicate.
     *
     * @param array     the array to check
     * @param predicate the condition to match
     * @return true if any element matches the predicate, false otherwise
     */
    public static <T> boolean anyMatch(T[] array, Predicate<T> predicate) {
        if (array == null || predicate == null) return false;
        for (T t : array) {
            if (predicate.test(t)) {
                return true;
            }
        }
        return false;
    }
}
