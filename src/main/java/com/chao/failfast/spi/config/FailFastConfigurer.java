package com.chao.failfast.spi.config;

import com.chao.failfast.spi.filter.SkipPrefixRegistry;
import com.chao.failfast.spi.filter.SkipTypeRegistry;
import com.chao.failfast.spi.i18n.LocalizedResponseResolver;
import com.chao.failfast.spi.validation.ValidatorRegistry;
import com.chao.failfast.spi.validation.ValidatorWhitelistRegistry;

/**
 * Extension interface for configuring fail-fast framework behavior.
 * Implement this interface in a Spring @Configuration class to customize.
 *
 * @author Kyrie Chao
 * @version 1.3.0
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

    default void addValidatorWhitelist(ValidatorWhitelistRegistry registry) {
    }

    /**
     * Register localized response resolver for code-level i18n.
     *
     * @param resolver resolver to customize message/detail by code and locale
     */
    default void customizeLocalizedResponseResolver(LocalizedResponseResolver resolver) {
    }
}

