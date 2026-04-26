package com.chao.failfast.internal.chain;

import com.chao.failfast.internal.chain.pipeline.ChainCore;

/**
 * JSR-303 validation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.3.0
 */
public interface JsrTerm<S extends ChainCore<S>> {

    /**
     * Get chain core.
     *
     * @return Chain core instance
     */
    S core();

    /**
     * Start JSR-303 validation chain for an object.
     *
     * @param target Target object to validate
     * @return JsrValidator instance
     */
    JsrValidator<S> jsr(Object target);

    /**
     * Start JSR-303 validation chain for a specific class.
     *
     * @param beanClass Bean class
     * @return JsrValidator instance
     */
    JsrValidator<S> jsr(Class<?> beanClass);

    /**
     * JSR-303 validator interface.
     *
     * @param <S> Subclass type of ChainCore
     */
    interface JsrValidator<S extends ChainCore<S>> {

        /**
         * Validate the target object.
         *
         * @return Parent chain instance
         */
        S validate();

        /**
         * Validate a specific property value.
         *
         * @param propertyName Property name
         * @param value        Property value
         * @return Parent chain instance
         */
        S value(String propertyName, Object value);

        /**
         * Set path prefix for validation errors.
         *
         * @param pathPrefix Path prefix
         * @return Current JsrValidator instance
         */
        JsrValidator<S> pathPrefix(String pathPrefix);
    }
}
