package com.chao.failfast.aspect;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.annotation.SkipValidation;
import com.chao.failfast.annotation.Validate;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.MultiBusiness;
import com.chao.failfast.util.I18n;
import com.chao.failfast.validator.TypedValidator;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证切面 - 处理 @Validate 注解声明的自定义验证器
 * 当前只保留最核心功能：
 * - value：指定自定义验证器
 * - fast：是否快速失败（默认 true）
 */
@Slf4j
@Aspect
@Component
@Order(100)
public class ValidationAspect {

    /**
     * 使用ConcurrentHashMap作为缓存，存储验证器实例
     */
    private static final ConcurrentHashMap<Class<? extends FastValidator<Object>>, FastValidator<Object>> VALIDATOR_CACHE = new ConcurrentHashMap<>();

    /**
     * 定义一个不可变的Set集合，包含需要跳过验证的类型
     */
    private static final Set<Class<?>> SKIP_TYPES = Set.of(
            ServletRequest.class,  // 服务请求对象
            ServletResponse.class, // 服务响应对象
            HttpSession.class,     // HTTP会话对象
            MultipartFile.class,   // 多部分文件对象
            InputStream.class,     // 输入流
            OutputStream.class,    // 输出流
            Reader.class,          // 字符读取流
            Writer.class          // 字符写入流
    );

    @Autowired
    private ApplicationContext applicationContext;

    @Around("@annotation(validate)")
    public Object around(ProceedingJoinPoint point, Validate validate) throws Throwable {
        if (validate.value().length == 0) return point.proceed();

        // 1. 收集可校验参数
        List<Object> validatableArgs = collectValidatableArgs(point);
        if (validatableArgs.isEmpty()) return point.proceed();

        // 2. 执行所有验证器
        List<Business> errors = executeValidators(validate.value(), validatableArgs, validate.fast());

        // 3. 处理错误
        if (!errors.isEmpty()) throw errors.size() == 1 ? errors.get(0) : new MultiBusiness(errors);

        return point.proceed();
    }

    /**
     * 收集需要校验的参数（过滤 null、@SkipValidation、容器类型）
     */
    private List<Object> collectValidatableArgs(ProceedingJoinPoint point) {
        Object[] args = point.getArgs();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Annotation[][] paramAnnotations = signature.getMethod().getParameterAnnotations();

        List<Object> result = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) continue;
            if (hasSkipAnnotation(paramAnnotations[i])) continue;
            if (shouldSkip(arg.getClass())) continue;
            result.add(arg);
        }
        return result;
    }

    /**
     * 执行所有验证器，收集错误
     */
    private List<Business> executeValidators(Class<? extends FastValidator>[] validatorClasses, List<Object> args, boolean failFast) {
        List<Business> errors = new ArrayList<>();

        for (Class<? extends FastValidator> validatorClass : validatorClasses) {
            FastValidator<Object> validator = getOrCreateValidator(validatorClass);
            List<Business> validatorErrors = executeSingleValidator(validator, args, failFast);
            errors.addAll(validatorErrors);
            if (failFast && !errors.isEmpty()) break;
        }

        return errors;
    }

    /**
     * 执行单个验证器
     */
    private List<Business> executeSingleValidator(FastValidator<Object> validator, List<Object> args, boolean failFast) {

        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(failFast);

        if (validator instanceof TypedValidator typed) {
            executeTypedValidator(typed, args, ctx);
        } else {
            executePlainValidator(validator, args, ctx);
        }
        return ctx.isValid() ? List.of() : ctx.hasCauses();
    }

    /**
     * 执行 TypedValidator（多类型）
     */
    private void executeTypedValidator(TypedValidator validator, List<Object> args, FastValidator.ValidationContext ctx) {
        Set<Class<?>> registeredTypes = validator.getRegisteredTypes();

        for (Object arg : args) {
            if (!registeredTypes.contains(arg.getClass())) continue;
            validator.validate(arg, ctx);
            if (ctx.isStopped()) break;
        }
    }

    /**
     * 执行普通 FastValidator（单类型）
     */
    private void executePlainValidator(FastValidator<Object> validator, List<Object> args, FastValidator.ValidationContext ctx) {

        Class<?> supportedType = getValidatorSupportedType(validator);
        if (supportedType == Object.class) {
            log.warn(I18n.get("log.validation.aspect.validator.type.unknown", validator.getClass().getSimpleName()));
            return;
        }

        for (Object arg : args) {
            if (!supportedType.isAssignableFrom(arg.getClass())) continue;

            validator.validate(arg, ctx);
            if (ctx.isStopped()) break;
        }
    }

    /**
     * 获取或创建一个验证器实例
     *
     * @param clazz 验证器的类对象
     * @return 返回验证器实例，如果已存在则从应用上下文中获取，否则创建新实例
     */
    @SuppressWarnings("unchecked")
    private FastValidator<Object> getOrCreateValidator(Class<? extends FastValidator> clazz) {
        if (applicationContext.getBeanNamesForType(clazz).length > 0) {
            return applicationContext.getBean(clazz);
        }
        return VALIDATOR_CACHE.computeIfAbsent(
                (Class<? extends FastValidator<Object>>) clazz,  // 类型转换，确保类型安全
                key -> {
                    try {
                        return key.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to instantiate validator: " + key.getName(), e);
                    }
                });
    }

    /**
     * 获取验证器支持的类型
     *
     * @param validator 验证器实例
     * @return 支持的类型，如果无法确定则返回Object.class
     */
    private Class<?> getValidatorSupportedType(FastValidator<?> validator) {
        Class<?> declared = validator.getSupportedType();
        if (declared != null && declared != Object.class) {
            return declared;
        }
        // 如果无法从声明中获取类型，则通过反射推断泛型类型
        Class<?> type = GenericTypeResolver.resolveTypeArgument(validator.getClass(), FastValidator.class);
        return type != null ? type : Object.class;
    }

    /**
     * 判断给定的类是否应该被跳过
     *
     * @param clazz 需要检查的类
     * @return 如果类在SKIP_TYPES列表中或其父类/接口在SKIP_TYPES列表中，则返回true；否则返回false
     */
    private boolean shouldSkip(Class<?> clazz) {
        return SKIP_TYPES.stream().anyMatch(t -> t.isAssignableFrom(clazz));
    }

    /**
     * 检查注解数组中是否包含SkipValidation注解
     *
     * @param annotations 需要检查的注解数组
     * @return 如果包含SkipValidation注解则返回true，否则返回false
     */
    private boolean hasSkipAnnotation(Annotation[] annotations) {
        if (annotations == null) return false;
        for (Annotation ann : annotations) {
            if (ann instanceof SkipValidation) return true;
        }
        return false;
    }
}