package com.chao.failfast.autoconfigure;

import com.chao.failfast.config.mapping.CodeMappingConfig;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.integration.webflux.FailFastWebExceptionHandler;
import com.chao.failfast.integration.webflux.ReactiveTrace;
import com.chao.failfast.internal.core.Chain;
import com.chao.failfast.internal.core.Ex;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableConfigurationProperties(FailureProperties.class)
public class FailFastReactiveAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public CodeMappingConfig codeMappingConfig(FailureProperties properties) {
        return new CodeMappingConfig(properties);
    }

    @ConditionalOnMissingBean
    @Bean
    public FailureContext failureContext(FailureProperties properties, CodeMappingConfig codeMappingConfig) {
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        Ex.setContext(context);
        Chain.setFailureProperties(properties);
        return context;
    }

    @AutoConfiguration
    static class ReactiveBeans {

        @ConditionalOnMissingBean(name = "failFastReactiveCleanupFilter")
        @Bean
        public WebFilter failFastReactiveCleanupFilter(FailureContext context) {
            return new FailFastReactiveCleanupFilter(context);
        }

        @ConditionalOnProperty(prefix = "fail-fast.trace-id", name = "enabled", havingValue = "true")
        @ConditionalOnMissingBean(name = "failFastReactiveTraceIdFilter")
        @Bean
        public WebFilter failFastReactiveTraceIdFilter(FailureContext context, FailureProperties properties) {
            return new FailFastReactiveTraceIdFilter(context, properties.getTraceId());
        }

        @ConditionalOnMissingBean
        @org.springframework.context.annotation.Bean
        public FailFastWebExceptionHandler failFastWebExceptionHandler(FailureContext context, FailureProperties properties, ObjectProvider<ObjectMapper> objectMapperProvider) {
            return new FailFastWebExceptionHandler(context, properties, objectMapperProvider.getIfAvailable(ObjectMapper::new));
        }
    }

    static class FailFastReactiveCleanupFilter implements WebFilter, Ordered {
        private final FailureContext context;

        FailFastReactiveCleanupFilter(FailureContext context) {
            this.context = context;
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE + 10;
        }

        @Override
        public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
            return chain.filter(exchange)
                    .doFinally(signalType -> context.clearThreadContext());
        }
    }

    public static class FailFastReactiveTraceIdFilter implements WebFilter, Ordered {
        private final FailureContext context;
        private final FailureProperties.TraceId traceId;

        public FailFastReactiveTraceIdFilter(FailureContext context, FailureProperties.TraceId traceId) {
            this.context = context;
            this.traceId = traceId;
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE + 5;
        }

        @Override
        public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
            String headerName = traceId.getHeaderName();
            String traceIdValue = StringUtils.hasText(headerName) ? exchange.getRequest().getHeaders().getFirst(headerName) : null;
            if (!StringUtils.hasText(traceIdValue) && traceId.isGenerateIfMissing()) {
                traceIdValue = UUID.randomUUID().toString();
            }
            if (StringUtils.hasText(traceIdValue)) {
                context.setTraceId(traceIdValue);
                exchange.getAttributes().put(com.chao.failfast.constant.FailureConst.FIELD_TRACE_ID, traceIdValue);
                if (traceId.isResponseHeader()) {
                    String responseHeaderName = StringUtils.hasText(traceId.getResponseHeaderName()) ? traceId.getResponseHeaderName() : headerName;
                    if (StringUtils.hasText(responseHeaderName)) {
                        exchange.getResponse().getHeaders().set(responseHeaderName, traceIdValue);
                    }
                }
            }
            String finalTraceId = traceIdValue;
            Mono<Void> result = chain.filter(exchange);
            if (StringUtils.hasText(finalTraceId)) {
                result = result
                        .contextWrite(ctx -> ctx.put(ReactiveTrace.TRACE_ID_KEY, finalTraceId));
            }
            return result;
        }
    }
}
