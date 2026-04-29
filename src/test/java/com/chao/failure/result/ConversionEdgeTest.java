package com.chao.failure.result;

import com.chao.failure.model.TestResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Nested
@DisplayName("转换操作边界测试")
class ConversionEdgeTest {

    @Test
    @DisplayName("getOrElseGet 成功时不应调用 errorHandler")
    void getOrElseGetShouldNotCallHandlerWhenSuccess() {
        Result<String> result = Result.success("test");
        boolean[] called = {false};

        String value = result.getOrElseGet(e -> {
            called[0] = true;
            return "default";
        });

        assertThat(value).isEqualTo("test");
        assertThat(called[0]).isFalse();
    }

    @Test
    @DisplayName("fold 返回 null 时应创建包含 null 的成功 Result")
    void foldShouldAllowNullReturn() {
        Result<Integer> result = Result.success(10);
        Result<String> folded = result.fold(
                i -> null,
                e -> "error"
        );

        assertThat(folded.isSuccess()).isTrue();
        assertThat(folded.get()).isNull();
    }

    @Test
    @DisplayName("swap 成功时应转为失败")
    void swapShouldConvertSuccessToFailure() {
        Result<String> result = Result.success("test");
        Result<String> swapped = result.swap(TestResponseCode.SYSTEM_ERROR);

        assertThat(swapped.isFail()).isTrue();
        assertThat(swapped.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.SYSTEM_ERROR.getCode());
        assertThat(swapped.getError().getDetail()).isEqualTo("Success result swapped to failure");
    }

    @Test
    @DisplayName("swap 失败时应转为成功 null")
    void swapShouldConvertFailureToSuccessWithNull() {
        Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
        Result<String> swapped = result.swap(TestResponseCode.SYSTEM_ERROR);

        assertThat(swapped.isSuccess()).isTrue();
        assertThat(swapped.get()).isNull();
    }

    @Test
    @DisplayName("swap 的 code 为 null 时应由 Result.fail 抛出异常")
    void swapShouldThrowExceptionWhenCodeIsNull() {
        Result<String> result = Result.success("test");

        // Result.fail 会校验 code 不能为空，抛出 IllegalArgumentException
        assertThatThrownBy(() -> result.swap(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("code");
    }
}