package com.chao.failfast.internal;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.internal.chain.*;

/**
 * 验证链 - 门面类
 * <p>
 * 仅保留工厂方法，所有逻辑委托给：
 * - ChainCore: 状态管理
 * - ChainTerminator: 终结操作
 * - *Validation: 各类校验
 */
public final class Chain extends ChainCore<Chain> implements
        ChainTerminator<Chain>,
        ObjectTerm<Chain>,
        StringTerm<Chain>,
        NumberTerm<Chain>,
        CollectionTerm<Chain>,
        ArrayTerm<Chain>,
        MapTerm<Chain>,
        DateTerm<Chain>,
        OptionalTerm<Chain>,
        EnumTerm<Chain>,
        IdentityTerm<Chain>,
        BooleanTerm<Chain>,
        CustomTerm<Chain> {


    public static Chain begin(boolean failFast) {
        return new Chain(failFast, null);
    }

    public static Chain begin(ValidationContext context) {
        return new Chain(context.isFast(), context);
    }


    private Chain(boolean failFast, ValidationContext context) {
        super(failFast, context);
    }


    @Override
    public Chain core() {
        return this;
    }
}