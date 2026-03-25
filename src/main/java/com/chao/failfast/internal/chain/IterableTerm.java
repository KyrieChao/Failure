package com.chao.failfast.internal.chain;

import com.chao.failfast.internal.chain.pipeline.ChainCore;
import com.chao.failfast.internal.chain.pipeline.Scope;
import com.chao.failfast.internal.check.IterableChecks;
import java.util.function.Consumer;

/**
 * Iterable validation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.2.0
 */
public interface IterableTerm<S extends ChainCore<S>> {

    /**
     * Get chain core.
     *
     * @return Chain core instance
     */
    S core();

    /**
     * ForEach validation with path support.
     *
     * @param items Iterable of items to validate
     * @param block Validation block
     * @param <T>   Item type
     * @return Current chain instance
     */
    default <T> S forEach(Iterable<T> items, Consumer<Scope<T>> block) {
        return forEach(items, "", block);
    }

    /**
     * ForEach validation with custom path prefix.
     *
     * @param items      Iterable of items to validate
     * @param pathPrefix Path prefix
     * @param block      Validation block
     * @param <T>        Item type
     * @return Current chain instance
     */
    default <T> S forEach(Iterable<T> items, String pathPrefix, Consumer<Scope<T>> block) {
        return IterableChecks.forEach(core(), items, pathPrefix, block);
    }
}
