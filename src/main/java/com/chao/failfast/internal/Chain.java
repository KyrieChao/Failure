package com.chao.failfast.internal;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.internal.chain.*;

/**
 * Validation Chain - Facade class.
 *
 * @author Kyrie Chao
 * @version 1.0.0
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
