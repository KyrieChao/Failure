package com.chao.failure.internal.chain;

import com.chao.failure.validator.FastValidator.ValidationContext;
import com.chao.failure.internal.chain.pipeline.ChainCore;
import com.chao.failure.internal.chain.pipeline.Scope;
import com.chao.failure.spi.validation.CancelToken;
import com.chao.failure.spi.validation.ProgressListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IterableTerm 100% 覆盖率测试
 */
@DisplayName("IterableTerm 完整覆盖测试")
public class IterableTermTest {

    // 测试用的 ChainCore 实现
    static class TestChainCore extends ChainCore<TestChainCore> {
        public TestChainCore(ValidationContext context) {
            super(false, context);
        }
    }

    @Test
    @DisplayName("测试 forEach 方法 - 无路径前缀")
    void testForEachWithoutPathPrefix() {
        ValidationContext mockContext = new ValidationContext(false);
        TestChainCore mockCore = new TestChainCore(mockContext);
        IterableTerm<TestChainCore> iterableTerm = new IterableTerm<TestChainCore>() {
            @Override
            public TestChainCore core() {
                return mockCore;
            }
        };

        Consumer<Scope<String>> block = scope -> { };

        TestChainCore result = iterableTerm.forEach(null, block);

        assertSame(mockCore, result);
    }

    @Test
    @DisplayName("测试 forEach 方法 - 有路径前缀")
    void testForEachWithPathPrefix() {
        ValidationContext mockContext = new ValidationContext(false);
        TestChainCore mockCore = new TestChainCore(mockContext);
        IterableTerm<TestChainCore> iterableTerm = new IterableTerm<TestChainCore>() {
            @Override
            public TestChainCore core() {
                return mockCore;
            }
        };

        String pathPrefix = "test";
        Consumer<Scope<String>> block = scope -> { };

        TestChainCore result = iterableTerm.forEach(null, pathPrefix, block);

        assertSame(mockCore, result);
    }

    @Test
    @DisplayName("测试 forEach 方法 - 带监听器和取消令牌")
    void testForEachWithListenerAndCancelToken() {
        ValidationContext context = new ValidationContext(false);
        TestChainCore core = new TestChainCore(context);
        IterableTerm<TestChainCore> iterableTerm = new IterableTerm<TestChainCore>() {
            @Override
            public TestChainCore core() {
                return core;
            }
        };

        List<String> processed = new ArrayList<>();
        CancelToken token = new CancelToken();
        ProgressListener listener = new ProgressListener() {
            @Override
            public void onProgress(long processedItems, long totalItems, com.chao.failure.exception.Business error) {
                token.cancel();
            }
        };

        TestChainCore result = iterableTerm.forEach(List.of("a", "b"), "p", scope -> processed.add(scope.it().value()), listener, token);

        assertSame(core, result);
        assertEquals(List.of("a"), processed);
    }

    @Test
    @DisplayName("测试 forEach 简化重载 - 带监听器和取消令牌")
    void testForEachShortcutWithListenerAndCancelToken() {
        ValidationContext context = new ValidationContext(false);
        TestChainCore core = new TestChainCore(context);
        IterableTerm<TestChainCore> iterableTerm = new IterableTerm<TestChainCore>() {
            @Override
            public TestChainCore core() {
                return core;
            }
        };

        List<String> processed = new ArrayList<>();
        CancelToken token = new CancelToken();
        ProgressListener listener = new ProgressListener() {
            @Override
            public void onProgress(long processedItems, long totalItems, com.chao.failure.exception.Business error) {
                token.cancel();
            }
        };

        TestChainCore result = iterableTerm.forEach(List.of("x", "y"), scope -> processed.add(scope.it().path()), listener, token);

        assertSame(core, result);
        assertEquals(List.of("[0]"), processed);
    }
}
