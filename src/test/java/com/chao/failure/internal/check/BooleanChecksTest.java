package com.chao.failure.internal.check;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BooleanChecks 工具类测试")
class BooleanChecksTest {

    @Nested
    @DisplayName("state 方法测试")
    class StateTest {
        @Test
        @DisplayName("当状态为true时应返回true")
        void shouldReturnTrueWhenStateIsTrue() {
            assertThat(BooleanChecks.state(true)).isTrue();
        }

        @Test
        @DisplayName("当状态为false时应返回false")
        void shouldReturnFalseWhenStateIsFalse() {
            assertThat(BooleanChecks.state(false)).isFalse();
        }
    }

    @Nested
    @DisplayName("isTrue 方法测试")
    class IsTrueTest {
        @Test
        @DisplayName("当条件为true时应返回true")
        void shouldReturnTrueWhenConditionIsTrue() {
            assertThat(BooleanChecks.isTrue(true)).isTrue();
        }

        @Test
        @DisplayName("当条件为false时应返回false")
        void shouldReturnFalseWhenConditionIsFalse() {
            assertThat(BooleanChecks.isTrue(false)).isFalse();
        }
    }

    @Nested
    @DisplayName("isFalse 方法测试")
    class IsFalseTest {
        @Test
        @DisplayName("当条件为false时应返回true")
        void shouldReturnTrueWhenConditionIsFalse() {
            assertThat(BooleanChecks.isFalse(false)).isTrue();
        }

        @Test
        @DisplayName("当条件为true时应返回false")
        void shouldReturnFalseWhenConditionIsTrue() {
            assertThat(BooleanChecks.isFalse(true)).isFalse();
        }
    }
}
