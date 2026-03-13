package com.chao.failfast.internal.chain;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.check.ObjectChecks;
import com.chao.failfast.internal.core.ResponseCode;

/**
 * Object validation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.0.0
 */
public interface ObjectTerm<S extends ChainCore<S>> {

    /**
     * Get chain core.
     *
     * @return Chain core instance
     */
    S core();

    // Alias: notNull -> exists
    default S notNull(Object obj) {
        return exists(obj, FailureConst.NOT_NULL_ERROR);
    }
    default S notNull(Object obj, ResponseCode code) {
        return exists(obj, code);
    }
    default S notNull(Object obj, ResponseCode code, String detail) {
        return exists(obj, code, detail);
    }

    default S exists(Object obj) {
        return exists(obj, FailureConst.EXISTS_ERROR, null);
    }

    default S exists(Object obj, ResponseCode code) {
        return exists(obj, code, null);
    }

    default S exists(Object obj, ResponseCode code, String detail) {
        return core().check(ObjectChecks.exists(obj), code, detail);
    }

    default S isNull(Object obj) {
        return isNull(obj, FailureConst.IS_NULL_ERROR, null);
    }

    default S isNull(Object obj, ResponseCode code) {
        return isNull(obj, code, null);
    }

    default S isNull(Object obj, ResponseCode code, String detail) {
        return core().check(ObjectChecks.isNull(obj), code, detail);
    }

    default S instanceOf(Object obj, Class<?> type) {
        return instanceOf(obj, type, FailureConst.INSTANCE_OF_ERROR, null);
    }

    default S instanceOf(Object obj, Class<?> type, ResponseCode code) {
        return instanceOf(obj, type, code, null);
    }

    default S instanceOf(Object obj, Class<?> type, ResponseCode code, String detail) {
        return core().check(ObjectChecks.instanceOf(obj, type), code, detail);
    }

    default S notInstanceOf(Object obj, Class<?> type) {
        return notInstanceOf(obj, type, FailureConst.NOT_INSTANCE_OF_ERROR, null);
    }

    default S notInstanceOf(Object obj, Class<?> type, ResponseCode code) {
        return notInstanceOf(obj, type, code, null);
    }

    default S notInstanceOf(Object obj, Class<?> type, ResponseCode code, String detail) {
        return core().check(ObjectChecks.notInstanceOf(obj, type), code, detail);
    }

    default S allNotNull(Object... objs) {
        return allNotNull(FailureConst.ALL_NOT_NULL_ERROR, null, objs);
    }

    default S allNotNull(ResponseCode code, Object... objs) {
        return allNotNull(code, null, objs);
    }

    default S allNotNull(ResponseCode code, String detail, Object... objs) {
        return core().check(ObjectChecks.allNotNull(objs), code, detail);
    }

}
