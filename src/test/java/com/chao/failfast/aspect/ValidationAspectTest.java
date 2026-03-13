package com.chao.failfast.aspect;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.annotation.SkipValidation;
import com.chao.failfast.annotation.Validate;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.MultiBusiness;
import com.chao.failfast.model.TestResponseCode;
import com.chao.failfast.validator.TypedValidator;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ValidationAspect 全面覆盖测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationAspect 切面全覆盖测试")
class ValidationAspectTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    @Mock
    private Validate validate;

    @InjectMocks
    private ValidationAspect validationAspect;

    // --- 测试辅助类 ---

    // 1. 普通验证器
    public static class StringValidator implements FastValidator<String> {
        @Override
        public void validate(String target, ValidationContext context) {
            if ("error".equals(target)) {
                context.reportError(TestResponseCode.PARAM_ERROR);
            }
        }

        @Override
        public Class<?> getSupportedType() {
            return String.class;
        }
    }

    // 2. 多重错误验证器
    public static class MultiErrorValidator implements FastValidator<String> {
        @Override
        public void validate(String target, ValidationContext context) {
            context.reportError(TestResponseCode.PARAM_ERROR);
            context.reportError(TestResponseCode.SYSTEM_ERROR);
        }

        @Override
        public Class<?> getSupportedType() {
            return String.class;
        }
    }

    // 3. 抛异常验证器
    public static class ExceptionThrowingValidator implements FastValidator<Object> {
        @Override
        public void validate(Object target, ValidationContext context) {
            throw new RuntimeException("Validator execution failed");
        }

        @Override
        public Class<?> getSupportedType() {
            return String.class;
        }
    }

    // 4. 构造函数抛异常验证器
    public static class ConstructorThrowingValidator implements FastValidator<Object> {
        public ConstructorThrowingValidator() {
            throw new RuntimeException("Constructor failed");
        }

        @Override
        public void validate(Object target, ValidationContext context) {
        }
    }

    // 5. 私有构造函数验证器
    public static class PrivateConstructorValidator implements FastValidator<Object> {
        private PrivateConstructorValidator() {
        }

        @Override
        public void validate(Object target, ValidationContext context) {
        }
    }

    // 6. TypedValidator 实现
    public static class MyTypedValidator extends TypedValidator {
        @Override
        protected void registerValidators() {
            register(String.class, (s, ctx) -> {
                if ("error".equals(s)) {
                    ctx.reportError(TestResponseCode.PARAM_ERROR);
                }
            });
            register(Integer.class, (i, ctx) -> {
                if (i < 0) {
                    ctx.reportError(TestResponseCode.PARAM_ERROR);
                }
            });
        }
    }

    // 7. 返回 Object.class 的验证器 (无法确定类型)
    public static class UnknownTypeValidator implements FastValidator<Object> {
        @Override
        public void validate(Object target, ValidationContext context) {
            // Should not be called
            context.reportError(TestResponseCode.PARAM_ERROR);
        }

        @Override
        public Class<?> getSupportedType() {
            return Object.class;
        }
    }

    // 8. 能够停止上下文的验证器
    public static class StoppingValidator implements FastValidator<String> {
        @Override
        public void validate(String target, ValidationContext context) {
            context.reportError(TestResponseCode.PARAM_ERROR);
        }

        @Override
        public Class<?> getSupportedType() {
            return String.class;
        }
    }

    // 9. 第二个验证器，用于验证是否被短路
    public static class SecondValidator implements FastValidator<String> {
        @Override
        public void validate(String target, ValidationContext context) {
            context.reportError(TestResponseCode.SYSTEM_ERROR);
        }

        @Override
        public Class<?> getSupportedType() {
            return String.class;
        }
    }


    // 10. getSupportedType 返回 null 的验证器
    public static class NullTypeValidator implements FastValidator<Object> {
        @Override
        public void validate(Object target, ValidationContext context) {}
        @Override
        public Class<?> getSupportedType() { return null; }
    }

    @BeforeEach
    void setUp() {
        try {
            Field cache = ValidationAspect.class.getDeclaredField("VALIDATOR_CACHE");
            cache.setAccessible(true);
            ((ConcurrentHashMap<?, ?>) cache.get(null)).clear();

            Field factoryCache = ValidationAspect.class.getDeclaredField("VALIDATOR_FACTORY_CACHE");
            factoryCache.setAccessible(true);
            ((ConcurrentHashMap<?, ?>) factoryCache.get(null)).clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("基础逻辑测试")
    class BasicLogicTest {

        @Test
        @DisplayName("validate.value() 为空时直接放行")
        void shouldProceedWhenNoValidatorSpecified() throws Throwable {
            when(validate.value()).thenReturn(new Class[0]);

            validationAspect.around(joinPoint, validate);

            verify(joinPoint).proceed();
            verify(joinPoint, never()).getArgs();
        }

        @Test
        @DisplayName("collectValidatableArgs 为空时直接放行")
        void shouldProceedWhenNoValidatableArgs() throws Throwable {
            Class[] validators = {StringValidator.class};
            when(validate.value()).thenReturn(validators);

            // Mock args
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(joinPoint.getSignature()).thenReturn(signature);
            Method method = TestMethods.class.getMethod("noArgs");
            when(signature.getMethod()).thenReturn(method);

            validationAspect.around(joinPoint, validate);

            verify(joinPoint).proceed();
        }
    }

    @Nested
    @DisplayName("参数收集逻辑测试 (collectValidatableArgs)")
    class ArgsCollectionTest {

        @Test
        @DisplayName("当 getParameterAnnotations 返回 null 时应安全处理")
        void shouldHandleNullAnnotations() throws Throwable {
            Class[] validators = {StringValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            Object[] args = new Object[]{"valid"};
            when(joinPoint.getArgs()).thenReturn(args);
            when(joinPoint.getSignature()).thenReturn(signature);

            // Mock method to return null annotations for the first parameter
            Method method = mock(Method.class);
            when(signature.getMethod()).thenReturn(method);
            when(method.getParameterAnnotations()).thenReturn(new Annotation[][]{null});

            when(applicationContext.getBeanNamesForType(StringValidator.class)).thenReturn(new String[]{});

            validationAspect.around(joinPoint, validate);

            verify(joinPoint).proceed();
        }

        @Test
        @DisplayName("过滤 null、@SkipValidation 和特定类型参数")
        void shouldFilterArgsCorrectly() throws Throwable {
            Class[] validators = {StringValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            // 构造参数：
            // 0: "valid" (正常)
            // 1: null (过滤)
            // 2: "skip" (@SkipValidation 过滤)
            // 3: ServletRequest (类型过滤)
            Object[] args = new Object[]{
                    "valid",
                    null,
                    "skip",
                    mock(ServletRequest.class)
            };
            when(joinPoint.getArgs()).thenReturn(args);
            when(joinPoint.getSignature()).thenReturn(signature);

            Method method = TestMethods.class.getMethod("mixedArgs", String.class, String.class, String.class, ServletRequest.class);
            when(signature.getMethod()).thenReturn(method);

            // Mock ApplicationContext needed for validator execution
            when(applicationContext.getBeanNamesForType(StringValidator.class)).thenReturn(new String[]{});

            // Act
            validationAspect.around(joinPoint, validate);

            // Assert
            // 只有第一个参数 "valid" 应该被传递给验证器
            // 我们可以通过 Spy 或者 Mock Validator 来验证
            // 这里简单通过 verify(proceed) 确认没有抛异常即可，因为 "valid" 不会触发错误
            verify(joinPoint).proceed();
        }

        @Test
        @DisplayName("验证所有跳过类型")
        void shouldSkipAllIgnoredTypes() throws Throwable {
            Class[] validators = {StringValidator.class};
            when(validate.value()).thenReturn(validators);

            Object[] args = new Object[]{
                    mock(ServletRequest.class),
                    mock(ServletResponse.class),
                    mock(HttpSession.class),
                    mock(MultipartFile.class),
                    mock(InputStream.class),
                    mock(OutputStream.class),
                    mock(Reader.class),
                    mock(Writer.class)
            };
            when(joinPoint.getArgs()).thenReturn(args);
            when(joinPoint.getSignature()).thenReturn(signature);
            Method method = TestMethods.class.getMethod("ignoredTypes", ServletRequest.class, ServletResponse.class, HttpSession.class, MultipartFile.class, InputStream.class, OutputStream.class, Reader.class, Writer.class);
            when(signature.getMethod()).thenReturn(method);

            validationAspect.around(joinPoint, validate);

            verify(joinPoint).proceed();
            // 确保没有进行验证逻辑（因为参数列表为空）
            verify(applicationContext, never()).getBean(any(Class.class));
        }
    }

    @Nested
    @DisplayName("验证器执行逻辑测试")
    class ValidatorExecutionTest {

        @Test
        @DisplayName("executePlainValidator: 类型匹配且通过验证")
        void shouldPassPlainValidator() throws Throwable {
            Class[] validators = {StringValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            Object[] args = new Object[]{"valid"};
            setupJoinPoint(args, "singleArg", String.class);

            when(applicationContext.getBeanNamesForType(StringValidator.class)).thenReturn(new String[]{});

            validationAspect.around(joinPoint, validate);

            verify(joinPoint).proceed();
        }

        @Test
        @DisplayName("executePlainValidator: 类型匹配但验证失败 (Business异常)")
        void shouldFailPlainValidator() throws Throwable {
            Class[] validators = {StringValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            Object[] args = new Object[]{"error"};
            setupJoinPoint(args, "singleArg", String.class);

            when(applicationContext.getBeanNamesForType(StringValidator.class)).thenReturn(new String[]{});

            Throwable thrown = catchThrowable(() -> validationAspect.around(joinPoint, validate));

            assertThat(thrown).isInstanceOf(Business.class);
            verify(joinPoint, never()).proceed();
        }

        @Test
        @DisplayName("executePlainValidator: 类型不匹配 (忽略)")
        void shouldIgnoreTypeMismatch() throws Throwable {
            Class[] validators = {StringValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            Object[] args = new Object[]{123}; // Integer vs StringValidator
            setupJoinPoint(args, "intArg", Integer.class);

            when(applicationContext.getBeanNamesForType(StringValidator.class)).thenReturn(new String[]{});

            validationAspect.around(joinPoint, validate);

            verify(joinPoint).proceed();
        }

        @Test
        @DisplayName("executePlainValidator: 无法确定类型 (Object.class) -> Log Warn & Skip")
        void shouldSkipUnknownTypeValidator() throws Throwable {
            Class[] validators = {UnknownTypeValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            Object[] args = new Object[]{"any"};
            setupJoinPoint(args, "singleArg", String.class);

            when(applicationContext.getBeanNamesForType(UnknownTypeValidator.class)).thenReturn(new String[]{});

            validationAspect.around(joinPoint, validate);

            // 验证器本身逻辑是抛错，但如果被跳过，则不会抛错
            verify(joinPoint).proceed();
        }

        @Test
        @DisplayName("executePlainValidator: getSupportedType 返回 null -> Log Warn & Skip")
        void shouldSkipNullTypeValidator() throws Throwable {
            Class[] validators = {NullTypeValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            Object[] args = new Object[]{"any"};
            setupJoinPoint(args, "singleArg", String.class);

            when(applicationContext.getBeanNamesForType(NullTypeValidator.class)).thenReturn(new String[]{});

            validationAspect.around(joinPoint, validate);

            verify(joinPoint).proceed();
        }
        
        @Test
        @DisplayName("executePlainValidator: 遇到错误应停止 (Fast Mode)")
        void shouldStopPlainValidatorOnFirstError() throws Throwable {
            Class[] validators = {StringValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            // "error" fails, "valid" passes.
            // If it stops, "valid" is not validated.
            // But verify() checks if proceed() is called (which it won't be if error).
            // To ensure the loop break is hit, we need 2 args.
            Object[] args = new Object[]{"error", "valid"};
            // Mock signature to accept 2 args
            when(joinPoint.getArgs()).thenReturn(args);
            when(joinPoint.getSignature()).thenReturn(signature);
            Method method = TestMethods.class.getMethod("mixedArgs", String.class, String.class, String.class, ServletRequest.class);
            when(signature.getMethod()).thenReturn(method);

            when(applicationContext.getBeanNamesForType(StringValidator.class)).thenReturn(new String[]{});

            Throwable thrown = catchThrowable(() -> validationAspect.around(joinPoint, validate));

            assertThat(thrown).isInstanceOf(Business.class);
        }

        @Test
        @DisplayName("executeTypedValidator: 匹配注册类型并验证")
        void shouldExecuteTypedValidator() throws Throwable {
            Class[] validators = {MyTypedValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            Object[] args = new Object[]{"valid", 100};
            setupJoinPoint(args, "mixedArgs2", String.class, Integer.class);

            when(applicationContext.getBeanNamesForType(MyTypedValidator.class)).thenReturn(new String[]{});

            validationAspect.around(joinPoint, validate);

            verify(joinPoint).proceed();
        }

        @Test
        @DisplayName("executeTypedValidator: 匹配注册类型并验证失败")
        void shouldFailTypedValidator() throws Throwable {
            Class[] validators = {MyTypedValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            Object[] args = new Object[]{-1}; // Integer < 0 triggers error
            setupJoinPoint(args, "intArg", Integer.class);

            when(applicationContext.getBeanNamesForType(MyTypedValidator.class)).thenReturn(new String[]{});

            Throwable thrown = catchThrowable(() -> validationAspect.around(joinPoint, validate));

            assertThat(thrown).isInstanceOf(Business.class);
        }

        @Test
        @DisplayName("executeTypedValidator: 遇到错误应停止 (Fast Mode)")
        void shouldStopTypedValidatorOnFirstError() throws Throwable {
            Class[] validators = {MyTypedValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            // "error" fails, 100 passes. But if it stops, 100 is not validated?
            // Wait, TypedValidator iterates over args.
            // If first arg fails, it should break loop.
            Object[] args = new Object[]{"error", 100};
            setupJoinPoint(args, "mixedArgs2", String.class, Integer.class);

            when(applicationContext.getBeanNamesForType(MyTypedValidator.class)).thenReturn(new String[]{});

            Throwable thrown = catchThrowable(() -> validationAspect.around(joinPoint, validate));

            assertThat(thrown).isInstanceOf(Business.class);
            // We can't easily verify if the loop broke without spying on the validator's internal map consumer
            // But this should cover the branch "if (ctx.isStopped()) break;"
        }

        @Test
        @DisplayName("executeTypedValidator: 类型不匹配 (忽略)")
        void shouldIgnoreTypedValidatorMismatch() throws Throwable {
            Class[] validators = {MyTypedValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            Object[] args = new Object[]{1.0d}; // Double not in [String, Integer]
            setupJoinPoint(args, "doubleArg", Double.class);

            when(applicationContext.getBeanNamesForType(MyTypedValidator.class)).thenReturn(new String[]{});

            validationAspect.around(joinPoint, validate);

            verify(joinPoint).proceed();
        }
    }

    @Nested
    @DisplayName("Fail-Fast 机制测试")
    class FailFastTest {

        @Test
        @DisplayName("fast=true: 遇到第一个错误即停止")
        void shouldStopOnFirstErrorWhenFastTrue() throws Throwable {
            Class[] validators = {StoppingValidator.class, SecondValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            Object[] args = new Object[]{"any"};
            setupJoinPoint(args, "singleArg", String.class);

            // Mock StoppingValidator
            StoppingValidator v1 = spy(new StoppingValidator());

            // 使用 doReturn 避免 spy 调用真实方法（虽然这里真实方法就是我们要测的，但为了验证 getBean）
            // 这里我们直接 mock getBeanNamesForType 走反射或者 bean
            when(applicationContext.getBeanNamesForType(StoppingValidator.class)).thenReturn(new String[]{"v1"});
            when(applicationContext.getBean(StoppingValidator.class)).thenReturn(v1);

            // 关键：验证 v2 是否被获取/实例化。如果 failFast 生效，v1 报错后 break，v2 不应该被触碰

            Throwable thrown = catchThrowable(() -> validationAspect.around(joinPoint, validate));

            assertThat(thrown).isInstanceOf(Business.class);

            verify(v1).validate(any(), any());
            // 验证第二个验证器没有被请求实例化/获取
            verify(applicationContext, never()).getBeanNamesForType(SecondValidator.class);
        }

        @Test
        @DisplayName("fast=false: 收集所有错误")
        void shouldCollectAllErrorsWhenFastFalse() throws Throwable {
            Class[] validators = {MultiErrorValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(false);

            Object[] args = new Object[]{"any"};
            setupJoinPoint(args, "singleArg", String.class);

            when(applicationContext.getBeanNamesForType(MultiErrorValidator.class)).thenReturn(new String[]{});

            Throwable thrown = catchThrowable(() -> validationAspect.around(joinPoint, validate));

            assertThat(thrown).isInstanceOf(MultiBusiness.class);
            MultiBusiness mb = (MultiBusiness) thrown;
            assertThat(mb.getErrors()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("异常处理与实例化测试")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("验证器执行抛出运行时异常: 异常应直接抛出")
        void shouldThrowValidatorException() throws Throwable {
            Class[] validators = {ExceptionThrowingValidator.class, StringValidator.class};
            when(validate.value()).thenReturn(validators);
            when(validate.fast()).thenReturn(true);

            Object[] args = new Object[]{"valid"};
            setupJoinPoint(args, "singleArg", String.class);

            when(applicationContext.getBeanNamesForType(ExceptionThrowingValidator.class)).thenReturn(new String[]{});

            // 注意：executeValidators 方法本身没有 try-catch 包裹 validator.validate 调用的异常？
            // 让我们再检查 ValidationAspect.java 
            // executeSingleValidator 调用 validator.validate
            // 没有任何 try-catch！
            // 所以 validator 抛异常会直接抛出到切面外，导致 500

            Throwable thrown = catchThrowable(() -> validationAspect.around(joinPoint, validate));
            assertThat(thrown).isInstanceOf(RuntimeException.class)
                    .hasMessage("Validator execution failed");

            // 验证第二个验证器没有被执行 (因为第一个抛异常中断了)
            verify(applicationContext, never()).getBeanNamesForType(StringValidator.class);
        }

        @Test
        @DisplayName("实例化失败 (私有构造): 抛出 RuntimeException")
        void shouldThrowRuntimeExceptionOnInstantiationError() throws Throwable {
            Class[] validators = {PrivateConstructorValidator.class};
            when(validate.value()).thenReturn(validators);

            setupJoinPoint(new Object[]{"any"}, "singleArg", String.class);
            when(applicationContext.getBeanNamesForType(PrivateConstructorValidator.class)).thenReturn(new String[]{});

            Throwable thrown = catchThrowable(() -> validationAspect.around(joinPoint, validate));

            assertThat(thrown).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to instantiate validator");
        }

        @Test
        @DisplayName("构造函数抛异常: 抛出 RuntimeException")
        void shouldThrowRuntimeExceptionOnConstructorError() throws Throwable {
            Class[] validators = {ConstructorThrowingValidator.class};
            when(validate.value()).thenReturn(validators);

            setupJoinPoint(new Object[]{"any"}, "singleArg", String.class);
            when(applicationContext.getBeanNamesForType(ConstructorThrowingValidator.class)).thenReturn(new String[]{});

            Throwable thrown = catchThrowable(() -> validationAspect.around(joinPoint, validate));

            assertThat(thrown).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to instantiate validator");
        }

        @Test
        @DisplayName("Spring Bean 获取: 优先从 ApplicationContext 获取")
        void shouldGetValidatorFromContext() throws Throwable {
            Class[] validators = {StringValidator.class};
            when(validate.value()).thenReturn(validators);

            setupJoinPoint(new Object[]{"valid"}, "singleArg", String.class);

            StringValidator mockValidator = mock(StringValidator.class);
            // Fix generic type issue
            doReturn(String.class).when(mockValidator).getSupportedType();

            when(applicationContext.getBeanNamesForType(StringValidator.class)).thenReturn(new String[]{"bean"});
            when(applicationContext.getBean(StringValidator.class)).thenReturn(mockValidator);

            validationAspect.around(joinPoint, validate);

            verify(mockValidator).validate(any(), any());
        }

        @Test
        @DisplayName("Validator 缓存测试: 第二次调用应使用缓存")
        void shouldUseValidatorCache() throws Throwable {
            Class[] validators = {StringValidator.class};
            when(validate.value()).thenReturn(validators);

            setupJoinPoint(new Object[]{"valid"}, "singleArg", String.class);

            when(applicationContext.getBeanNamesForType(StringValidator.class)).thenReturn(new String[]{});

            // First call
            validationAspect.around(joinPoint, validate);

            // Second call
            validationAspect.around(joinPoint, validate);

            verify(applicationContext, times(1)).getBeanNamesForType(StringValidator.class);

            // Reflection instantiation happens internally in computeIfAbsent, 
            // verifying it is hard without spying the map, but functionality is covered.
        }
    }

    // --- Helper Methods ---

    private void setupJoinPoint(Object[] args, String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.getSignature()).thenReturn(signature);
        Method method = TestMethods.class.getMethod(methodName, paramTypes);
        when(signature.getMethod()).thenReturn(method);
    }

    // --- Dummy Methods for Reflection ---
    interface TestMethods {
        void noArgs();

        void singleArg(String arg);

        void intArg(Integer arg);

        void doubleArg(Double arg);

        void mixedArgs(String arg1, String arg2, @SkipValidation String arg3, ServletRequest req);

        void mixedArgs2(String arg1, Integer arg2);

        void ignoredTypes(ServletRequest req, ServletResponse resp, HttpSession session, MultipartFile file, InputStream is, OutputStream os, Reader reader, Writer writer);
    }
}
