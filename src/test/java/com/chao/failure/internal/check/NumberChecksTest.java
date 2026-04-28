package com.chao.failure.internal.check;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NumberChecks 工具类测试")
class NumberChecksTest {

    @Nested
    @DisplayName("positive 方法测试")
    class PositiveTest {
        @Test
        @DisplayName("当数值为正数时应返回true")
        void shouldReturnTrueWhenNumberIsPositive() {
            assertThat(NumberChecks.positive(1)).isTrue();
            assertThat(NumberChecks.positive(0.1)).isTrue();
        }

        @Test
        @DisplayName("当数值为0或负数时应返回false")
        void shouldReturnFalseWhenNumberIsNotPositive() {
            assertThat(NumberChecks.positive(0)).isFalse();
            assertThat(NumberChecks.positive(-1)).isFalse();
        }

        @Test
        @DisplayName("当数值为null时应返回false")
        void shouldReturnFalseWhenNumberIsNull() {
            assertThat(NumberChecks.positive(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("inRange 方法测试")
    class InRangeTest {
        @Test
        @DisplayName("当数值在范围内时应返回true")
        void shouldReturnTrueWhenNumberIsInRange() {
            assertThat(NumberChecks.inRange(5, 1, 10)).isTrue();
        }

        @Test
        @DisplayName("当数值不在范围内时应返回false")
        void shouldReturnFalseWhenNumberIsOutOfRange() {
            assertThat(NumberChecks.inRange(0, 1, 10)).isFalse();
            assertThat(NumberChecks.inRange(11, 1, 10)).isFalse();
        }

        @Test
        @DisplayName("当任一参数为null时应返回false")
        void shouldReturnFalseWhenAnyParameterIsNull() {
            assertThat(NumberChecks.inRange(null, 1, 10)).isFalse();
            assertThat(NumberChecks.inRange(5, null, 10)).isFalse();
            assertThat(NumberChecks.inRange(5, 1, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("inRangeNumber 方法测试")
    class InRangeNumberTest {
        @Test
        @DisplayName("当数值在范围内时应返回true")
        void shouldReturnTrueWhenNumberIsInRange() {
            assertThat(NumberChecks.inRangeNumber(5, 1, 10)).isTrue();
            assertThat(NumberChecks.inRangeNumber(5.5, 1.1, 10.9)).isTrue();
        }

        @Test
        @DisplayName("当数值不在范围内时应返回false")
        void shouldReturnFalseWhenNumberIsOutOfRange() {
            assertThat(NumberChecks.inRangeNumber(0, 1, 10)).isFalse();
            assertThat(NumberChecks.inRangeNumber(11, 1, 10)).isFalse();
        }

        @Test
        @DisplayName("当任一参数为null时应返回false")
        void shouldReturnFalseWhenAnyParameterIsNull() {
            assertThat(NumberChecks.inRangeNumber(null, 1, 10)).isFalse();
            assertThat(NumberChecks.inRangeNumber(5, null, 10)).isFalse();
            assertThat(NumberChecks.inRangeNumber(5, 1, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("nonNegative 方法测试")
    class NonNegativeTest {
        @Test
        @DisplayName("当数值为非负数时应返回true")
        void shouldReturnTrueWhenNumberIsNonNegative() {
            assertThat(NumberChecks.nonNegative(0)).isTrue();
            assertThat(NumberChecks.nonNegative(1)).isTrue();
        }

        @Test
        @DisplayName("当数值为负数时应返回false")
        void shouldReturnFalseWhenNumberIsNegative() {
            assertThat(NumberChecks.nonNegative(-1)).isFalse();
        }

        @Test
        @DisplayName("当数值为null时应返回false")
        void shouldReturnFalseWhenNumberIsNull() {
            assertThat(NumberChecks.nonNegative(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("greaterThan 方法测试")
    class GreaterThanTest {
        @Test
        void shouldReturnTrueWhenValueGreaterThanThreshold() {
            assertThat(NumberChecks.greaterThan(5, 3)).isTrue();
            assertThat(NumberChecks.greaterThan(5.5, 5.0)).isTrue();
        }

        @Test
        void shouldReturnFalseWhenValueNotGreaterThanThreshold() {
            assertThat(NumberChecks.greaterThan(3, 5)).isFalse();   // 小于
            assertThat(NumberChecks.greaterThan(5, 5)).isFalse();   // 等于
        }

        @Test
        void shouldReturnFalseWhenAnyParameterIsNull() {
            assertThat(NumberChecks.greaterThan(null, 5)).isFalse();
            assertThat(NumberChecks.greaterThan(5, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("greaterOrEqual 方法测试")
    class GreaterOrEqualTest {
        @Test
        void shouldReturnTrueWhenValueGreaterOrEqualThreshold() {
            assertThat(NumberChecks.greaterOrEqual(5, 3)).isTrue();   // 大于
            assertThat(NumberChecks.greaterOrEqual(5, 5)).isTrue();   // 等于
        }

        @Test
        void shouldReturnFalseWhenValueLessThanThreshold() {
            assertThat(NumberChecks.greaterOrEqual(3, 5)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenAnyParameterIsNull() {
            assertThat(NumberChecks.greaterOrEqual(null, 5)).isFalse();
            assertThat(NumberChecks.greaterOrEqual(5, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("lessThan 方法测试")
    class LessThanTest {
        @Test
        void shouldReturnTrueWhenValueLessThanThreshold() {
            assertThat(NumberChecks.lessThan(3, 5)).isTrue();
        }

        @Test
        void shouldReturnFalseWhenValueNotLessThanThreshold() {
            assertThat(NumberChecks.lessThan(5, 3)).isFalse();   // 大于
            assertThat(NumberChecks.lessThan(5, 5)).isFalse();   // 等于
        }

        @Test
        void shouldReturnFalseWhenAnyParameterIsNull() {
            assertThat(NumberChecks.lessThan(null, 5)).isFalse();
            assertThat(NumberChecks.lessThan(5, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("lessOrEqual 方法测试")
    class LessOrEqualTest {
        @Test
        void shouldReturnTrueWhenValueLessOrEqualThreshold() {
            assertThat(NumberChecks.lessOrEqual(3, 5)).isTrue();    // 小于
            assertThat(NumberChecks.lessOrEqual(5, 5)).isTrue();    // 等于
        }

        @Test
        void shouldReturnFalseWhenValueGreaterThanThreshold() {
            assertThat(NumberChecks.lessOrEqual(5, 3)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenAnyParameterIsNull() {
            assertThat(NumberChecks.lessOrEqual(null, 5)).isFalse();
            assertThat(NumberChecks.lessOrEqual(5, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("notZero 方法测试")
    class NotZeroTest {
        @Test
        void shouldReturnTrueWhenValueIsNotZero() {
            assertThat(NumberChecks.notZero(1)).isTrue();
            assertThat(NumberChecks.notZero(-1)).isTrue();
            assertThat(NumberChecks.notZero(0.1)).isTrue();
        }

        @Test
        void shouldReturnFalseWhenValueIsZero() {
            assertThat(NumberChecks.notZero(0)).isFalse();
            assertThat(NumberChecks.notZero(0.0)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenValueIsNull() {
            assertThat(NumberChecks.notZero(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isZero 方法测试")
    class IsZeroTest {
        @Test
        void shouldReturnTrueWhenValueIsZero() {
            assertThat(NumberChecks.isZero(0)).isTrue();
            assertThat(NumberChecks.isZero(0.0)).isTrue();
        }

        @Test
        void shouldReturnFalseWhenValueIsNotZero() {
            assertThat(NumberChecks.isZero(1)).isFalse();
            assertThat(NumberChecks.isZero(-1)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenValueIsNull() {
            assertThat(NumberChecks.isZero(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("negative 方法测试")
    class NegativeTest {
        @Test
        void shouldReturnTrueWhenValueIsNegative() {
            assertThat(NumberChecks.negative(-1)).isTrue();
            assertThat(NumberChecks.negative(-0.1)).isTrue();
        }

        @Test
        void shouldReturnFalseWhenValueIsNotNegative() {
            assertThat(NumberChecks.negative(0)).isFalse();
            assertThat(NumberChecks.negative(1)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenValueIsNull() {
            assertThat(NumberChecks.negative(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("multipleOf 方法测试")
    class MultipleOfTest {
        @Test
        void shouldReturnTrueWhenValueIsMultipleOfDivisor() {
            assertThat(NumberChecks.multipleOf(10, 2)).isTrue();
            assertThat(NumberChecks.multipleOf(9, 3)).isTrue();
            assertThat(NumberChecks.multipleOf(0, 5)).isTrue();  // 0 是任何数的倍数
        }

        @Test
        void shouldReturnFalseWhenValueIsNotMultipleOfDivisor() {
            assertThat(NumberChecks.multipleOf(10, 3)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenDivisorIsZero() {
            assertThat(NumberChecks.multipleOf(10, 0)).isFalse();  // 除数为0
        }

        @Test
        void shouldReturnFalseWhenAnyParameterIsNull() {
            assertThat(NumberChecks.multipleOf(null, 2)).isFalse();
            assertThat(NumberChecks.multipleOf(10, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("decimalScale 方法测试")
    class DecimalScaleTest {
        @Test
        void shouldReturnTrueWhenScaleMatches() {
            assertThat(NumberChecks.decimalScale(new java.math.BigDecimal("1.23"), 2)).isTrue();
            assertThat(NumberChecks.decimalScale(new java.math.BigDecimal("100"), 0)).isTrue();
        }

        @Test
        void shouldReturnFalseWhenScaleDoesNotMatch() {
            assertThat(NumberChecks.decimalScale(new java.math.BigDecimal("1.2"), 2)).isFalse();
            assertThat(NumberChecks.decimalScale(new java.math.BigDecimal("1.234"), 2)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenValueIsNull() {
            assertThat(NumberChecks.decimalScale(null, 2)).isFalse();
        }
    }
}
