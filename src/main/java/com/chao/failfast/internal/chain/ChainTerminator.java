package com.chao.failfast.internal.chain;

import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.MultiBusiness;
import com.chao.failfast.internal.core.ResponseCode;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Terminal operation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.0.0
 */
public interface ChainTerminator<S extends ChainCore<S>> {


    /**
     * Get core instance.
     *
     * @return Core instance
     */
    S core();


    /**
     * Default failure handling method.
     *
     * @throws Business Throws specific business exception on validation failure
     */
    default void fail() {
        if (!core().isValid()) {
            if (core().getCauses().isEmpty()) {
                throw Business.of(ResponseCode.VALIDATION_ERROR_500);
            }
            throw core().getCauses().get(0);
        }
    }

    /**
     * Default method for handling validation failure.
     *
     * @throws Business Throws MultiBusiness exception if multiple errors exist
     */
    default void failAll() {
        if (!core().isValid()) {
            if (core().getCauses().isEmpty()) {
                throw Business.of(ResponseCode.VALIDATION_ERROR_500);
            }
            if (core().getCauses().size() == 1) throw core().getCauses().get(0);
            throw new MultiBusiness(core().getCauses());
        }
    }


    /**
     * Default method to throw business exception when core is not alive.
     *
     * @param code Response status code
     * @return Returns core object if alive
     * @throws Business Throws business exception when core is not alive
     */
    default S failNow(ResponseCode code) {
        if (core().isAlive()) return core();
        throw Business.of(code);
    }

    /**
     * Default method: return core component if alive, otherwise throw business exception.
     *
     * @param code   Response status code
     * @param detail Error detailed info
     * @return Returns core component instance if alive
     * @throws Business Throws business exception when core is not alive
     */
    default S failNow(ResponseCode code, String detail) {
        if (core().isAlive()) return core();
        throw Business.of(code, detail);
    }

    /**
     * Formatted message version.
     *
     * @param code      Response code
     * @param msgFormat Message format string
     * @param args      Format arguments
     * @return Current chain instance
     */
    default S failNow(ResponseCode code, String msgFormat, Object... args) {
        if (core().isAlive()) return core();
        throw Business.of(code, String.format(msgFormat, args));
    }


    /**
     * Execute specified action when core object is not alive.
     *
     * @param action Action to execute when core object is not alive
     * @return Returns core object itself, supporting chain call
     */
    default S onFail(Runnable action) {
        if (!core().isAlive()) action.run();
        return core();
    }

    /**
     * Get value from supplier and wrap as Optional when core is not alive.
     *
     * @param <T>      Return value type
     * @param supplier Supplier to provide value
     * @return Optional containing supplier value if core not alive, otherwise empty Optional
     */
    default <T> Optional<T> onFailGet(Supplier<T> supplier) {
        return !core().isAlive() ? Optional.ofNullable(supplier.get()) : Optional.empty();
    }


    /**
     * Default validation method implementation.
     */
    default void verify() {
        // No-op: Errors are reported to context immediately
    }
}
