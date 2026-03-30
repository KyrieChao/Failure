package com.chao.failfast.integration.webflux;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.exception.Business;
import com.chao.failfast.exception.MultiBusiness;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FailFastWebExceptionHandlerTest {

    @Test
    void testGetOrder() {
        // 准备
        FailureContext context = Mockito.mock(FailureContext.class);
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        ObjectMapper objectMapper = new ObjectMapper();
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, properties, objectMapper);
        
        // 执行
        int order = handler.getOrder();
        
        // 验证
        assertEquals(org.springframework.core.Ordered.HIGHEST_PRECEDENCE, order);
    }

    @Test
    void testHandle() {
        // 准备
        FailureContext context = Mockito.mock(FailureContext.class);
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        ObjectMapper objectMapper = new ObjectMapper();
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, properties, objectMapper);
        
        ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
        Business businessException = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        
        // 模拟
        org.springframework.http.server.reactive.ServerHttpResponse response = Mockito.mock(org.springframework.http.server.reactive.ServerHttpResponse.class);
        when(exchange.getResponse()).thenReturn(response);
        when(response.isCommitted()).thenReturn(false);
        
        // 执行
        Mono<Void> result = handler.handle(exchange, businessException);
        
        // 验证
        assertNotNull(result);
    }

    @Test
    void testHandleWithBusinessException() {
        // 准备
        FailureContext context = Mockito.mock(FailureContext.class);
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        ObjectMapper objectMapper = new ObjectMapper();
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, properties, objectMapper);
        
        ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
        Business businessException = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        
        // 模拟
        org.springframework.http.server.reactive.ServerHttpResponse response = Mockito.mock(org.springframework.http.server.reactive.ServerHttpResponse.class);
        when(exchange.getResponse()).thenReturn(response);
        when(response.isCommitted()).thenReturn(false);
        when(response.bufferFactory()).thenReturn(mock(org.springframework.core.io.buffer.DataBufferFactory.class));
        when(response.writeWith(any())).thenReturn(Mono.empty());
        
        // 执行
        Mono<Void> result = handler.handle(exchange, businessException);
        
        // 验证
        assertNotNull(result);
    }

    @Test
    void testHandleWithMultiBusinessException() {
        // 准备
        FailureContext context = Mockito.mock(FailureContext.class);
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        ObjectMapper objectMapper = new ObjectMapper();
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, properties, objectMapper);
        
        ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
        List<Business> errors = Collections.singletonList(Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error"));
        MultiBusiness multiBusinessException = new MultiBusiness(errors);
        
        // 模拟
        org.springframework.http.server.reactive.ServerHttpResponse response = Mockito.mock(org.springframework.http.server.reactive.ServerHttpResponse.class);
        when(exchange.getResponse()).thenReturn(response);
        when(response.isCommitted()).thenReturn(false);
        when(response.bufferFactory()).thenReturn(mock(org.springframework.core.io.buffer.DataBufferFactory.class));
        when(response.writeWith(any())).thenReturn(Mono.empty());
        
        // 执行
        Mono<Void> result = handler.handle(exchange, multiBusinessException);
        
        // 验证
        assertNotNull(result);
    }

    @Test
    void testHandleWithWebExchangeBindException() {
        // 准备
        FailureContext context = Mockito.mock(FailureContext.class);
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        ObjectMapper objectMapper = new ObjectMapper();
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, properties, objectMapper);
        
        ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
        WebExchangeBindException webExchangeBindException = Mockito.mock(WebExchangeBindException.class);
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError = Mockito.mock(FieldError.class);
        
        // 模拟
        org.springframework.http.server.reactive.ServerHttpResponse response = Mockito.mock(org.springframework.http.server.reactive.ServerHttpResponse.class);
        when(exchange.getResponse()).thenReturn(response);
        when(response.isCommitted()).thenReturn(false);
        when(response.bufferFactory()).thenReturn(mock(org.springframework.core.io.buffer.DataBufferFactory.class));
        when(response.writeWith(any())).thenReturn(Mono.empty());
        when(webExchangeBindException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
        when(fieldError.getField()).thenReturn("testField");
        when(fieldError.getDefaultMessage()).thenReturn("Test error message");
        
        // 执行
        Mono<Void> result = handler.handle(exchange, webExchangeBindException);
        
        // 验证
        assertNotNull(result);
    }

    @Test
    void testHandleWithConstraintViolationException() {
        // 准备
        FailureContext context = Mockito.mock(FailureContext.class);
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        ObjectMapper objectMapper = new ObjectMapper();
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, properties, objectMapper);
        
        ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
        ConstraintViolationException constraintViolationException = Mockito.mock(ConstraintViolationException.class);
        ConstraintViolation<?> constraintViolation = Mockito.mock(ConstraintViolation.class);
        
        // 模拟
        org.springframework.http.server.reactive.ServerHttpResponse response = Mockito.mock(org.springframework.http.server.reactive.ServerHttpResponse.class);
        when(exchange.getResponse()).thenReturn(response);
        when(response.isCommitted()).thenReturn(false);
        when(response.bufferFactory()).thenReturn(mock(org.springframework.core.io.buffer.DataBufferFactory.class));
        when(response.writeWith(any())).thenReturn(Mono.empty());
        when(constraintViolationException.getConstraintViolations()).thenReturn(Collections.singleton(constraintViolation));
        when(constraintViolation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(constraintViolation.getMessage()).thenReturn("Test constraint violation");
        
        // 执行
        Mono<Void> result = handler.handle(exchange, constraintViolationException);
        
        // 验证
        assertNotNull(result);
    }

    @Test
    void testHandleWithUnknownException() {
        // 准备
        FailureContext context = Mockito.mock(FailureContext.class);
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        ObjectMapper objectMapper = new ObjectMapper();
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, properties, objectMapper);
        
        ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
        RuntimeException unknownException = new RuntimeException("Unknown error");
        
        // 模拟
        org.springframework.http.server.reactive.ServerHttpResponse response = Mockito.mock(org.springframework.http.server.reactive.ServerHttpResponse.class);
        when(exchange.getResponse()).thenReturn(response);
        when(response.isCommitted()).thenReturn(false);
        
        // 执行
        Mono<Void> result = handler.handle(exchange, unknownException);
        
        // 验证
        assertNotNull(result);
    }

    @Test
    void testHandleWithCommittedResponse() {
        // 准备
        FailureContext context = Mockito.mock(FailureContext.class);
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        ObjectMapper objectMapper = new ObjectMapper();
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, properties, objectMapper);
        
        ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
        Business businessException = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        
        // 模拟
        org.springframework.http.server.reactive.ServerHttpResponse response = Mockito.mock(org.springframework.http.server.reactive.ServerHttpResponse.class);
        when(exchange.getResponse()).thenReturn(response);
        when(response.isCommitted()).thenReturn(true);
        
        // 执行
        Mono<Void> result = handler.handle(exchange, businessException);
        
        // 验证
        assertNotNull(result);
    }
}
