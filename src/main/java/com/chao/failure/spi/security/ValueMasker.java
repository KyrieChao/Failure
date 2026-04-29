package com.chao.failure.spi.security;

/**
 * Mask sensitive values before exposing to logs/serialization.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public interface ValueMasker {

    /**
     * @param value Original value
     * @return Masked/safe value for output and serialization
     */
    Object mask(Object value);

    /**
     * @param value Original value
     * @param mask Masking configuration
     * @return Masked/safe value for output and serialization
     */
    Object mask(Object value, Mask mask);
}
