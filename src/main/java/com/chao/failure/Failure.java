package com.chao.failure;

import com.chao.failure.internal.core.Chain;
import com.chao.failure.validator.FastValidator.ValidationContext;

/**
 * Failure static entry class - Chain validation API.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class Failure {

    private Failure() {
    }

    /**
     * Start a new validation chain (default fail-fast mode).
     *
     * @return New Chain instance
     */
    public static Chain begin() {
        return Chain.begin(true);
    }

    /**
     * Start a new validation chain (strict mode).
     *
     * @return New Chain instance
     */
    public static Chain strict() {
        return Chain.begin(false);
    }

    /**
     * Start a new validation chain (integrate ValidationContext).
     *
     * @param context Validation context
     * @return New Chain instance
     */
    public static Chain with(ValidationContext context) {
        return Chain.begin(context);
    }
}
