package com.chao.failure.spi.security;

/**
 * Mask descriptor for explicit masking.
 *
 * <p>This interface represents a logical masking type (for example: password, token, apiKey).
 * Implementations are typically enums in application code.</p>
 *
 * <p>The masking engine decides how to apply masking based on {@link #type()}.</p>
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public interface Mask {
    /**
     * Logical mask type identifier.
     *
     * @return mask type code, usually lowercase (e.g. {@code "password"}, {@code "token"})
     */
    String type();
}
