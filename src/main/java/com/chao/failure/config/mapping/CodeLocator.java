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
    private final Map<String, int[]> groupExactCodes;

    private CodeLocator(Map<String, List<CodeRange>> groupRanges, Map<String, int[]> groupExactCodes) {
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
        LinkedHashMap<String, int[]> exactMap = new LinkedHashMap<>();

        for (Map.Entry<String, List<Object>> entry : groups.entrySet()) {
            String groupName = entry.getKey();
            List<Object> rawList = entry.getValue();
            if (groupName == null || groupName.isBlank() || rawList == null || rawList.isEmpty()) {
                continue;
            }

            ArrayList<CodeRange> inputRanges = new ArrayList<>();
            IntCollector exact = new IntCollector();

            for (Object raw : rawList) {
                if (raw instanceof Number num) {
                    int code = num.intValue();
                    exact.add(code);
                    continue;
                }
                if (raw instanceof String str) {
                    String s = str.trim();
                    if (s.isEmpty()) {
                        continue;
                    }
                    CodeRange r = parseRange(s);
                    if (r != null) {
                        inputRanges.add(r);
                        continue;
                    }
                    Integer v = parseIntOrNull(s);
                    if (v != null) {
                        exact.add(v);
                    }
                }
            }

            int[] exactCodes = exact.toSortedUniqueArray();
            List<CodeRange> merged = mergeRangesAndExact(inputRanges, exactCodes);
            if (!merged.isEmpty()) {
                rangesMap.put(groupName, merged);
            }
            if (exactCodes.length > 0) {
                exactMap.put(groupName, exactCodes);
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
        return isInRanges(code, ranges);
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
            if (isInRanges(code, ranges)) {
                return entry.getKey();
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
        int[] exact = groupExactCodes.get(groupName);
        if (exact == null || exact.length == 0) {
            return List.of();
        }
        return new IntArrayListView(exact);
    }

    /**
     * Gets all expanded codes for a specific group, including both exact codes and ranges.
     *
     * @param groupName The name of the group
     * @return A sorted list of all codes in the group (expanded from ranges), or an empty list if the group doesn't exist
     */
    public List<Integer> getExpandedCodes(String groupName) {
        if (groupName == null || groupName.isBlank()) {
            return List.of();
        }
        List<CodeRange> ranges = groupRanges.get(groupName);
        if (ranges == null || ranges.isEmpty()) {
            return List.of();
        }
        return new ExpandedCodeList(ranges);
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
        if (groupName == null || groupName.isBlank()) {
            return "[]";
        }
        List<CodeRange> ranges = groupRanges.get(groupName);
        if (ranges == null || ranges.isEmpty()) {
            return "[]";
        }
        int first = ranges.get(0).start;
        int last = ranges.get(ranges.size() - 1).end;

        long total = totalSize(ranges);
        if (total <= n) {
            return buildAllCodesString(ranges);
        }
        int[] mids = firstMids(ranges, first, last, 3);
        if (mids.length == 0) {
            return String.format("[%s, ..., %s]", first, last);
        }
        String middle = Arrays.stream(mids).mapToObj(String::valueOf).collect(Collectors.joining(", "));
        return String.format("[%s, %s, ..., %s]", first, middle, last);
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

    private static boolean isInRanges(int code, List<CodeRange> ranges) {
        int lo = 0;
        int hi = ranges.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            CodeRange r = ranges.get(mid);
            if (code < r.start) {
                hi = mid - 1;
            } else if (code > r.end) {
                lo = mid + 1;
            } else {
                return true;
            }
        }
        return false;
    }

    private static List<CodeRange> mergeRangesAndExact(List<CodeRange> ranges, int[] exactCodes) {
        if ((ranges == null || ranges.isEmpty()) && (exactCodes == null || exactCodes.length == 0)) {
            return List.of();
        }

        ArrayList<CodeRange> all = new ArrayList<>((ranges != null ? ranges.size() : 0) + (exactCodes != null ? exactCodes.length : 0));
        if (ranges != null && !ranges.isEmpty()) {
            all.addAll(ranges);
        }
        if (exactCodes != null && exactCodes.length > 0) {
            for (int v : exactCodes) {
                all.add(new CodeRange(v, v));
            }
        }

        all.sort(Comparator.comparingInt(CodeRange::start).thenComparingInt(CodeRange::end));
        ArrayList<CodeRange> merged = new ArrayList<>(all.size());
        CodeRange cur = all.get(0);
        int start = cur.start;
        int end = cur.end;
        for (int i = 1; i < all.size(); i++) {
            CodeRange r = all.get(i);
            if (r.start <= end + 1) {
                end = Math.max(end, r.end);
                continue;
            }
            merged.add(new CodeRange(start, end));
            start = r.start;
            end = r.end;
        }
        merged.add(new CodeRange(start, end));
        return List.copyOf(merged);
    }

    private static long totalSize(List<CodeRange> ranges) {
        long total = 0;
        for (CodeRange r : ranges) {
            total += (long) r.end - (long) r.start + 1L;
        }
        return total;
    }

    private static String buildAllCodesString(List<CodeRange> ranges) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (CodeRange r : ranges) {
            for (int i = r.start; i <= r.end; i++) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(i);
                first = false;
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private static int[] firstMids(List<CodeRange> ranges, int first, int last, int limit) {
        if (limit <= 0) {
            return new int[0];
        }
        int[] out = new int[Math.min(3, limit)];
        int count = 0;
        boolean skippedFirst = false;
        for (CodeRange r : ranges) {
            for (int i = r.start; i <= r.end; i++) {
                if (!skippedFirst) {
                    if (i == first) {
                        skippedFirst = true;
                    }
                    continue;
                }
                if (i == last) {
                    return Arrays.copyOf(out, count);
                }
                out[count++] = i;
                if (count >= out.length) {
                    return out;
                }
            }
        }
        return Arrays.copyOf(out, count);
    }

    private static final class IntCollector {
        private int[] a;
        private int size;

        private void add(int v) {
            if (a == null) {
                a = new int[8];
            } else if (size >= a.length) {
                a = Arrays.copyOf(a, a.length * 2);
            }
            a[size++] = v;
        }

        private int[] toSortedUniqueArray() {
            if (size <= 0) {
                return new int[0];
            }
            int[] out = Arrays.copyOf(a, size);
            Arrays.sort(out);
            int w = 1;
            for (int i = 1; i < out.length; i++) {
                if (out[i] != out[w - 1]) {
                    out[w++] = out[i];
                }
            }
            return w == out.length ? out : Arrays.copyOf(out, w);
        }
    }

    private static final class IntArrayListView extends AbstractList<Integer> implements RandomAccess {
        private final int[] a;

        private IntArrayListView(int[] a) {
            this.a = a;
        }

        @Override
        public Integer get(int index) {
            return a[index];
        }

        @Override
        public int size() {
            return a.length;
        }
    }

    private static final class ExpandedCodeList extends AbstractList<Integer> implements RandomAccess {
        private final int[] starts;
        private final int[] ends;
        private final int[] prefix;
        private final int size;

        private ExpandedCodeList(List<CodeRange> ranges) {
            int m = ranges.size();
            this.starts = new int[m];
            this.ends = new int[m];
            this.prefix = new int[m + 1];
            long total = 0;
            for (int i = 0; i < m; i++) {
                CodeRange r = ranges.get(i);
                starts[i] = r.start;
                ends[i] = r.end;
                long len = (long) r.end - (long) r.start + 1L;
                total += len;
                if (total > Integer.MAX_VALUE) {
                    throw new IllegalStateException("Expanded codes exceed List limit: " + total);
                }
                prefix[i + 1] = (int) total;
            }
            this.size = (int) total;
        }

        @Override
        public Integer get(int index) {
            Objects.checkIndex(index, size);
            int target = index + 1;
            int seg = Arrays.binarySearch(prefix, target);
            if (seg < 0) {
                seg = -seg - 2;
            } else {
                seg = Math.max(0, seg - 1);
            }
            int offset = index - prefix[seg];
            return starts[seg] + offset;
        }

        @Override
        public int size() {
            return size;
        }
    }
}

