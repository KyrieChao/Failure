package com.chao.failure.internal.check;

import com.chao.failure.internal.chain.pipeline.ChainCore;
import com.chao.failure.internal.chain.pipeline.Scope;
import com.chao.failure.exception.Business;
import com.chao.failure.spi.validation.CancelToken;
import com.chao.failure.spi.validation.ProgressListener;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Utility class for iterable validation.
 *
 * @author Kyrie Chao
 * @version 1.3.0
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
        return forEach(core, items, pathPrefix, block, null, null);
    }

    /**
     * ForEach validation with progress listener and cancel token.
     */
    public static <S extends ChainCore<S>, T> S forEach(S core, Iterable<T> items, String pathPrefix, Consumer<Scope<T>> block,
                                                        ProgressListener listener, CancelToken cancelToken) {
        if (core.shouldSkip()) return core;

        // Production protection: return directly if items is null or empty
        if (items == null) {
            notifyStart(listener, 0);
            notifyCompleted(listener, 0, core);
            return core;
        }
        long total = resolveTotal(items);
        notifyStart(listener, total);

        int index = 0;
        int maxItems = 1000; // Maximum element count limit
        int maxErrors = 100; // Maximum error count limit
        int errorCount = 0;
        int previousErrorCount = core.errorSize();

        Iterator<T> iterator = items.iterator();
        while (iterator.hasNext() && !core.shouldSkip() && index < maxItems && errorCount < maxErrors) {
            if (cancelToken != null && cancelToken.isCancelled()) {
                if (listener != null) listener.onCancelled();
                break;
            }
            T item = iterator.next();
            String path = (pathPrefix == null || pathPrefix.isEmpty()) ? "[" + index + "]" : pathPrefix + "[" + index + "]";
            Scope<T> scope = new Scope<>(core, item, path);
            block.accept(scope);
            index++;

            // Check for new errors
            int currentErrorCount = core.errorSize();
            errorCount += currentErrorCount - previousErrorCount;
            previousErrorCount = currentErrorCount;
            Business latestError = currentErrorCount > 0 ? core.latestCause() : null;
            if (listener != null) {
                listener.onProgress(index, total, latestError);
            }
        }
        notifyCompleted(listener, total >= 0 ? total : index, core);
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

    private static <S extends ChainCore<S>> void notifyCompleted(ProgressListener listener, long total, S core) {
        if (listener != null) {
            listener.onCompleted(total, core.getCauses());
        }
    }

    private static void notifyStart(ProgressListener listener, long total) {
        if (listener != null) {
            listener.onStarted(total);
        }
    }

    private static long resolveTotal(Iterable<?> items) {
        if (items instanceof Collection<?> collection) {
            return collection.size();
        }
        return -1;
    }
}

