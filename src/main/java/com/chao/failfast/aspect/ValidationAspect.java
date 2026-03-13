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
import org.springframework.beans.factory.ObjectProvider;
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
 * Validation aspect - Handle custom validators declared by @Validate annotation.
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
@Slf4j
@Aspect
@Component
@Order(100)
public class ValidationAspect {

    /**
     * Use ConcurrentHashMap as cache to store validator instances.
     */
    private static final ConcurrentHashMap<Class<? extends FastValidator<Object>>, FastValidator<Object>> VALIDATOR_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<? extends FastValidator<Object>>, ValidatorFactory> VALIDATOR_FACTORY_CACHE = new ConcurrentHashMap<>();

    /**
     * Define an immutable Set containing types to skip validation.
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
     * Collect parameters that need validation (filter null, @SkipValidation, container types).
     *
     * @param point Join point
     * @return List of validatable arguments
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
     * Execute all validators and collect errors.
     *
     * @param validatorClasses Validator classes
     * @param args             Arguments to validate
     * @param failFast         Whether to fail fast
     * @return List of Business errors
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
     * Execute single validator.
     *
     * @param validator Validator instance
     * @param args      Arguments to validate
     * @param failFast  Whether to fail fast
     * @return List of Business errors
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
     * Execute TypedValidator.
     *
     * @param validator TypedValidator instance
     * @param args      Arguments to validate
     * @param ctx       Validation context
     */
    private void executeTypedValidator(TypedValidator validator, List<Object> args, FastValidator.ValidationContext ctx) {
        for (Object arg : args) {
            validator.validateIfRegistered(arg, ctx);
            if (ctx.isStopped()) break;
        }
    }

    /**
     * Execute plain FastValidator.
     *
     * @param validator FastValidator instance
     * @param args      Arguments to validate
     * @param ctx       Validation context
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
     * Get or create a validator instance.
     *
     * @param clazz Validator class object
     * @return Validator instance, get from application context if exists, otherwise create new instance
     */
    @SuppressWarnings("unchecked")
    private FastValidator<Object> getOrCreateValidator(Class<? extends FastValidator> clazz) {
        Class<? extends FastValidator<Object>> type = (Class<? extends FastValidator<Object>>) clazz;
        ValidatorFactory factory = VALIDATOR_FACTORY_CACHE.computeIfAbsent(type, this::buildValidatorFactory);
        return factory.get();
    }

    private ValidatorFactory buildValidatorFactory(Class<? extends FastValidator<Object>> type) {
        ObjectProvider<FastValidator<Object>> provider = applicationContext != null
                ? (ObjectProvider<FastValidator<Object>>) applicationContext.getBeanProvider((Class<?>) type)
                : null;
        if (provider != null) {
            FastValidator<Object> bean = provider.getIfAvailable();
            if (bean != null) {
                return provider::getObject;
            }
        }
        if (applicationContext != null) {
            String[] names = applicationContext.getBeanNamesForType(type);
            if (names != null && names.length > 0) {
                return () -> applicationContext.getBean(type);
            }
        }
        return () -> VALIDATOR_CACHE.computeIfAbsent(type, this::newValidatorInstance);
    }

    private FastValidator<Object> newValidatorInstance(Class<? extends FastValidator<Object>> key) {
        try {
            return key.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate validator: " + key.getName(), e);
        }
    }

    private interface ValidatorFactory {
        FastValidator<Object> get();
    }

    /**
     * Get type supported by validator.
     *
     * @param validator Validator instance
     * @return Supported type, return Object.class if undetermined
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
     * Determine if the given class should be skipped.
     *
     * @param clazz Class to check
     * @return True if class is in SKIP_TYPES list or its superclass/interface is in SKIP_TYPES list, false otherwise
     */
    private boolean shouldSkip(Class<?> clazz) {
        return SKIP_TYPES.stream().anyMatch(t -> t.isAssignableFrom(clazz));
    }

    /**
     * Check if annotation array contains SkipValidation annotation.
     *
     * @param annotations Array of annotations to check
     * @return True if contains SkipValidation annotation, false otherwise
     */
    private boolean hasSkipAnnotation(Annotation[] annotations) {
        if (annotations == null) return false;
        for (Annotation ann : annotations) {
            if (ann instanceof SkipValidation) return true;
        }
        return false;
    }
}
