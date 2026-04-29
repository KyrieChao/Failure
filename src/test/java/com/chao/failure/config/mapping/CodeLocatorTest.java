package com.chao.failure.config.mapping;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CodeLocatorTest {

    @Test
    void fromNullGroups() {
        CodeLocator groups = CodeLocator.from(null);
        assertTrue(groups.getGroupNames().isEmpty());
        assertEquals("default", groups.getGroupForCode(40001));
        assertFalse(groups.isInGroup(40001, "client"));
        assertEquals(List.of(), groups.getExactCodes("client"));
        assertEquals(List.of(), groups.getExpandedCodes("client"));
        assertEquals("[]", groups.getExpandedCodesPreview("client"));
    }

    @Test
    void fromEmptyGroups() {
        CodeLocator groups = CodeLocator.from(new LinkedHashMap<>());
        assertTrue(groups.getGroupNames().isEmpty());
    }

    @Test
    void fromWithInvalidGroupNames() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put(null, new ArrayList<>(List.of(100)));
        raw.put("", new ArrayList<>(List.of(200)));
        raw.put("  ", new ArrayList<>(List.of(300)));
        raw.put("valid", new ArrayList<>(List.of(400)));

        CodeLocator groups = CodeLocator.from(raw);

        Set<String> names = groups.getGroupNames();
        assertEquals(1, names.size());
        assertTrue(names.contains("valid"));
        assertTrue(groups.isInGroup(400, "valid"));
    }

    @Test
    void fromWithNullAndEmptyLists() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("group1", null);
        raw.put("group2", new ArrayList<>());
        raw.put("group3", new ArrayList<>(List.of(100)));

        CodeLocator groups = CodeLocator.from(raw);

        assertEquals(1, groups.getGroupNames().size());
        assertTrue(groups.isInGroup(100, "group3"));
    }

    @Test
    void fromWithNumberTypes() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("test", new ArrayList<>(List.of(100, 200L, 300.0)));

        CodeLocator groups = CodeLocator.from(raw);

        assertTrue(groups.isInGroup(100, "test"));
        assertTrue(groups.isInGroup(200, "test"));
        assertTrue(groups.isInGroup(300, "test"));
        assertEquals(List.of(100, 200, 300), groups.getExactCodes("test"));
    }

    @Test
    void fromWithStringValues() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("test", new ArrayList<>(List.of("100", "  200  ", "invalid")));

        CodeLocator groups = CodeLocator.from(raw);

        assertTrue(groups.isInGroup(100, "test"));
        assertTrue(groups.isInGroup(200, "test"));
        assertFalse(groups.isInGroup(300, "test"));
        assertEquals(List.of(100, 200), groups.getExactCodes("test"));
    }

    @Test
    void fromWithEmptyString() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("test", new ArrayList<>(List.of("", "  ", 100)));

        CodeLocator groups = CodeLocator.from(raw);

        assertTrue(groups.isInGroup(100, "test"));
        assertEquals(List.of(100), groups.getExactCodes("test"));
    }

    @Test
    void fromWithRangePatterns() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("range1", new ArrayList<>(List.of("100-200")));
        raw.put("range2", new ArrayList<>(List.of("300..400")));
        raw.put("range3", new ArrayList<>(List.of("  500 -  600  ")));
        raw.put("range4", new ArrayList<>(List.of("  700  ..  800  ")));

        CodeLocator groups = CodeLocator.from(raw);

        assertTrue(groups.isInGroup(100, "range1"));
        assertTrue(groups.isInGroup(150, "range1"));
        assertTrue(groups.isInGroup(200, "range1"));
        assertFalse(groups.isInGroup(99, "range1"));
        assertFalse(groups.isInGroup(201, "range1"));

        assertTrue(groups.isInGroup(350, "range2"));
        assertTrue(groups.isInGroup(550, "range3"));
        assertTrue(groups.isInGroup(750, "range4"));
    }

    @Test
    void fromWithReversedRange() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("test", new ArrayList<>(List.of("200-100")));

        CodeLocator groups = CodeLocator.from(raw);

        assertTrue(groups.isInGroup(100, "test"));
        assertTrue(groups.isInGroup(150, "test"));
        assertTrue(groups.isInGroup(200, "test"));
    }

    @Test
    void exactAndRangeParsing() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("client", new ArrayList<>(List.of("40000-40002", 40010, "40011", "bad")));
        raw.put("server", new ArrayList<>(List.of("50000..50001")));

        CodeLocator groups = CodeLocator.from(raw);

        assertTrue(groups.isInGroup(40000, "client"));
        assertTrue(groups.isInGroup(40002, "client"));
        assertFalse(groups.isInGroup(40003, "client"));

        assertEquals("client", groups.getGroupForCode(40001));
        assertEquals("server", groups.getGroupForCode(50001));
        assertEquals("default", groups.getGroupForCode(12345));

        assertEquals(List.of(40010, 40011), groups.getExactCodes("client"));
        assertEquals(List.of(40000, 40001, 40002, 40010, 40011), groups.getExpandedCodes("client"));
        assertEquals(List.of(50000, 50001), groups.getExpandedCodes("server"));
    }

    @Test
    void isInGroupWithNullAndBlankGroupName() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("client", new ArrayList<>(List.of("40000-40002")));
        CodeLocator groups = CodeLocator.from(raw);

        assertFalse(groups.isInGroup(40001, null));
        assertFalse(groups.isInGroup(40001, ""));
        assertFalse(groups.isInGroup(40001, "  "));
        assertFalse(groups.isInGroup(40001, "unknown"));
    }

    @Test
    void getGroupNames() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("group1", new ArrayList<>(List.of(100)));
        raw.put("group2", new ArrayList<>(List.of(200)));

        CodeLocator groups = CodeLocator.from(raw);

        Set<String> names = groups.getGroupNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("group1"));
        assertTrue(names.contains("group2"));
    }

    @Test
    void getGroupForCodeWithEmptyGroups() {
        CodeLocator groups = CodeLocator.from(null);
        assertEquals("default", groups.getGroupForCode(100));
    }

    @Test
    void getGroupForCodeNotFound() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("group1", new ArrayList<>(List.of("100-200")));
        CodeLocator groups = CodeLocator.from(raw);

        assertEquals("default", groups.getGroupForCode(50));
        assertEquals("default", groups.getGroupForCode(250));
    }

    @Test
    void getExactCodesWithNullAndBlankGroupName() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("group1", new ArrayList<>(List.of(100, 200)));
        CodeLocator groups = CodeLocator.from(raw);

        assertEquals(List.of(), groups.getExactCodes(null));
        assertEquals(List.of(), groups.getExactCodes(""));
        assertEquals(List.of(), groups.getExactCodes("  "));
        assertEquals(List.of(), groups.getExactCodes("unknown"));
        assertEquals(List.of(100, 200), groups.getExactCodes("group1"));
    }

    @Test
    void getExpandedCodesWithNullAndBlankGroupName() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("group1", new ArrayList<>(List.of("100-102")));
        CodeLocator groups = CodeLocator.from(raw);

        assertEquals(List.of(), groups.getExpandedCodes(null));
        assertEquals(List.of(), groups.getExpandedCodes(""));
        assertEquals(List.of(), groups.getExpandedCodes("  "));
        assertEquals(List.of(), groups.getExpandedCodes("unknown"));
        assertEquals(List.of(100, 101, 102), groups.getExpandedCodes("group1"));
    }

    @Test
    void getExpandedCodesPreviewWithNegativeN() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("group1", new ArrayList<>(List.of("1-10")));
        CodeLocator groups = CodeLocator.from(raw);

        assertEquals("[]", groups.getExpandedCodesPreview("group1", -1));
        assertEquals("[]", groups.getExpandedCodesPreview("group1", 0));
    }

    @Test
    void getExpandedCodesPreviewWithSmallSet() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("group1", new ArrayList<>(List.of("1-3")));
        CodeLocator groups = CodeLocator.from(raw);

        assertEquals("[1, 2, 3]", groups.getExpandedCodesPreview("group1", 5));
    }

    @Test
    void getExpandedCodesPreviewWithSingleElement() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("group1", new ArrayList<>(List.of(7)));
        CodeLocator groups = CodeLocator.from(raw);

        assertEquals("[7]", groups.getExpandedCodesPreview("group1", 5));
    }

    @Test
    void getExpandedCodesPreviewWithEmptyGroup() {
        CodeLocator groups = CodeLocator.from(null);
        assertEquals("[]", groups.getExpandedCodesPreview("unknown"));
        assertEquals("[]", groups.getExpandedCodesPreview("unknown", 5));
    }

    @Test
    void getExpandedCodesPreviewWithLargeRange() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("group1", new ArrayList<>(List.of("1-10")));
        CodeLocator groups = CodeLocator.from(raw);

        String preview = groups.getExpandedCodesPreview("group1", 5);
        assertTrue(preview.startsWith("[1, "));
        assertTrue(preview.endsWith(", ..., 10]"));
    }

    @Test
    void getExpandedCodesPreviewWithTwoElements() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("group1", new ArrayList<>(List.of("1-2")));
        CodeLocator groups = CodeLocator.from(raw);

        String preview = groups.getExpandedCodesPreview("group1", 1);
        assertEquals("[1, ..., 2]", preview);
    }

    @Test
    void fromWithBothRangesAndExactCodes() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("test", new ArrayList<>(List.of("100-102", 200, "300")));

        CodeLocator groups = CodeLocator.from(raw);

        assertTrue(groups.isInGroup(100, "test"));
        assertTrue(groups.isInGroup(200, "test"));
        assertTrue(groups.isInGroup(300, "test"));
    }

    @Test
    void getGroupForCodeWithEmptyRangesInSecondGroup() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("empty", new ArrayList<>(List.of()));
        raw.put("test", new ArrayList<>(List.of("100-200")));

        CodeLocator groups = CodeLocator.from(raw);

        assertEquals("test", groups.getGroupForCode(150));
        assertEquals("default", groups.getGroupForCode(999));
    }

    @Test
    void getExpandedCodesPreviewWithExactlyTwoElementsInMids() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("test", new ArrayList<>(List.of("1-4")));

        CodeLocator groups = CodeLocator.from(raw);

        String preview = groups.getExpandedCodesPreview("test", 2);
        assertTrue(preview.contains("1"));
        assertTrue(preview.contains("4"));
        assertTrue(preview.contains("..."));
    }

    @Test
    void fromWithOnlyInvalidStrings() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("test", new ArrayList<>(List.of("invalid1", "invalid2")));

        CodeLocator groups = CodeLocator.from(raw);

        assertFalse(groups.getGroupNames().contains("test"));
    }

    @Test
    void fromWithNonStringNonNumberValues() {
        Map<String, List<Object>> raw = new LinkedHashMap<>();
        raw.put("test", new ArrayList<>(List.of(true, new Object())));

        CodeLocator groups = CodeLocator.from(raw);

        assertTrue(groups.getGroupNames().isEmpty());
        assertEquals("default", groups.getGroupForCode(1));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void reflectiveConstructionCoversUnreachableBranches() throws Exception {
        Class<?> codeRangeClass = Class.forName("com.chao.failure.config.mapping.CodeLocator$CodeRange");
        var codeRangeCtor = codeRangeClass.getDeclaredConstructor(int.class, int.class);
        codeRangeCtor.setAccessible(true);
        Object range = codeRangeCtor.newInstance(10, 20);

        Map groupRanges = new LinkedHashMap();
        groupRanges.put("nullRanges", null);
        groupRanges.put("emptyRanges", List.of());
        groupRanges.put("ok", List.of(range));

        var ctor = CodeLocator.class.getDeclaredConstructor(Map.class, Map.class);
        ctor.setAccessible(true);
        CodeLocator groups = ctor.newInstance(groupRanges, Map.of());

        assertFalse(groups.isInGroup(15, "emptyRanges"));
        assertEquals("ok", groups.getGroupForCode(15));
        assertEquals("default", groups.getGroupForCode(999));
        assertEquals(List.of(), groups.getExpandedCodes("emptyRanges"));
    }
}
