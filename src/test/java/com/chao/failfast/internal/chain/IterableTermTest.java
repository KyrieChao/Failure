package com.chao.failfast.internal.chain;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.internal.chain.pipeline.ChainCore;
import com.chao.failfast.internal.chain.pipeline.Scope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

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
        // 创建测试对象
        ValidationContext mockContext = mock(ValidationContext.class);
        TestChainCore mockCore = mock(TestChainCore.class);
        IterableTerm<TestChainCore> iterableTerm = new IterableTerm<TestChainCore>() {
            @Override
            public TestChainCore core() {
                return mockCore;
            }
        };

        // 准备测试数据
        Consumer<Scope<String>> mockBlock = mock(Consumer.class);

        // 调用 forEach 方法
        TestChainCore result = iterableTerm.forEach(null, mockBlock);

        // 验证结果
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 forEach 方法 - 有路径前缀")
    void testForEachWithPathPrefix() {
        // 创建测试对象
        ValidationContext mockContext = mock(ValidationContext.class);
        TestChainCore mockCore = mock(TestChainCore.class);
        IterableTerm<TestChainCore> iterableTerm = new IterableTerm<TestChainCore>() {
            @Override
            public TestChainCore core() {
                return mockCore;
            }
        };

        // 准备测试数据
        String pathPrefix = "test";
        Consumer<Scope<String>> mockBlock = mock(Consumer.class);

        // 调用 forEach 方法
        TestChainCore result = iterableTerm.forEach(null, pathPrefix, mockBlock);

        // 验证结果
        assertNotNull(result);
    }
}
