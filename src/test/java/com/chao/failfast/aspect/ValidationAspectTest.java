package com.chao.failfast.aspect;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.annotation.Scene;
import com.chao.failfast.annotation.SkipValidation;
import com.chao.failfast.annotation.Validate;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.constant.Scenario;
import com.chao.failfast.internal.core.Ex;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.exception.Business;
import com.chao.failfast.internal.policy.ErrorPolicy;
import com.chao.failfast.validator.TypedValidator;
import jakarta.servlet.ServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@Slf4j
class ValidationAspectTest {


    private TestValidationAspect aspect;
    private ApplicationContext applicationContext;
    private Validator validator;
    private FailureContext context;

    @BeforeEach
    void setUp() {
        aspect = new TestValidationAspect();
        applicationContext = Mockito.mock(ApplicationContext.class);
        validator = Mockito.mock(Validator.class);
        context = Mockito.mock(FailureContext.class);
        aspect.setApplicationContext(applicationContext);
        aspect.setValidator(validator);
        Ex.setContext(context);
    }

    // 测试子类，暴露私有方�?
    private static class TestValidationAspect extends ValidationAspect {
        public void setApplicationContext(ApplicationContext applicationContext) {
            try {
                Field field = ValidationAspect.class.getDeclaredField("applicationContext");
                field.setAccessible(true);
                field.set(this, applicationContext);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void setValidator(Validator validator) {
            try {
                Field field = ValidationAspect.class.getDeclaredField("validator");
                field.setAccessible(true);
                field.set(this, validator);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String toSceneName(Scenario[] scenes) {
            try {
                Method method = ValidationAspect.class.getDeclaredMethod("toSceneName", Scenario[].class);
                method.setAccessible(true);
                return (String) method.invoke(this, (Object) scenes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public List<Business> executeBridgeValidation(List<Object> args, Class<?>[] groups, boolean fast, Scenario[] scenes) {
            try {
                Method method = ValidationAspect.class.getDeclaredMethod("executeBridgeValidation", List.class, Class[].class, boolean.class, Scenario[].class);
                method.setAccessible(true);
                return (List<Business>) method.invoke(this, args, groups, fast, scenes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public boolean shouldKeepViolation(ConstraintViolation<?> violation, Scenario[] scenes) {
            try {
                Method method = ValidationAspect.class.getDeclaredMethod("shouldKeepViolation", ConstraintViolation.class, Scenario[].class);
                method.setAccessible(true);
                return (boolean) method.invoke(this, violation, scenes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String parseFieldName(String propertyPath) {
            try {
                Method method = ValidationAspect.class.getDeclaredMethod("parseFieldName", String.class);
                method.setAccessible(true);
                return (String) method.invoke(this, propertyPath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Field findField(Class<?> clazz, String fieldName) {
            try {
                Method method = ValidationAspect.class.getDeclaredMethod("findField", Class.class, String.class);
                method.setAccessible(true);
                return (Field) method.invoke(this, clazz, fieldName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Set<Scenario> getSceneValues(Field field) {
            try {
                Method method = ValidationAspect.class.getDeclaredMethod("getSceneValues", Field.class);
                method.setAccessible(true);
                return (Set<Scenario>) method.invoke(this, field);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public List<Object> collectValidatableArgs(ProceedingJoinPoint point) {
            try {
                Method method = ValidationAspect.class.getDeclaredMethod("collectValidatableArgs", ProceedingJoinPoint.class);
                method.setAccessible(true);
                return (List<Object>) method.invoke(this, point);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public boolean shouldSkip(Class<?> clazz) {
            try {
                Method method = ValidationAspect.class.getDeclaredMethod("shouldSkip", Class.class);
                method.setAccessible(true);
                return (boolean) method.invoke(this, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public boolean hasSkipAnnotation(Annotation[] annotations) {
            try {
                Method method = ValidationAspect.class.getDeclaredMethod("hasSkipAnnotation", Annotation[].class);
                method.setAccessible(true);
                return (boolean) method.invoke(this, (Object) annotations);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String formatValidationLocation(Class<?> clazz, String fieldOrPath) {
            try {
                Method method = ValidationAspect.class.getDeclaredMethod("formatValidationLocation", Class.class, String.class);
                method.setAccessible(true);
                return (String) method.invoke(this, clazz, fieldOrPath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void executeTypedValidator(TypedValidator validator, List<Object> args, FastValidator.ValidationContext ctx) {
            try {
                Method method = ValidationAspect.class.getDeclaredMethod("executeTypedValidator", TypedValidator.class, List.class, FastValidator.ValidationContext.class);
                method.setAccessible(true);
                method.invoke(this, validator, args, ctx);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void executePlainValidator(FastValidator<Object> validator, List<Object> args, FastValidator.ValidationContext ctx) {
            try {
                Method method = ValidationAspect.class.getDeclaredMethod("executePlainValidator", FastValidator.class, List.class, FastValidator.ValidationContext.class);
                method.setAccessible(true);
                method.invoke(this, validator, args, ctx);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Class<?> getValidatorSupportedType(FastValidator<?> validator) {
            try {
                Method method = ValidationAspect.class.getDeclaredMethod("getValidatorSupportedType", FastValidator.class);
                method.setAccessible(true);
                return (Class<?>) method.invoke(this, validator);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    void testAroundWithNoValidatorsAndNoGroups() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{});

        Object expectedResult = new Object();
        Mockito.when(point.proceed()).thenReturn(expectedResult);

        Object result = aspect.around(point, validate);
        assertEquals(expectedResult, result);
    }

    @Test
    void testAroundWithExceptionAndSceneRestoration() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.CREATE});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{});

        Mockito.when(context.getScene()).thenReturn(FailureConst.DEFAULT_SCENE);

        Mockito.when(point.proceed()).thenThrow(new RuntimeException("Simulated exception"));

        assertThrows(RuntimeException.class, () -> aspect.around(point, validate));

        // verify scene is set to CREATE, and then restored to DEFAULT
        verify(context).setScene("CREATE");
        verify(context).setScene(FailureConst.DEFAULT_SCENE);
    }

    @Test
    void testAroundWithValidators() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{"test"});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.DEFAULT});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{TestValidator.class});

        Object expectedResult = new Object();
        Mockito.when(point.proceed()).thenReturn(expectedResult);

        ObjectProvider<FastValidator<Object>> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(applicationContext.getBeanProvider(TestValidator.class)).thenReturn((ObjectProvider) provider);
        Mockito.when(provider.getIfAvailable()).thenReturn(null);

        Object result = aspect.around(point, validate);
        assertEquals(expectedResult, result);
    }

    @Test
    void testAroundWithValidationErrors() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{""});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.DEFAULT});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{TestValidator.class});

        ObjectProvider<com.chao.failfast.annotation.FastValidator<Object>> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(applicationContext.getBeanProvider(TestValidator.class)).thenReturn((ObjectProvider) provider);
        Mockito.when(provider.getIfAvailable()).thenReturn(new TestValidator());

        assertThrows(Business.class, () -> aspect.around(point, validate));
    }

    @Test
    void testToSceneNameWithEmptyScenes() {
        String sceneName = aspect.toSceneName(new Scenario[]{});
        assertEquals(FailureConst.DEFAULT_SCENE, sceneName);
    }

    @Test
    void testToSceneNameWithDefaultScene() {
        String sceneName = aspect.toSceneName(new Scenario[]{Scenario.DEFAULT});
        assertEquals(FailureConst.DEFAULT_SCENE, sceneName);
    }

    @Test
    void testToSceneNameWithCustomScenes() {
        String sceneName = aspect.toSceneName(new Scenario[]{Scenario.CREATE, Scenario.UPDATE});
        assertEquals("CREATE,UPDATE", sceneName);
    }

    @Test
    void testExecuteBridgeValidationWithNullValidator() {
        // 通过反射设置validator为null
        try {
            Field field = ValidationAspect.class.getDeclaredField("validator");
            field.setAccessible(true);
            field.set(aspect, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<Business> errors = aspect.executeBridgeValidation(List.of("test"), new Class<?>[]{}, true, new Scenario[]{});
        assertTrue(errors.isEmpty());
    }

    @Test
    void testShouldKeepViolationWithEmptyScenes() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
        jakarta.validation.Path path = Mockito.mock(jakarta.validation.Path.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(path.toString()).thenReturn("field");
        boolean result = aspect.shouldKeepViolation(violation, new Scenario[]{});
        assertTrue(result);
    }

    @Test
    void testShouldKeepViolationWithDefaultScene() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
        jakarta.validation.Path path = Mockito.mock(jakarta.validation.Path.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(path.toString()).thenReturn("field");
        boolean result = aspect.shouldKeepViolation(violation, new Scenario[]{Scenario.DEFAULT});
        assertTrue(result);
    }

    @Test
    void testParseFieldName() {
        assertEquals("field", aspect.parseFieldName("field"));
        assertEquals("field", aspect.parseFieldName("field.subfield"));
        assertEquals("items", aspect.parseFieldName("items[0]"));
    }

    @Test
    void testFindField() {
        Field field = aspect.findField(TestClass.class, "name");
        assertNotNull(field);
    }

    @Test
    void testGetSceneValues() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("name");
        Set<Scenario> scenes = aspect.getSceneValues(field);
        assertNotNull(scenes);
    }

    @Test
    void testCollectValidatableArgsWithSkipValidation() {
        try {
            ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
            MethodSignature signature = Mockito.mock(MethodSignature.class);
            Method method = getClass().getMethod("testMethodWithSkipValidation", String.class);
            Mockito.when(point.getSignature()).thenReturn(signature);
            Mockito.when(signature.getMethod()).thenReturn(method);
            Mockito.when(point.getArgs()).thenReturn(new Object[]{"test"});

            List<Object> args = aspect.collectValidatableArgs(point);
            assertTrue(args.isEmpty());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCollectValidatableArgsWithSkipType() {
        try {
            ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
            MethodSignature signature = Mockito.mock(MethodSignature.class);
            Method method = getClass().getMethod("testMethodWithServletRequest", ServletRequest.class);
            Mockito.when(point.getSignature()).thenReturn(signature);
            Mockito.when(signature.getMethod()).thenReturn(method);
            Mockito.when(point.getArgs()).thenReturn(new Object[]{Mockito.mock(ServletRequest.class)});

            List<Object> args = aspect.collectValidatableArgs(point);
            assertTrue(args.isEmpty());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testShouldSkip() {
        assertTrue(aspect.shouldSkip(ServletRequest.class));
        assertTrue(aspect.shouldSkip(InputStream.class));
        assertFalse(aspect.shouldSkip(String.class));
    }

    @Test
    void testHasSkipAnnotation() {
        assertTrue(aspect.hasSkipAnnotation(new Annotation[]{Mockito.mock(SkipValidation.class)}));
        assertFalse(aspect.hasSkipAnnotation(new Annotation[]{}));
        assertFalse(aspect.hasSkipAnnotation(null));
    }

    @Test
    void testFormatValidationLocation() {
        String location = aspect.formatValidationLocation(TestClass.class, "field");
        assertNotNull(location);
    }

    @Test
    void testExecuteTypedValidator() {
        TypedValidator typedValidator = Mockito.mock(TypedValidator.class);
        com.chao.failfast.annotation.FastValidator.ValidationContext ctx = new com.chao.failfast.annotation.FastValidator.ValidationContext(true);
        aspect.executeTypedValidator(typedValidator, List.of("test"), ctx);
        verify(typedValidator).validateIfRegistered("test", ctx);
    }

    @Test
    void testExecutePlainValidator() {
        TestValidator validator = new TestValidator();
        com.chao.failfast.annotation.FastValidator.ValidationContext ctx = new com.chao.failfast.annotation.FastValidator.ValidationContext(true);
        aspect.executePlainValidator(validator, List.of("test"), ctx);
    }

    @Test
    void testGetValidatorSupportedType() {
        TestValidator validator = new TestValidator();
        Class<?> type = aspect.getValidatorSupportedType(validator);
        assertEquals(String.class, type);
    }

    @Test
    void testGetValidatorSupportedTypeWithObjectClass() {
        ObjectValidator validator = new ObjectValidator();
        Class<?> type = aspect.getValidatorSupportedType(validator);
        assertEquals(Object.class, type);
    }

    @Test
    void testFormatValidationLocationWithNullField() {
        String location = aspect.formatValidationLocation(TestClass.class, null);
        assertEquals("", location);
    }

    @Test
    void testFormatValidationLocationWithNullClass() {
        String location = aspect.formatValidationLocation(null, "field");
        assertEquals("field", location);
    }

    @Test
    void testExecutePlainValidatorWithUnsupportedType() {
        TestValidator validator = new TestValidator();
        com.chao.failfast.annotation.FastValidator.ValidationContext ctx = new com.chao.failfast.annotation.FastValidator.ValidationContext(true);
        aspect.executePlainValidator(validator, List.of(123), ctx);
        assertTrue(ctx.isValid());
    }

    @Test
@DisplayName("display")
    void testExecutePlainValidatorWithObjectType() {
        // 1. 准备：这是一个没有注册任何规则的验证�?
        ObjectValidator validator = new ObjectValidator();
        com.chao.failfast.annotation.FastValidator.ValidationContext ctx = new com.chao.failfast.annotation.FastValidator.ValidationContext(false);

        // 2. 执行：传入任意对�?(�?"test" �?new Object())
        Object testData = new Object();
        aspect.executePlainValidator(validator, List.of(testData), ctx);

        // 3. 【关键修正】断言验证应该失败
        // 因为 validators 地图为空，resolveHandler 返回 null，触�?"Unsupported validation type" 错误
        assertFalse(ctx.isValid(), "未注册任何规则的验证器应导致验证失败");

        // 4. 进阶断言：验证错误确实被记录，且错误数量大于 0
        assertTrue(ctx.errorSize() > 0, "validation should record at least one error");

        Business firstError = ctx.getFirstError();
        assertTrue(firstError.getDetail().contains("Object"));
    }

    @Test
    void testAroundWithSceneHandling() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.CREATE});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{});

        Object expectedResult = new Object();
        Mockito.when(point.proceed()).thenReturn(expectedResult);

        Mockito.when(context.getScene()).thenReturn(null);

        Object result = aspect.around(point, validate);
        assertEquals(expectedResult, result);
        verify(context).setScene("CREATE");
        verify(context).setScene(FailureConst.DEFAULT_SCENE);
    }

    @Test
    void testAroundWithValidationGroups() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{"test"});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{Object.class});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{});

        Object expectedResult = new Object();
        Mockito.when(point.proceed()).thenReturn(expectedResult);

        Object result = aspect.around(point, validate);
        assertEquals(expectedResult, result);
    }

    @Test
    void testAroundWithMultipleValidators() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{"test"});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{TestValidator.class, TestValidator.class});

        Object expectedResult = new Object();
        Mockito.when(point.proceed()).thenReturn(expectedResult);

        ObjectProvider<FastValidator<Object>> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(applicationContext.getBeanProvider(TestValidator.class)).thenReturn((ObjectProvider) provider);
        Mockito.when(provider.getIfAvailable()).thenReturn(null);

        Object result = aspect.around(point, validate);
        assertEquals(expectedResult, result);
    }

    @Test
    void testShouldKeepViolationWithSceneFiltering() throws NoSuchFieldException {
        // 使用原始类型来避免Mockito的类型推断问�?
        @SuppressWarnings("rawtypes")
        ConstraintViolation violation = Mockito.mock(ConstraintViolation.class);
        jakarta.validation.Path path = Mockito.mock(jakarta.validation.Path.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(path.toString()).thenReturn("name");
        Mockito.when(violation.getRootBeanClass()).thenReturn(TestClass.class);

        boolean result = aspect.shouldKeepViolation(violation, new Scenario[]{Scenario.CREATE});
        assertTrue(result);

        result = aspect.shouldKeepViolation(violation, new Scenario[]{Scenario.DELETE});
        assertFalse(result);
    }

    @Test
    void testShouldKeepViolationWithFieldNotFound() {
        // 使用原始类型来避免Mockito的类型推断问�?
        @SuppressWarnings("rawtypes")
        ConstraintViolation violation = Mockito.mock(ConstraintViolation.class);
        jakarta.validation.Path path = Mockito.mock(jakarta.validation.Path.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(path.toString()).thenReturn("nonExistentField");
        Mockito.when(violation.getRootBeanClass()).thenReturn(TestClass.class);

        boolean result = aspect.shouldKeepViolation(violation, new Scenario[]{Scenario.CREATE});
        assertTrue(result); // 找不到字段时应该保留错误
    }

    @Test
    void testFormatValidationLocationWithCglibProxy() {
        // 创建一个模拟的 CGLIB 代理�?
        class CglibProxyTestClass {
        }
        String proxyClassName = CglibProxyTestClass.class.getName() + "$$EnhancerByCGLIB$$123456";
        Class<?> proxyClass = null;
        try {
            proxyClass = Class.forName(proxyClassName, false, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            // 如果类不存在，我们可以使用一个简单的类来测试
            proxyClass = TestClass.class;
        }
        String location = aspect.formatValidationLocation(proxyClass, "field");
        assertNotNull(location);
    }

    @Test
    void testAroundWithNullContext() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.CREATE});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{});

        Object expectedResult = new Object();
        Mockito.when(point.proceed()).thenReturn(expectedResult);

        Ex.setContext(null); // 设置为null

        Object result = aspect.around(point, validate);
        assertEquals(expectedResult, result);
    }

    @Test
    void testAroundWithMultipleErrors() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{"", ""});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.DEFAULT});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(false); // 不使用failFast，收集所有错�?
        Mockito.when(validate.value()).thenReturn(new Class[]{TestValidator.class, TestValidator.class});

        ObjectProvider<com.chao.failfast.annotation.FastValidator<Object>> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(applicationContext.getBeanProvider(TestValidator.class)).thenReturn((ObjectProvider) provider);
        Mockito.when(provider.getIfAvailable()).thenReturn(new TestValidator());

        Exception exception = assertThrows(Exception.class, () -> aspect.around(point, validate));
        assertTrue(exception instanceof com.chao.failfast.exception.MultiBusiness);
    }

    @Test
    void testGetValidatorSupportedTypeWithGenericInference() {
        // 使用已定义的 GenericValidator �?
        GenericValidator validator = new GenericValidator();
        Class<?> type = aspect.getValidatorSupportedType(validator);
        assertEquals(String.class, type);
    }

    @Test
    void testGetValidatorSupportedTypeWithNullGeneric() {
        // 使用已定义的 NonGenericValidator �?
        NonGenericValidator validator = new NonGenericValidator();
        Class<?> type = aspect.getValidatorSupportedType(validator);
        assertEquals(Object.class, type);
    }

    @Test
    void testBuildValidatorFactoryWithApplicationContext() throws Exception {
        // 测试通过 ApplicationContext 获取验证�?
        TestValidator testValidator = new TestValidator();
        ObjectProvider<com.chao.failfast.annotation.FastValidator<Object>> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(applicationContext.getBeanProvider(TestValidator.class)).thenReturn((ObjectProvider) provider);
        Mockito.when(provider.getIfAvailable()).thenReturn(testValidator);

        // 通过反射调用 buildValidatorFactory
        Method method = ValidationAspect.class.getDeclaredMethod("buildValidatorFactory", Class.class);
        method.setAccessible(true);
        Object factory = method.invoke(aspect, TestValidator.class);
        assertNotNull(factory);
    }

    @Test
    void testBuildValidatorFactoryWithBeanNames() throws Exception {
        // 测试通过 BeanNames 获取验证�?
        TestValidator testValidator = new TestValidator();
        ObjectProvider<com.chao.failfast.annotation.FastValidator<Object>> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(applicationContext.getBeanProvider(TestValidator.class)).thenReturn((ObjectProvider) provider);
        Mockito.when(provider.getIfAvailable()).thenReturn(null);
        Mockito.when(applicationContext.getBeanNamesForType(TestValidator.class)).thenReturn(new String[]{"testValidator"});
        Mockito.when(applicationContext.getBean(TestValidator.class)).thenReturn(testValidator);

        // 通过反射调用 buildValidatorFactory
        Method method = ValidationAspect.class.getDeclaredMethod("buildValidatorFactory", Class.class);
        method.setAccessible(true);
        Object factory = method.invoke(aspect, TestValidator.class);
        assertNotNull(factory);
    }

    @Test
    void testBuildValidatorFactoryWithNullApplicationContext() throws Exception {
        // 测试 ApplicationContext �?null 的情�?
        // 通过反射设置 applicationContext �?null
        Field field = ValidationAspect.class.getDeclaredField("applicationContext");
        field.setAccessible(true);
        field.set(aspect, null);

        // 通过反射调用 buildValidatorFactory
        Method method = ValidationAspect.class.getDeclaredMethod("buildValidatorFactory", Class.class);
        method.setAccessible(true);
        Object factory = method.invoke(aspect, TestValidator.class);
        assertNotNull(factory);
    }

    @Test
    void testNewValidatorInstance() throws Exception {
        // 测试正常创建验证器实�?
        Method method = ValidationAspect.class.getDeclaredMethod("newValidatorInstance", Class.class);
        method.setAccessible(true);
        Object validator = method.invoke(aspect, TestValidator.class);
        assertNotNull(validator);
    }

    @Test
    void testNewValidatorInstanceWithException() {
        // 测试创建验证器实例失败的情况
        class InvalidValidator extends TypedValidator {
            // 私有构造函数，无法实例�?
            private InvalidValidator() {
            }

            @Override
            protected void registerValidators() {
            }

            @Override
            public Class<?> getSupportedType() {
                return Object.class;
            }
        }

        Method method = null;
        try {
            method = ValidationAspect.class.getDeclaredMethod("newValidatorInstance", Class.class);
            method.setAccessible(true);
            method.invoke(aspect, InvalidValidator.class);
            fail("应该抛出异常");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof RuntimeException);
        }
    }

    @Test
    void testExecuteBridgeValidationWithFastMode() {
        // 测试 fast 模式下的验证
        try {
            Field field = ValidationAspect.class.getDeclaredField("validator");
            field.setAccessible(true);
            field.set(aspect, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<Business> errors = aspect.executeBridgeValidation(List.of("test"), new Class<?>[]{}, true, new Scenario[]{});
        assertTrue(errors.isEmpty());
    }

    @Test
    void testShouldKeepViolationWithNullScenes() {
        // 测试 scenes �?null 的情�?
        @SuppressWarnings("rawtypes")
        ConstraintViolation violation = Mockito.mock(ConstraintViolation.class);
        jakarta.validation.Path path = Mockito.mock(jakarta.validation.Path.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(path.toString()).thenReturn("field");
        boolean result = aspect.shouldKeepViolation(violation, null);
        assertTrue(result);
    }

    @Test
    void testShouldKeepViolationWithEmptySceneSet() throws NoSuchFieldException {
        // 测试场景集合为空的情�?
        @SuppressWarnings("rawtypes")
        ConstraintViolation violation = Mockito.mock(ConstraintViolation.class);
        jakarta.validation.Path path = Mockito.mock(jakarta.validation.Path.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(path.toString()).thenReturn("name");
        Mockito.when(violation.getRootBeanClass()).thenReturn(TestClass.class);

        // 移除字段上的 @Scene 注解
        Field field = TestClass.class.getDeclaredField("name");
        // 创建一个没�?@Scene 注解的字�?
        class TestClassWithoutScene {
            private String name;
        }
        Field fieldWithoutScene = TestClassWithoutScene.class.getDeclaredField("name");

        boolean result = aspect.shouldKeepViolation(violation, new Scenario[]{Scenario.CREATE});
        assertTrue(result);
    }

    @Test
    void testCollectValidatableArgsWithNullArgs() {
        try {
            ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
            MethodSignature signature = Mockito.mock(MethodSignature.class);
            Method method = getClass().getMethod("testMethod");
            Mockito.when(point.getSignature()).thenReturn(signature);
            Mockito.when(signature.getMethod()).thenReturn(method);
            Mockito.when(point.getArgs()).thenReturn(new Object[]{}); // 使用空数组而不�?null

            List<Object> args = aspect.collectValidatableArgs(point);
            assertNotNull(args);
            assertTrue(args.isEmpty());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testHasSkipAnnotationWithNullAnnotations() {
        boolean result = aspect.hasSkipAnnotation(null);
        assertFalse(result);
    }

    @Test
    void testHasSkipAnnotationWithNoSkipAnnotation() {
        boolean result = aspect.hasSkipAnnotation(new Annotation[]{});
        assertFalse(result);
    }

    @Test
    void testShouldSkipWithNonSkipType() {
        boolean result = aspect.shouldSkip(String.class);
        assertFalse(result);
    }

    @Test
    void testFormatValidationLocationWithEmptyField() {
        String location = aspect.formatValidationLocation(TestClass.class, "");
        assertEquals("TestClass at ", location);
    }

    @Test
    void testFormatValidationLocationWithNullClassAndNullField() {
        String location = aspect.formatValidationLocation(null, null);
        assertEquals("", location);
    }

    @Test
    void testToSceneNameWithNullScenes() {
        String sceneName = aspect.toSceneName(null);
        assertEquals(FailureConst.DEFAULT_SCENE, sceneName);
    }

    @Test
    void testToSceneNameSkipsNullAndDefault() {
        String sceneName = aspect.toSceneName(new Scenario[]{null, Scenario.DEFAULT, Scenario.CREATE});
        assertEquals("CREATE", sceneName);
    }

    @Test
    void testAroundRestoresOriginalSceneWhenPresent() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.CREATE});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{});

        Object expectedResult = new Object();
        Mockito.when(point.proceed()).thenReturn(expectedResult);

        Mockito.when(context.getScene()).thenReturn("ORIGINAL");

        Object result = aspect.around(point, validate);
        assertEquals(expectedResult, result);
        verify(context).setScene("CREATE");
        verify(context).setScene("ORIGINAL");
    }

    @Test
    void testAroundRestoresSceneOnFailure() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{""});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.CREATE});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{TestValidator.class});

        ObjectProvider<com.chao.failfast.annotation.FastValidator<Object>> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(applicationContext.getBeanProvider(TestValidator.class)).thenReturn((ObjectProvider) provider);
        Mockito.when(provider.getIfAvailable()).thenReturn(new TestValidator());

        Mockito.when(context.getScene()).thenReturn("ORIGINAL");

        assertThrows(Business.class, () -> aspect.around(point, validate));
        verify(context).setScene("CREATE");
        // Verify scene IS restored when failure occurs
        verify(context).setScene("ORIGINAL");
    }

    @Test
    void testBuildValidatorFactoryBeanNamePathInvokesGet() throws Exception {
        TestValidator testValidator = new TestValidator();
        ObjectProvider<com.chao.failfast.annotation.FastValidator<Object>> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(applicationContext.getBeanProvider(TestValidator.class)).thenReturn((ObjectProvider) provider);
        Mockito.when(provider.getIfAvailable()).thenReturn(null);
        Mockito.when(applicationContext.getBeanNamesForType(TestValidator.class)).thenReturn(new String[]{"testValidator"});
        Mockito.when(applicationContext.getBean(TestValidator.class)).thenReturn(testValidator);

        Method method = ValidationAspect.class.getDeclaredMethod("buildValidatorFactory", Class.class);
        method.setAccessible(true);
        Object factory = method.invoke(aspect, TestValidator.class);
        Method get = factory.getClass().getDeclaredMethod("get");
        get.setAccessible(true);
        Object v = get.invoke(factory);
        assertSame(testValidator, v);
    }

    @Test
    void testGetValidatorSupportedTypeWithNullDeclaredType() {
        class NullDeclaredValidator implements com.chao.failfast.annotation.FastValidator<String> {
            @Override
            public void validate(String value, com.chao.failfast.annotation.FastValidator.ValidationContext ctx) {
            }

            @Override
            public Class<?> getSupportedType() {
                return null;
            }
        }

        Class<?> type = aspect.getValidatorSupportedType(new NullDeclaredValidator());
        assertEquals(String.class, type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void testGetValidatorSupportedTypeWithRawValidator() {
        class RawValidator implements com.chao.failfast.annotation.FastValidator {
            @Override
            public void validate(Object value, com.chao.failfast.annotation.FastValidator.ValidationContext ctx) {
            }

            @Override
            public Class<?> getSupportedType() {
                return Object.class;
            }
        }

        Class<?> type = aspect.getValidatorSupportedType(new RawValidator());
        assertEquals(Object.class, type);
    }

    @Test
    void testFormatValidationLocationWithDollarDollarClassName() {
        class $$Proxy extends TestClass {
        }
        String location = aspect.formatValidationLocation($$Proxy.class, "field");
        assertNotNull(location);
    }

    @Test
    void testExecuteBridgeValidationCapturesInvalidValueBasedOnPolicy() {
        Object arg = new Object();

        @SuppressWarnings("rawtypes")
        ConstraintViolation violation = Mockito.mock(ConstraintViolation.class);
        jakarta.validation.Path path = Mockito.mock(jakarta.validation.Path.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(path.toString()).thenReturn("field");
        Mockito.when(violation.getMessage()).thenReturn("msg");
        Mockito.when(violation.getInvalidValue()).thenReturn("bad");
        Mockito.when(violation.getRootBeanClass()).thenReturn(TestClass.class);

        Mockito.when(validator.validate(arg)).thenReturn((Set) Set.of(violation));

        Ex.setContext(null);
        List<Business> errorsWithDefaultPolicy = aspect.executeBridgeValidation(List.of(arg), new Class<?>[]{}, true, new Scenario[]{Scenario.DEFAULT});
        assertEquals(1, errorsWithDefaultPolicy.size());
        assertEquals("bad", errorsWithDefaultPolicy.get(0).getInvalidValue());

        FailureContext ctx = Mockito.mock(FailureContext.class);
        ErrorPolicy noCapture = Mockito.mock(ErrorPolicy.class);
        Mockito.when(noCapture.captureInvalidValue(ctx)).thenReturn(false);
        Mockito.when(ctx.getErrorPolicy()).thenReturn(noCapture);
        Ex.setContext(ctx);
        List<Business> errorsWithCustomPolicy = aspect.executeBridgeValidation(List.of(arg), new Class<?>[]{}, true, new Scenario[]{Scenario.DEFAULT});
        assertEquals(1, errorsWithCustomPolicy.size());
        assertNull(errorsWithCustomPolicy.get(0).getInvalidValue());
    }

    @Test
    void testAroundSkipsCustomValidatorsWhenBridgeErrorsAndFailFast() throws Throwable {
        class ThrowingValidator implements com.chao.failfast.annotation.FastValidator<Object> {
            @Override
            public void validate(Object value, com.chao.failfast.annotation.FastValidator.ValidationContext ctx) {
                throw new RuntimeException("should not run");
            }

            @Override
            public Class<?> getSupportedType() {
                return Object.class;
            }
        }

        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{new Object()});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.CREATE});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{ThrowingValidator.class});

        ObjectProvider<com.chao.failfast.annotation.FastValidator<Object>> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(applicationContext.getBeanProvider(ThrowingValidator.class)).thenReturn((ObjectProvider) provider);
        Mockito.when(provider.getIfAvailable()).thenReturn(new ThrowingValidator());

        Object arg = new Object();
        @SuppressWarnings("rawtypes")
        ConstraintViolation violation = Mockito.mock(ConstraintViolation.class);
        jakarta.validation.Path path = Mockito.mock(jakarta.validation.Path.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(path.toString()).thenReturn("field");
        Mockito.when(violation.getMessage()).thenReturn("msg");
        Mockito.when(violation.getInvalidValue()).thenReturn(null);
        Mockito.when(violation.getRootBeanClass()).thenReturn(TestClass.class);
        Mockito.when(validator.validate(Mockito.any())).thenReturn((Set) Set.of(violation));

        assertThrows(Business.class, () -> aspect.around(point, validate));
    }

    @Test
    void testExecuteSingleValidatorNormalizesNullScenes() throws Exception {
        Method method = ValidationAspect.class.getDeclaredMethod("executeSingleValidator", com.chao.failfast.annotation.FastValidator.class, List.class, boolean.class, Scenario[].class, Class[].class);
        method.setAccessible(true);
        Object result = method.invoke(aspect, new TestValidator(), List.of("test"), true, null, new Class<?>[]{});
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    void testExecuteSingleValidatorNormalizesEmptyScenes() throws Exception {
        Method method = ValidationAspect.class.getDeclaredMethod("executeSingleValidator", com.chao.failfast.annotation.FastValidator.class, List.class, boolean.class, Scenario[].class, Class[].class);
        method.setAccessible(true);
        Object result = method.invoke(aspect, new TestValidator(), List.of("test"), true, new Scenario[]{}, new Class<?>[]{});
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    void testAroundSkipsBridgeWhenOnlyCustomValidatorsAndDefaultScene() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{"x"});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.DEFAULT});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{TestValidator.class});

        ObjectProvider<com.chao.failfast.annotation.FastValidator<Object>> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(applicationContext.getBeanProvider(TestValidator.class)).thenReturn((ObjectProvider) provider);
        Mockito.when(provider.getIfAvailable()).thenReturn(new TestValidator());

        Object expected = new Object();
        Mockito.when(point.proceed()).thenReturn(expected);

        Object result = aspect.around(point, validate);
        assertSame(expected, result);
        Mockito.verify(validator, Mockito.never()).validate(Mockito.any());
        Mockito.verify(validator, Mockito.never()).validate(Mockito.any(), Mockito.any());
    }

    @Test
    void testAroundRunsBridgeWhenScenesLengthGreaterThanOne() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{new Object()});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.DEFAULT, Scenario.CREATE});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{TestValidator.class});

        Mockito.when(validator.validate(Mockito.any())).thenReturn(Collections.emptySet());

        ObjectProvider<com.chao.failfast.annotation.FastValidator<Object>> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(applicationContext.getBeanProvider(TestValidator.class)).thenReturn((ObjectProvider) provider);
        Mockito.when(provider.getIfAvailable()).thenReturn(new TestValidator());

        Object expected = new Object();
        Mockito.when(point.proceed()).thenReturn(expected);

        Object result = aspect.around(point, validate);
        assertSame(expected, result);
        Mockito.verify(validator).validate(Mockito.any());
    }

    @Test
    void testAroundRunsBridgeWithGroupsUsesGroupValidate() throws Throwable {
        interface Group1 {
        }

        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{new Object()});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.DEFAULT});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{Group1.class});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{});

        Mockito.when(validator.validate(Mockito.any(), Mockito.any())).thenReturn(Collections.emptySet());

        Object expected = new Object();
        Mockito.when(point.proceed()).thenReturn(expected);

        Object result = aspect.around(point, validate);
        assertSame(expected, result);
        Mockito.verify(validator).validate(Mockito.any(), Mockito.any());
    }

    @Test
    void testAroundRestoresDefaultSceneWhenOriginalSceneNull() throws Throwable {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{new Object()});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.CREATE});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{});

        Mockito.when(validator.validate(Mockito.any())).thenReturn(Collections.emptySet());
        Mockito.when(context.getScene()).thenReturn(null);

        Object expected = new Object();
        Mockito.when(point.proceed()).thenReturn(expected);

        Object result = aspect.around(point, validate);
        assertSame(expected, result);
        Mockito.verify(context).setScene("CREATE");
        Mockito.verify(context).setScene(FailureConst.DEFAULT_SCENE);
    }

    @Test
    void testAroundWithDefaultSceneDoesNotApplyOrRestoreScene() throws Throwable {
        interface Group2 {
        }

        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{new Object()});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.DEFAULT});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{Group2.class});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{});

        Mockito.when(validator.validate(Mockito.any(), Mockito.any())).thenReturn(Collections.emptySet());

        Object expected = new Object();
        Mockito.when(point.proceed()).thenReturn(expected);

        Object result = aspect.around(point, validate);
        assertSame(expected, result);
        Mockito.verify(context, Mockito.never()).setScene(Mockito.anyString());
    }

    @Test
    void testAroundWithNullFailureContextDoesNotRestoreScene() throws Throwable {
        Ex.setContext(null);

        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(point.getArgs()).thenReturn(new Object[]{new Object()});

        Validate validate = Mockito.mock(Validate.class);
        Mockito.when(validate.scene()).thenReturn(new Scenario[]{Scenario.CREATE});
        Mockito.when(validate.groups()).thenReturn(new Class<?>[]{});
        Mockito.when(validate.fast()).thenReturn(true);
        Mockito.when(validate.value()).thenReturn(new Class[]{});

        Mockito.when(validator.validate(Mockito.any())).thenReturn(Collections.emptySet());

        Object expected = new Object();
        Mockito.when(point.proceed()).thenReturn(expected);

        Object result = aspect.around(point, validate);
        assertSame(expected, result);
    }

    // 测试方法
    public void testMethod() {
    }

    public void testMethodWithSkipValidation(@SkipValidation String value) {
    }

    public void testMethodWithServletRequest(ServletRequest request) {
    }

    // 测试�?
    static class TestClass {
        @Scene(value = {Scenario.CREATE, Scenario.UPDATE})
        private String name;
    }

    // 测试验证�?
    static class TestValidator extends TypedValidator {
        @Override
        protected void registerValidators() {
            register(String.class, (s, ctx) -> {
                if (s == null || s.isEmpty()) {
                    ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL);
                }
            });
        }

        @Override
        public Class<?> getSupportedType() {
            return String.class;
        }
    }

    // 测试验证器（返回Object.class�?
    static class ObjectValidator extends TypedValidator {
        @Override
        protected void registerValidators() {
        }

        @Override
        public Class<?> getSupportedType() {
            return Object.class;
        }
    }

    // 测试验证器（用于泛型推断测试�?
    static class GenericValidator implements com.chao.failfast.annotation.FastValidator<String> {
        @Override
        public void validate(String value, com.chao.failfast.annotation.FastValidator.ValidationContext ctx) {
        }

        @Override
        public Class<?> getSupportedType() {
            return Object.class; // 返回Object.class，触发泛型推�?
        }
    }

    static class NonGenericValidator implements com.chao.failfast.annotation.FastValidator<Object> {
        @Override
        public void validate(Object value, com.chao.failfast.annotation.FastValidator.ValidationContext ctx) {
        }

        @Override
        public Class<?> getSupportedType() {
            return Object.class;
        }
    }
} 
