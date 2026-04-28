package com.chao.failure.internal.check;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EnumChecks 工具类测试")
class EnumChecksTest {

    enum TestEnum {
        VALUE1, VALUE2
    }

    @Nested
    @DisplayName("enumValue 方法测试")
    class EnumValueTest {
        @Test
        @DisplayName("当字符串是有效的枚举值时应返回true")
        void shouldReturnTrueWhenValueIsEnumValue() {
            assertThat(EnumChecks.enumValue(TestEnum.class, "VALUE1")).isTrue();
            assertThat(EnumChecks.enumValue(TestEnum.class, "VALUE2")).isTrue();
        }

        @Test
        @DisplayName("当字符串不是有效的枚举值时应返回false")
        void shouldReturnFalseWhenValueIsNotEnumValue() {
            assertThat(EnumChecks.enumValue(TestEnum.class, "INVALID")).isFalse();
        }

        @Test
        @DisplayName("当字符串为null时应返回false")
        void shouldReturnFalseWhenValueIsNull() {
            assertThat(EnumChecks.enumValue(TestEnum.class, null)).isFalse();
        }

        @Test
        @DisplayName("当枚举类为null时应返回false")
        void shouldReturnFalseWhenEnumTypeIsNull() {
            assertThat(EnumChecks.enumValue(null, "VALUE1")).isFalse();
        }
    }

    @Nested
    @DisplayName("enumConstant 方法测试")
    class EnumConstantTest {
        @Test
        @DisplayName("当值是有效的枚举常量时应返回true")
        void shouldReturnTrueWhenValueIsEnumConstant() {
            assertThat(EnumChecks.enumConstant(TestEnum.VALUE1, TestEnum.class)).isTrue();
        }

        @Test
        @DisplayName("当值不匹配枚举类型时应返回false") // 实际上编译器会报错，但这里测试类型匹配
        void shouldReturnFalseWhenTypeMismatch() {
            // 这里泛型限制了E，所以只能测null
            assertThat(EnumChecks.enumConstant(null, TestEnum.class)).isFalse();
            assertThat(EnumChecks.enumConstant(TestEnum.VALUE1, null)).isFalse();
        }
    }
}
