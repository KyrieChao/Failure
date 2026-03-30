package com.chao.failfast.exception;

import com.chao.failfast.config.mapping.CodeMappingConfig;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.core.Ex;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.internal.core.ContextResolver;
import com.chao.failfast.internal.policy.DefaultErrorPolicy;
import com.chao.failfast.internal.policy.ErrorPolicy;
import com.chao.failfast.util.I18n;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.regex.Matcher;

/**
 * Business exception class - Enhanced version.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
@Getter
public class Business extends RuntimeException implements Serializable {

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
    private final Object invalidValue;

    /**
     * Field path causing exception.
     *
     */
    @Getter
    private final String path;


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
        super(I18n.get(responseCode != null ? responseCode.getMessage() : FailureConst.UNKNOWN_ERROR), null, true, shouldFillStackTrace(responseCode));
        this.responseCode = responseCode;
        this.detail = detail;
        this.method = method;
        this.location = location;
        this.httpStatus = httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR;
        this.invalidValue = invalidValue;
        this.path = path;
    }

    private static boolean shouldFillStackTrace(ResponseCode code) {
        if (code == null) return true;
        FailureContext ctx = Ex.getContext();
        CodeMappingConfig cfg = ctx != null ? ctx.getCodeMappingConfig() : null;
        boolean printMethod = ctx != null && ContextResolver.shadowTrace(ctx, null);
        return printMethod || (cfg != null && cfg.resolveHttpStatus(code.getCode()).is5xxServerError());
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
            CodeMappingConfig cfg = Ex.getContext() != null ? Ex.getContext().getCodeMappingConfig() : null;
            HttpStatus status = (cfg != null) ? cfg.resolveHttpStatus(responseCode.getCode()) : HttpStatus.INTERNAL_SERVER_ERROR;
            Business business = new Business(responseCode, detail, method, location, status, invalidValue, path);
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
        if (invalidValue != null && ctx != null && ctx.isDebugSnapshot()) {
            String masked = maskValue(invalidValue);
            valStr = ", val=" + masked;
        }

        String base = "{code=%s, mes=%s, des=%s%s%s}".formatted(
                codeStr,
                I18n.get(responseCode.getMessage()),
                I18n.get(detail),
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

    private String maskValue(Object value) {
        String str = value.toString();
        if (str.isEmpty()) return str;

        if (FailureConst.Mobile.matcher(str).matches()) {
            return str.substring(0, 3) + "****" + str.substring(7);
        }

        Matcher emailMatcher = FailureConst.Email.matcher(str);
        if (emailMatcher.matches()) {
            return emailMatcher.group(1) + "****" + emailMatcher.group(3);
        }

        if (FailureConst.Card.matcher(str).matches()) {
            return str.substring(0, 4) + "****" + str.substring(str.length() - 4);
        }

        if (str.length() > 50) {
            return str.substring(0, 5) + "...(" + str.length() + "char)..."
                    + str.substring(str.length() - 5);
        }
        return str;
    }

    private static StackTraceElement[] trimStackTrace(StackTraceElement[] stack) {
        if (stack == null || stack.length == 0) return stack;
        com.chao.failfast.spi.SkipPrefixRegistry registry = Ex.getSkipPrefixRegistry();
        if (registry == null) return stack;

        int i = 0;
        while (i < stack.length) {
            String cls = stack[i].getClassName();
            if (!registry.shouldSkip(cls)) break;
            i++;
        }
        if (i <= 0 || i >= stack.length) return stack;
        StackTraceElement[] trimmed = Arrays.copyOfRange(stack, i, stack.length);
        return trimmed;
    }
}
