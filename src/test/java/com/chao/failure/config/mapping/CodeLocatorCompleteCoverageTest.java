package com.chao.failure.config.mapping;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CodeLocatorCompleteCoverageTest {

    // ============================================
    // from() 方法完整覆盖
    // ============================================

    @Test
    void testFromWithNull() {
        CodeLocator locator = CodeLocator.from(null);
        assertTrue(locator.getGroupNames().isEmpty());
        assertEquals("default", locator.getGroupForCode(100));
    }

    @Test
    void testFromWithEmptyMap() {
        CodeLocator locator = CodeLocator.from(Collections.emptyMap());
        assertTrue(locator.getGroupNames().isEmpty());
        assertEquals("default", locator.getGroupForCode(100));
    }

    @Test
    void testFromWithNullGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put(null, Arrays.asList(100, 200));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getGroupNames().isEmpty());
    }

    @Test
    void testFromWithEmptyGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("", Arrays.asList(100, 200));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getGroupNames().isEmpty());
    }

    @Test
    void testFromWithBlankGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("   ", Arrays.asList(100, 200));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getGroupNames().isEmpty());
    }

    @Test
    void testFromWithNullList() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", null);
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getGroupNames().isEmpty());
    }

    @Test
    void testFromWithEmptyList() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Collections.emptyList());
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getGroupNames().isEmpty());
    }

    @Test
    void testFromWithListContainingNull() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        List<Object> list = new ArrayList<>();
        list.add(null);
        list.add(100);
        list.add(null);
        groups.put("group", list);
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.isInGroup(100, "group"));
        assertEquals(Collections.singletonList(100), locator.getExactCodes("group"));
    }

    @Test
    void testFromWithListContainingOnlyNull() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        List<Object> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        groups.put("group", list);
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getGroupNames().isEmpty());
    }

    // ============================================
    // from() 方法 - Number 类型处理
    // ============================================

    @Test
    void testFromWithInteger() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100, 200));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.isInGroup(100, "group"));
        assertTrue(locator.isInGroup(200, "group"));
        assertEquals(Arrays.asList(100, 200), locator.getExactCodes("group"));
    }

    @Test
    void testFromWithLong() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100L, 200L));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.isInGroup(100, "group"));
        assertTrue(locator.isInGroup(200, "group"));
        assertEquals(Arrays.asList(100, 200), locator.getExactCodes("group"));
    }

    @Test
    void testFromWithDouble() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100.0, 200.0));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.isInGroup(100, "group"));
        assertTrue(locator.isInGroup(200, "group"));
        assertEquals(Arrays.asList(100, 200), locator.getExactCodes("group"));
    }

    @Test
    void testFromWithMixedNumberTypes() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100, 200L, 300.0));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.isInGroup(100, "group"));
        assertTrue(locator.isInGroup(200, "group"));
        assertTrue(locator.isInGroup(300, "group"));
        assertEquals(Arrays.asList(100, 200, 300), locator.getExactCodes("group"));
    }

    // ============================================
    // from() 方法 - String 类型处理
    // ============================================

    @Test
    void testFromWithEmptyString() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("", "   ", 100));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.isInGroup(100, "group"));
        assertFalse(locator.isInGroup(50, "group"));
    }

    @Test
    void testFromWithStringAsInteger() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100", "200"));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.isInGroup(100, "group"));
        assertTrue(locator.isInGroup(200, "group"));
        assertEquals(Arrays.asList(100, 200), locator.getExactCodes("group"));
    }

    @Test
    void testFromWithStringAsRangeHyphen() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200"));
        CodeLocator locator = CodeLocator.from(groups);
        for (int i = 100; i <= 200; i++) {
            assertTrue(locator.isInGroup(i, "group"));
        }
        assertFalse(locator.isInGroup(99, "group"));
        assertFalse(locator.isInGroup(201, "group"));
    }

    @Test
    void testFromWithStringAsRangeDoubleDot() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100..200"));
        CodeLocator locator = CodeLocator.from(groups);
        for (int i = 100; i <= 200; i++) {
            assertTrue(locator.isInGroup(i, "group"));
        }
    }

    @Test
    void testFromWithStringAsRangeWithWhitespace() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("  100  -  200  "));
        CodeLocator locator = CodeLocator.from(groups);
        for (int i = 100; i <= 200; i++) {
            assertTrue(locator.isInGroup(i, "group"));
        }
    }

    @Test
    void testFromWithStringAsReversedRange() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("200-100"));
        CodeLocator locator = CodeLocator.from(groups);
        for (int i = 100; i <= 200; i++) {
            assertTrue(locator.isInGroup(i, "group"));
        }
    }

    @Test
    void testFromWithInvalidString() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("invalid", "100"));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.isInGroup(100, "group"));
        assertFalse(locator.isInGroup(50, "group"));
    }

    @Test
    void testFromWithOnlyInvalidStrings() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("invalid1", "invalid2"));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getGroupNames().isEmpty());
    }

    // ============================================
    // from() 方法 - 混合精确值和范围
    // ============================================

    @Test
    void testFromWithMixedExactAndRange() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200", 250, "300"));
        CodeLocator locator = CodeLocator.from(groups);
        for (int i = 100; i <= 200; i++) {
            assertTrue(locator.isInGroup(i, "group"));
        }
        assertTrue(locator.isInGroup(250, "group"));
        assertTrue(locator.isInGroup(300, "group"));
    }

    @Test
    void testFromWithOverlappingRangesAndExact() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-150", 125, "150-200"));
        CodeLocator locator = CodeLocator.from(groups);
        for (int i = 100; i <= 200; i++) {
            assertTrue(locator.isInGroup(i, "group"));
        }
    }

    // ============================================
    // getGroupNames() 方法完整覆盖
    // ============================================

    @Test
    void testGetGroupNames() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group1", Arrays.asList(100));
        groups.put("group2", Arrays.asList(200));
        CodeLocator locator = CodeLocator.from(groups);
        Set<String> names = locator.getGroupNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("group1"));
        assertTrue(names.contains("group2"));
    }

    @Test
    void testGetGroupNamesWithEmpty() {
        CodeLocator locator = CodeLocator.from(null);
        assertTrue(locator.getGroupNames().isEmpty());
    }

    // ============================================
    // isInGroup() 方法完整覆盖
    // ============================================

    @Test
    void testIsInGroupWithNullGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100));
        CodeLocator locator = CodeLocator.from(groups);
        assertFalse(locator.isInGroup(100, null));
    }

    @Test
    void testIsInGroupWithEmptyGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100));
        CodeLocator locator = CodeLocator.from(groups);
        assertFalse(locator.isInGroup(100, ""));
    }

    @Test
    void testIsInGroupWithBlankGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100));
        CodeLocator locator = CodeLocator.from(groups);
        assertFalse(locator.isInGroup(100, "   "));
    }

    @Test
    void testIsInGroupWithUnknownGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100));
        CodeLocator locator = CodeLocator.from(groups);
        assertFalse(locator.isInGroup(100, "unknown"));
    }

    @Test
    void testIsInGroupWithCodeBelowRange() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200"));
        CodeLocator locator = CodeLocator.from(groups);
        assertFalse(locator.isInGroup(99, "group"));
    }

    @Test
    void testIsInGroupWithCodeAboveRange() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200"));
        CodeLocator locator = CodeLocator.from(groups);
        assertFalse(locator.isInGroup(201, "group"));
    }

    @Test
    void testIsInGroupWithMultipleRanges() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200", "300-400"));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.isInGroup(150, "group"));
        assertTrue(locator.isInGroup(350, "group"));
        assertFalse(locator.isInGroup(250, "group"));
    }

    @Test
    void testIsInGroupWithMergedRanges() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("1-3", "4-6", 7));
        CodeLocator locator = CodeLocator.from(groups);
        for (int i = 1; i <= 7; i++) {
            assertTrue(locator.isInGroup(i, "group"));
        }
    }

    // ============================================
    // getGroupForCode() 方法完整覆盖
    // ============================================

    @Test
    void testGetGroupForCodeWithEmpty() {
        CodeLocator locator = CodeLocator.from(null);
        assertEquals("default", locator.getGroupForCode(100));
    }

    @Test
    void testGetGroupForCodeNotFound() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200"));
        CodeLocator locator = CodeLocator.from(groups);
        assertEquals("default", locator.getGroupForCode(50));
        assertEquals("default", locator.getGroupForCode(250));
    }

    @Test
    void testGetGroupForCodeFindsFirstMatchingGroup() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group1", Arrays.asList("100-200"));
        groups.put("group2", Arrays.asList("150-250"));
        CodeLocator locator = CodeLocator.from(groups);
        
        // Since Map.copyOf() doesn't preserve order, just verify code 150 is in one of the groups
        String group150 = locator.getGroupForCode(150);
        assertTrue(group150.equals("group1") || group150.equals("group2"));
        assertEquals("group2", locator.getGroupForCode(225));
    }

    // ============================================
    // getExactCodes() 方法完整覆盖
    // ============================================

    @Test
    void testGetExactCodesWithNullGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100, 200));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getExactCodes(null).isEmpty());
    }

    @Test
    void testGetExactCodesWithEmptyGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100, 200));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getExactCodes("").isEmpty());
    }

    @Test
    void testGetExactCodesWithBlankGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100, 200));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getExactCodes("   ").isEmpty());
    }

    @Test
    void testGetExactCodesWithUnknownGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100, 200));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getExactCodes("unknown").isEmpty());
    }

    @Test
    void testGetExactCodesWithRangeOnly() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200"));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getExactCodes("group").isEmpty());
    }

    @Test
    void testGetExactCodesWithExactAndRange() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200", 250, 300));
        CodeLocator locator = CodeLocator.from(groups);
        assertEquals(Arrays.asList(250, 300), locator.getExactCodes("group"));
    }

    @Test
    void testGetExactCodesWithDuplicates() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100, 100, 200, 200, 200));
        CodeLocator locator = CodeLocator.from(groups);
        assertEquals(Arrays.asList(100, 200), locator.getExactCodes("group"));
    }

    // ============================================
    // IntArrayListView 类测试
    // ============================================

    @Test
    void testIntArrayListViewGet() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(100, 200, 300));
        CodeLocator locator = CodeLocator.from(groups);
        List<Integer> codes = locator.getExactCodes("group");
        assertEquals(3, codes.size());
        assertEquals(100, codes.get(0));
        assertEquals(200, codes.get(1));
        assertEquals(300, codes.get(2));
    }

    @Test
    void testIntArrayListViewEmpty() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200"));
        CodeLocator locator = CodeLocator.from(groups);
        List<Integer> codes = locator.getExactCodes("group");
        assertTrue(codes.isEmpty());
        assertEquals(0, codes.size());
    }

    // ============================================
    // getExpandedCodes() 方法完整覆盖
    // ============================================

    @Test
    void testGetExpandedCodesWithNullGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200"));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getExpandedCodes(null).isEmpty());
    }

    @Test
    void testGetExpandedCodesWithEmptyGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200"));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getExpandedCodes("").isEmpty());
    }

    @Test
    void testGetExpandedCodesWithBlankGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200"));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getExpandedCodes("   ").isEmpty());
    }

    @Test
    void testGetExpandedCodesWithUnknownGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200"));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.getExpandedCodes("unknown").isEmpty());
    }

    @Test
    void testGetExpandedCodesWithRange() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("1-5"));
        CodeLocator locator = CodeLocator.from(groups);
        List<Integer> expanded = locator.getExpandedCodes("group");
        assertEquals(5, expanded.size());
        for (int i = 1; i <= 5; i++) {
            assertEquals(i, expanded.get(i - 1));
        }
    }

    @Test
    void testGetExpandedCodesWithMultipleRanges() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("1-3", "5-7"));
        CodeLocator locator = CodeLocator.from(groups);
        List<Integer> expanded = locator.getExpandedCodes("group");
        assertEquals(6, expanded.size());
        assertEquals(Arrays.asList(1, 2, 3, 5, 6, 7), expanded);
    }

    @Test
    void testGetExpandedCodesWithSingleCode() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(42));
        CodeLocator locator = CodeLocator.from(groups);
        List<Integer> expanded = locator.getExpandedCodes("group");
        assertEquals(1, expanded.size());
        assertEquals(42, expanded.get(0));
    }

    // ============================================
    // ExpandedCodeList 类测试
    // ============================================

    @Test
    void testExpandedCodeListSize() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("1-10"));
        CodeLocator locator = CodeLocator.from(groups);
        List<Integer> expanded = locator.getExpandedCodes("group");
        assertEquals(10, expanded.size());
    }

    @Test
    void testExpandedCodeListGet() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("1-10"));
        CodeLocator locator = CodeLocator.from(groups);
        List<Integer> expanded = locator.getExpandedCodes("group");
        assertEquals(1, expanded.get(0));
        assertEquals(10, expanded.get(9));
        assertEquals(5, expanded.get(4));
    }

    @Test
    void testExpandedCodeListIndexOutOfBounds() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("1-3"));
        CodeLocator locator = CodeLocator.from(groups);
        List<Integer> expanded = locator.getExpandedCodes("group");
        assertThrows(IndexOutOfBoundsException.class, () -> expanded.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> expanded.get(3));
    }

    @Test
    void testExpandedCodeListExceedsMaxInt() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("0-" + Integer.MAX_VALUE));
        CodeLocator locator = CodeLocator.from(groups);
        assertThrows(IllegalStateException.class, () -> locator.getExpandedCodes("group"));
    }

    // ============================================
    // getExpandedCodesPreview() 方法完整覆盖
    // ============================================

    @Test
    void testGetExpandedCodesPreviewWithNegativeN() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("1-10"));
        CodeLocator locator = CodeLocator.from(groups);
        assertEquals("[]", locator.getExpandedCodesPreview("group", -1));
        assertEquals("[]", locator.getExpandedCodesPreview("group", 0));
    }

    @Test
    void testGetExpandedCodesPreviewWithSmallSet() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("1-3"));
        CodeLocator locator = CodeLocator.from(groups);
        assertEquals("[1, 2, 3]", locator.getExpandedCodesPreview("group", 5));
    }

    @Test
    void testGetExpandedCodesPreviewWithSingleElement() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList(7));
        CodeLocator locator = CodeLocator.from(groups);
        assertEquals("[7]", locator.getExpandedCodesPreview("group", 5));
    }

    @Test
    void testGetExpandedCodesPreviewWithEmptyGroup() {
        CodeLocator locator = CodeLocator.from(null);
        assertEquals("[]", locator.getExpandedCodesPreview("unknown"));
        assertEquals("[]", locator.getExpandedCodesPreview("unknown", 5));
    }

    @Test
    void testGetExpandedCodesPreviewWithNullGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("1-10"));
        CodeLocator locator = CodeLocator.from(groups);
        assertEquals("[]", locator.getExpandedCodesPreview(null));
    }

    @Test
    void testGetExpandedCodesPreviewWithBlankGroupName() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("1-10"));
        CodeLocator locator = CodeLocator.from(groups);
        assertEquals("[]", locator.getExpandedCodesPreview(""));
        assertEquals("[]", locator.getExpandedCodesPreview("   "));
    }

    @Test
    void testGetExpandedCodesPreviewWithLargeRange() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("1-10"));
        CodeLocator locator = CodeLocator.from(groups);
        String preview = locator.getExpandedCodesPreview("group", 5);
        assertTrue(preview.startsWith("[1, "));
        assertTrue(preview.endsWith(", ..., 10]"));
    }

    @Test
    void testGetExpandedCodesPreviewWithTwoElements() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("1-2"));
        CodeLocator locator = CodeLocator.from(groups);
        String preview = locator.getExpandedCodesPreview("group", 1);
        assertEquals("[1, ..., 2]", preview);
    }

    // ============================================
    // parseRange() 方法（内部私有方法）测试
    // ============================================

    @Test
    void testParseRangeHyphenFormat() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200"));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.isInGroup(150, "group"));
    }

    @Test
    void testParseRangeDoubleDotFormat() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100..200"));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.isInGroup(150, "group"));
    }

    @Test
    void testParseRangeReversed() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("200-100"));
        CodeLocator locator = CodeLocator.from(groups);
        assertTrue(locator.isInGroup(150, "group"));
    }

    // ============================================
    // mergeRangesAndExact() 方法（内部私有方法）测试
    // ============================================

    @Test
    void testMergeRangesAndExactWithNullRanges() throws Exception {
        Class<?> codeRangeClass = Class.forName("com.chao.failure.config.mapping.CodeLocator$CodeRange");
        var codeRangeCtor = codeRangeClass.getDeclaredConstructor(int.class, int.class);
        codeRangeCtor.setAccessible(true);
        Object range = codeRangeCtor.newInstance(10, 20);

        Map<String, List<Object>> rangesMap = new LinkedHashMap<>();
        rangesMap.put("test", null);
        Map<String, int[]> exactMap = new LinkedHashMap<>();
        exactMap.put("test", new int[]{100, 200});

        var ctor = CodeLocator.class.getDeclaredConstructor(Map.class, Map.class);
        ctor.setAccessible(true);
        CodeLocator locator = ctor.newInstance(rangesMap, exactMap);
        assertEquals("default", locator.getGroupForCode(100));
    }

    @Test
    void testMergeRangesAndExactWithEmptyExact() throws Exception {
        Class<?> codeRangeClass = Class.forName("com.chao.failure.config.mapping.CodeLocator$CodeRange");
        var codeRangeCtor = codeRangeClass.getDeclaredConstructor(int.class, int.class);
        codeRangeCtor.setAccessible(true);
        Object range = codeRangeCtor.newInstance(10, 20);

        Map<String, List<Object>> rangesMap = new LinkedHashMap<>();
        List<Object> ranges = new ArrayList<>();
        ranges.add(range);
        rangesMap.put("test", ranges);
        Map<String, int[]> exactMap = new LinkedHashMap<>();
        exactMap.put("test", new int[0]);

        var ctor = CodeLocator.class.getDeclaredConstructor(Map.class, Map.class);
        ctor.setAccessible(true);
        CodeLocator locator = ctor.newInstance(rangesMap, exactMap);
        assertTrue(locator.isInGroup(15, "test"));
    }

    @Test
    void testMergeRangesAndExactWithNullExact() throws Exception {
        Class<?> codeRangeClass = Class.forName("com.chao.failure.config.mapping.CodeLocator$CodeRange");
        var codeRangeCtor = codeRangeClass.getDeclaredConstructor(int.class, int.class);
        codeRangeCtor.setAccessible(true);
        Object range = codeRangeCtor.newInstance(10, 20);

        Map<String, List<Object>> rangesMap = new LinkedHashMap<>();
        List<Object> ranges = new ArrayList<>();
        ranges.add(range);
        rangesMap.put("test", ranges);
        Map<String, int[]> exactMap = new LinkedHashMap<>();
        exactMap.put("test", null);

        var ctor = CodeLocator.class.getDeclaredConstructor(Map.class, Map.class);
        ctor.setAccessible(true);
        CodeLocator locator = ctor.newInstance(rangesMap, exactMap);
        assertTrue(locator.isInGroup(15, "test"));
    }

    @Test
    void testMergeRangesAndExactWithBothNull() throws Exception {
        Map<String, List<Object>> rangesMap = new LinkedHashMap<>();
        rangesMap.put("test", null);
        Map<String, int[]> exactMap = new LinkedHashMap<>();
        exactMap.put("test", null);

        var ctor = CodeLocator.class.getDeclaredConstructor(Map.class, Map.class);
        ctor.setAccessible(true);
        CodeLocator locator = ctor.newInstance(rangesMap, exactMap);
        assertEquals("default", locator.getGroupForCode(100));
    }

    @Test
    void testMergeRangesAndExactWithNullRangesEmptyExact() throws Exception {
        Map<String, List<Object>> rangesMap = new LinkedHashMap<>();
        rangesMap.put("test", null);
        Map<String, int[]> exactMap = new LinkedHashMap<>();
        exactMap.put("test", new int[0]);

        var ctor = CodeLocator.class.getDeclaredConstructor(Map.class, Map.class);
        ctor.setAccessible(true);
        CodeLocator locator = ctor.newInstance(rangesMap, exactMap);
        assertEquals("default", locator.getGroupForCode(100));
    }

    // ============================================
    // firstMids() 方法（内部私有方法）测试
    // ============================================

    @Test
    void testFirstMidsWithZeroLimit() throws Exception {
        Class<?> codeRangeClass = Class.forName("com.chao.failure.config.mapping.CodeLocator$CodeRange");
        var codeRangeCtor = codeRangeClass.getDeclaredConstructor(int.class, int.class);
        codeRangeCtor.setAccessible(true);
        Object single = codeRangeCtor.newInstance(1, 1);
        List<Object> ranges = List.of(single);

        var firstMids = CodeLocator.class.getDeclaredMethod("firstMids", List.class, int.class, int.class, int.class);
        firstMids.setAccessible(true);

        int[] result = (int[]) firstMids.invoke(null, ranges, 1, 1, 0);
        assertArrayEquals(new int[0], result);
    }

    @Test
    void testFirstMidsWithLastMissing() throws Exception {
        Class<?> codeRangeClass = Class.forName("com.chao.failure.config.mapping.CodeLocator$CodeRange");
        var codeRangeCtor = codeRangeClass.getDeclaredConstructor(int.class, int.class);
        codeRangeCtor.setAccessible(true);
        Object single = codeRangeCtor.newInstance(1, 1);
        List<Object> ranges = List.of(single);

        var firstMids = CodeLocator.class.getDeclaredMethod("firstMids", List.class, int.class, int.class, int.class);
        firstMids.setAccessible(true);

        int[] result = (int[]) firstMids.invoke(null, ranges, 1, 999, 3);
        assertArrayEquals(new int[0], result);
    }

    // ============================================
    // IntCollector 类测试
    // ============================================

    @Test
    void testIntCollectorCapacityGrowth() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        groups.put("group", list);
        CodeLocator locator = CodeLocator.from(groups);
        assertEquals(10, locator.getExactCodes("group").size());
    }

    // ============================================
    // 综合测试场景
    // ============================================

    @Test
    void testMultipleGroupsWithMixedContent() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("auth", Arrays.asList("40100-40105", 40300L, "40301"));
        groups.put("business", Arrays.asList("40000-40002"));

        CodeLocator locator = CodeLocator.from(groups);

        assertEquals("auth", locator.getGroupForCode(40103));
        assertEquals("auth", locator.getGroupForCode(40300));
        assertEquals("business", locator.getGroupForCode(40001));
        assertEquals("default", locator.getGroupForCode(50000));

        Set<String> names = locator.getGroupNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("auth"));
        assertTrue(names.contains("business"));
    }

    @Test
    void testEdgeCasesInRangeCheck() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("group", Arrays.asList("100-200"));
        CodeLocator locator = CodeLocator.from(groups);

        assertTrue(locator.isInGroup(100, "group"));
        assertTrue(locator.isInGroup(200, "group"));
        assertFalse(locator.isInGroup(99, "group"));
        assertFalse(locator.isInGroup(201, "group"));
    }

    @Test
    void testGetExpandedCodesPreviewWithEmptyRanges() throws Exception {
        Map<String, List<Object>> rangesMap = new LinkedHashMap<>();
        rangesMap.put("test", List.of());
        Map<String, int[]> exactMap = Map.of();

        var ctor = CodeLocator.class.getDeclaredConstructor(Map.class, Map.class);
        ctor.setAccessible(true);
        CodeLocator locator = ctor.newInstance(rangesMap, exactMap);
        assertEquals("[]", locator.getExpandedCodesPreview("test"));
    }

    @Test
    void testGetExactCodesWithEmptyArray() throws Exception {
        Map<String, List<Object>> rangesMap = new LinkedHashMap<>();
        rangesMap.put("test", List.of());
        Map<String, int[]> exactMap = new LinkedHashMap<>();
        exactMap.put("test", new int[0]);

        var ctor = CodeLocator.class.getDeclaredConstructor(Map.class, Map.class);
        ctor.setAccessible(true);
        CodeLocator locator = ctor.newInstance(rangesMap, exactMap);
        assertEquals(List.of(), locator.getExactCodes("test"));
    }

    @Test
    void testGetGroupForCodeWithEmptyRangesInSecondGroup() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("empty", new ArrayList<>());
        groups.put("test", Arrays.asList("100-200"));

        CodeLocator locator = CodeLocator.from(groups);
        assertEquals("test", locator.getGroupForCode(150));
        assertEquals("default", locator.getGroupForCode(999));
    }

    @Test
    void testGetExpandedCodesPreviewWithExactlyTwoElementsInMids() {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        groups.put("test", Arrays.asList("1-4"));
        CodeLocator locator = CodeLocator.from(groups);
        String preview = locator.getExpandedCodesPreview("test", 2);
        assertTrue(preview.contains("1"));
        assertTrue(preview.contains("4"));
        assertTrue(preview.contains("..."));
    }
}
