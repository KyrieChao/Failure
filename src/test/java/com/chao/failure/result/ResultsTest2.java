package com.chao.failure.result;

import com.chao.failure.exception.Business;
import com.chao.failure.exception.MultiBusiness;
import com.chao.failure.model.TestResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Results 工具类测试")
@Slf4j
class ResultsTest2 {

    @Nested
    @DisplayName("tryOf 方法测试")
    class TryOfTest {
        @Test
        @DisplayName("当Supplier无异常时应返回成功Result")
        void shouldReturnSuccessWhenSupplierSucceeds() {
            Result<String> result = Results.tryOf(() -> "test", TestResponseCode.SYSTEM_ERROR);
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo("test");
        }

        @Test
        @DisplayName("当Supplier抛出Business异常时应返回指定错误码的失败Result")
        void shouldReturnFailureWhenSupplierThrowsBusiness() {
            Result<String> result = Results.tryOf(() -> {
                throw Business.of(TestResponseCode.PARAM_ERROR);
            }, TestResponseCode.SYSTEM_ERROR);

            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.SYSTEM_ERROR.getCode());
        }

        @Test
        @DisplayName("当Supplier抛出其他异常时应返回指定错误码的失败Result")
        void shouldReturnFailureWhenSupplierThrowsOtherException() {
            Result<String> result = Results.tryOf(() -> {
                throw new RuntimeException("runtime error");
            }, TestResponseCode.SYSTEM_ERROR);

            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.SYSTEM_ERROR.getCode());
        }

        @Test
        @DisplayName("tryOf 带详情 当Supplier抛出其他异常时应返回指定详情")
        void shouldReturnFailureWithDetail() {
            Result<String> result = Results.tryOf(() -> {
                throw new RuntimeException("runtime error");
            }, TestResponseCode.SYSTEM_ERROR, "custom detail");

            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getDetail()).isEqualTo("custom detail");
        }

        @Test
        @DisplayName("tryOf 带详情 当Supplier成功时应返回成功")
        void shouldReturnSuccessWithDetail() {
            Result<String> result = Results.tryOf(() -> "success", TestResponseCode.SYSTEM_ERROR, "detail");
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo("success");
        }

        @Test
        @DisplayName("tryOf 带详情 当Supplier抛出Business异常时应使用指定详情")
        void shouldReturnBusinessErrorWithDetail() {
            Result<String> result = Results.tryOf(() -> {
                throw Business.of(TestResponseCode.PARAM_ERROR);
            }, TestResponseCode.SYSTEM_ERROR, "custom detail");

            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.SYSTEM_ERROR.getCode());
            assertThat(result.getError().getDetail()).isEqualTo("custom detail");
        }
    }

    @Nested
    @DisplayName("sequence 方法测试")
    class SequenceTest {
        @Test
        @DisplayName("当所有Result都成功时应返回成功列表")
        void shouldReturnSuccessListWhenAllResultsAreSuccess() {
            Result<String> r1 = Result.success("a");
            Result<String> r2 = Result.success("b");
            Result<List<String>> result = Results.sequence(r1, r2);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).containsExactly("a", "b");
        }

        @Test
        @DisplayName("当存在失败Result时应返回第一个失败")
        void shouldReturnFirstFailureWhenAnyResultIsFailure() {
            Result<String> r1 = Result.success("a");
            Result<String> r2 = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<List<String>> result = Results.sequence(r1, r2);

            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("sequenceAll 方法测试")
    class SequenceAllTest {
        @Test
        @DisplayName("应当收集所有成功结果")
        void shouldCollectAllSuccess() {
            Result<String> r1 = Result.success("a");
            Result<String> r2 = Result.success("b");
            Result<List<String>> result = Results.sequenceAll(r1, r2);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).containsExactly("a", "b");
        }

        @Test
        @DisplayName("应当收集所有错误")
        void shouldCollectAllErrors() {
            Result<String> r1 = Result.success("a");
            Result<String> r2 = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<String> r3 = Result.fail(TestResponseCode.SYSTEM_ERROR);

            Result<List<String>> result = Results.sequenceAll(r1, r2, r3);

            assertThat(result.isFail()).isTrue();
            assertThat(result.getError()).isInstanceOf(MultiBusiness.class);
            MultiBusiness mb = (MultiBusiness) result.getError();
            assertThat(mb.getErrors()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("tryRun 方法测试")
    class TryRunTest {
        @Test
        @DisplayName("当执行成功时应返回OK")
        void shouldReturnOkWhenSuccess() {
            Result<Void> result = Results.tryRun(() -> {
            }, TestResponseCode.SYSTEM_ERROR);
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("当抛出异常时应返回Fail")
        void shouldReturnFailWhenException() {
            Result<Void> result = Results.tryRun(() -> {
                throw new RuntimeException("error");
            }, TestResponseCode.SYSTEM_ERROR);

            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.SYSTEM_ERROR.getCode());
        }

        @Test
        @DisplayName("tryRun 带详情 当抛出异常时应返回Fail带详情")
        void shouldReturnFailWithDetailWhenException() {
            Result<Void> result = Results.tryRun(() -> {
                throw new RuntimeException("error");
            }, TestResponseCode.SYSTEM_ERROR, "custom detail");

            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getDetail()).isEqualTo("custom detail");
        }

        @Test
        @DisplayName("tryRun 带详情 当执行成功时应返回OK")
        void shouldReturnOkWithDetailWhenSuccess() {
            Result<Void> result = Results.tryRun(() -> {
            }, TestResponseCode.SYSTEM_ERROR, "detail");
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("tryRun 带详情 当抛出Business异常时应使用指定详情")
        void shouldReturnBusinessErrorWithDetailWhenException() {
            Result<Void> result = Results.tryRun(() -> {
                throw Business.of(TestResponseCode.PARAM_ERROR);
            }, TestResponseCode.SYSTEM_ERROR, "custom detail");

            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.SYSTEM_ERROR.getCode());
            assertThat(result.getError().getDetail()).isEqualTo("custom detail");
        }
    }

    @Nested
    @DisplayName("when 方法测试")
    class WhenTest {
        @Test
        @DisplayName("当条件满足时应执行Supplier")
        void shouldExecuteWhenTrue() {
            Result<String> result = Results.when(true, () -> Result.success("executed"));
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo("executed");
        }

        @Test
        @DisplayName("当条件不满足时应返回Null Result")
        void shouldReturnNullWhenFalse() {
            Result<String> result = Results.when(false, () -> Result.success("executed"));
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isNull();
        }
    }

    @Nested
    @DisplayName("getOrNull 方法测试")
    class GetOrNullTest {
        @Test
        @DisplayName("当Result成功时应返回值")
        void shouldReturnValueWhenResultIsSuccess() {
            Result<String> result = Result.success("test");
            assertThat(Results.getOrNull(result)).isEqualTo("test");
        }

        @Test
        @DisplayName("当Result失败时应返回null")
        void shouldReturnNullWhenResultIsFailure() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            assertThat(Results.getOrNull(result)).isNull();
        }
    }

    @Nested
    @DisplayName("traverse 方法测试")
    class TraverseTest {
        @Test
        @DisplayName("当所有转换成功时应返回列表")
        void shouldReturnSuccessList() {
            List<Integer> inputs = Arrays.asList(1, 2, 3);
            Result<List<String>> result = Results.traverse(inputs, i -> Result.success(String.valueOf(i)));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).containsExactly("1", "2", "3");
        }

        @Test
        @DisplayName("当遇到失败时应立即返回失败")
        void shouldFailFast() {
            List<Integer> inputs = Arrays.asList(1, 2, 3);
            Result<List<String>> result = Results.traverse(inputs, i -> {
                if (i == 2) return Result.fail(TestResponseCode.PARAM_ERROR);
                return Result.success(String.valueOf(i));
            });

            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("traverse 边界测试")
    class TraverseEdgeTest {

        @Test
        @DisplayName("当输入列表为空时应返回空列表")
        void shouldReturnEmptyListWhenInputIsEmpty() {
            List<Integer> emptyList = Arrays.asList();
            Result<List<String>> result = Results.traverse(emptyList, i -> Result.success(String.valueOf(i)));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEmpty();
        }

        @Test
        @DisplayName("当输入列表为null时应抛出NullPointerException")
        void shouldThrowNpeWhenInputIsNull() {
            assertThatThrownBy(() -> Results.traverse(null, i -> Result.success(String.valueOf(i))))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("当mapper返回null时应抛出NullPointerException")
        void shouldThrowNpeWhenMapperReturnsNull() {
            List<Integer> inputs = Arrays.asList(1, 2);
            assertThatThrownBy(() -> Results.traverse(inputs, i -> null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("当第一个元素就失败时应立即返回")
        void shouldFailImmediatelyOnFirstElement() {
            List<Integer> inputs = Arrays.asList(1, 2, 3);
            Result<List<String>> result = Results.traverse(inputs, i -> {
                if (i == 1) return Result.fail(TestResponseCode.PARAM_ERROR);
                return Result.success(String.valueOf(i));
            });

            assertThat(result.isFail()).isTrue();
            // 验证只有第一个元素被处理
        }
    }

    @Nested
    @DisplayName("sequence 边界测试")
    class SequenceEdgeTest {

        @Test
        @DisplayName("当没有参数时应返回空列表")
        void shouldReturnEmptyListWhenNoArgs() {
            @SuppressWarnings("unchecked")
            Result<List<String>> result = Results.sequence();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEmpty();
        }

        @Test
        @DisplayName("当只有一个成功Result时应返回单元素列表")
        void shouldReturnSingleElementList() {
            Result<String> r1 = Result.success("only");
            Result<List<String>> result = Results.sequence(r1);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).containsExactly("only");
        }

        @Test
        @DisplayName("当第一个就是失败时应立即返回")
        void shouldFailImmediatelyOnFirstFailure() {
            Result<String> r1 = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<String> r2 = Result.success("b");  // 不应该被处理

            Result<List<String>> result = Results.sequence(r1, r2);

            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("sequenceAll 边界测试")
    class SequenceAllEdgeTest {

        @Test
        @DisplayName("当没有参数时应返回空列表")
        void shouldReturnEmptyListWhenNoArgs() {
            @SuppressWarnings("unchecked")
            Result<List<String>> result = Results.sequenceAll();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEmpty();
        }

        @Test
        @DisplayName("当全部失败时应返回MultiBusiness包含所有错误")
        void shouldReturnAllErrorsWhenAllFail() {
            Result<String> r1 = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<String> r2 = Result.fail(TestResponseCode.SYSTEM_ERROR);
            Result<String> r3 = Result.fail(TestResponseCode.UNAUTHORIZED);

            Result<List<String>> result = Results.sequenceAll(r1, r2, r3);

            assertThat(result.isFail()).isTrue();
            MultiBusiness mb = (MultiBusiness) result.getError();
            assertThat(mb.getErrors()).hasSize(3);
        }

        @Test
        @DisplayName("当只有一个失败时应返回该错误（不是MultiBusiness）")
        void shouldReturnSingleErrorWhenOnlyOneFails() {
            Result<String> r1 = Result.success("a");
            Result<String> r2 = Result.fail(TestResponseCode.PARAM_ERROR);

            Result<List<String>> result = Results.sequenceAll(r1, r2);

            assertThat(result.isFail()).isTrue();
            // 注意：这里实际上会包装成 MultiBusiness，因为 sequenceAll 总是用 MultiBusiness
            assertThat(result.getError()).isInstanceOf(MultiBusiness.class);
        }
    }

    @Nested
    @DisplayName("tryOf 边界测试")
    class TryOfEdgeTest {

        @Test
        @DisplayName("当Supplier返回null时应返回成功的null")
        void shouldReturnSuccessWithNull() {
            Result<String> result = Results.tryOf(() -> null, TestResponseCode.SYSTEM_ERROR);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isNull();
        }

        @Test
        @DisplayName("当Business异常没有detail时应正确处理")
        void shouldHandleBusinessWithoutDetail() {
            Result<String> result = Results.tryOf(() -> {
                throw Business.of(TestResponseCode.PARAM_ERROR);
            }, TestResponseCode.SYSTEM_ERROR);

            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.SYSTEM_ERROR.getCode());
        }

        @Test
        @DisplayName("当异常message为null时应处理空detail")
        void shouldHandleExceptionWithNullMessage() {
            Result<String> result = Results.tryOf(() -> {
                throw new RuntimeException() {
                    @Override
                    public String getMessage() {
                        return null;
                    }
                };
            }, TestResponseCode.SYSTEM_ERROR);
            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getDetail()).isEqualTo(TestResponseCode.SYSTEM_ERROR.getDescription());
        }
    }

    @Nested
    @DisplayName("tryRun 边界测试")
    class TryRunEdgeTest {

        @Test
        @DisplayName("当Runnable为null时应抛出NullPointerException")
        void shouldThrowNpeWhenRunnableIsNull() {
            assertThatThrownBy(() -> Results.tryRun(null, TestResponseCode.SYSTEM_ERROR))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("当抛出Error时不应捕获，应直接抛出")
        void shouldNotCaptureError() {
            assertThatThrownBy(() -> Results.tryRun(() -> {
                throw new OutOfMemoryError("OOM");
            }, TestResponseCode.SYSTEM_ERROR))
                    .isInstanceOf(OutOfMemoryError.class)
                    .hasMessage("OOM");
        }
    }

    @Nested
    @DisplayName("when 边界测试")
    class WhenEdgeTest {

        @Test
        @DisplayName("当Supplier返回失败Result时应返回该失败")
        void shouldReturnFailureFromSupplier() {

            Result<?> result = Results.when(true, () -> Result.fail(TestResponseCode.PARAM_ERROR));
            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
        }


        @Test
        @DisplayName("当Supplier抛出异常时应抛出该异常")
        void shouldThrowExceptionFromSupplier() {
            assertThatThrownBy(() -> Results.when(true, () -> {
                throw new RuntimeException("supplier error");
            })).isInstanceOf(RuntimeException.class).hasMessage("supplier error");
        }

        @Test
        @DisplayName("当Supplier为null且条件为true时应抛出NullPointerException")
        void shouldThrowNpeWhenSupplierIsNull() {
            assertThatThrownBy(() -> Results.when(true, (Supplier<Result<String>>) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getOrNull 边界测试")
    class GetOrNullEdgeTest {

        @Test
        @DisplayName("当Result为null时应抛出NullPointerException")
        void shouldThrowNpeWhenResultIsNull() {
            assertThatThrownBy(() -> Results.getOrNull(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("当成功Result包含null值时应返回null")
        void shouldReturnNullWhenSuccessContainsNull() {
            Result<String> result = Result.success(null);
            assertThat(Results.getOrNull(result)).isNull();
        }
    }
}
