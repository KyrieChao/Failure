package com.chao.failure.internal.core;

import com.chao.failure.config.mapping.CodeMappingConfig;
import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.constant.Severity;
import com.chao.failure.constant.FailureConst;
import com.chao.failure.integration.webflux.ReactiveTrace;
import com.chao.failure.internal.policy.DefaultErrorPolicy;
import com.chao.failure.internal.policy.ErrorPolicy;
import lombok.Getter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Map;
import java.util.function.Supplier;

/**
 * FailFast Context - Thread-safe configuration management.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
@Component
public class FailureContext {
    private static final int DEFAULT_STRICT_MAX_ERRORS = 50;

    /**
     * Global configuration properties.
     */
    private final FailureProperties properties;
    /**
     * Global code mapping configuration.
     */
    @Getter
    private final CodeMappingConfig codeMappingConfig;
    @Getter
    private final ErrorPolicy errorPolicy;

    /**
     * Thread-local method enabled override.
     */
    private final ThreadLocal<Boolean> methodEnabledOverride = ThreadLocal.withInitial(() -> null);

    /**
     * Thread-local method print override.
     */
    private final ThreadLocal<Boolean> printMethodOverride = ThreadLocal.withInitial(() -> null);

    /**
     * Thread-local traceId.
     */
    private final ThreadLocal<String> traceId = ThreadLocal.withInitial(() -> null);

    /**
     * Thread-local scene.
     */
    private final ThreadLocal<String> scene = ThreadLocal.withInitial(() -> FailureConst.DEFAULT_SCENE);

    /**
     * Constructor.
     *
     * @param properties FailFast configuration properties
     */
    public FailureContext(FailureProperties properties, CodeMappingConfig codeMappingConfig, ErrorPolicy errorPolicy) {
        this.properties = properties;
        this.codeMappingConfig = codeMappingConfig;
        this.errorPolicy = errorPolicy != null ? errorPolicy : DefaultErrorPolicy.INSTANCE;
    }

    /**
     * Check if method name info should be printed.
     *
     * @return True to print method info, false otherwise
     */
    public boolean isShadowTrace() {
        Boolean override = printMethodOverride.get();
        if (override != null) {
            return override;
        }
        return properties.isShadowTrace();
    }

    public boolean isTrimStackTrace() {
        return properties.isTrimStackTrace();
    }

    public boolean isMethodEnabled() {
        Boolean override = methodEnabledOverride.get();
        if (override != null) {
            return override;
        }
        return properties.isMethodValidationEnabled();
    }

    /**
     * Check if debug snapshot is enabled.
     *
     * @return True if snapshot is enabled
     */
    public boolean isDebugSnapshot() {
        return properties.isDebugSnapshot();
    }

    public boolean isReactiveContextFirst() {
        FailureProperties.Reactive reactive = properties.getReactive();
        return reactive != null && reactive.isContextFirst();
    }

    /**
     * Clear context variables for current thread.
     */
    public void clearThreadContext() {
        printMethodOverride.remove();
        methodEnabledOverride.remove();
        traceId.remove();
        scene.remove();
    }

    /**
     * Get traceId.
     *
     * @return traceId
     */
    public String getTraceId() {
        return traceId.get();
    }

    /**
     * Set traceId.
     *
     * @param traceId traceId
     */
    public void setTraceId(String traceId) {
        this.traceId.set(traceId);
    }

    /**
     * Get scene.
     *
     * @return scene
     */
    public String getScene() {
        return scene.get();
    }

    /**
     * Set scene.
     *
     * @param scene scene
     */
    public void setScene(String scene) {
        this.scene.set(scene);
    }

    public void setMethodEnabled(Boolean enabled) {
        if (enabled == null) {
            methodEnabledOverride.remove();
        } else {
            methodEnabledOverride.set(enabled);
        }
    }

    public void setShadowTrace(Boolean enabled) {
        if (enabled == null) {
            printMethodOverride.remove();
        } else {
            printMethodOverride.set(enabled);
        }
    }

    /**
     * Execute code block under specified method print configuration.
     *
     * @param printMethod Whether to print method info
     * @param action      Code block to execute
     * @param <T>         Return value type
     * @return Execution result
     */
    @SuppressWarnings("unchecked")
    public <T> T withPrintMethod(boolean printMethod, Supplier<T> action) {
        Boolean original = printMethodOverride.get();
        try {
            printMethodOverride.set(printMethod);
            T result = action.get();
            if (result instanceof Mono<?> mono) {
                String traceId = getTraceId();
                String sc = getScene();
                return (T) mono
                        .contextWrite(ctx -> {
                            Context c = ctx.put(ReactiveTrace.SHADOW_TRACE_KEY, printMethod);
                            if (traceId != null && !traceId.isBlank()) {
                                c = c.put(ReactiveTrace.TRACE_ID_KEY, traceId);
                            }
                            if (sc != null && !sc.isBlank()) {
                                c = c.put(ReactiveTrace.SCENE_KEY, sc);
                            }
                            return c;
                        });
            }
            if (result instanceof Flux<?> flux) {
                String traceId = getTraceId();
                String sc = getScene();
                return (T) flux
                        .contextWrite(ctx -> {
                            Context c = ctx.put(ReactiveTrace.SHADOW_TRACE_KEY, printMethod);
                            if (traceId != null && !traceId.isBlank()) {
                                c = c.put(ReactiveTrace.TRACE_ID_KEY, traceId);
                            }
                            if (sc != null && !sc.isBlank()) {
                                c = c.put(ReactiveTrace.SCENE_KEY, sc);
                            }
                            return c;
                        });
            }
            return result;
        } finally {
            if (original == null) printMethodOverride.remove();
            else printMethodOverride.set(original);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T withMethodEnabled(boolean enabled, Supplier<T> action) {
        Boolean original = methodEnabledOverride.get();
        try {
            methodEnabledOverride.set(enabled);
            T result = action.get();
            if (result instanceof Mono<?> mono) {
                String traceId = getTraceId();
                String sc = getScene();
                return (T) mono
                        .contextWrite(ctx -> {
                            Context c = ctx.put(ReactiveTrace.METHOD_ENABLED_KEY, enabled);
                            if (traceId != null && !traceId.isBlank()) {
                                c = c.put(ReactiveTrace.TRACE_ID_KEY, traceId);
                            }
                            if (sc != null && !sc.isBlank()) {
                                c = c.put(ReactiveTrace.SCENE_KEY, sc);
                            }
                            return c;
                        });
            }
            if (result instanceof Flux<?> flux) {
                String traceId = getTraceId();
                String sc = getScene();
                return (T) flux
                        .contextWrite(ctx -> {
                            Context c = ctx.put(ReactiveTrace.METHOD_ENABLED_KEY, enabled);
                            if (traceId != null && !traceId.isBlank()) {
                                c = c.put(ReactiveTrace.TRACE_ID_KEY, traceId);
                            }
                            if (sc != null && !sc.isBlank()) {
                                c = c.put(ReactiveTrace.SCENE_KEY, sc);
                            }
                            return c;
                        });
            }
            return result;
        } finally {
            if (original == null) methodEnabledOverride.remove();
            else methodEnabledOverride.set(original);
        }
    }

    /**
     * Execute void code block under specified method print configuration.
     *
     * @param printMethod Whether to print method info
     * @param action      Code block to execute
     */
    public void withPrintMethod(boolean printMethod, Runnable action) {
        Boolean original = printMethodOverride.get();
        try {
            printMethodOverride.set(printMethod);
            action.run();
        } finally {
            if (original == null) printMethodOverride.remove();
            else printMethodOverride.set(original);
        }
    }

    public int getStrictMaxErrors() {
        FailureProperties.Chain chain = properties.getChain();
        if (chain == null || chain.getMaxErrors() <= 0) {
            return DEFAULT_STRICT_MAX_ERRORS;
        }
        return chain.getMaxErrors();
    }

    public Severity resolveSeverity(ResponseCode code) {
        FailureProperties.Logging logging = properties.getLogging();
        Severity fallback = Severity.INFO;
        if (logging == null) {
            return fallback;
        }

        fallback = Severity.from(logging.getDefaultSeverity(), fallback);
        if (code == null) {
            return fallback;
        }

        Map<String, String> mapping = logging.getSeverityMapping();
        if (mapping == null || mapping.isEmpty()) {
            return fallback;
        }
        String configured = mapping.get(String.valueOf(code.getCode()));
        return Severity.from(configured, fallback);
    }

    public ContextScope openScope() {
        return new ContextScope(this, snapshot());
    }

    public ContextScope openScope(String traceId, String scene, Boolean methodEnabled, Boolean shadowTrace) {
        ContextScope scope = new ContextScope(this, snapshot());
        if (traceId == null) {
            this.traceId.remove();
        } else {
            setTraceId(traceId);
        }
        if (scene != null) {
            setScene(scene);
        }
        setMethodEnabled(methodEnabled);
        setShadowTrace(shadowTrace);
        return scope;
    }

    public <T> T withThreadScope(String traceId, String scene, Supplier<T> action) {
        try (ContextScope ignored = openScope(traceId, scene, null, null)) {
            return action.get();
        }
    }

    public void withThreadScope(String traceId, String scene, Runnable action) {
        try (ContextScope ignored = openScope(traceId, scene, null, null)) {
            action.run();
        }
    }

    private Snapshot snapshot() {
        return new Snapshot(traceId.get(), scene.get(), methodEnabledOverride.get(), printMethodOverride.get());
    }

    public static final class ContextScope implements AutoCloseable {
        private final FailureContext context;
        private final Snapshot snapshot;
        private boolean closed;

        private ContextScope(FailureContext context, Snapshot snapshot) {
            this.context = context;
            this.snapshot = snapshot;
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            if (snapshot.traceId() == null) context.traceId.remove();
            else context.traceId.set(snapshot.traceId());

            if (snapshot.scene() == null) context.scene.remove();
            else context.scene.set(snapshot.scene());

            if (snapshot.methodEnabled() == null) context.methodEnabledOverride.remove();
            else context.methodEnabledOverride.set(snapshot.methodEnabled());

            if (snapshot.shadowTrace() == null) context.printMethodOverride.remove();
            else context.printMethodOverride.set(snapshot.shadowTrace());
        }
    }

    private record Snapshot(String traceId, String scene, Boolean methodEnabled, Boolean shadowTrace) {
    }
}
