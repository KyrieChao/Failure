package com.chao.failure.integration.mvc;

import com.chao.failure.constant.FailureConst;
import com.chao.failure.exception.Business;
import com.chao.failure.exception.MultiBusiness;
import com.chao.failure.util.I18n;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Default exception handler - Enhanced version.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@ConditionalOnMissingBean(FailFastExceptionHandler.class)
public final class DefaultExceptionHandler extends FailFastExceptionHandler {

    /**
     * Handle single Business exception.
     *
     * @param e Business exception object
     * @return ResponseEntity response object
     */
    @Override
    @ExceptionHandler(Business.class)
    public ResponseEntity<?> handleBusinessException(Business e) {
        return super.handleBusinessException(e);
    }

    /**
     * Handle batch Business exceptions.
     *
     * @param e MultiBusiness exception object
     * @return ResponseEntity response object
     */
    @Override
    @ExceptionHandler(MultiBusiness.class)
    public ResponseEntity<?> handleMultiBusinessException(MultiBusiness e) {
        return super.handleMultiBusinessException(e);
    }

    /**
     * Handle Spring MVC parameter validation exceptions.
     *
     * @param e MethodArgumentNotValidException exception object
     * @return ResponseEntity response object
     */
    @Override
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return super.handleMethodArgumentNotValidException(e);
    }

    /**
     * Handle Bean Validation constraint violation exceptions.
     *
     * @param e ConstraintViolationException exception object
     * @return ResponseEntity response object
     */
    @Override
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        return super.handleConstraintViolationException(e);
    }

    /**
     * Handle form binding exception (BindException).
     *
     * @param e BindException exception object
     * @return ResponseEntity response object, returning 500 status code
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBindException(BindException e) {
        Map<String, Object> body = new HashMap<>();
        body.put(FailureConst.FIELD_CODE, FailureConst.SYSTEM_CODE);
        body.put(FailureConst.FIELD_MESSAGE, I18n.get(FailureConst.DEFAULT_MESSAGE));
        String description = e.getAllErrors().isEmpty() ? I18n.get(FailureConst.UNKNOWN_ERROR) : e.getAllErrors().get(0).getDefaultMessage();
        body.put(FailureConst.FIELD_DESCRIPTION, I18n.get(description));
        String format = ZonedDateTime.now(FailureConst.CST).format(FailureConst.DEFAULT_DATETIME_FORMATTER);
        body.put(FailureConst.FIELD_TIMESTAMP, format);
        return ResponseEntity.badRequest().body(body);
    }
}
