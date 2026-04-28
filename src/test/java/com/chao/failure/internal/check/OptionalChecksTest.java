package com.chao.failure.internal.check;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Optional校验工具测试")
class OptionalChecksTest {

    @Nested
    @DisplayName("isPresent 测试")
    class IsPresentTest {
        @Test
        @DisplayName("Optional 为 null 时应返回 false")
        void shouldReturnFalseWhenOptionalIsNull() {
            assertThat(OptionalChecks.isPresent(null)).isFalse();
        }

        @Test
        @DisplayName("Optional 为 empty 时应返回 false")
        void shouldReturnFalseWhenOptionalIsEmpty() {
            assertThat(OptionalChecks.isPresent(Optional.empty())).isFalse();
        }

        @Test
        @DisplayName("Optional 有值时应返回 true")
        void shouldReturnTrueWhenOptionalIsPresent() {
            assertThat(OptionalChecks.isPresent(Optional.of("value"))).isTrue();
        }
    }

    @Nested
    @DisplayName("isEmpty 测试")
    class IsEmptyTest {
        @Test
        @DisplayName("Optional 为 null 时应返回 true")
        void shouldReturnTrueWhenOptionalIsNull() {
            assertThat(OptionalChecks.isEmpty(null)).isTrue();
        }

        @Test
        @DisplayName("Optional 为 empty 时应返回 true")
        void shouldReturnTrueWhenOptionalIsEmpty() {
            assertThat(OptionalChecks.isEmpty(Optional.empty())).isTrue();
        }

        @Test
        @DisplayName("Optional 有值时应返回 false")
        void shouldReturnFalseWhenOptionalIsPresent() {
            assertThat(OptionalChecks.isEmpty(Optional.of("value"))).isFalse();
        }
    }
}
