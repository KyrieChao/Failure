package com.chao.failure.config.mapping;

import com.chao.failure.constant.FailureConst;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
/**
 * CodeLocator is a utility class that resolves and locates failure codes based on group configurations.
 * It parses a map of groups (containing exact codes or ranges like "100-200") and provides
 * methods to check code membership, find group names, and expand code ranges.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public final class CodeLocator {

    private final Map<String, List<CodeRange>> groupRanges;
    private final Map<String, List<Integer>> groupExactCodes;

    private CodeLocator(Map<String, List<CodeRange>> groupRanges, Map<String, List<Integer>> groupExactCodes) {
        this.groupRanges = groupRanges;
        this.groupExactCodes = groupExactCodes;
    }

    /**
     * Creates a CodeLocator instance from the provided groups map.
     *
     * @param groups A map where keys are group names and values are lists of objects representing code ranges or exact codes
     * @return A new CodeLocator instance initialized with the parsed group information
     */
    public static CodeLocator from(Map<String, List<Object>> groups) {
        if (groups == null || groups.isEmpty()) {
            return new CodeLocator(Map.of(), Map.of());
        }

        LinkedHashMap<String, List<CodeRange>> rangesMap = new LinkedHashMap<>();
        LinkedHashMap<String, List<Integer>> exactMap = new LinkedHashMap<>();

        for (Map.Entry<String, List<Object>> entry : groups.entrySet()) {
            String groupName = entry.getKey();
            List<Object> rawList = entry.getValue();
            if (groupName == null || groupName.isBlank() || rawList == null || rawList.isEmpty()) {
                continue;
            }

            ArrayList<CodeRange> ranges = new ArrayList<>();
            ArrayList<Integer> exact = new ArrayList<>();

            for (Object raw : rawList) {
                if (raw instanceof Number num) {
                    int code = num.intValue();
                    exact.add(code);
                    ranges.add(new CodeRange(code, code));
                    continue;
                }
                if (raw instanceof String str) {
                    String s = str.trim();
                    if (s.isEmpty()) {
                        continue;
                    }
                    CodeRange r = parseRange(s);
                    if (r != null) {
                        ranges.add(r);
                        continue;
                    }
                    Integer v = parseIntOrNull(s);
                    if (v != null) {
                        exact.add(v);
                        ranges.add(new CodeRange(v, v));
                    }
                }
            }

            if (!ranges.isEmpty()) {
                rangesMap.put(groupName, List.copyOf(ranges));
            }
            if (!exact.isEmpty()) {
                exactMap.put(groupName, List.copyOf(exact));
            }
        }

        return new CodeLocator(Map.copyOf(rangesMap), Map.copyOf(exactMap));
    }

    /**
     * Gets all available group names in this locator.
     *
     * @return A set containing all group names
     */
    public Set<String> getGroupNames() {
        return groupRanges.keySet();
    }

    /**
     * Checks if a given code belongs to the specified group.
     *
     * @param code The code to check
     * @param groupName The name of the group to check against
     * @return true if the code is within any range of the specified group, false otherwise
     */
    public boolean isInGroup(int code, String groupName) {
        if (groupName == null || groupName.isBlank()) {
            return false;
        }
        List<CodeRange> ranges = groupRanges.get(groupName);
        if (ranges == null || ranges.isEmpty()) {
            return false;
        }
        for (CodeRange r : ranges) {
            if (code >= r.start && code <= r.end) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the group that contains the given code.
     *
     * @param code The code to find the group for
     * @return The name of the group containing the code, or "default" if no group contains it
     */
    public String getGroupForCode(int code) {
        if (groupRanges.isEmpty()) {
            return "default";
        }
        for (Map.Entry<String, List<CodeRange>> entry : groupRanges.entrySet()) {
            List<CodeRange> ranges = entry.getValue();
            if (ranges == null || ranges.isEmpty()) {
                continue;
            }
            for (CodeRange r : ranges) {
                if (code >= r.start && code <= r.end) {
                    return entry.getKey();
                }
            }
        }
        return "default";
    }

    /**
     * Gets the exact codes defined for a specific group.
     *
     * @param groupName The name of the group
     * @return A list of exact codes for the group, or an empty list if the group doesn't exist
     */
    public List<Integer> getExactCodes(String groupName) {
        if (groupName == null || groupName.isBlank()) {
            return List.of();
        }
        return groupExactCodes.getOrDefault(groupName, List.of());
    }

    /**
     * Gets all expanded codes for a specific group, including both exact codes and ranges.
     *
     * @param groupName The name of the group
     * @return A sorted list of all codes in the group (expanded from ranges), or an empty list if the group doesn't exist
     */
    public List<Integer> getExpandedCodes(String groupName) {
        SortedSet<Integer> expanded = expandToSortedSet(groupName);
        if (expanded.isEmpty()) {
            return List.of();
        }
        return List.copyOf(expanded);
    }

    /**
     * Gets a preview string representation of the expanded codes for a group, showing up to 5 codes.
     *
     * @param groupName The name of the group
     * @return A string representation of the first few codes in the group
     */
    public String getExpandedCodesPreview(String groupName) {
        return getExpandedCodesPreview(groupName, 5);
    }

    /**
     * Gets a preview string representation of the expanded codes for a group, showing up to n codes.
     *
     * @param groupName The name of the group
     * @param n The maximum number of codes to show in the preview
     * @return A formatted string showing a subset of codes from the group
     */
    public String getExpandedCodesPreview(String groupName, int n) {
        if (n <= 0) {
            return "[]";
        }
        NavigableSet<Integer> expanded = expandToSortedSet(groupName);
        if (expanded.isEmpty()) {
            return "[]";
        }
        Integer first = expanded.first();
        Integer last = expanded.last();

        ArrayList<Integer> mids = new ArrayList<>(3);
        for (Integer v : expanded.tailSet(first, false)) {
            if (Objects.equals(v, last)) {
                break;
            }
            mids.add(v);
            if (mids.size() >= 3) {
                break;
            }
        }
        if (expanded.size() <= n) {
            return expanded.toString();
        }
        if (mids.isEmpty()) {
            return String.format("[%s, ..., %s]", first, last);
        }
        String middle = mids.stream().map(String::valueOf).collect(Collectors.joining(", "));
        return String.format("[%s, %s, ..., %s]", first, middle, last);
    }

    /**
     * Expands all code ranges for a group into a sorted set of individual codes.
     *
     * @param groupName The name of the group
     * @return A sorted set containing all individual codes from the group's ranges
     */
    private NavigableSet<Integer> expandToSortedSet(String groupName) {
        if (groupName == null || groupName.isBlank()) {
            return new TreeSet<>();
        }
        List<CodeRange> ranges = groupRanges.get(groupName);
        if (ranges == null || ranges.isEmpty()) {
            return new TreeSet<>();
        }
        TreeSet<Integer> expanded = new TreeSet<>();
        for (CodeRange r : ranges) {
            for (int i = r.start; i <= r.end; i++) {
                expanded.add(i);
            }
        }
        return expanded;
    }

    /**
     * Parses a string representation of a code range into a CodeRange object.
     *
     * @param input The string to parse (e.g., "100-200" or "[100,200]")
     * @return A CodeRange object if parsing succeeds, null otherwise
     */
    private static CodeRange parseRange(String input) {
        Matcher matcher = FailureConst.Range.matcher(input);
        if (!matcher.matches()) {
            return null;
        }
        int start = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : Integer.parseInt(matcher.group(3));
        int end = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : Integer.parseInt(matcher.group(4));
        return new CodeRange(Math.min(start, end), Math.max(start, end));
    }

    /**
     * Attempts to parse a string as an integer.
     *
     * @param s The string to parse
     * @return The parsed integer value, or null if parsing fails
     */
    private static Integer parseIntOrNull(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Represents a range of codes with start and end values.
     *
     * @param start The starting code value (inclusive)
     * @param end The ending code value (inclusive)
     */
    private record CodeRange(int start, int end) {
    }
}

