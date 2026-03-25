package com.chao.failfast.internal.check;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.internal.chain.pipeline.ChainCore;
import com.chao.failfast.internal.chain.pipeline.Scope;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IterableChecks 工具类测试")
class IterableChecksTest {

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
    @DisplayName("forEach: 当链应跳过验证时应直接返回")
    void forEachShouldReturnDirectlyWhenChainShouldSkip() {
        TestChain chain = TestChain.create(true);
        // 触发失败，使链进入跳过状态
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
    @DisplayName("forEach: 当集合为空时应直接返回")
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
    @DisplayName("forEach: pathPrefix 为 null 时使用默认索引路径")
    void forEachShouldUseIndexPathWhenPrefixIsNull() {
        TestChain chain = TestChain.create(false);
        List<String> items = List.of("a", "b");

        List<String> paths = new ArrayList<>();
        IterableChecks.forEach(chain, items, null, scope -> paths.add(scope.it().path()));

        assertThat(paths).containsExactly("[0]", "[1]");
    }

    @Test
    @DisplayName("forEach: failFast 模式下产生错误后应停止遍历")
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
    @DisplayName("forEach: 错误数达到上限后应停止遍历")
    void forEachShouldStopWhenMaxErrorsReached() {
        TestChain chain = TestChain.create(false);
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < 200; i++) items.add(i);

        List<Integer> processed = new ArrayList<>();
        IterableChecks.forEach(chain, items, "p", scope -> {
            processed.add(scope.it().value());
            scope.check(scope.it(), v -> false, ResponseCode.VALIDATION_ERROR_400, "err");
        });

        assertThat(processed.size()).isEqualTo(100);
    }

    @Test
    @DisplayName("forEach: 元素数达到上限后应停止遍历")
    void forEachShouldStopWhenMaxItemsReached() {
        TestChain chain = TestChain.create(false);
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < 1100; i++) items.add(i);

        List<Integer> processed = new ArrayList<>();
        IterableChecks.forEach(chain, items, "", scope -> processed.add(scope.it().value()));

        assertThat(processed.size()).isEqualTo(1000);
    }
}
