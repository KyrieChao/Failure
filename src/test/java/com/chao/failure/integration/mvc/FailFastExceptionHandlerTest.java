package com.chao.failure.integration.mvc;

import com.chao.failure.annotation.Validate;
import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.constant.FailureConst;
import com.chao.failure.constant.Severity;
import com.chao.failure.exception.Business;
import com.chao.failure.exception.MultiBusiness;
import com.chao.failure.internal.core.Ex;
import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.internal.core.observability.OpenTelemetryBridge;
import com.chao.failure.internal.validation.ValidationEventManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class FailFastExceptionHandlerTest {

    private TestFailFastExceptionHandler handler;
    private FailureProperties properties;
    private FailureContext context;

    @BeforeEach
    void setUp() {
        handler = new TestFailFastExceptionHandler();
        properties = new FailureProperties();
        context = Mockito.mock(FailureContext.class);
        handler.setFailFastProperties(properties);
        Ex.setContext(context);
        when(context.getScene()).thenReturn(null);
        when(context.getTraceId()).thenReturn(null);
        when(context.resolveSeverity(Mockito.any())).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        Ex.setContext(null);
    }

    @Test
    void handleBusinessException_shouldBuildSingleResponse() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "single");

        ResponseEntity<?> response = handler.handleBusinessException(business);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ResponseCode.VALIDATION_ERROR_400.getCode(), body(response).get(FailureConst.FIELD_CODE));
        assertInstanceOf(Map.class, handler.buildBody(business));
    }

    @Test
    void handleMultiBusinessException_shouldBuildVerboseMultiResponse() {
        properties.setVerbose(true);
        MultiBusiness multi = new MultiBusiness(List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "first"),
                Business.of(ResponseCode.VALIDATION_ERROR_400, "second")
        ));

        ResponseEntity<?> response = handler.handleMultiBusinessException(multi);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(2, errors(body(response)).size());
        assertInstanceOf(Map.class, handler.buildBody(multi));
    }

    @Test
    void handleMethodArgumentNotValidException_shouldFailFastByDefaultWhenMethodAndTargetAreMissing() {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        when(parameter.getMethod()).thenReturn(null);

        BindingResult result = Mockito.mock(BindingResult.class);
        FieldError first = Mockito.mock(FieldError.class);
        FieldError second = Mockito.mock(FieldError.class);
        when(first.getField()).thenReturn("name");
        when(first.getDefaultMessage()).thenReturn("410: first");
        when(second.getField()).thenReturn("age");
        when(second.getDefaultMessage()).thenReturn("411: second");
        when(result.getFieldErrors()).thenReturn(List.of(first, second));
        when(result.getTarget()).thenReturn(null);

        ResponseEntity<?> response = handler.handleMethodArgumentNotValidException(
                new MethodArgumentNotValidException(parameter, result)
        );

        Map<String, Object> body = body(response);

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertEquals(410, body.get(FailureConst.FIELD_CODE));
        assertFalse(body.containsKey(FailureConst.FIELD_ERRORS));
    }

    @Test
    void handleMethodArgumentNotValidException_shouldKeepAllErrorsWhenValidateFastIsFalse() throws Exception {
        properties.setVerbose(true);
        when(context.getScene()).thenReturn("mvc-scene");
        Method method = SampleController.class.getMethod("collectAll", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Payload(), "payload");
        result.addError(new FieldError("payload", "name", "420: invalid-name"));
        result.addError(new FieldError("payload", "age", "421: invalid-age"));

        ResponseEntity<?> response = handler.handleMethodArgumentNotValidException(
                new MethodArgumentNotValidException(parameter, result)
        );

        Map<String, Object> body = body(response);

        assertEquals(HttpStatus.METHOD_FAILURE, response.getStatusCode());
        assertEquals("mvc-scene", body.get(FailureConst.FIELD_SCENE));
        assertEquals(2, errors(body).size());
    }

    @Test
    void handleConstraintViolationException_shouldSupportSingleAndMultiViolations() {
        properties.setVerbose(true);

        ConstraintViolation<Object> single = violation(SampleController.class, "plain.name", "430: invalid-name");
        ResponseEntity<?> singleResponse = handler.handleConstraintViolationException(
                new ConstraintViolationException(rawSet(single))
        );
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, singleResponse.getStatusCode());
        assertEquals(1, errors(body(singleResponse)).size());

        ConstraintViolation<Object> first = violation(SampleController.class, "plain.name", "431: first");
        ConstraintViolation<Object> second = violation(null, "age", "432: second");
        ResponseEntity<?> multiResponse = handler.handleConstraintViolationException(
                new ConstraintViolationException(rawSet(first, second))
        );

        assertEquals(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, multiResponse.getStatusCode());
        assertEquals(2, errors(body(multiResponse)).size());
    }

    @Test
    void buildMap_shouldCoverTraceSceneAndDetailBranches() {
        properties.getTraceId().setEnabled(true);
        when(context.getScene()).thenReturn("api-scene");
        Business business = mockedBusiness("detail", Severity.ERROR, null, null, "payload.name", "raw-value");

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentTraceId).thenReturn("otel-trace");
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn("otel-span");

            Map<String, Object> map = handler.buildMap(business);
            Map<String, Object> detail = handler.buildMapDetail(business);

            assertEquals("otel-trace", map.get(FailureConst.FIELD_TRACE_ID));
            assertEquals("otel-span", map.get(FailureConst.FIELD_SPAN_ID));
            assertEquals("api-scene", map.get(FailureConst.FIELD_SCENE));
            assertEquals("payload.name", detail.get(FailureConst.FIELD_PATH));
            assertEquals("raw-value", detail.get(FailureConst.FIELD_REJECTED));
        }
    }

    @Test
    void buildMap_shouldSkipTraceAndSceneWhenDisabledOrBlank() {
        properties.getTraceId().setEnabled(false);
        when(context.getScene()).thenReturn(FailureConst.DEFAULT_SCENE);
        Business business = mockedBusiness("detail", Severity.INFO, "trace-direct", "span-direct", null, null);

        Map<String, Object> defaultScene = handler.buildMap(business);

        assertFalse(defaultScene.containsKey(FailureConst.FIELD_TRACE_ID));
        assertFalse(defaultScene.containsKey(FailureConst.FIELD_SPAN_ID));
        assertFalse(defaultScene.containsKey(FailureConst.FIELD_SCENE));

        when(context.getScene()).thenReturn("   ");
        assertFalse(handler.buildMap(business).containsKey(FailureConst.FIELD_SCENE));
    }

    @Test
    void buildMultiMap_shouldCoverVerboseAndTraceIdConfigurationBranches() {
        FailureProperties traceIdMissingProps = new FailureProperties();
        traceIdMissingProps.setVerbose(true);
        traceIdMissingProps.setTraceId(null);
        handler.setFailFastProperties(traceIdMissingProps);
        when(context.getScene()).thenReturn("multi-scene");

        MultiBusiness verboseMulti = mockedMultiBusiness("trace-explicit", List.of(
                mockedBusiness("first", Severity.INFO, null, null, "f1", "v1"),
                mockedBusiness("second", Severity.WARNING, null, null, "f2", "v2")
        ));

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn("otel-span");

            Map<String, Object> verboseMap = handler.buildMultiMap(verboseMulti);

            assertEquals("trace-explicit", verboseMap.get(FailureConst.FIELD_TRACE_ID));
            assertEquals("otel-span", verboseMap.get(FailureConst.FIELD_SPAN_ID));
            assertEquals("multi-scene", verboseMap.get(FailureConst.FIELD_SCENE));
            assertEquals(2, errors(verboseMap).size());
        }

        FailureProperties disabledProps = new FailureProperties();
        disabledProps.setVerbose(false);
        disabledProps.getTraceId().setEnabled(true);
        handler.setFailFastProperties(disabledProps);
        when(context.getScene()).thenReturn(FailureConst.DEFAULT_SCENE);
        when(context.getTraceId()).thenReturn(" ");
        MultiBusiness blankMulti = mockedMultiBusiness(" ", List.of(mockedBusiness("only", Severity.INFO, null, null, null, null)));

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentTraceId).thenReturn(" ");
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn(" ");

            Map<String, Object> map = handler.buildMultiMap(blankMulti);

            assertFalse(map.containsKey(FailureConst.FIELD_TRACE_ID));
            assertFalse(map.containsKey(FailureConst.FIELD_SPAN_ID));
            assertFalse(map.containsKey(FailureConst.FIELD_ERRORS));
        }
    }

    @Test
    void privateHelpers_shouldCoverFormattingParsingAndNumericChecks() {
        String unknown = invokePrivate("formatValidationLocation", new Class[]{Class.class, String.class}, Payload.class, null);
        String simple = invokePrivate("formatValidationLocation", new Class[]{Class.class, String.class}, Payload.class, "name");
        String nested = invokePrivate("formatValidationLocation", new Class[]{Class.class, String.class}, Payload.class, "plain.name");
        String noClass = invokePrivate("formatValidationLocation", new Class[]{Class.class, String.class}, null, "plain.name");
        String proxy = invokePrivate("formatValidationLocation", new Class[]{Class.class, String.class}, Proxy$$Payload.class, "field");

        assertNotNull(unknown);
        assertTrue(simple.contains("name"));
        assertTrue(nested.contains("plain"));
        assertTrue(noClass.contains("plain"));
        assertTrue(proxy.contains(Payload.class.getSimpleName()));

        Business nullMessage = invokePrivate("parseError",
                new Class[]{String.class, String.class, String.class}, null, "location", "method");
        Business blankMessage = invokePrivate("parseError",
                new Class[]{String.class, String.class, String.class}, "   ", "location", "method");
        Business codeAndText = invokePrivate("parseError",
                new Class[]{String.class, String.class, String.class}, "440: custom", "location", "method");
        Business codeOnly = invokePrivate("parseError",
                new Class[]{String.class, String.class, String.class}, "441", "location", "method");
        Business plainText = invokePrivate("parseError",
                new Class[]{String.class, String.class, String.class}, "plain-text", null, "method");

        assertEquals(ResponseCode.VALIDATION_ERROR_400.getCode(), nullMessage.getResponseCode().getCode());
        assertEquals(ResponseCode.VALIDATION_ERROR_400.getCode(), blankMessage.getResponseCode().getCode());
        assertEquals(440, codeAndText.getResponseCode().getCode());
        assertEquals(441, codeOnly.getResponseCode().getCode());
        assertEquals("plain-text", plainText.getDetail());

        Object parsedNull = invokePrivate("parseValidationMessage", new Class[]{String.class}, new Object[]{null});
        Object parsedBlank = invokePrivate("parseValidationMessage", new Class[]{String.class}, "   ");
        Object parsedCodeText = invokePrivate("parseValidationMessage", new Class[]{String.class}, "442: parsed");
        Object parsedCodeOnly = invokePrivate("parseValidationMessage", new Class[]{String.class}, "443");
        Object parsedColonText = invokePrivate("parseValidationMessage", new Class[]{String.class}, "x: parsed");

        assertNull(invokeRecord(parsedNull, "code"));
        assertNull(invokeRecord(parsedBlank, "text"));
        assertEquals(Integer.valueOf(442), invokeRecord(parsedCodeText, "code"));
        assertEquals("parsed", invokeRecord(parsedCodeText, "text"));
        assertEquals(Integer.valueOf(443), invokeRecord(parsedCodeOnly, "code"));
        assertEquals("x: parsed", invokeRecord(parsedColonText, "text"));

        assertFalse((Boolean) invokePrivate("isNumeric", new Class[]{String.class}, new Object[]{null}));
        assertFalse((Boolean) invokePrivate("isNumeric", new Class[]{String.class}, ""));
        assertTrue((Boolean) invokePrivate("isNumeric", new Class[]{String.class}, "12345"));
        assertFalse((Boolean) invokePrivate("isNumeric", new Class[]{String.class}, "12a45"));
    }

    @Test
    void privateHelpers_shouldCoverTraceSceneSeverityLoggingAndResolution() {
        Business nullSeverity = mockedBusiness("null-severity", null, null, null, null, null);
        Business critical = mockedBusiness("critical", Severity.CRITICAL, "trace-direct", "span-direct", null, null);

        MultiBusiness severityMulti = Mockito.mock(MultiBusiness.class);
        when(severityMulti.getErrors()).thenReturn(Arrays.asList(null, nullSeverity, critical));
        assertEquals(Severity.CRITICAL, invokePrivate("resolveMultiSeverity", new Class[]{MultiBusiness.class}, severityMulti));

        invokePrivate("logBySeverity", new Class[]{Severity.class, String.class, Object[].class}, null, "{}", new Object[]{"info"});
        invokePrivate("logBySeverity", new Class[]{Severity.class, String.class, Object[].class}, Severity.DEBUG, "{}", new Object[]{"debug"});
        invokePrivate("logBySeverity", new Class[]{Severity.class, String.class, Object[].class}, Severity.INFO, "{}", new Object[]{"info"});
        invokePrivate("logBySeverity", new Class[]{Severity.class, String.class, Object[].class}, Severity.WARNING, "{}", new Object[]{"warn"});
        invokePrivate("logBySeverity", new Class[]{Severity.class, String.class, Object[].class}, Severity.ERROR, "{}", new Object[]{"error"});
        invokePrivate("logBySeverity", new Class[]{Severity.class, String.class, Object[].class}, Severity.CRITICAL, "{}", new Object[]{"critical"});
        handler.logException(critical);
        handler.logException(new MultiBusiness(List.of(Business.of(ResponseCode.VALIDATION_ERROR_400, "a"))));

        assertEquals("null", invokePrivate("renderLogMessage", new Class[]{Business.class}, new Object[]{null}));

        properties.getTraceId().setEnabled(false);
        assertEquals("business{disabled}", invokePrivate("renderLogMessage",
                new Class[]{Business.class}, mockedBusiness("disabled", Severity.INFO, null, null, null, null, "business{disabled}")));

        properties.getTraceId().setEnabled(true);
        when(context.getTraceId()).thenReturn("ctx-trace");
        assertEquals("business{trace} [traceId=ctx-trace]", invokePrivate("renderLogMessage",
                new Class[]{Business.class}, mockedBusiness("trace", Severity.INFO, null, null, null, null, "business{trace}")));

        when(context.getTraceId()).thenReturn(" ");
        assertEquals("business{blank}", invokePrivate("renderLogMessage",
                new Class[]{Business.class}, mockedBusiness("blank", Severity.INFO, " ", null, null, null, "business{blank}")));

        when(context.getTraceId()).thenReturn("ctx-trace");
        assertEquals("trace-direct", invokePrivate("resolveTraceId", new Class[]{Business.class}, critical));
        assertEquals("span-direct", invokePrivate("resolveSpanId", new Class[]{Business.class}, critical));
        assertEquals("ctx-trace", invokePrivate("resolveTraceId", new Class[]{Business.class}, new Object[]{null}));

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentTraceId).thenReturn("otel-trace");
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn("otel-span");

            Ex.setContext(null);
            assertEquals("otel-trace", invokePrivate("getTraceId", new Class[]{}));
            assertEquals("otel-span", invokePrivate("resolveSpanId", new Class[]{Business.class}, new Object[]{null}));
            assertEquals(FailureConst.DEFAULT_SCENE, invokePrivate("getScene", new Class[]{}));
        }

        Ex.setContext(context);
        when(context.getTraceId()).thenReturn(" ");
        assertEquals(" ", invokePrivate("getTraceId", new Class[]{}));

        when(context.getTraceId()).thenReturn(null);
        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentTraceId).thenReturn(" ");

            String generated = invokePrivate("getTraceId", new Class[]{});

            assertDoesNotThrow(() -> UUID.fromString(generated));
        }

        when(context.getScene()).thenReturn("scene-x");
        assertEquals("scene-x", invokePrivate("getScene", new Class[]{}));

        handler.setFailFastProperties(null);
        Business fallbackBusiness = mockedBusiness("fallback", Severity.INFO, null, null, null, null);
        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentTraceId).thenReturn("otel-trace-2");
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn("otel-span-2");

            Map<String, Object> map = handler.buildMap(fallbackBusiness);

            assertEquals("otel-trace-2", map.get(FailureConst.FIELD_TRACE_ID));
            assertEquals("otel-span-2", map.get(FailureConst.FIELD_SPAN_ID));
        }
    }

    @Test
    void privateNotificationAndMultiErrorHelpers_shouldDelegateCorrectly() {
        try (MockedStatic<ValidationEventManager> events = mockStatic(ValidationEventManager.class)) {
            invokePrivate("notifyValidationStart", new Class[]{String.class}, "scene-a");
            invokePrivate("notifyValidationEnd", new Class[]{long.class, boolean.class}, 7L, true);
            invokePrivate("notifyValidationFailure", new Class[]{String.class}, "499");

            events.verify(() -> ValidationEventManager.notifyStart(FailureConst.FIELD_METHOD, "scene-a"));
            events.verify(() -> ValidationEventManager.notifyEnd(FailureConst.FIELD_METHOD, 7L, true));
            events.verify(() -> ValidationEventManager.notifyFailure(FailureConst.FIELD_METHOD, "499"));
        }

        properties.setVerbose(true);
        Map<String, Object> empty = body((ResponseEntity<?>) invokePrivate("handleMultiErrors",
                new Class[]{List.class}, List.of()));
        Map<String, Object> single = body((ResponseEntity<?>) invokePrivate("handleMultiErrors",
                new Class[]{List.class}, List.of(Business.of(ResponseCode.VALIDATION_ERROR_400, "single"))));
        Map<String, Object> multi = body((ResponseEntity<?>) invokePrivate("handleMultiErrors",
                new Class[]{List.class}, List.of(
                        Business.of(ResponseCode.VALIDATION_ERROR_400, "first"),
                        Business.of(ResponseCode.VALIDATION_ERROR_400, "second")
                )));

        assertEquals(ResponseCode.VALIDATION_ERROR.getCode(), empty.get(FailureConst.FIELD_CODE));
        assertEquals(1, errors(single).size());
        assertEquals(2, errors(multi).size());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> body(ResponseEntity<?> response) {
        return (Map<String, Object>) response.getBody();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> errors(Map<String, Object> body) {
        return (List<Map<String, Object>>) body.get(FailureConst.FIELD_ERRORS);
    }

    @SuppressWarnings("unchecked")
    private static Set<ConstraintViolation<?>> rawSet(ConstraintViolation<?>... violations) {
        return (Set<ConstraintViolation<?>>) (Set<?>) new LinkedHashSet<>(Arrays.asList(violations));
    }

    @SuppressWarnings("unchecked")
    private <T> T invokePrivate(String name, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = FailFastExceptionHandler.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return (T) method.invoke(handler, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokeRecord(Object target, String accessor) {
        try {
            Method method = target.getClass().getMethod(accessor);
            return (T) method.invoke(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ConstraintViolation<Object> violation(Class<?> rootBeanClass, String propertyPath, String message) {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
        Path path = Mockito.mock(Path.class);
        @SuppressWarnings("unchecked")
        Class<Object> typedRootBeanClass = (Class<Object>) rootBeanClass;
        when(violation.getRootBeanClass()).thenReturn(typedRootBeanClass);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    private static Business mockedBusiness(String detail, Severity severity, String traceId, String spanId,
                                          String path, Object invalidValue) {
        return mockedBusiness(detail, severity, traceId, spanId, path, invalidValue, "business{" + detail + "}");
    }

    private static Business mockedBusiness(String detail, Severity severity, String traceId, String spanId,
                                          String path, Object invalidValue, String text) {
        Business business = Mockito.mock(Business.class);
        when(business.getResponseCode()).thenReturn(ResponseCode.VALIDATION_ERROR_400);
        when(business.getDetail()).thenReturn(detail);
        when(business.getHttpStatus()).thenReturn(HttpStatus.BAD_REQUEST);
        when(business.getSeverity()).thenReturn(severity);
        when(business.getTraceId()).thenReturn(traceId);
        when(business.getSpanId()).thenReturn(spanId);
        when(business.getPath()).thenReturn(path);
        when(business.getInvalidValue()).thenReturn(invalidValue);
        when(business.toString()).thenReturn(text);
        return business;
    }

    private static MultiBusiness mockedMultiBusiness(String traceId, List<Business> errors) {
        MultiBusiness multi = Mockito.mock(MultiBusiness.class);
        when(multi.getResponseCode()).thenReturn(ResponseCode.VALIDATION_ERROR_400);
        when(multi.getDetail()).thenReturn("multi");
        when(multi.getHttpStatus()).thenReturn(HttpStatus.BAD_REQUEST);
        when(multi.getTraceId()).thenReturn(traceId);
        when(multi.getErrors()).thenReturn(errors);
        return multi;
    }

    static class Payload {
        String name;
        int age;
    }

    static class Proxy$$Payload extends Payload {
    }

    static class SampleController {
        public void plain(String name) {
        }

        @Validate(fast = false)
        public void collectAll(String name) {
        }
    }

    private static class TestFailFastExceptionHandler extends FailFastExceptionHandler {
    }
}
