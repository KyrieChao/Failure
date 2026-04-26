package com.chao.failfast.internal.check;

import com.chao.failfast.validator.FastValidator;
import com.chao.failfast.config.mapping.CodeMappingConfig;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.exception.Business;
import com.chao.failfast.internal.chain.pipeline.ChainCore;
import com.chao.failfast.internal.core.Ex;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.spi.validation.CancelToken;
import com.chao.failfast.spi.validation.ProgressListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("display")
class IterableChecksTest {

    @AfterEach
    void tearDown() {
        Ex.setContext(null);
    }

    static class TestChain extends ChainCore<TestChain> {
        protected TestChain(boolean failFast, FastValidator.ValidationContext context) {
            super(failFast, context);
        }

        public static TestChain create(boolean failFast) {
            return new TestChain(failFast, null);
        }
    }

    @Test
    @DisplayName("forEach: 当集合不为空时应遍历执行验证")
    void forEachShouldExecuteValidationForEachItem() {
        TestChain chain = TestChain.create(false);
        List<String> items = new ArrayList<>();
        items.add("test1");
        items.add("test2");

        List<String> processedItems = new ArrayList<>();
        TestChain result = IterableChecks.forEach(chain, items, "prefix", scope -> {
            processedItems.add(scope.it().value());
        });

        assertThat(result).isSameAs(chain);
        assertThat(processedItems).containsExactly("test1", "test2");
    }

    @Test
    @DisplayName("forEach: 当集合为 null 时应直接返回")
    void forEachShouldReturnDirectlyWhenItemsIsNull() {
        TestChain chain = TestChain.create(false);

        List<Object> processedItems = new ArrayList<>();
        TestChain result = IterableChecks.forEach(chain, null, "prefix", scope -> {
            processedItems.add(scope.it().value());
        });

        assertThat(result).isSameAs(chain);
        assertThat(processedItems).isEmpty();
    }

    @Test
@DisplayName("display")
    void forEachShouldReturnDirectlyWhenChainShouldSkip() {
        TestChain chain = TestChain.create(true);
        // 触发失败，使链进入跳过状�?
        chain.check(false, ResponseCode.VALIDATION_ERROR_400, "Error");

        List<String> items = new ArrayList<>();
        items.add("test1");
        List<String> processedItems = new ArrayList<>();

        TestChain result = IterableChecks.forEach(chain, items, "prefix", scope -> {
            processedItems.add(scope.it().value());
        });

        assertThat(result).isSameAs(chain);
        assertThat(processedItems).isEmpty();
    }

    @Test
    @DisplayName("forEach: 当使用默认路径前缀时应正常工作")
    void forEachShouldWorkWithDefaultPathPrefix() {
        TestChain chain = TestChain.create(false);
        List<String> items = new ArrayList<>();
        items.add("test1");

        List<String> processedItems = new ArrayList<>();
        TestChain result = IterableChecks.forEach(chain, items, scope -> {
            processedItems.add(scope.it().value());
        });

        assertThat(result).isSameAs(chain);
        assertThat(processedItems).containsExactly("test1");
    }

    @Test
@DisplayName("display")
    void forEachShouldReturnDirectlyWhenItemsIsEmpty() {
        TestChain chain = TestChain.create(false);
        List<String> items = new ArrayList<>();

        List<String> processedItems = new ArrayList<>();
        TestChain result = IterableChecks.forEach(chain, items, "prefix", scope -> {
            processedItems.add(scope.it().value());
        });

        assertThat(result).isSameAs(chain);
        assertThat(processedItems).isEmpty();
    }

    @Test
@DisplayName("display")
    void forEachShouldUseIndexPathWhenPrefixIsNull() {
        TestChain chain = TestChain.create(false);
        List<String> items = List.of("a", "b");

        List<String> paths = new ArrayList<>();
        IterableChecks.forEach(chain, items, null, scope -> paths.add(scope.it().path()));

        assertThat(paths).containsExactly("[0]", "[1]");
    }

    @Test
@DisplayName("display")
    void forEachShouldStopWhenFailFastAndErrorOccurs() {
        TestChain chain = TestChain.create(true);
        List<String> items = List.of("a", "b");

        List<String> processed = new ArrayList<>();
        IterableChecks.forEach(chain, items, "p", scope -> {
            processed.add(scope.it().value());
            scope.check(scope.it(), v -> false, ResponseCode.VALIDATION_ERROR_400, "err");
        });

        assertThat(processed).containsExactly("a");
    }

    @Test
@DisplayName("display")
    void forEachShouldStopWhenMaxErrorsReached() {
        TestChain chain = TestChain.create(false);
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < 200; i++) items.add(i);

        List<Integer> processed = new ArrayList<>();
        IterableChecks.forEach(chain, items, "p", scope -> {
            processed.add(scope.it().value());
            scope.check(scope.it(), v -> false, ResponseCode.VALIDATION_ERROR_400, "err");
        });

        assertThat(processed.size()).isEqualTo(50);
    }

    @Test
@DisplayName("display")
    void forEachShouldStopWhenMaxItemsReached() {
        TestChain chain = TestChain.create(false);
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < 1100; i++) items.add(i);

        List<Integer> processed = new ArrayList<>();
        IterableChecks.forEach(chain, items, "", scope -> processed.add(scope.it().value()));

        assertThat(processed.size()).isEqualTo(1000);
    }

    @Test
    @DisplayName("forEach: 支持进度监听")
    void forEachShouldNotifyProgressListener() {
        TestChain chain = TestChain.create(false);
        List<String> items = List.of("a", "b", "c");
        List<String> events = new ArrayList<>();

        ProgressListener listener = new ProgressListener() {
            @Override
            public void onStarted(long totalItems) {
                events.add("start:" + totalItems);
            }

            @Override
            public void onProgress(long processedItems, long totalItems, Business error) {
                events.add("progress:" + processedItems);
            }

            @Override
            public void onCompleted(long totalItems, List<Business> allErrors) {
                events.add("done:" + totalItems);
            }
        };

        IterableChecks.forEach(chain, items, "p", scope -> {
        }, listener, null);
        assertThat(events).contains("start:3", "progress:1", "progress:2", "progress:3", "done:3");
    }

    @Test
    @DisplayName("forEach: 支持取消令牌")
    void forEachShouldStopWhenCancelled() {
        TestChain chain = TestChain.create(false);
        List<Integer> items = List.of(1, 2, 3, 4);
        CancelToken token = new CancelToken();
        List<Integer> processed = new ArrayList<>();
        List<String> events = new ArrayList<>();
        ProgressListener listener = new ProgressListener() {
            @Override
            public void onProgress(long processedItems, long totalItems, Business error) {
                events.add("progress:" + processedItems);
                token.cancel();
            }

            @Override
            public void onCancelled() {
                events.add("cancelled");
            }
        };

        IterableChecks.forEach(chain, items, "p", scope -> processed.add(scope.it().value()), listener, token);
        assertThat(processed).hasSize(1);
        assertThat(events).contains("cancelled");
    }

    @Test
    void should_iterateNormally_when_cancelTokenExistsButIsNotCancelled() {
        TestChain chain = TestChain.create(false);
        CancelToken token = new CancelToken();
        List<Integer> processed = new ArrayList<>();

        IterableChecks.forEach(chain, List.of(1, 2), "p", scope -> processed.add(scope.it().value()), null, token);

        assertThat(processed).containsExactly(1, 2);
    }

    @Test
    void should_returnUnknownTotal_when_iterableIsNotCollection() {
        TestChain chain = TestChain.create(false);
        List<Long> totals = new ArrayList<>();
        ProgressListener listener = new ProgressListener() {
            @Override
            public void onStarted(long totalItems) {
                totals.add(totalItems);
            }
        };

        Iterable<String> iterable = () -> List.of("a", "b").iterator();
        IterableChecks.forEach(chain, iterable, "p", scope -> { }, listener, null);

        assertThat(totals).containsExactly(-1L);
    }

    @Test
    void should_stopWhenLocalMaxErrorsReached_before_chainStrictLimitTriggers() {
        FailureProperties properties = new FailureProperties();
        properties.getChain().setMaxErrors(200);
        Ex.setContext(new FailureContext(properties, new CodeMappingConfig(properties), null));

        TestChain chain = TestChain.create(false);
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            items.add(i);
        }

        List<Integer> processed = new ArrayList<>();
        IterableChecks.forEach(chain, items, "p", scope -> {
            processed.add(scope.it().value());
            scope.check(scope.it(), value -> false, ResponseCode.VALIDATION_ERROR_400, "err");
        });

        assertThat(processed).hasSize(100);
    }
}
