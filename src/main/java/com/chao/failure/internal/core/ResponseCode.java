package com.chao.failure.internal.core;

/**
 * Response code interface - Support configurable HTTP status mapping.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public interface ResponseCode {

    // ==================== Framework Built-in Error Codes ====================

    /**
     * General validation error (500).
     */
    ResponseCode VALIDATION_ERROR = of(500, "{response.code.validation.error}");

    /**
     * Parameter validation failed (400).
     */
    ResponseCode VALIDATION_ERROR_400 = of(400, "{response.code.validation.error}", "{response.code.validation.error}");

    /**
     * Validation object cannot be null (500).
     */
    ResponseCode VALIDATION_ERROR_NULL = of(500, "{response.code.validation.error}", "{response.code.validation.error.null}");


    /**
     * Retry interrupted (500).
     */
    ResponseCode INTERRUPTED_ERROR = of(500, "{response.code.interrupted.error}", "{response.code.interrupted.error}");

    /**
     * Illegal argument (500).
     */
    ResponseCode ILLEGAL_ARGUMENT = of(500, "{response.code.illegal.argument}", "{response.code.illegal.argument}");

    /**
     * Default validation failed (500).
     */
    ResponseCode VALIDATION_ERROR_500 = of(500, "{response.code.validation.failed}", "{response.code.validation.failed}");
    ResponseCode VALIDATION_ERROR_500_DYNAMIC = of(500, "{response.code.validation.failed}", "{response.code.validation.failed.dynamic}");

    /**
     * Get error code value.
     *
     * @return Error code integer value
     */
    int getCode();

    /**
     * Get error message template.
     *
     * @return Error message string
     */
    String getMessage();

    /**
     * Get detailed error description.
     *
     * @return Detailed description info
     */
    String getDescription();

    /**
     * Support dynamic message template.
     *
     * @param args Formatting arguments
     * @return Formatted message
     */
    default String formatMessage(Object... args) {
        if (getMessage() == null) {
            return null; // or return default value
        }
        return String.format(getMessage(), args);
    }

    /**
     * Create simple response code (error code only).
     *
     * @param code Error code
     * @return ResponseCode instance
     */
    static ResponseCode of(int code) {
        return new Simple(code, null, null);
    }

    /**
     * Create response code (error code + message).
     *
     * @param code    Error code
     * @param message Error message
     * @return ResponseCode instance
     */
    static ResponseCode of(int code, String message) {
        return new Simple(code, message, null);
    }

    /**
     * Create complete response code (error code + message + description).
     *
     * @param code        Error code
     * @param message     Error message
     * @param description Detailed description
     * @return ResponseCode instance
     */
    static ResponseCode of(int code, String message, String description) {
        return new Simple(code, message, description);
    }

    /**
     * Simple response code implementation class.
     */
    record Simple(int code, String message, String description) implements ResponseCode {
        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }
}
