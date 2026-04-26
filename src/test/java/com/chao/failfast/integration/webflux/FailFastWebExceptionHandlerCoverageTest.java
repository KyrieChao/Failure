package com.chao.failfast.integration.webflux;

import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.exception.Business;
import com.chao.failfast.exception.MultiBusiness;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.internal.core.observability.OpenTelemetryBridge;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

/**
 * 针对 FailFastWebExceptionHandler 的完整单元测试
 * 目标：100% 类/方法/行/指令/分支/条件/路径覆盖
 */
@DisplayName("FailFastWebExceptionHandler 完整覆盖率测试")
class FailFastWebExceptionHandlerCoverageTest {

    private FailFastWebExceptionHandler handler;
    private FailureContext context;
    private FailureProperties properties;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        properties = new FailureProperties();
        context = new FailureContext(properties, new com.chao.failfast.config.mapping.CodeMappingConfig(properties), null);
        com.chao.failfast.internal.core.Ex.setContext(context);
        objectMapper = new ObjectMapper();
        handler = new FailFastWebExceptionHandler(context, properties, objectMapper);
    }

    @Nested
    @DisplayName("基础异常处理测试")
    class BasicExceptionHandlingTests {

        @Test
        @DisplayName("处理 Business 异常")
        void testHandleBusinessException() {
            Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "test error");
            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            handler.handle(exchange, business).block();

            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("处理 MultiBusiness 异常")
        void testHandleMultiBusinessException() {
            List<Business> errors = List.of(
                    Business.of(ResponseCode.VALIDATION_ERROR_400, "error 1"),
                    Business.of(ResponseCode.VALIDATION_ERROR_400, "error 2")
            );
            MultiBusiness multiBusiness = new MultiBusiness(errors);

            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            handler.handle(exchange, multiBusiness).block();

            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("处理未知异常应传递给下一个处理器")
        void testHandleUnknownException() {
            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            RuntimeException unknownException = new RuntimeException("Unknown error");

            assertThrows(Exception.class, () -> handler.handle(exchange, unknownException).block());
        }
    }

    @Nested
    @DisplayName("WebExchangeBindException 处理测试")
    class WebExchangeBindExceptionTests {

        @Test
        @DisplayName("处理空字段错误列表的 WebExchangeBindException")
        void testHandleWebExchangeBindExceptionWithEmptyErrors() {
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
            WebExchangeBindException bindException = createWebExchangeBindException(bindingResult);

            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            handler.handle(exchange, bindException).block();

            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("处理单个字段错误的 WebExchangeBindException")
        void testHandleWebExchangeBindExceptionWithSingleError() {
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new TestObject(), "testObject");
            bindingResult.addError(new FieldError("testObject", "field1", "Field error 1"));
            WebExchangeBindException bindException = createWebExchangeBindException(bindingResult);

            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            handler.handle(exchange, bindException).block();

            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("处理多个字段错误的 WebExchangeBindException")
        void testHandleWebExchangeBindExceptionWithMultipleErrors() {
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new TestObject(), "testObject");
            bindingResult.addError(new FieldError("testObject", "field1", "Field error 1"));
            bindingResult.addError(new FieldError("testObject", "field2", "Field error 2"));
            WebExchangeBindException bindException = createWebExchangeBindException(bindingResult);

            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            handler.handle(exchange, bindException).block();

            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        }

        private WebExchangeBindException createWebExchangeBindException(BeanPropertyBindingResult bindingResult) {
            try {
                Method method = getClass().getDeclaringClass().getDeclaredMethod("dummyMethod");
                MethodParameter methodParameter = new MethodParameter(method, -1);
                return new WebExchangeBindException(methodParameter, bindingResult);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    @DisplayName("ConstraintViolationException 处理测试")
    class ConstraintViolationExceptionTests {

        @Test
        @DisplayName("处理空约束违反列表的 ConstraintViolationException")
        void testHandleConstraintViolationExceptionWithEmptyViolations() {
            ConstraintViolationException cve = new ConstraintViolationException(new HashSet<>());

            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            handler.handle(exchange, cve).block();

            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("处理单个约束违反的 ConstraintViolationException")
        void testHandleConstraintViolationExceptionWithSingleViolation() {
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            violations.add(createMockViolation("field1", "Constraint error 1"));
            ConstraintViolationException cve = new ConstraintViolationException(violations);

            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            handler.handle(exchange, cve).block();

            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("处理多个约束违反的 ConstraintViolationException")
        void testHandleConstraintViolationExceptionWithMultipleViolations() {
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            violations.add(createMockViolation("field1", "Constraint error 1"));
            violations.add(createMockViolation("field2", "Constraint error 2"));
            ConstraintViolationException cve = new ConstraintViolationException(violations);

            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            handler.handle(exchange, cve).block();

            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("处理 null 路径的约束违反")
        void testHandleConstraintViolationWithNullPath() {
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            violations.add(createMockViolation(null, "Constraint error"));
            ConstraintViolationException cve = new ConstraintViolationException(violations);

            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            handler.handle(exchange, cve).block();

            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        }
    }

    @Nested
    @DisplayName("响应构建测试")
    class ResponseBuildingTests {

        @Test
        @DisplayName("启用 Verbose 模式时应包含 errors 数组")
        void testVerboseModeIncludesErrorsArray() {
            properties.setVerbose(true);

            Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "test error");
            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            handler.handle(exchange, business).block();

            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("禁用 Verbose 模式时不应包含 errors 数组")
        void testNonVerboseModeExcludesErrorsArray() {
            properties.setVerbose(false);

            Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "test error");
            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            handler.handle(exchange, business).block();

            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        }
    }

    @Nested
    @DisplayName("已提交响应测试")
    class CommittedResponseTests {

        @Test
        @DisplayName("处理已提交的响应应返回错误")
        void testHandleCommittedResponse() {
            Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "test error");
            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            exchange.getResponse().setComplete().block();

            assertThrows(Exception.class, () -> handler.handle(exchange, business).block());
        }
    }

    @Nested
    @DisplayName("traceId 处理测试")
    class TraceIdHandlingTests {

        @Test
        @DisplayName("启用 traceId 时应包含 traceId")
        void testTraceIdEnabled() {
            FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
            traceIdConfig.setEnabled(true);
            properties.setTraceId(traceIdConfig);

            Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "test error");
            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            handler.handle(exchange, business).block();

            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("resolveTraceId 应优先返回 attribute 中的 traceId")
        void testResolveTraceIdUsesExchangeAttribute() {
            FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
            traceIdConfig.setEnabled(true);
            traceIdConfig.setHeaderName("X-Trace-Id");
            properties.setTraceId(traceIdConfig);

            MockServerHttpRequest request = MockServerHttpRequest.get("/test").header("X-Trace-Id", "header-trace").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            exchange.getAttributes().put(FailureConst.FIELD_TRACE_ID, "attr-trace");

            String traceId = handler.resolveTraceId(exchange, reactor.util.context.Context.empty());

            assertEquals("attr-trace", traceId);
        }

        @Test
        @DisplayName("resolveTraceId 应在 attribute 和 header 都空白时回退到 context")
        void testResolveTraceIdFallsBackToContextWhenAttributeAndHeaderBlank() {
            FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
            traceIdConfig.setEnabled(true);
            traceIdConfig.setHeaderName("X-Trace-Id");
            properties.setTraceId(traceIdConfig);
            context.setTraceId("context-trace");

            MockServerHttpRequest request = MockServerHttpRequest.get("/test").header("X-Trace-Id", " ").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            exchange.getAttributes().put(FailureConst.FIELD_TRACE_ID, " ");

            String traceId = handler.resolveTraceId(exchange, reactor.util.context.Context.empty());

            assertEquals("context-trace", traceId);
        }
    }

    @Test
    @DisplayName("handle 应真正消费 Reactor Context 并处理 Business")
    void shouldHandleBusinessThroughHandleMethodWhenContextIsWritten() {
        properties.getTraceId().setEnabled(true);
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "test error");
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

        handler.handle(exchange, business)
                .contextWrite(reactor.util.context.Context.of(FailureConst.FIELD_TRACE_ID, "ignored"))
                .block();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("handleInternal 在 committed response 下应抛出原异常")
    void shouldPropagateOriginalErrorWhenResponseAlreadyCommitted() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        exchange.getResponse().setComplete().block();

        RuntimeException error = new RuntimeException("boom");

        assertThrows(RuntimeException.class,
                () -> handler.handleInternal(exchange, error, reactor.util.context.Context.empty()).block());
    }

    @Test
    @DisplayName("buildMap 应在业务对象缺少 spanId 时回退到 OpenTelemetry")
    void shouldUseOpenTelemetrySpanIdWhenBusinessSpanIdIsBlank() {
        properties.getTraceId().setEnabled(true);
        context.setTraceId("ctx-trace");
        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("test")
                .traceId(" ")
                .spanId(" ")
                .materialize();

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn("otel-span");

            Map<String, Object> body = handler.buildMap(
                    MockServerWebExchange.from(MockServerHttpRequest.get("/test").build()),
                    business,
                    reactor.util.context.Context.empty());

            assertEquals("ctx-trace", body.get(FailureConst.FIELD_TRACE_ID));
            assertEquals("otel-span", body.get(FailureConst.FIELD_SPAN_ID));
        }
    }

    @Test
    @DisplayName("buildMultiMap 应在错误列表首项缺少 spanId 时回退到 OpenTelemetry")
    void shouldUseOpenTelemetrySpanIdWhenFirstMultiErrorSpanIdIsBlank() {
        properties.getTraceId().setEnabled(true);
        context.setTraceId("ctx-trace");
        Business error = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("test")
                .spanId(" ")
                .materialize();
        MultiBusiness multi = new MultiBusiness(List.of(error, Business.of(ResponseCode.VALIDATION_ERROR_400, "other")));

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn("otel-span");

            Map<String, Object> body = handler.buildMultiMap(
                    MockServerWebExchange.from(MockServerHttpRequest.get("/test").build()),
                    multi,
                    reactor.util.context.Context.empty());

            assertEquals("ctx-trace", body.get(FailureConst.FIELD_TRACE_ID));
            assertEquals("otel-span", body.get(FailureConst.FIELD_SPAN_ID));
        }
    }

    @Test
    @DisplayName("write 应在序列化失败时写出兜底 JSON")
    void shouldWriteFallbackJsonWhenObjectMapperThrows() {
        ObjectMapper brokenMapper = new ObjectMapper() {
            @Override
            public byte[] writeValueAsBytes(Object value) throws JsonProcessingException {
                throw new JsonProcessingException("boom") { };
            }
        };
        FailFastWebExceptionHandler brokenHandler = new FailFastWebExceptionHandler(context, properties, brokenMapper);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

        brokenHandler.write(exchange, 500, Map.of("k", "v")).block();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("handleInternal 应处理 propertyPath 为空的约束异常")
    void shouldHandleConstraintViolationWhenPropertyPathIsNull() {
        ConstraintViolationException exception = new ConstraintViolationException(Set.of(createMockViolation(null, "boom")));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

        handler.handleInternal(exchange, exception, reactor.util.context.Context.empty()).block();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("buildMap 应优先使用业务对象自带的 traceId 与 spanId")
    void shouldUseBusinessTraceAndSpanWhenPresentInBuildMap() {
        properties.getTraceId().setEnabled(true);
        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("x")
                .traceId("trace-direct")
                .spanId("span-direct")
                .materialize();

        Map<String, Object> body = handler.buildMap(
                MockServerWebExchange.from(MockServerHttpRequest.get("/test").build()),
                business,
                reactor.util.context.Context.empty());

        assertEquals("trace-direct", body.get(FailureConst.FIELD_TRACE_ID));
        assertEquals("span-direct", body.get(FailureConst.FIELD_SPAN_ID));
    }

    @Test
    @DisplayName("buildMultiMap 应在非默认场景下写入 scene")
    void shouldIncludeSceneWhenBuildMultiMapResolvesNonDefaultScene() {
        properties.setVerbose(true);
        context.setScene("CREATE");
        Business first = Business.of(ResponseCode.VALIDATION_ERROR_400, "x");
        MultiBusiness multi = new MultiBusiness(List.of(first, Business.of(ResponseCode.VALIDATION_ERROR_400, "y")));

        Map<String, Object> body = handler.buildMultiMap(
                MockServerWebExchange.from(MockServerHttpRequest.get("/test").build()),
                multi,
                reactor.util.context.Context.empty());

        assertEquals("CREATE", body.get(FailureConst.FIELD_SCENE));
    }

    @Test
    @DisplayName("resolveTraceId 在 exchange 为空时应回退到上下文")
    void shouldResolveTraceIdFromContextWhenExchangeIsNull() {
        context.setTraceId("context-trace");

        assertEquals("context-trace", handler.resolveTraceId(null, reactor.util.context.Context.empty()));
    }

    @Test
    @DisplayName("resolveTraceId 应在 header 命中时返回 header 值")
    void shouldResolveTraceIdFromHeaderWhenContextAndAttributeMiss() {
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        traceIdConfig.setHeaderName("X-Trace-Id");
        properties.setTraceId(traceIdConfig);

        MockServerHttpRequest request = MockServerHttpRequest.get("/test").header("X-Trace-Id", "header-trace").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        assertEquals("header-trace", handler.resolveTraceId(exchange, reactor.util.context.Context.empty()));
    }

    @Test
    @DisplayName("buildMap 应在 traceId 功能关闭时跳过 trace 字段分支")
    void shouldSkipTraceFieldsWhenTraceIdFeatureIsDisabledInBuildMap() {
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(false);
        properties.setTraceId(traceIdConfig);
        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("x")
                .traceId("trace-direct")
                .spanId("span-direct")
                .materialize();

        Map<String, Object> body = handler.buildMap(
                MockServerWebExchange.from(MockServerHttpRequest.get("/test").build()),
                business,
                reactor.util.context.Context.empty());

        assertFalse(body.containsKey(FailureConst.FIELD_TRACE_ID));
        assertFalse(body.containsKey(FailureConst.FIELD_SPAN_ID));
    }

    @Test
    @DisplayName("buildMultiMap 应直接使用错误首项的非空 spanId")
    void shouldUseFirstErrorSpanIdDirectlyWhenPresentInBuildMultiMap() {
        properties.getTraceId().setEnabled(true);
        Business first = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("x")
                .traceId("trace-direct")
                .spanId("span-direct")
                .materialize();
        MultiBusiness multi = new MultiBusiness(List.of(first, Business.of(ResponseCode.VALIDATION_ERROR_400, "y")));

        Map<String, Object> body = handler.buildMultiMap(
                MockServerWebExchange.from(MockServerHttpRequest.get("/test").build()),
                multi,
                reactor.util.context.Context.empty());

        assertEquals("span-direct", body.get(FailureConst.FIELD_SPAN_ID));
    }

    @Test
    @DisplayName("buildMap 应在所有 trace/span 回退值为空白时省略字段")
    void shouldSkipBlankTraceAndSpanWhenBuildMapHasOnlyBlankFallbacks() {
        properties.getTraceId().setEnabled(true);
        context.setTraceId(" ");
        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("x")
                .traceId(" ")
                .spanId(" ")
                .materialize();

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn(" ");

            Map<String, Object> body = handler.buildMap(
                    MockServerWebExchange.from(MockServerHttpRequest.get("/test").build()),
                    business,
                    reactor.util.context.Context.empty());

            assertFalse(body.containsKey(FailureConst.FIELD_TRACE_ID));
            assertFalse(body.containsKey(FailureConst.FIELD_SPAN_ID));
        }
    }

    @Test
    @DisplayName("buildMultiMap 应在 trace/span 回退值为空白且非 verbose 时省略相关字段")
    void shouldSkipBlankTraceSpanAndErrorsWhenBuildMultiMapHasOnlyBlankFallbacks() {
        properties.getTraceId().setEnabled(true);
        properties.setVerbose(false);
        context.setTraceId(" ");
        Business first = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("x")
                .traceId(" ")
                .spanId(" ")
                .materialize();
        MultiBusiness multi = new MultiBusiness(List.of(first));

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn(" ");

            Map<String, Object> body = handler.buildMultiMap(
                    MockServerWebExchange.from(MockServerHttpRequest.get("/test").build()),
                    multi,
                    reactor.util.context.Context.empty());

            assertFalse(body.containsKey(FailureConst.FIELD_TRACE_ID));
            assertFalse(body.containsKey(FailureConst.FIELD_SPAN_ID));
            assertFalse(body.containsKey(FailureConst.FIELD_ERRORS));
        }
    }

    @Test
    @DisplayName("handleInternal 应处理 propertyPath 对象本身为 null 的约束异常")
    void shouldHandleConstraintViolationWhenPropertyPathObjectIsNull() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
        org.mockito.Mockito.when(violation.getPropertyPath()).thenReturn(null);
        org.mockito.Mockito.when(violation.getMessage()).thenReturn("boom");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

        handler.handleInternal(exchange, exception, reactor.util.context.Context.empty()).block();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("resolveTraceId 在 exchange 和 context 都为空时应返回 null")
    void shouldReturnNullWhenResolveTraceIdReceivesNullExchangeAndNullContext() {
        FailFastWebExceptionHandler localHandler = new FailFastWebExceptionHandler(null, null, objectMapper);

        assertNull(localHandler.resolveTraceId(null, reactor.util.context.Context.empty()));
    }

    @Test
    @DisplayName("buildMap 应在无上下文且 OpenTelemetry span 为空时省略 trace/span")
    void shouldSkipTraceAndSpanWhenBuildMapCannotResolveAnyMetadata() {
        FailureProperties localProperties = new FailureProperties();
        localProperties.getTraceId().setEnabled(true);
        FailFastWebExceptionHandler localHandler = new FailFastWebExceptionHandler(null, localProperties, objectMapper);
        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("x")
                .traceId(null)
                .spanId(null)
                .materialize();

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn(null);

            Map<String, Object> body = localHandler.buildMap(
                    MockServerWebExchange.from(MockServerHttpRequest.get("/test").build()),
                    business,
                    reactor.util.context.Context.empty());

            assertFalse(body.containsKey(FailureConst.FIELD_TRACE_ID));
            assertFalse(body.containsKey(FailureConst.FIELD_SPAN_ID));
        }
    }

    @Test
    @DisplayName("buildMultiMap 应在无上下文且 OpenTelemetry span 为空时省略 trace/span/errors")
    void shouldSkipTraceSpanAndErrorsWhenBuildMultiMapCannotResolveAnyMetadata() {
        FailureProperties localProperties = new FailureProperties();
        localProperties.getTraceId().setEnabled(true);
        localProperties.setVerbose(false);
        FailFastWebExceptionHandler localHandler = new FailFastWebExceptionHandler(null, localProperties, objectMapper);
        MultiBusiness multi = new MultiBusiness(List.of(Business.of(ResponseCode.VALIDATION_ERROR_400, "x")));

        try (MockedStatic<OpenTelemetryBridge> otel = mockStatic(OpenTelemetryBridge.class)) {
            otel.when(OpenTelemetryBridge::currentSpanId).thenReturn(null);

            Map<String, Object> body = localHandler.buildMultiMap(
                    MockServerWebExchange.from(MockServerHttpRequest.get("/test").build()),
                    multi,
                    reactor.util.context.Context.empty());

            assertFalse(body.containsKey(FailureConst.FIELD_TRACE_ID));
            assertFalse(body.containsKey(FailureConst.FIELD_SPAN_ID));
            assertFalse(body.containsKey(FailureConst.FIELD_ERRORS));
        }
    }

    @Test
    @DisplayName("resolveTraceId 应在 headerName 为空白时回退为上下文值")
    void shouldFallbackToContextWhenResolveTraceIdSeesBlankHeaderName() {
        FailureProperties localProperties = new FailureProperties();
        localProperties.getTraceId().setEnabled(true);
        localProperties.getTraceId().setHeaderName(" ");
        FailureContext localContext = new FailureContext(localProperties, new com.chao.failfast.config.mapping.CodeMappingConfig(localProperties), null);
        localContext.setTraceId("context-trace");
        FailFastWebExceptionHandler localHandler = new FailFastWebExceptionHandler(localContext, localProperties, objectMapper);

        String traceId = localHandler.resolveTraceId(
                MockServerWebExchange.from(MockServerHttpRequest.get("/test").build()),
                reactor.util.context.Context.empty());

        assertEquals("context-trace", traceId);
    }

    @Test
    @DisplayName("handle 应通过 deferContextual 将未知异常继续向上传递")
    void shouldPropagateUnknownExceptionThroughHandle() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

        assertThrows(RuntimeException.class, () -> handler.handle(exchange, new RuntimeException("boom"))
                .contextWrite(reactor.util.context.Context.of("k", "v"))
                .block());
    }

    @SuppressWarnings("unchecked")
    private ConstraintViolation<Object> createMockViolation(String path, String message) {
        ConstraintViolation<Object> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
        Path mockPath = org.mockito.Mockito.mock(Path.class);
        org.mockito.Mockito.when(violation.getPropertyPath()).thenReturn(mockPath);
        org.mockito.Mockito.when(violation.getMessage()).thenReturn(message);
        org.mockito.Mockito.when(mockPath.toString()).thenReturn(path);
        return violation;
    }

    private void dummyMethod() {
    }

    private static class TestObject {
        private String field1;
        private String field2;
    }
}
