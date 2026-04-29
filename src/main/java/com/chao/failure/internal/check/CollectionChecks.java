package com.chao.failure.internal.check;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Utility class for collection validation.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public final class CollectionChecks {

    private CollectionChecks() {}

    /**
     * Checks if the collection is not empty.
     *
     * @param col the collection to check
     * @return true if the collection is not null and not empty, false otherwise
     */
    public static boolean notEmpty(Collection<?> col) {
        return col != null && !col.isEmpty();
    }
    
    /**
     * Checks if the collection is empty.
     *
     * @param col the collection to check
     * @return true if the collection is null or empty, false otherwise
     */
    public static boolean isEmpty(Collection<?> col) {
        return col == null || col.isEmpty();
    }

    /**
     * Checks if the collection size is within the specified range.
     *
     * @param col the collection to check
     * @param min the minimum size (inclusive)
     * @param max the maximum size (inclusive)
     * @return true if the collection size is within [min, max], false otherwise
     */
    public static boolean sizeBetween(Collection<?> col, int min, int max) {
        int size = (col == null) ? 0 : col.size();
        return size >= min && size <= max;
    }

    /**
     * Checks if the collection size equals the expected size.
     *
     * @param col          the collection to check
     * @param expectedSize the expected size
     * @return true if the collection size equals the expected size, false otherwise
     */
    public static boolean sizeEquals(Collection<?> col, int expectedSize) {
        return col != null && col.size() == expectedSize;
    }

    /**
     * Checks if the collection contains the specified element.
     *
     * @param col the collection to check
     * @param o   the element to find
     * @return true if the collection contains the element, false otherwise
     */
    public static boolean contains(Collection<?> col, Object o) {
        return col != null && col.contains(o);
    }

    /**
     * Checks if the collection does not contain the specified element.
     *
     * @param col the collection to check
     * @param o   the element to exclude
     * @return true if the collection does not contain the element, false otherwise
     */
    public static boolean notContains(Collection<?> col, Object o) {
        return col == null || !col.contains(o);
    }

    public static boolean containsAll(Collection<?> col, Collection<?> required) {
        return col != null && required != null && col.containsAll(required);
    }

    public static <T> boolean noneMatch(Collection<T> col, Predicate<T> predicate) {
        if (col == null || predicate == null) return false;
        for (T t : col) {
            if (predicate.test(t)) {
                return false;
            }
        }
        return true;
    }

    public static boolean uniqueElements(Collection<?> col) {
        if (col == null) return true;
        Set<Object> set = new HashSet<>(Math.max(16, (int) (col.size() / 0.75f) + 1));
        for (Object element : col) {
            if (!set.add(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the collection contains no null elements.
     *
     * @param col the collection to check
     * @return true if the collection is null or contains no null elements, false otherwise
     */
    public static boolean hasNoNullElements(Collection<?> col) {
        if (col == null) return true;
        for (Object element : col) {
            if (element == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all elements in the collection match the predicate.
     *
     * @param col       the collection to check
     * @param predicate the condition to match
     * @return true if all elements match the predicate, false otherwise
     */
    public static <T> boolean allMatch(Collection<T> col, Predicate<T> predicate) {
        if (col == null || predicate == null) return false;
        for (T t : col) {
            if (!predicate.test(t)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if any element in the collection matches the predicate.
     *
     * @param col       the collection to check
     * @param predicate the condition to match
     * @return true if any element matches the predicate, false otherwise
     */
    public static <T> boolean anyMatch(Collection<T> col, Predicate<T> predicate) {
        if (col == null || predicate == null) return false;
        for (T t : col) {
            if (predicate.test(t)) {
                return true;
            }
        }
        return false;
    }
}
