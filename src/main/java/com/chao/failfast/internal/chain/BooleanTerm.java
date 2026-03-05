package com.chao.failfast.internal.chain;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.check.BooleanChecks;
import com.chao.failfast.internal.core.ResponseCode;

public interface BooleanTerm<S extends ChainCore<S>> {

    S core();

    default S state(boolean condition) {
        return core().check(BooleanChecks.state(condition), FailureConst.STATE_ERROR, null);
    }

    default S state(boolean condition, ResponseCode code) {
        return core().check(BooleanChecks.state(condition), code, null);
    }

    default S state(boolean condition, ResponseCode code, String detail) {
        return core().check(BooleanChecks.state(condition), code, detail);
    }

    default S isTrue(boolean cond) {
        return core().check(BooleanChecks.isTrue(cond), FailureConst.IS_TRUE_ERROR, null);
    }

    default S isTrue(boolean cond, ResponseCode code) {
        return core().check(BooleanChecks.isTrue(cond), code, null);
    }

    default S isTrue(boolean cond, ResponseCode code, String detail) {
        return core().check(BooleanChecks.isTrue(cond), code, detail);
    }

    default S isFalse(boolean cond) {
        return core().check(BooleanChecks.isFalse(cond), FailureConst.IS_FALSE_ERROR, null);
    }

    default S isFalse(boolean cond, ResponseCode code) {
        return core().check(BooleanChecks.isFalse(cond), code, null);
    }

    default S isFalse(boolean cond, ResponseCode code, String detail) {
        return core().check(BooleanChecks.isFalse(cond), code, detail);
    }

}