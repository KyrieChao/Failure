package com.chao.failfast.internal.chain;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.check.EnumChecks;
import com.chao.failfast.internal.core.ResponseCode;

public interface EnumTerm<S extends ChainCore<S>> {

    S core();

    default <E extends Enum<E>> S enumValue(Class<E> enumType, String value) {
        return core().check(EnumChecks.enumValue(enumType, value), FailureConst.ENUM_VALUE_ERROR, null);
    }

    default <E extends Enum<E>> S enumValue(Class<E> enumType, String value, ResponseCode code) {
        return core().check(EnumChecks.enumValue(enumType, value), code, null);
    }

    default <E extends Enum<E>> S enumValue(Class<E> enumType, String value, ResponseCode code, String detail) {
        return core().check(EnumChecks.enumValue(enumType, value), code, detail);
    }

    default <E extends Enum<E>> S enumConstant(E value, Class<E> type) {
        return core().check(EnumChecks.enumConstant(value, type), FailureConst.ENUM_CONSTANT_ERROR, null);
    }

    default <E extends Enum<E>> S enumConstant(E value, Class<E> type, ResponseCode code) {
        return core().check(EnumChecks.enumConstant(value, type), code, null);
    }

    default <E extends Enum<E>> S enumConstant(E value, Class<E> type, ResponseCode code, String detail) {
        return core().check(EnumChecks.enumConstant(value, type), code, detail);
    }

}