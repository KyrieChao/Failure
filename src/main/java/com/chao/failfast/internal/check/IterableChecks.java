package com.chao.failfast.internal.check;

import com.chao.failfast.internal.chain.pipeline.ChainCore;
import com.chao.failfast.internal.chain.pipeline.Scope;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Utility class for iterable validation.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
public final class IterableChecks {

    private IterableChecks() {}

    /**
     * ForEach validation with path support.
     *
     * @param core       Chain core instance
     * @param items      Iterable of items to validate
     * @param pathPrefix Path prefix
     * @param block      Validation block
     * @param <S>        Chain core type
     * @param <T>        Item type
     * @return Current chain instance
     */
    public static <S extends ChainCore<S>, T> S forEach(S core, Iterable<T> items, String pathPrefix, Consumer<Scope<T>> block) {
        if (core.shouldSkip()) return core;

        // 生产保护：items 为 null 或空时直接返回
        if (items == null) return core;

        int index = 0;
        int maxItems = 1000; // 最大元素数限制
        int maxErrors = 100; // 最大错误数限制
        int errorCount = 0;
        int previousErrorCount = core.errorSize();

        Iterator<T> iterator = items.iterator();
        while (iterator.hasNext() && !core.shouldSkip() && index < maxItems && errorCount < maxErrors) {
            T item = iterator.next();
            String path = (pathPrefix == null || pathPrefix.isEmpty()) ? "[" + index + "]" : pathPrefix + "[" + index + "]";
            Scope<T> scope = new Scope<>(core, item, path);
            block.accept(scope);
            index++;

            // 检查新增错误数
            int currentErrorCount = core.errorSize();
            errorCount += currentErrorCount - previousErrorCount;
            previousErrorCount = currentErrorCount;
        }
        return core;
    }

    /**
     * ForEach validation with default path prefix.
     *
     * @param core  Chain core instance
     * @param items Iterable of items to validate
     * @param block Validation block
     * @param <S>   Chain core type
     * @param <T>   Item type
     * @return Current chain instance
     */
    public static <S extends ChainCore<S>, T> S forEach(S core, Iterable<T> items, Consumer<Scope<T>> block) {
        return forEach(core, items, "", block);
    }
}

