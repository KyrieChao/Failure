package com.chao.failfast.internal;

import com.chao.failfast.config.CodeMappingConfig;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.util.I18n;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Matcher;

/**
 * Business exception class - Enhanced version.
 *
 * @author Kyrie Chao
 * @version 1.0.0
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
    Business(ResponseCode responseCode, String detail, String method, String location, HttpStatus httpStatus, Object invalidValue) {
        super(I18n.get(responseCode != null ? responseCode.getMessage() : FailureConst.UNKNOWN_ERROR), null, true, shouldFillStackTrace(responseCode));
        this.responseCode = responseCode;
        this.detail = detail;
        this.method = method;
        this.location = location;
        this.httpStatus = httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR;
        this.invalidValue = invalidValue;
    }

    private static boolean shouldFillStackTrace(ResponseCode code) {
        if (code == null) return true;
        FailureContext ctx = Ex.getContext();
        CodeMappingConfig cfg = ctx != null ? ctx.getCodeMappingConfig() : null;
        boolean printMethod = ctx != null && ctx.isShadowTrace();
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
         * Set location info (package-private).
         *
         * @param location Location info
         * @return Current builder instance
         */
        Fabricator location(String location) {
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
         * Build final Business object.
         *
         * @return Constructed Business exception object
         * @throws IllegalArgumentException Thrown when code is null
         */
        public Business materialize() {
            // 校验必要参数
            if (responseCode == null) throw new IllegalArgumentException(FailureConst.CODE_REQUIRED);
            // 设置默认详细描述
            if (detail == null) {
                detail = responseCode.getDescription();
                if (detail == null) detail = responseCode.getMessage();
                if (detail == null) detail = FailureConst.MESSAGE_OR_DESCRIPTION_REQUIRED;
            }
            // 根据上下文自动填充方法和位置信息
            FailureContext ctx = Ex.getContext();
            if (ctx != null && ctx.isShadowTrace()) {
                if (method == null) method = Ex.method();
                if (location == null) location = Ex.location();
            }
            CodeMappingConfig cfg = Ex.getContext() != null ? Ex.getContext().getCodeMappingConfig() : null;
            HttpStatus status = (cfg != null) ? cfg.resolveHttpStatus(responseCode.getCode()) : HttpStatus.INTERNAL_SERVER_ERROR;
            return new Business(responseCode, detail, method, location, status, invalidValue);
        }
    }

    /**
     * Override toString method to provide formatted exception info.
     *
     * @return Formatted string representation
     */
    @Override
    public String toString() {
        // 格式化代码为xxx_xx格式
        String codeStr = String.valueOf(responseCode.getCode()).replaceFirst("(\\d{3})(\\d{2})", "$1_$2");

        // 处理 invalidValue 快照
        String valStr = "";
        FailureContext ctx = Ex.getContext();
        if (invalidValue != null && ctx != null && ctx.isDebugSnapshot()) {
            valStr = ", val=" + maskValue(invalidValue);
        }

        // 构建基础信息字符串
        String base = "{code=%s, mes=%s, des=%s%s}".formatted(codeStr, I18n.get(responseCode.getMessage()), I18n.get(detail), valStr);
        // 根据是否有方法信息决定输出格式
        if (method == null) return base + (location != null ? " (" + extractFileLine(location) + ")" : "");

        // 处理内部类方法名 (如 TestController$AdvancedUserValidator#validate -> TestController#validate)
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

    private String maskValue(Object value) {
        if (value == null) return "null";
        String str = value.toString();
        if (str.isEmpty()) return str;

        // 手机号: 138****8888
        if (FailureConst.Mobile.matcher(str).matches()) {
            return str.substring(0, 3) + "****" + str.substring(7);
        }

        // 邮箱: a****@gmail.com（用户名全掩码更安全）
        Matcher emailMatcher = FailureConst.Email.matcher(str);
        if (emailMatcher.matches()) {
            return emailMatcher.group(1) + "****" + emailMatcher.group(3);
        }

        // 身份证/银行卡: 前4后4
        if (FailureConst.Card.matcher(str).matches()) {
            return str.substring(0, 4) + "****" + str.substring(str.length() - 4);
        }

        // 长文本截断（修复版）
        if (str.length() > 50) {
            return str.substring(0, 5) + "...(" + str.length() + "字符)..."
                    + str.substring(str.length() - 5);
        }

        return str;
    }

    /**
     * Extract filename and line number from full location info.
     *
     * @param loc Full location info string
     * @return Extracted filename and line number
     */
    private String extractFileLine(String loc) {
        // 查找左括号位置
        int start = loc.indexOf("(");
        if (start < 0) return loc;  // 如果没有找到括号，返回原字符串
        // 提取括号内的内容（去除右括号）
        String content = loc.substring(start + 1, loc.length() - 1);

        // 处理内部类文件名包含 $ 的情况 (如 TestController$AdvancedUserValidator.java:103)
        int dollarIndex = content.indexOf('$');
        if (dollarIndex > 0) {
            // 查找文件名结束的点号 (如 .java)
            int dotIndex = content.indexOf('.', dollarIndex);
            if (dotIndex > 0) {
                return content.substring(0, dollarIndex) + content.substring(dotIndex);
            }
        }
        return content;
    }
}
