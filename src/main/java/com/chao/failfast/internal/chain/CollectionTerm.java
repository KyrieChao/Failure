package com.chao.failfast.internal.chain;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.chain.pipeline.ChainCore;
import com.chao.failfast.internal.check.CollectionChecks;
import com.chao.failfast.internal.core.ResponseCode;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Collection validation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.3.0
 */
public interface CollectionTerm<S extends ChainCore<S>> {

    /**
     * Get chain core.
     *
     * @return Chain core instance
     */
    S core();

    default S notEmpty(Collection<?> col) {
        return notEmpty(col, FailureConst.NOT_EMPTY_ERROR, null);
    }

    default S notEmpty(Collection<?> col, ResponseCode code) {
        return notEmpty(col, code, null);
    }

    default S notEmpty(Collection<?> col, ResponseCode code, String detail) {
        return core().check(CollectionChecks.notEmpty(col), code, detail);
    }

    default S isEmpty(Collection<?> col) {
        return isEmpty(col, FailureConst.IS_EMPTY_ERROR, null);
    }

    default S isEmpty(Collection<?> col, ResponseCode code) {
        return isEmpty(col, code, null);
    }

    default S isEmpty(Collection<?> col, ResponseCode code, String detail) {
        return core().check(CollectionChecks.isEmpty(col), code, detail);
    }

    default S sizeBetween(Collection<?> col, int min, int max) {
        return sizeBetween(col, min, max, FailureConst.SIZE_BETWEEN_ERROR, null);
    }

    default S sizeBetween(Collection<?> col, int min, int max, ResponseCode code) {
        return sizeBetween(col, min, max, code, null);
    }

    default S sizeBetween(Collection<?> col, int min, int max, ResponseCode code, String detail) {
        return core().check(CollectionChecks.sizeBetween(col, min, max), code, detail);
    }

    default S sizeEquals(Collection<?> col, int expectedSize) {
        return sizeEquals(col, expectedSize, FailureConst.SIZE_EQUALS_ERROR, null);
    }

    default S sizeEquals(Collection<?> col, int expectedSize, ResponseCode code) {
        return sizeEquals(col, expectedSize, code, null);
    }

    default S sizeEquals(Collection<?> col, int expectedSize, ResponseCode code, String detail) {
        return core().check(CollectionChecks.sizeEquals(col, expectedSize), code, detail);
    }

    default S contains(Collection<?> col, Object o) {
        return contains(col, o, FailureConst.CONTAINS_ERROR, null);
    }

    default S contains(Collection<?> col, Object o, ResponseCode code) {
        return contains(col, o, code, null);
    }

    default S contains(Collection<?> col, Object o, ResponseCode code, String detail) {
        return core().check(CollectionChecks.contains(col, o), code, detail);
    }

    default S notContains(Collection<?> col, Object o) {
        return notContains(col, o, FailureConst.NOT_CONTAINS_ERROR, null);
    }

    default S notContains(Collection<?> col, Object o, ResponseCode code) {
        return notContains(col, o, code, null);
    }

    default S notContains(Collection<?> col, Object o, ResponseCode code, String detail) {
        return core().check(CollectionChecks.notContains(col, o), code, detail);
    }

    default S containsAll(Collection<?> col, Collection<?> required) {
        return containsAll(col, required, FailureConst.CONTAINS_ALL_ERROR, null);
    }

    default S containsAll(Collection<?> col, Collection<?> required, ResponseCode code) {
        return containsAll(col, required, code, null);
    }

    default S containsAll(Collection<?> col, Collection<?> required, ResponseCode code, String detail) {
        return core().check(CollectionChecks.containsAll(col, required), code, detail);
    }

    default <T> S noneMatch(Collection<T> col, Predicate<T> predicate) {
        return noneMatch(col, predicate, FailureConst.NONE_MATCH_ERROR, null);
    }

    default <T> S noneMatch(Collection<T> col, Predicate<T> predicate, ResponseCode code) {
        return noneMatch(col, predicate, code, null);
    }

    default <T> S noneMatch(Collection<T> col, Predicate<T> predicate, ResponseCode code, String detail) {
        return core().check(CollectionChecks.noneMatch(col, predicate), code, detail);
    }

    default S uniqueElements(Collection<?> col) {
        return uniqueElements(col, FailureConst.UNIQUE_ELEMENTS_ERROR, null);
    }

    default S uniqueElements(Collection<?> col, ResponseCode code) {
        return uniqueElements(col, code, null);
    }

    default S uniqueElements(Collection<?> col, ResponseCode code, String detail) {
        return core().check(CollectionChecks.uniqueElements(col), code, detail);
    }

    default S hasNoNullElements(Collection<?> col) {
        return hasNoNullElements(col, FailureConst.HAS_NO_NULL_ELEMENTS_ERROR, null);
    }

    default S hasNoNullElements(Collection<?> col, ResponseCode code) {
        return hasNoNullElements(col, code, null);
    }

    default S hasNoNullElements(Collection<?> col, ResponseCode code, String detail) {
        return core().check(CollectionChecks.hasNoNullElements(col), code, detail);
    }

    default <T> S allMatch(Collection<T> col, Predicate<T> predicate) {
        return allMatch(col, predicate, FailureConst.ALL_MATCH_ERROR, null);
    }

    default <T> S allMatch(Collection<T> col, Predicate<T> predicate, ResponseCode code) {
        return allMatch(col, predicate, code, null);
    }

    default <T> S allMatch(Collection<T> col, Predicate<T> predicate, ResponseCode code, String detail) {
        return core().check(CollectionChecks.allMatch(col, predicate), code, detail);
    }

    default <T> S anyMatch(Collection<T> col, Predicate<T> predicate) {
        return anyMatch(col, predicate, FailureConst.ANY_MATCH_ERROR, null);
    }

    default <T> S anyMatch(Collection<T> col, Predicate<T> predicate, ResponseCode code) {
        return anyMatch(col, predicate, code, null);
    }

    default <T> S anyMatch(Collection<T> col, Predicate<T> predicate, ResponseCode code, String detail) {
        return core().check(CollectionChecks.anyMatch(col, predicate), code, detail);
    }

}
