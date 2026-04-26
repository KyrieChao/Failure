package com.chao.failfast.integration.mvc;

import com.chao.failfast.annotation.Validate;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.constant.Severity;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.internal.core.Ex;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.exception.Business;
import com.chao.failfast.exception.MultiBusiness;
import com.chao.failfast.internal.core.observability.OpenTelemetryBridge;
import com.chao.failfast.internal.core.observability.TraceInfoExtractor;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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
import static org.mockito.Mockito.mockStatic;

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
    void testBuildResponseWithTraceAndSpan() {
        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("Error")
                .traceId("trace-mvc")
                .spanId("span-mvc")
                .materialize();
        FailureProperties local = new FailureProperties();
        local.getTraceId().setEnabled(true);
        handler.setFailFastProperties(local);
        var response = handler.buildResponse(business);
        assertNotNull(response);
        assertTrue(((Map<?, ?>) response.getBody()).containsKey(FailureConst.FIELD_TRACE_ID));
        assertTrue(((Map<?, ?>) response.getBody()).containsKey(FailureConst.FIELD_SPAN_ID));
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
    void testRenderLogMessageUseBannerWhenShadowTraceEnabled() throws Exception {
        FailureProperties local = new FailureProperties();
        local.getLogging().setBanner(true);
        handler.setFailFastProperties(local);
        Mockito.when(context.isShadowTrace()).thenReturn(true);
        Mockito.when(context.getTraceId()).thenReturn("trace-banner");

        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");
        java.lang.reflect.Method m = FailFastExceptionHandler.class.getDeclaredMethod("renderLogMessage", Business.class);
        m.setAccessible(true);
        String out = (String) m.invoke(handler, business);

        assertTrue(out.startsWith("BANNER{"));
        assertTrue(out.contains("trace-banner"));
    }

    @Test
    void testRenderLogMessageFallbackToDefaultWhenShadowTraceDisabled() throws Exception {
        FailureProperties local = new FailureProperties();
        local.getLogging().setBanner(true);
        handler.setFailFastProperties(local);
        Mockito.when(context.isShadowTrace()).thenReturn(false);

        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");
        java.lang.reflect.Method m = FailFastExceptionHandler.class.getDeclaredMethod("renderLogMessage", Business.class);
        m.setAccessible(true);
        String out = (String) m.invoke(handler, business);

        assertFalse(out.startsWith("BANNER{"));
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
    void testBuildMapWithoutTraceIdWhenDisabled() {
        FailureProperties.TraceId traceId = new FailureProperties.TraceId();
        traceId.setEnabled(false);
        Mockito.when(properties.getTraceId()).thenReturn(traceId);

        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");
        var map = handler.buildMap(business);
        assertNotNull(map);
        assertFalse(map.containsKey(FailureConst.FIELD_TRACE_ID));
    }

    @Test
    void testBuildMultiErrorResponseWithoutTraceIdWhenDisabled() {
        FailureProperties.TraceId traceId = new FailureProperties.TraceId();
        traceId.setEnabled(false);
        Mockito.when(properties.getTraceId()).thenReturn(traceId);
        Mockito.when(properties.isVerbose()).thenReturn(false);

        List<Business> errors = List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"),
                Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 2")
        );
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        var response = handler.buildMultiErrorResponse(multiBusiness);
        assertNotNull(response);
        assertFalse(((Map<?, ?>) response.getBody()).containsKey(FailureConst.FIELD_TRACE_ID));
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

    @Test
    void should_renderNullLiteral_when_renderLogMessageReceivesNullBusiness() throws Exception {
        Method m = FailFastExceptionHandler.class.getDeclaredMethod("renderLogMessage", Business.class);
        m.setAccessible(true);

        String out = (String) m.invoke(handler, new Object[]{null});

        assertEquals("null", out);
    }

    @Test
    void should_enableBannerMode_when_loggingConfigIsMissingButShadowTraceIsTrue() throws Exception {
        FailureContext localContext = Mockito.mock(FailureContext.class);
        Mockito.when(localContext.isShadowTrace()).thenReturn(true);
        Ex.setContext(localContext);
        handler.setFailFastProperties(new FailureProperties());
        handler.setFailFastProperties(null);

        Method method = FailFastExceptionHandler.class.getDeclaredMethod("isBannerMode");
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(handler);

        assertTrue(result);
    }

    @Test
    void should_pickHighestSeverity_when_multiBusinessContainsNulls() throws Exception {
        List<Business> errors = List.of(
                Business.compose().responseCode(ResponseCode.VALIDATION_ERROR_400).detail("a").severity(null).materialize(),
                Business.compose().responseCode(ResponseCode.VALIDATION_ERROR_400).detail("b").severity(Severity.CRITICAL).materialize()
        );
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        Method method = FailFastExceptionHandler.class.getDeclaredMethod("resolveMultiSeverity", MultiBusiness.class);
        method.setAccessible(true);

        Severity result = (Severity) method.invoke(handler, multiBusiness);

        assertEquals(Severity.CRITICAL, result);
    }

    @Test
    void should_coverAllSeverityBranches_when_logBySeverityInvokedReflectively() throws Exception {
        Method method = FailFastExceptionHandler.class.getDeclaredMethod("logBySeverity", Severity.class, String.class, Object[].class);
        method.setAccessible(true);

        method.invoke(handler, null, "{}", new Object[]{"a"});
        method.invoke(handler, Severity.WARNING, "{}", new Object[]{"a"});
        method.invoke(handler, Severity.ERROR, "{}", new Object[]{"a"});
        method.invoke(handler, Severity.INFO, "{}", new Object[]{"a"});
        method.invoke(handler, Severity.CRITICAL, "{}", new Object[]{"a"});
    }

    @Test
    void should_useOpenTelemetryFallbacks_when_traceAndSpanAreMissing() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");
        Mockito.when(context.getTraceId()).thenReturn(null);

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentTraceId).thenReturn("otel-trace");
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn("otel-span");

            Map<String, Object> body = handler.buildMap(business);

            assertEquals("otel-trace", body.get(FailureConst.FIELD_TRACE_ID));
            assertEquals("otel-span", body.get(FailureConst.FIELD_SPAN_ID));
        }
    }

    @Test
    void should_fallbackResolveTraceAndSpan_when_businessValuesAreBlank() throws Exception {
        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("x")
                .traceId(" ")
                .spanId(" ")
                .materialize();
        Mockito.when(context.getTraceId()).thenReturn("ctx-trace");

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn("otel-span");

            Method resolveTraceId = FailFastExceptionHandler.class.getDeclaredMethod("resolveTraceId", Business.class);
            resolveTraceId.setAccessible(true);
            Method resolveSpanId = FailFastExceptionHandler.class.getDeclaredMethod("resolveSpanId", Business.class);
            resolveSpanId.setAccessible(true);

            assertEquals("ctx-trace", resolveTraceId.invoke(handler, business));
            assertEquals("otel-span", resolveSpanId.invoke(handler, business));
        }
    }

    @Test
    void should_useBusinessTraceAndSpan_when_buildMultiErrorResponseReceivesExplicitValues() {
        FailureProperties local = new FailureProperties();
        local.getTraceId().setEnabled(true);
        handler.setFailFastProperties(local);

        MultiBusiness multi = Mockito.mock(MultiBusiness.class);
        Mockito.when(multi.getResponseCode()).thenReturn(ResponseCode.VALIDATION_ERROR_400);
        Mockito.when(multi.getDetail()).thenReturn("x");
        Mockito.when(multi.getHttpStatus()).thenReturn(org.springframework.http.HttpStatus.BAD_REQUEST);
        Mockito.when(multi.getTraceId()).thenReturn("trace-explicit");
        Mockito.when(multi.getErrors()).thenReturn(List.of());

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn("otel-span");

            var response = handler.buildMultiErrorResponse(multi);
            Map<?, ?> body = (Map<?, ?>) response.getBody();

            assertEquals("trace-explicit", body.get(FailureConst.FIELD_TRACE_ID));
            assertEquals("otel-span", body.get(FailureConst.FIELD_SPAN_ID));
        }
    }

    @Test
    void should_renderUnknownBannerFields_when_businessMetadataIsMissing() throws Exception {
        FailureProperties local = new FailureProperties();
        local.getLogging().setBanner(true);
        handler.setFailFastProperties(local);
        FailureContext localContext = Mockito.mock(FailureContext.class);
        Ex.setContext(localContext);
        Business business = Mockito.mock(Business.class);
        Mockito.when(business.getResponseCode()).thenReturn(null);
        Mockito.when(business.getPath()).thenReturn(null);
        Mockito.when(business.getTraceId()).thenReturn(null);

        try (var trace = mockStatic(com.chao.failfast.internal.core.observability.TraceInfoExtractor.class)) {
            trace.when(() -> com.chao.failfast.internal.core.observability.TraceInfoExtractor.shadowTrace(localContext, null)).thenReturn(true);
            Method method = FailFastExceptionHandler.class.getDeclaredMethod("renderLogMessage", Business.class);
            method.setAccessible(true);
            String result = (String) method.invoke(handler, business);

            assertTrue(result.contains("code=UNKNOWN"));
            assertTrue(result.contains("path=-"));
            assertTrue(result.contains("traceId="));
        }
    }

    @Test
    void should_useOpenTelemetryTraceId_when_contextTraceIdIsMissing() {
        Ex.setContext(null);

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentTraceId).thenReturn("otel-trace");

            assertEquals("otel-trace", handler.getTraceId());
        }
    }

    @Test
    void should_returnBusinessSpanId_when_resolveSpanIdReceivesNonBlankValue() throws Exception {
        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("x")
                .spanId("span-direct")
                .materialize();

        Method resolveSpanId = FailFastExceptionHandler.class.getDeclaredMethod("resolveSpanId", Business.class);
        resolveSpanId.setAccessible(true);

        assertEquals("span-direct", resolveSpanId.invoke(handler, business));
    }

    @Test
    void should_returnBusinessTraceId_when_resolveTraceIdReceivesNonBlankValue() throws Exception {
        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("x")
                .traceId("trace-direct")
                .materialize();

        Method resolveTraceId = FailFastExceptionHandler.class.getDeclaredMethod("resolveTraceId", Business.class);
        resolveTraceId.setAccessible(true);

        assertEquals("trace-direct", resolveTraceId.invoke(handler, business));
    }

    @Test
    void should_includeExplicitTraceAndSpan_when_buildMapReceivesBusinessMetadata() {
        FailureProperties local = new FailureProperties();
        local.getTraceId().setEnabled(true);
        handler.setFailFastProperties(local);
        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("x")
                .traceId("trace-direct")
                .spanId("span-direct")
                .materialize();

        Map<String, Object> body = handler.buildMap(business);

        assertEquals("trace-direct", body.get(FailureConst.FIELD_TRACE_ID));
        assertEquals("span-direct", body.get(FailureConst.FIELD_SPAN_ID));
    }

    @Test
    void should_treatDebugSeverityAsNoLogRequired_when_logBySeverityReceivesDebug() throws Exception {
        Method method = FailFastExceptionHandler.class.getDeclaredMethod("logBySeverity", Severity.class, String.class, Object[].class);
        method.setAccessible(true);

        method.invoke(handler, Severity.DEBUG, "{}", new Object[]{"a"});
    }

    @Test
    void should_skipBlankTraceAndSpan_when_buildMapResolvesOnlyBlankMetadata() {
        FailureProperties local = new FailureProperties();
        local.getTraceId().setEnabled(true);
        handler.setFailFastProperties(local);

        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("x")
                .traceId(" ")
                .spanId(" ")
                .materialize();
        Mockito.when(context.getTraceId()).thenReturn(" ");

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentTraceId).thenReturn(" ");
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn(" ");

            Map<String, Object> body = handler.buildMap(business);

            assertFalse(body.containsKey(FailureConst.FIELD_TRACE_ID));
            assertFalse(body.containsKey(FailureConst.FIELD_SPAN_ID));
        }
    }

    @Test
    void should_skipBlankTraceAndSpan_when_buildMultiErrorResponseResolvesOnlyBlankMetadata() {
        FailureProperties local = new FailureProperties();
        local.getTraceId().setEnabled(true);
        local.setVerbose(false);
        handler.setFailFastProperties(local);

        MultiBusiness multi = Mockito.mock(MultiBusiness.class);
        Mockito.when(multi.getResponseCode()).thenReturn(ResponseCode.VALIDATION_ERROR_400);
        Mockito.when(multi.getDetail()).thenReturn("x");
        Mockito.when(multi.getHttpStatus()).thenReturn(org.springframework.http.HttpStatus.BAD_REQUEST);
        Mockito.when(multi.getTraceId()).thenReturn(" ");
        Mockito.when(multi.getErrors()).thenReturn(List.of());
        Mockito.when(context.getTraceId()).thenReturn(" ");

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentTraceId).thenReturn(" ");
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn(" ");

            var response = handler.buildMultiErrorResponse(multi);
            Map<?, ?> body = (Map<?, ?>) response.getBody();

            assertFalse(body.containsKey(FailureConst.FIELD_TRACE_ID));
            assertFalse(body.containsKey(FailureConst.FIELD_SPAN_ID));
        }
    }

    @Test
    void should_enableBannerMode_when_shadowTraceTrueAndLoggingConfigIsNull() throws Exception {
        FailureProperties mockProperties = Mockito.mock(FailureProperties.class);
        FailureContext localContext = Mockito.mock(FailureContext.class);
        Ex.setContext(localContext);
        handler.setFailFastProperties(mockProperties);
        Mockito.when(mockProperties.getLogging()).thenReturn(null);

        Method method = FailFastExceptionHandler.class.getDeclaredMethod("isBannerMode");
        method.setAccessible(true);

        try (MockedStatic<TraceInfoExtractor> trace = mockStatic(TraceInfoExtractor.class)) {
            trace.when(() -> TraceInfoExtractor.shadowTrace(localContext, null)).thenReturn(true);

            assertTrue((boolean) method.invoke(handler));
        }
    }

    @Test
    void should_treatNullErrorsAsInfo_when_resolveMultiSeveritySeesNullEntry() throws Exception {
        Method method = FailFastExceptionHandler.class.getDeclaredMethod("resolveMultiSeverity", MultiBusiness.class);
        method.setAccessible(true);

        MultiBusiness multi = Mockito.mock(MultiBusiness.class);
        List<Business> errors = new ArrayList<>();
        errors.add(null);
        Mockito.when(multi.getErrors()).thenReturn(errors);

        assertEquals(Severity.INFO, method.invoke(handler, multi));
    }

    @Test
    void should_generateUuidTraceId_when_contextAndOpenTelemetryAreBlank() {
        Mockito.when(context.getTraceId()).thenReturn(null);

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentTraceId).thenReturn(" ");

            String traceId = handler.getTraceId();

            assertNotNull(traceId);
            assertFalse(traceId.isBlank());
            assertDoesNotThrow(() -> java.util.UUID.fromString(traceId));
        }
    }

    @Test
    void should_renderConcretePathInBanner_when_businessProvidesPath() throws Exception {
        FailureProperties local = new FailureProperties();
        local.getLogging().setBanner(true);
        handler.setFailFastProperties(local);
        Mockito.when(context.getTraceId()).thenReturn("trace-banner");

        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("x")
                .path("request.path")
                .materialize();

        Method method = FailFastExceptionHandler.class.getDeclaredMethod("renderLogMessage", Business.class);
        method.setAccessible(true);

        try (MockedStatic<TraceInfoExtractor> trace = mockStatic(TraceInfoExtractor.class)) {
            trace.when(() -> TraceInfoExtractor.shadowTrace(context, null)).thenReturn(true);
            String result = (String) method.invoke(handler, business);

            assertTrue(result.contains("path=request.path"));
        }
    }

    @Test
    void should_fallbackWhen_resolveTraceIdReceivesNullBusiness() throws Exception {
        Mockito.when(context.getTraceId()).thenReturn("ctx-trace");
        Method resolveTraceId = FailFastExceptionHandler.class.getDeclaredMethod("resolveTraceId", Business.class);
        resolveTraceId.setAccessible(true);

        assertEquals("ctx-trace", resolveTraceId.invoke(handler, new Object[]{null}));
    }

    @Test
    void should_fallbackWhen_resolveSpanIdReceivesNullBusiness() throws Exception {
        Method resolveSpanId = FailFastExceptionHandler.class.getDeclaredMethod("resolveSpanId", Business.class);
        resolveSpanId.setAccessible(true);

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn("otel-span");

            assertEquals("otel-span", resolveSpanId.invoke(handler, new Object[]{null}));
        }
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
