package com.chao.failure.reactive;



import com.chao.failure.exception.Business;
import com.chao.failure.internal.chain.pipeline.Scope;
import com.chao.failure.internal.core.Chain;
import com.chao.failure.spi.validation.CancelToken;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.function.Consumer;
/**
 * Strict processor for reactive validation streams.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public class StrictProcessor {

    /**
     * Generic method to create a strict Flux stream.
     * @param <T> Generic type parameter representing input item type
     * @param items Iterable containing elements to process
     * @param block Consumer function with Scope<T> parameter for processing logic
     * @return Flux<Business> stream representing business object flow
     */
    public static <T> Flux<Business> strictStream(Iterable<T> items, Consumer<Scope<T>> block) {
        return strictStream(items, "", block);
    }

    /**
     * Generic method to create a strict Flux stream with path prefix.
     * @param <T> Generic type parameter representing input item type
     * @param items Iterable containing elements to process
     * @param pathPrefix Path prefix for validation context
     * @param block Consumer function with Scope<T> parameter for processing logic
     * @return Flux<Business> stream representing business object flow
     */
    public static <T> Flux<Business> strictStream(Iterable<T> items, String pathPrefix, Consumer<Scope<T>> block) {
        return Flux.create(sink -> {
            Chain chain = Chain.begin(false);
            CancelToken token = new CancelToken();
            sink.onCancel(token::cancel);
            chain.setErrorConsumer(error -> {
                if (!sink.isCancelled()) {
                    sink.next(error);
                }
            });
            try {
                chain.forEach(items, pathPrefix, block, null, token);
            } catch (Throwable t) {
                sink.error(t);
                return;
            } finally {
                chain.setErrorConsumer(null);
            }
            if (!sink.isCancelled()) {
                sink.complete();
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }
}
