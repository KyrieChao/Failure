package com.chao.failure.internal.check;

import java.util.Map;

/**
 * Utility class for map validation.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class MapChecks {

    private MapChecks() {}

    /**
     * Checks if the map is not empty.
     *
     * @param map the map to check
     * @return true if the map is not null and not empty, false otherwise
     */
    public static boolean notEmpty(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    /**
     * Checks if the map is empty.
     *
     * @param map the map to check
     * @return true if the map is null or empty, false otherwise
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Checks if the map contains the specified key.
     *
     * @param map the map to check
     * @param key the key to find
     * @return true if the map contains the key, false otherwise
     */
    public static boolean containsKey(Map<?, ?> map, Object key) {
        return map != null && map.containsKey(key);
    }

    /**
     * Checks if the map does not contain the specified key.
     *
     * @param map the map to check
     * @param key the key to exclude
     * @return true if the map does not contain the key, false otherwise
     */
    public static boolean notContainsKey(Map<?, ?> map, Object key) {
        return map == null || !map.containsKey(key);
    }

    /**
     * Checks if the map contains the specified value.
     *
     * @param map   the map to check
     * @param value the value to find
     * @return true if the map contains the value, false otherwise
     */
    public static boolean containsValue(Map<?, ?> map, Object value) {
        return map != null && map.containsValue(value);
    }

    /**
     * Checks if the map size is within the specified range.
     *
     * @param map the map to check
     * @param min the minimum size (inclusive)
     * @param max the maximum size (inclusive)
     * @return true if the map size is within [min, max], false otherwise
     */
    public static boolean sizeBetween(Map<?, ?> map, int min, int max) {
        int size = (map == null) ? 0 : map.size();
        return size >= min && size <= max;
    }

    /**
     * Checks if the map size equals the expected size.
     *
     * @param map  the map to check
     * @param size the expected size
     * @return true if the map size equals the expected size, false otherwise
     */
    public static boolean sizeEquals(Map<?, ?> map, int size) {
        return map != null && map.size() == size;
    }
}
