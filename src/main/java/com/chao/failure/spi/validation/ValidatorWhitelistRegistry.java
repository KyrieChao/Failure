package com.chao.failure.spi.validation;

import com.chao.failure.validator.FastValidator;

/**
 * Validator whitelist registry interface
 * This interface is used to manage validator whitelist, can add validator types and check if validator is in whitelist
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public interface ValidatorWhitelistRegistry {

    /**
     * Add validator types to whitelist
     *
     * @param validatorTypes One or more validator types to add, must be FastValidator subclasses
     * @return Returns current ValidatorWhitelistRegistry instance, supports method chaining
     */
    @SuppressWarnings("unchecked")
    ValidatorWhitelistRegistry add(Class<? extends FastValidator<?>>... validatorTypes);

    /**
     * Check if specified validator type is in whitelist
     *
     * @param validatorType Validator type to check, must be FastValidator subclass
     * @return true if validator is in whitelist, false otherwise
     */
    boolean isWhitelisted(Class<? extends FastValidator<?>> validatorType);
}
