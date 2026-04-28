package com.chao.failure.internal.check;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ArrayChecks 工具类测试")
class ArrayChecksTest {

    @Nested
    @DisplayName("notEmpty 方法测试")
    class NotEmptyTest {
        @Test
        @DisplayName("当数组非空时应返回true")
        void shouldReturnTrueWhenArrayIsNotEmpty() {
            assertThat(ArrayChecks.notEmpty(new String[]{"a"})).isTrue();
        }

        @Test
        @DisplayName("当数组为空或null时应返回false")
        void shouldReturnFalseWhenArrayIsEmptyOrNull() {
            assertThat(ArrayChecks.notEmpty(new String[]{})).isFalse();
            assertThat(ArrayChecks.isEmpty(new String[]{})).isTrue();
            assertThat(ArrayChecks.notEmpty(null)).isFalse();
            assertThat(ArrayChecks.isEmpty(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("sizeBetween 方法测试")
    class SizeBetweenTest {
        @Test
        @DisplayName("当数组大小在范围内时应返回true")
        void shouldReturnTrueWhenSizeIsInRange() {
            String[] arr = {"a", "b", "c"};
            assertThat(ArrayChecks.sizeBetween(null, 1, 3)).isFalse();
            assertThat(ArrayChecks.sizeBetween(null, 0, 3)).isTrue();
            assertThat(ArrayChecks.sizeBetween(arr, 1, 3)).isTrue();
            assertThat(ArrayChecks.sizeBetween(arr, 4, 5)).isFalse();
            // 场景1: len >= min 为 true，但 len <= max 为 false（数组太长）
            assertThat(ArrayChecks.sizeBetween(arr, 1, 2)).isFalse();  // len=3, 3>=1 true, 3<=2 false
            // 场景2: min > max 的边界（虽然逻辑上可能不合法，但代码没拦）
            // 场景3: 空数组 {} 的各种边界
            assertThat(ArrayChecks.sizeBetween(new String[]{}, 0, 0)).isTrue();   // len=0
            assertThat(ArrayChecks.sizeBetween(new String[]{}, 1, 3)).isFalse();  // len=0 < min
        }

        @Test
        @DisplayName("当数组大小不在范围内时应返回false")
        void shouldReturnFalseWhenSizeIsOutOfRange() {
            String[] arr = {"a", "b"};
            assertThat(ArrayChecks.sizeBetween(arr, 3, 5)).isFalse();
        }

        @Test
        @DisplayName("当数组为null时视作大小为0")
        void shouldTreatNullAsSizeZero() {
            assertThat(ArrayChecks.sizeBetween(null, 0, 0)).isTrue();
            assertThat(ArrayChecks.sizeBetween(null, 1, 5)).isFalse();
        }
    }

    @Nested
    @DisplayName("sizeEquals 方法测试")
    class SizeEqualsTest {
        @Test
        @DisplayName("当数组大小等于预期值时应返回true")
        void shouldReturnTrueWhenSizeEqualsExpected() {
            String[] arr = {"a", "b"};
            assertThat(ArrayChecks.sizeEquals(arr, 2)).isTrue();
        }

        @Test
        @DisplayName("当数组大小不等于预期值时应返回false")
        void shouldReturnFalseWhenSizeDoesNotEqualExpected() {
            String[] arr = {"a", "b"};
            assertThat(ArrayChecks.sizeEquals(arr, 3)).isFalse();
        }

        @Test
        @DisplayName("当数组为null时应返回false")
        void shouldReturnFalseWhenArrayIsNull() {
            assertThat(ArrayChecks.sizeEquals(null, 0)).isFalse();
        }
    }

    @Nested
    @DisplayName("contains 方法测试")
    class ContainsTest {
        @Test
        @DisplayName("当数组包含元素时应返回true")
        void shouldReturnTrueWhenArrayContainsElement() {
            String[] arr = {"a", "b"};
            assertThat(ArrayChecks.contains(arr, "a")).isTrue();
        }

        @Test
        @DisplayName("当数组不包含元素时应返回false")
        void shouldReturnFalseWhenArrayDoesNotContainElement() {
            String[] arr = {"a", "b"};
            assertThat(ArrayChecks.contains(arr, "c")).isFalse();
        }

        @Test
        @DisplayName("当数组为null时应返回false")
        void shouldReturnFalseWhenArrayIsNull() {
            assertThat(ArrayChecks.contains(null, "a")).isFalse();
        }
    }

    @Nested
    @DisplayName("notContains 方法测试")
    class NotContainsTest {
        @Test
        @DisplayName("当数组不包含元素时应返回true")
        void shouldReturnTrueWhenArrayDoesNotContainElement() {
            String[] arr = {"a", "b"};
            assertThat(ArrayChecks.notContains(arr, "c")).isTrue();
        }

        @Test
        @DisplayName("当数组包含元素时应返回false")
        void shouldReturnFalseWhenArrayContainsElement() {
            String[] arr = {"a", "b"};
            assertThat(ArrayChecks.notContains(arr, "a")).isFalse();
        }

        @Test
        @DisplayName("检查数组是否不包含null元素")
        void shouldReturnFalseWhenArrayContainsNullElement() {
            String[] arr = {"a", null};
            assertThat(ArrayChecks.hasNoNullElements(null)).isTrue();
            assertThat(ArrayChecks.hasNoNullElements(arr)).isFalse();
        }

        @Test
        @DisplayName("检查数组是否所有元素都满足指定条件")
        void shouldReturnTrueWhenAllElementsSatisfyCondition() {
            String[] arr = {"a", "b"};
            assertThat(ArrayChecks.allMatch(arr, Objects::nonNull)).isTrue();
            assertThat(ArrayChecks.allMatch(null, Objects::nonNull)).isFalse();
            assertThat(ArrayChecks.allMatch(arr, null)).isFalse();
        }

        @Test
        @DisplayName("检查数组是否存在至少一个元素满足指定条件")
        void shouldReturnTrueWhenAnyElementSatisfiesCondition() {
            String[] arr = {"a", "b"};
//            Set<String> arr = Set.of("a", "b");
            assertThat(ArrayChecks.anyMatch(arr, Objects::nonNull)).isTrue();
            assertThat(ArrayChecks.anyMatch(null, Objects::nonNull)).isFalse();
            assertThat(ArrayChecks.anyMatch(arr, null)).isFalse();
        }

        @Test
        @DisplayName("当数组为null时应返回true")
        void shouldReturnTrueWhenArrayIsNull() {
            assertThat(ArrayChecks.notContains(null, "a")).isTrue();
        }
    }
}
