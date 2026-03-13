package com.chao.failfast.config;

import com.chao.failfast.internal.core.FailureProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CodeMappingConfig 配置类测试")
class CodeMappingConfigTest {

    private CodeMappingConfig config;
    private FailureProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FailureProperties();
        FailureProperties.CodeMapping mapping = new FailureProperties.CodeMapping();
        properties.setCodeMapping(mapping);
        config = new CodeMappingConfig(properties);
    }

    @Nested
    @DisplayName("HTTP 状态码解析测试")
    class ResolveHttpStatusTest {
        @Test
        @DisplayName("默认映射应当正确解析")
        void shouldResolveDefaultMappings() {
            assertThat(config.resolveHttpStatus(40000)).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(config.resolveHttpStatus(50000)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(config.resolveHttpStatus(99)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("自定义映射应当覆盖默认映射")
        void shouldResolveCustomMappings() {
            Map<String, Integer> custom = new HashMap<>();
            custom.put("90000", 418); // I'm a teapot
            properties.getCodeMapping().setHttpStatus(custom);

            // Re-initialize config to load custom mappings
            config = new CodeMappingConfig(properties);

            assertThat(config.resolveHttpStatus(90000)).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
        }

        @Test
        @DisplayName("范围匹配应当正确解析")
        void shouldResolveRangeMappings() {
            assertThat(config.resolveHttpStatus(40099)).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("大类匹配应当正确解析")
        void shouldResolveCategoryMappings() {
            // 49999 is not mapped, range 49900 not mapped.
            // But 40000 <= code < 50000 -> BAD_REQUEST
            assertThat(config.resolveHttpStatus(49999)).isEqualTo(HttpStatus.BAD_REQUEST);

            // 59999 -> INTERNAL_SERVER_ERROR
            assertThat(config.resolveHttpStatus(59999)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("标准HTTP状态码应当正确解析")
        void shouldResolveStandardHttpStatus() {
            assertThat(config.resolveHttpStatus(404)).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(config.resolveHttpStatus(200)).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("未知错误码应当解析为500")
        void shouldResolveUnknownToInternalServerError() {
            assertThat(config.resolveHttpStatus(99999)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("标准HTTP边界值应正确解析")
        void shouldResolveHttpStatusBoundaryValues() {
            assertThat(config.resolveHttpStatus(100)).isEqualTo(HttpStatus.CONTINUE);
            assertThat(config.resolveHttpStatus(599)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("非标准但有效的Spring HTTP码")
        void shouldResolveNonStandardButValidSpringCode() {
            // 418 是 Spring 的 HttpStatus 枚举，但不是 RFC 标准
            assertThat(config.resolveHttpStatus(418)).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
        }
    }

    @Nested
    @DisplayName("分组测试")
    class GroupTest {
        @BeforeEach
        void setUpGroups() {
            Map<String, List<Object>> groups = new HashMap<>();
            groups.put("testGroup", Arrays.asList(1001, "2000-2005", "3000..3002"));
            groups.put("product", Arrays.asList(40400, 40499));
            groups.put("range", List.of("skksdkm"));
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);
        }

        @Test
        @DisplayName("应当正确判断错误码是否在分组内")
        void shouldCheckIfInGroup() {
            assertThat(config.isInGroup(1001, "testGroup")).isTrue();
            assertThat(config.isInGroup(2003, "testGroup")).isTrue(); // In 2000-2005
            assertThat(config.isInGroup(3001, "testGroup")).isTrue(); // In 3000..3002

            assertThat(config.isInGroup(1002, "testGroup")).isFalse();
            assertThat(config.isInGroup(2006, "testGroup")).isFalse();
        }

        @Test
        @DisplayName("应当正确展开分组错误码")
        void shouldExpandGroupCodes() {
            // "2000-2005" -> 2000, 2001, 2002, 2003, 2004, 2005 (6 items)
            // Total items > 5
            String expanded = config.getGroupCodesExpanded("testGroup");
            assertThat(expanded).contains("...");

            // Small group
            Map<String, List<Object>> groups = new HashMap<>();
            groups.put("smallGroup", Arrays.asList(1, 2));
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);

            assertThat(config.getGroupCodesExpanded("smallGroup")).isEqualTo("[1, 2]");
        }

        @Test
        @DisplayName("获取指定分组的所有错误码(仅返回精确值列表，范围展开不返回)")
        void shouldGetAllCodesInGroup() {
            List<Integer> codes = config.getGroupCodes("product");
            assertThat(codes).isEqualTo(Arrays.asList(40400, 40499));
        }
    }

    @Nested
    @DisplayName("getGroupCodesExpanded 重载方法测试")
    class GetGroupCodesExpandedOverloadTest {

        @Test
        @DisplayName("当 n <= 0 时应返回空数组")
        void shouldReturnEmptyArrayWhenNIsZeroOrNegative() {
            Map<String, List<Object>> groups = new HashMap<>();
            groups.put("testGroup", List.of(1, 2, 3));
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);

            assertThat(config.getGroupCodesExpanded("testGroup", 0)).isEqualTo("[]");
            assertThat(config.getGroupCodesExpanded("testGroup", -1)).isEqualTo("[]");
        }

        @Test
        @DisplayName("当组不存在时应返回空数组")
        void shouldReturnEmptyArrayWhenGroupNotExists() {
            assertThat(config.getGroupCodesExpanded("nonExistentGroup", 5)).isEqualTo("[]");
        }

        @Test
        @DisplayName("当组为空时应返回空数组")
        void shouldReturnEmptyArrayWhenGroupIsEmpty() {
            Map<String, List<Object>> groups = new HashMap<>();
            groups.put("emptyGroup", Collections.emptyList());
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);

            assertThat(config.getGroupCodesExpanded("emptyGroup", 5)).isEqualTo("[]");
        }

        @Test
        @DisplayName("当总数等于 n 时应返回完整列表")
        void shouldReturnFullListWhenSizeEqualsN() {
            Map<String, List<Object>> groups = new HashMap<>();
            groups.put("testGroup", Arrays.asList(1, 2, 3, 4, 5));
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);

            assertThat(config.getGroupCodesExpanded("testGroup", 5)).isEqualTo("[1, 2, 3, 4, 5]");
        }

        @Test
        @DisplayName("当总数大于 n 时应返回省略格式")
        void shouldReturnAbbreviatedFormatWhenSizeGreaterThanN() {
            Map<String, List<Object>> groups = new HashMap<>();
            groups.put("testGroup", List.of("1-10"));
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);

            String result = config.getGroupCodesExpanded("testGroup", 3);
            assertThat(result).contains("...");
            assertThat(result).startsWith("[1,");
            assertThat(result).endsWith(", 10]");
        }

        @Test
        @DisplayName("省略格式中间部分应正确截断")
        void shouldCorrectlyTruncateMiddlePart() {
            Map<String, List<Object>> groups = new HashMap<>();
            groups.put("testGroup", List.of("100-200"));
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);

            String result = config.getGroupCodesExpanded("testGroup", 5);
            // 101, 102, 103 应该被截断显示
            assertThat(result).contains("...");
        }
    }

    @Nested
    @DisplayName("parseRange 边界测试")
    class ParseRangeEdgeTest {

        @Test
        @DisplayName("范围字符串应支持反向范围（大数-小数）")
        void shouldSupportReverseRange() {
            Map<String, List<Object>> groups = new HashMap<>();
            groups.put("testGroup", List.of("10-5"));  // 反向范围
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);

            assertThat(config.isInGroup(7, "testGroup")).isTrue();
            assertThat(config.isInGroup(5, "testGroup")).isTrue();
            assertThat(config.isInGroup(10, "testGroup")).isTrue();
        }

        @Test
        @DisplayName("范围字符串应支持点号格式（..）")
        void shouldSupportDotDotRange() {
            Map<String, List<Object>> groups = new HashMap<>();
            groups.put("testGroup", List.of("5..10"));
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);

            assertThat(config.isInGroup(7, "testGroup")).isTrue();
        }

        @Test
        @DisplayName("范围字符串应支持带空格")
        void shouldSupportRangeWithSpaces() {
            Map<String, List<Object>> groups = new HashMap<>();
            groups.put("testGroup", List.of("  5  -  10  "));
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);

            assertThat(config.isInGroup(7, "testGroup")).isTrue();
        }

        @Test
        @DisplayName("无效的范围字符串应被忽略")
        void shouldIgnoreInvalidRangeString() {
            Map<String, List<Object>> groups = new HashMap<>();
            groups.put("testGroup", List.of("invalid", "abc-def", "1-2-3"));
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);

            // 这些无效的范围应该被忽略，组内没有有效范围
            assertThat(config.isInGroup(1, "testGroup")).isFalse();
        }

        @Test
        @DisplayName("数字字符串应被解析为单值")
        void shouldParseNumberStringAsSingleValue() {
            Map<String, List<Object>> groups = new HashMap<>();
            groups.put("testGroup", List.of("123"));
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);

            assertThat(config.isInGroup(123, "testGroup")).isTrue();
            assertThat(config.isInGroup(124, "testGroup")).isFalse();
        }
    }

    @Nested
    @DisplayName("loadCustomMappings 边界测试")
    class LoadCustomMappingsEdgeTest {

        @Test
        @DisplayName("无效的业务码应被忽略并记录警告")
        void shouldIgnoreInvalidBusinessCode() {
            Map<String, Integer> custom = new HashMap<>();
            custom.put("not-a-number", 200);
            properties.getCodeMapping().setHttpStatus(custom);

            // 不应抛出异常
            config = new CodeMappingConfig(properties);
            assertThat(config.resolveHttpStatus(40000)).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("无效的HTTP状态码应被忽略并记录警告")
        void shouldIgnoreInvalidHttpStatus() {
            Map<String, Integer> custom = new HashMap<>();
            custom.put("90000", 999);  // 999 不是有效的 HttpStatus
            properties.getCodeMapping().setHttpStatus(custom);

            // 不应抛出异常
            config = new CodeMappingConfig(properties);
        }
    }

    @Nested
    @DisplayName("resolveHttpStatus 边界测试")
    class ResolveHttpStatusEdgeTest {

        @Test
        @DisplayName("非标准HTTP码（如499）应继续后续匹配")
        void shouldContinueMatchingForNonStandardHttpCode() {
            // 499 不是标准 HTTP 状态码，应该走业务码匹配逻辑
            assertThat(config.resolveHttpStatus(499)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("范围边界值应正确匹配")
        void shouldCorrectlyMatchRangeBoundaries() {
            // 40000 是精确匹配
            assertThat(config.resolveHttpStatus(40000)).isEqualTo(HttpStatus.BAD_REQUEST);

            // 40099 应该匹配 40000 的范围
            assertThat(config.resolveHttpStatus(40099)).isEqualTo(HttpStatus.BAD_REQUEST);

            // 40100 是精确匹配
            assertThat(config.resolveHttpStatus(40100)).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("5xx 业务码应正确匹配")
        void shouldCorrectlyMatch5xxBusinessCodes() {
            assertThat(config.resolveHttpStatus(50000)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(config.resolveHttpStatus(59999)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(config.resolveHttpStatus(55000)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("3xx 业务码应返回默认500")
        void shouldReturnDefaultFor3xxBusinessCodes() {
            assertThat(config.resolveHttpStatus(30000)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("parseGroupRanges 边界测试")
    class ParseGroupRangesEdgeTest {

        @Test
        @DisplayName("groups 为 null 时不应抛出异常")
        void shouldHandleNullGroups() {
            properties.getCodeMapping().setGroups(null);

            // 不应抛出异常
            config = new CodeMappingConfig(properties);

            assertThat(config.isInGroup(100, "anyGroup")).isFalse();
        }

        @Test
        @DisplayName("组内包含 null 元素时应被忽略")
        void shouldHandleNullElementsInGroup() {
            Map<String, List<Object>> groups = new HashMap<>();
            List<Object> listWithNull = new ArrayList<>();
            listWithNull.add(100);
            listWithNull.add(null);
            listWithNull.add(200);
            groups.put("testGroup", listWithNull);
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);

            assertThat(config.isInGroup(100, "testGroup")).isTrue();
            assertThat(config.isInGroup(200, "testGroup")).isTrue();
        }

        @Test
        @DisplayName("getGroupCodes 应过滤非整数类型")
        void shouldFilterNonIntegerTypesInGetGroupCodes() {
            Map<String, List<Object>> groups = new HashMap<>();
            groups.put("mixedGroup", Arrays.asList(100, "not-a-number", 200, "300-400"));
            properties.getCodeMapping().setGroups(groups);
            config = new CodeMappingConfig(properties);

            List<Integer> codes = config.getGroupCodes("mixedGroup");
            assertThat(codes).containsExactly(100, 200);
        }
    }
}
