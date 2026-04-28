package com.chao.failure.internal.check;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StringChecks 工具类测试")
class StringChecksTest {

    @Nested
    @DisplayName("blank 方法测试")
    class BlankTest {
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "  ", "\t", "\n"})
        @DisplayName("当字符串为空或空白时应返回true")
        void shouldReturnTrueWhenStringIsBlank(String input) {
            assertThat(StringChecks.blank(input)).isTrue();
        }

        @Test
        @DisplayName("当字符串非空时应返回false")
        void shouldReturnFalseWhenStringIsNotBlank() {
            assertThat(StringChecks.blank("abc")).isFalse();
        }
    }

    @Nested
    @DisplayName("notBlank 方法测试")
    class NotBlankTest {
        @Test
        @DisplayName("当字符串非空时应返回true")
        void shouldReturnTrueWhenStringIsNotBlank() {
            assertThat(StringChecks.notBlank("abc")).isTrue();
            assertThat(StringChecks.notBlank(" a ")).isTrue();
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "  "})
        @DisplayName("当字符串为空或空白时应返回false")
        void shouldReturnFalseWhenStringIsBlank(String input) {
            assertThat(StringChecks.notBlank(input)).isFalse();
        }
    }

    @Nested
    @DisplayName("lengthBetween 方法测试")
    class LengthBetweenTest {
        @Test
        @DisplayName("当字符串长度在范围内时应返回true")
        void shouldReturnTrueWhenLengthIsInRange() {
            assertThat(StringChecks.lengthBetween("abc", 1, 3)).isTrue();
            assertThat(StringChecks.lengthBetween("abc", 3, 5)).isTrue();
        }

        @Test
        @DisplayName("当字符串长度不在范围内时应返回false")
        void shouldReturnFalseWhenLengthIsOutOfRange() {
            assertThat(StringChecks.lengthBetween("abc", 4, 5)).isFalse();
            assertThat(StringChecks.lengthBetween("abc", 1, 2)).isFalse();
        }

        @Test
        @DisplayName("当字符串为null时应返回false")
        void shouldReturnFalseWhenStringIsNull() {
            assertThat(StringChecks.lengthBetween(null, 1, 3)).isFalse();
        }
    }

    @Nested
    @DisplayName("match 方法测试")
    class MatchTest {
        @Test
        @DisplayName("当字符串匹配正则时应返回true")
        void shouldReturnTrueWhenStringMatchesRegex() {
            assertThat(StringChecks.match("123", "\\d+")).isTrue();
        }

        @Test
        @DisplayName("当字符串不匹配正则时应返回false")
        void shouldReturnFalseWhenStringDoesNotMatchRegex() {
            assertThat(StringChecks.match("abc", "\\d+")).isFalse();
        }

        @Test
        @DisplayName("当字符串为null时应返回false")
        void shouldReturnFalseWhenStringIsNull() {
            assertThat(StringChecks.match(null, "\\d+")).isFalse();
        }
    }

    @Nested
    @DisplayName("email 方法测试")
    class EmailTest {
        @ParameterizedTest
        @ValueSource(strings = {"test@example.com", "user.name@domain.co.jp"})
        @DisplayName("当邮箱格式正确时应返回true")
        void shouldReturnTrueWhenEmailIsValid(String email) {
            assertThat(StringChecks.email(email)).isTrue();
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"invalid-email", "test@", "@example.com"})
        @DisplayName("当邮箱格式错误时应返回false")
        void shouldReturnFalseWhenEmailIsInvalid(String email) {
            assertThat(StringChecks.email(email)).isFalse();
        }
    }

    @Nested
    @DisplayName("startsWith 方法测试")
    class StartsWithTest {
        @Test
        @DisplayName("当字符串以前缀开头时应返回true")
        void shouldReturnTrueWhenStringStartsWithPrefix() {
            assertThat(StringChecks.startsWith("abcdef", "abc")).isTrue();
        }

        @Test
        @DisplayName("当字符串不以前缀开头时应返回false")
        void shouldReturnFalseWhenStringDoesNotStartWithPrefix() {
            assertThat(StringChecks.startsWith("abcdef", "def")).isFalse();
        }

        @Test
        void shouldReturnFalseWhenStringIsNull() {
            assertThat(StringChecks.startsWith(null, "abc")).isFalse();
        }
    }

    @Nested
    @DisplayName("endsWith 方法测试")
    class EndsWithTest {
        @Test
        @DisplayName("当字符串以后缀结尾时应返回true")
        void shouldReturnTrueWhenStringEndsWithSuffix() {
            assertThat(StringChecks.endsWith("abcdef", "def")).isTrue();
        }

        @Test
        @DisplayName("当字符串不以后缀结尾时应返回false")
        void shouldReturnFalseWhenStringDoesNotEndWithSuffix() {
            assertThat(StringChecks.endsWith("abcdef", "abc")).isFalse();
        }

        @Test
        void shouldReturnFalseWhenStringIsNull() {
            assertThat(StringChecks.endsWith(null, "def")).isFalse();
        }
    }

    @Nested
    @DisplayName("contains 方法测试")
    class ContainsTest {
        @Test
        void shouldReturnTrueWhenStringContainsSubstring() {
            assertThat(StringChecks.contains("abc", "b")).isTrue();
        }

        @Test
        void shouldReturnFalseWhenStringDoesNotContainSubstring() {
            assertThat(StringChecks.contains("abc", "d")).isFalse();
        }

        @Test
        void shouldReturnFalseWhenInputIsNull() {
            assertThat(StringChecks.contains(null, "b")).isFalse();
            assertThat(StringChecks.contains("abc", null)).isFalse();
        }
    }

    @Nested
    @DisplayName("notContains 方法测试")
    class NotContainsTest {
        @Test
        void shouldReturnTrueWhenStringDoesNotContainSubstring() {
            assertThat(StringChecks.notContains("abc", "d")).isTrue();
        }

        @Test
        void shouldReturnFalseWhenStringContainsSubstring() {
            assertThat(StringChecks.notContains("abc", "b")).isFalse();
        }

        @Test
        void shouldReturnTrueWhenInputIsNull() {
            assertThat(StringChecks.notContains(null, "b")).isTrue();
            assertThat(StringChecks.notContains("abc", null)).isTrue();
        }
    }

    @Nested
    @DisplayName("lengthMin/Max 方法测试")
    class LengthMinMaxTest {
        @Test
        void shouldCheckMinLength() {
            assertThat(StringChecks.lengthMin("abc", 3)).isTrue();
            assertThat(StringChecks.lengthMin("abc", 4)).isFalse();
            assertThat(StringChecks.lengthMin(null, 1)).isFalse();
        }

        @Test
        void shouldCheckMaxLength() {
            assertThat(StringChecks.lengthMax("abc", 3)).isTrue();
            assertThat(StringChecks.lengthMax("abc", 2)).isFalse();
            assertThat(StringChecks.lengthMax(null, 5)).isFalse();
        }
    }

    @Nested
    @DisplayName("character check 方法测试")
    class CharacterCheckTest {
        @Test
        void shouldCheckIsNumeric() {
            assertThat(StringChecks.isNumeric("123")).isTrue();
            assertThat(StringChecks.isNumeric("12a")).isFalse();
            assertThat(StringChecks.isNumeric(null)).isFalse();
            assertThat(StringChecks.isNumeric("")).isFalse();
        }

        @Test
        void shouldCheckIsAlpha() {
            assertThat(StringChecks.isAlpha("abc")).isTrue();
            assertThat(StringChecks.isAlpha("123")).isFalse();
            assertThat(StringChecks.isAlpha(null)).isFalse();
            assertThat(StringChecks.isAlpha("")).isFalse();
        }

        @Test
        void shouldCheckIsAlphanumeric() {
            assertThat(StringChecks.isAlphanumeric("abc123")).isTrue();
            assertThat(StringChecks.isAlphanumeric("abc-123")).isFalse();
            assertThat(StringChecks.isAlphanumeric(null)).isFalse();
            assertThat(StringChecks.isAlphanumeric("")).isFalse();
        }
    }

    @Nested
    @DisplayName("case check 方法测试")
    class CaseCheckTest {
        @Test
        void shouldCheckIsLowerCase() {
            assertThat(StringChecks.isLowerCase("abc")).isTrue();
            assertThat(StringChecks.isLowerCase("Abc")).isFalse();
            assertThat(StringChecks.isLowerCase(null)).isFalse();
        }

        @Test
        void shouldCheckIsUpperCase() {
            assertThat(StringChecks.isUpperCase("ABC")).isTrue();
            assertThat(StringChecks.isUpperCase("Abc")).isFalse();
            assertThat(StringChecks.isUpperCase(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("special format 方法测试")
    class SpecialFormatTest {
        @Test
        void shouldCheckMobile() {
            assertThat(StringChecks.mobile("13800138000")).isTrue();
            assertThat(StringChecks.mobile("123")).isFalse();
            assertThat(StringChecks.mobile(null)).isFalse();
        }

        @Test
        void shouldCheckUrl() {
            assertThat(StringChecks.url("http://www.google.com")).isTrue();
            assertThat(StringChecks.url("https://example.com")).isTrue();
            assertThat(StringChecks.url("ftp://example.com")).isTrue();
            assertThat(StringChecks.url(null)).isFalse();
        }

        @Test
        void shouldCheckIpAddress() {
            assertThat(StringChecks.ipAddress("192.168.1.1")).isTrue();
            assertThat(StringChecks.ipAddress("256.0.0.1")).isFalse();
            assertThat(StringChecks.ipAddress(null)).isFalse();
        }

        @Test
        void shouldCheckUuid() {
            assertThat(StringChecks.uuid("123e4567-e89b-12d3-a456-426614174000")).isTrue();
            assertThat(StringChecks.uuid("invalid-uuid")).isFalse();
            assertThat(StringChecks.uuid(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("equalsIgnoreCase 方法测试")
    class EqualsIgnoreCaseTest {
        @Test
        @DisplayName("当字符串忽略大小写相等时应返回true")
        void shouldReturnTrueWhenStringsAreEqualIgnoreCase() {
            assertThat(StringChecks.equalsIgnoreCase("abc", "ABC")).isTrue();
            assertThat(StringChecks.equalsIgnoreCase("abc", "abc")).isTrue();  // 补：相同字符串
        }

        @Test
        @DisplayName("当字符串不相等时应返回false")
        void shouldReturnFalseWhenStringsAreNotEqual() {
            assertThat(StringChecks.equalsIgnoreCase("abc", "def")).isFalse();
            assertThat(StringChecks.equalsIgnoreCase("abc", "ABC ")).isFalse(); // 补：带空格
        }

        @Test
        @DisplayName("当任一字符串为null时应返回false")
        void shouldReturnFalseWhenEitherStringIsNull() {
            assertThat(StringChecks.equalsIgnoreCase(null, "abc")).isFalse();
            assertThat(StringChecks.equalsIgnoreCase("abc", null)).isFalse();
            // 补：都非null但内容不同
            assertThat(StringChecks.equalsIgnoreCase("abc", "def")).isFalse();
        }
    }

    @Nested
    @DisplayName("新特性校验测试")
    class NewFeaturesTest {
        @Test
        @DisplayName("JSON校验")
        void isJson() {
            assertThat(StringChecks.isJson("{\"a\":1}")).isTrue();
            assertThat(StringChecks.isJson("[1, 2]")).isTrue();
            assertThat(StringChecks.isJson("{invalid}")).isFalse();
            assertThat(StringChecks.isJson(null)).isFalse();
        }

        @Test
        @DisplayName("Base64校验")
        void isBase64() {
            assertThat(StringChecks.isBase64("SGVsbG8=")).isTrue();
            // "InvalidBase64!" might be valid if characters are in alphabet, but padding might be wrong.
            // "!!!!" is definitely invalid.
            assertThat(StringChecks.isBase64("!!!!")).isFalse();
            assertThat(StringChecks.isBase64(null)).isFalse();
        }

        @Test
        @DisplayName("信用卡校验(Luhn)")
        void isCreditCard() {
            // Valid Luhn
            assertThat(StringChecks.isCreditCard("79927398713")).isTrue();
            // Invalid
            assertThat(StringChecks.isCreditCard("79927398710")).isFalse();
            assertThat(StringChecks.isCreditCard("abc")).isFalse();
        }

        @Test
        @DisplayName("URL严格校验")
        void urlStrict() {
            assertThat(StringChecks.url("https://google.com")).isTrue();
            assertThat(StringChecks.url("ftp://example.com")).isTrue(); // URI allows ftp
            assertThat(StringChecks.url("google.com")).isFalse();
            assertThat(StringChecks.url("mailto:test@example.com")).isFalse();
            assertThat(StringChecks.url("not a url")).isFalse();
        }
    }
}
