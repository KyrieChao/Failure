package com.chao.failfast.aspect;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.annotation.Validate;
import com.chao.failfast.config.mapping.CodeMappingConfig;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.constant.Scenario;
import com.chao.failfast.exception.Business;
import com.chao.failfast.internal.core.Ex;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.config.registry.DefaultValidatorRegistry;
import com.chao.failfast.integration.webflux.ReactiveTrace;
import com.chao.failfast.spi.ValidatorRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidationAspectMissingCoverageTest {

    static class RawValidator implements FastValidator {
        @Override
        public void validate(Object target, ValidationContext context) {
            context.reportError(ResponseCode.VALIDATION_ERROR_400, "x");
        }
    }

    static class NullDeclaredRawValidator implements FastValidator {
        @Override
        public void validate(Object target, ValidationContext context) {
        }

        @Override
        public Class<?> getSupportedType() {
            return null;
        }
    }

    static class AllowAllValidator implements FastValidator<Object> {
        @Override
        public void validate(Object target, ValidationContext context) {
        }

        @Override
        public Class<?> getSupportedType() {
            return null;
        }

        @Override
        public boolean allowObjectSupportedType() {
            return true;
        }
    }

    static class NullDeclaredObjectGenericValidator implements FastValidator<Object> {
        @Override
        public void validate(Object target, ValidationContext context) {
        }

        @Override
        public Class<?> getSupportedType() {
            return null;
        }
    }

    @AfterEach
    void tearDown() {
        Ex.setContext(null);
    }

    @Test
    void aroundAddsSceneToMonoContextWhenApplied() throws Throwable {
        FailureProperties props = new FailureProperties();
        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(ctx);

        ValidationAspect aspect = new ValidationAspect();
        setField(aspect, "applicationContext", mock(ApplicationContext.class));
        setField(aspect, "validator", null);
        setField(aspect, "validatorRegistry", null);

        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method m = getClass().getDeclaredMethod("dummyMethod", String.class);
        when(point.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(m);
        when(point.getArgs()).thenReturn(new Object[]{"a"});
        when(point.proceed()).thenReturn(Mono.deferContextual(c -> Mono.just((String) c.getOrDefault(ReactiveTrace.SCENE_KEY, "none"))));

        Validate validate = mock(Validate.class);
        when(validate.scene()).thenReturn(new Scenario[]{Scenario.CREATE});
        when(validate.groups()).thenReturn(new Class<?>[0]);
        when(validate.fast()).thenReturn(true);
        when(validate.value()).thenReturn(new Class[0]);

        Object out = aspect.around(point, validate);
        @SuppressWarnings("unchecked")
        Mono<String> mono = (Mono<String>) out;

        assertThat(mono.block()).isEqualTo("CREATE");
    }

    @Test
    void aroundAddsSceneToFluxContextWhenApplied() throws Throwable {
        FailureProperties props = new FailureProperties();
        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(ctx);

        ValidationAspect aspect = new ValidationAspect();
        setField(aspect, "applicationContext", mock(ApplicationContext.class));
        setField(aspect, "validator", null);
        setField(aspect, "validatorRegistry", null);

        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method m = getClass().getDeclaredMethod("dummyMethod", String.class);
        when(point.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(m);
        when(point.getArgs()).thenReturn(new Object[]{"a"});
        when(point.proceed()).thenReturn(Flux.deferContextual(c -> Flux.just((String) c.getOrDefault(ReactiveTrace.SCENE_KEY, "none"))));

        Validate validate = mock(Validate.class);
        when(validate.scene()).thenReturn(new Scenario[]{Scenario.CREATE});
        when(validate.groups()).thenReturn(new Class<?>[0]);
        when(validate.fast()).thenReturn(true);
        when(validate.value()).thenReturn(new Class[0]);

        Object out = aspect.around(point, validate);
        @SuppressWarnings("unchecked")
        Flux<String> flux = (Flux<String>) out;

        assertThat(flux.blockFirst()).isEqualTo("CREATE");
    }

    @Test
    void executeValidatorsRunsGlobalRegistryValidators() throws Throwable {
        FailureProperties props = new FailureProperties();
        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(ctx);

        ValidationAspect aspect = new ValidationAspect();
        setField(aspect, "applicationContext", mock(ApplicationContext.class));
        setField(aspect, "validator", null);

        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<String> v = (target, c) -> c.reportError(ResponseCode.VALIDATION_ERROR_400, "x");
        registry.register(String.class, v);
        setField(aspect, "validatorRegistry", registry);

        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method m = getClass().getDeclaredMethod("dummyMethod", String.class);
        when(point.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(m);
        when(point.getArgs()).thenReturn(new Object[]{"a"});

        Validate validate = mock(Validate.class);
        when(validate.scene()).thenReturn(new Scenario[]{Scenario.DEFAULT});
        when(validate.groups()).thenReturn(new Class<?>[0]);
        when(validate.fast()).thenReturn(true);
        when(validate.value()).thenReturn(new Class[0]);

        assertThrows(Business.class, () -> aspect.around(point, validate));
    }

    @Test
    void aroundClearsThreadContextWhenSceneAppliedAndSuccess() throws Throwable {
        FailureProperties props = new FailureProperties();
        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        ctx.setTraceId("t1");
        ctx.setScene("OLD");
        Ex.setContext(ctx);

        ValidationAspect aspect = new ValidationAspect();
        setField(aspect, "applicationContext", mock(ApplicationContext.class));
        setField(aspect, "validator", null);
        setField(aspect, "validatorRegistry", null);

        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method m = getClass().getDeclaredMethod("dummyMethod", String.class);
        when(point.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(m);
        when(point.getArgs()).thenReturn(new Object[]{"a"});
        when(point.proceed()).thenReturn("ok");

        Validate validate = mock(Validate.class);
        when(validate.scene()).thenReturn(new Scenario[]{Scenario.CREATE});
        when(validate.groups()).thenReturn(new Class<?>[0]);
        when(validate.fast()).thenReturn(false);
        when(validate.value()).thenReturn(new Class[0]);

        Object out = aspect.around(point, validate);
        assertThat(out).isEqualTo("ok");
        assertThat(ctx.getScene()).isEqualTo("OLD");
    }

    @Test
    void aroundDoesNothingWhenNoSceneApplied() throws Throwable {
        FailureProperties props = new FailureProperties();
        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        ctx.setScene("OLD");
        Ex.setContext(ctx);

        ValidationAspect aspect = new ValidationAspect();
        setField(aspect, "applicationContext", mock(ApplicationContext.class));
        setField(aspect, "validator", null);
        setField(aspect, "validatorRegistry", null);

        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method m = getClass().getDeclaredMethod("dummyMethod", String.class);
        when(point.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(m);
        when(point.getArgs()).thenReturn(new Object[]{"a"});
        when(point.proceed()).thenReturn("ok");

        Validate validate = mock(Validate.class);
        when(validate.scene()).thenReturn(new Scenario[]{Scenario.DEFAULT});
        when(validate.groups()).thenReturn(new Class<?>[0]);
        when(validate.fast()).thenReturn(false);
        when(validate.value()).thenReturn(new Class[0]);

        Object out = aspect.around(point, validate);
        assertThat(out).isEqualTo("ok");
        assertThat(ctx.getScene()).isEqualTo("OLD");
    }

    @Test
    void aroundRestoresSceneWhenProceedThrows() throws Throwable {
        FailureProperties props = new FailureProperties();
        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        ctx.setScene("OLD");
        Ex.setContext(ctx);

        ValidationAspect aspect = new ValidationAspect();
        setField(aspect, "applicationContext", mock(ApplicationContext.class));
        setField(aspect, "validator", null);
        setField(aspect, "validatorRegistry", null);

        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method m = getClass().getDeclaredMethod("dummyMethod", String.class);
        when(point.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(m);
        when(point.getArgs()).thenReturn(new Object[]{"a"});
        when(point.proceed()).thenThrow(new RuntimeException("x"));

        Validate validate = mock(Validate.class);
        when(validate.scene()).thenReturn(new Scenario[]{Scenario.CREATE});
        when(validate.groups()).thenReturn(new Class<?>[0]);
        when(validate.fast()).thenReturn(false);
        when(validate.value()).thenReturn(new Class[0]);

        assertThrows(RuntimeException.class, () -> {
            try {
                aspect.around(point, validate);
            } catch (Throwable t) {
                throw (t instanceof RuntimeException rt) ? rt : new RuntimeException(t);
            }
        });
        assertThat(ctx.getScene()).isEqualTo("OLD");
    }

    @Test
    void executeValidatorsCoversGlobalValidatorCacheAndLoopCompletion() throws Throwable {
        FailureProperties props = new FailureProperties();
        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(ctx);

        ValidationAspect aspect = new ValidationAspect();
        setField(aspect, "applicationContext", mock(ApplicationContext.class));
        setField(aspect, "validator", null);

        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<String> v = (target, c) -> {
        };
        registry.register(String.class, v);
        setField(aspect, "validatorRegistry", registry);

        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method m = getClass().getDeclaredMethod("dummyMethod2", String.class, Integer.class);
        when(point.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(m);
        when(point.getArgs()).thenReturn(new Object[]{"a", 1});
        when(point.proceed()).thenReturn("ok");

        Validate validate = mock(Validate.class);
        when(validate.scene()).thenReturn(new Scenario[]{Scenario.DEFAULT});
        when(validate.groups()).thenReturn(new Class<?>[0]);
        when(validate.fast()).thenReturn(false);
        when(validate.value()).thenReturn(new Class[0]);

        Object out = aspect.around(point, validate);
        assertThat(out).isEqualTo("ok");
    }

    @Test
    void getValidatorSupportedTypeWarnsAndReturnsNullForRawValidator() throws Exception {
        ValidationAspect aspect = new ValidationAspect();
        Method m = ValidationAspect.class.getDeclaredMethod("getValidatorSupportedType", FastValidator.class);
        m.setAccessible(true);
        Object out = m.invoke(aspect, new NullDeclaredRawValidator());
        assertThat(out).isNull();
    }

    @Test
    void getValidatorSupportedTypeReturnsObjectWhenAllowAllEnabled() throws Exception {
        ValidationAspect aspect = new ValidationAspect();
        Method m = ValidationAspect.class.getDeclaredMethod("getValidatorSupportedType", FastValidator.class);
        m.setAccessible(true);
        Object out = m.invoke(aspect, new AllowAllValidator());
        assertThat(out).isEqualTo(Object.class);
    }

    @Test
    void getValidatorSupportedTypeWarnsWhenGenericResolvesToObject() throws Exception {
        ValidationAspect aspect = new ValidationAspect();
        Method m = ValidationAspect.class.getDeclaredMethod("getValidatorSupportedType", FastValidator.class);
        m.setAccessible(true);
        Object out = m.invoke(aspect, new NullDeclaredObjectGenericValidator());
        assertThat(out).isNull();
    }

    @Test
    void executePlainValidatorReturnsWhenSupportedTypeIsNull() throws Exception {
        ValidationAspect aspect = new ValidationAspect();
        Method m = ValidationAspect.class.getDeclaredMethod("executePlainValidator", FastValidator.class, List.class, FastValidator.ValidationContext.class);
        m.setAccessible(true);

        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(false);
        m.invoke(aspect, new NullDeclaredRawValidator(), List.of("a"), ctx);
        assertThat(ctx.hasCauses()).isEmpty();
    }

    @Test
    void shouldSkipReturnsTrueForNullClass() throws Exception {
        ValidationAspect aspect = new ValidationAspect();
        Method m = ValidationAspect.class.getDeclaredMethod("shouldSkip", Class.class);
        m.setAccessible(true);
        boolean out = (boolean) m.invoke(aspect, new Object[]{null});
        assertThat(out).isTrue();
    }

    @Test
    void getFieldAndGetSceneValuesHandleNullInputs() throws Exception {
        ValidationAspect aspect = new ValidationAspect();

        Method findField = ValidationAspect.class.getDeclaredMethod("findField", Class.class, String.class);
        findField.setAccessible(true);
        Object f1 = findField.invoke(aspect, null, "x");
        Object f2 = findField.invoke(aspect, String.class, null);
        Object f3 = findField.invoke(aspect, String.class, "");
        assertThat(f1).isNull();
        assertThat(f2).isNull();
        assertThat(f3).isNull();

        Method getSceneValues = ValidationAspect.class.getDeclaredMethod("getSceneValues", java.lang.reflect.Field.class);
        getSceneValues.setAccessible(true);
        Object out = getSceneValues.invoke(aspect, new Object[]{null});
        assertThat(out).isEqualTo(Collections.emptySet());
    }

    @Test
    void executeValidatorsCoversFailFastBreakInGlobalLoop() throws Exception {
        ValidationAspect aspect = new ValidationAspect();
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<String> v1 = (target, ctx) -> ctx.reportError(ResponseCode.VALIDATION_ERROR_400, "x");
        FastValidator<Integer> v2 = (target, ctx) -> ctx.reportError(ResponseCode.VALIDATION_ERROR_400, "y");
        registry.register(String.class, v1);
        registry.register(Integer.class, v2);
        setField(aspect, "validatorRegistry", registry);

        Method m = ValidationAspect.class.getDeclaredMethod("executeValidators", Class[].class, List.class, boolean.class, Scenario[].class, Class[].class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Business> out = (List<Business>) m.invoke(aspect, new Class[0], List.of("a", 1), true, new Scenario[]{Scenario.DEFAULT}, new Class<?>[0]);
        assertThat(out).isNotEmpty();
    }

    @Test
    void executeValidatorsCoversGlobalValidatorAlreadyExecutedAndNullBranches() throws Exception {
        ValidationAspect aspect = new ValidationAspect();
        setField(aspect, "applicationContext", mock(ApplicationContext.class));

        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        PlainValidator plain = new PlainValidator();
        registry.register(String.class, plain);
        setField(aspect, "validatorRegistry", registry);

        ApplicationContext applicationContext = (ApplicationContext) getField(aspect, "applicationContext");
        when(applicationContext.getBean(org.mockito.ArgumentMatchers.eq(PlainValidator.class))).thenReturn(plain);

        Method m = ValidationAspect.class.getDeclaredMethod("executeValidators", Class[].class, List.class, boolean.class, Scenario[].class, Class[].class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Business> out = (List<Business>) m.invoke(aspect, new Class[]{PlainValidator.class}, List.of("a", 1.0d), false, new Scenario[]{Scenario.DEFAULT}, new Class<?>[0]);
        assertThat(out).isEmpty();
    }

    @Test
    void executeValidatorsSkipsGlobalValidatorWhenAlreadyExecuted() throws Exception {
        ValidationAspect aspect = new ValidationAspect();
        setField(aspect, "applicationContext", null);

        ValidatorRegistry registry = new ValidatorRegistry() {
            @Override
            public <T> ValidatorRegistry register(Class<T> type, FastValidator<T> validator) {
                throw new UnsupportedOperationException();
            }

            @Override
            public FastValidator<Object> getValidator(Class<?> type) {
                try {
                    Field f = ValidationAspect.class.getDeclaredField("VALIDATOR_CACHE");
                    f.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    java.util.concurrent.ConcurrentHashMap<Class<?>, FastValidator<Object>> cache =
                            (java.util.concurrent.ConcurrentHashMap<Class<?>, FastValidator<Object>>) f.get(null);
                    return cache.get(PlainValidator.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        setField(aspect, "validatorRegistry", registry);

        Method m = ValidationAspect.class.getDeclaredMethod("executeValidators", Class[].class, List.class, boolean.class, Scenario[].class, Class[].class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Business> out = (List<Business>) m.invoke(aspect, new Class[]{PlainValidator.class}, List.of("a"), false, new Scenario[]{Scenario.DEFAULT}, new Class<?>[0]);
        assertThat(out).isEmpty();
    }

    @Test
    void executeValidatorsCoversFailFastNoBreakWhenNoErrors() throws Exception {
        ValidationAspect aspect = new ValidationAspect();
        setField(aspect, "applicationContext", mock(ApplicationContext.class));

        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<String> v = (target, ctx) -> {
        };
        registry.register(String.class, v);
        setField(aspect, "validatorRegistry", registry);

        Method m = ValidationAspect.class.getDeclaredMethod("executeValidators", Class[].class, List.class, boolean.class, Scenario[].class, Class[].class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Business> out = (List<Business>) m.invoke(aspect, new Class[0], List.of("a"), true, new Scenario[]{Scenario.DEFAULT}, new Class<?>[0]);
        assertThat(out).isEmpty();
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = ValidationAspect.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Object getField(Object target, String name) throws Exception {
        Field f = ValidationAspect.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String a) {
    }

    @SuppressWarnings("unused")
    private void dummyMethod2(String a, Integer b) {
    }

    static class PlainValidator implements FastValidator<String> {
        @Override
        public void validate(String target, ValidationContext context) {
        }
    }
}

