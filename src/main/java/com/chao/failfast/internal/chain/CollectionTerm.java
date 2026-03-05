package com.chao.failfast.internal.chain;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.check.CollectionChecks;
import com.chao.failfast.internal.core.ResponseCode;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public interface CollectionTerm<S extends ChainCore<S>> {

    S core();

    default S notEmpty(Collection<?> col) {
        return core().check(CollectionChecks.notEmpty(col), FailureConst.NOT_EMPTY_ERROR, null);
    }

    default S notEmpty(Collection<?> col, ResponseCode code) {
        return core().check(CollectionChecks.notEmpty(col), code, null);
    }

    default S notEmpty(Collection<?> col, ResponseCode code, String detail) {
        return core().check(CollectionChecks.notEmpty(col), code, detail);
    }

    default S isEmpty(Collection<?> col) {
        return core().check(CollectionChecks.isEmpty(col), FailureConst.IS_EMPTY_ERROR, null);
    }

    default S isEmpty(Collection<?> col, ResponseCode code) {
        return core().check(CollectionChecks.isEmpty(col), code, null);
    }

    default S isEmpty(Collection<?> col, ResponseCode code, String detail) {
        return core().check(CollectionChecks.isEmpty(col), code, detail);
    }

    default S sizeBetween(Collection<?> col, int min, int max) {
        return core().check(CollectionChecks.sizeBetween(col, min, max), FailureConst.SIZE_BETWEEN_ERROR, null);
    }

    default S sizeBetween(Collection<?> col, int min, int max, ResponseCode code) {
        return core().check(CollectionChecks.sizeBetween(col, min, max), code, null);
    }

    default S sizeBetween(Collection<?> col, int min, int max, ResponseCode code, String detail) {
        return core().check(CollectionChecks.sizeBetween(col, min, max), code, detail);
    }

    default S sizeEquals(Collection<?> col, int expectedSize) {
        return core().check(CollectionChecks.sizeEquals(col, expectedSize), FailureConst.SIZE_EQUALS_ERROR, null);
    }

    default S sizeEquals(Collection<?> col, int expectedSize, ResponseCode code) {
        return core().check(CollectionChecks.sizeEquals(col, expectedSize), code, null);
    }

    default S sizeEquals(Collection<?> col, int expectedSize, ResponseCode code, String detail) {
        return core().check(CollectionChecks.sizeEquals(col, expectedSize), code, detail);
    }

    default S contains(Collection<?> col, Object o) {
        return core().check(CollectionChecks.contains(col, o), FailureConst.CONTAINS_ERROR, null);
    }

    default S contains(Collection<?> col, Object o, ResponseCode code) {
        return core().check(CollectionChecks.contains(col, o), code, null);
    }

    default S contains(Collection<?> col, Object o, ResponseCode code, String detail) {
        return core().check(CollectionChecks.contains(col, o), code, detail);
    }

    default S notContains(Collection<?> col, Object o) {
        return core().check(CollectionChecks.notContains(col, o), FailureConst.NOT_CONTAINS_ERROR, null);
    }

    default S notContains(Collection<?> col, Object o, ResponseCode code) {
        return core().check(CollectionChecks.notContains(col, o), code, null);
    }

    default S notContains(Collection<?> col, Object o, ResponseCode code, String detail) {
        return core().check(CollectionChecks.notContains(col, o), code, detail);
    }

    default S hasNoNullElements(Collection<?> col) {
        return core().check(CollectionChecks.hasNoNullElements(col), FailureConst.HAS_NO_NULL_ELEMENTS_ERROR, null);
    }

    default S hasNoNullElements(Collection<?> col, ResponseCode code) {
        return core().check(CollectionChecks.hasNoNullElements(col), code, null);
    }

    default S hasNoNullElements(Collection<?> col, ResponseCode code, String detail) {
        return core().check(CollectionChecks.hasNoNullElements(col), code, detail);
    }

    default <T> S allMatch(Collection<T> col, Predicate<T> predicate) {
        return core().check(CollectionChecks.allMatch(col, predicate), FailureConst.ALL_MATCH_ERROR, null);
    }

    default <T> S allMatch(Collection<T> col, Predicate<T> predicate, ResponseCode code) {
        return core().check(CollectionChecks.allMatch(col, predicate), code, null);
    }

    default <T> S allMatch(Collection<T> col, Predicate<T> predicate, ResponseCode code, String detail) {
        return core().check(CollectionChecks.allMatch(col, predicate), code, detail);
    }

    default <T> S anyMatch(Collection<T> col, Predicate<T> predicate) {
        return core().check(CollectionChecks.anyMatch(col, predicate), FailureConst.ANY_MATCH_ERROR, null);
    }

    default <T> S anyMatch(Collection<T> col, Predicate<T> predicate, ResponseCode code) {
        return core().check(CollectionChecks.anyMatch(col, predicate), code, null);
    }

    default <T> S anyMatch(Collection<T> col, Predicate<T> predicate, ResponseCode code, String detail) {
        return core().check(CollectionChecks.anyMatch(col, predicate), code, detail);
    }

}