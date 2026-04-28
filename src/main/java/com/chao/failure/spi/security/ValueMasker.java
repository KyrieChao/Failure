package com.chao.failure.spi.security;

/**
 * Mask sensitive values before exposing to logs/serialization.
 *
 * @author Kyrie Chao
 * @version 1.2.2
 */
public interface ValueMasker {

    /**
     * Mask value by field path.
     *
     * @param value Original value
     * @param fieldPath Field path (nullable)
     * @return Masked/safe value for output and serialization
     */
    Object mask(Object value, String fieldPath);
}
