package com.chao.failure.result;

import com.chao.failure.model.TestResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Nested
@DisplayName("转换操作测试")
class ConversionOpsTest {

    @Test
    @DisplayName("toOptional 成功时应返回包含值的 Optional")
    void toOptionalShouldReturnPresentOptionalWhenSuccess() {
        Result<String> result = Result.success("test");
        Optional<String> optional = result.toOptional();

        assertThat(optional).isPresent();
        assertThat(optional.get()).isEqualTo("test");
    }

    @Test
    @DisplayName("toOptional 成功且值为 null 时应返回 empty Optional")
    void toOptionalShouldReturnEmptyOptionalWhenSuccessWithNull() {
        Result<String> result = Result.success(null);
        Optional<String> optional = result.toOptional();

        assertThat(optional).isEmpty();
    }

    @Test
    @DisplayName("toOptional 失败时应返回 empty Optional")
    void toOptionalShouldReturnEmptyOptionalWhenFailure() {
        Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
        Optional<String> optional = result.toOptional();

        assertThat(optional).isEmpty();
    }

    @Test
    @DisplayName("stream 成功时应返回单元素流")
    void streamShouldReturnSingleElementStreamWhenSuccess() {
        Result<String> result = Result.success("test");
        List<String> list = result.stream().toList();

        assertThat(list).containsExactly("test");
    }

    @Test
    @DisplayName("stream 成功且值为 null 时应返回空流")
    void streamShouldReturnEmptyStreamWhenSuccessWithNull() {
        Result<String> result = Result.success(null);
        List<String> list = result.stream().toList();

        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("stream 失败时应返回空流")
    void streamShouldReturnEmptyStreamWhenFailure() {
        Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
        List<String> list = result.stream().toList();

        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("getOrElse 成功时应返回值")
    void getOrElseShouldReturnValueWhenSuccess() {
        Result<String> result = Result.success("test");
        assertThat(result.getOrElse("default")).isEqualTo("test");
    }

    @Test
    @DisplayName("getOrElse 失败时应返回默认值")
    void getOrElseShouldReturnDefaultWhenFailure() {
        Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
        assertThat(result.getOrElse("default")).isEqualTo("default");
    }

    @Test
    @DisplayName("getOrElse 成功且值为 null 时应返回 null")
    void getOrElseShouldReturnNullWhenSuccessWithNull() {
        Result<String> result = Result.success(null);
        assertThat(result.getOrElse("default")).isNull();
    }

    @Test
    @DisplayName("getOrElseGet 成功时应返回值")
    void getOrElseGetShouldReturnValueWhenSuccess() {
        Result<String> result = Result.success("test");
        assertThat(result.getOrElseGet(e -> "default")).isEqualTo("test");
    }

    @Test
    @DisplayName("getOrElseGet 失败时应从错误计算值")
    void getOrElseGetShouldComputeFromErrorWhenFailure() {
        Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
        assertThat(result.getOrElseGet(e -> "error: " + e.getMessage())).isEqualTo("error: " + TestResponseCode.PARAM_ERROR.getMessage());
    }

    @Test
    @DisplayName("getOrElseGet 失败时 errorHandler 抛出异常应继续抛出")
    void getOrElseGetShouldRethrowExceptionFromHandler() {
        Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);

        assertThatThrownBy(() -> result.getOrElseGet(e -> {
            throw new RuntimeException("handler error");
        })).isInstanceOf(RuntimeException.class).hasMessage("handler error");
    }

    @Test
    @DisplayName("fold 成功时应应用 successFn")
    void foldShouldApplySuccessFnWhenSuccess() {
        Result<Integer> result = Result.success(10);
        Result<String> folded = result.fold(
                i -> "success: " + i,
                e -> "error: " + e.getMessage()
        );

        assertThat(folded.isSuccess()).isTrue();
        assertThat(folded.get()).isEqualTo("success: 10");
    }

    @Test
    @DisplayName("fold 失败时应应用 failureFn")
    void foldShouldApplyFailureFnWhenFailure() {
        Result<Integer> result = Result.fail(TestResponseCode.PARAM_ERROR);
        Result<String> folded = result.fold(
                i -> "success: " + i,
                e -> "error: " + e.getMessage()
        );

        assertThat(folded.isSuccess()).isTrue();
        assertThat(folded.get()).isEqualTo("error: " + TestResponseCode.PARAM_ERROR.getMessage());
    }

    @Test
    @DisplayName("fold 中 successFn 抛出异常应继续抛出")
    void foldShouldRethrowExceptionFromSuccessFn() {
        Result<Integer> result = Result.success(10);

        assertThatThrownBy(() -> result.fold(
                i -> {
                    throw new RuntimeException("success error");
                },
                e -> "error"
        )).isInstanceOf(RuntimeException.class).hasMessage("success error");
    }

    @Test
    @DisplayName("fold 中 failureFn 抛出异常应继续抛出")
    void foldShouldRethrowExceptionFromFailureFn() {
        Result<Integer> result = Result.fail(TestResponseCode.PARAM_ERROR);

        assertThatThrownBy(() -> result.fold(
                i -> "success",
                e -> {
                    throw new RuntimeException("failure error");
                }
        )).isInstanceOf(RuntimeException.class).hasMessage("failure error");
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
    @DisplayName("contains 成功且值相等时应返回 true")
    void containsShouldReturnTrueWhenSuccessAndValueEquals() {
        Result<String> result = Result.success("test");
        assertThat(result.contains("test")).isTrue();
    }

    @Test
    @DisplayName("contains 成功但值不等时应返回 false")
    void containsShouldReturnFalseWhenSuccessButValueNotEquals() {
        Result<String> result = Result.success("test");
        assertThat(result.contains("other")).isFalse();
    }

    @Test
    @DisplayName("contains 成功且值为 null 时应返回 false")
    void containsShouldReturnFalseWhenSuccessWithNull() {
        Result<String> result = Result.success(null);
        assertThat(result.contains("test")).isFalse();
        assertThat(result.contains(null)).isTrue();
    }

    @Test
    @DisplayName("contains 失败时应返回 false")
    void containsShouldReturnFalseWhenFailure() {
        Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
        assertThat(result.contains("test")).isFalse();
    }

    @Test
    @DisplayName("exists 成功且有值时应返回 true")
    void existsShouldReturnTrueWhenSuccessWithValue() {
        Result<String> result = Result.success("test");
        assertThat(result.exists()).isTrue();
    }

    @Test
    @DisplayName("exists 成功但值为 null 时应返回 false")
    void existsShouldReturnFalseWhenSuccessWithNull() {
        Result<String> result = Result.success(null);
        assertThat(result.exists()).isFalse();
    }

    @Test
    @DisplayName("exists 失败时应返回 false")
    void existsShouldReturnFalseWhenFailure() {
        Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
        assertThat(result.exists()).isFalse();
    }
}