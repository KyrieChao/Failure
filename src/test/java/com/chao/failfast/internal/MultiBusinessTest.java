package com.chao.failfast.internal;

import com.chao.failfast.i18n.I18nExtension;
import com.chao.failfast.model.TestResponseCode;
import com.chao.failfast.exception.Business;
import com.chao.failfast.exception.MultiBusiness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MultiBusiness 批量异常测试")
@ExtendWith(I18nExtension.class)
class MultiBusinessTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTest {
        @Test
        @DisplayName("应正确保存传入的错误列表")
        void shouldStoreErrorsCorrectly() {
            List<Business> errors = new ArrayList<>();
            errors.add(Business.of(TestResponseCode.PARAM_ERROR));
            errors.add(Business.of(TestResponseCode.SYSTEM_ERROR));

            MultiBusiness multiBusiness = new MultiBusiness(errors);
            assertThat(multiBusiness.getErrors()).hasSize(2);
            assertThat(multiBusiness.getErrors().get(0).getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
        }

        @Test
        @DisplayName("错误列表应为不可变副本")
        void errorsShouldBeImmutable() {
            List<Business> errors = new ArrayList<>();
            errors.add(Business.of(TestResponseCode.PARAM_ERROR));

            MultiBusiness multiBusiness = new MultiBusiness(errors);

            // Modify original list
            errors.add(Business.of(TestResponseCode.SYSTEM_ERROR));

            assertThat(multiBusiness.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("当错误数量超过限制时应截断")
        void shouldTruncateWhenErrorsExceedLimit() {
            List<Business> errors = new ArrayList<>();
            for (int i = 0; i < 60; i++) {
                errors.add(Business.of(TestResponseCode.PARAM_ERROR));
            }

            MultiBusiness multiBusiness = new MultiBusiness(errors);
            assertThat(multiBusiness.getErrors()).hasSize(50);
        }
    }

    @Nested
    @DisplayName("toString 方法测试")
    class ToStringTest {

        @Test
        @DisplayName("toString 应包含所有错误信息")
        void shouldFormatAllErrors() {
            List<Business> errors = new ArrayList<>();
            errors.add(Business.of(TestResponseCode.PARAM_ERROR));
            errors.add(Business.of(TestResponseCode.SYSTEM_ERROR));

            MultiBusiness multiBusiness = new MultiBusiness(errors);
            String str = multiBusiness.toString();

            assertThat(str).startsWith("Multi={");
            assertThat(str).contains("1.");
            assertThat(str).contains("2.");
            assertThat(str).endsWith("}");
        }

        @Test
        @DisplayName("toString 应正确处理单个错误")
        void shouldHandleSingleError() {
            List<Business> errors = List.of(Business.of(TestResponseCode.PARAM_ERROR));

            MultiBusiness multiBusiness = new MultiBusiness(errors);
            String str = multiBusiness.toString();

            assertThat(str).contains("1.");
            assertThat(str).doesNotContain("2.");
        }

        @Test
        @DisplayName("toString 应处理超过 MAX_ERRORS 的情况")
        void shouldHandleTruncatedErrors() {
            List<Business> errors = new ArrayList<>();
            for (int i = 0; i < 55; i++) {
                errors.add(Business.of(TestResponseCode.PARAM_ERROR));
            }

            MultiBusiness multiBusiness = new MultiBusiness(errors);
            String str = multiBusiness.toString();

            // 只包含 50 个错误
            assertThat(multiBusiness.getErrors()).hasSize(50);
            assertThat(str).contains("1.");
            // 最后一个应该是 50
            assertThat(str).contains("50.");
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("当错误数量正好为 50 时不应截断")
        void shouldNotTruncateWhenExactly50() {
            List<Business> errors = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                errors.add(Business.of(TestResponseCode.PARAM_ERROR));
            }

            MultiBusiness multiBusiness = new MultiBusiness(errors);

            assertThat(multiBusiness.getErrors()).hasSize(50);
            // description 应该是 "50 项校验失败" 而不是 "校验失败，错误过多"
            assertThat(multiBusiness.getResponseCode().getDescription()).contains("50");
        }

        @Test
        @DisplayName("当错误数量为 51 时应截断为 50")
        void shouldTruncateWhen51() {
            List<Business> errors = new ArrayList<>();
            for (int i = 0; i < 51; i++) {
                errors.add(Business.of(TestResponseCode.PARAM_ERROR));
            }

            MultiBusiness multiBusiness = new MultiBusiness(errors);

            assertThat(multiBusiness.getErrors()).hasSize(50);
            // 校验失败，错误过多 (Translation) or {failure.const.too.many.errors} (Key)
            assertThat(multiBusiness.getResponseCode().getDescription()).satisfiesAnyOf(
                s -> assertThat(s).contains("错误过多"),
                s -> assertThat(s).contains("{failure.const.too.many.errors}")
            );
        }

        @Test
        @DisplayName("空错误列表应正确处理")
        void shouldHandleEmptyList() {
            List<Business> errors = new ArrayList<>();

            MultiBusiness multiBusiness = new MultiBusiness(errors);

            assertThat(multiBusiness.getErrors()).isEmpty();
            assertThat(multiBusiness.getResponseCode().getDescription()).contains("0");
        }

        @Test
        @DisplayName("httpStatus 应为第一个错误的 HTTP 状态码")
        void shouldHaveFirstErrorHttpStatus() {
            List<Business> errors = List.of(Business.of(TestResponseCode.PARAM_ERROR));

            MultiBusiness multiBusiness = new MultiBusiness(errors);

            // MultiBusiness 应该继承第一个 Business 对象的 HTTP 状态码
            assertThat(multiBusiness.getHttpStatus()).isEqualTo(errors.get(0).getHttpStatus());
        }
    }
}
