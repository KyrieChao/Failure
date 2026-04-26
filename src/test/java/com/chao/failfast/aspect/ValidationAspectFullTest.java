package com.chao.failfast.aspect;

import com.chao.failfast.validator.FastValidator;
import com.chao.failfast.annotation.Scene;
import com.chao.failfast.annotation.SkipValidation;
import com.chao.failfast.annotation.Validate;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.constant.Scenario;
import com.chao.failfast.exception.Business;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.validator.TypedValidator;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ValidationAspect完整测试")
class ValidationAspectFullTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private Validator validator;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private ObjectProvider<FastValidator<Object>> validatorProvider;

    private ValidationAspect validationAspect;

    @BeforeEach
    void setUp() throws Exception {
        // 清除缓存
        Field cache = ValidationAspect.class.getDeclaredField("VALIDATOR_CACHE");
        cache.setAccessible(true);
        ((ConcurrentHashMap<?, ?>) cache.get(null)).clear();

        Field factoryCache = ValidationAspect.class.getDeclaredField("VALIDATOR_FACTORY_CACHE");
        factoryCache.setAccessible(true);
        ((ConcurrentHashMap<?, ?>) factoryCache.get(null)).clear();

        validationAspect = new ValidationAspect();
        setField(validationAspect, "applicationContext", applicationContext);
        setField(validationAspect, "validator", validator);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = ValidationAspect.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    

    /**
     * 通过反射调用私有方法（增强版：智能处理 List 实现类和参数类型匹配）
     */
    private Object invokePrivateMethod(Object target, String methodName, Object... args) throws Exception {
        Class<?> clazz = target.getClass();

        // 1. 尝试直接构建类型数组
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                paramTypes[i] = null;
            } else {
                Class<?> type = args[i].getClass();
                if (List.class.isAssignableFrom(type)) {
                    type = List.class;
                }
                paramTypes[i] = type;
            }
        }

        try {
            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (NoSuchMethodException e) {
            // 2. 备用策略：遍历同名方法，手动比对兼容性
            for (Method candidate : clazz.getDeclaredMethods()) {
                if (!candidate.getName().equals(methodName)) continue;
                if (candidate.getParameterCount() != args.length) continue;

                Class<?>[] candidateTypes = candidate.getParameterTypes();
                boolean match = true;

                for (int i = 0; i < args.length; i++) {
                    Class<?> expectedType = candidateTypes[i];

                    if (args[i] == null) {
                        // null 只能匹配引用类型，不能匹配基本类型
                        if (expectedType.isPrimitive()) {
                            match = false;
                            break;
                        }
                    } else {
                        Class<?> actualType = args[i].getClass();

                        // 特殊处理：List 实现类视为 List 接口
                        if (List.class.isAssignableFrom(actualType)) {
                            actualType = List.class;
                        }

                        // 【核心修复】处理基本类型与包装类的兼容性
                        // 如果期望的是基本类型 (如 boolean)，而实际是包装类 (如 Boolean)，视为匹配
                        if (expectedType.isPrimitive() && actualType.isAssignableFrom(getWrapperType(expectedType))) {
                            // 匹配成功，继续下一个参数
                            continue;
                        }
                        // 如果期望的是包装类，实际是基本类型 (虽然通过 Object... 传参不太可能发生，但为了健壮性)
                        if (actualType.isPrimitive() && expectedType.isAssignableFrom(getWrapperType(actualType))) {
                            continue;
                        }

                        // 常规赋值兼容性检查
                        if (!expectedType.isAssignableFrom(actualType)) {
                            match = false;
                            break;
                        }
                    }
                }

                if (match) {
                    candidate.setAccessible(true);
                    return candidate.invoke(target, args);
                }
            }

            throw new NoSuchMethodException("Cannot find method " + methodName + " with compatible arguments. " +
                    "Args types: " + java.util.Arrays.toString(paramTypes));
        }
    }

    // 辅助方法：获取基本类型对应的包装类
    private Class<?> getWrapperType(Class<?> primitiveType) {
        if (primitiveType == boolean.class) return Boolean.class;
        if (primitiveType == int.class) return Integer.class;
        if (primitiveType == long.class) return Long.class;
        if (primitiveType == double.class) return Double.class;
        if (primitiveType == float.class) return Float.class;
        if (primitiveType == char.class) return Character.class;
        if (primitiveType == byte.class) return Byte.class;
        if (primitiveType == short.class) return Short.class;
        if (primitiveType == void.class) return Void.class;
        return primitiveType;
    }

    // ==================== around 方法测试 ====================

    @Test
    @DisplayName("around方法 - 无验证器、无组、默认场景 - 直接执行")
    void testAroundNoValidation() throws Throwable {
        Validate validate = mock(Validate.class);
        when(validate.scene()).thenReturn(new Scenario[]{});
        when(validate.groups()).thenReturn(new Class<?>[]{});
        when(validate.value()).thenReturn(new Class[]{});
        when(validate.fast()).thenReturn(true);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethod"));
        when(joinPoint.proceed()).thenReturn("result");

        Object result = validationAspect.around(joinPoint, validate);

        assertThat(result).isEqualTo("result");
        verify(joinPoint).proceed();
    }



    @Test
    @DisplayName("around方法 - 场景设置和恢复")
    void testAroundSceneSettingAndRestoration() throws Throwable {
        Validate validate = mock(Validate.class);
        when(validate.scene()).thenReturn(new Scenario[]{Scenario.CREATE, Scenario.UPDATE});
        when(validate.groups()).thenReturn(new Class<?>[]{TestGroup.class});
        when(validate.value()).thenReturn(new Class[]{});
        when(validate.fast()).thenReturn(true);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethod"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn("result");

        Object result = validationAspect.around(joinPoint, validate);

        assertThat(result).isEqualTo("result");
    }

    @Test
    @DisplayName("around方法 - scenes为null的处理")
    void testAroundWithNullScenes() throws Throwable {
        Validate validate = mock(Validate.class);
        when(validate.scene()).thenReturn(null);
        when(validate.groups()).thenReturn(new Class<?>[]{TestGroup.class});
        when(validate.value()).thenReturn(new Class[]{});
        when(validate.fast()).thenReturn(true);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethod"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn("result");

        Object result = validationAspect.around(joinPoint, validate);

        assertThat(result).isEqualTo("result");
    }

    @Test
    @DisplayName("around方法 - groups为null的处理")
    void testAroundWithNullGroups() throws Throwable {
        Validate validate = mock(Validate.class);
        when(validate.scene()).thenReturn(new Scenario[]{Scenario.DEFAULT});
        when(validate.groups()).thenReturn(null);
        when(validate.value()).thenReturn(new Class[]{});
        when(validate.fast()).thenReturn(true);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethod"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn("result");

        Object result = validationAspect.around(joinPoint, validate);

        assertThat(result).isEqualTo("result");
    }

    @Test
    @DisplayName("around方法 - validatorClasses为null的处理")
    void testAroundWithNullValidators() throws Throwable {
        Validate validate = mock(Validate.class);
        when(validate.scene()).thenReturn(new Scenario[]{Scenario.DEFAULT});
        when(validate.groups()).thenReturn(new Class<?>[]{TestGroup.class});
        when(validate.value()).thenReturn(null);
        when(validate.fast()).thenReturn(true);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethod"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn("result");

        Object result = validationAspect.around(joinPoint, validate);

        assertThat(result).isEqualTo("result");
    }

    // ==================== parseFieldName 测试 ====================

    @Test
    @DisplayName("parseFieldName - 简单字段名")
    void testParseFieldNameSimple() throws Exception {
        String fieldName = (String) invokePrivateMethod(validationAspect, "parseFieldName", "name");
        assertThat(fieldName).isEqualTo("name");
    }

    @Test
    @DisplayName("parseFieldName - 带点的路径")
    void testParseFieldNameWithDot() throws Exception {
        String fieldName = (String) invokePrivateMethod(validationAspect,"parseFieldName", "user.name");
        assertThat(fieldName).isEqualTo("user");
    }

    @Test
    @DisplayName("parseFieldName - 带索引的路径")
    void testParseFieldNameWithIndex() throws Exception {
        String fieldName = (String) invokePrivateMethod(validationAspect,"parseFieldName", "items[0]");
        assertThat(fieldName).isEqualTo("items");
    }

    @Test
    @DisplayName("parseFieldName - 带点和索引的路径")
    void testParseFieldNameWithDotAndIndex() throws Exception {
        String fieldName = (String) invokePrivateMethod(validationAspect,"parseFieldName", "users[0].name");
        assertThat(fieldName).isEqualTo("users");
    }

    // ==================== findField 测试 ====================

    @Test
    @DisplayName("findField - 找到字段")
    void testFindFieldFound() throws Exception {
        Field field = (Field) invokePrivateMethod(validationAspect,"findField", TestObject.class, "name");
        assertThat(field).isNotNull();
        assertThat(field.getName()).isEqualTo("name");
    }

    @Test
    @DisplayName("findField - 找不到字段")
    void testFindFieldNotFound() throws Exception {
        Field field = (Field) invokePrivateMethod(validationAspect,"findField", TestObject.class, "nonExistent");
        assertThat(field).isNull();
    }

    @Test
    @DisplayName("findField - 从父类找到字段")
    void testFindFieldFromParent() throws Exception {
        Field field = (Field) invokePrivateMethod(validationAspect,"findField", TestChildObject.class, "name");
        assertThat(field).isNotNull();
        assertThat(field.getName()).isEqualTo("name");
    }

    // ==================== getSceneValues 测试 ====================

    @Test
    @DisplayName("getSceneValues - 有@Scene注解")
    void testGetSceneValuesWithAnnotation() throws Exception {
        Field field = TestObject.class.getDeclaredField("name");
        Set<Scenario> scenes = (Set<Scenario>) invokePrivateMethod(validationAspect,"getSceneValues", field);
        assertThat(scenes).contains(Scenario.DEFAULT);
    }

    @Test
    @DisplayName("getSceneValues - 无@Scene注解")
    void testGetSceneValuesWithoutAnnotation() throws Exception {
        Field field = TestObjectNoScene.class.getDeclaredField("value");
        Set<Scenario> scenes = (Set<Scenario>) invokePrivateMethod(validationAspect,"getSceneValues", field);
        assertThat(scenes).isEmpty();
    }

    // ==================== collectValidatableArgs 测试 ====================

    @Test
    @DisplayName("collectValidatableArgs - 空参数")
    void testCollectValidatableArgsEmpty() throws Exception {
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethod"));

        List<Object> args = (List<Object>) invokePrivateMethod(validationAspect,"collectValidatableArgs", joinPoint);
        assertThat(args).isEmpty();
    }

    @Test
    @DisplayName("collectValidatableArgs - null参数被过滤")
    void testCollectValidatableArgsWithNull() throws Exception {
        when(joinPoint.getArgs()).thenReturn(new Object[]{null, "test"});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethodWithArgs", String.class, String.class));

        List<Object> args = (List<Object>) invokePrivateMethod(validationAspect,"collectValidatableArgs", joinPoint);
        assertThat(args).hasSize(1);
        assertThat(args.get(0)).isEqualTo("test");
    }

    @Test
    @DisplayName("collectValidatableArgs - @SkipValidation参数被过滤")
    void testCollectValidatableArgsWithSkipValidation() throws Exception {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
        when(joinPoint.getSignature()).thenReturn(methodSignature);

        Method method = getClass().getMethod("testMethodWithSkipAnnotation", String.class, String.class);
        when(methodSignature.getMethod()).thenReturn(method);

        List<Object> args = (List<Object>) invokePrivateMethod(validationAspect,"collectValidatableArgs", joinPoint);
        assertThat(args).hasSize(1);
        assertThat(args.get(0)).isEqualTo("arg1");
    }

    @Test
    @DisplayName("collectValidatableArgs - SKIP_TYPES被过滤")
    void testCollectValidatableArgsWithSkipTypes() throws Exception {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"test", mock(InputStream.class)});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethodWithArgs", String.class, InputStream.class));

        List<Object> args = (List<Object>) invokePrivateMethod(validationAspect,"collectValidatableArgs", joinPoint);
        assertThat(args).hasSize(1);
        assertThat(args.get(0)).isEqualTo("test");
    }

    @Test
    @DisplayName("collectValidatableArgs - ServletRequest被过滤")
    void testCollectValidatableArgsWithServletRequest() throws Exception {
        when(joinPoint.getArgs()).thenReturn(new Object[]{mock(ServletRequest.class), "test"});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethodWithArgs", ServletRequest.class, String.class));

        List<Object> args = (List<Object>) invokePrivateMethod(validationAspect,"collectValidatableArgs", joinPoint);
        assertThat(args).hasSize(1);
        assertThat(args.get(0)).isEqualTo("test");
    }

    @Test
    @DisplayName("collectValidatableArgs - ServletResponse被过滤")
    void testCollectValidatableArgsWithServletResponse() throws Exception {
        when(joinPoint.getArgs()).thenReturn(new Object[]{mock(ServletResponse.class), "test"});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethodWithArgs", ServletResponse.class, String.class));

        List<Object> args = (List<Object>) invokePrivateMethod(validationAspect,"collectValidatableArgs", joinPoint);
        assertThat(args).hasSize(1);
        assertThat(args.get(0)).isEqualTo("test");
    }

    @Test
    @DisplayName("collectValidatableArgs - HttpSession被过滤")
    void testCollectValidatableArgsWithHttpSession() throws Exception {
        when(joinPoint.getArgs()).thenReturn(new Object[]{mock(HttpSession.class), "test"});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethodWithArgs", HttpSession.class, String.class));

        List<Object> args = (List<Object>) invokePrivateMethod(validationAspect,"collectValidatableArgs", joinPoint);
        assertThat(args).hasSize(1);
        assertThat(args.get(0)).isEqualTo("test");
    }

    @Test
    @DisplayName("collectValidatableArgs - MultipartFile被过滤")
    void testCollectValidatableArgsWithMultipartFile() throws Exception {
        when(joinPoint.getArgs()).thenReturn(new Object[]{mock(MultipartFile.class), "test"});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethodWithArgs", MultipartFile.class, String.class));

        List<Object> args = (List<Object>) invokePrivateMethod(validationAspect,"collectValidatableArgs", joinPoint);
        assertThat(args).hasSize(1);
        assertThat(args.get(0)).isEqualTo("test");
    }

    @Test
    @DisplayName("collectValidatableArgs - OutputStream被过滤")
    void testCollectValidatableArgsWithOutputStream() throws Exception {
        when(joinPoint.getArgs()).thenReturn(new Object[]{mock(OutputStream.class), "test"});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethodWithArgs", OutputStream.class, String.class));

        List<Object> args = (List<Object>) invokePrivateMethod(validationAspect,"collectValidatableArgs", joinPoint);
        assertThat(args).hasSize(1);
        assertThat(args.get(0)).isEqualTo("test");
    }

    @Test
    @DisplayName("collectValidatableArgs - Reader被过滤")
    void testCollectValidatableArgsWithReader() throws Exception {
        when(joinPoint.getArgs()).thenReturn(new Object[]{mock(Reader.class), "test"});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethodWithArgs", Reader.class, String.class));

        List<Object> args = (List<Object>) invokePrivateMethod(validationAspect,"collectValidatableArgs", joinPoint);
        assertThat(args).hasSize(1);
        assertThat(args.get(0)).isEqualTo("test");
    }

    @Test
    @DisplayName("collectValidatableArgs - Writer被过滤")
    void testCollectValidatableArgsWithWriter() throws Exception {
        when(joinPoint.getArgs()).thenReturn(new Object[]{mock(Writer.class), "test"});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getClass().getMethod("testMethodWithArgs", Writer.class, String.class));

        List<Object> args = (List<Object>) invokePrivateMethod(validationAspect,"collectValidatableArgs", joinPoint);
        assertThat(args).hasSize(1);
        assertThat(args.get(0)).isEqualTo("test");
    }

    // ==================== getOrCreateValidator 测试 ====================

    @Test
    @DisplayName("getOrCreateValidator - 创建新实例")
    void testGetOrCreateValidatorNewInstance() throws Exception {
        when(applicationContext.getBeanProvider((Class<?>) TestFastValidator.class)).thenReturn((ObjectProvider) validatorProvider);
        when(validatorProvider.getIfAvailable()).thenReturn(null);
        when(applicationContext.getBeanNamesForType(TestFastValidator.class)).thenReturn(new String[]{});

        FastValidator<Object> result1 = (FastValidator<Object>) invokePrivateMethod(validationAspect,"getOrCreateValidator", TestFastValidator.class);
        FastValidator<Object> result2 = (FastValidator<Object>) invokePrivateMethod(validationAspect,"getOrCreateValidator", TestFastValidator.class);

        assertThat(result1).isNotNull();
        assertThat(result1).isSameAs(result2); // 缓存
    }

    // ==================== buildValidatorFactory 测试 ====================

    @Test
    @DisplayName("buildValidatorFactory - 从ObjectProvider获取")
    void testBuildValidatorFactoryFromProvider() throws Exception {
        TestFastValidator validator = new TestFastValidator();
        when(applicationContext.getBeanProvider((Class<?>) TestFastValidator.class)).thenReturn((ObjectProvider) validatorProvider);
        when(validatorProvider.getIfAvailable()).thenReturn(validator);

        Object factory = (Object) invokePrivateMethod(validationAspect,"buildValidatorFactory", TestFastValidator.class);
        assertThat(factory).isNotNull();
    }

    @Test
    @DisplayName("buildValidatorFactory - 从bean名称获取")
    void testBuildValidatorFactoryFromBeanName() throws Exception {
        TestFastValidator validator = new TestFastValidator();
        when(applicationContext.getBeanProvider((Class<?>) TestFastValidator.class)).thenReturn((ObjectProvider) validatorProvider);
        when(validatorProvider.getIfAvailable()).thenReturn(null);
        when(applicationContext.getBeanNamesForType(TestFastValidator.class)).thenReturn(new String[]{"testValidator"});
        when(applicationContext.getBean(TestFastValidator.class)).thenReturn(validator);

        Object factory = (Object) invokePrivateMethod(validationAspect,"buildValidatorFactory", TestFastValidator.class);
        assertThat(factory).isNotNull();
    }

    @Test
    @DisplayName("buildValidatorFactory - 创建反射实例")
    void testBuildValidatorFactoryReflection() throws Exception {
        when(applicationContext.getBeanProvider((Class<?>) TestFastValidator.class)).thenReturn((ObjectProvider) validatorProvider);
        when(validatorProvider.getIfAvailable()).thenReturn(null);
        when(applicationContext.getBeanNamesForType(TestFastValidator.class)).thenReturn(new String[]{});

        Object factory = invokePrivateMethod(validationAspect,"buildValidatorFactory", TestFastValidator.class);
        assertThat(factory).isNotNull();
    }

    @Test
    @DisplayName("buildValidatorFactory - applicationContext为null")
    void testBuildValidatorFactoryNullContext() throws Exception {
        setField(validationAspect, "applicationContext", null);

        Object factory = invokePrivateMethod(validationAspect,"buildValidatorFactory", TestFastValidator.class);
        assertThat(factory).isNotNull();
    }

    // ==================== newValidatorInstance 测试 ====================

    @Test
    @DisplayName("newValidatorInstance - 成功创建实例")
    void testNewValidatorInstanceSuccess() throws Exception {
        FastValidator<Object> validator = (FastValidator<Object>) invokePrivateMethod(validationAspect,"newValidatorInstance", TestFastValidator.class);
        assertThat(validator).isNotNull();
        assertThat(validator).isInstanceOf(TestFastValidator.class);
    }

    @Test
    @DisplayName("newValidatorInstance - 创建失败抛出异常")
    void testNewValidatorInstanceFailure() {
        assertThatThrownBy(() -> invokePrivateMethod(validationAspect,"newValidatorInstance", AbstractFastValidator.class))
                .isInstanceOf(Exception.class);
    }

    // ==================== getValidatorSupportedType 测试 ====================

    @Test
    @DisplayName("getValidatorSupportedType - 从getSupportedType获取")
    void testGetValidatorSupportedTypeFromMethod() throws Exception {
        TestPlainValidator validator = new TestPlainValidator();
        Class<?> type = (Class<?>) invokePrivateMethod(validationAspect,"getValidatorSupportedType", validator);
        assertThat(type).isEqualTo(String.class);
    }

    @Test
    @DisplayName("getValidatorSupportedType - 从泛型参数推断")
    void testGetValidatorSupportedTypeFromGeneric() throws Exception {
        TestFastValidator validator = new TestFastValidator();
        Class<?> type = (Class<?>) invokePrivateMethod(validationAspect,"getValidatorSupportedType", validator);
        assertThat(type).isEqualTo(Object.class);
    }

    @Test
    @DisplayName("getValidatorSupportedType - 返回Object.class当无法推断")
    void testGetValidatorSupportedTypeObject() throws Exception {
        TestPlainValidatorObjectType validator = new TestPlainValidatorObjectType();
        Class<?> type = (Class<?>) invokePrivateMethod(validationAspect,"getValidatorSupportedType", validator);
        assertThat(type).isEqualTo(Object.class);
    }

    // ==================== shouldSkip 测试 ====================

    @Test
    @DisplayName("shouldSkip - ServletRequest")
    void testShouldSkipServletRequest() throws Exception {
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldSkip", ServletRequest.class);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldSkip - ServletResponse")
    void testShouldSkipServletResponse() throws Exception {
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldSkip", ServletResponse.class);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldSkip - HttpSession")
    void testShouldSkipHttpSession() throws Exception {
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldSkip", HttpSession.class);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldSkip - MultipartFile")
    void testShouldSkipMultipartFile() throws Exception {
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldSkip", MultipartFile.class);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldSkip - InputStream")
    void testShouldSkipInputStream() throws Exception {
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldSkip", InputStream.class);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldSkip - OutputStream")
    void testShouldSkipOutputStream() throws Exception {
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldSkip", OutputStream.class);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldSkip - Reader")
    void testShouldSkipReader() throws Exception {
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldSkip", Reader.class);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldSkip - Writer")
    void testShouldSkipWriter() throws Exception {
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldSkip", Writer.class);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldSkip - 普通类型")
    void testShouldSkipNormalType() throws Exception {
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldSkip", String.class);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("shouldSkip - 子类")
    void testShouldSkipSubclass() throws Exception {
        // InputStream的子类应该也被跳过
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldSkip", java.io.ByteArrayInputStream.class);
        assertThat(result).isTrue();
    }

    // ==================== hasSkipAnnotation 测试 ====================

    @Test
    @DisplayName("hasSkipAnnotation - 有@SkipValidation")
    void testHasSkipAnnotationTrue() throws Exception {
        Annotation[] annotations = new Annotation[]{mock(SkipValidation.class)};
        boolean result = (boolean) invokePrivateMethod(validationAspect,"hasSkipAnnotation", (Object) annotations);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasSkipAnnotation - 无@SkipValidation")
    void testHasSkipAnnotationFalse() throws Exception {
        Annotation[] annotations = new Annotation[]{mock(Validate.class)};
        boolean result = (boolean) invokePrivateMethod(validationAspect,"hasSkipAnnotation", (Object) annotations);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasSkipAnnotation - null数组")
    void testHasSkipAnnotationNull() throws Exception {
        boolean result = (boolean) invokePrivateMethod(validationAspect,"hasSkipAnnotation", (Object) null);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasSkipAnnotation - 空数组")
    void testHasSkipAnnotationEmpty() throws Exception {
        Annotation[] annotations = new Annotation[]{};
        boolean result = (boolean) invokePrivateMethod(validationAspect,"hasSkipAnnotation", (Object) annotations);
        assertThat(result).isFalse();
    }

    // ==================== executeBridgeValidation 测试 ====================

    @Test
    @DisplayName("executeBridgeValidation - validator为null")
    void testExecuteBridgeValidationNullValidator() throws Exception {
        setField(validationAspect, "validator", null);
        
        List<Object> args = List.of(new TestObject("test"));
        Class<?>[] groups = new Class<?>[]{};
        boolean fast = true;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeBridgeValidation", args, groups, fast, scenes);
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("executeBridgeValidation - 无验证错误")
    void testExecuteBridgeValidationNoErrors() throws Exception {
        when(validator.validate(any(), any(Class[].class))).thenReturn(Collections.emptySet());
        
        List<Object> args = List.of(new TestObject("test"));
        Class<?>[] groups = new Class<?>[]{};
        boolean fast = true;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeBridgeValidation", args, groups, fast, scenes);
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("executeBridgeValidation - 有验证错误")
    void testExecuteBridgeValidationWithErrors() throws Exception {
        @SuppressWarnings("rawtypes")
        ConstraintViolation violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("name");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Validation error");
        when(violation.getRootBeanClass()).thenReturn(TestObject.class);
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Set.of(violation);
        when(validator.validate(any(), any(Class[].class))).thenReturn(violations);
        
        List<Object> args = List.of(new TestObject("test"));
        Class<?>[] groups = new Class<?>[]{};
        boolean fast = false;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeBridgeValidation", args, groups, fast, scenes);
        assertThat(errors).hasSize(1);
    }

    @Test
    @DisplayName("executeBridgeValidation - fast模式")
    void testExecuteBridgeValidationFastMode() throws Exception {
        @SuppressWarnings("rawtypes")
        ConstraintViolation violation1 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        when(path1.toString()).thenReturn("name");
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("Error 1");
        when(violation1.getRootBeanClass()).thenReturn(TestObject.class);
        
        @SuppressWarnings("rawtypes")
        ConstraintViolation violation2 = mock(ConstraintViolation.class);
        Path path2 = mock(Path.class);
        when(path2.toString()).thenReturn("value");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("Error 2");
        when(violation2.getRootBeanClass()).thenReturn(TestObject.class);
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Set.of(violation1, violation2);
        when(validator.validate(any(), any(Class[].class))).thenReturn(violations);
        
        List<Object> args = List.of(new TestObject("test"), new TestObject("test2"));
        Class<?>[] groups = new Class<?>[]{};
        boolean fast = true;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeBridgeValidation", args, groups, fast, scenes);
        assertThat(errors).hasSize(1);
    }

    @Test
    @DisplayName("executeBridgeValidation - 带groups")
    void testExecuteBridgeValidationWithGroups() throws Exception {
        when(validator.validate(any(), any(Class[].class))).thenReturn(Collections.emptySet());
        
        List<Object> args = List.of(new TestObject("test"));
        Class<?>[] groups = new Class<?>[]{TestGroup.class};
        boolean fast = true;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeBridgeValidation", args, groups, fast, scenes);
        assertThat(errors).isEmpty();
        verify(validator).validate(any(), eq(groups));
    }

    // ==================== shouldKeepViolation 测试 ====================

    @Test
    @DisplayName("shouldKeepViolation - scenes为null")
    void testShouldKeepViolationNullScenes() throws Exception {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldKeepViolation", violation, (Object) null);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldKeepViolation - scenes为空")
    void testShouldKeepViolationEmptyScenes() throws Exception {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldKeepViolation", violation, new Scenario[]{});
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldKeepViolation - 包含DEFAULT场景")
    void testShouldKeepViolationWithDefaultScene() throws Exception {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldKeepViolation", violation, new Scenario[]{Scenario.DEFAULT});
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldKeepViolation - 找不到字段")
    void testShouldKeepViolationFieldNotFound() throws Exception {
        @SuppressWarnings("rawtypes")
        ConstraintViolation violation = mock(ConstraintViolation.class);
        when(violation.getRootBeanClass()).thenReturn(TestObject.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("nonExistentField");
        when(violation.getPropertyPath()).thenReturn(path);
        
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldKeepViolation", violation, new Scenario[]{Scenario.CREATE});
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldKeepViolation - 字段无@Scene注解")
    void testShouldKeepViolationNoSceneAnnotation() throws Exception {
        @SuppressWarnings("rawtypes")
        ConstraintViolation violation = mock(ConstraintViolation.class);
        when(violation.getRootBeanClass()).thenReturn(TestObjectNoScene.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("value");
        when(violation.getPropertyPath()).thenReturn(path);
        
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldKeepViolation", violation, new Scenario[]{Scenario.CREATE});
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldKeepViolation - 场景匹配")
    void testShouldKeepViolationSceneMatch() throws Exception {
        @SuppressWarnings("rawtypes")
        ConstraintViolation violation = mock(ConstraintViolation.class);
        when(violation.getRootBeanClass()).thenReturn(TestObject.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("name");
        when(violation.getPropertyPath()).thenReturn(path);
        
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldKeepViolation", violation, new Scenario[]{Scenario.DEFAULT});
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldKeepViolation - 场景不匹配")
    void testShouldKeepViolationSceneNoMatch() throws Exception {
        @SuppressWarnings("rawtypes")
        ConstraintViolation violation = mock(ConstraintViolation.class);
        when(violation.getRootBeanClass()).thenReturn(TestObject.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("name");
        when(violation.getPropertyPath()).thenReturn(path);
        
        boolean result = (boolean) invokePrivateMethod(validationAspect,"shouldKeepViolation", violation, new Scenario[]{Scenario.CREATE});
        assertThat(result).isFalse();
    }

    // ==================== executeValidators 测试 ====================

    @Test
    @DisplayName("executeValidators - 空验证器列表")
    void testExecuteValidatorsEmpty() throws Exception {
        List<Object> args = List.of("test");
        boolean fast = true;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        Class<?>[] groups = new Class<?>[]{};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeValidators", new Class[]{}, args, fast, scenes, groups);
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("executeValidators - 单个验证器通过")
    void testExecuteValidatorsSinglePass() throws Exception {
        List<Object> args = List.of("test");
        boolean fast = true;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        Class<?>[] groups = new Class<?>[]{};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeValidators", new Class[]{TestFastValidator.class}, args, fast, scenes, groups);
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("executeValidators - 单个验证器失败")
    void testExecuteValidatorsSingleFail() throws Exception {
        List<Object> args = List.of("test");
        boolean fast = true;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        Class<?>[] groups = new Class<?>[]{};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeValidators", new Class[]{FailingFastValidator.class}, args, fast, scenes, groups);
        assertThat(errors).hasSize(1);
    }

    @Test
    @DisplayName("executeValidators - 多个验证器")
    void testExecuteValidatorsMultiple() throws Exception {
        List<Object> args = List.of("test");
        boolean fast = false;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        Class<?>[] groups = new Class<?>[]{};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeValidators",
                new Class[]{FailingFastValidator.class, AnotherFailingFastValidator.class}, 
                args, fast, scenes, groups);
        assertThat(errors).hasSize(2);
    }

    @Test
    @DisplayName("executeValidators - fast模式")
    void testExecuteValidatorsFastMode() throws Exception {
        List<Object> args = List.of("test");
        boolean fast = true;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        Class<?>[] groups = new Class<?>[]{};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeValidators",
                new Class[]{FailingFastValidator.class, AnotherFailingFastValidator.class}, 
                args, fast, scenes, groups);
        assertThat(errors).hasSize(1);
    }

    // ==================== executeSingleValidator 测试 ====================

    @Test
    @DisplayName("executeSingleValidator - TypedValidator")
    void testExecuteSingleValidatorTyped() throws Exception {
        TestTypedValidator validator = new TestTypedValidator();
        List<Object> args = List.of("test");
        boolean fast = true;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        Class<?>[] groups = new Class<?>[]{};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeSingleValidator", validator, args, fast, scenes, groups);
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("executeSingleValidator - TypedValidator失败")
    void testExecuteSingleValidatorTypedFail() throws Exception {
        TestTypedValidatorFailing validator = new TestTypedValidatorFailing();
        List<Object> args = List.of("test");
        boolean fast = true;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        Class<?>[] groups = new Class<?>[]{};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeSingleValidator", validator, args, fast, scenes, groups);
        assertThat(errors).hasSize(1);
    }

    @Test
    @DisplayName("executeSingleValidator - 普通验证器")
    void testExecuteSingleValidatorPlain() throws Exception {
        TestPlainValidator validator = new TestPlainValidator();
        List<Object> args = List.of("test");
        boolean fast = true;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        Class<?>[] groups = new Class<?>[]{};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeSingleValidator", validator, args, fast, scenes, groups);
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("executeSingleValidator - 普通验证器失败")
    void testExecuteSingleValidatorPlainFail() throws Exception {
        TestPlainValidatorFailing validator = new TestPlainValidatorFailing();
        List<Object> args = List.of("test");
        boolean fast = true;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        Class<?>[] groups = new Class<?>[]{};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeSingleValidator", validator, args, fast, scenes, groups);
        assertThat(errors).hasSize(1);
    }

    @Test
    @DisplayName("executeSingleValidator - 类型不匹配")
    void testExecuteSingleValidatorTypeMismatch() throws Exception {
        TestPlainValidator validator = new TestPlainValidator();
        List<Object> args = List.of(123); // 类型不匹配
        boolean fast = true;
        Scenario[] scenes = new Scenario[]{Scenario.DEFAULT};
        Class<?>[] groups = new Class<?>[]{};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeSingleValidator", validator, args, fast, scenes, groups);
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("executeSingleValidator - scenes为null")
    void testExecuteSingleValidatorNullScenes() throws Exception {
        TestFastValidator validator = new TestFastValidator();
        List<Object> args = List.of("test");
        Boolean fast = true;
        Scenario[] scenes = null;
        Class<?>[] groups = new Class<?>[]{};
        
        List<Business> errors = (List<Business>) invokePrivateMethod(validationAspect,"executeSingleValidator", validator, args, fast, scenes, groups);
        assertThat(errors).isEmpty();
    }

    // ==================== executeTypedValidator 测试 ====================

    @Test
    @DisplayName("executeTypedValidator - 正常执行")
    void testExecuteTypedValidator() throws Exception {
        TestTypedValidator validator = new TestTypedValidator();
        List<Object> args = List.of("test", 123);
        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(true, new Scenario[]{Scenario.DEFAULT}, new Class<?>[]{});
        
        invokePrivateMethod(validationAspect,"executeTypedValidator", validator, args, ctx);
        assertThat(ctx.isValid()).isTrue();
    }

    @Test
    @DisplayName("executeTypedValidator - 验证失败")
    void testExecuteTypedValidatorFail() throws Exception {
        TestTypedValidatorFailing validator = new TestTypedValidatorFailing();
        List<Object> args = List.of("test");
        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(true, new Scenario[]{Scenario.DEFAULT}, new Class<?>[]{});
        
        invokePrivateMethod(validationAspect,"executeTypedValidator", validator, args, ctx);
        assertThat(ctx.isValid()).isFalse();
    }

    // ==================== executePlainValidator 测试 ====================

    @Test
    @DisplayName("executePlainValidator - 支持类型")
    void testExecutePlainValidatorSupportedType() throws Exception {
        TestPlainValidator validator = new TestPlainValidator();
        List<Object> args = List.of("test");
        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(true, new Scenario[]{Scenario.DEFAULT}, new Class<?>[]{});
        
        invokePrivateMethod(validationAspect,"executePlainValidator", validator, args, ctx);
        assertThat(ctx.isValid()).isTrue();
    }

    @Test
    @DisplayName("executePlainValidator - 类型不匹配")
    void testExecutePlainValidatorTypeMismatch() throws Exception {
        TestPlainValidator validator = new TestPlainValidator();
        List<Object> args = List.of(123);
        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(true, new Scenario[]{Scenario.DEFAULT}, new Class<?>[]{});
        
        invokePrivateMethod(validationAspect,"executePlainValidator", validator, args, ctx);
        assertThat(ctx.isValid()).isTrue();
    }

    @Test
    @DisplayName("executePlainValidator - Object类型警告")
    void testExecutePlainValidatorObjectType() throws Exception {
        TestPlainValidatorObjectType validator = new TestPlainValidatorObjectType();
        List<Object> args = List.of("test");
        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(true, new Scenario[]{Scenario.DEFAULT}, new Class<?>[]{});
        
        invokePrivateMethod(validationAspect,"executePlainValidator", validator, args, ctx);
        assertThat(ctx.isValid()).isTrue();
    }

    // ==================== formatValidationLocation 测试 ====================

    @Test
    @DisplayName("formatValidationLocation - 普通类")
    void testFormatValidationLocationNormal() throws Exception {
        String location = (String) invokePrivateMethod(validationAspect,"formatValidationLocation", TestObject.class, "name");
        assertThat(location).contains("TestObject");
        assertThat(location).contains("name");
    }

    @Test
    @DisplayName("formatValidationLocation - CGLIB代理类")
    void testFormatValidationLocationCglib() throws Exception {
        // 创建模拟的CGLIB代理类名
        Class<?> cglibClass = new TestObject("test") {
            // 匿名类模拟CGLIB
        }.getClass();

        String location = (String) invokePrivateMethod(validationAspect,"formatValidationLocation", cglibClass, "name");
        assertThat(location).contains("name");
    }

    @Test
    @DisplayName("formatValidationLocation - null fieldOrPath")
    void testFormatValidationLocationNullPath() throws Exception {
        String location = (String) invokePrivateMethod(validationAspect,"formatValidationLocation", TestObject.class, (String) null);
        assertThat(location).isEmpty();
    }

    @Test
    @DisplayName("formatValidationLocation - null clazz")
    void testFormatValidationLocationNullClass() throws Exception {
        String location = (String) invokePrivateMethod(validationAspect,"formatValidationLocation", (Class<?>) null, "name");
        assertThat(location).isEqualTo("name");
    }

    // ==================== toSceneName 测试 ====================

    @Test
    @DisplayName("toSceneName - empty scenes")
    void testToSceneNameEmpty() throws Exception {
        String sceneName = (String) invokePrivateMethod(validationAspect,"toSceneName", (Object) new Scenario[]{});
        assertThat(sceneName).isEqualTo(FailureConst.DEFAULT_SCENE);
    }

    @Test
    @DisplayName("toSceneName - 只有DEFAULT")
    void testToSceneNameOnlyDefault() throws Exception {
        String sceneName = (String) invokePrivateMethod(validationAspect,"toSceneName", (Object) new Scenario[]{Scenario.DEFAULT});
        assertThat(sceneName).isEqualTo(FailureConst.DEFAULT_SCENE);
    }

    @Test
    @DisplayName("toSceneName - 单个非DEFAULT场景")
    void testToSceneNameSingleNonDefault() throws Exception {
        String sceneName = (String) invokePrivateMethod(validationAspect,"toSceneName", (Object) new Scenario[]{Scenario.CREATE});
        assertThat(sceneName).isEqualTo("CREATE");
    }

    @Test
    @DisplayName("toSceneName - 多个场景")
    void testToSceneNameMultiple() throws Exception {
        String sceneName = (String) invokePrivateMethod(validationAspect,"toSceneName", (Object) new Scenario[]{Scenario.CREATE, Scenario.UPDATE});
        assertThat(sceneName).isEqualTo("CREATE,UPDATE");
    }

    @Test
    @DisplayName("toSceneName - 混合场景（包含DEFAULT）")
    void testToSceneNameMixed() throws Exception {
        String sceneName = (String) invokePrivateMethod(validationAspect,"toSceneName", (Object) new Scenario[]{Scenario.DEFAULT, Scenario.CREATE, Scenario.UPDATE});
        assertThat(sceneName).isEqualTo("CREATE,UPDATE");
    }

    // ==================== 测试辅助类 ====================

    public void testMethod() {
    }

    public void testMethodWithArgs(String arg1, String arg2) {
    }

    public void testMethodWithArgs(String arg1, InputStream arg2) {
    }

    public void testMethodWithArgs(ServletRequest arg1, String arg2) {
    }

    public void testMethodWithArgs(ServletResponse arg1, String arg2) {
    }

    public void testMethodWithArgs(HttpSession arg1, String arg2) {
    }

    public void testMethodWithArgs(MultipartFile arg1, String arg2) {
    }

    public void testMethodWithArgs(OutputStream arg1, String arg2) {
    }

    public void testMethodWithArgs(Reader arg1, String arg2) {
    }

    public void testMethodWithArgs(Writer arg1, String arg2) {
    }

    public void testMethodWithSkipAnnotation(String arg1, @SkipValidation String arg2) {
    }

    // 测试分组
    public interface TestGroup {
    }

    // 测试验证器
    public static class TestFastValidator implements FastValidator<Object> {
        @Override
        public void validate(Object target, ValidationContext context) {
            // 通过验证
        }
    }

    public static class FailingFastValidator implements FastValidator<Object> {
        @Override
        public void validate(Object target, ValidationContext context) {
            // 【调试】打印日志，确认该方法是否被执行
            System.out.println(">>> [DEBUG] FailingFastValidator.validate() called!");
            System.out.println(">>> [DEBUG] Target: " + target);
            System.out.println(">>> [DEBUG] Context: " + context);

            if (context != null) {
                // 报告错误
                context.reportError(ResponseCode.VALIDATION_ERROR_400, "Validation failed intentionally");
                System.out.println(">>> [DEBUG] Error reported. Current error count: " + context.errorSize());
            } else {
                System.err.println(">>> [ERROR] Context is NULL!");
            }
        }
        @Override
        public Class<?> getSupportedType() {
            return Object.class;
        }
    }

    public static class AnotherFailingFastValidator implements FastValidator<Object> {
        @Override
        public void validate(Object target, ValidationContext context) {
            context.reportError(ResponseCode.VALIDATION_ERROR_400, "Another validation failed");
        }
    }

    public static class MultipleErrorsFastValidator implements FastValidator<Object> {
        @Override
        public void validate(Object target, ValidationContext context) {
            context.reportError(ResponseCode.VALIDATION_ERROR_400, "Error 1");
            context.reportError(ResponseCode.VALIDATION_ERROR_400, "Error 2");
        }
    }

    public abstract static class AbstractFastValidator implements FastValidator<Object> {
        // 抽象类，无法实例化
    }

    public static class TestTypedValidator extends TypedValidator {
        @Override
        protected void registerValidators() {
            register(String.class, (s, ctx) -> {
                // 通过验证
            });
        }
    }

    public static class TestTypedValidatorFailing extends TypedValidator {
        @Override
        protected void registerValidators() {
            register(String.class, (s, ctx) -> {
                ctx.reportError(ResponseCode.VALIDATION_ERROR_400, "Typed validation failed");
            });
        }
    }

    public static class TestPlainValidator implements FastValidator<String> {
        @Override
        public void validate(String target, ValidationContext context) {
            // 通过验证
        }

        @Override
        public Class<String> getSupportedType() {
            return String.class;
        }
    }

    public static class TestPlainValidatorFailing implements FastValidator<String> {
        @Override
        public void validate(String target, ValidationContext context) {
            context.reportError(ResponseCode.VALIDATION_ERROR_400, "Plain validation failed");
        }

        @Override
        public Class<String> getSupportedType() {
            return String.class;
        }
    }

    public static class TestPlainValidatorObjectType implements FastValidator<Object> {
        @Override
        public void validate(Object target, ValidationContext context) {
            // 通过验证
        }

        @Override
        public Class<Object> getSupportedType() {
            return Object.class;
        }
    }

    // 测试对象
    @Setter
    @Getter
    public static class TestObject {
        @Scene(Scenario.DEFAULT)
        private String name;

        public TestObject() {
        }

        public TestObject(String name) {
            this.name = name;
        }

    }

    @Setter
    @Getter
    public static class TestChildObject extends TestObject {
        private String extra;

    }

    @Setter
    @Getter
    public static class TestObjectNoScene {
        private String value;
    }
}
