package com.chao.failfast.advice;

import com.chao.failfast.annotation.ToImprove;
import com.chao.failfast.annotation.Validate;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.MultiBusiness;
import com.chao.failfast.internal.core.FailureProperties;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.util.I18n;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象异常处理器 - 可扩展的基础类
 * 提供统一的异常处理框架，支持Business异常和多异常处理
 * 子类可以通过重写方法来自定义响应格式和处理逻辑
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public abstract class FailFastExceptionHandler {

    /**
     * Fail-Fast配置属性
     * 通过Setter注入，避免构造函数注入导致子类必须调用super
     */
    private FailureProperties properties;

    @Autowired(required = false)
    public void setFailFastProperties(FailureProperties properties) {
        this.properties = properties;
    }


    /**
     * 处理单个Business异常的入口方法
     * 记录日志并构建响应
     *
     * @param e Business异常对象
     * @return ResponseEntity响应对象
     */
    public ResponseEntity<?> handleBusinessException(Business e) {
        logException(e);
        return buildResponse(e);
    }

    /**
     * 处理批量业务异常的入口方法
     * 记录日志并构建批量错误响应
     *
     * @param e MultiBusiness批量异常对象
     * @return ResponseEntity响应对象
     */
    public ResponseEntity<?> handleMultiBusinessException(MultiBusiness e) {
        logException(e);
        return buildMultiErrorResponse(e);
    }

    /**
     * 处理 Spring MVC 参数校验异常 (@Valid / @Validated)
     * 转换为统一的Business异常格式进行处理
     *
     * @param e MethodArgumentNotValidException异常对象
     * @return ResponseEntity响应对象
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        List<Business> errors = new ArrayList<>();

        // 尝试获取目标类信息用于位置格式化
        Class<?> targetClass = null;
        if (result.getTarget() != null) targetClass = result.getTarget().getClass();

        // 获取方法名
        String methodName = "Validation";
        if (e.getParameter().getMethod() != null) {
            java.lang.reflect.Method method = e.getParameter().getMethod();
            methodName = method.getDeclaringClass().getSimpleName() + "#" + method.getName();
        }

        // 遍历所有字段错误并转换为Business异常
        for (FieldError fieldError : result.getFieldErrors()) {
            String location = formatValidationLocation(targetClass, fieldError.getField());
            errors.add(parseError(fieldError.getDefaultMessage(), location, methodName));
        }

        // 检查方法上是否有 @Validate 注解来控制是否快速失败
        boolean failFast = true;
        if (e.getParameter().getMethod() != null) {
            Validate validate = e.getParameter().getMethod().getAnnotation(Validate.class);
            if (validate != null) {
                failFast = validate.fast();
            }
        }

        // 如果是快速失败模式且有多个错误，只保留第一个
        if (failFast && errors.size() > 1) {
            Business first = errors.get(0);
            errors.clear();
            errors.add(first);
        }

        return handleMultiErrors(errors);
    }

    /**
     * 处理 Bean Validation 异常 (ConstraintViolationException)
     * 主要处理方法参数级别的约束违反
     *
     * @param e ConstraintViolationException异常对象
     * @return ResponseEntity响应对象
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        List<Business> errors = new ArrayList<>();
        // 遍历所有约束违反并转换为Business异常
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            String location = formatValidationLocation(violation.getRootBeanClass(), violation.getPropertyPath().toString());

            // 尝试获取方法名
            String methodName = "Validation";
            if (violation.getRootBeanClass() != null) {
                String className = violation.getRootBeanClass().getSimpleName();
                // 尝试从 propertyPath 获取方法名 (通常是第一个节点)
                String path = violation.getPropertyPath().toString();
                String methodPart = path.split("\\.")[0];
                methodName = className + "#" + methodPart;
            }

            errors.add(parseError(violation.getMessage(), location, methodName));
        }
        return handleMultiErrors(errors);
    }

    /**
     * 构建单个异常的HTTP响应体
     * 子类可以重写此方法来自定义响应格式
     *
     * @param e Business异常对象
     * @return ResponseEntity响应对象
     */
    protected ResponseEntity<?> buildResponse(Business e) {
        Map<String, Object> body = buildMap(e);
        return ResponseEntity.status(e.getHttpStatus()).body(body);
    }

    /**
     * 构建批量异常的HTTP响应体
     * 子类可以重写此方法来自定义批量错误的响应格式
     *
     * @param e MultiBusiness批量异常对象
     * @return ResponseEntity响应对象
     */
    protected ResponseEntity<?> buildMultiErrorResponse(MultiBusiness e) {
        Map<String, Object> body = buildMap(e);
        // 只有开启verbose模式才返回errors详情
        if (properties != null && properties.isVerbose()) {
            body.put(FailureConst.FIELD_ERRORS, e.getErrors().stream()
                    .map(err -> {
                        Map<String, String> item = new HashMap<>(2);
                        item.put(FailureConst.FIELD_MESSAGE, I18n.get(err.getMessage()));
                        item.put(FailureConst.FIELD_DESCRIPTION, I18n.get(err.getResponseCode().getDescription()));
                        item.put(FailureConst.FIELD_DETAIL, I18n.get(err.getDetail()));
                        return item;
                    })
                    .toList()
            );
        }
        // 将所有错误简要拼接到 description 中，以便前端展示
        String description = I18n.get(FailureConst.VALIDATION_ERROR_PREFIX)
                           + e.getErrors().size() 
                           + I18n.get(FailureConst.ERROR_ITEM_SUFFIX);
        body.put(FailureConst.FIELD_DESCRIPTION, description);
        return ResponseEntity.status(e.getHttpStatus()).body(body);
    }

    /**
     * 统一处理多个验证错误
     * 根据错误数量决定返回单个错误还是批量错误响应
     *
     * @param errors Business错误列表
     * @return ResponseEntity响应对象
     */
    private ResponseEntity<?> handleMultiErrors(List<Business> errors) {
        // 处理空错误列表的情况
        if (errors.isEmpty()) {
            return buildResponse(Business.of(ResponseCode.VALIDATION_ERROR, FailureConst.VALIDATION_ERROR));
        }

        // 单个错误：使用单错误处理逻辑
        if (errors.size() == 1) {
            Business first = errors.get(0);
            logException(first);
            return buildResponse(first);
        }

        // 多个错误：构建批量错误对象
        MultiBusiness multi = new MultiBusiness(errors);
        logException(multi);
        return buildMultiErrorResponse(multi);
    }

    /**
     * 记录异常日志的通用方法
     * 根据异常类型决定日志记录格式
     *
     * @param e 要记录的Business异常对象
     */
    protected void logException(Business e) {
        if (e instanceof MultiBusiness m) {
            // 批量异常：记录错误数量和每个具体错误
            log.error("Multi Failure: {} errors", m.getErrors().size());
            for (int i = 0; i < m.getErrors().size(); i++) {
                log.error("{}. {}", i + 1, m.getErrors().get(i).toString());
            }
        } else {
            // 单个异常：直接记录异常信息
            log.error("Failure :{}", e.toString());
        }
    }

    /**
     * 格式化校验异常的位置信息
     * 统一处理不同类型的校验错误位置格式
     * 格式示例:
     * - Bean校验: UserDTO at age
     * - 方法参数校验: TestController.annoSimple at name
     *
     * @param clazz       目标类对象
     * @param fieldOrPath 字段名或路径
     * @return 格式化后的位置字符串
     */
    private String formatValidationLocation(Class<?> clazz, String fieldOrPath) {
        if (fieldOrPath == null) return I18n.get(FailureConst.UNKNOWN_ERROR);

        String className = "";
        if (clazz != null) {
            // 处理 CGLIB 代理类，获取原始类名
            if (clazz.getName().contains("$$")) clazz = clazz.getSuperclass();
            className = clazz.getSimpleName();
        }
        
        String at = I18n.get(FailureConst.AT);
        
        // 如果是方法参数校验 (e.g. annoSimple.name)，将最后一个点替换为 " at "
        if (fieldOrPath.contains(".")) {
            int lastDot = fieldOrPath.lastIndexOf('.');
            String methodAndArg = fieldOrPath.substring(0, lastDot) + at + fieldOrPath.substring(lastDot + 1);
            if (!className.isEmpty()) {
                return className + "." + methodAndArg;
            }
            return methodAndArg;
        }

        // 如果是 Bean 校验 (e.g. UserDTO 的 age 字段)
        if (!className.isEmpty()) {
            return className + at + fieldOrPath;
        }

        return fieldOrPath;
    }

    /**
     * 解析验证错误消息并构建成Business异常
     * 支持 "code:message" 格式的自定义错误码
     *
     * @param message    错误消息字符串
     * @param location   错误发生位置
     * @param methodName 方法名称
     * @return 构建好的Business异常对象
     */
    @ToImprove(value = "默认使用400错误码 待完善")
    private Business parseError(String message, String location, String methodName) {
        Business business;

        // 处理空消息情况
        if (message == null) {
            business = Business.of(ResponseCode.VALIDATION_ERROR_400, FailureConst.INVALID_PARAMETER);
        } else {
            // 解析 "code:message" 格式，支持自定义错误码
            String[] parts = message.split(":", 2);
            if (parts.length == 2 && isNumeric(parts[0])) {
                int code = Integer.parseInt(parts[0]);
                String msg = parts[1].trim();
                business = Business.of(ResponseCode.of(code, msg), msg);
            } else {
                // 默认使用400错误码 (参数校验错误通常是客户端问题)
                business = Business.of(ResponseCode.VALIDATION_ERROR_400, message);
            }
        }

        // 注入位置信息以提供更详细的错误上下文
        if (location != null) {
            return Business.of(business.getResponseCode(), business.getDetail(), methodName, location);
        }
        return business;
    }

    /**
     * 检查字符串是否为纯数字
     * 用于验证错误码格式
     *
     * @param str 待检查的字符串
     * @return 如果字符串只包含数字字符则返回true，否则返回false
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }


    /**
     * 构建包含业务响应信息的Map对象
     *
     * @param e 业务对象，包含响应代码、消息和详细信息
     * @return 包含响应代码、消息、详细信息和时间戳的Map对象
     */
    private Map<String, Object> buildMap(Business e) {
        Map<String, Object> body = new HashMap<>();
        body.put(FailureConst.FIELD_CODE, e.getResponseCode().getCode());
        body.put(FailureConst.FIELD_MESSAGE, I18n.get(e.getResponseCode().getMessage()));
        body.put(FailureConst.FIELD_DESCRIPTION, I18n.get(e.getDetail()));
        String format = ZonedDateTime.now(FailureConst.CST).format(FailureConst.DEFAULT_DATETIME_FORMATTER);
        body.put(FailureConst.FIELD_TIMESTAMP, format);
        return body;
    }
}
