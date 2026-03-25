package com.chao.failfast.aspect;

import com.chao.failfast.annotation.*;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.constant.Scenario;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.Ex;
import com.chao.failfast.internal.MultiBusiness;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.internal.policy.DefaultErrorPolicy;
import com.chao.failfast.internal.policy.ErrorPolicy;
import com.chao.failfast.util.I18n;
import com.chao.failfast.validator.TypedValidator;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
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
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validation aspect - Handle custom validators declared by @Validate annotation.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
@Slf4j
@Aspect
@Component
@Order(100)
@ToImprove(value = "太长了 后续再做优化",version = "1.2.0",tag = "1.8.0")
public class ValidationAspect {

    /**
     * Use ConcurrentHashMap as cache to store validator instances.
     */
    private static final ConcurrentHashMap<Class<? extends FastValidator<Object>>, FastValidator<Object>> VALIDATOR_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<? extends FastValidator<Object>>, ValidatorFactory> VALIDATOR_FACTORY_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for fields to avoid reflection overhead.
     */
    private static final ConcurrentHashMap<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for scene values to avoid repeated annotation reading.
     */
    private static final ConcurrentHashMap<Field, Set<Scenario>> SCENE_CACHE = new ConcurrentHashMap<>();

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

    @Autowired(required = false)
    private Validator validator;

    @Around("@annotation(validate)")
    public Object around(ProceedingJoinPoint point, Validate validate) throws Throwable {
        // 1. 读取注解参数
        Scenario[] scenes = validate.scene();
        Class<?>[] groups = validate.groups();
        boolean fast = validate.fast();
        Class<? extends FastValidator>[] validatorClasses = validate.value();

        // 确保 scenes 数组不为空
        if (scenes == null || scenes.length == 0) {
            scenes = new Scenario[]{Scenario.DEFAULT};
        }
        if (groups == null) {
            groups = new Class<?>[0];
        }
        if (validatorClasses == null) {
            validatorClasses = new Class[0];
        }

        FailureContext ctx = Ex.getContext();
        String originalScene = ctx != null ? ctx.getScene() : null;
        boolean sceneApplied = false;

        String sceneName = toSceneName(scenes);
        if ((ctx != null) && !FailureConst.DEFAULT_SCENE.equals(sceneName)) {
            ctx.setScene(sceneName);
            sceneApplied = true;
        }

        boolean success = false;
        try {
            if (validatorClasses.length == 0 && groups.length == 0 && scenes[0] == Scenario.DEFAULT) {
                return point.proceed();
            }

            // 2. 收集可校验参数
            List<Object> validatableArgs = collectValidatableArgs(point);

            // 3. 第一步（bridge）：执行 jakarta Validator 验证
            boolean shouldRunBridge;
            if (groups.length > 0) {
                shouldRunBridge = true;
            } else if (scenes.length > 1) {
                shouldRunBridge = true;
            } else {
                shouldRunBridge = scenes[0] != Scenario.DEFAULT;
            }
            List<Business> errors = shouldRunBridge ? executeBridgeValidation(validatableArgs, groups, fast, scenes) : new ArrayList<>();

            // 4. 第二步（自定义）：执行 FastValidator 验证
            if (validatorClasses.length > 0 && (errors.isEmpty() || !fast)) {
                List<Business> customErrors = executeValidators(validatorClasses, validatableArgs, fast, scenes, groups);
                errors.addAll(customErrors);
            }

            // 5. 处理错误
            if (!errors.isEmpty()) throw errors.size() == 1 ? errors.get(0) : new MultiBusiness(errors);

            Object result = point.proceed();
            success = true;
            return result;
        } finally {
            if (sceneApplied && success) {
                ctx.setScene(originalScene != null ? originalScene : FailureConst.DEFAULT_SCENE);
            }
        }
    }

    private String toSceneName(Scenario[] scenes) {
        if (scenes == null || scenes.length == 0) return FailureConst.DEFAULT_SCENE;
        StringBuilder sb = new StringBuilder();
        for (Scenario s : scenes) {
            if (s == null || s == Scenario.DEFAULT) continue;
            if (!sb.isEmpty()) sb.append(',');
            sb.append(s.name());
        }
        return sb.isEmpty() ? FailureConst.DEFAULT_SCENE : sb.toString();
    }

    /**
     * Execute bridge validation using jakarta Validator.
     *
     * @param args   Validatable arguments
     * @param groups Validation groups
     * @param fast   Whether to fail fast
     * @param scenes Business scenes
     * @return List of Business errors
     */
    private List<Business> executeBridgeValidation(List<Object> args, Class<?>[] groups, boolean fast, Scenario[] scenes) {
        List<Business> errors = new ArrayList<>();
        if (validator == null) return errors;

        for (Object arg : args) {
            Set<ConstraintViolation<Object>> violations = groups.length > 0 ?
                    validator.validate(arg, groups) : validator.validate(arg);

            for (ConstraintViolation<Object> violation : violations) {
                // 场景过滤
                if (!shouldKeepViolation(violation, scenes)) {
                    continue;
                }

                String path = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                Object invalidValue = violation.getInvalidValue();

                // 按 FailureContext/ErrorPolicy 决定是否写入 invalidValue
                String location = formatValidationLocation(violation.getRootBeanClass(), path);
                Business.Fabricator fabricator = Business.compose()
                        .responseCode(ResponseCode.VALIDATION_ERROR_400)
                        .detail(message)
                        .location(location)
                        .path(path);

                // 检查是否需要捕获 invalidValue
                FailureContext ctx = Ex.getContext();
                ErrorPolicy policy = ctx != null ? java.util.Objects.requireNonNullElse(ctx.getErrorPolicy(), DefaultErrorPolicy.INSTANCE) : DefaultErrorPolicy.INSTANCE;
                if (invalidValue != null && policy.captureInvalidValue(ctx)) {
                    fabricator.invalidValue(invalidValue);
                }

                Business business = fabricator.materialize();
                errors.add(business);

                if (fast) break;
            }

            if (fast && !errors.isEmpty()) break;
        }

        return errors;
    }

    /**
     * Check if the violation should be kept based on scene filtering.
     *
     * @param violation Constraint violation
     * @param scenes    Business scenes
     * @return True if violation should be kept, false otherwise
     */
    private boolean shouldKeepViolation(ConstraintViolation<?> violation, Scenario[] scenes) {
        // 如果 scenes 数组为空，保留（不要吞错）
        if (scenes == null || scenes.length == 0) {
            return true;
        }

        // 如果包含默认场景，不过滤
        for (Scenario scene : scenes) {
            if (scene == Scenario.DEFAULT) {
                return true;
            }
        }

        // 拿到根 Bean 类
        Class<?> rootClass = violation.getRootBeanClass();
        // 拿到属性路径
        String propertyPath = violation.getPropertyPath().toString();
        // 解析字段名
        String fieldName = parseFieldName(propertyPath);

        // 查找字段
        Field field = findField(rootClass, fieldName);
        if (field == null) {
            // 找不到字段，保留（不要吞错）
            return true;
        }

        // 检查字段上的 @Scene 注解
        Set<Scenario> sceneSet = getSceneValues(field);
        if (sceneSet.isEmpty()) {
            // 没标 @Scene，保留
            return true;
        }

        // 标了 @Scene，检查是否包含任一当前场景
        for (Scenario scene : scenes) {
            if (sceneSet.contains(scene)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Parse field name from property path.
     *
     * @param propertyPath Property path
     * @return Field name
     */
    private String parseFieldName(String propertyPath) {
        // 分割 . 取第一段
        int dotIndex = propertyPath.indexOf('.');
        String firstPart = dotIndex > 0 ? propertyPath.substring(0, dotIndex) : propertyPath;
        // 去掉下标，比如把 items[0] 变成 items
        int bracketIndex = firstPart.indexOf('[');
        return bracketIndex > 0 ? firstPart.substring(0, bracketIndex) : firstPart;
    }

    /**
     * Find field by name, including superclasses.
     *
     * @param clazz     Class to search
     * @param fieldName Field name
     * @return Field if found, null otherwise
     */
    private Field findField(Class<?> clazz, String fieldName) {
        Map<String, Field> fieldMap = FIELD_CACHE.computeIfAbsent(clazz, c -> {
            Map<String, Field> map = new ConcurrentHashMap<>();
            ReflectionUtils.doWithFields(c, field -> map.put(field.getName(), field));
            return map;
        });
        return fieldMap.get(fieldName);
    }

    /**
     * Get scene values from field annotation.
     *
     * @param field Field
     * @return Set of scene values
     */
    private Set<Scenario> getSceneValues(Field field) {
        return SCENE_CACHE.computeIfAbsent(field, f -> {
            Scene sceneAnnotation = f.getAnnotation(Scene.class);
            if (sceneAnnotation == null) {
                return Set.of();
            }
            return new HashSet<>(Set.of(sceneAnnotation.value()));
        });
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
            if (i < paramAnnotations.length && hasSkipAnnotation(paramAnnotations[i])) continue;
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
     * @param scenes           Business scenes
     * @param groups           Validation groups
     * @return List of Business errors
     */
    private List<Business> executeValidators(Class<? extends FastValidator>[] validatorClasses, List<Object> args,
                                             boolean failFast, Scenario[] scenes, Class<?>[] groups) {
        List<Business> errors = new ArrayList<>();

        for (Class<? extends FastValidator> validatorClass : validatorClasses) {
            FastValidator<Object> validator = getOrCreateValidator(validatorClass);
            List<Business> validatorErrors = executeSingleValidator(validator, args, failFast, scenes, groups);
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
     * @param scenes    Business scenes
     * @param groups    Validation groups
     * @return List of Business errors
     */
    private List<Business> executeSingleValidator(FastValidator<Object> validator, List<Object> args, boolean failFast, Scenario[] scenes, Class<?>[] groups) {

        // 确保 scenes 数组不为空
        if (scenes == null || scenes.length == 0) {
            scenes = new Scenario[]{Scenario.DEFAULT};
        }
        
        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(failFast, scenes, groups);

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
//        if (supportedType == Object.class) {
//            log.warn(I18n.get("log.validation.aspect.validator.type.unknown", validator.getClass().getSimpleName()));
//            return;
//        }

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

    @SuppressWarnings("unchecked")
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

    /**
     * Format validation exception location information.
     *
     * @param clazz Target class object
     * @param fieldOrPath Field name or path
     * @return Formatted location string
     */
    private String formatValidationLocation(Class<?> clazz, String fieldOrPath) {
        if (fieldOrPath == null) return "";

        String className = "";
        if (clazz != null) {
            // 处理 CGLIB 代理类，获取原始类名
            if (clazz.getName().contains("$$")) clazz = clazz.getSuperclass();
            className = clazz.getSimpleName();
        }

        // 如果是 Bean 校验 (e.g. UserDTO 的 age 字段)
        if (!className.isEmpty()) {
            return className + " at " + fieldOrPath;
        }

        return fieldOrPath;
    }
}
