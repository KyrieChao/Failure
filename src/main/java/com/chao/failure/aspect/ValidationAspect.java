package com.chao.failure.aspect;

import com.chao.failure.validator.FastValidator;
import com.chao.failure.annotation.SkipValidation;
import com.chao.failure.annotation.ToImprove;
import com.chao.failure.annotation.Validate;
import com.chao.failure.constant.FailureConst;
import com.chao.failure.constant.Scenario;
import com.chao.failure.exception.Business;
import com.chao.failure.exception.MultiBusiness;
import com.chao.failure.integration.webflux.ReactiveTrace;
import com.chao.failure.internal.core.Ex;
import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.internal.policy.DefaultErrorPolicy;
import com.chao.failure.internal.policy.ErrorPolicy;
import com.chao.failure.spi.filter.SkipTypeRegistry;
import com.chao.failure.spi.validation.ValidatorRegistry;
import com.chao.failure.spi.validation.ValidatorWhitelistRegistry;
import com.chao.failure.util.ReflectionCache;
import com.chao.failure.validator.TypedValidator;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validation aspect - Handle custom validators declared by @Validate annotation.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
@Slf4j
@Aspect
@Component
@Order(100)
@ToImprove(value = "Too long, need optimization later", version = "1.2.0", tag = "1.8.0")
public class ValidationAspect {

    /**
     * Use ConcurrentHashMap as cache to store validator instances.
     */
    private static final ConcurrentHashMap<Class<? extends FastValidator<Object>>, FastValidator<Object>> VALIDATOR_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<? extends FastValidator<Object>>, ValidatorFactory> VALIDATOR_FACTORY_CACHE = new ConcurrentHashMap<>();
    private static final Set<Class<?>> DEFAULT_SKIP_TYPES = Set.of(
            ServletRequest.class,
            ServletResponse.class,
            HttpSession.class,
            MultipartFile.class,
            BindingResult.class,
            Validator.class,
            InputStream.class,
            OutputStream.class,
            Reader.class,
            Writer.class
    );

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private Validator validator;

    @Autowired(required = false)
    private SkipTypeRegistry skipTypeRegistry;

    @Autowired(required = false)
    private ValidatorRegistry validatorRegistry;

    @Autowired(required = false)
    private ValidatorWhitelistRegistry validatorWhitelistRegistry;

    @Around("@annotation(validate)")
    public Object around(ProceedingJoinPoint point, Validate validate) throws Throwable {
        // 1. Read annotation parameters
        Scenario[] scenes = validate.scene();
        Class<?>[] groups = validate.groups();
        boolean fast = validate.fast();
        Class<? extends FastValidator>[] validatorClasses = validate.value();

        // Ensure scenes array is not empty
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
        String restoreScene = originalScene != null ? originalScene : FailureConst.DEFAULT_SCENE;
        boolean sceneApplied = false;

        String sceneName = toSceneName(scenes);
        if ((ctx != null) && !FailureConst.DEFAULT_SCENE.equals(sceneName)) {
            ctx.setScene(sceneName);
            sceneApplied = true;
        }

        boolean success = false;
        try {
            if (validatorClasses.length == 0 && groups.length == 0 && scenes[0] == Scenario.DEFAULT && validatorRegistry == null) {
                return point.proceed();
            }

            // 2. Collect validatable parameters
            List<Object> validatableArgs = collectValidatableArgs(point);

            // 3. Step 1 (bridge): Execute jakarta Validator validation
            boolean shouldRunBridge;
            if (groups.length > 0) {
                shouldRunBridge = true;
            } else if (scenes.length > 1) {
                shouldRunBridge = true;
            } else {
                shouldRunBridge = scenes[0] != Scenario.DEFAULT;
            }
            List<Business> errors = shouldRunBridge ? executeBridgeValidation(validatableArgs, groups, fast, scenes) : new ArrayList<>();

            // 4. Step 2 (custom): Execute FastValidator validation
            if (errors.isEmpty() || !fast) {
                List<Business> customErrors = executeValidators(validatorClasses, validatableArgs, fast, scenes, groups);
                errors.addAll(customErrors);
            }

            // 5. Handle errors
            if (!errors.isEmpty()) throw errors.size() == 1 ? errors.get(0) : new MultiBusiness(errors);

            Object result = point.proceed();
            if (sceneApplied && result instanceof Mono<?> mono) {
                return mono.contextWrite(reactorCtx -> reactorCtx.put(ReactiveTrace.SCENE_KEY, sceneName));
            }
            if (sceneApplied && result instanceof Flux<?> flux) {
                return flux.contextWrite(reactorCtx -> reactorCtx.put(ReactiveTrace.SCENE_KEY, sceneName));
            }
            return result;
        } finally {
            if (sceneApplied) {
                ctx.setScene(restoreScene);
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
                // Scene filtering
                if (!shouldKeepViolation(violation, scenes)) {
                    continue;
                }

                String path = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                Object invalidValue = violation.getInvalidValue();

                // Determine whether to write invalidValue based on FailureContext/ErrorPolicy
                String location = formatValidationLocation(violation.getRootBeanClass(), path);
                Business.Fabricator fabricator = Business.compose()
                        .responseCode(ResponseCode.VALIDATION_ERROR_400)
                        .detail(message)
                        .location(location)
                        .path(path);

                // Check if invalidValue needs to be captured
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
        // If scenes array is empty, keep (don't swallow errors)
        if (scenes == null || scenes.length == 0) {
            return true;
        }

        // If default scene is included, don't filter
        for (Scenario scene : scenes) {
            if (scene == Scenario.DEFAULT) {
                return true;
            }
        }

        // Get root Bean class
        Class<?> rootClass = violation.getRootBeanClass();
        // Get property path
        String propertyPath = violation.getPropertyPath().toString();
        // Parse field name
        String fieldName = parseFieldName(propertyPath);

        // Find field
        Field field = findField(rootClass, fieldName);
        if (field == null) {
            // Field not found, keep (don't swallow errors)
            return true;
        }

        // Check @Scene annotation on field
        Set<Scenario> sceneSet = getSceneValues(field);
        if (sceneSet.isEmpty()) {
            // No @Scene annotation, keep
            return true;
        }

        // Has @Scene, check if any current scene is included
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
        // Split by . and take first part
        int dotIndex = propertyPath.indexOf('.');
        String firstPart = dotIndex > 0 ? propertyPath.substring(0, dotIndex) : propertyPath;
        // Remove index, e.g., change items[0] to items
        int bracketIndex = firstPart.indexOf('[');
        return bracketIndex > 0 ? firstPart.substring(0, bracketIndex) : firstPart;
    }

    private Field findField(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null || fieldName.isEmpty()) return null;
        return ReflectionCache.findField(clazz, fieldName);
    }

    private Set<Scenario> getSceneValues(Field field) {
        if (field == null) return Collections.emptySet();
        return ReflectionCache.getSceneValues(field);
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
        Set<FastValidator<Object>> executedGlobalValidators = new HashSet<>();

        for (Class<? extends FastValidator> validatorClass : validatorClasses) {
            try {
                FastValidator<Object> validator = getOrCreateValidator(validatorClass);
                executedGlobalValidators.add(validator);
                List<Business> validatorErrors = executeSingleValidator(validator, args, failFast, scenes, groups);
                errors.addAll(validatorErrors);
                if (failFast && !errors.isEmpty()) break;
            } catch (Exception e) {
                // Capture validator instantiation failure exception and convert it to error
                Business error = Business.of(ResponseCode.VALIDATION_ERROR_400, "Failed to instantiate validator: " + e.getMessage());
                errors.add(error);
                if (failFast && !errors.isEmpty()) break;
            }
        }

        if (validatorRegistry != null && (errors.isEmpty() || !failFast)) {
            Map<FastValidator<Object>, List<Object>> globalValidatorArgs = new HashMap<>();
            for (Object arg : args) {
                FastValidator<Object> globalValidator = validatorRegistry.getValidator(arg.getClass());
                if (globalValidator != null && !executedGlobalValidators.contains(globalValidator)) {
                    globalValidatorArgs.computeIfAbsent(globalValidator, k -> new ArrayList<>()).add(arg);
                }
            }
            for (Map.Entry<FastValidator<Object>, List<Object>> entry : globalValidatorArgs.entrySet()) {
                try {
                    List<Business> validatorErrors = executeSingleValidator(entry.getKey(), entry.getValue(), failFast, scenes, groups);
                    errors.addAll(validatorErrors);
                    if (failFast && !errors.isEmpty()) break;
                } catch (Exception e) {
                    // Capture validator execution failure exception and convert it to error
                    Business error = Business.of(ResponseCode.VALIDATION_ERROR_400, "Failed to execute validator: " + e.getMessage());
                    errors.add(error);
                    if (failFast && !errors.isEmpty()) break;
                }
            }
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

        // Ensure scenes array is not empty
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
        if (supportedType == null) {
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
            if (validatorWhitelistRegistry != null && !validatorWhitelistRegistry.isWhitelisted((Class<? extends FastValidator<?>>) key)) {
                throw new SecurityException("Validator is not whitelisted: " + key.getName());
            }
            if (key.isInterface() || Modifier.isAbstract(key.getModifiers())) {
                throw new IllegalArgumentException("Validator type is not instantiable: " + key.getName());
            }
            java.lang.reflect.Constructor<? extends FastValidator<Object>> constructor = key.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
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
        // If type cannot be obtained from declaration, infer generic type through reflection
        Class<?> type = GenericTypeResolver.resolveTypeArgument(validator.getClass(), FastValidator.class);
        if (type != null && type != Object.class) {
            return type;
        }
        if (declared == Object.class || validator.allowObjectSupportedType()) {
            return Object.class;
        }
        String declaredReason = "getSupportedType() returned null";
        String genericReason = type == null ? "generic type cannot be resolved" : "generic type resolved to Object.class";
        log.warn("[FailFast] Skip validator {}: cannot resolve supported type ({}; {}). " +
                        "Fix: override getSupportedType() to return a concrete class, " +
                        "or override allowObjectSupportedType() to return true to validate all arguments.",
                validator.getClass().getName(), declaredReason, genericReason);
        return null;
    }

    /**
     * Determine if the given class should be skipped.
     *
     * @param clazz Class to check
     * @return True if class is in SKIP_TYPES list or its superclass/interface is in SKIP_TYPES list, false otherwise
     */
    private boolean shouldSkip(Class<?> clazz) {
        if (clazz == null) {
            return true;
        }
        if (skipTypeRegistry != null) {
            return skipTypeRegistry.shouldSkip(clazz);
        }
        for (Class<?> skip : DEFAULT_SKIP_TYPES) {
            if (skip.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
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
            if (clazz.getName().contains("$$")) clazz = clazz.getSuperclass();
            className = clazz.getSimpleName();
        }
        if (!className.isEmpty()) {
            return className + " at " + fieldOrPath;
        }

        return fieldOrPath;
    }
}
