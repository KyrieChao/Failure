package com.chao.failure.internal.check;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Map校验工具测试")
class MapChecksTest {

    @Nested
    @DisplayName("notEmpty 测试")
    class NotEmptyTest {
        @Test
        @DisplayName("Map 为 null 时应返回 false")
        void shouldReturnFalseWhenMapIsNull() {
            assertThat(MapChecks.notEmpty(null)).isFalse();
        }

        @Test
        @DisplayName("Map 为空时应返回 false")
        void shouldReturnFalseWhenMapIsEmpty() {
            assertThat(MapChecks.notEmpty(Collections.emptyMap())).isFalse();
        }

        @Test
        @DisplayName("Map 非空时应返回 true")
        void shouldReturnTrueWhenMapIsNotEmpty() {
            assertThat(MapChecks.notEmpty(Map.of("k", "v"))).isTrue();
        }
    }

    @Nested
    @DisplayName("isEmpty 测试")
    class IsEmptyTest {
        @Test
        @DisplayName("Map 为 null 时应返回 true")
        void shouldReturnTrueWhenMapIsNull() {
            assertThat(MapChecks.isEmpty(null)).isTrue();
        }

        @Test
        @DisplayName("Map 为空时应返回 true")
        void shouldReturnTrueWhenMapIsEmpty() {
            assertThat(MapChecks.isEmpty(Collections.emptyMap())).isTrue();
        }

        @Test
        @DisplayName("Map 非空时应返回 false")
        void shouldReturnFalseWhenMapIsNotEmpty() {
            assertThat(MapChecks.isEmpty(Map.of("k", "v"))).isFalse();
        }
    }

    @Nested
    @DisplayName("containsKey 测试")
    class ContainsKeyTest {
        @Test
        @DisplayName("Map 为 null 时应返回 false")
        void shouldReturnFalseWhenMapIsNull() {
            assertThat(MapChecks.containsKey(null, "key")).isFalse();
        }

        @Test
        @DisplayName("Map 包含 key 时应返回 true")
        void shouldReturnTrueWhenMapContainsKey() {
            assertThat(MapChecks.containsKey(Map.of("key", "val"), "key")).isTrue();
        }

        @Test
        @DisplayName("Map 不包含 key 时应返回 false")
        void shouldReturnFalseWhenMapDoesNotContainKey() {
            assertThat(MapChecks.containsKey(Map.of("other", "val"), "key")).isFalse();
        }
    }

    @Nested
    @DisplayName("notContainsKey 测试")
    class NotContainsKeyTest {
        @Test
        @DisplayName("Map 为 null 时应返回 true")
        void shouldReturnTrueWhenMapIsNull() {
            assertThat(MapChecks.notContainsKey(null, "key")).isTrue();
        }

        @Test
        @DisplayName("Map 包含 key 时应返回 false")
        void shouldReturnFalseWhenMapContainsKey() {
            assertThat(MapChecks.notContainsKey(Map.of("key", "val"), "key")).isFalse();
        }

        @Test
        @DisplayName("Map 不包含 key 时应返回 true")
        void shouldReturnTrueWhenMapDoesNotContainKey() {
            assertThat(MapChecks.notContainsKey(Map.of("other", "val"), "key")).isTrue();
        }
    }

    @Nested
    @DisplayName("containsValue 测试")
    class ContainsValueTest {
        @Test
        @DisplayName("Map 为 null 时应返回 false")
        void shouldReturnFalseWhenMapIsNull() {
            assertThat(MapChecks.containsValue(null, "val")).isFalse();
        }

        @Test
        @DisplayName("Map 包含 value 时应返回 true")
        void shouldReturnTrueWhenMapContainsValue() {
            assertThat(MapChecks.containsValue(Map.of("key", "val"), "val")).isTrue();
        }

        @Test
        @DisplayName("Map 不包含 value 时应返回 false")
        void shouldReturnFalseWhenMapDoesNotContainValue() {
            assertThat(MapChecks.containsValue(Map.of("key", "other"), "val")).isFalse();
        }
    }

    @Nested
    @DisplayName("sizeBetween 测试")
    class SizeBetweenTest {
        @Test
        @DisplayName("Map 为 null 且范围包含 0 时应返回 true")
        void shouldReturnTrueWhenMapIsNullAndRangeIncludesZero() {
            assertThat(MapChecks.sizeBetween(null, 0, 1)).isTrue();
        }

        @Test
        @DisplayName("Map 为 null 且范围不包含 0 时应返回 false")
        void shouldReturnFalseWhenMapIsNullAndRangeExcludesZero() {
            assertThat(MapChecks.sizeBetween(null, 1, 2)).isFalse();
        }

        @Test
        @DisplayName("Map 大小在范围内应返回 true")
        void shouldReturnTrueWhenSizeInRange() {
            Map<String, String> map = new HashMap<>();
            map.put("k1", "v1");
            assertThat(MapChecks.sizeBetween(map, 1, 2)).isTrue();
        }

        @Test
        @DisplayName("Map 大小小于最小值应返回 false")
        void shouldReturnFalseWhenSizeTooSmall() {
            Map<String, String> map = new HashMap<>();
            map.put("k1", "v1");
            assertThat(MapChecks.sizeBetween(map, 2, 5)).isFalse();
        }

        @Test
        @DisplayName("Map 大小大于最大值应返回 false")
        void shouldReturnFalseWhenSizeTooLarge() {
            Map<String, String> map = new HashMap<>();
            map.put("k1", "v1");
            map.put("k2", "v2");
            assertThat(MapChecks.sizeBetween(map, 0, 1)).isFalse();
        }
    }

    @Nested
    @DisplayName("sizeEquals 测试")
    class SizeEqualsTest {
        @Test
        @DisplayName("Map 为 null 时应返回 false")
        void shouldReturnFalseWhenMapIsNull() {
            assertThat(MapChecks.sizeEquals(null, 0)).isFalse();
        }

        @Test
        @DisplayName("Map 大小相等应返回 true")
        void shouldReturnTrueWhenSizeEquals() {
            assertThat(MapChecks.sizeEquals(Map.of("k", "v"), 1)).isTrue();
        }

        @Test
        @DisplayName("Map 大小不相等应返回 false")
        void shouldReturnFalseWhenSizeNotEquals() {
            assertThat(MapChecks.sizeEquals(Map.of("k", "v"), 2)).isFalse();
        }
    }
}
