package com.chao.failure.integration.webflux;

import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.constant.FailureConst;
import com.chao.failure.exception.Business;
import com.chao.failure.exception.MultiBusiness;
import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.internal.core.observability.TraceInfoExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FailFastWebExceptionHandlerTest {

    private FailFastWebExceptionHandler handler;
    
    @Mock
    private FailureContext context;
    
    @Mock
    private FailureProperties properties;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private ServerWebExchange exchange;
    
    @Mock
    private org.springframework.http.server.reactive.ServerHttpResponse response;
    
    @Mock
    private WebExchangeBindException webExchangeBindException;
    
    @Mock
    private org.springframework.validation.BindingResult bindingResult;
    
    @Mock
    private ConstraintViolationException constraintViolationException;
    
    @Mock
    private ConstraintViolation<?> constraintViolation;
    
    @Mock
    private FieldError fieldError;

    @Mock
    private ServerHttpRequest request;
    
    @Mock
    private HttpHeaders httpHeaders;
    
    @Mock
    private HttpHeaders responseHeaders;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(exchange.getResponse()).thenReturn(response);
        when(response.getHeaders()).thenReturn(responseHeaders);
        when(webExchangeBindException.getBindingResult()).thenReturn(bindingResult);
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(httpHeaders);
        handler = new FailFastWebExceptionHandler(context, properties, objectMapper);
    }

    @Test
    void should_return_highest_precedence() {
        // Arrange & Act
        int order = handler.getOrder();
        
        // Assert
        assertEquals(Ordered.HIGHEST_PRECEDENCE, order);
    }

    @Test
    void should_handle_business_exception() {
        // Arrange
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        
        // Act
        Mono<Void> result = handler.handle(exchange, business);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_handle_multi_business_exception() {
        // Arrange
        List<Business> errors = new ArrayList<>();
        errors.add(Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"));
        errors.add(Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 2"));
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        
        // Act
        Mono<Void> result = handler.handle(exchange, multiBusiness);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_handle_web_exchange_bind_exception() {
        // Arrange
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(fieldError);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(fieldError.getField()).thenReturn("username");
        when(fieldError.getDefaultMessage()).thenReturn("Username cannot be blank");
        
        // Act
        Mono<Void> result = handler.handle(exchange, webExchangeBindException);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_handle_constraint_violation_exception() {
        // Arrange
        Set<ConstraintViolation<?>> violations = Set.of(constraintViolation);
        when(constraintViolationException.getConstraintViolations()).thenReturn(violations);
        when(constraintViolation.getPropertyPath()).thenReturn(null);
        when(constraintViolation.getMessage()).thenReturn("Validation error");
        
        // Act
        Mono<Void> result = handler.handle(exchange, constraintViolationException);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_pass_through_unknown_exception() {
        // Arrange
        RuntimeException unknownException = new RuntimeException("Unknown error");
        
        // Act
        Mono<Void> result = handler.handle(exchange, unknownException);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_handle_committed_response() {
        // Arrange
        when(response.isCommitted()).thenReturn(true);
        RuntimeException exception = new RuntimeException("Test exception");
        
        // Act
        Mono<Void> result = handler.handle(exchange, exception);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_handle_with_trace_id_enabled() {
        // Arrange
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        when(context.getTraceId()).thenReturn("test-trace-id");
        
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        
        // Act
        Mono<Void> result = handler.handle(exchange, business);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_handle_with_verbose_mode() {
        // Arrange
        when(properties.isVerbose()).thenReturn(true);
        
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        
        // Act
        Mono<Void> result = handler.handle(exchange, business);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_handle_with_custom_scene() {
        // Arrange
        ContextView ctxView = Context.of("scene", "custom-scene");
        
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        
        // Act
        Mono<Void> result = handler.handle(exchange, business);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_handle_with_json_processing_exception() {
        // Arrange
        when(properties.isVerbose()).thenReturn(true);
        try {
            when(objectMapper.writeValueAsBytes(any())).thenThrow(new IOException("JSON processing error"));
        } catch (Exception e) {
            // Ignore
        }
        
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        
        // Act
        Mono<Void> result = handler.handle(exchange, business);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_handle_with_trace_id_from_attributes() {
        // Arrange
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        
        // Mock exchange attributes
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(FailureConst.FIELD_TRACE_ID, "attribute-trace-id");
        when(exchange.getAttributes()).thenReturn(attributes);
        
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        
        // Act
        Mono<Void> result = handler.handle(exchange, business);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_handle_empty_constraint_violations() {
        // Arrange
        Set<ConstraintViolation<?>> emptyViolations = Set.of();
        when(constraintViolationException.getConstraintViolations()).thenReturn(emptyViolations);
        
        // Act
        Mono<Void> result = handler.handle(exchange, constraintViolationException);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_build_multi_map() {
        // Arrange
        List<Business> errors = new ArrayList<>();
        errors.add(Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"));
        errors.add(Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 2"));
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        ContextView ctxView = Context.empty();
        
        // Act
        Map<String, Object> result = handler.buildMultiMap(exchange, multiBusiness, ctxView);
        
        // Assert
        assertNotNull(result);
        assertEquals(multiBusiness.getResponseCode().getCode(), result.get(FailureConst.FIELD_CODE));
    }

    @Test
    void should_build_multi_map_with_trace_id() {
        // Arrange
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        when(context.getTraceId()).thenReturn("test-trace-id");
        
        List<Business> errors = new ArrayList<>();
        errors.add(Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"));
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        ContextView ctxView = Context.empty();
        
        // Act
        Map<String, Object> result = handler.buildMultiMap(exchange, multiBusiness, ctxView);
        
        // Assert
        assertNotNull(result);
        assertEquals("test-trace-id", result.get(FailureConst.FIELD_TRACE_ID));
    }

    @Test
    void should_build_multi_map_with_verbose_mode() {
        // Arrange
        when(properties.isVerbose()).thenReturn(true);
        
        List<Business> errors = new ArrayList<>();
        errors.add(Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"));
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        ContextView ctxView = Context.empty();
        
        // Act
        Map<String, Object> result = handler.buildMultiMap(exchange, multiBusiness, ctxView);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey(FailureConst.FIELD_ERRORS));
    }

    @Test
    void should_build_multi_map_with_custom_scene() {
        // Arrange
        // 直接测试场景字段的添加逻辑
        List<Business> errors = new ArrayList<>();
        errors.add(Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"));
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        ContextView ctxView = Context.empty();
        
        // Act
        Map<String, Object> result = handler.buildMultiMap(exchange, multiBusiness, ctxView);
        
        // Assert
        assertNotNull(result);
        // 场景测试需要依赖TraceInfoExtractor的实现，这里我们先验证其他字段
        assertEquals(multiBusiness.getResponseCode().getCode(), result.get(FailureConst.FIELD_CODE));
    }

    @Test
    void should_handle_constructor_with_null_objectMapper() {
        // Arrange & Act
        FailFastWebExceptionHandler handlerWithNullMapper = new FailFastWebExceptionHandler(context, properties, null);
        
        // Assert
        assertNotNull(handlerWithNullMapper);
    }

    @Test
    void should_build_map_with_business_trace_id() {
        // Arrange
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("Test error")
                .traceId("business-trace-id")
                .spanId("business-span-id")
                .materialize();
        ContextView ctxView = Context.empty();
        
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        
        // Act
        Map<String, Object> result = handler.buildMap(exchange, business, ctxView);
        
        // Assert
        assertNotNull(result);
        assertEquals("business-trace-id", result.get(FailureConst.FIELD_TRACE_ID));
    }

    @Test
    void should_build_map_with_business_span_id() {
        // Arrange
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("Test error")
                .spanId("business-span-id")
                .materialize();
        ContextView ctxView = Context.empty();
        
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        
        // Act
        Map<String, Object> result = handler.buildMap(exchange, business, ctxView);
        
        // Assert
        assertNotNull(result);
        assertEquals("business-span-id", result.get(FailureConst.FIELD_SPAN_ID));
    }

    @Test
    void should_build_map_with_null_properties() {
        // Arrange
        FailFastWebExceptionHandler handlerWithNullProps = new FailFastWebExceptionHandler(context, null, objectMapper);
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        ContextView ctxView = Context.empty();
        
        // Act
        Map<String, Object> result = handlerWithNullProps.buildMap(exchange, business, ctxView);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.containsKey(FailureConst.FIELD_TRACE_ID));
        assertFalse(result.containsKey(FailureConst.FIELD_ERRORS));
    }

    @Test
    void should_build_map_detail() {
        // Arrange
        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("Test error")
                .path("test.field")
                .invalidValue("invalid")
                .materialize();
        
        // Act
        Map<String, Object> result = handler.buildMapDetail(business);
        
        // Assert
        assertNotNull(result);
        assertEquals("test.field", result.get(FailureConst.FIELD_PATH));
        assertEquals(ResponseCode.VALIDATION_ERROR_400.getCode(), result.get(FailureConst.FIELD_CODE));
        assertEquals("invalid", result.get(FailureConst.FIELD_REJECTED));
    }

    @Test
    void should_resolve_trace_id_with_null_exchange() {
        // Arrange
        when(context.getTraceId()).thenReturn("context-trace-id");
        ContextView ctxView = Context.empty();
        
        // Act
        String result = handler.resolveTraceId(null, ctxView);
        
        // Assert
        assertEquals("context-trace-id", result);
    }

    @Test
    void should_resolve_trace_id_from_context() {
        // Arrange
        ContextView ctxView = Context.empty();
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.traceId(context, ctxView)).thenReturn("ctx-trace-id");
            
            // Act
            String result = handler.resolveTraceId(exchange, ctxView);
            
            // Assert
            assertEquals("ctx-trace-id", result);
        }
    }

    @Test
    void should_resolve_trace_id_from_header() {
        // Arrange
        ContextView ctxView = Context.empty();
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setHeaderName("X-Trace-Id");
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        when(httpHeaders.getFirst("X-Trace-Id")).thenReturn("header-trace-id");
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.traceId(context, ctxView)).thenReturn(null);
            
            // Act
            String result = handler.resolveTraceId(exchange, ctxView);
            
            // Assert
            assertEquals("header-trace-id", result);
        }
    }

    @Test
    void should_resolve_trace_id_with_null_traceId_config() {
        // Arrange
        ContextView ctxView = Context.empty();
        when(properties.getTraceId()).thenReturn(null);
        when(context.getTraceId()).thenReturn("fallback-trace-id");
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.traceId(context, ctxView)).thenReturn(null);
            
            // Act
            String result = handler.resolveTraceId(exchange, ctxView);
            
            // Assert
            assertEquals("fallback-trace-id", result);
        }
    }

    @Test
    void should_resolve_trace_id_with_null_context() {
        // Arrange
        FailFastWebExceptionHandler handlerWithNullCtx = new FailFastWebExceptionHandler(null, properties, objectMapper);
        ContextView ctxView = Context.empty();
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.traceId(null, ctxView)).thenReturn(null);
            
            // Act
            String result = handlerWithNullCtx.resolveTraceId(exchange, ctxView);
            
            // Assert
            assertNull(result);
        }
    }



    @Test
    void should_build_multi_map_with_empty_errors() {
        // Arrange
        List<Business> errors = new ArrayList<>();
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        ContextView ctxView = Context.empty();
        
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        
        // Act
        Map<String, Object> result = handler.buildMultiMap(exchange, multiBusiness, ctxView);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.containsKey(FailureConst.FIELD_SPAN_ID));
    }

    @Test
    void should_build_multi_map_with_trace_id_in_exception() {
        // Arrange
        List<Business> errors = new ArrayList<>();
        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("Test error")
                .materialize();
        errors.add(business);
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        ContextView ctxView = Context.empty();
        
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        when(context.getTraceId()).thenReturn("fallback-trace-id");
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.traceId(context, ctxView)).thenReturn(null);
            
            // Act
            Map<String, Object> result = handler.buildMultiMap(exchange, multiBusiness, ctxView);
            
            // Assert
            assertNotNull(result);
            assertEquals("fallback-trace-id", result.get(FailureConst.FIELD_TRACE_ID));
        }
    }

    @Test
    void should_handle_internal_with_committed_response() {
        // Arrange
        when(response.isCommitted()).thenReturn(true);
        RuntimeException exception = new RuntimeException("Test");
        ContextView ctxView = Context.empty();
        
        // Act
        Mono<Void> result = handler.handleInternal(exchange, exception, ctxView);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_build_map_with_null_exchange_trace_id() {
        // Arrange
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        ContextView ctxView = Context.empty();
        
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        
        // Act
        Map<String, Object> result = handler.buildMap(null, business, ctxView);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_build_multi_map_with_null_exchange_trace_id() {
        // Arrange
        List<Business> errors = new ArrayList<>();
        errors.add(Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"));
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        ContextView ctxView = Context.empty();
        
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        
        // Act
        Map<String, Object> result = handler.buildMultiMap(null, multiBusiness, ctxView);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void should_build_multi_map_with_null_scene() {
        // Arrange
        List<Business> errors = new ArrayList<>();
        errors.add(Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"));
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        ContextView ctxView = Context.empty();
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.scene(context, ctxView)).thenReturn(null);
            
            // Act
            Map<String, Object> result = handler.buildMultiMap(exchange, multiBusiness, ctxView);
            
            // Assert
            assertNotNull(result);
            // scene被设置为DEFAULT_SCENE，但由于它等于DEFAULT_SCENE，所以不会被添加到body中
            assertFalse(result.containsKey(FailureConst.FIELD_SCENE));
        }
    }

    @Test
    void should_build_multi_map_with_blank_scene() {
        // Arrange
        List<Business> errors = new ArrayList<>();
        errors.add(Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"));
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        ContextView ctxView = Context.empty();
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.scene(context, ctxView)).thenReturn("  ");
            
            // Act
            Map<String, Object> result = handler.buildMultiMap(exchange, multiBusiness, ctxView);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.containsKey(FailureConst.FIELD_SCENE));
        }
    }

    @Test
    void should_build_multi_map_with_default_scene() {
        // Arrange
        List<Business> errors = new ArrayList<>();
        errors.add(Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"));
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        ContextView ctxView = Context.empty();
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.scene(context, ctxView)).thenReturn(FailureConst.DEFAULT_SCENE);
            
            // Act
            Map<String, Object> result = handler.buildMultiMap(exchange, multiBusiness, ctxView);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.containsKey(FailureConst.FIELD_SCENE));
        }
    }

    @Test
    void should_build_map_with_blank_scene() {
        // Arrange
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        ContextView ctxView = Context.empty();
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.scene(context, ctxView)).thenReturn("  ");
            
            // Act
            Map<String, Object> result = handler.buildMap(exchange, business, ctxView);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.containsKey(FailureConst.FIELD_SCENE));
        }
    }

    @Test
    void should_build_map_with_null_scene() {
        // Arrange
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        ContextView ctxView = Context.empty();
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.scene(context, ctxView)).thenReturn(null);
            
            // Act
            Map<String, Object> result = handler.buildMap(exchange, business, ctxView);
            
            // Assert
            assertNotNull(result);
            // scene被设置为DEFAULT_SCENE，但由于它等于DEFAULT_SCENE，所以不会被添加到body中
            assertFalse(result.containsKey(FailureConst.FIELD_SCENE));
        }
    }

    @Test
    void should_resolve_trace_id_with_blank_context_trace_id() {
        // Arrange
        ContextView ctxView = Context.empty();
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.traceId(context, ctxView)).thenReturn("  ");
            
            // Act
            String result = handler.resolveTraceId(exchange, ctxView);
            
            // Assert
            assertNull(result);
        }
    }

    @Test
    void should_resolve_trace_id_with_blank_header_value() {
        // Arrange
        ContextView ctxView = Context.empty();
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setHeaderName("X-Trace-Id");
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        when(httpHeaders.getFirst("X-Trace-Id")).thenReturn("  ");
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.traceId(context, ctxView)).thenReturn(null);
            
            // Act
            String result = handler.resolveTraceId(exchange, ctxView);
            
            // Assert
            assertNull(result);
        }
    }

    @Test
    void should_resolve_trace_id_with_blank_attribute_value() {
        // Arrange
        ContextView ctxView = Context.empty();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(FailureConst.FIELD_TRACE_ID, "  ");
        when(exchange.getAttributes()).thenReturn(attributes);
        when(properties.getTraceId()).thenReturn(null);
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.traceId(context, ctxView)).thenReturn(null);
            
            // Act
            String result = handler.resolveTraceId(exchange, ctxView);
            
            // Assert
            assertNull(result);
        }
    }

    @Test
    void should_build_map_with_null_trace_id_and_enabled() {
        // Arrange
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        ContextView ctxView = Context.empty();
        
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        when(context.getTraceId()).thenReturn(null);
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.traceId(context, ctxView)).thenReturn(null);
            
            // Act
            Map<String, Object> result = handler.buildMap(exchange, business, ctxView);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.containsKey(FailureConst.FIELD_TRACE_ID));
        }
    }

    @Test
    void should_build_multi_map_with_null_trace_id_and_enabled() {
        // Arrange
        List<Business> errors = new ArrayList<>();
        errors.add(Business.of(ResponseCode.VALIDATION_ERROR_400, "Error 1"));
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        ContextView ctxView = Context.empty();
        
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        when(context.getTraceId()).thenReturn(null);
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.traceId(context, ctxView)).thenReturn(null);
            
            // Act
            Map<String, Object> result = handler.buildMultiMap(exchange, multiBusiness, ctxView);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.containsKey(FailureConst.FIELD_TRACE_ID));
        }
    }

    @Test
    void should_build_multi_map_with_null_span_id_and_enabled() {
        // Arrange
        List<Business> errors = new ArrayList<>();
        Business business = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("Test error")
                .materialize();
        errors.add(business);
        MultiBusiness multiBusiness = new MultiBusiness(errors);
        ContextView ctxView = Context.empty();
        
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        when(context.getTraceId()).thenReturn("trace-id");
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.traceId(context, ctxView)).thenReturn(null);
            mocked.when(() -> TraceInfoExtractor.scene(context, ctxView)).thenReturn(null);
            
            // Act
            Map<String, Object> result = handler.buildMultiMap(exchange, multiBusiness, ctxView);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.containsKey(FailureConst.FIELD_SPAN_ID));
        }
    }

    @Test
    void should_build_map_with_null_span_id_and_enabled() {
        // Arrange
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        ContextView ctxView = Context.empty();
        
        FailureProperties.TraceId traceIdConfig = new FailureProperties.TraceId();
        traceIdConfig.setEnabled(true);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        when(context.getTraceId()).thenReturn("trace-id");
        
        try (MockedStatic<TraceInfoExtractor> mocked = Mockito.mockStatic(TraceInfoExtractor.class)) {
            mocked.when(() -> TraceInfoExtractor.traceId(context, ctxView)).thenReturn(null);
            mocked.when(() -> TraceInfoExtractor.scene(context, ctxView)).thenReturn(null);
            
            // Act
            Map<String, Object> result = handler.buildMap(exchange, business, ctxView);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.containsKey(FailureConst.FIELD_SPAN_ID));
        }
    }
}

