package com.chao.failfast.advice;

import com.chao.failfast.annotation.Validate;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.Ex;
import com.chao.failfast.internal.MultiBusiness;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.FailureProperties;
import com.chao.failfast.internal.core.ResponseCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FailFastExceptionHandlerTest {

    private TestFailFastExceptionHandler handler;
    private FailureProperties properties;
    private FailureContext context;

    @BeforeEach
    void setUp() {
        handler = new TestFailFastExceptionHandler();
        properties = Mockito.mock(FailureProperties.class);
        context = Mockito.mock(FailureContext.class);
        handler.setFailFastProperties(properties);
        Ex.setContext(context);
    }

    @Test
    void testHandleBusinessException() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");
        var response = handler.handleBusinessException(business);
        assertNotNull(response);
    }

    @Test
    void testHandleMultiBusinessException() {
        List<Business> errors = List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"),
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 2")
        );
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        var response = handler.handleMultiBusinessException(multiBusiness);
        assertNotNull(response);
    }

    @Test
    void testHandleMethodArgumentNotValidException() throws Exception {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(parameter.getMethod()).thenReturn(method);

        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError = Mockito.mock(FieldError.class);
        Mockito.when(fieldError.getField()).thenReturn("field");
        Mockito.when(fieldError.getDefaultMessage()).thenReturn("Invalid field");
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        Mockito.when(bindingResult.getTarget()).thenReturn(new Object());

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);
        var response = handler.handleMethodArgumentNotValidException(exception);
        assertNotNull(response);
    }

    @Test
    void testHandleMethodArgumentNotValidExceptionWithValidateAnnotation() throws Exception {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        Method method = getClass().getMethod("testMethodWithValidate");
        Mockito.when(parameter.getMethod()).thenReturn(method);

        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError1 = Mockito.mock(FieldError.class);
        FieldError fieldError2 = Mockito.mock(FieldError.class);
        Mockito.when(fieldError1.getField()).thenReturn("field1");
        Mockito.when(fieldError1.getDefaultMessage()).thenReturn("Invalid field1");
        Mockito.when(fieldError2.getField()).thenReturn("field2");
        Mockito.when(fieldError2.getDefaultMessage()).thenReturn("Invalid field2");
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);
        var response = handler.handleMethodArgumentNotValidException(exception);
        assertNotNull(response);
    }

    @Test
    void testHandleConstraintViolationException() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
        jakarta.validation.Path path = Mockito.mock(jakarta.validation.Path.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(path.toString()).thenReturn("method.field");
        Mockito.when(violation.getMessage()).thenReturn("Invalid field");
        Mockito.when(violation.getRootBeanClass()).thenReturn(Object.class);

        @SuppressWarnings("unchecked")
        ConstraintViolationException exception = new ConstraintViolationException((Set) Set.of(violation));
        var response = handler.handleConstraintViolationException(exception);
        assertNotNull(response);
    }

    @Test
    void testBuildResponse() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");
        var response = handler.buildResponse(business);
        assertNotNull(response);
    }

    @Test
    void testBuildResponseWithScene() {
        Mockito.when(context.getScene()).thenReturn("custom-scene");
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");
        var response = handler.buildResponse(business);
        assertNotNull(response);
        assertTrue(((Map<?, ?>) response.getBody()).containsKey(FailureConst.FIELD_SCENE));
    }

    @Test
    void testBuildResponseWithoutSceneWhenBlankOrDefault() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");

        Mockito.when(context.getScene()).thenReturn("   ");
        var blankScene = handler.buildResponse(business);
        assertNotNull(blankScene);
        assertFalse(((Map<?, ?>) blankScene.getBody()).containsKey(FailureConst.FIELD_SCENE));

        Mockito.when(context.getScene()).thenReturn(FailureConst.DEFAULT_SCENE);
        var defaultScene = handler.buildResponse(business);
        assertNotNull(defaultScene);
        assertFalse(((Map<?, ?>) defaultScene.getBody()).containsKey(FailureConst.FIELD_SCENE));
    }

    @Test
    void testBuildMultiErrorResponseWithVerbose() {
        List<Business> errors = List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"),
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 2")
        );
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        Mockito.when(properties.isVerbose()).thenReturn(true);
        var response = handler.buildMultiErrorResponse(multiBusiness);
        assertNotNull(response);
    }

    @Test
    void testBuildMultiErrorResponseWithoutVerbose() {
        List<Business> errors = List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1")
        );
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        Mockito.when(properties.isVerbose()).thenReturn(false);
        var response = handler.buildMultiErrorResponse(multiBusiness);
        assertNotNull(response);
    }

    @Test
    void testBuildMultiErrorResponseWithScene() {
        List<Business> errors = List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"),
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 2")
        );
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        Mockito.when(properties.isVerbose()).thenReturn(false);
        Mockito.when(context.getScene()).thenReturn("custom-scene");

        var response = handler.buildMultiErrorResponse(multiBusiness);
        assertNotNull(response);
        assertTrue(((Map<?, ?>) response.getBody()).containsKey(FailureConst.FIELD_SCENE));
    }

    @Test
    void testBuildMultiErrorResponseWithoutSceneWhenBlankOrDefault() {
        List<Business> errors = List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"),
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 2")
        );
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        Mockito.when(properties.isVerbose()).thenReturn(false);

        Mockito.when(context.getScene()).thenReturn("   ");
        var blankScene = handler.buildMultiErrorResponse(multiBusiness);
        assertNotNull(blankScene);
        assertFalse(((Map<?, ?>) blankScene.getBody()).containsKey(FailureConst.FIELD_SCENE));

        Mockito.when(context.getScene()).thenReturn(FailureConst.DEFAULT_SCENE);
        var defaultScene = handler.buildMultiErrorResponse(multiBusiness);
        assertNotNull(defaultScene);
        assertFalse(((Map<?, ?>) defaultScene.getBody()).containsKey(FailureConst.FIELD_SCENE));
    }

    @Test
    void testIsVerboseWithProperties() {
        Mockito.when(properties.isVerbose()).thenReturn(true);
        assertTrue(handler.isVerbose());
    }

    @Test
    void testIsVerboseWithoutProperties() {
        handler.setFailFastProperties(null);
        assertFalse(handler.isVerbose());
    }

    @Test
    void testHandleMultiErrorsWithEmptyList() {
        var response = handler.handleMultiErrors(new ArrayList<>());
        assertNotNull(response);
    }

    @Test
    void testHandleMultiErrorsWithSingleError() {
        List<Business> errors = List.of(Business.of(ResponseCode.VALIDATION_ERROR_400, "Error"));
        var response = handler.handleMultiErrors(errors);
        assertNotNull(response);
    }

    @Test
    void testHandleMultiErrorsWithMultipleErrors() {
        List<Business> errors = List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"),
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 2")
        );
        var response = handler.handleMultiErrors(errors);
        assertNotNull(response);
    }

    @Test
    void testLogExceptionWithSingleBusiness() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");
        handler.logException(business);
    }

    @Test
    void testLogExceptionWithMultiBusiness() {
        List<Business> errors = List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"),
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 2")
        );
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        handler.logException(multiBusiness);
    }

    @Test
    void testFormatValidationLocationWithNullField() {
        String location = handler.formatValidationLocation(getClass(), null);
        assertNotNull(location);
    }

    @Test
    void testFormatValidationLocationWithSimpleField() {
        String location = handler.formatValidationLocation(getClass(), "field");
        assertNotNull(location);
    }

    @Test
    void testFormatValidationLocationWithNestedField() {
        String location = handler.formatValidationLocation(getClass(), "method.field");
        assertNotNull(location);
    }

    @Test
    void testFormatValidationLocationWithNestedFieldWithoutClass() {
        String location = handler.formatValidationLocation(null, "method.field");
        assertNotNull(location);
        assertTrue(location.contains("method"));
    }

    @Test
    void testFormatValidationLocationWithProxyClass() {
        class ProxyClass {}
        String location = handler.formatValidationLocation(ProxyClass.class, "field");
        assertNotNull(location);
    }

    @Test
    void testFormatValidationLocationWithDollarDollarClassName() {
        class $$ProxyClass extends Object {}
        String location = handler.formatValidationLocation($$ProxyClass.class, "field");
        assertNotNull(location);
    }

    @Test
    void testParseErrorWithNullMessage() {
        Business business = handler.parseError(null, "location", "method");
        assertNotNull(business);
    }

    @Test
    void testParseErrorWithBlankMessage() {
        Business business = handler.parseError("", "location", "method");
        assertNotNull(business);
    }

    @Test
    void testParseErrorWithCodeAndMessage() {
        Business business = handler.parseError("400: Invalid field", "location", "method");
        assertNotNull(business);
    }

    @Test
    void testParseErrorWithCodeAndBlankMessage() {
        Business business = handler.parseError("400:   ", "location", "method");
        assertNotNull(business);
    }

    @Test
    void testParseErrorWithCodeOnly() {
        Business business = handler.parseError("400", "location", "method");
        assertNotNull(business);
    }

    @Test
    void testParseErrorWithMessageOnly() {
        Business business = handler.parseError("Invalid field", "location", "method");
        assertNotNull(business);
    }

    @Test
    void testParseErrorWithoutLocation() {
        Business business = handler.parseError("Invalid field", null, "method");
        assertNotNull(business);
    }

    @Test
    void testParseValidationMessageWithNull() {
        Object result = handler.parseValidationMessage(null);
        // 通过反射获取code和text属性
        try {
            var codeMethod = result.getClass().getMethod("code");
            var textMethod = result.getClass().getMethod("text");
            assertNull(codeMethod.invoke(result));
            assertNull(textMethod.invoke(result));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testParseValidationMessageWithEmptyString() {
        Object result = handler.parseValidationMessage("   ");
        // 通过反射获取code和text属性
        try {
            var codeMethod = result.getClass().getMethod("code");
            var textMethod = result.getClass().getMethod("text");
            assertNull(codeMethod.invoke(result));
            assertNull(textMethod.invoke(result));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testParseValidationMessageWithCodeAndText() {
        Object result = handler.parseValidationMessage("400: Invalid field");
        // 通过反射获取code和text属性
        try {
            var codeMethod = result.getClass().getMethod("code");
            var textMethod = result.getClass().getMethod("text");
            assertEquals(400, codeMethod.invoke(result));
            assertEquals("Invalid field", textMethod.invoke(result));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testParseValidationMessageWithCodeAndEmptyText() {
        Object result = handler.parseValidationMessage("400:");
        try {
            var codeMethod = result.getClass().getMethod("code");
            var textMethod = result.getClass().getMethod("text");
            assertEquals(400, codeMethod.invoke(result));
            assertNull(textMethod.invoke(result));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testParseValidationMessageWithCodeOnly() {
        Object result = handler.parseValidationMessage("400");
        // 通过反射获取code和text属性
        try {
            var codeMethod = result.getClass().getMethod("code");
            var textMethod = result.getClass().getMethod("text");
            assertEquals(400, codeMethod.invoke(result));
            assertNull(textMethod.invoke(result));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testParseValidationMessageWithTextOnly() {
        Object result = handler.parseValidationMessage("Invalid field");
        // 通过反射获取code和text属性
        try {
            var codeMethod = result.getClass().getMethod("code");
            var textMethod = result.getClass().getMethod("text");
            assertNull(codeMethod.invoke(result));
            assertEquals("Invalid field", textMethod.invoke(result));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testIsNumericWithNull() {
        assertFalse(handler.isNumeric(null));
    }

    @Test
    void testIsNumericWithEmptyString() {
        assertFalse(handler.isNumeric(""));
    }

    @Test
    void testIsNumericWithValidNumber() {
        assertTrue(handler.isNumeric("123"));
    }

    @Test
    void testIsNumericWithInvalidNumber() {
        assertFalse(handler.isNumeric("123a"));
    }

    @Test
    void testBuildMap() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");
        var map = handler.buildMap(business);
        assertNotNull(map);
    }

    @Test
    void testBuildMapDetail() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");
        var map = handler.buildMapDetail(business);
        assertNotNull(map);
    }

    @Test
    void testGetTraceIdWithContext() {
        Mockito.when(context.getTraceId()).thenReturn("test-trace-id");
        assertEquals("test-trace-id", handler.getTraceId());
    }

    @Test
    void testGetTraceIdWithoutContext() {
        Ex.setContext(null);
        String traceId = handler.getTraceId();
        assertNotNull(traceId);
    }

    @Test
    void testGetSceneWithContext() {
        Mockito.when(context.getScene()).thenReturn("test-scene");
        assertEquals("test-scene", handler.getScene());
    }

    @Test
    void testGetSceneWithoutContext() {
        Ex.setContext(null);
        String scene = handler.getScene();
        assertNotNull(scene);
    }

    @Test
    void testNotifyValidationStart() {
        handler.notifyValidationStart("test-scene");
    }

    @Test
    void testNotifyValidationEnd() {
        handler.notifyValidationEnd(1000, true);
    }

    @Test
    void testNotifyValidationFailure() {
        handler.notifyValidationFailure("400");
    }

    @Test
    void testHandleMethodArgumentNotValidExceptionWithoutValidateAnnotation() throws Exception {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        Method method = getClass().getMethod("testMethod");
        Mockito.when(parameter.getMethod()).thenReturn(method);

        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError = Mockito.mock(FieldError.class);
        Mockito.when(fieldError.getField()).thenReturn("field");
        Mockito.when(fieldError.getDefaultMessage()).thenReturn("Invalid field");
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);
        var response = handler.handleMethodArgumentNotValidException(exception);
        assertNotNull(response);
    }

    @Test
    void testHandleConstraintViolationExceptionWithNullRootBeanClass() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
        jakarta.validation.Path path = Mockito.mock(jakarta.validation.Path.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(path.toString()).thenReturn("field");
        Mockito.when(violation.getMessage()).thenReturn("Invalid field");
        Mockito.when(violation.getRootBeanClass()).thenReturn(null);

        @SuppressWarnings("unchecked")
        ConstraintViolationException exception = new ConstraintViolationException((Set) Set.of(violation));
        var response = handler.handleConstraintViolationException(exception);
        assertNotNull(response);
    }

    // 测试方法，用于模拟MethodArgumentNotValidException
    public void testMethod() {}

    @Validate(fast = true)
    public void testMethodWithValidate() {}

    // 测试实现类
    private static class TestFailFastExceptionHandler extends FailFastExceptionHandler {
        // 暴露protected和private方法用于测试
        public boolean isVerbose() {
            try {
                var method = FailFastExceptionHandler.class.getDeclaredMethod("isVerbose");
                method.setAccessible(true);
                return (boolean) method.invoke(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String formatValidationLocation(Class<?> clazz, String fieldOrPath) {
            try {
                var method = FailFastExceptionHandler.class.getDeclaredMethod("formatValidationLocation", Class.class, String.class);
                method.setAccessible(true);
                return (String) method.invoke(this, clazz, fieldOrPath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Business parseError(String message, String location, String methodName) {
            try {
                var method = FailFastExceptionHandler.class.getDeclaredMethod("parseError", String.class, String.class, String.class);
                method.setAccessible(true);
                return (Business) method.invoke(this, message, location, methodName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Map<String, Object> buildMap(Business e) {
            try {
                var method = FailFastExceptionHandler.class.getDeclaredMethod("buildMap", Business.class);
                method.setAccessible(true);
                return (Map<String, Object>) method.invoke(this, e);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        public Map<String, Object> buildMapDetail(Business e) {
            try {
                var method = FailFastExceptionHandler.class.getDeclaredMethod("buildMapDetail", Business.class);
                method.setAccessible(true);
                return (Map<String, Object>) method.invoke(this, e);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        public String getTraceId() {
            try {
                var method = FailFastExceptionHandler.class.getDeclaredMethod("getTraceId");
                method.setAccessible(true);
                return (String) method.invoke(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getScene() {
            try {
                var method = FailFastExceptionHandler.class.getDeclaredMethod("getScene");
                method.setAccessible(true);
                return (String) method.invoke(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void notifyValidationStart(String scene) {
            try {
                var method = FailFastExceptionHandler.class.getDeclaredMethod("notifyValidationStart", String.class);
                method.setAccessible(true);
                method.invoke(this, scene);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void notifyValidationEnd(long durationNanos, boolean success) {
            try {
                var method = FailFastExceptionHandler.class.getDeclaredMethod("notifyValidationEnd", long.class, boolean.class);
                method.setAccessible(true);
                method.invoke(this, durationNanos, success);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void notifyValidationFailure(String errorCode) {
            try {
                var method = FailFastExceptionHandler.class.getDeclaredMethod("notifyValidationFailure", String.class);
                method.setAccessible(true);
                method.invoke(this, errorCode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 解析验证消息的方法
        public Object parseValidationMessage(String raw) {
            try {
                var method = FailFastExceptionHandler.class.getDeclaredMethod("parseValidationMessage", String.class);
                method.setAccessible(true);
                return method.invoke(this, raw);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 检查字符串是否为数字的方法
        public boolean isNumeric(String str) {
            try {
                var method = FailFastExceptionHandler.class.getDeclaredMethod("isNumeric", String.class);
                method.setAccessible(true);
                return (boolean) method.invoke(this, str);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 处理多个错误的方法
        public Object handleMultiErrors(List<Business> errors) {
            try {
                var method = FailFastExceptionHandler.class.getDeclaredMethod("handleMultiErrors", List.class);
                method.setAccessible(true);
                return method.invoke(this, errors);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
