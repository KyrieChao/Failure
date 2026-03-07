package com.chao.failfast.advice;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.MultiBusiness;
import com.chao.failfast.util.I18n;
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
 * 默认异常处理器 - 增强版
 * 当用户没有自定义异常处理器时自动生效
 * 提供完整的异常处理能力，包括Business异常、验证异常等
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@ConditionalOnMissingBean(FailFastExceptionHandler.class)
public final class DefaultExceptionHandler extends FailFastExceptionHandler {

    /**
     * 处理单个Business异常
     * 直接调用父类实现，保持统一处理逻辑
     *
     * @param e Business异常对象
     * @return ResponseEntity响应对象
     */
    @Override
    @ExceptionHandler(Business.class)
    public ResponseEntity<?> handleBusinessException(Business e) {
        return super.handleBusinessException(e);
    }

    /**
     * 处理批量Business异常
     * 直接调用父类实现，保持统一处理逻辑
     *
     * @param e MultiBusiness异常对象
     * @return ResponseEntity响应对象
     */
    @Override
    @ExceptionHandler(MultiBusiness.class)
    public ResponseEntity<?> handleMultiBusinessException(MultiBusiness e) {
        return super.handleMultiBusinessException(e);
    }

    /**
     * 处理Spring MVC参数校验异常
     * 直接调用父类实现，保持统一处理逻辑
     *
     * @param e MethodArgumentNotValidException异常对象
     * @return ResponseEntity响应对象
     */
    @Override
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return super.handleMethodArgumentNotValidException(e);
    }

    /**
     * 处理Bean Validation约束违反异常
     * 直接调用父类实现，保持统一处理逻辑
     *
     * @param e ConstraintViolationException异常对象
     * @return ResponseEntity响应对象
     */
    @Override
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        return super.handleConstraintViolationException(e);
    }

    /**
     * 处理表单绑定异常（BindException）
     * 主要处理传统表单提交时的参数绑定错误
     *
     * @param e BindException异常对象
     * @return ResponseEntity响应对象，返回500状态码
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
