package com.chao.failfast.internal.chain;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.check.IdentityChecks;
import com.chao.failfast.internal.core.ResponseCode;

/**
 * Identity validation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.0.0
 */
public interface IdentityTerm<S extends ChainCore<S>> {

    /**
     * Get chain core.
     *
     * @return Chain core instance
     */
    S core();

    default S same(Object obj1, Object obj2) {
        return core().check(IdentityChecks.same(obj1, obj2), FailureConst.SAME_ERROR, null);
    }

    default S same(Object obj1, Object obj2, ResponseCode code) {
        return core().check(IdentityChecks.same(obj1, obj2), code, null);
    }

    default S same(Object obj1, Object obj2, ResponseCode code, String detail) {
        return core().check(IdentityChecks.same(obj1, obj2), code, detail);
    }

    default S notSame(Object obj1, Object obj2) {
        return core().check(IdentityChecks.notSame(obj1, obj2), FailureConst.NOT_SAME_ERROR, null);
    }

    default S notSame(Object obj1, Object obj2, ResponseCode code) {
        return core().check(IdentityChecks.notSame(obj1, obj2), code, null);
    }

    default S notSame(Object obj1, Object obj2, ResponseCode code, String detail) {
        return core().check(IdentityChecks.notSame(obj1, obj2), code, detail);
    }

    default S equals(Object obj1, Object obj2) {
        return core().check(IdentityChecks.equals(obj1, obj2), FailureConst.EQUALS_ERROR, null);
    }

    default S equals(Object obj1, Object obj2, ResponseCode code) {
        return core().check(IdentityChecks.equals(obj1, obj2), code, null);
    }

    default S equals(Object obj1, Object obj2, ResponseCode code, String detail) {
        return core().check(IdentityChecks.equals(obj1, obj2), code, detail);
    }

    default S notEquals(Object obj1, Object obj2) {
        return core().check(IdentityChecks.notEquals(obj1, obj2), FailureConst.NOT_EQUALS_ERROR, null);
    }

    default S notEquals(Object obj1, Object obj2, ResponseCode code) {
        return core().check(IdentityChecks.notEquals(obj1, obj2), code, null);
    }

    default S notEquals(Object obj1, Object obj2, ResponseCode code, String detail) {
        return core().check(IdentityChecks.notEquals(obj1, obj2), code, detail);
    }

}
