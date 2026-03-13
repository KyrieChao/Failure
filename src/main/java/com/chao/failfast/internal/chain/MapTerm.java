package com.chao.failfast.internal.chain;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.check.MapChecks;
import com.chao.failfast.internal.core.ResponseCode;

import java.util.Map;

/**
 * Map validation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.0.0
 */
public interface MapTerm<S extends ChainCore<S>> {

    /**
     * Get chain core.
     *
     * @return Chain core instance
     */
    S core();

    default S notEmpty(Map<?, ?> map) {
        return notEmpty(map, FailureConst.NOT_EMPTY_ERROR, null);
    }

    default S notEmpty(Map<?, ?> map, ResponseCode code) {
        return notEmpty(map, code, null);
    }

    default S notEmpty(Map<?, ?> map, ResponseCode code, String detail) {
        return core().check(MapChecks.notEmpty(map), code, detail);
    }

    default S isEmpty(Map<?, ?> map) {
        return isEmpty(map, FailureConst.IS_EMPTY_ERROR, null);
    }

    default S isEmpty(Map<?, ?> map, ResponseCode code) {
        return isEmpty(map, code, null);
    }

    default S isEmpty(Map<?, ?> map, ResponseCode code, String detail) {
        return core().check(MapChecks.isEmpty(map), code, detail);
    }

    default S containsKey(Map<?, ?> map, Object key) {
        return containsKey(map, key, FailureConst.CONTAINS_KEY_ERROR, null);
    }

    default S containsKey(Map<?, ?> map, Object key, ResponseCode code) {
        return containsKey(map, key, code, null);
    }

    default S containsKey(Map<?, ?> map, Object key, ResponseCode code, String detail) {
        return core().check(MapChecks.containsKey(map, key), code, detail);
    }

    default S notContainsKey(Map<?, ?> map, Object key) {
        return notContainsKey(map, key, FailureConst.NOT_CONTAINS_KEY_ERROR, null);
    }

    default S notContainsKey(Map<?, ?> map, Object key, ResponseCode code) {
        return notContainsKey(map, key, code, null);
    }

    default S notContainsKey(Map<?, ?> map, Object key, ResponseCode code, String detail) {
        return core().check(MapChecks.notContainsKey(map, key), code, detail);
    }

    default S containsValue(Map<?, ?> map, Object value) {
        return containsValue(map, value, FailureConst.CONTAINS_VALUE_ERROR, null);
    }

    default S containsValue(Map<?, ?> map, Object value, ResponseCode code) {
        return containsValue(map, value, code, null);
    }

    default S containsValue(Map<?, ?> map, Object value, ResponseCode code, String detail) {
        return core().check(MapChecks.containsValue(map, value), code, detail);
    }

    default S sizeBetween(Map<?, ?> map, int min, int max) {
        return sizeBetween(map, min, max, FailureConst.SIZE_BETWEEN_ERROR, null);
    }

    default S sizeBetween(Map<?, ?> map, int min, int max, ResponseCode code) {
        return sizeBetween(map, min, max, code, null);
    }

    default S sizeBetween(Map<?, ?> map, int min, int max, ResponseCode code, String detail) {
        return core().check(MapChecks.sizeBetween(map, min, max), code, detail);
    }

    default S sizeEquals(Map<?, ?> map, int size) {
        return sizeEquals(map, size, FailureConst.SIZE_EQUALS_ERROR, null);
    }

    default S sizeEquals(Map<?, ?> map, int size, ResponseCode code) {
        return sizeEquals(map, size, code, null);
    }

    default S sizeEquals(Map<?, ?> map, int size, ResponseCode code, String detail) {
        return core().check(MapChecks.sizeEquals(map, size), code, detail);
    }

}
