package com.chao.failfast.internal.core;

import com.chao.failfast.config.mapping.CodeMappingConfig;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.integration.webflux.ReactiveTrace;
import com.chao.failfast.internal.policy.DefaultErrorPolicy;
import com.chao.failfast.internal.policy.ErrorPolicy;
import lombok.Getter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.function.Supplier;

/**
 * FailFast Context - Thread-safe configuration management.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
@Component
public class FailureContext {

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
}
