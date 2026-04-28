package com.chao.failure.internal.check;

import java.util.Optional;

/**
 * Utility class for optional validation.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class OptionalChecks {
    private OptionalChecks() {
    }

    /**
     * Checks if the optional has a value.
     *
     * @param opt the optional to check
     * @return true if the optional is present, false otherwise
     */
    public static boolean isPresent(Optional<?> opt) {
        return opt != null && opt.isPresent();
    }

    /**
     * Checks if the optional is empty.
     *
     * @param opt the optional to check
     * @return true if the optional is empty, false otherwise
     */
    public static boolean isEmpty(Optional<?> opt) {
        return opt == null || !opt.isPresent();
    }
}
