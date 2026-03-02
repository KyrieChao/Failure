package com.chao.failfast.test;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.MultiBusiness;
import com.chao.failfast.model.TestResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Failure 核心功能测试")
class FailTest {

    @Nested
    @DisplayName("Fail-Fast 模式测试")
    class FailFastModeTest {

        @Test
        @DisplayName("应当在第一个错误时立即抛出 Business 异常")
        void shouldThrowImmediatelyOnFirstError() {
            Throwable thrown = catchThrowable(() ->
                    Failure.begin()
                            .notNull(null, TestResponseCode.PARAM_ERROR) // 错误 1
                            .isTrue(false, TestResponseCode.SYSTEM_ERROR) // 错误 2 (不应执行)
                            .fail()
            );

            assertThat(thrown).isInstanceOf(Business.class);
            Business business = (Business) thrown;
            assertThat(business.getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
        }

        @Test
        @DisplayName("当所有校验通过时应当无异常")
        void shouldNotThrowWhenAllChecksPass() {
            assertDoesNotThrow(() ->
                    Failure.begin()
                            .notNull(new Object(), TestResponseCode.PARAM_ERROR)
                            .isTrue(true, TestResponseCode.SYSTEM_ERROR)
                            .fail()
            );
        }
    }

    @Nested
    @DisplayName("Fail-Strict 模式测试")
    class FailStrictModeTest {

        @Test
        @DisplayName("应当收集所有错误并抛出 MultiBusiness 异常")
        void shouldCollectAllErrors() {
            Throwable thrown = catchThrowable(() ->
                    Failure.strict()
                            .notNull(null, TestResponseCode.PARAM_ERROR) // 错误 1
                            .isTrue(false, TestResponseCode.SYSTEM_ERROR) // 错误 2
                            .failAll()
            );

            assertThat(thrown).isInstanceOf(MultiBusiness.class);
            MultiBusiness multiBusiness = (MultiBusiness) thrown;
            assertThat(multiBusiness.getErrors()).hasSize(2);
            assertThat(multiBusiness.getErrors().get(0).getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
            assertThat(multiBusiness.getErrors().get(1).getResponseCode().getCode()).isEqualTo(TestResponseCode.SYSTEM_ERROR.getCode());
        }

        @Test
        @DisplayName("当只有一个错误时应当抛出单个 Business 异常")
        void shouldThrowSingleBusinessWhenOnlyOneError() {
            Throwable thrown = catchThrowable(() ->
                    Failure.strict()
                            .notNull(null, TestResponseCode.PARAM_ERROR)
                            .isTrue(true, TestResponseCode.SYSTEM_ERROR)
                            .failAll()
            );

            assertThat(thrown).isInstanceOf(Business.class);
            assertThat(thrown).isNotInstanceOf(MultiBusiness.class);
            Business business = (Business) thrown;
            assertThat(business.getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("常用校验方法测试")
    class CommonChecksTest {

        @Test
        @DisplayName("notNull 测试")
        void testNotNull() {
            assertDoesNotThrow(() -> Failure.begin().notNull(new Object()).fail());
            assertThat(catchThrowable(() -> Failure.begin().notNull(null).fail())).isInstanceOf(Business.class);
        }

        @Test
        @DisplayName("notBlank 测试")
        void testNotBlank() {
            assertDoesNotThrow(() -> Failure.begin().notBlank("abc").fail());
            assertThat(catchThrowable(() -> Failure.begin().notBlank("").fail())).isInstanceOf(Business.class);
            assertThat(catchThrowable(() -> Failure.begin().notBlank(null).fail())).isInstanceOf(Business.class);
        }

        @Test
        @DisplayName("matches (正则) 测试")
        void testMatches() {
            assertDoesNotThrow(() -> Failure.begin().match("123", "\\d+").fail());
            assertThat(catchThrowable(() -> Failure.begin().match("abc", "\\d+").fail())).isInstanceOf(Business.class);
        }

        @Test
        @DisplayName("positive (数值) 测试")
        void testPositive() {
            assertDoesNotThrow(() -> Failure.begin().positive(1).fail());
            assertThat(catchThrowable(() -> Failure.begin().positive(-1).fail())).isInstanceOf(Business.class);
        }

        @Test
        @DisplayName("集合校验测试")
        void testCollection() {
            List<String> list = Arrays.asList("A", "B");
            assertDoesNotThrow(() -> Failure.begin().notEmpty(list).fail());
            assertThat(catchThrowable(() -> Failure.begin().notEmpty(Collections.emptyList()).fail())).isInstanceOf(Business.class);
        }

        @Test
        @DisplayName("日期校验测试")
        void testDate() {
            Date now = new Date();
            Date future = new Date(now.getTime() + 10000);
            assertDoesNotThrow(() -> Failure.begin().after(future, now).fail());
            assertThat(catchThrowable(() -> Failure.begin().before(future, now).fail())).isInstanceOf(Business.class);
        }
    }
}
