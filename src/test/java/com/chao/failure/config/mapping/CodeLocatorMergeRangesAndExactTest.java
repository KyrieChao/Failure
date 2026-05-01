package com.chao.failure.config.mapping;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeLocatorMergeRangesAndExactTest {

    @Test
    void shouldReturnEmptyWhenRangesAndExactAreBothMissing() throws Exception {
        assertTrue(invokeMerge(null, null).isEmpty());
        assertTrue(invokeMerge(List.of(), new int[0]).isEmpty());
        assertTrue(invokeMerge(null, new int[0]).isEmpty());
        assertTrue(invokeMerge(List.of(), null).isEmpty());
    }

    @Test
    void shouldMergeAdjacentAndOverlappingIncludingExactCodes() throws Exception {
        List<Object> ranges = new ArrayList<>();
        ranges.add(newCodeRange(20, 30));
        ranges.add(newCodeRange(5, 8));
        int[] exact = new int[]{9, 4, 10};

        List<?> merged = invokeMerge(ranges, exact);
        assertEquals(List.of("4-10", "20-30"), toPairs(merged));
    }

    @Test
    void shouldWorkWithOnlyExactCodes() throws Exception {
        int[] exact = new int[]{3, 2, 2, 1};
        List<?> merged = invokeMerge(null, exact);
        assertEquals(List.of("1-3"), toPairs(merged));
    }

    @Test
    void shouldWorkWithEmptyExactCodesWhenRangesPresent() throws Exception {
        List<Object> ranges = new ArrayList<>();
        ranges.add(newCodeRange(1, 1));
        List<?> merged = invokeMerge(ranges, new int[0]);
        assertEquals(List.of("1-1"), toPairs(merged));
    }

    @Test
    void shouldKeepSeparatedRangesWhenGapExists() throws Exception {
        List<Object> ranges = new ArrayList<>();
        ranges.add(newCodeRange(10, 12));
        ranges.add(newCodeRange(15, 18));

        List<?> merged = invokeMerge(ranges, null);
        assertEquals(List.of("10-12", "15-18"), toPairs(merged));
    }

    private static List<?> invokeMerge(List<?> ranges, int[] exactCodes) throws Exception {
        Method m = CodeLocator.class.getDeclaredMethod("mergeRangesAndExact", List.class, int[].class);
        m.setAccessible(true);
        return (List<?>) m.invoke(null, ranges, exactCodes);
    }

    private static Object newCodeRange(int start, int end) throws Exception {
        Class<?> codeRangeClass = Class.forName("com.chao.failure.config.mapping.CodeLocator$CodeRange");
        Constructor<?> ctor = codeRangeClass.getDeclaredConstructor(int.class, int.class);
        ctor.setAccessible(true);
        return ctor.newInstance(start, end);
    }

    private static List<String> toPairs(List<?> ranges) throws Exception {
        ArrayList<String> out = new ArrayList<>(ranges.size());
        for (Object r : ranges) {
            Method start = r.getClass().getDeclaredMethod("start");
            Method end = r.getClass().getDeclaredMethod("end");
            start.setAccessible(true);
            end.setAccessible(true);
            out.add(start.invoke(r) + "-" + end.invoke(r));
        }
        return List.copyOf(out);
    }
}
