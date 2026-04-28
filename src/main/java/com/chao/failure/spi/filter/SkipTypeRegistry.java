package com.chao.failure.spi.filter;

/**
 * Registry for types that should be skipped during deep validation.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public interface SkipTypeRegistry {

    /**
     * Add types to skip during validation.
     *
     * @param types Classes to skip
     * @return this registry instance for chaining
     */
    SkipTypeRegistry add(Class<?>... types);

    /**
     * Check if a type should be skipped.
     *
     * @param type Class to check
     * @return true if the type should be skipped
     */
    boolean shouldSkip(Class<?> type);
}

