package com.chao.failure.internal.core;

import com.chao.failure.validator.FastValidator.ValidationContext;
import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.constant.Scenario;
import com.chao.failure.exception.Business;
import com.chao.failure.exception.MultiBusiness;
import com.chao.failure.internal.chain.*;
import com.chao.failure.internal.chain.pipeline.ChainCore;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Validation Chain - Facade class.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public final class Chain extends ChainCore<Chain> implements
        ArrayTerm<Chain>,
        IterableTerm<Chain>,
        BooleanTerm<Chain>,
        ChainTerminator<Chain>,
        ObjectTerm<Chain>,
        StringTerm<Chain>,
        NumberTerm<Chain>,
        CollectionTerm<Chain>,
        MapTerm<Chain>,
        DateTerm<Chain>,
        OptionalTerm<Chain>,
        EnumTerm<Chain>,
        IdentityTerm<Chain>,
        CustomTerm<Chain>,
        JsrTerm<Chain> {

    private static Validator validator;
    private static FailureProperties failureProperties;

    public static void setValidator(Validator validator) {
        Chain.validator = validator;
    }

    public static void setFailureProperties(FailureProperties properties) {
        Chain.failureProperties = properties;
    }

    public static Chain begin(boolean failFast) {
        return new Chain(failFast, null);
    }

    public static Chain begin(ValidationContext context) {
        return new Chain(context.isFast(), context);
    }


    private Chain(boolean failFast, ValidationContext context) {
        super(failFast, context);
    }


    @Override
    public Chain core() {
        return this;
    }

    @Override
    public void fail() {
        String scene = getSceneName();
        notifyValidationStart("chain", scene);
        long startTime = System.nanoTime();
        boolean success = true;
        try {
            if (!isValid()) {
                success = false;
                if (getCauses().isEmpty()) {
                    throw Business.of(ResponseCode.VALIDATION_ERROR_500);
                }
                throw getCauses().get(0);
            }
        } finally {
            long duration = System.nanoTime() - startTime;
            notifyValidationEnd("chain", duration, success);
            if (!success) {
                notifyValidationFailure("chain", String.valueOf(ResponseCode.VALIDATION_ERROR_400.getCode()));
            }
        }
    }

    @Override
    public void failAll() {
        String scene = getSceneName();
        notifyValidationStart("chain", scene);
        long startTime = System.nanoTime();
        boolean success = true;
        try {
            if (!isValid()) {
                success = false;
                if (getCauses().isEmpty()) {
                    throw Business.of(ResponseCode.VALIDATION_ERROR_500);
                }
                if (getCauses().size() == 1 && !isErrorsTruncated()) throw getCauses().get(0);
                throw new MultiBusiness(getCauses(), isErrorsTruncated());
            }
        } finally {
            long duration = System.nanoTime() - startTime;
            notifyValidationEnd("chain", duration, success);
            if (!success) {
                notifyValidationFailure("chain", String.valueOf(ResponseCode.VALIDATION_ERROR_400.getCode()));
            }
        }
    }

    @Override
    public void verify() {
        String scene = getSceneName();
        notifyValidationStart("chain", scene);
        long startTime = System.nanoTime();
        boolean success = isValid();
        long duration = System.nanoTime() - startTime;
        notifyValidationEnd("chain", duration, success);
        if (!success) {
            notifyValidationFailure("chain", String.valueOf(ResponseCode.VALIDATION_ERROR_400.getCode()));
        }
    }

    public CompletableFuture<Void> failAsync() {
        String scene = getSceneName();
        notifyValidationStart("chain-async", scene);
        long startTime = System.nanoTime();
        return applyAsyncChecks()
                .thenRun(() -> {
                    if (!isValid()) {
                        if (getCauses().isEmpty()) {
                            throw Business.of(ResponseCode.VALIDATION_ERROR_500);
                        }
                        throw getCauses().get(0);
                    }
                })
                .whenComplete((v, ex) -> {
                    long duration = System.nanoTime() - startTime;
                    boolean success = ex == null;
                    notifyValidationEnd("chain-async", duration, success);
                    if (!success) {
                        notifyValidationFailure("chain-async", String.valueOf(ResponseCode.VALIDATION_ERROR_400.getCode()));
                    }
                });
    }

    public CompletableFuture<Void> failAllAsync() {
        String scene = getSceneName();
        notifyValidationStart("chain-async", scene);
        long startTime = System.nanoTime();
        return applyAsyncChecks()
                .thenRun(() -> {
                    if (!isValid()) {
                        if (getCauses().isEmpty()) {
                            throw Business.of(ResponseCode.VALIDATION_ERROR_500);
                        }
                        if (getCauses().size() == 1) throw getCauses().get(0);
                        throw new MultiBusiness(getCauses());
                    }
                })
                .whenComplete((v, ex) -> {
                    long duration = System.nanoTime() - startTime;
                    boolean success = ex == null;
                    notifyValidationEnd("chain-async", duration, success);
                    if (!success) {
                        notifyValidationFailure("chain-async", String.valueOf(ResponseCode.VALIDATION_ERROR_400.getCode()));
                    }
                });
    }

    public CompletableFuture<Boolean> verifyAsync() {
        String scene = getSceneName();
        notifyValidationStart("chain-async", scene);
        long startTime = System.nanoTime();
        return applyAsyncChecks()
                .handle((v, ex) -> {
                    long duration = System.nanoTime() - startTime;
                    boolean success;
                    if (ex == null) {
                        success = isValid();
                    } else {
                        Throwable t = ex.getCause() != null ? ex.getCause() : ex;
                        throw t instanceof RuntimeException rt ? rt : new RuntimeException(t);
                    }
                    notifyValidationEnd("chain-async", duration, success);
                    if (!success) {
                        notifyValidationFailure("chain-async", String.valueOf(ResponseCode.VALIDATION_ERROR_400.getCode()));
                    }
                    return success;
                });
    }

    public Mono<Void> failMono() {
        return Mono.fromFuture(failAsync());
    }

    public Mono<Void> failAllMono() {
        return Mono.fromFuture(failAllAsync());
    }

    public Mono<Boolean> verifyMono() {
        return Mono.fromFuture(verifyAsync());
    }

    public Chain checkAsync(Mono<Boolean> stage, ResponseCode code, String detail) {
        if (stage == null) {
            return checkAsync((java.util.concurrent.CompletionStage<Boolean>) null, code, detail);
        }
        return checkAsync(stage.toFuture(), code, detail);
    }

    private String getSceneName() {
        if (context != null) {
            Scenario[] scenes = context.getScenes();
            if (scenes != null && scenes.length > 0) {
                if (scenes.length == 1) {
                    return scenes[0].name();
                } else {
                    // Multiple scenes, return "MULTI"
                    return "MULTI";
                }
            }
        }
        return Scenario.DEFAULT.name();
    }

    @Override
    public JsrValidator<Chain> jsr(Object target) {
        return new JsrValidatorImpl<>(this, target, null);
    }

    @Override
    public JsrValidator<Chain> jsr(Class<?> beanClass) {
        return new JsrValidatorImpl<>(this, null, beanClass);
    }

    private class JsrValidatorImpl<S extends ChainCore<S>> implements JsrValidator<S> {
        private final S chain;
        private final Object target;
        private final Class<?> beanClass;
        private String pathPrefix = "";

        public JsrValidatorImpl(S chain, Object target, Class<?> beanClass) {
            this.chain = chain;
            this.target = target;
            this.beanClass = beanClass;
        }

        @Override
        public S validate() {
            // Rule A: Check shouldSkip() first
            if (Chain.this.shouldSkip() || target == null) {
                return chain;
            }

            // Get Validator instance
            Validator validator = getValidator();
            if (validator == null) {
                return chain;
            }

            // Get groups from context
            Class<?>[] groups = getGroups();

            // Perform validation
            Set<?> rawViolations;
            if (groups.length > 0) {
                rawViolations = validator.validate(target, groups);
            } else {
                rawViolations = validator.validate(target);
            }
            @SuppressWarnings("unchecked")
            Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) rawViolations;

            // Process violations
            processViolations(violations);

            return chain;
        }

        @Override
        public S value(String propertyName, Object value) {
            // Rule A: Check shouldSkip() first
            if (Chain.this.shouldSkip() || beanClass == null || propertyName == null) {
                return chain;
            }

            // Get Validator instance
            Validator validator = getValidator();
            if (validator == null) {
                return chain;
            }

            // Get groups from context
            Class<?>[] groups = getGroups();

            // Perform validation
            Set<?> rawViolations;
            if (groups.length > 0) {
                rawViolations = validator.validateValue(beanClass, propertyName, value, groups);
            } else {
                rawViolations = validator.validateValue(beanClass, propertyName, value);
            }
            @SuppressWarnings("unchecked")
            Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) rawViolations;

            // Process violations
            processViolations(violations);

            return chain;
        }

        @Override
        public JsrValidator<S> pathPrefix(String pathPrefix) {
            this.pathPrefix = pathPrefix;
            return this;
        }

        private Validator getValidator() {
            return Chain.validator;
        }

        private Class<?>[] getGroups() {
            if (Chain.this.context != null) {
                return Chain.this.context.getGroups();
            }
            return new Class<?>[0];
        }

        private void processViolations(Set<ConstraintViolation<?>> violations) {
            String scene = getSceneName();

            Chain.this.notifyValidationStart("jsr", scene);
            long startTime = System.nanoTime();
            boolean success = true;

            try {
                for (ConstraintViolation<?> violation : violations) {
                    if (Chain.this.shouldSkip()) {
                        break;
                    }

                    String path = violation.getPropertyPath().toString();
                    if (!pathPrefix.isEmpty()) {
                        if (!path.isEmpty()) {
                            path = pathPrefix + "." + path;
                        } else {
                            path = pathPrefix;
                        }
                    }

                    String message = violation.getMessage();
                    Object invalidValue = violation.getInvalidValue();

                    String constraintName = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
                    ResponseCode responseCode = ResponseCode.VALIDATION_ERROR_400;
                    if (failureProperties != null) {
                        FailureProperties.CodeMapping mapping = failureProperties.getCodeMapping();
                        if (mapping != null) {
                            Integer code = null;
                            List<FailureProperties.CodeMapping.ConstraintPathMapping> constraintPathMapping = mapping.getConstraintPathMapping();
                            if (constraintPathMapping != null) {
                                for (var rule : constraintPathMapping) {
                                    if (rule == null) continue;
                                    if (rule.getCode() == null) continue;
                                    if (rule.getConstraint() == null || rule.getPath() == null) continue;
                                    if (constraintName.equals(rule.getConstraint()) && path.equals(rule.getPath())) {
                                        code = rule.getCode();
                                        break;
                                    }
                                }
                            }
                            if (code != null) {
                                responseCode = ResponseCode.of(code, "{response.code.validation.error}", "{response.code.validation.error}");
                            } else {
                                String rootBeanName = violation.getRootBeanClass() != null ? violation.getRootBeanClass().getName() : null;
                                Integer beanCode = null;
                                if (rootBeanName != null) {
                                    List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = mapping.getConstraintBeanMapping();
                                    if (constraintBeanMapping != null) {
                                        for (var rule : constraintBeanMapping) {
                                            if (rule == null) continue;
                                            if (rule.getCode() == null) continue;
                                            if (rule.getConstraint() == null || rule.getBean() == null) continue;
                                            if (constraintName.equals(rule.getConstraint()) && rootBeanName.equals(rule.getBean())) {
                                                beanCode = rule.getCode();
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (beanCode != null) {
                                    responseCode = ResponseCode.of(beanCode, "{response.code.validation.error}", "{response.code.validation.error}");
                                } else {
                                    java.util.Map<String, Integer> constraintMapping = mapping.getConstraintMapping();
                                    if (constraintMapping != null) {
                                        Integer mappingCode = constraintMapping.get(constraintName);
                                        if (mappingCode != null) {
                                            responseCode = ResponseCode.of(mappingCode, "{response.code.validation.error}", "{response.code.validation.error}");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Chain.this.checkWithPathAndConstraint(false, responseCode, message, invalidValue, path, constraintName, "jsr");
                }
                success = Chain.this.isValid();
            } finally {
                long duration = System.nanoTime() - startTime;
                Chain.this.notifyValidationEnd("jsr", duration, success);
                if (!success) {
                    Chain.this.notifyValidationFailure("jsr", String.valueOf(ResponseCode.VALIDATION_ERROR_400.getCode()));
                }
            }
        }

        private String getSceneName() {
            return Chain.this.getSceneName();
        }
    }

}
