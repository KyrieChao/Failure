package com.chao.failfast.spi.filter;

/**
 * Registry for package prefixes to skip when building exception stack traces.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public interface SkipPrefixRegistry {

    /**
     * Add package prefixes to skip.
     *
     * @param prefixes Package prefixes to skip
     * @return this registry instance for chaining
     */
    SkipPrefixRegistry add(String... prefixes);

    /**
     * Check if a class name starts with any of the skipped prefixes.
     *
     * @param className Class name to check
     * @return true if the class should be skipped
     */
    boolean shouldSkip(String className);
}

