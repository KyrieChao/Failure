package com.chao.failure.internal.chain;

import com.chao.failure.constant.FailureConst;
import com.chao.failure.internal.chain.pipeline.ChainCore;
import com.chao.failure.internal.core.ResponseCode;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Custom condition validation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.3.1
 */
public interface CustomTerm<S extends ChainCore<S>> {

    /**
     * Get chain core.
     *
     * @return Chain core instance
     */
    S core();

    // ========== satisfies ==========

    default <T> S satisfies(T value, Predicate<T> condition) {
        return satisfies(value, condition, FailureConst.SATISFIES_ERROR, null);
    }

    default <T> S satisfies(T value, Predicate<T> condition, ResponseCode code) {
        return satisfies(value, condition, code, null);
    }

    default <T> S satisfies(T value, Predicate<T> condition, ResponseCode code, String detail) {
        // Check value for null to avoid NPE
        return core().check(value != null && condition.test(value), code, detail);
    }

    // ========== compare ==========

    default <T> S compare(T field1, T field2, Comparator<T> comparator) {
        return compare(field1, field2, comparator, FailureConst.COMPARE_ERROR, null);
    }

    default <T> S compare(T field1, T field2, Comparator<T> comparator, ResponseCode code) {
        return compare(field1, field2, comparator, code, null);
    }

    default <T> S compare(T field1, T field2, Comparator<T> comparator, ResponseCode code, String detail) {
        return core().check(comparator.compare(field1, field2) == 0, code, detail);
    }

    // ========== defer ==========
    default S defer(Supplier<Boolean> condition) {
        return defer(condition, FailureConst.DEFER_ERROR, null);
    }

    default S defer(Supplier<Boolean> condition, ResponseCode code) {
        return defer(condition, code, null);
    }

    default S defer(Supplier<Boolean> condition, ResponseCode code, String detail) {
        return core().check(condition, code, detail);
    }
}
