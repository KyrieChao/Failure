package com.chao.failure.integration.mvc;

import com.chao.failure.annotation.Validate;
import com.chao.failure.constant.FailureConst;
import com.chao.failure.internal.core.Ex;
import com.chao.failure.exception.Business;
import com.chao.failure.exception.MultiBusiness;
import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.constant.Severity;
import com.chao.failure.internal.core.i18n.LocalizedTexts;
import com.chao.failure.internal.core.observability.OpenTelemetryBridge;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.internal.validation.ValidationEventManager;
import com.chao.failure.util.I18n;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.UUID;

/**
 * Abstract exception handler - Extensible base class.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public abstract class FailFastExceptionHandler {

    /**
     * Failure configuration properties.
     */
    private FailureProperties properties;

    @Autowired(required = false)
    public void setFailFastProperties(FailureProperties properties) {
        this.properties = properties;
    }


    /**
     * Entry method for handling single Business exception.
     *
     * @param e Business exception object
     * @return ResponseEntity response object
     */
    public ResponseEntity<?> handleBusinessException(Business e) {
        logException(e);
        return buildResponse(e);
    }

    /**
     * Entry method for handling batch business exceptions.
     *
     * @param e MultiBusiness batch exception object
     * @return ResponseEntity response object
     */
    public ResponseEntity<?> handleMultiBusinessException(MultiBusiness e) {
        logException(e);
        return buildMultiErrorResponse(e);
    }

    /**
     * Handle Spring MVC parameter validation exceptions (@Valid / @Validated).
     *
     * @param e MethodArgumentNotValidException exception object
     * @return ResponseEntity response object
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // Start method validation metrics
        String scene = getScene();
        notifyValidationStart(scene);
        long startTime = System.nanoTime();
        boolean success = false;

        try {
            BindingResult result = e.getBindingResult();
            List<Business> errors = new ArrayList<>();

            // Try to get target class information for location formatting
            Class<?> targetClass = null;
            if (result.getTarget() != null) targetClass = result.getTarget().getClass();

            // Get method name
            String methodName = "Validation";
            if (e.getParameter().getMethod() != null) {
                java.lang.reflect.Method method = e.getParameter().getMethod();
                methodName = method.getDeclaringClass().getSimpleName() + "#" + method.getName();
            }

            // Iterate through all field errors and convert to Business exceptions
            for (FieldError fieldError : result.getFieldErrors()) {
                String location = formatValidationLocation(targetClass, fieldError.getField());
                errors.add(parseError(fieldError.getDefaultMessage(), location, methodName));
            }

            // Check if method has @Validate annotation to control fail-fast behavior
            boolean failFast = true;
            if (e.getParameter().getMethod() != null) {
                Validate validate = e.getParameter().getMethod().getAnnotation(Validate.class);
                if (validate != null) {
                    failFast = validate.fast();
                }
            }

            // If fail-fast mode and multiple errors, keep only the first one
            if (failFast && errors.size() > 1) {
                Business first = errors.get(0);
                errors.clear();
                errors.add(first);
            }

            return handleMultiErrors(errors);
        } finally {
            // End method validation metrics
            long duration = System.nanoTime() - startTime;
            notifyValidationEnd(duration, success);
            notifyValidationFailure(String.valueOf(ResponseCode.VALIDATION_ERROR_400.getCode()));
        }
    }

    /**
     * Handle Bean Validation exceptions (ConstraintViolationException).
     *
     * @param e ConstraintViolationException exception object
     * @return ResponseEntity response object
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        String scene = getScene();
        notifyValidationStart(scene);
        long startTime = System.nanoTime();
        boolean success = false;

        try {
            List<Business> errors = new ArrayList<>();
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                String location = formatValidationLocation(violation.getRootBeanClass(), violation.getPropertyPath().toString());
                String methodName = "Validation";
                if (violation.getRootBeanClass() != null) {
                    String className = violation.getRootBeanClass().getSimpleName();
                    String path = violation.getPropertyPath().toString();
                    String methodPart = path.split("\\.")[0];
                    methodName = className + "#" + methodPart;
                }

                errors.add(parseError(violation.getMessage(), location, methodName));
            }
            errors.sort(Comparator.comparingInt(b -> b.getResponseCode().getCode()));
            return handleMultiErrors(errors);
        } finally {
            long duration = System.nanoTime() - startTime;
            notifyValidationEnd(duration, success);
            notifyValidationFailure(String.valueOf(com.chao.failure.internal.core.ResponseCode.VALIDATION_ERROR_400.getCode()));
        }
    }

    /**
     * Build HTTP response body for single exception.
     *
     * @param e Business exception object
     * @return ResponseEntity response object
     */
    protected ResponseEntity<?> buildResponse(Business e) {
        Object body = buildBody(e);
        return ResponseEntity.status(e.getHttpStatus()).body(body);
    }

    /**
     * Build HTTP response body for batch exceptions.
     *
     * @param e MultiBusiness batch exception object
     * @return ResponseEntity response object
     */
    protected ResponseEntity<?> buildMultiErrorResponse(MultiBusiness e) {
        Object body = buildBody(e);
        return ResponseEntity.status(e.getHttpStatus()).body(body);
    }

    protected Object buildBody(Business e) {
        return buildMap(e);
    }

    protected Object buildBody(MultiBusiness e) {
        return buildMultiMap(e);
    }

    /**
     * Check if verbose mode is enabled.
     *
     * @return true if verbose mode is enabled
     */
    private boolean isVerbose() {
        return properties != null && properties.isVerbose();
    }

    /**
     * Unified handling of multiple validation errors.
     *
     * @param errors List of Business errors
     * @return ResponseEntity response object
     */
    private ResponseEntity<?> handleMultiErrors(List<Business> errors) {
        if (errors.isEmpty()) {
            return buildResponse(Business.of(ResponseCode.VALIDATION_ERROR, FailureConst.VALIDATION_ERROR));
        }
        if (errors.size() == 1) {
            Business first = errors.get(0);
            logException(first);
            return buildResponse(first);
        }
        MultiBusiness multi = new MultiBusiness(errors);
        logException(multi);
        return buildMultiErrorResponse(multi);
    }

    /**
     * Common method for logging exceptions.
     *
     * @param e Business exception object to be logged
     */
    protected void logException(Business e) {
        if (e instanceof MultiBusiness m) {
            Severity severity = resolveMultiSeverity(m);
            logBySeverity(severity, "Multi Failure: {} errors", m.getErrors().size());
            for (int i = 0; i < m.getErrors().size(); i++) {
                logBySeverity(severity, "{}. {}", i + 1, renderLogMessage(m.getErrors().get(i)));
            }
        } else {
            logBySeverity(e.getSeverity(), "{}", renderLogMessage(e));
        }
    }

    private String renderLogMessage(Business e) {
        if (e == null) {
            return "null";
        }
        if (!isTraceIdEnabled()) {
            return e.toString();
        }
        String trace = resolveTraceId(e);
        if (trace == null || trace.isBlank()) {
            return e.toString();
        }
        return e + " [traceId=" + trace + "]";
    }

    private Severity resolveMultiSeverity(MultiBusiness multi) {
        Severity best = Severity.INFO;
        for (Business err : multi.getErrors()) {
            Severity current = err != null && err.getSeverity() != null ? err.getSeverity() : Severity.INFO;
            if (current.getWeight() > best.getWeight()) {
                best = current;
            }
        }
        return best;
    }

    private void logBySeverity(Severity severity, String pattern, Object... args) {
        Severity level = severity != null ? severity : Severity.INFO;
        if (!level.isLogRequired()) {
            return;
        }
        switch (level) {
            case DEBUG -> log.debug(pattern, args);
            case INFO -> log.info(pattern, args);
            case WARNING -> log.warn(pattern, args);
            case ERROR, CRITICAL -> log.error(pattern, args);
        }
    }

    /**
     * Format validation exception location information.
     *
     * @param clazz Target class object
     * @param fieldOrPath Field name or path
     * @return Formatted location string
     */
    private String formatValidationLocation(Class<?> clazz, String fieldOrPath) {
        if (fieldOrPath == null) return I18n.get(FailureConst.UNKNOWN_ERROR);
        String className = "";
        if (clazz != null) {
            if (clazz.getName().contains("$$")) clazz = clazz.getSuperclass();
            className = clazz.getSimpleName();
        }

        String at = I18n.get(FailureConst.AT);
        if (fieldOrPath.contains(".")) {
            int lastDot = fieldOrPath.lastIndexOf('.');
            String methodAndArg = fieldOrPath.substring(0, lastDot) + at + fieldOrPath.substring(lastDot + 1);
            if (!className.isEmpty()) {
                return className + "." + methodAndArg;
            }
            return methodAndArg;
        }
        if (!className.isEmpty()) {
            return className + at + fieldOrPath;
        }

        return fieldOrPath;
    }

    /**
     * Parse validation error message and build Business exception.
     *
     * @param message Error message string
     * @param location Error location
     * @param methodName Method name
     * @return Constructed Business exception object
     */
    private Business parseError(String message, String location, String methodName) {
        Business business;

        if (message == null || message.isBlank()) {
            business = Business.of(ResponseCode.VALIDATION_ERROR_400, FailureConst.INVALID_PARAMETER);
        } else {
            ParsedValidationMessage parsed = parseValidationMessage(message);
            if (parsed.code() != null) {
                int code = parsed.code();
                String text = parsed.text();
                if (text == null) {
                    ResponseCode base = ResponseCode.VALIDATION_ERROR_400;
                    business = Business.of(ResponseCode.of(code, base.getMessage(), base.getDescription()), FailureConst.INVALID_PARAMETER);
                } else {
                    business = Business.of(ResponseCode.of(code, text), text);
                }
            } else {
                business = Business.of(ResponseCode.VALIDATION_ERROR_400, message);
            }
        }
        if (location != null) {
            return Business.of(business.getResponseCode(), business.getDetail(), methodName, location);
        }
        return business;
    }

    private record ParsedValidationMessage(Integer code, String text) {
    }

    private ParsedValidationMessage parseValidationMessage(String raw) {
        if (raw == null) return new ParsedValidationMessage(null, null);
        String s = raw.trim();
        if (s.isEmpty()) return new ParsedValidationMessage(null, null);

        int idx = s.indexOf(':');
        if (idx > 0) {
            String prefix = s.substring(0, idx).trim();
            if (isNumeric(prefix)) {
                int code = Integer.parseInt(prefix);
                String rest = s.substring(idx + 1).trim();
                return new ParsedValidationMessage(code, rest.isEmpty() ? null : rest);
            }
        }

        if (isNumeric(s)) {
            return new ParsedValidationMessage(Integer.parseInt(s), null);
        }

        return new ParsedValidationMessage(null, s);
    }

    /**
     * Check if string is numeric.
     *
     * @param str String to be checked
     * @return True if string contains only digits, false otherwise
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }


    /**
     * Build Map object containing business response information.
     *
     * @param e Business object containing response code, message and detail
     * @return Map object containing response code, message, detail and timestamp
     */
    protected Map<String, Object> buildMap(Business e) {
        Map<String, Object> body = new HashMap<>();
        body.put(FailureConst.FIELD_CODE, e.getResponseCode().getCode());
        body.put(FailureConst.FIELD_MESSAGE, LocalizedTexts.message(e.getResponseCode()));
        body.put(FailureConst.FIELD_DESCRIPTION, LocalizedTexts.detail(e.getResponseCode(), e.getDetail()));
        if (isTraceIdEnabled()) {
            String traceId = e.getTraceId();
            if (traceId == null || traceId.isBlank()) {
                traceId = getTraceId();
            }
            if (traceId != null && !traceId.isBlank()) {
                body.put(FailureConst.FIELD_TRACE_ID, traceId);
            }
            String spanId = e.getSpanId();
            if (spanId == null || spanId.isBlank()) {
                spanId = OpenTelemetryBridge.currentSpanId();
            }
            if (spanId != null && !spanId.isBlank()) {
                body.put(FailureConst.FIELD_SPAN_ID, spanId);
            }
        }

        String scene = getScene();
        if (!scene.isBlank() && !FailureConst.DEFAULT_SCENE.equals(scene)) {
            body.put(FailureConst.FIELD_SCENE, scene);
        }
        if (isVerbose()) {
            List<Map<String, Object>> errorList = new ArrayList<>();
            Map<String, Object> errorItem = buildMapDetail(e);
            errorList.add(errorItem);
            body.put(FailureConst.FIELD_ERRORS, errorList);
        }

        String format = ZonedDateTime.now(FailureConst.CST).format(FailureConst.DEFAULT_DATETIME_FORMATTER);
        body.put(FailureConst.FIELD_TIMESTAMP, format);

        return body;
    }

    protected Map<String, Object> buildMultiMap(MultiBusiness e) {
        Map<String, Object> body = new HashMap<>();
        body.put(FailureConst.FIELD_CODE, e.getResponseCode().getCode());
        body.put(FailureConst.FIELD_MESSAGE, LocalizedTexts.message(e.getResponseCode()));
        body.put(FailureConst.FIELD_DESCRIPTION, LocalizedTexts.detail(e.getResponseCode(), e.getDetail()));
        if (isTraceIdEnabled()) {
            String traceId = resolveTraceId(e);
            if (traceId != null && !traceId.isBlank()) {
                body.put(FailureConst.FIELD_TRACE_ID, traceId);
            }
            String spanId = resolveSpanId(e);
            if (spanId != null && !spanId.isBlank()) {
                body.put(FailureConst.FIELD_SPAN_ID, spanId);
            }
        }

        String scene = getScene();
        if (!scene.isBlank() && !FailureConst.DEFAULT_SCENE.equals(scene)) {
            body.put(FailureConst.FIELD_SCENE, scene);
        }
        if (isVerbose()) {
            List<Map<String, Object>> errorList = new ArrayList<>();
            for (Business err : e.getErrors()) {
                Map<String, Object> errorItem = buildMapDetail(err);
                errorList.add(errorItem);
            }
            body.put(FailureConst.FIELD_ERRORS, errorList);
        }
        String format = ZonedDateTime.now(FailureConst.CST).format(FailureConst.DEFAULT_DATETIME_FORMATTER);
        body.put(FailureConst.FIELD_TIMESTAMP, format);

        return body;
    }

    protected Map<String, Object> buildMapDetail(Business e) {
        Map<String, Object> errorItem = new HashMap<>();
        errorItem.put(FailureConst.FIELD_CODE, e.getResponseCode().getCode());
        errorItem.put(FailureConst.FIELD_MESSAGE, LocalizedTexts.message(e.getResponseCode()));
        errorItem.put(FailureConst.FIELD_PATH, e.getPath());
        errorItem.put(FailureConst.FIELD_DETAIL, LocalizedTexts.detail(e.getResponseCode(), e.getDetail()));
        errorItem.put(FailureConst.FIELD_REJECTED, e.getInvalidValue());
        return errorItem;
    }

    private boolean isTraceIdEnabled() {
        if (properties == null) {
            return true;
        }
        FailureProperties.TraceId traceId = properties.getTraceId();
        if (traceId == null) {
            return true;
        }
        return traceId.isEnabled();
    }

    /**
     * Get traceId from context or generate a new one.
     *
     * @return TraceId
     */
    private String getTraceId() {
        FailureContext ctx = Ex.getContext();
        if (ctx != null) {
            String traceId = ctx.getTraceId();
            if (traceId != null) {
                return traceId;
            }
        }
        String otel = OpenTelemetryBridge.currentTraceId();
        if (otel != null && !otel.isBlank()) {
            return otel;
        }
        return UUID.randomUUID().toString();
    }

    private String resolveTraceId(Business business) {
        if (business != null && business.getTraceId() != null && !business.getTraceId().isBlank()) {
            return business.getTraceId();
        }
        return getTraceId();
    }

    private String resolveSpanId(Business business) {
        if (business != null && business.getSpanId() != null && !business.getSpanId().isBlank()) {
            return business.getSpanId();
        }
        return OpenTelemetryBridge.currentSpanId();
    }

    /**
     * Get scene from context.
     *
     * @return Scene
     */
    private String getScene() {
        FailureContext ctx = Ex.getContext();
        if (ctx != null) {
            String scene = ctx.getScene();
            if (scene != null) {
                return scene;
            }
        }
        return FailureConst.DEFAULT_SCENE;
    }


    /**
     * Notify observer of validation start.
     *
     * @param scene validation scene
     */
    private void notifyValidationStart(String scene) {
        ValidationEventManager.notifyStart(FailureConst.FIELD_METHOD, scene);
    }

    /**
     * Notify observer of validation end.
     *
     * @param durationNanos duration in nanoseconds
     * @param success       whether validation was successful
     */
    private void notifyValidationEnd(long durationNanos, boolean success) {
        ValidationEventManager.notifyEnd(FailureConst.FIELD_METHOD, durationNanos, success);
    }

    /**
     * Notify observer of validation failure.
     *
     * @param errorCode error code
     */
    private void notifyValidationFailure(String errorCode) {
        ValidationEventManager.notifyFailure(FailureConst.FIELD_METHOD, errorCode);
    }
}
