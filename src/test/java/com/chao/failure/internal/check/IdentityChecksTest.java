package com.chao.failure.internal.check;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IdentityChecks 工具类测试")
class IdentityChecksTest {

    @Nested
    @DisplayName("same 方法测试")
    class SameTest {
        @Test
        @DisplayName("当对象引用相同时应返回true")
        void shouldReturnTrueWhenObjectsAreSame() {
            Object obj = new Object();
            assertThat(IdentityChecks.same(obj, obj)).isTrue();
        }

        @Test
        @DisplayName("当对象引用不同时应返回false")
        void shouldReturnFalseWhenObjectsAreNotSame() {
            assertThat(IdentityChecks.same(new Object(), new Object())).isFalse();
        }

        @Test
        @DisplayName("当两个对象都为null时应返回true")
        void shouldReturnTrueWhenBothAreNull() {
            assertThat(IdentityChecks.same(null, null)).isTrue();
        }
    }

    @Nested
    @DisplayName("notSame 方法测试")
    class NotSameTest {
        @Test
        @DisplayName("当对象引用不同时应返回true")
        void shouldReturnTrueWhenObjectsAreNotSame() {
            assertThat(IdentityChecks.notSame(new Object(), new Object())).isTrue();
        }

        @Test
        @DisplayName("当对象引用相同时应返回false")
        void shouldReturnFalseWhenObjectsAreSame() {
            Object obj = new Object();
            assertThat(IdentityChecks.notSame(obj, obj)).isFalse();
        }
    }

    @Nested
    @DisplayName("equals 方法测试")
    class EqualsTest {
        @Test
        @DisplayName("当对象相等时应返回true")
        void shouldReturnTrueWhenObjectsAreEqual() {
            assertThat(IdentityChecks.equals("abc", "abc")).isTrue();
        }

        @Test
        @DisplayName("当对象不相等时应返回false")
        void shouldReturnFalseWhenObjectsAreNotEqual() {
            assertThat(IdentityChecks.equals("abc", "def")).isFalse();
        }

        @Test
        @DisplayName("当两个对象都为null时应返回true")
        void shouldReturnTrueWhenBothAreNull() {
            assertThat(IdentityChecks.equals(null, null)).isTrue();
        }

        @Test
        @DisplayName("当其中一个对象为null时应返回false")
        void shouldReturnFalseWhenOneIsNull() {
            assertThat(IdentityChecks.equals(null, "abc")).isFalse();
            assertThat(IdentityChecks.equals("abc", null)).isFalse();
        }
    }

    @Nested
    @DisplayName("notEquals 方法测试")
    class NotEqualsTest {
        @Test
        @DisplayName("当对象不相等时应返回true")
        void shouldReturnTrueWhenObjectsAreNotEqual() {
            assertThat(IdentityChecks.notEquals("abc", "def")).isTrue();
        }

        @Test
        @DisplayName("当对象相等时应返回false")
        void shouldReturnFalseWhenObjectsAreEqual() {
            assertThat(IdentityChecks.notEquals("abc", "abc")).isFalse();
        }
    }
}
