package com.chao.failfast.internal.chain;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.check.OptionalChecks;
import com.chao.failfast.internal.core.ResponseCode;
import java.util.Optional;

public interface OptionalTerm<S extends ChainCore<S>> {

    S core();

    default S isPresent(Optional<?> opt) {
        return core().check(OptionalChecks.isPresent(opt), FailureConst.IS_PRESENT_ERROR, null);
    }

    default S isPresent(Optional<?> opt, ResponseCode code) {
        return core().check(OptionalChecks.isPresent(opt), code, null);
    }

    default S isPresent(Optional<?> opt, ResponseCode code, String detail) {
        return core().check(OptionalChecks.isPresent(opt), code, detail);
    }

    default S isEmpty(Optional<?> opt) {
        return core().check(OptionalChecks.isEmpty(opt), FailureConst.IS_EMPTY_ERROR, null);
    }

    default S isEmpty(Optional<?> opt, ResponseCode code) {
        return core().check(OptionalChecks.isEmpty(opt), code, null);
    }

    default S isEmpty(Optional<?> opt, ResponseCode code, String detail) {
        return core().check(OptionalChecks.isEmpty(opt), code, detail);
    }

}