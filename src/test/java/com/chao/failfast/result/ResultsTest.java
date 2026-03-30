package com.chao.failfast.result;

import com.chao.failfast.exception.Business;
import com.chao.failfast.exception.MultiBusiness;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Results测试")
class ResultsTest {

    @Test
    @DisplayName("tryOf方法 - 成功")
    void testTryOfSuccess() {
        Result<String> result = Results.tryOf(() -> "test", ResponseCode.VALIDATION_ERROR_400);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("test");
    }

    @Test
    @DisplayName("tryOf方法 - 失败")
    void testTryOfFail() {
        Result<String> result = Results.tryOf(() -> {
            throw new RuntimeException("Error");
        }, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("tryOf方法 - 失败带详细描述")
    void testTryOfFailWithDetail() {
        Result<String> result = Results.tryOf(() -> {
            throw new RuntimeException("Error");
        }, ResponseCode.VALIDATION_ERROR_400, "详细错误信息");
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("tryRun方法 - 成功")
    void testTryRunSuccess() {
        Result<Void> result = Results.tryRun(() -> {
        }, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("tryRun方法 - 失败")
    void testTryRunFail() {
        Result<Void> result = Results.tryRun(() -> {
            throw new RuntimeException("Error");
        }, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("tryRun方法 - null runnable")
    void testTryRunNull() {
        assertThatThrownBy(() -> Results.tryRun(null, ResponseCode.VALIDATION_ERROR_400))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("fromOptional方法 - 有值")
    void testFromOptionalWithValue() {
        Result<String> result = Results.fromOptional(Optional.of("test"), ResponseCode.VALIDATION_ERROR_400);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("test");
    }

    @Test
    @DisplayName("fromOptional方法 - 无值")
    void testFromOptionalEmpty() {
        Result<String> result = Results.fromOptional(Optional.empty(), ResponseCode.VALIDATION_ERROR_400);
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("fromOptional方法 - null Optional")
    void testFromOptionalNull() {
        Result<String> result = Results.fromOptional(null, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("fromOptionalOrElse方法 - 有值")
    void testFromOptionalOrElseWithValue() {
        Result<String> result = Results.fromOptionalOrElse(Optional.of("test"), "default");
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("test");
    }

    @Test
    @DisplayName("fromOptionalOrElse方法 - 无值")
    void testFromOptionalOrElseEmpty() {
        Result<String> result = Results.fromOptionalOrElse(Optional.empty(), "default");
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("default");
    }

    @Test
    @DisplayName("fromOptionalOrElse方法 - null Optional")
    void testFromOptionalOrElseNull() {
        Result<String> result = Results.fromOptionalOrElse(null, "default");
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("default");
    }

    @Test
    @DisplayName("when方法 - 条件为true")
    void testWhenTrue() {
        Result<String> result = Results.when(true, () -> Result.ok("test"));
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("test");
    }

    @Test
    @DisplayName("when方法 - 条件为false")
    void testWhenFalse() {
        Result<String> result = Results.when(false, () -> Result.ok("test"));
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isNull();
    }

    @Test
    @DisplayName("when方法 - 条件为true但supplier为null")
    void testWhenTrueNullSupplier() {
        assertThatThrownBy(() -> Results.when(true, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("whenOrFail方法 - 条件为true")
    void testWhenOrFailTrue() {
        Result<String> result = Results.whenOrFail(true, "test", ResponseCode.VALIDATION_ERROR_400);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("test");
    }

    @Test
    @DisplayName("whenOrFail方法 - 条件为false")
    void testWhenOrFailFalse() {
        Result<String> result = Results.whenOrFail(false, "test", ResponseCode.VALIDATION_ERROR_400);
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("whenOrFail方法2 - 条件为false")
    void testWhenOrFail2False() {
        Result<String> result = Results.whenOrFail(false, () -> "test", ResponseCode.VALIDATION_ERROR_400, "详细错误信息");
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("whenOrFail - 条件为 false 时的消息优先级测试")
    void testWhenOrFailFalseMessagePriority() {
        // 准备数据
        // 场景 A: 只有 message, 没有 description
        ResponseCode codeNoDesc = ResponseCode.of(400, "默认消息");

        // 场景 B: 既有 message, 也有 description
        ResponseCode codeHasDesc = ResponseCode.of(400, "默认消息", "详细描述");

        String customDetail = "自定义业务详情";

        // --- 测试点 1: detail 不为 null -> 应返回 detail ---
        Result<String> r1 = Results.whenOrFail(false, () -> "ignored", codeNoDesc, customDetail);
        assertThat(r1.isFail()).isTrue();
        // 核心断言：必须优先返回自定义详情
        assertThat(r1.getDescription()).isEqualTo(customDetail);

        // --- 测试点 2: detail 为 null, description 不为 null -> 应返回 description ---
        Result<String> r2 = Results.whenOrFail(false, () -> "ignored", codeHasDesc, null);
        assertThat(r2.isFail()).isTrue();
        // 核心断言：返回详细描述
        assertThat(r2.getDescription()).isEqualTo("详细描述");

        // --- 测试点 3: detail 为 null, description 为 null -> 应返回 message ---
        Result<String> r3 = Results.whenOrFail(false, () -> "ignored", codeNoDesc, null);
        assertThat(r3.isFail()).isTrue();
        // 核心断言：返回默认消息
        assertThat(r3.getMessage()).isEqualTo("默认消息");
    }

    @Test
    @DisplayName("whenOrFail方法 - 条件为true且supplier成功")
    void testWhenOrFailSupplierSuccess() {
        Result<String> result = Results.whenOrFail(true, () -> "test", ResponseCode.VALIDATION_ERROR_400);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("test");
    }

    @Test
    @DisplayName("whenOrFail方法 - 条件为true但supplier失败")
    void testWhenOrFailSupplierFail() {
        Result<String> result = Results.whenOrFail(true, () -> {
            throw new RuntimeException("Error");
        }, ResponseCode.VALIDATION_ERROR_400);
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("sequence方法 - 全部成功")
    void testSequenceAllSuccess() {
        Result<String> r1 = Result.ok("test1");
        Result<String> r2 = Result.ok("test2");
        Result<List<String>> result = Results.sequence(r1, r2);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).contains("test1", "test2");
    }

    @Test
    @DisplayName("sequence方法 - 有失败")
    void testSequenceWithFail() {
        Result<String> r1 = Result.ok("test1");
        Result<String> r2 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<List<String>> result = Results.sequence(r1, r2);
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("sequenceAll方法 - 全部成功")
    void testSequenceAllAllSuccess() {
        Result<String> r1 = Result.ok("test1");
        Result<String> r2 = Result.ok("test2");
        Result<List<String>> result = Results.sequenceAll(r1, r2);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).contains("test1", "test2");
    }

    @Test
    @DisplayName("sequenceAll方法 - 有失败")
    void testSequenceAllWithFail() {
        Result<String> r1 = Result.ok("test1");
        Result<String> r2 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<List<String>> result = Results.sequenceAll(r1, r2);
        assertThat(result.isFail()).isTrue();
        assertThat(result.getError()).isInstanceOf(MultiBusiness.class);
    }

    @Test
    @DisplayName("partition方法")
    void testPartition() {
        Result<String> r1 = Result.ok("test1");
        Result<String> r2 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> r3 = Result.ok("test3");

        Results.Partition<String> partition = Results.partition(List.of(r1, r2, r3));
        assertThat(partition.successes()).contains("test1", "test3");
        assertThat(partition.failures()).hasSize(1);
        assertThat(partition.hasSuccesses()).isTrue();
        assertThat(partition.hasFailures()).isTrue();
        assertThat(partition.isAllSuccess()).isFalse();
        assertThat(partition.isAllFail()).isFalse();
    }

    @Test
    @DisplayName("successes方法")
    void testSuccesses() {
        Result<String> r1 = Result.ok("test1");
        Result<String> r2 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> r3 = Result.ok("test3");

        List<String> successes = Results.successes(List.of(r1, r2, r3));
        assertThat(successes).contains("test1", "test3");
    }

    @Test
    @DisplayName("failures方法")
    void testFailures() {
        Result<String> r1 = Result.ok("test1");
        Result<String> r2 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> r3 = Result.ok("test3");

        List<Business> failures = Results.failures(List.of(r1, r2, r3));
        assertThat(failures).hasSize(1);
    }

    @Test
    @DisplayName("fold方法")
    void testFold() {
        Result<Integer> r1 = Result.ok(1);
        Result<Integer> r2 = Result.ok(2);
        Result<Integer> r3 = Result.ok(3);

        Result<Integer> result = Results.fold(List.of(r1, r2, r3), 0, Integer::sum);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo(6);
    }

    @Test
    @DisplayName("reduce方法 - 非空列表")
    void testReduceNonEmpty() {
        Result<Integer> r1 = Result.ok(1);
        Result<Integer> r2 = Result.ok(2);
        Result<Integer> r3 = Result.ok(3);

        Result<Integer> result = Results.reduce(List.of(r1, r2, r3), Integer::sum);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo(6);
    }

    @Test
    @DisplayName("reduce方法 - 空列表")
    void testReduceEmpty() {
        Result<Integer> result = Results.reduce(List.of(), Integer::sum);
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("traverse方法 - 全部成功")
    void testTraverseAllSuccess() {
        List<String> list = List.of("1", "2", "3");
        Result<List<Integer>> result = Results.traverse(list, s -> Result.ok(Integer.parseInt(s)));
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).contains(1, 2, 3);
    }

    @Test
    @DisplayName("traverse方法 - 有失败")
    void testTraverseWithFail() {
        List<String> list = List.of("1", "abc", "3");
        Result<List<Integer>> result = Results.traverse(list, s -> {
            try {
                return Result.ok(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                return Result.fail(ResponseCode.VALIDATION_ERROR_400);
            }
        });
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("traverseAll方法 - 全部成功")
    void testTraverseAllAllSuccess() {
        List<String> list = List.of("1", "2", "3");
        Result<List<Integer>> result = Results.traverseAll(list, s -> Result.ok(Integer.parseInt(s)));
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).contains(1, 2, 3);
    }

    @Test
    @DisplayName("traverseAll方法 - 有失败")
    void testTraverseAllWithFail() {
        List<String> list = List.of("1", "abc", "3");
        Result<List<Integer>> result = Results.traverseAll(list, s -> {
            try {
                return Result.ok(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                return Result.fail(ResponseCode.VALIDATION_ERROR_400);
            }
        });
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("traverseIndexed方法")
    void testTraverseIndexed() {
        List<String> list = List.of("1", "2", "3");
        Result<List<String>> result = Results.traverseIndexed(list, (index, s) -> Result.ok(index + ":" + s));
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).contains("0:1", "1:2", "2:3");
    }

    @Test
    @DisplayName("traverseAllIndexed方法")
    void testTraverseAllIndexed() {
        List<String> list = List.of("1", "abc", "3");
        Result<List<String>> result = Results.traverseAllIndexed(list, (index, s) -> {
            try {
                Integer.parseInt(s);
                return Result.ok(index + ":" + s);
            } catch (NumberFormatException e) {
                return Result.fail(ResponseCode.VALIDATION_ERROR_400);
            }
        });
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("zip方法 - 两个结果")
    void testZipTwo() {
        Result<String> r1 = Result.ok("Hello");
        Result<String> r2 = Result.ok("World");
        Result<String> result = Results.zip(r1, r2, (s1, s2) -> s1 + " " + s2);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("zip方法 - 三个结果")
    void testZipThree() {
        Result<String> r1 = Result.ok("Hello");
        Result<String> r2 = Result.ok(" ");
        Result<String> r3 = Result.ok("World");
        Result<String> result = Results.zip(r1, r2, r3, (s1, s2, s3) -> s1 + s2 + s3);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("zip方法 - 四个结果")
    void testZipFour() {
        Result<String> r1 = Result.ok("Hello");
        Result<String> r2 = Result.ok(" ");
        Result<String> r3 = Result.ok("Beautiful");
        Result<String> r4 = Result.ok(" World");
        Result<String> result = Results.zip(r1, r2, r3, r4, (s1, s2, s3, s4) -> s1 + s2 + s3 + s4);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("Hello Beautiful World");
    }

    @Test
    @DisplayName("tap方法")
    void testTap() {
        Result<String> result = Result.ok("test");
        AtomicInteger counter = new AtomicInteger(0);
        Result<String> tapped = Results.tap(result, r -> counter.incrementAndGet());
        assertThat(tapped).isSameAs(result);
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("tapSuccess方法")
    void testTapSuccess() {
        Result<String> result = Result.ok("test");
        AtomicInteger counter = new AtomicInteger(0);
        Result<String> tapped = Results.tapSuccess(result, s -> counter.incrementAndGet());
        assertThat(tapped).isSameAs(result);
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("tapFailure方法")
    void testTapFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        AtomicInteger counter = new AtomicInteger(0);
        Result<String> tapped = Results.tapFailure(result, e -> counter.incrementAndGet());
        assertThat(tapped).isSameAs(result);
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("tapAsync方法")
    void testTapAsync() {
        Result<String> result = Result.ok("test");
        Result<String> tapped = Results.tapAsync(result, r -> {
        });
        assertThat(tapped).isSameAs(result);
    }

    @Test
    @DisplayName("ensure方法 - 成功且条件满足")
    void testEnsureSuccess() {
        Result<String> result = Result.ok("test");
        Result<String> ensured = Results.ensure(result, s -> s.length() > 0, ResponseCode.VALIDATION_ERROR_400);
        assertThat(ensured.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("ensure方法 - 成功但条件不满足")
    void testEnsureFail() {
        Result<String> result = Result.ok("test");
        Result<String> ensured = Results.ensure(result, s -> s.length() > 10, ResponseCode.VALIDATION_ERROR_400);
        assertThat(ensured.isFail()).isTrue();
    }

    @Test
    @DisplayName("ensure方法 - 失败状态")
    void testEnsureWithFail() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> ensured = Results.ensure(result, s -> s.length() > 0, ResponseCode.VALIDATION_ERROR_400);
        assertThat(ensured.isFail()).isTrue();
    }

    @Test
    @DisplayName("getOrNull方法 - 成功状态")
    void testGetOrNullSuccess() {
        Result<String> result = Result.ok("test");
        String value = Results.getOrNull(result);
        assertThat(value).isEqualTo("test");
    }

    @Test
    @DisplayName("getOrNull方法 - 失败状态")
    void testGetOrNullFail() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        String value = Results.getOrNull(result);
        assertThat(value).isNull();
    }

    @Test
    @DisplayName("race方法 - 第一个成功")
    void testRaceFirstSuccess() {
        Result<String> result = Results.race(
                () -> Result.ok("first"),
                () -> Result.fail(ResponseCode.VALIDATION_ERROR_400)
        );
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("first");
    }

    @Test
    @DisplayName("race方法 - 全部失败")
    void testRaceAllFail() {
        Result<String> result = Results.race(
                () -> Result.fail(ResponseCode.VALIDATION_ERROR_400),
                () -> Result.fail(ResponseCode.VALIDATION_ERROR_400)
        );
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("race方法 - 空参数")
    void testRaceEmpty() {
        Result<String> result = Results.race();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isNull();
    }

    @Test
    @DisplayName("retry方法 - 成功")
    void testRetrySuccess() {
        AtomicInteger counter = new AtomicInteger(0);
        Result<String> result = Results.retry(3, () -> {
            counter.incrementAndGet();
            return Result.ok("test");
        });
        assertThat(result.isSuccess()).isTrue();
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("retry方法 - 失败后成功")
    void testRetryFailThenSuccess() {
        AtomicInteger counter = new AtomicInteger(0);
        Result<String> result = Results.retry(3, () -> {
            int count = counter.incrementAndGet();
            if (count < 3) {
                return Result.fail(ResponseCode.VALIDATION_ERROR_400);
            }
            return Result.ok("test");
        });
        assertThat(result.isSuccess()).isTrue();
        assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("retry方法 - 全部失败")
    void testRetryAllFail() {
        AtomicInteger counter = new AtomicInteger(0);
        Result<String> result = Results.retry(3, () -> {
            counter.incrementAndGet();
            return Result.fail(ResponseCode.VALIDATION_ERROR_400);
        });
        assertThat(result.isFail()).isTrue();
        assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("retry方法 - 睡眠中被中断覆盖")
    void testRetryInterrupted() throws Exception {
        // 1. 准备一个会一直失败的 Supplier，强制 retry 进入循环并触发 sleep
        AtomicInteger counter = new AtomicInteger(0);
        Supplier<Result<String>> failingSupplier = () -> {
            counter.incrementAndGet();
            // 始终返回失败，迫使 retry 进行下一次尝试并执行 sleep
            return Result.fail(ResponseCode.VALIDATION_ERROR_400);
        };

        // 2. 在一个单独的线程中执行 retry
        // 设置较大的重试次数和延迟，给中断操作留出时间窗口
        int times = 5;
        Duration delay = Duration.ofSeconds(2);

        final Result<String>[] resultHolder = new Result[1];
        final Exception[] exceptionHolder = new Exception[1];

        Thread retryThread = new Thread(() -> {
            try {
                resultHolder[0] = Results.retry(times, delay, failingSupplier);
            } catch (Exception e) {
                exceptionHolder[0] = e;
            }
        });

        retryThread.start();

        // 3. 等待短暂时间，确保线程已经进入第一次 sleep (i=1 时的 sleep)
        // 第一次执行 (i=0) 不 sleep，第二次 (i=1) 才会 sleep。
        // 我们等待 200ms，足够让它完成第一次尝试并开始 sleep
        Thread.sleep(200);

        // 4. 【关键】发送中断信号
        retryThread.interrupt();

        // 5. 等待线程结束
        retryThread.join(2000); // 最多等2秒，防止死锁

        // 6. 断言验证
        // 确保线程正常结束了，没有抛出未捕获的异常
        assertThat(exceptionHolder[0]).isNull();

        // 确保结果是被中断导致的失败
        assertThat(resultHolder[0]).isNotNull();
        assertThat(resultHolder[0].isFail()).isTrue();

        // 验证错误码是否是中断错误
        assertThat(resultHolder[0].getCode()).isEqualTo(ResponseCode.INTERRUPTED_ERROR.getCode());
        // 或者验证消息
        // Results.java
//        String msg = Locale.getDefault().getLanguage().equals("zh") ? "重试被中断" : "Retry interrupted";
        assertThat(resultHolder[0].getMessage()).contains("重试被中断");

        // 验证重试次数：
        // i=0: 执行supplier (count=1), 失败
        // i=1: sleep (被中断), 直接返回，不再执行 supplier
        // 所以 counter 应该是 1
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("pipe方法 - 全部成功")
    void testPipeAllSuccess() {
        Result<String> result = Results.pipe(
                Result.ok("test"),
                s -> Result.ok(s.toUpperCase()),
                s -> Result.ok(s + "!")
        );
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("TEST!");
    }

    @Test
    @DisplayName("pipe方法 - 中间失败")
    void testPipeWithFail() {
        Result<String> result = Results.pipe(
                Result.ok("test"),
                s -> Result.fail(ResponseCode.VALIDATION_ERROR_400),
                s -> Result.ok(s + "!")
        );
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("defer方法")
    void testDefer() {
        AtomicInteger counter = new AtomicInteger(0);
        var supplier = Results.defer(() -> {
            counter.incrementAndGet();
            return Result.ok("test");
        });

        // 第一次调用
        Result<String> result1 = supplier.get();
        assertThat(result1.isSuccess()).isTrue();
        assertThat(counter.get()).isEqualTo(1);

        // 第二次调用（应该返回缓存的结果）
        Result<String> result2 = supplier.get();
        assertThat(result2.isSuccess()).isTrue();
        assertThat(counter.get()).isEqualTo(1); // 计数器应该还是1
    }

    @Test
    @DisplayName("defer 方法 - 多线程并发覆盖双重检查")
    void testDeferConcurrency() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1); // 用于让两个线程同时起跑
        CountDownLatch mergeLatch = new CountDownLatch(2);  // 用于等待两个线程结束

        var supplier = Results.defer(() -> {
            // 模拟一点耗时，增加线程 B 在外层等待锁的概率
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            counter.incrementAndGet();
            return Result.ok("test");
        });

        final Result<?>[] resultA = new Result[1];
        final Result<?>[] resultB = new Result[1];

        // 线程 A
        Thread threadA = new Thread(() -> {
            try {
                startLatch.await(); // 等待发令枪
                resultA[0] = supplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                mergeLatch.countDown();
            }
        });

        // 线程 B
        Thread threadB = new Thread(() -> {
            try {
                startLatch.await(); // 等待发令枪
                resultB[0] = supplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                mergeLatch.countDown();
            }
        });

        threadA.start();
        threadB.start();

        // 发射！让两个线程几乎同时执行
        startLatch.countDown();

        // 等待两个线程结束 (设置超时防止死锁)
        boolean finished = mergeLatch.await(2, java.util.concurrent.TimeUnit.SECONDS);
        assertThat(finished).isTrue();

        // --- 断言验证 ---

        // 1. 两个结果都应该成功
        assertThat(resultA[0].isSuccess()).isTrue();
        assertThat(resultB[0].isSuccess()).isTrue();

        // 2. 核心验证：计数器只能为 1
        // 这证明了：
        // - 线程 A 走了内层 if(true) 分支 (执行了计算)
        // - 线程 B 走了内层 if(false) 分支 (被拦截，没执行计算)
        // 如果计数器是 2，说明双重检查失效了。
        assertThat(counter.get()).isEqualTo(1);

        // 3. 两个结果应该是同一个对象引用 (证明都返回了缓存)
        assertThat(resultA[0]).isSameAs(resultB[0]);
    }

    @Test
    @DisplayName("lazy方法")
    void testLazy() {
        AtomicInteger counter = new AtomicInteger(0);
        var supplier = Results.lazy(() -> {
            counter.incrementAndGet();
            return Result.ok("test");
        });

        Result<String> result = supplier.get();
        assertThat(result.isSuccess()).isTrue();
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("memoize方法")
    void testMemoize() {
        AtomicInteger counter = new AtomicInteger(0);
        var supplier = Results.memoize(() -> {
            counter.incrementAndGet();
            return Result.ok("test");
        });

        // 第一次调用
        Result<String> result1 = supplier.get();
        assertThat(result1.isSuccess()).isTrue();
        assertThat(counter.get()).isEqualTo(1);

        // 第二次调用（应该返回缓存的结果）
        Result<String> result2 = supplier.get();
        assertThat(result2.isSuccess()).isTrue();
        assertThat(counter.get()).isEqualTo(1); // 计数器应该还是1
    }

    @Test
    @DisplayName("traverseIndexed方法 - 有失败")
    void testTraverseIndexedWithFail() {
        List<String> list = List.of("1", "abc", "3");
        Result<List<String>> result = Results.traverseIndexed(list, (index, s) -> {
            try {
                Integer.parseInt(s);
                return Result.ok(index + ":" + s);
            } catch (NumberFormatException e) {
                return Result.fail(ResponseCode.VALIDATION_ERROR_400);
            }
        });
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("traverseAllIndexed方法 - 全部成功")
    void testTraverseAllIndexedAllSuccess() {
        List<String> list = List.of("1", "2", "3");
        Result<List<String>> result = Results.traverseAllIndexed(list, (index, s) -> Result.ok(index + ":" + s));
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).contains("0:1", "1:2", "2:3");
    }

    @Test
    @DisplayName("zip方法 - 两个结果有失败")
    void testZipTwoWithFail() {
        Result<String> r1 = Result.ok("Hello");
        Result<String> r2 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> result = Results.zip(r1, r2, (s1, s2) -> s1 + " " + s2);
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("zip方法 - 三个结果有失败")
    void testZipThreeWithFail() {
        Result<String> r1 = Result.ok("Hello");
        Result<String> r2 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> r3 = Result.ok("World");
        Result<String> result = Results.zip(r1, r2, r3, (s1, s2, s3) -> s1 + s2 + s3);
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("zip方法 - 四个结果有失败")
    void testZipFourWithFail() {
        Result<String> r1 = Result.ok("Hello");
        Result<String> r2 = Result.ok(" ");
        Result<String> r3 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> r4 = Result.ok("World");
        Result<String> result = Results.zip(r1, r2, r3, r4, (s1, s2, s3, s4) -> s1 + s2 + s3 + s4);
        assertThat(result.isFail()).isTrue();
    }

    @Test
    @DisplayName("zip方法(4参) - r2 失败 (覆盖第3行)")
    void testZipFour_R2_Fail() {
        Result<String> r1 = Result.ok("Hello");
        Result<String> r2 = Result.fail(ResponseCode.VALIDATION_ERROR_400); // r2 失败
        Result<String> r3 = Result.ok("World");
        Result<String> r4 = Result.ok("!");

        Result<String> result = Results.zip(r1, r2, r3, r4, (s1, s2, s3, s4) -> s1 + s2 + s3 + s4);

        assertThat(result.isFail()).isTrue();
        // 可选：验证返回的是 r2 的错误
        // assertThat(result.getCode()).isEqualTo(ResponseCode.VALIDATION_ERROR_400.getCode());
    }

    @Test
    @DisplayName("zip方法(4参) - r4 失败 (覆盖第5行)")
    void testZipFour_R4_Fail() {
        Result<String> r1 = Result.ok("Hello");
        Result<String> r2 = Result.ok(" ");
        Result<String> r3 = Result.ok("Beautiful");
        Result<String> r4 = Result.fail(ResponseCode.VALIDATION_ERROR_400); // r4 失败

        Result<String> result = Results.zip(r1, r2, r3, r4, (s1, s2, s3, s4) -> s1 + s2 + s3 + s4);

        assertThat(result.isFail()).isTrue();
        // 可选：验证返回的是 r4 的错误
        // assertThat(result.getCode()).isEqualTo(ResponseCode.VALIDATION_ERROR_400.getCode());
    }

    @Test
    @DisplayName("zip 方法 (4 参) - r1 失败 (覆盖第 2 行)")
    void testZipFour_R1_Fail() {
        // 关键点：第一个参数直接失败
        Result<String> r1 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> r2 = Result.ok("World");
        Result<String> r3 = Result.ok("!");
        Result<String> r4 = Result.ok("!");

        Result<String> result = Results.zip(r1, r2, r3, r4, (s1, s2, s3, s4) -> s1 + s2 + s3 + s4);

        // 断言：结果应该是失败的
        assertThat(result.isFail()).isTrue();

        // 可选：验证返回的错误信息确实来自 r1
        // assertThat(result.getCode()).isEqualTo(ResponseCode.VALIDATION_ERROR_400.getCode());
    }

    @Test
    @DisplayName("tapSuccess方法 - 失败状态")
    void testTapSuccessWithFail() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        AtomicInteger counter = new AtomicInteger(0);
        Result<String> tapped = Results.tapSuccess(result, s -> counter.incrementAndGet());
        assertThat(tapped).isSameAs(result);
        assertThat(counter.get()).isEqualTo(0); // 不应该执行
    }

    @Test
    @DisplayName("tapFailure方法 - 成功状态")
    void testTapFailureWithSuccess() {
        Result<String> result = Result.ok("test");
        AtomicInteger counter = new AtomicInteger(0);
        Result<String> tapped = Results.tapFailure(result, e -> counter.incrementAndGet());
        assertThat(tapped).isSameAs(result);
        assertThat(counter.get()).isEqualTo(0); // 不应该执行
    }

    @Test
    @DisplayName("retry方法 - 带延迟")
    void testRetryWithDelay() {
        AtomicInteger counter = new AtomicInteger(0);
        Result<String> result = Results.retry(2, Duration.ofMillis(10), () -> {
            int count = counter.incrementAndGet();
            if (count < 2) {
                return Result.fail(ResponseCode.VALIDATION_ERROR_400);
            }
            return Result.ok("test");
        });
        assertThat(result.isSuccess()).isTrue();
        assertThat(counter.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("getBusinessMessage方法")
    void testGetBusinessMessage() {
        // 测试只有errorCode的情况
        String message1 = ResultsTest.getBusinessMessage(ResponseCode.VALIDATION_ERROR_400, null);
        assertThat(message1).isNotNull();

        // 测试有detail的情况
        String detail = "详细错误信息";
        String message2 = ResultsTest.getBusinessMessage(ResponseCode.VALIDATION_ERROR_400, detail);
        assertThat(message2).isEqualTo(detail);
    }

    // 辅助方法，用于测试private方法
    private static String getBusinessMessage(ResponseCode errorCode, String detail) {
        try {
            var method = Results.class.getDeclaredMethod("getBusinessMessage", ResponseCode.class, String.class);
            method.setAccessible(true);
            return (String) method.invoke(null, errorCode, detail);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Partition方法 - 全部成功")
    void testPartitionAllSuccess() {
        Result<String> r1 = Result.ok("test1");
        Result<String> r2 = Result.ok("test2");

        Results.Partition<String> partition = Results.partition(List.of(r1, r2));
        assertThat(partition.successes()).contains("test1", "test2");
        assertThat(partition.failures()).isEmpty();
        assertThat(partition.hasSuccesses()).isTrue();
        assertThat(partition.hasFailures()).isFalse();
        assertThat(partition.isAllSuccess()).isTrue();
        assertThat(partition.isAllFail()).isFalse();
    }

    @Test
    @DisplayName("Partition方法 - 全部失败")
    void testPartitionAllFail() {
        Result<String> r1 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> r2 = Result.fail(ResponseCode.VALIDATION_ERROR_400);

        Results.Partition<String> partition = Results.partition(List.of(r1, r2));
        assertThat(partition.successes()).isEmpty();
        assertThat(partition.failures()).hasSize(2);
        assertThat(partition.hasSuccesses()).isFalse();
        assertThat(partition.hasFailures()).isTrue();
        assertThat(partition.isAllSuccess()).isFalse();
        assertThat(partition.isAllFail()).isTrue();
    }

}
