package com.chao.failure.internal.core.security;

import com.chao.failure.config.masking.DefaultValueMasker;
import com.chao.failure.spi.security.ValueMasker;


/**
 * ValueMaskerRegistry is a value masker registry class for managing and obtaining default ValueMasker implementations.
 * This class is designed with singleton pattern to ensure only one instance globally, and provides thread-safe access and setting methods.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class ValueMaskerRegistry {

    // Default value masker implementation, used when no other masker is set
    private static final ValueMasker FALLBACK = new DefaultValueMasker();
    // Currently used value masker, using volatile to ensure visibility in multi-threaded environment
    private static volatile ValueMasker masker = FALLBACK;

    /**
     * Private constructor to prevent external instantiation of this class
     */
    private ValueMaskerRegistry() {
    }

    /**
     * Set default value masker
     * @param valueMasker Value masker to set, uses default FALLBACK masker if null
     */
    public static void setDefault(ValueMasker valueMasker) {
        masker = valueMasker != null ? valueMasker : FALLBACK;
    }

    /**
     * Get currently used value masker
     * @return Currently set value masker, returns default FALLBACK masker if not set
     */
    public static ValueMasker getDefault() {
        return masker != null ? masker : FALLBACK;
    }
}
