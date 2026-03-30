package com.chao.failfast.integration.webflux;

import com.chao.failfast.config.mapping.CodeMappingConfig;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.exception.Business;
import com.chao.failfast.exception.MultiBusiness;
import com.chao.failfast.internal.core.Ex;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FailFastWebExceptionHandlerCoverageTest {

    @AfterEach
    void tearDown() {
        Ex.setContext(null);
    }

    @Test
    void handleCommittedResponseReturnsError() {
        FailureProperties props = new FailureProperties();
        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(ctx);
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(ctx, props, new ObjectMapper());

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        org.springframework.http.server.reactive.ServerHttpResponse response = mock(org.springframework.http.server.reactive.ServerHttpResponse.class);
        when(exchange.getResponse()).thenReturn(response);
        when(response.isCommitted()).thenReturn(true);

        RuntimeException ex = new RuntimeException("x");
        assertThrows(RuntimeException.class, () -> handler.handle(exchange, ex).block());
    }

    @Test
    void handleBusinessCoversTraceIdSceneVerboseAndWrite() {
        FailureProperties props = new FailureProperties();
        props.setVerbose(true);
        props.getTraceId().setEnabled(true);
        props.getTraceId().setHeaderName("X-Trace-Id");

        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(context);
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t")
                .header("X-Trace-Id", "hdr")
                .build());
        exchange.getAttributes().put(FailureConst.FIELD_TRACE_ID, "attr");

        Business ex = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        handler.handle(exchange, ex)
                .contextWrite(Context.of(
                        ReactiveTrace.TRACE_ID_KEY, "ctx",
                        ReactiveTrace.SCENE_KEY, "CREATE"
                ))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains(FailureConst.FIELD_TRACE_ID);
        assertThat(body).contains("ctx");
        assertThat(body).contains(FailureConst.FIELD_SCENE);
        assertThat(body).contains("CREATE");
        assertThat(exchange.getResponse().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).contains("application/json");
    }

    @Test
    void handleMultiBusinessCoversMultiMapAndVerboseList() {
        FailureProperties props = new FailureProperties();
        props.setVerbose(true);
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(context);
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        MultiBusiness ex = new MultiBusiness(List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "a"),
                Business.of(ResponseCode.VALIDATION_ERROR_400, "b")
        ));

        handler.handle(exchange, ex).contextWrite(Context.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains(FailureConst.FIELD_ERRORS);
    }

    @Test
    void handleWebExchangeBindExceptionCoversFieldErrorMapping() {
        FailureProperties props = new FailureProperties();
        props.setVerbose(true);
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(context);
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());

        Object target = new Object();
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(target, "t");
        br.addError(new FieldError("t", "f1", "m1"));
        WebExchangeBindException ex = new WebExchangeBindException(null, br);

        handler.handle(exchange, ex).contextWrite(Context.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains("f1");
        assertThat(body).contains("m1");
    }

    @Test
    void handleConstraintViolationExceptionCoversViolationMapping() {
        FailureProperties props = new FailureProperties();
        props.setVerbose(true);
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(context);
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> v = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("p1");
        when(v.getPropertyPath()).thenReturn(path);
        when(v.getMessage()).thenReturn("m1");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(v));

        handler.handle(exchange, ex).contextWrite(Context.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains("p1");
        assertThat(body).contains("m1");
    }

    @Test
    void handleMultiBusinessUsesMultiBranchAndBuildMultiMap() {
        FailureProperties props = new FailureProperties();
        props.setVerbose(true);
        props.getTraceId().setEnabled(true);
        props.getTraceId().setHeaderName("X-Trace-Id");

        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        context.setTraceId("tid");
        context.setScene(null);
        Ex.setContext(context);

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, null);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t")
                .header("X-Trace-Id", "")
                .build());
        exchange.getAttributes().put(FailureConst.FIELD_TRACE_ID, "");

        MultiBusiness ex = new MultiBusiness(List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "a"),
                Business.of(ResponseCode.VALIDATION_ERROR_400, "b")
        ));

        handler.handle(exchange, ex).contextWrite(Context.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains(FailureConst.FIELD_ERRORS);
        assertThat(body).contains(FailureConst.FIELD_TRACE_ID);
        assertThat(body).contains("tid");
    }

    @Test
    void handleWebExchangeBindExceptionWithTwoErrorsTriggersHandleErrorsMultiPath() {
        FailureProperties props = new FailureProperties();
        props.setVerbose(true);
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(context);
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());

        Object target = new Object();
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(target, "t");
        br.addError(new FieldError("t", "f1", "m1"));
        br.addError(new FieldError("t", "f2", "m2"));
        WebExchangeBindException ex = new WebExchangeBindException(null, br);

        handler.handle(exchange, ex).contextWrite(Context.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains(FailureConst.FIELD_ERRORS);
    }

    @Test
    void handleConstraintViolationExceptionWithTwoViolationsTriggersHandleErrorsMultiPath() {
        FailureProperties props = new FailureProperties();
        props.setVerbose(true);
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(context);
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> v1 = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        jakarta.validation.Path p1 = mock(jakarta.validation.Path.class);
        when(p1.toString()).thenReturn("p1");
        when(v1.getPropertyPath()).thenReturn(p1);
        when(v1.getMessage()).thenReturn("m1");

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> v2 = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(v2.getPropertyPath()).thenReturn(null);
        when(v2.getMessage()).thenReturn("m2");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(v1, v2));
        handler.handle(exchange, ex).contextWrite(Context.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void resolveTraceIdReturnsContextValueWhenExchangeNull() throws Exception {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setEnabled(true);
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        context.setTraceId("tid");
        Ex.setContext(context);

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());
        java.lang.reflect.Method m = FailFastWebExceptionHandler.class.getDeclaredMethod("resolveTraceId", ServerWebExchange.class, ContextView.class);
        m.setAccessible(true);
        Object out = m.invoke(handler, null, Context.empty());
        assertThat(out).isEqualTo("tid");
    }

    @Test
    void resolveTraceIdFallsBackToContextAtEndWhenAllSourcesBlank() throws Exception {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setEnabled(true);
        props.getTraceId().setHeaderName("X-Trace-Id");
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        context.setTraceId(" ");
        Ex.setContext(context);

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());
        java.lang.reflect.Method m = FailFastWebExceptionHandler.class.getDeclaredMethod("resolveTraceId", ServerWebExchange.class, ContextView.class);
        m.setAccessible(true);

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        Object out = m.invoke(handler, exchange, Context.empty());
        assertThat(out).isEqualTo(" ");
    }

    @Test
    void handleMultiBusinessAddsSceneWhenNotDefault() {
        FailureProperties props = new FailureProperties();
        props.setVerbose(true);
        props.getTraceId().setEnabled(false);
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(context);

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, null);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());

        MultiBusiness ex = new MultiBusiness(List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "a"),
                Business.of(ResponseCode.VALIDATION_ERROR_400, "b")
        ));

        handler.handle(exchange, ex).contextWrite(Context.of(ReactiveTrace.SCENE_KEY, "CREATE")).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains(FailureConst.FIELD_SCENE);
        assertThat(body).contains("CREATE");
        assertThat(body).doesNotContain(FailureConst.FIELD_TRACE_ID);
    }

    @Test
    void handleBindExceptionWithNoErrorsFallsBackToDefaultValidationError() {
        FailureProperties props = new FailureProperties();
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(context);
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());

        Object target = new Object();
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(target, "t");
        WebExchangeBindException ex = new WebExchangeBindException(null, br);

        handler.handle(exchange, ex).contextWrite(Context.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains("未知校验错误");
    }

    @Test
    void writeJsonProcessingExceptionFallsBackToSimpleJsonBytes() throws Exception {
        FailureProperties props = new FailureProperties();
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(context);

        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsBytes(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new JsonProcessingException("x") {
                });

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, mapper);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());

        Business ex = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        handler.handle(exchange, ex).contextWrite(Context.empty()).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains("\"code\":500");
    }

    @Test
    void handleUnknownExceptionReturnsError() {
        FailureProperties props = new FailureProperties();
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(context);
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        RuntimeException ex = new RuntimeException("x");

        assertThrows(RuntimeException.class, () -> handler.handle(exchange, ex).block());
    }

    @Test
    void resolveTraceIdReturnsNullWhenExchangeNullAndContextNull() throws Exception {
        FailureProperties props = new FailureProperties();
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(null, props, new ObjectMapper());

        java.lang.reflect.Method m = FailFastWebExceptionHandler.class.getDeclaredMethod("resolveTraceId", ServerWebExchange.class, ContextView.class);
        m.setAccessible(true);
        Object out = m.invoke(handler, null, Context.empty());
        assertThat(out).isNull();
    }

    @Test
    void resolveTraceIdUsesAttributeWhenContextBlank() throws Exception {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setEnabled(true);
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        context.setTraceId(" ");
        Ex.setContext(context);
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());

        java.lang.reflect.Method m = FailFastWebExceptionHandler.class.getDeclaredMethod("resolveTraceId", ServerWebExchange.class, ContextView.class);
        m.setAccessible(true);

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        exchange.getAttributes().put(FailureConst.FIELD_TRACE_ID, "attr");

        Object out = m.invoke(handler, exchange, Context.empty());
        assertThat(out).isEqualTo("attr");
    }

    @Test
    void resolveTraceIdUsesHeaderWhenAttributeBlank() throws Exception {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setEnabled(true);
        props.getTraceId().setHeaderName("X-Trace-Id");
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        context.setTraceId(" ");
        Ex.setContext(context);
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());

        java.lang.reflect.Method m = FailFastWebExceptionHandler.class.getDeclaredMethod("resolveTraceId", ServerWebExchange.class, ContextView.class);
        m.setAccessible(true);

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t")
                .header("X-Trace-Id", "hdr")
                .build());
        exchange.getAttributes().put(FailureConst.FIELD_TRACE_ID, "");

        Object out = m.invoke(handler, exchange, Context.empty());
        assertThat(out).isEqualTo("hdr");
    }

    @Test
    void resolveTraceIdSkipsHeaderWhenHeaderNameBlankAndPropertiesNull() throws Exception {
        FailureProperties props = new FailureProperties();
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        context.setTraceId(" ");
        Ex.setContext(context);

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, null, new ObjectMapper());
        java.lang.reflect.Method m = FailFastWebExceptionHandler.class.getDeclaredMethod("resolveTraceId", ServerWebExchange.class, ContextView.class);
        m.setAccessible(true);

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        Object out = m.invoke(handler, exchange, Context.empty());
        assertThat(out).isEqualTo(" ");
    }

    @Test
    void handleBusinessOmitsTraceIdWhenPropertiesNull() {
        FailureProperties props = new FailureProperties();
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(context);

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, null, new ObjectMapper());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());

        Business ex = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        handler.handle(exchange, ex).contextWrite(Context.of(ReactiveTrace.SCENE_KEY, "CREATE")).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains(FailureConst.FIELD_SCENE);
        assertThat(body).doesNotContain(FailureConst.FIELD_TRACE_ID);
        assertThat(body).doesNotContain(FailureConst.FIELD_ERRORS);
    }

    @Test
    void handleBusinessOmitsTraceIdWhenTraceIdConfigNull() {
        FailureProperties props = new FailureProperties();
        props.setVerbose(false);
        props.setTraceId(null);
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(context);

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());

        Business ex = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        handler.handle(exchange, ex).contextWrite(Context.of(ReactiveTrace.TRACE_ID_KEY, "ctx", ReactiveTrace.SCENE_KEY, "CREATE")).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).doesNotContain(FailureConst.FIELD_TRACE_ID);
    }

    @Test
    void handleBusinessOmitsSceneWhenSceneBlank() {
        FailureProperties props = new FailureProperties();
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        context.setScene(" ");
        Ex.setContext(context);

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());

        Business ex = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        handler.handle(exchange, ex).contextWrite(Context.empty()).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).doesNotContain(FailureConst.FIELD_SCENE);
    }

    @Test
    void handleMultiBusinessOmitsTraceIdAndErrorsWhenPropertiesNull() {
        FailureProperties props = new FailureProperties();
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        context.setScene(" ");
        Ex.setContext(context);

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, null, new ObjectMapper());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());

        MultiBusiness ex = new MultiBusiness(List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "a"),
                Business.of(ResponseCode.VALIDATION_ERROR_400, "b")
        ));
        handler.handle(exchange, ex).contextWrite(Context.empty()).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).doesNotContain(FailureConst.FIELD_TRACE_ID);
        assertThat(body).doesNotContain(FailureConst.FIELD_ERRORS);
        assertThat(body).doesNotContain(FailureConst.FIELD_SCENE);
    }

    @Test
    void handleMultiBusinessOmitsTraceIdWhenTraceIdConfigNullAndVerboseFalse() {
        FailureProperties props = new FailureProperties();
        props.setVerbose(false);
        props.setTraceId(null);
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(context);

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());

        MultiBusiness ex = new MultiBusiness(List.of(
                Business.of(ResponseCode.VALIDATION_ERROR_400, "a"),
                Business.of(ResponseCode.VALIDATION_ERROR_400, "b")
        ));
        handler.handle(exchange, ex).contextWrite(Context.empty()).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).doesNotContain(FailureConst.FIELD_TRACE_ID);
        assertThat(body).doesNotContain(FailureConst.FIELD_ERRORS);
    }

    @Test
    void resolveTraceIdSkipsHeaderWhenHeaderNameBlank() throws Exception {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setEnabled(true);
        props.getTraceId().setHeaderName(" ");
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        context.setTraceId(" ");
        Ex.setContext(context);

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());
        java.lang.reflect.Method m = FailFastWebExceptionHandler.class.getDeclaredMethod("resolveTraceId", ServerWebExchange.class, ContextView.class);
        m.setAccessible(true);

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        Object out = m.invoke(handler, exchange, Context.empty());
        assertThat(out).isEqualTo(" ");
    }

    @Test
    void resolveTraceIdSkipsHeaderWhenHeaderValueBlank() throws Exception {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setEnabled(true);
        props.getTraceId().setHeaderName("X-Trace-Id");
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        context.setTraceId(" ");
        Ex.setContext(context);

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());
        java.lang.reflect.Method m = FailFastWebExceptionHandler.class.getDeclaredMethod("resolveTraceId", ServerWebExchange.class, ContextView.class);
        m.setAccessible(true);

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t")
                .header("X-Trace-Id", " ")
                .build());
        Object out = m.invoke(handler, exchange, Context.empty());
        assertThat(out).isEqualTo(" ");
    }

    @Test
    void resolveTraceIdHitsFinalReturnWhenContextNull() throws Exception {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setEnabled(true);
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(null, props, new ObjectMapper());

        java.lang.reflect.Method m = FailFastWebExceptionHandler.class.getDeclaredMethod("resolveTraceId", ServerWebExchange.class, ContextView.class);
        m.setAccessible(true);

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        Object out = m.invoke(handler, exchange, Context.empty());
        assertThat(out).isNull();
    }
}

