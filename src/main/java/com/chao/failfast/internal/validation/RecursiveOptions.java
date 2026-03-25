package com.chao.failfast.internal.validation;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Recursive validation options.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
@Getter
@Builder
public class RecursiveOptions {
    /**
     * Maximum recursion depth.
     */
    @Builder.Default
    private int maxDepth = 4;

    /**
     * Included fields (whitelist).
     */
    private List<String> include;

    /**
     * Excluded fields (blacklist).
     */
    private List<String> exclude;

    /**
     * Maximum items in collection.
     */
    @Builder.Default
    private int maxItems = 1000;

    /**
     * Maximum errors to collect.
     */
    @Builder.Default
    private int maxErrors = 100;
}
