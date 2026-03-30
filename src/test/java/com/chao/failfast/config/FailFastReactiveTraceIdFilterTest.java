package com.chao.failfast.config;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.autoconfigure.FailFastReactiveAutoConfiguration;
import com.chao.failfast.config.mapping.CodeMappingConfig;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.integration.webflux.ReactiveTrace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Reactive TraceIdFilter 测试")
class FailFastReactiveTraceIdFilterTest {

    @Test
@DisplayName("display")
    void testPutAttributesAndContext() {
        FailureProperties props = new FailureProperties();
        props.getTraceId().setEnabled(true);
        props.getTraceId().setHeaderName("X-Trace-Id");
        props.getTraceId().setGenerateIfMissing(false);
        props.getReactive().setContextFirst(true);

        FailureContext context = new FailureContext(props, new CodeMappingConfig(props), null);

        FailFastReactiveAutoConfiguration.FailFastReactiveTraceIdFilter filter =
                new FailFastReactiveAutoConfiguration.FailFastReactiveTraceIdFilter(context, props.getTraceId());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/t").header("X-Trace-Id", "abc").build()
        );

        AtomicReference<String> ctxTraceId = new AtomicReference<>();
        WebFilterChain chain = webExchange -> Mono.deferContextual(c -> {
            Object v = c.getOrDefault(ReactiveTrace.TRACE_ID_KEY, null);
            ctxTraceId.set(v instanceof String s ? s : null);
            return Mono.empty();
        });

        filter.filter(exchange, chain).block();

        assertThat(exchange.getAttributes().get(FailureConst.FIELD_TRACE_ID)).isEqualTo("abc");
        assertThat(ctxTraceId.get()).isEqualTo("abc");
    }
}
