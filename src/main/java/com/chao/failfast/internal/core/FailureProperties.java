package com.chao.failfast.internal.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fail-Fast configuration properties - Enhanced version.
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "fail-fast")
public class FailureProperties {
    /**
     * Whether to print method name (for debugging).
     */
    private boolean shadowTrace;

    /**
     * Whether to enable verbose error response mode.
     */
    private boolean verbose = false;

    /**
     * Whether to enable debug snapshot (DX-2).
     */
    private boolean debugSnapshot = false;

    /**
     * Error code mapping configuration.
     */
    private CodeMapping codeMapping = new CodeMapping();

    /**
     * Internationalization configuration.
     */
    private I18n i18n = new I18n();

    /**
     * Internationalization configuration class.
     */
    @Data
    public static class I18n {
        /**
         * Whether to enable internationalization support.
         */
        private boolean enabled = true;

        /**
         * Default locale.
         */
        private String defaultLocale = "zh_CN";

        /**
         * Internationalization resource file base path.
         */
        private String basename = "classpath:i18n/messages";

        /**
         * Resource file encoding.
         */
        private String encoding = "UTF-8";

        /**
         * Resource file cache time (seconds).
         */
        private int cacheSeconds = 3600;
    }

    /**
     * Error code mapping configuration class.
     */
    @Data
    public static class CodeMapping {
        /**
         * HTTP status code mapping: error code -> HTTP status code.
         */
        private Map<String, Integer> httpStatus = new HashMap<>();

        /**
         * Error code grouping.
         */
        private Map<String, List<Object>> groups = new HashMap<>();
    }
}
