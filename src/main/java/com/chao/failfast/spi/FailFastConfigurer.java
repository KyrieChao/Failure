package com.chao.failfast.spi;

/**
 * Extension interface for configuring fail-fast framework behavior.
 * Implement this interface in a Spring @Configuration class to customize.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
public interface FailFastConfigurer {

    /**
     * Add types that should be skipped during deep validation.
     *
     * @param registry Registry for skip types
     */
    default void addValidationSkipTypes(SkipTypeRegistry registry) {
    }

    /**
     * Add package prefixes to skip when building exception stack traces.
     *
     * @param registry Registry for skip prefixes
     */
    default void addExceptionSkipPrefixes(SkipPrefixRegistry registry) {
    }

    /**
     * Register global custom validators.
     * Validators registered here will be automatically applied to matching types
     * without needing to explicitly specify them in @Validate(value = ...).
     *
     * @param registry Registry for validators
     */
    default void addCustomValidators(ValidatorRegistry registry) {
    }
}

