package com.chao.failfast.internal.chain;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.check.ArrayChecks;
import com.chao.failfast.internal.core.ResponseCode;

/**
 * Array validation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.0.0
 */
public interface ArrayTerm<S extends ChainCore<S>> {

    /**
     * Get chain core.
     *
     * @return Chain core instance
     */
    S core();

    default <T> S notEmpty(T[] array) {
        return core().check(ArrayChecks.notEmpty(array), FailureConst.NOT_EMPTY_ERROR, null);
    }

    default <T> S notEmpty(T[] array, ResponseCode code) {
        return core().check(ArrayChecks.notEmpty(array), code, null);
    }

    default <T> S notEmpty(T[] array, ResponseCode code, String detail) {
        return core().check(ArrayChecks.notEmpty(array), code, detail);
    }

    default <T> S isEmpty(T[] array) {
        return core().check(ArrayChecks.isEmpty(array), FailureConst.IS_EMPTY_ERROR, null);
    }

    default <T> S isEmpty(T[] array, ResponseCode code) {
        return core().check(ArrayChecks.isEmpty(array), code, null);
    }

    default <T> S isEmpty(T[] array, ResponseCode code, String detail) {
        return core().check(ArrayChecks.isEmpty(array), code, detail);
    }

    default <T> S sizeBetween(T[] array, int min, int max) {
        return core().check(ArrayChecks.sizeBetween(array, min, max), FailureConst.SIZE_BETWEEN_ERROR, null);
    }

    default <T> S sizeBetween(T[] array, int min, int max, ResponseCode code) {
        return core().check(ArrayChecks.sizeBetween(array, min, max), code, null);
    }

    default <T> S sizeBetween(T[] array, int min, int max, ResponseCode code, String detail) {
        return core().check(ArrayChecks.sizeBetween(array, min, max), code, detail);
    }

    default <T> S sizeEquals(T[] array, int expectedSize) {
        return core().check(ArrayChecks.sizeEquals(array, expectedSize), FailureConst.SIZE_EQUALS_ERROR, null);
    }

    default <T> S sizeEquals(T[] array, int expectedSize, ResponseCode code) {
        return core().check(ArrayChecks.sizeEquals(array, expectedSize), code, null);
    }

    default <T> S sizeEquals(T[] array, int expectedSize, ResponseCode code, String detail) {
        return core().check(ArrayChecks.sizeEquals(array, expectedSize), code, detail);
    }

    default <T> S contains(T[] array, T o) {
        return core().check(ArrayChecks.contains(array, o), FailureConst.CONTAINS_ERROR, null);
    }

    default <T> S contains(T[] array, T o, ResponseCode code) {
        return core().check(ArrayChecks.contains(array, o), code, null);
    }

    default <T> S contains(T[] array, T o, ResponseCode code, String detail) {
        return core().check(ArrayChecks.contains(array, o), code, detail);
    }

    default <T> S notContains(T[] array, T o) {
        return core().check(ArrayChecks.notContains(array, o), FailureConst.NOT_CONTAINS_ERROR, null);
    }

    default <T> S notContains(T[] array, T o, ResponseCode code) {
        return core().check(ArrayChecks.notContains(array, o), code, null);
    }

    default <T> S notContains(T[] array, T o, ResponseCode code, String detail) {
        return core().check(ArrayChecks.notContains(array, o), code, detail);
    }

    default <T> S hasNoNullElements(T[] array) {
        return core().check(ArrayChecks.hasNoNullElements(array), FailureConst.HAS_NO_NULL_ELEMENTS_ERROR, null);
    }

    default <T> S hasNoNullElements(T[] array, ResponseCode code) {
        return core().check(ArrayChecks.hasNoNullElements(array), code, null);
    }

    default <T> S hasNoNullElements(T[] array, ResponseCode code, String detail) {
        return core().check(ArrayChecks.hasNoNullElements(array), code, detail);
    }

    default <T> S allMatch(T[] array, java.util.function.Predicate<T> predicate) {
        return core().check(ArrayChecks.allMatch(array, predicate), FailureConst.ALL_MATCH_ERROR, null);
    }

    default <T> S allMatch(T[] array, java.util.function.Predicate<T> predicate, ResponseCode code) {
        return core().check(ArrayChecks.allMatch(array, predicate), code, null);
    }

    default <T> S allMatch(T[] array, java.util.function.Predicate<T> predicate, ResponseCode code, String detail) {
        return core().check(ArrayChecks.allMatch(array, predicate), code, detail);
    }

    default <T> S anyMatch(T[] array, java.util.function.Predicate<T> predicate) {
        return core().check(ArrayChecks.anyMatch(array, predicate), FailureConst.ANY_MATCH_ERROR, null);
    }

    default <T> S anyMatch(T[] array, java.util.function.Predicate<T> predicate, ResponseCode code) {
        return core().check(ArrayChecks.anyMatch(array, predicate), code, null);
    }

    default <T> S anyMatch(T[] array, java.util.function.Predicate<T> predicate, ResponseCode code, String detail) {
        return core().check(ArrayChecks.anyMatch(array, predicate), code, detail);
    }

}
