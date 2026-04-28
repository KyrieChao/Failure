package com.chao.failure.internal.check;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CollectionChecks 工具类测试")
class CollectionChecksTest {

    @Nested
    @DisplayName("notEmpty 方法测试")
    class NotEmptyTest {
        @Test
        @DisplayName("当集合非空时应返回true")
        void shouldReturnTrueWhenCollectionIsNotEmpty() {
            assertThat(CollectionChecks.notEmpty(Collections.singletonList("a"))).isTrue();
        }

        @Test
        @DisplayName("当集合为空或null时应返回false")
        void shouldReturnFalseWhenCollectionIsEmptyOrNull() {
            assertThat(CollectionChecks.notEmpty(Collections.emptyList())).isFalse();
            assertThat(CollectionChecks.notEmpty(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("sizeBetween 方法测试")
    class SizeBetweenTest {
        @Test
        @DisplayName("当集合大小在范围内时应返回true")
        void shouldReturnTrueWhenSizeIsInRange() {
            List<String> list = Arrays.asList("a", "b", "c");
            assertThat(CollectionChecks.sizeBetween(list, 1, 3)).isTrue();
            assertThat(CollectionChecks.sizeBetween(list, 3, 5)).isTrue();
        }

        @Test
        @DisplayName("当集合大小不在范围内时应返回false")
        void shouldReturnFalseWhenSizeIsOutOfRange() {
            List<String> list = Arrays.asList("a", "b");
            assertThat(CollectionChecks.sizeBetween(list, 3, 5)).isFalse();
        }

        @Test
        @DisplayName("当集合为null时视作大小为0")
        void shouldTreatNullAsSizeZero() {
            assertThat(CollectionChecks.sizeBetween(null, 0, 0)).isTrue();
            assertThat(CollectionChecks.sizeBetween(null, 1, 5)).isFalse();
        }
    }

    @Test
    @DisplayName("边界条件：精确匹配上下界")
    void shouldHandleExactBoundary() {
        List<String> list = Arrays.asList("a", "b", "c"); // size=3

        assertThat(CollectionChecks.sizeBetween(list, 3, 3)).isTrue();  // min=max=3
        assertThat(CollectionChecks.sizeBetween(list, 0, 3)).isTrue();  // max=3
        assertThat(CollectionChecks.sizeBetween(list, 3, 10)).isTrue(); // min=3
        assertThat(CollectionChecks.sizeBetween(list, 0, 2)).isFalse(); // size > max
        assertThat(CollectionChecks.sizeBetween(list, 4, 10)).isFalse(); // size < min
    }

    @Test
    @DisplayName("空集合边界")
    void shouldHandleEmptyCollectionBoundary() {
        assertThat(CollectionChecks.sizeBetween(Collections.emptyList(), 0, 0)).isTrue();
        assertThat(CollectionChecks.sizeBetween(Collections.emptyList(), 0, 5)).isTrue();
        assertThat(CollectionChecks.sizeBetween(Collections.emptyList(), 1, 5)).isFalse();
    }

    @Nested
    @DisplayName("sizeEquals 方法测试")
    class SizeEqualsTest {
        @Test
        @DisplayName("当集合大小等于预期值时应返回true")
        void shouldReturnTrueWhenSizeEqualsExpected() {
            List<String> list = Arrays.asList("a", "b");
            assertThat(CollectionChecks.sizeEquals(list, 2)).isTrue();
        }

        @Test
        @DisplayName("当集合大小不等于预期值时应返回false")
        void shouldReturnFalseWhenSizeDoesNotEqualExpected() {
            List<String> list = Arrays.asList("a", "b");
            assertThat(CollectionChecks.sizeEquals(list, 3)).isFalse();
        }

        @Test
        @DisplayName("当集合为null时应返回false")
        void shouldReturnFalseWhenCollectionIsNull() {
            assertThat(CollectionChecks.sizeEquals(null, 0)).isFalse();
        }
    }

    @Nested
    @DisplayName("contains 方法测试")
    class ContainsTest {
        @Test
        @DisplayName("当集合包含元素时应返回true")
        void shouldReturnTrueWhenCollectionContainsElement() {
            List<String> list = Arrays.asList("a", "b");
            assertThat(CollectionChecks.contains(list, "a")).isTrue();
        }

        @Test
        @DisplayName("当集合不包含元素时应返回false")
        void shouldReturnFalseWhenCollectionDoesNotContainElement() {
            List<String> list = Arrays.asList("a", "b");
            assertThat(CollectionChecks.contains(list, "c")).isFalse();
        }

        @Test
        @DisplayName("当集合为null时应返回false")
        void shouldReturnFalseWhenCollectionIsNull() {
            assertThat(CollectionChecks.contains(null, "a")).isFalse();
        }
    }

    @Nested
    @DisplayName("notContains 方法测试")
    class NotContainsTest {
        @Test
        @DisplayName("当集合不包含元素时应返回true")
        void shouldReturnTrueWhenCollectionDoesNotContainElement() {
            List<String> list = Arrays.asList("a", "b");
            assertThat(CollectionChecks.notContains(list, "c")).isTrue();
        }

        @Test
        @DisplayName("当集合包含元素时应返回false")
        void shouldReturnFalseWhenCollectionContainsElement() {
            List<String> list = Arrays.asList("a", "b");
            assertThat(CollectionChecks.notContains(list, "a")).isFalse();
        }

        @Test
        @DisplayName("当集合为null时应返回true")
        void shouldReturnTrueWhenCollectionIsNull() {
            assertThat(CollectionChecks.notContains(null, "a")).isTrue();
        }
    }

    @Nested
    @DisplayName("isEmpty 方法测试")
    class IsEmptyTest {
        @Test
        @DisplayName("当集合为空或null时应返回true")
        void shouldReturnTrueWhenCollectionIsEmptyOrNull() {
            assertThat(CollectionChecks.isEmpty(Collections.emptyList())).isTrue();
            assertThat(CollectionChecks.isEmpty(null)).isTrue();
        }

        @Test
        @DisplayName("当集合非空时应返回false")
        void shouldReturnFalseWhenCollectionIsNotEmpty() {
            assertThat(CollectionChecks.isEmpty(Collections.singletonList("a"))).isFalse();
            assertThat(CollectionChecks.isEmpty(Arrays.asList("a", "b"))).isFalse();
        }
    }

    @Nested
    @DisplayName("hasNoNullElements 方法测试")
    class HasNoNullElementsTest {
        @Test
        @DisplayName("当集合没有null元素时应返回true")
        void shouldReturnTrueWhenCollectionHasNoNullElements() {
            assertThat(CollectionChecks.hasNoNullElements(Arrays.asList("a", "b"))).isTrue();
            assertThat(CollectionChecks.hasNoNullElements(Collections.emptyList())).isTrue();
        }

        @Test
        @DisplayName("当集合包含null元素时应返回false")
        void shouldReturnFalseWhenCollectionHasNullElements() {
            assertThat(CollectionChecks.hasNoNullElements(Arrays.asList("a", null, "b"))).isFalse();
            assertThat(CollectionChecks.hasNoNullElements(Collections.singletonList(null))).isFalse();
        }

        @Test
        @DisplayName("当集合为null时应返回true")
        void shouldReturnTrueWhenCollectionIsNull() {
            assertThat(CollectionChecks.hasNoNullElements(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("allMatch 方法测试")
    class AllMatchTest {
        @Test
        @DisplayName("当所有元素都满足条件时应返回true")
        void shouldReturnTrueWhenAllElementsMatch() {
            List<Integer> list = Arrays.asList(2, 4, 6);
            assertThat(CollectionChecks.allMatch(list, n -> n % 2 == 0)).isTrue();
        }

        @Test
        @DisplayName("当任一元素不满足条件时应返回false")
        void shouldReturnFalseWhenAnyElementDoesNotMatch() {
            List<Integer> list = Arrays.asList(2, 3, 6);
            assertThat(CollectionChecks.allMatch(list, n -> n % 2 == 0)).isFalse();
        }

        @Test
        @DisplayName("当集合为空时应返回true")
        void shouldReturnTrueWhenCollectionIsEmpty() {
            assertThat(CollectionChecks.allMatch(Collections.emptyList(), n -> true)).isTrue();
        }

        @Test
        @DisplayName("当集合为null时应返回false")
        void shouldReturnFalseWhenCollectionIsNull() {
            assertThat(CollectionChecks.allMatch(null, n -> true)).isFalse();
        }

        @Test
        @DisplayName("当predicate为null时应返回false")
        void shouldReturnFalseWhenPredicateIsNull() {
            assertThat(CollectionChecks.allMatch(Arrays.asList(1, 2), null)).isFalse();
        }
    }

    @Nested
    @DisplayName("anyMatch 方法测试")
    class AnyMatchTest {
        @Test
        @DisplayName("当至少一个元素满足条件时应返回true")
        void shouldReturnTrueWhenAnyElementMatches() {
            List<Integer> list = Arrays.asList(1, 2, 3);
            assertThat(CollectionChecks.anyMatch(list, n -> n % 2 == 0)).isTrue();
        }

        @Test
        @DisplayName("当没有元素满足条件时应返回false")
        void shouldReturnFalseWhenNoElementMatches() {
            List<Integer> list = Arrays.asList(1, 3, 5);
            assertThat(CollectionChecks.anyMatch(list, n -> n % 2 == 0)).isFalse();
        }

        @Test
        @DisplayName("当集合为空时应返回false")
        void shouldReturnFalseWhenCollectionIsEmpty() {
            assertThat(CollectionChecks.anyMatch(Collections.emptyList(), n -> true)).isFalse();
        }

        @Test
        @DisplayName("当集合为null时应返回false")
        void shouldReturnFalseWhenCollectionIsNull() {
            assertThat(CollectionChecks.anyMatch(null, n -> true)).isFalse();
        }

        @Test
        @DisplayName("当predicate为null时应返回false")
        void shouldReturnFalseWhenPredicateIsNull() {
            assertThat(CollectionChecks.anyMatch(Arrays.asList(1, 2), null)).isFalse();
        }
    }

    @Test
    @DisplayName("noneMatch 全部不命中时应返回true")
    void noneMatchShouldReturnTrueWhenNoElementMatches() {
        List<Integer> list = Arrays.asList(1, 3, 5);
        assertThat(CollectionChecks.noneMatch(list, n -> n % 2 == 0)).isTrue();
    }

    @Test
    @DisplayName("uniqueElements 全部唯一时应返回true")
    void uniqueElementsShouldReturnTrueWhenAllUnique() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertThat(CollectionChecks.uniqueElements(list)).isTrue();
    }

    @Test
    @DisplayName("containsAll 对 null 入参分支覆盖")
    void containsAllHandlesNulls() {
        assertThat(CollectionChecks.containsAll(null, List.of(1))).isFalse();
        assertThat(CollectionChecks.containsAll(List.of(1), null)).isFalse();
        assertThat(CollectionChecks.containsAll(List.of(1), List.of(2))).isFalse();
    }

    @Test
    @DisplayName("noneMatch 对 null collection 分支覆盖")
    void noneMatchReturnsFalseWhenCollectionIsNull() {
        assertThat(CollectionChecks.noneMatch(null, n -> true)).isFalse();
    }

    @Test
    @DisplayName("noneMatch 对 null predicate 分支覆盖")
    void noneMatchReturnsFalseWhenPredicateIsNull() {
        assertThat(CollectionChecks.noneMatch(Arrays.asList(1, 2), null)).isFalse();
    }

    @Test
    @DisplayName("uniqueElements 对 null collection 分支覆盖")
    void uniqueElementsReturnsTrueWhenCollectionIsNull() {
        assertThat(CollectionChecks.uniqueElements(null)).isTrue();
    }
}
