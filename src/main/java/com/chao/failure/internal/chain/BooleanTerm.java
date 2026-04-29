package com.chao.failure.internal.chain;

import com.chao.failure.constant.FailureConst;
import com.chao.failure.internal.chain.pipeline.ChainCore;
import com.chao.failure.internal.check.BooleanChecks;
import com.chao.failure.internal.core.ResponseCode;

/**
 * Boolean validation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.3.1
 */
public interface BooleanTerm<S extends ChainCore<S>> {

    /**
     * Get chain core.
     *
     * @return Chain core instance
     */
    S core();

    default S state(boolean condition) {
        return state(condition, FailureConst.STATE_ERROR, null);
    }

    default S state(boolean condition, ResponseCode code) {
        return state(condition, code, null);
    }

    default S state(boolean condition, ResponseCode code, String detail) {
        return core().check(BooleanChecks.state(condition), code, detail);
    }

    default S isTrue(boolean cond) {
        return isTrue(cond, FailureConst.IS_TRUE_ERROR, null);
    }

    default S isTrue(boolean cond, ResponseCode code) {
        return isTrue(cond, code, null);
    }

    default S isTrue(boolean cond, ResponseCode code, String detail) {
        return core().check(BooleanChecks.isTrue(cond), code, detail);
    }

    default S isFalse(boolean cond) {
        return isFalse(cond, FailureConst.IS_FALSE_ERROR, null);
    }

    default S isFalse(boolean cond, ResponseCode code) {
        return isFalse(cond, code, null);
    }

    default S isFalse(boolean cond, ResponseCode code, String detail) {
        return core().check(BooleanChecks.isFalse(cond), code, detail);
    }

}
