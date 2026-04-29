package com.chao.failure.integration.webflux;

/**
 * FailFast web exception handler for reactive applications.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */

import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.constant.FailureConst;
import com.chao.failure.exception.Business;
import com.chao.failure.exception.MultiBusiness;
import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.internal.core.i18n.LocalizedTexts;
import com.chao.failure.internal.core.observability.OpenTelemetryBridge;
import com.chao.failure.internal.core.observability.TraceInfoExtractor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FailFastWebExceptionHandler implements WebExceptionHandler, Ordered {

    private final FailureContext context;
    private final FailureProperties properties;
    private final ObjectMapper objectMapper;

    public FailFastWebExceptionHandler(FailureContext context, FailureProperties properties, ObjectMapper objectMapper) {
        this.context = context;
        this.properties = properties;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    public Mono<Void> handleInternal(ServerWebExchange exchange, Throwable ex, ContextView ctxView) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        if (ex instanceof MultiBusiness m) {
            return write(exchange, m.getHttpStatus().value(), buildMultiMap(exchange, m, ctxView));
        }
        if (ex instanceof Business b) {
            return write(exchange, b.getHttpStatus().value(), buildMap(exchange, b, ctxView));
        }
        if (ex instanceof WebExchangeBindException w) {
            List<Business> errors = new ArrayList<>();
            for (FieldError fieldError : w.getBindingResult().getFieldErrors()) {
                String path = fieldError.getField();
                String detail = fieldError.getDefaultMessage();
                errors.add(Business.compose()
                        .responseCode(ResponseCode.VALIDATION_ERROR_400)
                        .detail(detail)
                        .path(path)
                        .materialize());
            }
            return handleErrors(exchange, errors, ctxView);
        }
        if (ex instanceof ConstraintViolationException c) {
            List<Business> errors = new ArrayList<>();
            for (ConstraintViolation<?> v : c.getConstraintViolations()) {
                String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : null;
                String detail = v.getMessage();
                errors.add(Business.compose()
                        .responseCode(ResponseCode.VALIDATION_ERROR_400)
                        .detail(detail)
                        .path(path)
                        .materialize());
            }
            return handleErrors(exchange, errors, ctxView);
        }

        return Mono.error(ex);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        return Mono.deferContextual(ctxView -> handleInternal(exchange, ex, ctxView));
    }

    public Mono<Void> handleErrors(ServerWebExchange exchange, List<Business> errors, ContextView ctxView) {
        if (errors.isEmpty()) {
            Business b = Business.of(ResponseCode.VALIDATION_ERROR_400, FailureConst.VALIDATION_ERROR);
            return write(exchange, 400, buildMap(exchange, b, ctxView));
        }
        if (errors.size() == 1) {
            Business b = errors.get(0);
            return write(exchange, b.getHttpStatus().value(), buildMap(exchange, b, ctxView));
        }
        MultiBusiness multi = new MultiBusiness(errors);
        return write(exchange, multi.getHttpStatus().value(), buildMultiMap(exchange, multi, ctxView));
    }

    public Map<String, Object> buildMap(ServerWebExchange exchange, Business e, ContextView ctxView) {
        Map<String, Object> body = new HashMap<>();
        body.put(FailureConst.FIELD_CODE, e.getResponseCode().getCode());
        body.put(FailureConst.FIELD_MESSAGE, LocalizedTexts.message(e.getResponseCode()));
        body.put(FailureConst.FIELD_DESCRIPTION, LocalizedTexts.detail(e.getResponseCode(), e.getDetail()));
        if (properties != null && properties.getTraceId() != null && properties.getTraceId().isEnabled()) {
            String traceId = e.getTraceId();
            if (traceId == null || traceId.isBlank()) {
                traceId = resolveTraceId(exchange, ctxView);
            }
            if (traceId != null && !traceId.isBlank()) {
                body.put(FailureConst.FIELD_TRACE_ID, traceId);
            }
            String spanId = e.getSpanId();
            if (spanId == null || spanId.isBlank()) {
                spanId = OpenTelemetryBridge.currentSpanId();
            }
            if (spanId != null && !spanId.isBlank()) {
                body.put(FailureConst.FIELD_SPAN_ID, spanId);
            }
        }
        String scene = TraceInfoExtractor.scene(context, ctxView);
        if (scene == null) {
            scene = FailureConst.DEFAULT_SCENE;
        }
        if (!scene.isBlank() && !FailureConst.DEFAULT_SCENE.equals(scene)) {
            body.put(FailureConst.FIELD_SCENE, scene);
        }
        if (properties != null && properties.isVerbose()) {
            body.put(FailureConst.FIELD_ERRORS, List.of(buildMapDetail(e)));
        }
        body.put(FailureConst.FIELD_TIMESTAMP, ZonedDateTime.now(FailureConst.CST).format(FailureConst.DEFAULT_DATETIME_FORMATTER));
        return body;
    }

    public Map<String, Object> buildMultiMap(ServerWebExchange exchange, MultiBusiness e, ContextView ctxView) {
        Map<String, Object> body = new HashMap<>();
        body.put(FailureConst.FIELD_CODE, e.getResponseCode().getCode());
        body.put(FailureConst.FIELD_MESSAGE, LocalizedTexts.message(e.getResponseCode()));
        body.put(FailureConst.FIELD_DESCRIPTION, LocalizedTexts.detail(e.getResponseCode(), e.getDetail()));
        if (properties != null && properties.getTraceId() != null && properties.getTraceId().isEnabled()) {
            String traceId = e.getTraceId();
            if (traceId == null || traceId.isBlank()) {
                traceId = resolveTraceId(exchange, ctxView);
            }
            if (traceId != null && !traceId.isBlank()) {
                body.put(FailureConst.FIELD_TRACE_ID, traceId);
            }
            String spanId = !e.getErrors().isEmpty() ? e.getErrors().get(0).getSpanId() : null;
            if (spanId == null || spanId.isBlank()) {
                spanId = OpenTelemetryBridge.currentSpanId();
            }
            if (spanId != null && !spanId.isBlank()) {
                body.put(FailureConst.FIELD_SPAN_ID, spanId);
            }
        }
        String scene = TraceInfoExtractor.scene(context, ctxView);
        if (scene == null) {
            scene = FailureConst.DEFAULT_SCENE;
        }
        if (!scene.isBlank() && !FailureConst.DEFAULT_SCENE.equals(scene)) {
            body.put(FailureConst.FIELD_SCENE, scene);
        }
        if (properties != null && properties.isVerbose()) {
            List<Map<String, Object>> errorList = new ArrayList<>();
            for (Business err : e.getErrors()) {
                errorList.add(buildMapDetail(err));
            }
            body.put(FailureConst.FIELD_ERRORS, errorList);
        }
        body.put(FailureConst.FIELD_TIMESTAMP, ZonedDateTime.now(FailureConst.CST).format(FailureConst.DEFAULT_DATETIME_FORMATTER));
        return body;
    }

    public Map<String, Object> buildMapDetail(Business e) {
        Map<String, Object> item = new HashMap<>();
        item.put(FailureConst.FIELD_PATH, e.getPath());
        item.put(FailureConst.FIELD_CODE, e.getResponseCode().getCode());
        item.put(FailureConst.FIELD_REJECTED, e.getInvalidValue());
        item.put(FailureConst.FIELD_DETAIL, LocalizedTexts.detail(e.getResponseCode(), e.getDetail()));
        item.put(FailureConst.FIELD_MESSAGE, LocalizedTexts.message(e.getResponseCode()));
        return item;
    }

    public Mono<Void> write(ServerWebExchange exchange, int status, Map<String, Object> body) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.valueOf(status));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = ("{\"code\":500,\"message\":\"" + FailureConst.UNKNOWN_ERROR + "\"}").getBytes(StandardCharsets.UTF_8);
        }
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    public String resolveTraceId(ServerWebExchange exchange, ContextView ctxView) {
        if (exchange == null) {
            return context != null ? context.getTraceId() : null;
        }
        String ctxFirst = TraceInfoExtractor.traceId(context, ctxView);
        if (ctxFirst != null && !ctxFirst.isBlank()) {
            return ctxFirst;
        }
        Object attr = exchange.getAttributes().get(FailureConst.FIELD_TRACE_ID);
        if (attr instanceof String s && !s.isBlank()) {
            return s;
        }
        FailureProperties.TraceId traceId = properties != null ? properties.getTraceId() : null;
        String headerName = traceId != null ? traceId.getHeaderName() : null;
        if (headerName != null && !headerName.isBlank()) {
            String headerValue = exchange.getRequest().getHeaders().getFirst(headerName);
            if (headerValue != null && !headerValue.isBlank()) {
                return headerValue;
            }
        }
        return context != null ? context.getTraceId() : null;
    }
}
