package com.chao.failfast.result;

import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.MultiBusiness;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.i18n.I18nExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Results 工具类单元测试 - 100% 覆盖率
 */
@DisplayName("Results 工具类测试")
@ExtendWith(I18nExtension.class)
class ResultsTest {

    // ==================== 测试数据 ====================

    private static final ResponseCode TEST_CODE = ResponseCode.of(40001, "TEST_ERROR", "Test error");
    private static final ResponseCode TEST_CODE_2 = ResponseCode.of(40002, "TEST_ERROR_2", "Test error 2");
    private static final Business TEST_BUSINESS = Business.of(TEST_CODE, "detail");

    // ==================== tryOf / tryRun ====================

    @Test
    @DisplayName("tryOf: 成功返回结果")
    void tryOf_success() {
        Result<String> result = Results.tryOf(() -> "success", TEST_CODE);

        assertTrue(result.isSuccess());
        assertEquals("success", result.get());
    }

    @Test
    @DisplayName("tryOf: 捕获普通异常转为失败")
    void tryOf_catchException() {
        RuntimeException ex = new RuntimeException("boom");
        Result<String> result = Results.tryOf(() -> {
            throw ex;
        }, TEST_CODE);

        assertTrue(result.isFail());
        assertEquals(40001, result.getError().getResponseCode().getCode());
    }

    @Test
    @DisplayName("tryOf: 捕获Business异常直接返回")
    void tryOf_catchBusiness() {
        Result<String> result = Results.tryOf(() -> {
            throw TEST_BUSINESS;
        }, TEST_CODE);

        assertTrue(result.isFail());
        assertSame(TEST_BUSINESS, result.getError());
    }

    @Test
    @DisplayName("tryOf: 使用自定义detail")
    void tryOf_withDetail() {
        Result<String> result = Results.tryOf(() -> {
            throw new RuntimeException();
        }, TEST_CODE, "custom detail");

        assertTrue(result.isFail());
        assertEquals("custom detail", result.getError().getDetail());
    }

    @Test
    @DisplayName("tryRun: 成功执行")
    void tryRun_success() {
        AtomicBoolean executed = new AtomicBoolean(false);
        Result<Void> result = Results.tryRun(() -> executed.set(true), TEST_CODE);

        assertTrue(result.isSuccess());
        assertTrue(executed.get());
    }

    @Test
    @DisplayName("tryRun: 捕获异常")
    void tryRun_exception() {
        Result<Void> result = Results.tryRun(() -> {
            throw new RuntimeException();
        }, TEST_CODE, "run failed");

        assertTrue(result.isFail());
        assertEquals("run failed", result.getError().getDetail());
    }

    @Test
    @DisplayName("tryRun: null runnable抛出NPE")
    void tryRun_nullRunnable() {
        assertThrows(NullPointerException.class, () -> Results.tryRun(null, TEST_CODE));
    }

    // ==================== fromOptional ====================

    @Test
    @DisplayName("fromOptional: 有值返回成功")
    void fromOptional_present() {
        Result<String> result = Results.fromOptional(Optional.of("value"), TEST_CODE);

        assertTrue(result.isSuccess());
        assertEquals("value", result.get());
    }

    @Test
    @DisplayName("fromOptional: empty返回失败")
    void fromOptional_empty() {
        Result<String> result = Results.fromOptional(Optional.empty(), TEST_CODE, "not found");

        assertTrue(result.isFail());
        assertEquals("not found", result.getError().getDetail());
    }

    @Test
    @DisplayName("fromOptional: null optional返回失败")
    void fromOptional_null() {
        Result<String> result = Results.fromOptional(null, TEST_CODE);

        assertTrue(result.isFail());
        assertEquals("Optional is null", result.getError().getDetail());
    }

    @Test
    @DisplayName("fromOptionalOrElse: 有值返回值")
    void fromOptionalOrElse_present() {
        Result<String> result = Results.fromOptionalOrElse(Optional.of("value"), "default");

        assertEquals("value", result.get());
    }

    @Test
    @DisplayName("fromOptionalOrElse: empty返回默认值")
    void fromOptionalOrElse_empty() {
        Result<String> result = Results.fromOptionalOrElse(Optional.empty(), "default");

        assertEquals("default", result.get());
    }

    @Test
    @DisplayName("fromOptionalOrElse: null optional返回默认值")
    void fromOptionalOrElse_null() {
        Result<String> result = Results.fromOptionalOrElse(null, "default");

        assertEquals("default", result.get());
    }

    // ==================== when / whenOrFail ====================

    @Test
    @DisplayName("when: 条件true执行supplier")
    void when_true() {
        Result<String> result = Results.when(true, () -> Result.ok("success"));

        assertEquals("success", result.get());
    }

    @Test
    @DisplayName("when: 条件false返回ok(null)")
    void when_false() {
        Result<String> result = Results.when(false, () -> Result.ok("success"));

        assertTrue(result.isSuccess());
        assertNull(result.getOrNull());
    }

    @Test
    @DisplayName("when: null supplier抛出NPE")
    void when_nullSupplier() {
        assertThrows(NullPointerException.class, () -> Results.when(true, (Supplier<Result<String>>) null));
    }

    @Test
    @DisplayName("whenOrFail: 条件true返回值")
    void whenOrFail_true() {
        Result<String> result = Results.whenOrFail(true, "value", TEST_CODE);

        assertEquals("value", result.get());
    }

    @Test
    @DisplayName("whenOrFail: 条件false返回失败")
    void whenOrFail_false() {
        Result<String> result = Results.whenOrFail(false, "value", TEST_CODE, "failed");

        assertTrue(result.isFail());
        assertEquals("failed", result.getError().getDetail());
    }

    @Test
    @DisplayName("whenOrFail with supplier: 条件true执行并返回")
    void whenOrFailSupplier_true() {
        Result<String> result = Results.whenOrFail(true, () -> "computed", TEST_CODE);

        assertEquals("computed", result.get());
    }

    @Test
    @DisplayName("whenOrFail with supplier: 条件false返回失败")
    void whenOrFailSupplier_false() {
        Result<String> result = Results.whenOrFail(false, () -> "computed", TEST_CODE, "not allowed");

        assertTrue(result.isFail());
    }

    @Test
    @DisplayName("whenOrFail with supplier: 捕获Business异常")
    void whenOrFailSupplier_catchBusiness() {
        Result<String> result = Results.whenOrFail(true, () -> {
            throw TEST_BUSINESS;
        }, TEST_CODE);

        assertSame(TEST_BUSINESS, result.getError());
    }

    @Test
    @DisplayName("whenOrFail with supplier: 捕获普通异常")
    void whenOrFailSupplier_catchException() {
        Result<String> result = Results.whenOrFail(true, () -> {
            throw new RuntimeException("oops");
        }, TEST_CODE, "wrapped");

        assertEquals("wrapped", result.getError().getDetail());
    }

    // ==================== sequence ====================

    @Test
    @DisplayName("sequence varargs: 全部成功")
    void sequenceVarargs_allSuccess() {
        Result<List<Integer>> result = Results.sequence(
                Result.ok(1),
                Result.ok(2),
                Result.ok(3)
        );

        assertEquals(List.of(1, 2, 3), result.get());
    }

    @Test
    @DisplayName("sequence varargs: 第一个失败快速返回")
    void sequenceVarargs_firstFail() {
        Result<List<Integer>> result = Results.sequence(
                Result.fail(TEST_CODE),
                Result.ok(2)
        );

        assertTrue(result.isFail());
        assertEquals(40001, result.getError().getResponseCode().getCode());
    }

    @Test
    @DisplayName("sequence list: 全部成功")
    void sequenceList_allSuccess() {
        Result<List<Integer>> result = Results.sequence(List.of(Result.ok(1), Result.ok(2)));

        assertEquals(2, result.get().size());
    }

    @Test
    @DisplayName("sequence list: 中间失败")
    void sequenceList_middleFail() {
        Result<List<Integer>> result = Results.sequence(List.of(
                Result.ok(1),
                Result.fail(TEST_CODE_2),
                Result.ok(3)
        ));

        assertEquals(40002, result.getError().getResponseCode().getCode());
    }

    // ==================== sequenceAll ====================

    @Test
    @DisplayName("sequenceAll varargs: 全部成功")
    void sequenceAllVarargs_allSuccess() {
        Result<List<Integer>> result = Results.sequenceAll(Result.ok(1), Result.ok(2));

        assertEquals(List.of(1, 2), result.get());
    }

    @Test
    @DisplayName("sequenceAll: 部分失败收集所有错误")
    void sequenceAll_partialFail() {
        Result<List<Integer>> result = Results.sequenceAll(
                Result.ok(1),
                Result.fail(TEST_CODE),
                Result.fail(TEST_CODE_2)
        );

        assertTrue(result.isFail());
        Business error = result.getError();
        assertTrue(error instanceof MultiBusiness);
        assertEquals(2, ((MultiBusiness) error).getErrors().size());
    }

    @Test
    @DisplayName("sequenceAll list: 全部失败")
    void sequenceAllList_allFail() {
        Result<List<Integer>> result = Results.sequenceAll(List.of(
                Result.fail(TEST_CODE),
                Result.fail(TEST_CODE_2)
        ));

        assertTrue(result.getError() instanceof MultiBusiness);
    }

    // ==================== partition ====================

    @Test
    @DisplayName("partition: 混合结果")
    void partition_mixed() {
        Results.Partition<Integer> partition = Results.partition(List.of(
                Result.ok(1),
                Result.fail(TEST_CODE),
                Result.ok(2),
                Result.fail(TEST_CODE_2)
        ));

        assertEquals(List.of(1, 2), partition.successes());
        assertEquals(2, partition.failures().size());
        assertTrue(partition.hasSuccesses());
        assertTrue(partition.hasFailures());
        assertFalse(partition.isAllSuccess());
        assertFalse(partition.isAllFail());
    }

    @Test
    @DisplayName("partition: 全部成功")
    void partition_allSuccess() {
        Results.Partition<Integer> partition = Results.partition(List.of(Result.ok(1), Result.ok(2)));

        assertTrue(partition.isAllSuccess());
        assertFalse(partition.hasFailures());
        assertFalse(partition.isAllFail());
    }

    @Test
    @DisplayName("partition: 全部失败")
    void partition_allFail() {
        Results.Partition<Integer> partition = Results.partition(List.of(Result.fail(TEST_CODE)));

        assertTrue(partition.isAllFail());
        assertFalse(partition.hasSuccesses());
    }

    // ==================== successes / failures ====================

    @Test
    @DisplayName("successes: 提取所有成功值")
    void successes_extract() {
        List<Integer> list = Results.successes(List.of(
                Result.ok(1),
                Result.fail(TEST_CODE),
                Result.ok(2)
        ));

        assertEquals(List.of(1, 2), list);
    }

    @Test
    @DisplayName("failures: 提取所有失败")
    void failures_extract() {
        List<Business> list = Results.failures(List.of(
                Result.ok(1),
                Result.fail(TEST_CODE),
                Result.fail(TEST_CODE_2)
        ));

        assertEquals(2, list.size());
    }

    // ==================== fold / reduce ====================

    @Test
    @DisplayName("fold: 空列表返回identity")
    void fold_empty() {
        Result<Integer> result = Results.fold(List.of(), 0, Integer::sum);

        assertEquals(0, result.get());
    }

    @Test
    @DisplayName("fold: 累加所有成功值")
    void fold_accumulate() {
        Result<Integer> result = Results.fold(List.of(
                Result.ok(1),
                Result.ok(2),
                Result.ok(3)
        ), 10, Integer::sum);

        assertEquals(16, result.get());
    }

    @Test
    @DisplayName("fold: 遇到失败停止")
    void fold_failFast() {
        Result<Integer> result = Results.fold(List.of(
                Result.ok(1),
                Result.fail(TEST_CODE),
                Result.ok(3)
        ), 0, Integer::sum);

        assertTrue(result.isFail());
    }

    @Test
    @DisplayName("reduce: 正常归约")
    void reduce_normal() {
        Result<Integer> result = Results.reduce(List.of(
                Result.ok(1),
                Result.ok(2),
                Result.ok(3)
        ), Integer::sum);

        assertEquals(6, result.get());
    }

    @Test
    @DisplayName("reduce: 空列表抛出异常")
    void reduce_empty() {
        Result<Integer> result = Results.reduce(List.of(), Integer::sum);

        assertTrue(result.isFail()); // sequence返回失败，map不会执行
        assertThat(result.getError().getDetail()).isIn("无法缩减空列表", "{failure.const.cannot.reduce.empty.list}");
    }

    // ==================== traverse ====================

    @Test
    @DisplayName("traverse: 全部映射成功")
    void traverse_allSuccess() {
        Result<List<Integer>> result = Results.traverse(
                List.of("1", "2", "3"),
                s -> Result.ok(Integer.parseInt(s))
        );

        assertEquals(List.of(1, 2, 3), result.get());
    }

    @Test
    @DisplayName("traverse: 中间映射失败")
    void traverse_middleFail() {
        Result<List<Integer>> result = Results.traverse(
                List.of("1", "oops", "3"),
                s -> {
                    try {
                        return Result.ok(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        return Result.fail(TEST_CODE);
                    }
                }
        );

        assertTrue(result.isFail());
    }

    @Test
    @DisplayName("traverseAll: 收集所有错误")
    void traverseAll_collectErrors() {
        Result<List<Integer>> result = Results.traverseAll(
                List.of("1", "oops", "3", "bad"),
                s -> {
                    try {
                        return Result.ok(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        return Result.fail(TEST_CODE);
                    }
                }
        );

        assertTrue(result.isFail());
        assertTrue(result.getError() instanceof MultiBusiness);
    }

    // ==================== traverseIndexed ====================

    @Test
    @DisplayName("traverseIndexed: 使用索引")
    void traverseIndexed_withIndex() {
        Result<List<String>> result = Results.traverseIndexed(
                List.of("a", "b"),
                (idx, val) -> Result.ok(idx + ":" + val)
        );

        assertEquals(List.of("0:a", "1:b"), result.get());
    }

    @Test
    @DisplayName("traverseIndexed: 快速失败")
    void traverseIndexed_failFast() {
        Result<List<String>> result = Results.traverseIndexed(
                List.of("a", "b", "c"),
                (idx, val) -> idx == 1 ? Result.fail(TEST_CODE) : Result.ok(val)
        );

        assertTrue(result.isFail());
    }

    @Test
    @DisplayName("traverseAllIndexed: 全量收集")
    void traverseAllIndexed_all() {
        Result<List<String>> result = Results.traverseAllIndexed(
                List.of("a", "b", "c"),
                (idx, val) -> idx % 2 == 0 ? Result.ok(val) : Result.fail(TEST_CODE)
        );

        assertTrue(result.isFail());
        // 索引1失败
    }

    // ==================== zip ====================

    @Test
    @DisplayName("zip 2: 全部成功")
    void zip2_allSuccess() {
        Result<String> result = Results.zip(
                Result.ok(1),
                Result.ok("a"),
                (i, s) -> s + i
        );

        assertEquals("a1", result.get());
    }

    @Test
    @DisplayName("zip 2: 第一个失败")
    void zip2_firstFail() {
        Result<String> result = Results.zip(
                Result.fail(TEST_CODE),
                Result.ok("a"),
                (i, s) -> s + i
        );

        assertEquals(40001, result.getError().getResponseCode().getCode());
    }

    @Test
    @DisplayName("zip 2: 第二个失败")
    void zip2_secondFail() {
        Result<String> result = Results.zip(
                Result.ok(1),
                Result.<String>fail(TEST_CODE_2),
                (i, s) -> s + i
        );
        assertTrue(result.isFail());
        assertEquals(40002, result.getError().getResponseCode().getCode());
    }

    @Test
    @DisplayName("zip 3: 全部成功")
    void zip3_allSuccess() {
        Result<String> result = Results.zip(
                Result.ok(1),
                Result.ok(2),
                Result.ok(3),
                (a, b, c) -> a + "-" + b + "-" + c
        );

        assertEquals("1-2-3", result.get());
    }

    @Test
    @DisplayName("zip 3: 中间失败")
    void zip3_middleFail() {
        Result<String> result = Results.zip(
                Result.ok(1),
                Result.fail(TEST_CODE),
                Result.ok(3),
                (a, b, c) -> "never"
        );

        assertTrue(result.isFail());
    }

    @Test
    @DisplayName("zip 4: 全部成功")
    void zip4_allSuccess() {
        Result<String> result = Results.zip(
                Result.ok("1"),
                Result.ok(2),
                Result.ok(3),
                Result.ok(4),
                (a, b, c, d) ->  a + b + c + d
        );

        assertEquals("1234", result.get());
    }

    @Test
    @DisplayName("zip 4: 第四个失败")
    void zip4_fourthFail() {
        Result<String> result = Results.zip(
                Result.ok(1),
                Result.ok(2),
                Result.ok(3),
                Result.fail(TEST_CODE),
                (a, b, c, d) -> "never"
        );

        assertTrue(result.isFail());
    }

    // ==================== tap ====================

    @Test
    @DisplayName("tap: 执行副作用")
    void tap_execute() {
        AtomicReference<Result<String>> captured = new AtomicReference<>();

        Result<String> result = Results.tap(Result.ok("value"), captured::set);

        assertEquals("value", result.get()); // 返回原result
        assertEquals("value", captured.get().get()); // 副作用执行
    }

    @Test
    @DisplayName("tapSuccess: 成功时执行")
    void tapSuccess_success() {
        AtomicReference<String> captured = new AtomicReference<>();

        Results.tapSuccess(Result.ok("value"), captured::set);

        assertEquals("value", captured.get());
    }

    @Test
    @DisplayName("tapSuccess: 失败时不执行")
    void tapSuccess_fail() {
        AtomicBoolean executed = new AtomicBoolean(false);

        Results.tapSuccess(Result.fail(TEST_CODE), v -> executed.set(true));

        assertFalse(executed.get());
    }

    @Test
    @DisplayName("tapFailure: 失败时执行")
    void tapFailure_fail() {
        AtomicReference<Business> captured = new AtomicReference<>();

        Results.tapFailure(Result.fail(TEST_CODE), captured::set);

        assertNotNull(captured.get());
    }

    @Test
    @DisplayName("tapFailure: 成功时不执行")
    void tapFailure_success() {
        AtomicBoolean executed = new AtomicBoolean(false);

        Results.tapFailure(Result.ok("value"), e -> executed.set(true));

        assertFalse(executed.get());
    }

    @Test
    @DisplayName("tapAsync: 异步执行不阻塞")
    void tapAsync_nonBlocking() {
        AtomicBoolean executed = new AtomicBoolean(false);

        Result<String> result = Results.tapAsync(Result.ok("value"), r -> {
            try {
                Thread.sleep(100);
                executed.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 立即返回，不会等待100ms
        assertEquals("value", result.get());
        // 异步任务可能还没执行完
        assertFalse(executed.get()); // 大概率还没执行
    }

    // ==================== ensure ====================

    @Test
    @DisplayName("ensure: 满足条件保持成功")
    void ensure_pass() {
        Result<Integer> result = Results.ensure(Result.ok(10), n -> n > 5, TEST_CODE);

        assertEquals(10, result.get());
    }

    @Test
    @DisplayName("ensure: 不满足条件转为失败")
    void ensure_fail() {
        Result<Integer> result = Results.ensure(Result.ok(3), n -> n > 5, TEST_CODE, "too small");

        assertTrue(result.isFail());
        assertEquals("too small", result.getError().getDetail());
    }

    @Test
    @DisplayName("ensure: 原结果失败直接返回")
    void ensure_alreadyFail() {
        Result<Integer> original = Result.fail(TEST_CODE);
        Result<Integer> result = Results.ensure(original, n -> true, TEST_CODE_2);

        assertSame(original.getError(), result.getError());
    }

    // ==================== getOrNull ====================

    @Test
    @DisplayName("getOrNull: 成功返回值")
    void getOrNull_success() {
        assertEquals("value", Results.getOrNull(Result.ok("value")));
    }

    @Test
    @DisplayName("getOrNull: 失败返回null")
    void getOrNull_fail() {
        assertNull(Results.getOrNull(Result.fail(TEST_CODE)));
    }

    // ==================== race ====================

    @Test
    @DisplayName("race: 第一个成功")
    void race_firstSuccess() {
        Result<String> result = Results.race(
                () -> Result.ok("first"),
                () -> Result.ok("second")
        );

        assertEquals("first", result.get());
    }

    @Test
    @DisplayName("race: 跳过失败返回成功")
    void race_skipFail() {
        Result<String> result = Results.race(
                () -> Result.fail(TEST_CODE),
                () -> Result.ok("second"),
                () -> Result.ok("third")
        );

        assertEquals("second", result.get());
    }

    @Test
    @DisplayName("race: 全部失败返回最后一个")
    void race_allFail() {
        Result<String> result = Results.race(
                () -> Result.fail(TEST_CODE),
                () -> Result.fail(TEST_CODE_2)
        );

        assertTrue(result.isFail());
        assertEquals(40002, result.getError().getResponseCode().getCode());
    }

    @Test
    @DisplayName("race: 空数组返回ok(null)")
    void race_empty() {
        @SuppressWarnings("unchecked")
        Result<String> result = Results.race(new Supplier[0]);

        assertTrue(result.isSuccess());
        assertNull(result.getOrNull());
    }

    // ==================== retry ====================

    @Test
    @DisplayName("retry: 第一次成功")
    void retry_firstSuccess() {
        AtomicInteger count = new AtomicInteger(0);

        Result<Integer> result = Results.retry(3, () -> {
            count.incrementAndGet();
            return Result.ok(42);
        });

        assertEquals(42, result.get());
        assertEquals(1, count.get());
    }

    @Test
    @DisplayName("retry: 第三次成功")
    void retry_thirdSuccess() {
        AtomicInteger count = new AtomicInteger(0);

        Result<Integer> result = Results.retry(3, () -> {
            if (count.incrementAndGet() < 3) {
                return Result.fail(TEST_CODE);
            }
            return Result.ok(42);
        });

        assertEquals(42, result.get());
        assertEquals(3, count.get());
    }

    @Test
    @DisplayName("retry: 全部失败返回最后")
    void retry_allFail() {
        Result<Integer> result = Results.retry(2, () -> Result.fail(TEST_CODE));

        assertTrue(result.isFail());
    }

    @Test
    @DisplayName("retry: 带延迟")
    void retry_withDelay() {
        long start = System.currentTimeMillis();

        Results.retry(2, Duration.ofMillis(50), () -> Result.fail(TEST_CODE));

        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 50); // 至少等待了50ms
    }

    @Test
    @DisplayName("retry: 中断异常")
    void retry_interrupted() {
        Thread.currentThread().interrupt();

        Result<Integer> result = Results.retry(2, Duration.ofMillis(100), () -> Result.fail(TEST_CODE));

        assertTrue(result.isFail());
        assertEquals("Retry interrupted", result.getError().getDetail());

        Thread.interrupted(); // 清除中断状态
    }

    // ==================== pipe ====================

    @Test
    @DisplayName("pipe: 全部成功")
    void pipe_allSuccess() {
        Result<Integer> result = Results.pipe(
                Result.ok(1),
                n -> Result.ok(n * 2),
                n -> Result.ok(n + 10)
        );

        assertEquals(12, result.get()); // (1 * 2) + 10 = 12
    }

    @Test
    @DisplayName("pipe: 中间失败停止")
    void pipe_middleFail() {
        Result<Integer> result = Results.pipe(
                Result.ok(1),
                n -> Result.fail(TEST_CODE),
                n -> Result.ok(n * 100) // 不会执行
        );

        assertTrue(result.isFail());
    }

    @Test
    @DisplayName("pipe: 初始失败直接返回")
    void pipe_initialFail() {
        Result<Integer> result = Results.pipe(
                Result.fail(TEST_CODE),
                n -> Result.ok(n * 2)
        );

        assertTrue(result.isFail());
    }

    @Test
    @DisplayName("pipe: 空函数数组")
    void pipe_empty() {
        Result<Integer> result = Results.pipe(Result.ok(42));

        assertEquals(42, result.get());
    }

    // ==================== defer / lazy / memoize ====================

    @Test
    @DisplayName("defer: 延迟执行")
    void defer_lazy() {
        AtomicInteger count = new AtomicInteger(0);
        Supplier<Result<Integer>> deferred = Results.defer(() -> {
            count.incrementAndGet();
            return Result.ok(42);
        });

        assertEquals(0, count.get()); // 还没执行

        Result<Integer> r1 = deferred.get();
        assertEquals(1, count.get());
        assertEquals(42, r1.get());

        Result<Integer> r2 = deferred.get();
        assertEquals(1, count.get()); // 不会重复执行
        assertEquals(42, r2.get());
    }

    @Test
    @DisplayName("lazy: 同defer")
    void lazy_alias() {
        AtomicInteger count = new AtomicInteger(0);
        Supplier<Result<Integer>> lazy = Results.lazy(() -> {
            count.incrementAndGet();
            return Result.ok(42);
        });

        lazy.get();
        lazy.get();

        assertEquals(1, count.get());
    }

    @Test
    @DisplayName("memoize: 缓存结果")
    void memoize_cache() {
        AtomicInteger count = new AtomicInteger(0);
        Supplier<Result<Integer>> memoized = Results.memoize(() -> {
            count.incrementAndGet();
            return Result.ok(count.get());
        });

        assertEquals(1, memoized.get().get());
        assertEquals(1, memoized.get().get()); // 返回缓存的1
        assertEquals(1, count.get());
    }

    @Test
    @DisplayName("memoize: 非线程安全但更快")
    void memoize_notThreadSafe() {
        // 单线程测试没问题
        Supplier<Result<Integer>> memoized = Results.memoize(() -> Result.ok(42));
        assertEquals(42, memoized.get().get());
    }

    // ==================== Partition 内部类 ====================

    @Test
    @DisplayName("Partition: 不可变性")
    void partition_immutable() {
        List<Integer> originalList = new ArrayList<>();
        originalList.add(1);

        Results.Partition<Integer> partition = new Results.Partition<>(originalList, List.of());

        // 修改原列表不影响partition
        originalList.add(2);
        assertEquals(1, partition.successes().size());

        // partition返回的列表不可变
        assertThrows(UnsupportedOperationException.class,
                () -> partition.successes().add(3));
    }
}