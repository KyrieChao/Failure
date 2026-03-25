package com.chao.failfast.config;

import com.chao.failfast.internal.core.FailureProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CodeMappingConfigTest {

    @Test
    void testConstructorWithDefaultMappings() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertNotNull(config);
    }

    @Test
    void testLoadCustomMappingsWithValidEntries() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, Integer> httpStatusMap = new HashMap<>();
        httpStatusMap.put("40000", 400);
        httpStatusMap.put("40100", 401);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(httpStatusMap);
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertNotNull(config);
    }

    @Test
    void testLoadCustomMappingsWithInvalidCode() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, Integer> httpStatusMap = new HashMap<>();
        httpStatusMap.put("invalid", 400);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(httpStatusMap);
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertNotNull(config);
    }

    @Test
    void testLoadCustomMappingsWithInvalidHttpStatus() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, Integer> httpStatusMap = new HashMap<>();
        httpStatusMap.put("40000", 999);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(httpStatusMap);
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertNotNull(config);
    }

    @Test
    void testParseGroupRangesWithNullGroups() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertNotNull(config);
    }

    @Test
    void testParseGroupRangesWithValidGroups() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, List<Object>> groups = new HashMap<>();
        groups.put("test", List.of(40000, "40100-40200", "40300"));
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(groups);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertNotNull(config);
    }

    @Test
    void testParseGroupRangesWithInvalidRange() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, List<Object>> groups = new HashMap<>();
        groups.put("test", List.of("invalid-range"));
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(groups);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertNotNull(config);
    }

    @Test
    void testIsInGroupWithExistingGroup() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, List<Object>> groups = new HashMap<>();
        groups.put("test", List.of(40000, "40100-40200"));
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(groups);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertTrue(config.isInGroup(40000, "test"));
        assertTrue(config.isInGroup(40150, "test"));
        assertFalse(config.isInGroup(40300, "test"));
    }

    @Test
    void testIsInGroupWithNonExistingGroup() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(new HashMap<>());

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertFalse(config.isInGroup(40000, "non-existent"));
    }

    @Test
    void testGetGroupCodes() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, List<Object>> groups = new HashMap<>();
        groups.put("test", List.of(40000, 40100, "40200-40300"));
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(groups);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        var codes = config.getGroupCodes("test");
        assertEquals(2, codes.size());
        assertTrue(codes.contains(40000));
        assertTrue(codes.contains(40100));
    }

    @Test
    void testGetGroupCodesExpanded() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, List<Object>> groups = new HashMap<>();
        groups.put("test", List.of(40000, "40001-40003"));
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(groups);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        String expanded = config.getGroupCodesExpanded("test");
        assertNotNull(expanded);
    }

    @Test
    void testGetGroupCodesExpandedWithLimit() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, List<Object>> groups = new HashMap<>();
        groups.put("test", List.of(40000, "40001-40010"));
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(groups);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        String expanded = config.getGroupCodesExpanded("test", 5);
        assertNotNull(expanded);
    }

    @Test
    void testGetGroupCodesExpandedWithNonExistingGroup() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(new HashMap<>());

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertEquals("[]", config.getGroupCodesExpanded("non-existent"));
    }

    @Test
    void testResolveHttpStatusWithStandardHttpCode() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertEquals(HttpStatus.BAD_REQUEST, config.resolveHttpStatus(400));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, config.resolveHttpStatus(500));
    }

    @Test
    void testResolveHttpStatusWithInvalidHttpCode() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, config.resolveHttpStatus(999));
    }

    @Test
    void testResolveHttpStatusWithExactMatch() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, Integer> httpStatusMap = new HashMap<>();
        httpStatusMap.put("40000", 400);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(httpStatusMap);
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertEquals(HttpStatus.BAD_REQUEST, config.resolveHttpStatus(40000));
    }

    @Test
    void testResolveHttpStatusWithRangeMatch() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertEquals(HttpStatus.BAD_REQUEST, config.resolveHttpStatus(40050));
        assertEquals(HttpStatus.UNAUTHORIZED, config.resolveHttpStatus(40150));
    }

    @Test
    void testResolveHttpStatusWith4xxRange() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertEquals(HttpStatus.BAD_REQUEST, config.resolveHttpStatus(49999));
    }

    @Test
    void testResolveHttpStatusWith5xxRange() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, config.resolveHttpStatus(50050));
    }

    @Test
    void testParseRangeWithValidRange() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        // 通过反射测试parseRange方法
        try {
            var method = CodeMappingConfig.class.getDeclaredMethod("parseRange", String.class);
            method.setAccessible(true);
            var result = method.invoke(config, "1-5");
            assertNotNull(result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testParseRangeWithInvalidRange() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        // 通过反射测试parseRange方法
        try {
            var method = CodeMappingConfig.class.getDeclaredMethod("parseRange", String.class);
            method.setAccessible(true);
            var result = method.invoke(config, "invalid");
            assertNull(result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testParseRangeWithReverseRange() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        // 通过反射测试parseRange方法
        try {
            var method = CodeMappingConfig.class.getDeclaredMethod("parseRange", String.class);
            method.setAccessible(true);
            var result = method.invoke(config, "5-1");
            assertNotNull(result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testIsInGroupWithBoundaryValues() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, List<Object>> groups = new HashMap<>();
        groups.put("test", List.of("40000-40100"));
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(groups);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertTrue(config.isInGroup(40000, "test"));
        assertTrue(config.isInGroup(40100, "test"));
        assertFalse(config.isInGroup(39999, "test"));
        assertFalse(config.isInGroup(40101, "test"));
    }

    @Test
    void testGetGroupCodesExpandedWithZeroLimit() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, List<Object>> groups = new HashMap<>();
        groups.put("test", List.of(40000, 40001));
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(groups);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertEquals("[]", config.getGroupCodesExpanded("test", 0));
    }

    @Test
    void testResolveHttpStatusWith4xxRangeBoundary() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertEquals(HttpStatus.BAD_REQUEST, config.resolveHttpStatus(40000));
        assertEquals(HttpStatus.BAD_REQUEST, config.resolveHttpStatus(49999));
    }

    @Test
    void testResolveHttpStatusWithNonStandardCode() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, config.resolveHttpStatus(30000));
    }

    @Test
    void testParseRangeWithDotDotRange() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        try {
            var method = CodeMappingConfig.class.getDeclaredMethod("parseRange", String.class);
            method.setAccessible(true);
            var result = method.invoke(config, "1..5");
            assertNotNull(result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testResolveHttpStatusWithUnknownInHttpRange() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, config.resolveHttpStatus(199));
    }

    @Test
    void testParseGroupRangesWithUnsupportedElementType() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, List<Object>> groups = new HashMap<>();
        List<Object> raw = new java.util.ArrayList<>();
        raw.add(null);
        raw.add(new Object());
        raw.add(40000);
        groups.put("test", raw);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(groups);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertFalse(config.isInGroup(12345, "test"));
        assertTrue(config.isInGroup(40000, "test"));
    }

    @Test
    void testIsInGroupWithEmptyRanges() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Map<String, List<Object>> groups = new HashMap<>();
        groups.put("empty", List.of());
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(groups);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertFalse(config.isInGroup(40000, "empty"));
        assertEquals("[]", config.getGroupCodesExpanded("empty", 5));
    }

    @Test
    void testResolveHttpStatusWithHighCodeFallsBackTo500() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = Mockito.mock(FailureProperties.CodeMapping.class);
        Mockito.when(properties.getCodeMapping()).thenReturn(codeMapping);
        Mockito.when(codeMapping.getHttpStatus()).thenReturn(new HashMap<>());
        Mockito.when(codeMapping.getGroups()).thenReturn(null);

        CodeMappingConfig config = new CodeMappingConfig(properties);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, config.resolveHttpStatus(60000));
    }
}
