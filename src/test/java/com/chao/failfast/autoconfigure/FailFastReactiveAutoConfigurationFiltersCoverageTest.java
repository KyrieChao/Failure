package com.chao.failfast.autoconfigure;

import com.chao.failfast.config.mapping.CodeMappingConfig;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.core.FailureContext;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FailFastReactiveAutoConfigurationFiltersCoverageTest {

    @Test
    void cleanupFilterCoversOrderAndFinallyCleanup() {
        FailureProperties props = new FailureProperties();
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        context.setTraceId("t1");
        context.setScene("CREATE");

        WebFilter filter = new FailFastReactiveAutoConfiguration.FailFastReactiveCleanupFilter(context);
        assertThat(((Ordered) filter).getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 10);

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        WebFilterChain chain = webExchange -> Mono.empty();

        filter.filter(exchange, chain).block();

        assertThat(context.getTraceId()).isNull();
        assertThat(context.getScene()).isEqualTo(FailureConst.DEFAULT_SCENE);
    }

    @Test
    void traceIdFilterCoversGenerateAndResponseHeader() {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setHeaderName("X-Trace-Id");
        props.getTraceId().setEnabled(true);
        props.getTraceId().setGenerateIfMissing(true);
        props.getTraceId().setResponseHeader(true);
        props.getTraceId().setResponseHeaderName("");

        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);

        WebFilter filter = new FailFastReactiveAutoConfiguration.FailFastReactiveTraceIdFilter(context, props.getTraceId());
        assertThat(((Ordered) filter).getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 5);

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        WebFilterChain chain = webExchange -> Mono.empty();

        filter.filter(exchange, chain).block();

        Object attr = exchange.getAttributes().get(FailureConst.FIELD_TRACE_ID);
        assertThat(attr).isInstanceOf(String.class);
        assertThat((String) attr).isNotBlank();
        assertThat(exchange.getResponse().getHeaders().getFirst("X-Trace-Id")).isEqualTo(attr);
    }

    @Test
    void traceIdFilterCoversBlankHeaderNameAndNoGenerationBranches() {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setHeaderName("");
        props.getTraceId().setEnabled(true);
        props.getTraceId().setGenerateIfMissing(false);
        props.getTraceId().setResponseHeader(true);
        props.getTraceId().setResponseHeaderName("");

        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        WebFilter filter = new FailFastReactiveAutoConfiguration.FailFastReactiveTraceIdFilter(context, props.getTraceId());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        WebFilterChain chain = webExchange -> Mono.empty();

        filter.filter(exchange, chain).block();

        assertThat(exchange.getAttributes()).doesNotContainKey(FailureConst.FIELD_TRACE_ID);
    }

    @Test
    void traceIdFilterCoversCustomResponseHeaderNameBranch() {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setHeaderName("X-Trace-Id");
        props.getTraceId().setEnabled(true);
        props.getTraceId().setGenerateIfMissing(true);
        props.getTraceId().setResponseHeader(true);
        props.getTraceId().setResponseHeaderName("X-Trace-Id-Resp");

        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        WebFilter filter = new FailFastReactiveAutoConfiguration.FailFastReactiveTraceIdFilter(context, props.getTraceId());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        WebFilterChain chain = webExchange -> Mono.empty();

        filter.filter(exchange, chain).block();

        Object attr = exchange.getAttributes().get(FailureConst.FIELD_TRACE_ID);
        assertThat(exchange.getResponse().getHeaders().getFirst("X-Trace-Id-Resp")).isEqualTo(attr);
    }

    @Test
    void traceIdFilterCoversBlankResponseHeaderNameBranch() {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setHeaderName("");
        props.getTraceId().setEnabled(true);
        props.getTraceId().setGenerateIfMissing(true);
        props.getTraceId().setResponseHeader(true);
        props.getTraceId().setResponseHeaderName("");

        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        WebFilter filter = new FailFastReactiveAutoConfiguration.FailFastReactiveTraceIdFilter(context, props.getTraceId());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        WebFilterChain chain = webExchange -> Mono.empty();

        filter.filter(exchange, chain).block();

        assertThat(exchange.getAttributes()).containsKey(FailureConst.FIELD_TRACE_ID);
        assertThat(exchange.getResponse().getHeaders().containsKey("X-Trace-Id")).isFalse();
    }

    @Test
    void cleanupFilterNullExchangeThrowsNpe() {
        FailureProperties props = new FailureProperties();
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        WebFilter filter = new FailFastReactiveAutoConfiguration.FailFastReactiveCleanupFilter(context);
        assertThrows(NullPointerException.class, () -> filter.filter(null, webExchange -> Mono.empty()));
    }

    @Test
    void traceIdFilterNullArgsThrowNpe() {
        FailureProperties props = new FailureProperties();
        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);
        WebFilter filter = new FailFastReactiveAutoConfiguration.FailFastReactiveTraceIdFilter(context, props.getTraceId());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/t").build());
        assertThrows(NullPointerException.class, () -> filter.filter(null, webExchange -> Mono.empty()));
        assertThrows(NullPointerException.class, () -> filter.filter(exchange, null));
    }
}

