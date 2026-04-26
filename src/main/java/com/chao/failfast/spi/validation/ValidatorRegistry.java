package com.chao.failfast.spi.validation;

import com.chao.failfast.validator.FastValidator;

/**
 * Registry for global custom validators.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public interface ValidatorRegistry {

    /**
     * Register a validator for a specific type.
     *
     * @param type      The type to validate
     * @param validator The validator instance
     * @param <T>       The type
     * @return this registry instance for chaining
     */
    <T> ValidatorRegistry register(Class<T> type, FastValidator<T> validator);

    /**
     * Get a registered validator for a specific type.
     *
     * @param type The type to validate
     * @return The best matching validator, or null if none found
     */
    FastValidator<Object> getValidator(Class<?> type);
}

