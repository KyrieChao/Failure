package com.chao.failfast.internal.chain;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.internal.core.ViolationSpec;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 自定义条件校验接口
 */
public interface CustomTerm<S extends ChainCore<S>> {

    S core();

    // ========== satisfies ==========

    default <T> S satisfies(T value, Predicate<T> condition, Consumer<ViolationSpec> spec) {
        return core().check(value != null && condition.test(value), spec);
    }

    default <T> S satisfies(T value, Predicate<T> condition) {
        return satisfies(value, condition, FailureConst.NO_OP);
    }

    default <T> S satisfies(T value, Predicate<T> condition, ResponseCode code) {
        return satisfies(value, condition, s -> s.responseCode(code));
    }

    default <T> S satisfies(T value, Predicate<T> condition, ResponseCode code, String detail) {
        return satisfies(value, condition, s -> s.responseCode(code).detail(detail));
    }

    // ========== compare ==========

    default <T> S compare(T field1, T field2, Comparator<T> comparator, Consumer<ViolationSpec> spec) {
        return core().check(comparator.compare(field1, field2) == 0, spec);
    }

    default <T> S compare(T field1, T field2, Comparator<T> comparator) {
        return compare(field1, field2, comparator, FailureConst.NO_OP);
    }

    default <T> S compare(T field1, T field2, Comparator<T> comparator, ResponseCode code) {
        return compare(field1, field2, comparator, s -> s.responseCode(code));
    }

    default <T> S compare(T field1, T field2, Comparator<T> comparator, ResponseCode code, String detail) {
        return compare(field1, field2, comparator, s -> s.responseCode(code).detail(detail));
    }

    /**
     * 延迟校验：当且仅当需要执行时（未被跳过、未 FailFast），才计算条件
     * 适用于开销较大的校验逻辑
     */
    default S defer(Supplier<Boolean> condition) {
        return defer(condition, FailureConst.NO_OP);
    }

    default S defer(Supplier<Boolean> condition, ResponseCode code) {
        return defer(condition, s -> s.responseCode(code));
    }

    default S defer(Supplier<Boolean> condition, ResponseCode code, String detail) {
        return defer(condition, s -> s.responseCode(code).detail(detail));
    }

    default S defer(Supplier<Boolean> condition, Consumer<ViolationSpec> spec) {
        return core().check(condition, spec);
    }
}