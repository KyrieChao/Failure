package com.chao.failure.internal.core.security;

import com.chao.failure.config.masking.DefaultValueMasker;
import com.chao.failure.spi.security.ValueMasker;

import java.util.concurrent.atomic.AtomicReference;


/**
 * ValueMaskerRegistry is a value masker registry class for managing and obtaining default ValueMasker implementations.
 * This class is designed with singleton pattern to ensure only one instance globally, and provides thread-safe access and setting methods.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public final class ValueMaskerRegistry {

    // Default value masker implementation, used when no other masker is set
    private static final ValueMasker FALLBACK = new DefaultValueMasker();
    private static final AtomicReference<ValueMasker> MASKER = new AtomicReference<>(FALLBACK);

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
        MASKER.set(valueMasker != null ? valueMasker : FALLBACK);
    }

    /**
     * Get currently used value masker
     * @return Currently set value masker, returns default FALLBACK masker if not set
     */
    public static ValueMasker getDefault() {
        ValueMasker current = MASKER.get();
        return current != null ? current : FALLBACK;
    }
}
