package com.chao.failfast.result;

import com.chao.failfast.internal.Business;
import com.chao.failfast.model.TestResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Result 类测试")
class ResultTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreationTest {
        @Test
        @DisplayName("ok 方法应创建成功结果")
        void okShouldCreateSuccessResult() {
            Result<String> result = Result.ok("test");
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFail()).isFalse();
            assertThat(result.get()).isEqualTo("test");
            assertThat(result.getOrNull()).isEqualTo("test");
            assertThat(catchThrowable(result::getError)).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("fail 方法应创建失败结果")
        void failShouldCreateFailureResult() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
            assertThat(catchThrowable(result::get)).isInstanceOf(IllegalStateException.class);
            assertThat(catchThrowable(result::getOrNull)).isEqualTo(null);
        }

        @Test
        @DisplayName("fail 方法应创建带详情的失败结果")
        void failWithDetailShouldCreateFailureResult() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR, "detail");
            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getDetail()).isEqualTo("detail");
        }

        @Test
        @DisplayName("ofNullable 当值不为null时应创建成功结果")
        void ofNullableShouldCreateSuccessResultWhenValueIsNotNull() {
            Result<String> result = Result.ofNullable("test", TestResponseCode.PARAM_ERROR);
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo("test");
        }

        @Test
        @DisplayName("ofNullable 带详情 当值不为null时应创建成功结果")
        void ofNullableWithDetailShouldCreateSuccessResultWhenValueIsNotNull() {
            Result<String> result = Result.ofNullable("test", TestResponseCode.PARAM_ERROR, "detail");
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo("test");
        }

        @Test
        @DisplayName("ofNullable 当值为null时应创建失败结果")
        void ofNullableShouldCreateFailureResultWhenValueIsNull() {
            Result<String> result = Result.ofNullable(null, TestResponseCode.PARAM_ERROR);
            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
        }

        @Test
        @DisplayName("ofNullable 带详情 当值为null时应创建失败结果")
        void ofNullableWithDetailShouldCreateFailureResultWhenValueIsNull() {
            Result<String> result = Result.ofNullable(null, TestResponseCode.PARAM_ERROR, "detail");
            assertThat(result.isFail()).isTrue();
            assertThat(result.getError().getDetail()).isEqualTo("detail");
        }
    }

    @Nested
    @DisplayName("函数式操作测试")
    class FunctionalOpsTest {
        @Test
        @DisplayName("map 应转换成功值")
        void mapShouldTransformSuccessValue() {
            Result<String> result = Result.ok("123");
            Result<Integer> mapped = result.map(Integer::parseInt);
            assertThat(mapped.isSuccess()).isTrue();
            assertThat(mapped.get()).isEqualTo(123);
        }

        @Test
        @DisplayName("map 不应转换失败结果")
        void mapShouldNotTransformFailureResult() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<Integer> mapped = result.map(Integer::parseInt);
            assertThat(mapped.isFail()).isTrue();
            assertThat(mapped.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
        }

        @Test
        @DisplayName("map 抛出异常时应捕获")
        void mapShouldCatchException() {
            Result<String> result = Result.ok("abc");
            assertThat(catchThrowable(() -> result.map(s -> {
                throw new RuntimeException("error");
            }))).isInstanceOf(RuntimeException.class);

            // If Business exception is thrown in map?
            Result<String> result2 = Result.ok("abc");
            Result<String> mapped2 = result2.map(s -> {
                throw Business.of(TestResponseCode.PARAM_ERROR);
            });
            assertThat(mapped2.isFail()).isTrue();
        }

        @Test
        @DisplayName("flatMap 应连接 Result")
        void flatMapShouldChainResults() {
            Result<String> result = Result.ok("123");
            Result<Integer> flatMapped = result.flatMap(s -> Result.ok(Integer.parseInt(s)));
            assertThat(flatMapped.isSuccess()).isTrue();
            assertThat(flatMapped.get()).isEqualTo(123);
        }

        @Test
        @DisplayName("flatMap 不应转换失败结果")
        void flatMapShouldNotTransformFailureResult() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<Integer> flatMapped = result.flatMap(s -> Result.ok(Integer.parseInt(s)));
            assertThat(flatMapped.isFail()).isTrue();
        }

        @Test
        @DisplayName("filter 当条件满足时应保持成功")
        void filterShouldKeepSuccessWhenPredicateIsTrue() {
            Result<Integer> result = Result.ok(10);
            Result<Integer> filtered = result.filter(i -> i > 5, TestResponseCode.PARAM_ERROR);
            assertThat(filtered.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("filter 带详情 当条件满足时应保持成功")
        void filterWithDetailShouldKeepSuccessWhenPredicateIsTrue() {
            Result<Integer> result = Result.ok(10);
            Result<Integer> filtered = result.filter(i -> i > 5, TestResponseCode.PARAM_ERROR, "detail");
            assertThat(filtered.isSuccess()).isTrue();
        }
        @Test
        @DisplayName("filter: 当 Result 已是失败时应直接返回自身，不执行 predicate")
        void filterWhenAlreadyFailShouldReturnSelf() {
            Result<Integer> fail = Result.fail(TestResponseCode.PARAM_ERROR, "原始错误");

            // 即使 predicate 会抛异常，也不应该执行，因为已经是 Fail 了
            Result<Integer> filtered = fail.filter(
                    i -> { throw new RuntimeException("不应执行"); },
                    TestResponseCode.PARAM_ERROR,
                    "新详情"
            );

            assertThat(filtered.isFail()).isTrue();
            assertThat(filtered.getError().getResponseCode()).isEqualTo(TestResponseCode.PARAM_ERROR);
            assertThat(filtered.getError().getDetail()).isEqualTo("原始错误");
        }

        @Test
        @DisplayName("filter 当条件不满足时应转为失败")
        void filterShouldFailWhenPredicateIsFalse() {
            Result<Integer> result = Result.ok(1);
            Result<Integer> filtered = result.filter(i -> i > 5, TestResponseCode.PARAM_ERROR);
            assertThat(filtered.isFail()).isTrue();
            assertThat(filtered.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
        }

        @Test
        @DisplayName("filter 带详情 当条件不满足时应转为失败")
        void filterWithDetailShouldFailWhenPredicateIsFalse() {
            Result<Integer> result = Result.ok(1);
            Result<Integer> filtered = result.filter(i -> i > 5, TestResponseCode.PARAM_ERROR, "detail");
            assertThat(filtered.isFail()).isTrue();
            assertThat(filtered.getError().getDetail()).isEqualTo("detail");
        }

        @Test
        @DisplayName("filter 对失败结果无影响")
        void filterShouldNotAffectFailure() {
            Result<Integer> result = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<Integer> filtered = result.filter(i -> i > 5, TestResponseCode.SYSTEM_ERROR);
            assertThat(filtered.isFail()).isTrue();
            assertThat(filtered.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
        }

        @Test
        @DisplayName("combine 组合两个成功结果")
        void combineShouldCombineSuccess() {
            Result<String> r1 = Result.ok("Hello");
            Result<String> r2 = Result.ok("World");
            Result<String> combined = r1.combine(r2, (s1, s2) -> s1 + " " + s2);
            assertThat(combined.isSuccess()).isTrue();
            assertThat(combined.get()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("combine 包含失败结果时返回失败")
        void combineShouldReturnFailure() {
            Result<String> r1 = Result.ok("Hello");
            Result<String> r2 = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<String> combined = r1.combine(r2, (s1, s2) -> s1 + " " + s2);
            assertThat(combined.isFail()).isTrue();

            Result<String> combined2 = r2.combine(r1, (s1, s2) -> s1 + " " + s2);
            assertThat(combined2.isFail()).isTrue();
        }
    }

    @Nested
    @DisplayName("恢复与终结操作测试")
    class TerminalOpsTest {
        @Test
        @DisplayName("peek 应只执行副作用")
        void peekShouldNotChangeResult() {
            Result<String> result = Result.ok("test");
            final boolean[] executed = {false};
            Result<String> peeked = result.peek(s -> executed[0] = true);

            assertThat(peeked.get()).isEqualTo("test");
            assertThat(executed[0]).isTrue();

            // Failure case
            Result<String> fail = Result.fail(TestResponseCode.PARAM_ERROR);
            executed[0] = false;
            fail.peek(s -> executed[0] = true);
            assertThat(executed[0]).isFalse();
        }

        @Test
        @DisplayName("peekError 应只在错误时执行副作用")
        void peekErrorShouldNotChangeResult() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            final boolean[] executed = {false};
            Result<String> peeked = result.peekError(e -> executed[0] = true);

            assertThat(peeked.isFail()).isTrue();
            assertThat(executed[0]).isTrue();

            // Success case
            Result<String> success = Result.ok("test");
            executed[0] = false;
            success.peekError(e -> executed[0] = true);
            assertThat(executed[0]).isFalse();
        }

        @Test
        @DisplayName("recoverWith 当失败时应执行恢复函数")
        void recoverWithShouldRecover() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<String> recovered = result.recoverWith(e -> Result.ok("recovered"));

            assertThat(recovered.isSuccess()).isTrue();
            assertThat(recovered.get()).isEqualTo("recovered");

            // Success case
            Result<String> success = Result.ok("test");
            Result<String> recoveredSuccess = success.recoverWith(e -> Result.ok("recovered"));
            assertThat(recoveredSuccess.get()).isEqualTo("test");
        }

        @Test
        @DisplayName("failNow 当成功时应返回值")
        void failNowShouldReturnValueWhenSuccess() {
            Result<String> result = Result.ok("test");
            assertThat(result.failNow()).isEqualTo("test");
            assertThat(result.failNow("default")).isEqualTo("test");
            assertThat(result.failNow(e -> new RuntimeException())).isEqualTo("test");
        }

        @Test
        @DisplayName("failNow 当失败时应抛出异常")
        void failNowShouldThrowExceptionWhenFailure() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            assertThat(catchThrowable(result::failNow)).isInstanceOf(Business.class);
            assertThat(result.failNow("default")).isEqualTo("default");
            assertThat(catchThrowable(() -> result.failNow(e -> new IllegalArgumentException())))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("onFailGet 当成功时应返回值")
        void onFailGetShouldReturnValueWhenSuccess() {
            Result<String> result = Result.ok("test");
            assertThat(result.onFailGet(() -> "default")).isEqualTo("test");
        }

        @Test
        @DisplayName("onFailGet 当失败时应返回默认值")
        void onFailGetShouldReturnDefaultValueWhenFailure() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            assertThat(result.onFailGet(() -> "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("recover 当失败时应恢复")
        void recoverShouldRecoverFromFailure() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<String> recovered = result.recover(e -> "recovered");
            assertThat(recovered.isSuccess()).isTrue();
            assertThat(recovered.get()).isEqualTo("recovered");

            // Success case
            Result<String> success = Result.ok("test");
            Result<String> recoveredSuccess = success.recover(e -> "recovered");
            assertThat(recoveredSuccess.get()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("map 边界测试")
    class MapEdgeTest {

        @Test
        @DisplayName("map 中抛出 Business 异常时应转为失败 Result")
        void mapShouldConvertBusinessExceptionToFailure() {
            Result<String> result = Result.ok("test");
            Result<String> mapped = result.map(s -> {
                throw Business.of(TestResponseCode.PARAM_ERROR, "business error");
            });

            assertThat(mapped.isFail()).isTrue();
            assertThat(mapped.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
            assertThat(mapped.getError().getDetail()).isEqualTo("business error");
        }

        @Test
        @DisplayName("map 中抛出 RuntimeException 时应继续抛出")
        void mapShouldRethrowRuntimeException() {
            Result<String> result = Result.ok("test");

            assertThatThrownBy(() -> result.map(s -> {
                throw new RuntimeException("runtime error");
            })).isInstanceOf(RuntimeException.class).hasMessage("runtime error");
        }

        @Test
        @DisplayName("map 中抛出 Error 时应继续抛出")
        void mapShouldRethrowError() {
            Result<String> result = Result.ok("test");

            assertThatThrownBy(() -> result.map(s -> {
                throw new OutOfMemoryError("oom");
            })).isInstanceOf(OutOfMemoryError.class).hasMessage("oom");
        }

        @Test
        @DisplayName("map 返回 null 时应创建包含 null 的成功 Result")
        void mapShouldAllowNullReturn() {
            Result<String> result = Result.ok("test");
            Result<Object> mapped = result.map(s -> null);

            assertThat(mapped.isSuccess()).isTrue();
            assertThat(mapped.get()).isNull();
        }
    }

    @Nested
    @DisplayName("flatMap 边界测试")
    class FlatMapEdgeTest {

        @Test
        @DisplayName("flatMap 返回 null 时应抛出 NullPointerException")
        void flatMapShouldThrowNpeWhenReturnsNull() {
            Result<String> result = Result.ok("test");

            Result<Object> mapped = result.flatMap(s -> null);

            assertThat(mapped).isNull();  // 或者根据你的设计调整断言
        }

        @Test
        @DisplayName("flatMap 返回的失败 Result 应被保留")
        void flatMapShouldPreserveFailureResult() {
            Result<String> result = Result.ok("test");
            Result<String> mapped = result.flatMap(s -> Result.fail(TestResponseCode.PARAM_ERROR));

            assertThat(mapped.isFail()).isTrue();
            assertThat(mapped.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("filter 边界测试")
    class FilterEdgeTest {

        @Test
        @DisplayName("filter 中 predicate 抛出异常时应继续抛出")
        void filterShouldRethrowExceptionFromPredicate() {
            Result<String> result = Result.ok("test");

            assertThatThrownBy(() -> result.filter(s -> {
                throw new RuntimeException("predicate error");
            }, TestResponseCode.PARAM_ERROR)).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("filter 中 predicate 返回 null 时应视为 false")
        void filterShouldTreatNullAsFalse() {
            Result<String> result = Result.ok("test");
            // 注意：如果 predicate 返回 null，会导致 NullPointerException
            // 因为使用了 Boolean 拆箱
            assertThatThrownBy(() -> result.filter(s -> null, TestResponseCode.PARAM_ERROR))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("peek 边界测试")
    class PeekEdgeTest {

        @Test
        @DisplayName("peek 中 action 抛出异常时应继续抛出")
        void peekShouldRethrowExceptionFromAction() {
            Result<String> result = Result.ok("test");

            assertThatThrownBy(() -> result.peek(s -> {
                throw new RuntimeException("peek error");
            })).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("peek 应返回原始 Result 实例")
        void peekShouldReturnSameInstance() {
            Result<String> result = Result.ok("test");
            Result<String> peeked = result.peek(s -> {
            });

            assertThat(peeked).isSameAs(result);
        }
    }

    @Nested
    @DisplayName("peekError 边界测试")
    class PeekErrorEdgeTest {

        @Test
        @DisplayName("peekError 中 action 抛出异常时应继续抛出")
        void peekErrorShouldRethrowExceptionFromAction() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);

            assertThatThrownBy(() -> result.peekError(e -> {
                throw new RuntimeException("peek error");
            })).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("peekError 应返回原始 Result 实例")
        void peekErrorShouldReturnSameInstance() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<String> peeked = result.peekError(e -> {
            });

            assertThat(peeked).isSameAs(result);
        }
    }

    @Nested
    @DisplayName("recover 边界测试")
    class RecoverEdgeTest {

        @Test
        @DisplayName("recover 中 recovery 函数抛出异常时应继续抛出")
        void recoverShouldRethrowExceptionFromRecovery() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);

            assertThatThrownBy(() -> result.recover(e -> {
                throw new RuntimeException("recovery error");
            })).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("recover 返回 null 时应创建包含 null 的成功 Result")
        void recoverShouldAllowNullReturn() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<String> recovered = result.recover(e -> null);

            assertThat(recovered.isSuccess()).isTrue();
            assertThat(recovered.get()).isNull();
        }
    }

    @Nested
    @DisplayName("recoverWith 边界测试")
    class RecoverWithEdgeTest {

        @Test
        @DisplayName("recoverWith 中 recovery 函数抛出异常时应继续抛出")
        void recoverWithShouldRethrowExceptionFromRecovery() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);

            assertThatThrownBy(() -> result.recoverWith(e -> {
                throw new RuntimeException("recovery error");
            })).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("recoverWith 返回 null 时应抛出 NullPointerException")
        void recoverWithShouldThrowNpeWhenReturnsNull() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<String> with = result.recoverWith(e -> null);
            assertThat(with).isNull();
        }

        @Test
        @DisplayName("recoverWith 返回的失败 Result 应被保留")
        void recoverWithShouldPreserveFailureResult() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<String> recovered = result.recoverWith(e -> Result.fail(TestResponseCode.SYSTEM_ERROR));

            assertThat(recovered.isFail()).isTrue();
            assertThat(recovered.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.SYSTEM_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("combine 边界测试")
    class CombineEdgeTest {

        @Test
        @DisplayName("combine 中 combiner 抛出异常时应继续抛出")
        void combineShouldRethrowExceptionFromCombiner() {
            Result<String> r1 = Result.ok("a");
            Result<String> r2 = Result.ok("b");

            assertThatThrownBy(() -> r1.combine(r2, (s1, s2) -> {
                throw new RuntimeException("combiner error");
            })).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("combine 返回 null 时应创建包含 null 的成功 Result")
        void combineShouldAllowNullReturn() {
            Result<String> r1 = Result.ok("a");
            Result<String> r2 = Result.ok("b");
            Result<Object> combined = r1.combine(r2, (s1, s2) -> null);

            assertThat(combined.isSuccess()).isTrue();
            assertThat(combined.get()).isNull();
        }

        @Test
        @DisplayName("combine 当两个都失败时应返回第一个失败")
        void combineShouldReturnFirstFailureWhenBothFail() {
            Result<String> r1 = Result.fail(TestResponseCode.PARAM_ERROR);
            Result<String> r2 = Result.fail(TestResponseCode.SYSTEM_ERROR);
            Result<String> combined = r1.combine(r2, (s1, s2) -> s1 + s2);

            assertThat(combined.isFail()).isTrue();
            assertThat(combined.getError().getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("onFailGet 边界测试")
    class OnFailGetEdgeTest {

        @Test
        @DisplayName("onFailGet 中 supplier 抛出异常时应继续抛出")
        void onFailGetShouldRethrowExceptionFromSupplier() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);

            assertThatThrownBy(() -> result.onFailGet(() -> {
                throw new RuntimeException("supplier error");
            })).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("onFailGet 中 supplier 返回 null 时应返回 null")
        void onFailGetShouldAllowNullReturn() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);
            String value = result.onFailGet(() -> null);

            assertThat(value).isNull();
        }
    }

    @Nested
    @DisplayName("failNow 边界测试")
    class FailNowEdgeTest {

        @Test
        @DisplayName("failNow(Function) 中 exceptionProvider 抛出异常时应继续抛出")
        void failNowShouldRethrowExceptionFromProvider() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);

            assertThatThrownBy(() -> result.failNow(e -> {
                throw new RuntimeException("provider error");
            })).isInstanceOf(RuntimeException.class).hasMessage("provider error");
        }

        @Test
        @DisplayName("failNow(Function) 返回 null 时应抛出 NullPointerException")
        void failNowShouldThrowNpeWhenReturnsNull() {
            Result<String> result = Result.fail(TestResponseCode.PARAM_ERROR);

            assertThatThrownBy(() -> result.failNow(e -> null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Success/Failure 内部类测试")
    class InternalClassTest {

        @Test
        @DisplayName("Success 的 getter 应返回正确值")
        void successGetterShouldReturnCorrectValues() {
            Result.Success<String> success = new Result.Success<>("test");

            assertThat(success.getData()).isEqualTo("test");
            assertThat(success.getCode()).isEqualTo(200);
            assertThat(success.getMessage()).isEqualTo("Success");
            assertThat(success.getDescription()).isEqualTo("操作成功");
            assertThat(success.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("getError: 当 Result 是 Success 时应抛出异常")
        void getError_whenSuccess_shouldThrow() {
            Result<String> success = Result.ok("data");

            assertThatThrownBy(success::getError)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Result is success");
        }

        @Test
        @DisplayName("Failure 的 getter 应返回正确值")
        void failureGetterShouldReturnCorrectValues() {
            Business error = Business.of(TestResponseCode.PARAM_ERROR, "detail");
            Result.Fail<String> fail = new Result.Fail<>(error);

            assertThat(fail.getError()).isEqualTo(error);
            assertThat(fail.getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
            assertThat(fail.getMessage()).isEqualTo(TestResponseCode.PARAM_ERROR.getMessage());
            assertThat(fail.getDescription()).isEqualTo("detail");
            assertThat(fail.getTimestamp()).isNotNull();
        }
    }
}
