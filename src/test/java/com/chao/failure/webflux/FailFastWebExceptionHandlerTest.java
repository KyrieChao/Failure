package com.chao.failure.webflux;

import com.chao.failure.constant.FailureConst;
import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.exception.Business;
import com.chao.failure.integration.webflux.FailFastWebExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("display")
class FailFastWebExceptionHandlerTest {

    @Test
    @DisplayName("traceId 优先使用 exchange attributes")
    void testTraceIdFromExchangeAttributes() {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setEnabled(true);
        props.getTraceId().setHeaderName("X-Trace-Id");
        props.getReactive().setContextFirst(true);

        FailureContext context = new FailureContext(props, new com.chao.failure.config.mapping.CodeMappingConfig(props), null);
        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(context, props, new ObjectMapper());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        exchange.getAttributes().put(FailureConst.FIELD_TRACE_ID, "attr-trace");

        Business ex = Business.of(ResponseCode.VALIDATION_ERROR_400, "bad");
        handler.handle(exchange, ex)
                .contextWrite(ctx -> ctx.put(com.chao.failure.integration.webflux.ReactiveTrace.SCENE_KEY, "SCENE_A"))
                .block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains("\"traceId\":\"attr-trace\"");
        assertThat(body).contains("\"scene\":\"SCENE_A\"");
    }

    @Test
    @DisplayName("traceId 回退到请求头")
    void testTraceIdFromHeader() {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setEnabled(true);
        props.getTraceId().setHeaderName("X-Trace-Id");

        FailFastWebExceptionHandler handler = new FailFastWebExceptionHandler(null, props, new ObjectMapper());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/t").header("X-Trace-Id", "header-trace").build()
        );

        Business ex = Business.of(ResponseCode.VALIDATION_ERROR_400, "bad");
        handler.handle(exchange, ex).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains("\"traceId\":\"header-trace\"");
    }
}
