package com.chao.failfast.internal.chain;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.check.EnumChecks;
import com.chao.failfast.internal.core.ResponseCode;

/**
 * Enum validation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.0.0
 */
public interface EnumTerm<S extends ChainCore<S>> {

    /**
     * Get chain core.
     *
     * @return Chain core instance
     */
    S core();

    default <E extends Enum<E>> S enumValue(Class<E> enumType, String value) {
        return enumValue(enumType, value, FailureConst.ENUM_VALUE_ERROR, null);
    }

    default <E extends Enum<E>> S enumValue(Class<E> enumType, String value, ResponseCode code) {
        return enumValue(enumType, value, code, null);
    }

    default <E extends Enum<E>> S enumValue(Class<E> enumType, String value, ResponseCode code, String detail) {
        return core().check(EnumChecks.enumValue(enumType, value), code, detail);
    }

    default <E extends Enum<E>> S enumConstant(E value, Class<E> type) {
        return enumConstant(value, type, FailureConst.ENUM_CONSTANT_ERROR, null);
    }

    default <E extends Enum<E>> S enumConstant(E value, Class<E> type, ResponseCode code) {
        return enumConstant(value, type, code, null);
    }

    default <E extends Enum<E>> S enumConstant(E value, Class<E> type, ResponseCode code, String detail) {
        return core().check(EnumChecks.enumConstant(value, type), code, detail);
    }

}
