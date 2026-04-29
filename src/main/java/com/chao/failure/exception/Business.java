package com.chao.failure.exception;

import com.chao.failure.config.mapping.CodeMappingConfig;
import com.chao.failure.config.masking.DefaultValueMasker;
import com.chao.failure.constant.FailureConst;
import com.chao.failure.constant.Severity;
import com.chao.failure.internal.core.Ex;
import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.internal.core.i18n.LocalizedTexts;
import com.chao.failure.internal.core.observability.OpenTelemetryBridge;
import com.chao.failure.internal.core.observability.TraceInfoExtractor;
import com.chao.failure.internal.core.security.MaskPickRegistry;
import com.chao.failure.internal.core.security.ValueMaskerRegistry;
import com.chao.failure.internal.policy.DefaultErrorPolicy;
import com.chao.failure.internal.policy.ErrorPolicy;
import com.chao.failure.spi.filter.SkipPrefixRegistry;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Business exception class - Enhanced version.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
@Getter
public class Business extends RuntimeException implements Serializable {

    private static final int MAX_DETAIL_LENGTH = 1024;
    private static final Pattern DANGEROUS_DETAIL_PATTERN = Pattern.compile("<script|javascript:|onerror=|onclick=", Pattern.CASE_INSENSITIVE);

    /**
     * Response code enum.
     */
    private final ResponseCode responseCode;

    /**
     * Detailed error description.
     */
    private final String detail;

    /**
     * Exception method name.
     */
    private final String method;

    /**
     * Exception location info.
     */
    private final String location;

    /**
     * HTTP status code.
     */
    private final HttpStatus httpStatus;

    /**
     * Invalid value causing exception.
     */
    private final transient Object invalidValue;

    /**
     * Masked value safe for serialization/logging.
     */
    private final Object maskedValue;

    /**
     * Field path causing exception.
     *
     */
    @Getter
    private final String path;
    private final Severity severity;
    private final String traceId;
    private final String spanId;


    /**
     * Serial version UID.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param responseCode Response code enum
     * @param detail       Detailed error description
     * @param method       Method name where exception occurred
     * @param location     Location info where exception occurred
     * @param httpStatus   HTTP status code
     * @param invalidValue Parameter value causing exception
     */
    public Business(ResponseCode responseCode, String detail, String method, String location, HttpStatus httpStatus, Object invalidValue) {
        this(responseCode, detail, method, location, httpStatus, invalidValue, null);
    }


    /**
     * Constructor.
     *
     * @param responseCode Response code enum
     * @param detail       Detailed error description
     * @param method       Method name where exception occurred
     * @param location     Location info where exception occurred
     * @param httpStatus   HTTP status code
     * @param invalidValue Parameter value causing exception
     * @param path         Field path causing exception
     */
    public Business(ResponseCode responseCode, String detail, String method, String location, HttpStatus httpStatus, Object invalidValue, String path) {
        this(resolveSeverity(responseCode), responseCode, detail, method, location, httpStatus, invalidValue, path, null, null);
    }

    /**
     * Constructor with traceId and spanId.
     *
     * @param responseCode Response code enum
     * @param detail       Detailed error description
     * @param method       Method name where exception occurred
     * @param location     Location info where exception occurred
     * @param httpStatus   HTTP status code
     * @param invalidValue Parameter value causing exception
     * @param path         Field path causing exception
     * @param traceId      Trace ID for distributed tracing
     * @param spanId       Span ID for distributed tracing
     */
    public Business(ResponseCode responseCode, String detail, String method, String location, HttpStatus httpStatus, Object invalidValue, String path, String traceId, String spanId) {
        this(resolveSeverity(responseCode), responseCode, detail, method, location, httpStatus, invalidValue, path, traceId, spanId);
    }

    private Business(Severity severity, ResponseCode responseCode, String detail, String method, String location,
                     HttpStatus httpStatus, Object invalidValue, String path, String traceId, String spanId) {
        super(LocalizedTexts.message(responseCode), null, true, shouldFillStackTrace(responseCode, severity));
        this.responseCode = responseCode;
        this.detail = sanitizeDetail(detail);
        this.method = method;
        this.location = location;
        this.httpStatus = httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR;
        this.path = path;
        this.invalidValue = invalidValue;
        this.maskedValue = maskValue(invalidValue, path);
        this.severity = severity != null ? severity : Severity.INFO;
        String otelTraceId = OpenTelemetryBridge.currentTraceId();
        String otelSpanId = OpenTelemetryBridge.currentSpanId();
        this.traceId = firstNonBlank(traceId, resolveContextTraceId(), otelTraceId);
        this.spanId = firstNonBlank(spanId, otelSpanId);
    }

    private static boolean shouldFillStackTrace(ResponseCode code, Severity severity) {
        FailureContext ctx = Ex.getContext();
        boolean printMethod = ctx != null && TraceInfoExtractor.shadowTrace(ctx, null);
        if (printMethod) {
            return true;
        }
        if (severity != null) {
            return severity.isFillStackTrace();
        }
        if (code == null) return true;
        CodeMappingConfig cfg = ctx != null ? ctx.getCodeMappingConfig() : null;
        return cfg != null && cfg.resolveHttpStatus(code.getCode()).is5xxServerError();
    }

    public static Business of(int code, String message) {
        return of(simpleCode(code, message));
    }

    public static Business of(int code, String message, String detail) {
        return of(simpleCode(code, message), detail);
    }

    public static Business of(int code, String message, String detail, Object... args) {
        return of(simpleCode(code, message), String.format(detail, args));
    }

    /**
     * Static factory method to create Business exception.
     *
     * @param code Response code
     * @return Constructed Business exception object
     */
    public static Business of(ResponseCode code) {
        return compose().responseCode(code).materialize();
    }

    /**
     * Create Business exception with detailed description.
     *
     * @param code   Response code
     * @param detail Detailed error description
     * @return Constructed Business exception object
     */
    public static Business of(ResponseCode code, String detail) {
        return compose().responseCode(code).detail(detail).materialize();
    }

    /**
     * Create Business exception with formatted parameters.
     *
     * @param code   Response code
     * @param detail Description template containing placeholders
     * @param args   Formatting arguments
     * @return Constructed Business exception object
     */
    public static Business of(ResponseCode code, String detail, Object... args) {
        return compose().responseCode(code).detail(String.format(detail, args)).materialize();
    }

    /**
     * Create Business exception with specified method and location.
     *
     * @param code     Response code
     * @param detail   Detailed error description
     * @param method   Method name
     * @param location Location info
     * @return Constructed Business exception object
     */
    public static Business of(ResponseCode code, String detail, String method, String location) {
        return compose().responseCode(code).detail(detail).method(method).location(location).materialize();
    }

    private static ResponseCode simpleCode(int code, String message) {
        return ResponseCode.of(code, message, message);
    }

    /**
     * Get builder instance for chain-building Business object.
     *
     * @return Fabricator builder instance
     */
    public static Fabricator compose() {
        return new Fabricator();
    }

    /**
     * Business object builder class.
     */
    public static class Fabricator implements Serializable {
        /**
         * Response code.
         */
        private ResponseCode responseCode;

        /**
         * Detailed description.
         */
        private String detail;

        /**
         * Method name.
         */
        private String method;

        /**
         * Location info.
         */
        private String location;

        /**
         * Invalid value.
         */
        private Object invalidValue;

        /**
         * Field path.
         */
        private String path;
        private Severity severity;
        private String traceId;
        private String spanId;


        /**
         * Serial version UID.
         */
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * Set response code.
         *
         * @param code Response code enum
         * @return Current builder instance
         */
        public Fabricator responseCode(ResponseCode code) {
            this.responseCode = code;
            return this;
        }

        /**
         * Set detailed description.
         *
         * @param detail Detailed description
         * @return Current builder instance
         */
        public Fabricator detail(String detail) {
            this.detail = detail;
            return this;
        }

        /**
         * Set method name (package-private).
         *
         * @param method Method name
         * @return Current builder instance
         */
        Fabricator method(String method) {
            this.method = method;
            return this;
        }

        /**
         * Set location info.
         *
         * @param location Location info
         * @return Current builder instance
         */
        public Fabricator location(String location) {
            this.location = location;
            return this;
        }

        /**
         * Set invalid value.
         *
         * @param value Parameter value causing exception
         * @return Current builder instance
         */
        public Fabricator invalidValue(Object value) {
            this.invalidValue = value;
            return this;
        }

        /**
         * Set field path.
         *
         * @param path Field path causing exception
         * @return Current builder instance
         */
        public Fabricator path(String path) {
            this.path = path;
            return this;
        }

        public Fabricator severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Fabricator traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Fabricator spanId(String spanId) {
            this.spanId = spanId;
            return this;
        }


        /**
         * Build final Business object.
         *
         * @return Constructed Business exception object
         * @throws IllegalArgumentException Thrown when code is null
         */
        public Business materialize() {
            if (responseCode == null) throw new IllegalArgumentException(FailureConst.CODE_REQUIRED);
            if (detail == null) {
                FailureContext ctx = Ex.getContext();
                ErrorPolicy policy = ctx != null ? java.util.Objects.requireNonNullElse(ctx.getErrorPolicy(), DefaultErrorPolicy.INSTANCE) : DefaultErrorPolicy.INSTANCE;
                detail = policy.defaultDetail(responseCode);
                if (detail == null) detail = responseCode.getDescription();
                if (detail == null) detail = responseCode.getMessage();
                if (detail == null) detail = FailureConst.MESSAGE_OR_DESCRIPTION_REQUIRED;
            }
            FailureContext ctx = Ex.getContext();
            if (ctx != null && ctx.isShadowTrace()) {
                if (method == null) method = Ex.method();
                if (location == null) location = Ex.location();
            }
            CodeMappingConfig cfg = ctx != null ? ctx.getCodeMappingConfig() : null;
            int code = responseCode.getCode();
            HttpStatus status = (cfg != null) ? cfg.resolveHttpStatus(code) : resolveHttpStatusWithoutContext(code);
            Severity finalSeverity = severity != null ? severity : resolveSeverity(responseCode);
            Business business = new Business(finalSeverity, responseCode, detail, method, location, status, invalidValue, path, traceId, spanId);
            if (ctx != null && ctx.isTrimStackTrace()) {
                business.setStackTrace(trimStackTrace(business.getStackTrace()));
            }
            return business;
        }
    }

    /**
     * Override toString method to provide formatted exception info.
     *
     * @return Formatted string representation
     */
    @Override
    public String toString() {
        String codeStr = String.valueOf(responseCode.getCode()).replaceFirst("(\\d{3})(\\d{2})", "$1_$2");

        String pathStr = "";
        if (path != null && !path.isBlank()) {
            pathStr = ", path=" + path;
        }

        String valStr = "";
        FailureContext ctx = Ex.getContext();
        if (maskedValue != null && ctx != null && ctx.isDebugSnapshot()) {
            valStr = ", val=" + maskedValue;
        }
        String base = "{code=%s, mes=%s, des=%s%s%s}".formatted(
                codeStr,
                LocalizedTexts.message(responseCode),
                LocalizedTexts.detail(responseCode, detail),
                pathStr,
                valStr
        );
        if (method == null) return base + (location != null ? " (" + extractFileLine(location) + ")" : "");

        String displayMethod = method;
        int dollarIndex = method.indexOf('$');
        if (dollarIndex > 0) {
            int hashIndex = method.lastIndexOf('#');
            if (hashIndex > dollarIndex) {
                displayMethod = method.substring(0, dollarIndex) + method.substring(hashIndex);
            }
        }
        return "[%s] %s".formatted(displayMethod, base) + (location != null ? " (" + extractFileLine(location) + ")" : "");
    }

    private static HttpStatus resolveHttpStatusWithoutContext(int code) {
        if (code >= 100 && code <= 599) {
            HttpStatus status = resolveHttpStatusEnum(code);
            if (status != null) {
                return status;
            }
        }
        if (code >= 40000 && code < 50000) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static HttpStatus resolveHttpStatusEnum(int code) {
        for (HttpStatus s : HttpStatus.values()) {
            if (s.value() == code) {
                return s;
            }
        }
        return null;
    }

    private String extractFileLine(String loc) {
        if (loc == null) return "";
        int left = loc.indexOf('(');
        if (left < 0) return loc;
        int right = loc.lastIndexOf(')');
        if (right <= left) return loc;

        String content = loc.substring(left + 1, right);
        int dollar = content.indexOf('$');
        if (dollar < 0) return content;
        int dot = content.indexOf('.', dollar);
        if (dot < 0) return content;
        return content.substring(0, dollar) + content.substring(dot);
    }

    private Object maskValue(Object value, String fieldPath) {
        if (value == null) return null;
        Object masked = ValueMaskerRegistry.getDefault().mask(value, MaskPickRegistry.getDefault().resolve(fieldPath));
        if (masked != null) return masked;
        return new DefaultValueMasker().mask(value, MaskPickRegistry.getDefault().resolve(fieldPath));
    }

    private String sanitizeDetail(String detail) {
        if (detail == null) return null;
        String s = detail.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
        if (s.length() > MAX_DETAIL_LENGTH) {
            s = s.substring(0, MAX_DETAIL_LENGTH) + "...";
        }
        if (DANGEROUS_DETAIL_PATTERN.matcher(s).find()) {
            return "Invalid detail content";
        }
        return s;
    }

    private static Severity resolveSeverity(ResponseCode code) {
        FailureContext ctx = Ex.getContext();
        if (ctx == null) {
            return Severity.INFO;
        }
        return ctx.resolveSeverity(code);
    }

    private static String resolveContextTraceId() {
        FailureContext ctx = Ex.getContext();
        if (ctx == null) return null;
        return ctx.getTraceId();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }

    private static StackTraceElement[] trimStackTrace(StackTraceElement[] stack) {
        if (stack == null || stack.length == 0) return stack;
        SkipPrefixRegistry registry = Ex.getSkipPrefixRegistry();
        if (registry == null) return stack;

        int i = 0;
        while (i < stack.length) {
            String cls = stack[i].getClassName();
            if (!registry.shouldSkip(cls)) break;
            i++;
        }
        if (i == 0 || i >= stack.length) return stack;
        return Arrays.copyOfRange(stack, i, stack.length);
    }
}
