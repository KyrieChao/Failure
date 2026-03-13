package com.chao.failfast.internal.chain;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.check.NumberChecks;
import com.chao.failfast.internal.core.ResponseCode;

/**
 * Number validation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.0.0
 */
public interface NumberTerm<S extends ChainCore<S>> {

    /**
     * Get chain core.
     *
     * @return Chain core instance
     */
    S core();

    default S positive(Number value) {
        return positive(value, FailureConst.POSITIVE_ERROR, null);
    }

    default S positive(Number value, ResponseCode code) {
        return positive(value, code, null);
    }

    default S positive(Number value, ResponseCode code, String detail) {
        return core().check(NumberChecks.positive(value), code, detail);
    }

    default <T extends Number & Comparable<T>> S inRange(T value, T min, T max) {
        return inRange(value, min, max, FailureConst.IN_RANGE_ERROR, null);
    }

    default <T extends Number & Comparable<T>> S inRange(T value, T min, T max, ResponseCode code) {
        return inRange(value, min, max, code, null);
    }

    default <T extends Number & Comparable<T>> S inRange(T value, T min, T max, ResponseCode code, String detail) {
        return core().check(NumberChecks.inRange(value, min, max), code, detail);
    }

    default S inRangeNumber(Number v, Number min, Number max) {
        return inRangeNumber(v, min, max, FailureConst.IN_RANGE_NUMBER_ERROR, null);
    }

    default S inRangeNumber(Number v, Number min, Number max, ResponseCode code) {
        return inRangeNumber(v, min, max, code, null);
    }

    default S inRangeNumber(Number v, Number min, Number max, ResponseCode code, String detail) {
        return core().check(NumberChecks.inRangeNumber(v, min, max), code, detail);
    }

    default S nonNegative(Number value) {
        return nonNegative(value, FailureConst.NON_NEGATIVE_ERROR, null);
    }

    default S nonNegative(Number value, ResponseCode code) {
        return nonNegative(value, code, null);
    }

    default S nonNegative(Number value, ResponseCode code, String detail) {
        return core().check(NumberChecks.nonNegative(value), code, detail);
    }

    default <T extends Number & Comparable<T>> S greaterThan(T value, T threshold) {
        return greaterThan(value, threshold, FailureConst.GREATER_THAN_ERROR, null);
    }

    default <T extends Number & Comparable<T>> S greaterThan(T value, T threshold, ResponseCode code) {
        return greaterThan(value, threshold, code, null);
    }

    default <T extends Number & Comparable<T>> S greaterThan(T value, T threshold, ResponseCode code, String detail) {
        return core().check(NumberChecks.greaterThan(value, threshold), code, detail);
    }

    default <T extends Number & Comparable<T>> S greaterOrEqual(T value, T threshold) {
        return greaterOrEqual(value, threshold, FailureConst.GREATER_OR_EQUAL_ERROR, null);
    }

    default <T extends Number & Comparable<T>> S greaterOrEqual(T value, T threshold, ResponseCode code) {
        return greaterOrEqual(value, threshold, code, null);
    }

    default <T extends Number & Comparable<T>> S greaterOrEqual(T value, T threshold, ResponseCode code, String detail) {
        return core().check(NumberChecks.greaterOrEqual(value, threshold), code, detail);
    }

    default <T extends Number & Comparable<T>> S lessThan(T value, T threshold) {
        return lessThan(value, threshold, FailureConst.LESS_THAN_ERROR, null);
    }

    default <T extends Number & Comparable<T>> S lessThan(T value, T threshold, ResponseCode code) {
        return lessThan(value, threshold, code, null);
    }

    default <T extends Number & Comparable<T>> S lessThan(T value, T threshold, ResponseCode code, String detail) {
        return core().check(NumberChecks.lessThan(value, threshold), code, detail);
    }

    default <T extends Number & Comparable<T>> S lessOrEqual(T value, T threshold) {
        return lessOrEqual(value, threshold, FailureConst.LESS_OR_EQUAL_ERROR, null);
    }

    default <T extends Number & Comparable<T>> S lessOrEqual(T value, T threshold, ResponseCode code) {
        return lessOrEqual(value, threshold, code, null);
    }

    default <T extends Number & Comparable<T>> S lessOrEqual(T value, T threshold, ResponseCode code, String detail) {
        return core().check(NumberChecks.lessOrEqual(value, threshold), code, detail);
    }

    default S notZero(Number value) {
        return notZero(value, FailureConst.NOT_ZERO_ERROR, null);
    }

    default S notZero(Number value, ResponseCode code) {
        return notZero(value, code, null);
    }

    default S notZero(Number value, ResponseCode code, String detail) {
        return core().check(NumberChecks.notZero(value), code, detail);
    }

    default S isZero(Number value) {
        return isZero(value, FailureConst.IS_ZERO_ERROR, null);
    }

    default S isZero(Number value, ResponseCode code) {
        return isZero(value, code, null);
    }

    default S isZero(Number value, ResponseCode code, String detail) {
        return core().check(NumberChecks.isZero(value), code, detail);
    }

    default S negative(Number value) {
        return negative(value, FailureConst.NEGATIVE_ERROR, null);
    }

    default S negative(Number value, ResponseCode code) {
        return negative(value, code, null);
    }

    default S negative(Number value, ResponseCode code, String detail) {
        return core().check(NumberChecks.negative(value), code, detail);
    }

    default S multipleOf(Number value, Number divisor) {
        return multipleOf(value, divisor, FailureConst.MULTIPLE_OF_ERROR, null);
    }

    default S multipleOf(Number value, Number divisor, ResponseCode code) {
        return multipleOf(value, divisor, code, null);
    }

    default S multipleOf(Number value, Number divisor, ResponseCode code, String detail) {
        return core().check(NumberChecks.multipleOf(value, divisor), code, detail);
    }

    default S decimalScale(java.math.BigDecimal value, int scale) {
        return decimalScale(value, scale, FailureConst.DECIMAL_SCALE_ERROR, null);
    }

    default S decimalScale(java.math.BigDecimal value, int scale, ResponseCode code) {
        return decimalScale(value, scale, code, null);
    }

    default S decimalScale(java.math.BigDecimal value, int scale, ResponseCode code, String detail) {
        return core().check(NumberChecks.decimalScale(value, scale), code, detail);
    }

}
