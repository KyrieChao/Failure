package com.chao.failfast.internal.validation;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Recursive option configuration class, using Builder pattern to build objects
 * Automatically generates getter methods through Lombok's @Getter and annotations
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
@Getter
@Builder
public class RecursiveOption {
    /**
     * Maximum depth of recursion, default value is 4
     * Uses @Builder.Default annotation to ensure Builder pattern uses default value when creating objects
     */
    @Builder.Default
    private int maxDepth = 4;

    /**
     * List of included files or directories
     * Used to specify items to include during recursion
     */
    private List<String> include;

    /**
     * List of excluded files or directories
     * Used to specify items to exclude during recursion
     */
    private List<String> exclude;

    /**
     * Maximum number of items to process, default value is 1000
     * Used to limit the total number of items processed recursively to prevent infinite recursion
     */
    @Builder.Default
    private int maxItems = 1000;

    /**
     * Maximum number of errors allowed, default value is 100
     * When error count exceeds this value, recursive process may terminate early
     */
    @Builder.Default
    private int maxErrors = 100;
    /**
     * Whether to enable global deduplication, default value is true
     * When set to true, all results will be globally deduplicated
     */
    @Builder.Default
    private boolean dedupeGlobal = true;
}
