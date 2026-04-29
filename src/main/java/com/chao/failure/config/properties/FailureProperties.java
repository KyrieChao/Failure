package com.chao.failure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Failure configuration properties - Enhanced version.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
@Data
@ConfigurationProperties(prefix = "fail-fast")
public class FailureProperties {
    /**
     * Whether to print method name (for debugging).
     */
    private boolean shadowTrace = false;

    private boolean trimStackTrace = true;

    /**
     * Whether to enable verbose error response mode.
     */
    private boolean verbose = false;

    /**
     * Whether to enable debug snapshot (DX-2).
     */
    private boolean debugSnapshot = false;

    /**
     * Whether to enable method validation.
     */
    private boolean methodValidationEnabled = false;

    /**
     * Error code mapping configuration.
     */
    private CodeMapping codeMapping = new CodeMapping();

    /**
     * Internationalization configuration.
     */
    private I18n i18n = new I18n();

    private TraceId traceId = new TraceId();
    private Reactive reactive = new Reactive();
    private Logging logging = new Logging();
    private Masking masking = new Masking();

    /**
     * Chain-level behavior configuration.
     */
    private Chain chain = new Chain();

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

        /**
         * Constraint to response code mapping: constraint name -> response code.
         */
        private Map<String, Integer> constraintMapping = new HashMap<>();

        /**
         * Constraint + path to response code mapping: constraintName:path -> response code.
         */
        private List<ConstraintPathMapping> constraintPathMapping = new ArrayList<>();

        /**
         * Constraint + bean class to response code mapping: constraintName:beanClass -> response code.
         */
        private List<ConstraintBeanMapping> constraintBeanMapping = new ArrayList<>();

        /**
         * Constraint + path to response code mapping configuration.
         */
        @Data
        public static class ConstraintPathMapping {
            /**
             * Constraint name.
             */
            private String constraint;

            /**
             * Field path.
             */
            private String path;

            /**
             * Response code.
             */
            private Integer code;
        }

        /**
         * Constraint + bean class to response code mapping configuration.
         */
        @Data
        public static class ConstraintBeanMapping {
            /**
             * Constraint name.
             */
            private String constraint;

            /**
             * Bean class name.
             */
            private String bean;

            /**
             * Response code.
             */
            private Integer code;
        }
    }

    /**
     * Trace ID configuration.
     */
    @Data
    public static class TraceId {
        /**
         * Whether to enable trace ID support.
         */
        private boolean enabled = false;

        /**
         * Request header name for trace ID.
         */
        private String headerName = "X-Trace-Id";

        /**
         * Whether to generate trace ID if missing.
         */
        private boolean generateIfMissing = false;

        /**
         * Whether to include trace ID in response header.
         */
        private boolean responseHeader = false;

        /**
         * Response header name for trace ID.
         */
        private String responseHeaderName = "X-Trace-Id";

        /**
         * Whether to enable MDC (Mapped Diagnostic Context) support.
         */
        private boolean mdcEnabled = false;

        /**
         * MDC key for trace ID.
         */
        private String mdcKey = "traceId";
    }

    /**
     * Reactive programming configuration.
     */
    @Data
    public static class Reactive {
        /**
         * Whether to prioritize context-first processing mode.
         */
        private boolean contextFirst = false;
    }

    @Data
    public static class Logging {
        /**
         * Default error severity for Business exception.
         */
        private String defaultSeverity = "ERROR";

        /**
         * Whether to use compact banner style in logs when shadow-trace is enabled.
         */
        private boolean banner = true;

        /**
         * Business code to severity mapping.
         */
        private Map<String, String> severityMapping = new HashMap<>();
    }

    @Data
    public static class Masking {
        /**
         * Enable structured masking for object snapshots.
         */
        private boolean structuredEnabled = false;
        /**
         * Max recursion depth.
         */
        private int maxDepth = 3;
        /**
         * Max collection entries.
         */
        private int maxCollectionSize = 20;
        /**
         * Max object fields.
         */
        private int maxFields = 30;
    }

    @Data
    public static class Chain {
        /**
         * Maximum number of errors collected in strict mode.
         * Non-positive value means using framework default.
         */
        private int maxErrors = 50;
    }
}
