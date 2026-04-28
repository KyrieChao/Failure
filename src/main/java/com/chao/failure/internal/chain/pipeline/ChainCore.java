package com.chao.failure.internal.chain.pipeline;

import com.chao.failure.validator.FastValidator.ValidationContext;
import com.chao.failure.condition.Predicate;
import com.chao.failure.constant.Scenario;
import com.chao.failure.exception.Business;
import com.chao.failure.internal.core.Ex;
import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.internal.policy.DefaultErrorPolicy;
import com.chao.failure.internal.policy.ErrorPolicy;
import com.chao.failure.internal.validation.ObjectGraphWalker;
import com.chao.failure.internal.validation.RecursiveOption;
import com.chao.failure.internal.validation.ValidationEventManager;
import com.chao.failure.validator.TypedValidator;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Validation chain core - Manage state (failFast, alive, errors, context).
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.3.0
 */
public abstract class ChainCore<S extends ChainCore<S>> {
    private static final int DEFAULT_STRICT_MAX_ERRORS = 50;


    /**
     * Notify observer of validation start.
     *
     * @param source validation source
     * @param scene validation scene
     */
    protected void notifyValidationStart(String source, String scene) {
        ValidationEventManager.notifyStart(source, scene);
    }

    /**
     * Notify observer of validation end.
     *
     * @param source validation source
     * @param durationNanos duration in nanoseconds
     * @param success whether validation was successful
     */
    protected void notifyValidationEnd(String source, long durationNanos, boolean success) {
        ValidationEventManager.notifyEnd(source, durationNanos, success);
    }

    /**
     * Notify observer of validation failure.
     *
     * @param source validation source
     * @param errorCode error code
     */
    protected void notifyValidationFailure(String source, String errorCode) {
        ValidationEventManager.notifyFailure(source, errorCode);
    }

    /**
     * Notify observer of violation.
     *
     * @param source validation source
     * @param constraint constraint name
     */
    protected void notifyViolation(String source, String constraint) {
        ValidationEventManager.notifyViolation(source, constraint);
    }

    @Getter
    protected final boolean failFast;
    @Getter
    protected boolean alive = true;
    // Dynamic skip state (true=execute, false=skip)
    @Getter
    private boolean conditionState = true;
    // OR state
    private boolean orMode = false;
    private boolean orHasSuccess = false;
    @Getter
    private boolean errorsTruncated = false;
    protected final ValidationContext context;
    protected final List<Business> errors = new ArrayList<>();
    private final List<AsyncCheck> asyncChecks = new ArrayList<>();
    @Setter
    private Consumer<Business> errorConsumer;

    private record AsyncCheck(CompletionStage<Boolean> stage, ResponseCode code, String detail) {
    }

    private static final Logger log = LoggerFactory.getLogger(ChainCore.class);

    protected ChainCore(boolean failFast, ValidationContext context) {
        this.failFast = failFast;
        this.context = context;
    }

    public int errorSize() {
        if (context != null) return context.errorSize();
        return errors.size();
    }

    public S checkAsync(CompletionStage<Boolean> stage, ResponseCode code) {
        return checkAsync(stage, code, null);
    }

    public S checkAsync(CompletionStage<Boolean> stage, ResponseCode code, String detail) {
        if (shouldSkip()) {
            return self();
        }
        if (stage == null) {
            return check(false, code, detail);
        }
        asyncChecks.add(new AsyncCheck(stage, code, detail));
        return self();
    }

    protected CompletableFuture<Void> applyAsyncChecks() {
        if (asyncChecks.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        for (AsyncCheck async : asyncChecks) {
            future = future.thenCompose(ignored -> {
                if (shouldSkip()) {
                    return CompletableFuture.completedFuture(null);
                }
                CompletableFuture<Boolean> cf = async.stage.toCompletableFuture();
                return cf.handle((ok, ex) -> {
                    boolean pass = ex == null && Boolean.TRUE.equals(ok);
                    if (!pass) {
                        String d = async.detail;
                        if (d == null && ex != null) {
                            d = ex.getMessage();
                        }
                        check(false, async.code, d);
                    }
                    return null;
                }).thenAccept(v -> {
                });
            });
        }
        return future.whenComplete((v, e) -> asyncChecks.clear());
    }

    /**
     * Dynamically control whether to execute subsequent validation.
     *
     * @param condition If false, skip subsequent check/or operations until when(true) is called again
     * @return Current chain instance
     */
    public S when(boolean condition) {
        this.conditionState = condition;
        return self();
    }

    public S when(Predicate condition) {
        return when(condition == null || condition.evaluate());
    }

    /**
     * Conditionally execute a validation block.
     *
     * @param condition The condition to evaluate
     * @param block     The validation block to execute if the condition is true
     * @return Current chain instance
     */
    public S ifTrue(boolean condition, Consumer<S> block) {
        if (condition && alive) {
            boolean originalState = conditionState;
            try {
                conditionState = true;
                block.accept(self());
            } finally {
                conditionState = originalState;
            }
        }
        return self();
    }

    public S or() {
        if (!conditionState) return self();
        if (context != null && context.isStopped()) return self();
        this.orMode = true;
        this.orHasSuccess = isValid();   // If current valid, mark success

        // If failFast and already failed, or gives a second chance, reset alive
        if (failFast && !alive) {
            alive = true;
        }
        // Clear current errors, prepare to collect errors on the right side of or
        if (!orHasSuccess) {
            errors.clear();
        }
        return self();
    }

    public boolean shouldSkip() {
        // If currently dynamically skipped, or context stopped, then skip
        if (!conditionState) return true;
        if (context != null && context.isStopped()) return true;
        return (!alive && failFast);
    }

    @SuppressWarnings("unchecked")
    protected S self() {
        return (S) this;
    }

    public S check(Supplier<Boolean> conditionSupplier, CheckSpec spec) {
        if (shouldSkip()) return self();
        return check(conditionSupplier.get(), spec);
    }

    public S check(Supplier<Boolean> conditionSupplier, ResponseCode code) {
        if (shouldSkip()) return self();
        return check(conditionSupplier.get(), code, null, null);
    }

    /**
     * Lazy calculation validation - Support Supplier.
     *
     * @param conditionSupplier Condition supplier
     * @param code              Response code
     * @param detail            Detailed description
     * @return Current chain instance
     */
    public S check(Supplier<Boolean> conditionSupplier, ResponseCode code, String detail) {
        if (shouldSkip()) return self();
        return check(conditionSupplier.get(), code, detail, (Object) null);
    }

    /**
     * Unified validation entry - Support configuration.
     *
     * @param condition Validation condition
     * @param code      Response code
     * @param detail    Detailed description
     * @return Current chain instance
     */
    public S check(boolean condition, ResponseCode code, String detail) {
        return check(condition, code, detail, null);
    }

    public S check(boolean condition, CheckSpec spec) {
        if (spec == null) return check(condition, null, null, null);
        return check(condition, spec.code(), spec.detail(), spec.invalidValue());
    }

    public S check(boolean condition, ResponseCode code, String detail, Supplier<Object> invalidValueSupplier) {
        if (shouldSkip()) return self();

        if (orMode) {
            orMode = false;
            boolean finalSuccess = orHasSuccess || condition;

            if (!finalSuccess) {
                addError(code, detail, resolveInvalidValue(invalidValueSupplier));
                if (failFast) alive = false;
            } else {
                alive = true;
            }
        } else {
            if (!condition) {
                addError(code, detail, resolveInvalidValue(invalidValueSupplier));
                if (failFast) alive = false;
            }
        }
        return self();
    }

    /**
     * Unified validation entry - Support configuration and value snapshot.
     *
     * @param condition Validation condition
     * @param code      Response code
     * @param detail    Detailed description
     * @param value     Value snapshot
     * @return Current chain instance
     */
    public S check(boolean condition, ResponseCode code, String detail, Object value) {
        if (shouldSkip()) return self();

        if (orMode) {
            // OR mode: calculate combined result
            orMode = false;  // Consume or state
            boolean finalSuccess = orHasSuccess || condition;

            if (!finalSuccess) {
                // Both failed, report error
                addError(code, detail, value);
                if (failFast) alive = false;
            } else {
                // One success, whole or passed, clear errors
                alive = true;
                // errors already cleared in or()
            }
        } else {
            // Normal mode
            if (!condition) {
                addError(code, detail, value);
                if (failFast) alive = false;
            }
        }
        return self();
    }

    /**
     * Validation entry with ValueRef support.
     *
     * @param condition Validation condition
     * @param code      Response code
     * @param valueRef  Value reference with path
     * @return Current chain instance
     */
    public S checkRef(boolean condition, ResponseCode code, PathEntry<?> valueRef) {
        if (shouldSkip()) return self();

        Object value = valueRef != null ? valueRef.value() : null;
        String path = valueRef != null ? valueRef.path() : null;

        return checkWithPathAndConstraint(condition, code, null, value, path, null);
    }

    /**
     * Validation entry with path and constraint support.
     *
     * @param condition  Validation condition
     * @param code       Response code
     * @param detail     Detailed description
     * @param value      Value snapshot
     * @param path       Path to the field
     * @param constraint Constraint name
     * @param source     Validation source
     * @return Current chain instance
     */
    public S checkWithPathAndConstraint(boolean condition, ResponseCode code, String detail, Object value, String path, String constraint, String source) {
        if (shouldSkip()) return self();

        if (orMode) {
            // OR mode: calculate combined result
            orMode = false;  // Consume or state
            boolean finalSuccess = orHasSuccess || condition;

            if (!finalSuccess) {
                // Both failed, report error
                addError(code, detail, value, path, constraint, source);
                if (failFast) alive = false;
            } else {
                // One success, whole or passed, clear errors
                alive = true;
                // errors already cleared in or()
            }
        } else {
            // Normal mode
            if (!condition) {
                addError(code, detail, value, path, constraint, source);
                if (failFast) alive = false;
            }
        }
        return self();
    }

    /**
     * Validation entry with path and constraint support (default source: chain).
     *
     * @param condition  Validation condition
     * @param code       Response code
     * @param detail     Detailed description
     * @param value      Value snapshot
     * @param path       Path to the field
     * @param constraint Constraint name
     * @return Current chain instance
     */
    public S checkWithPathAndConstraint(boolean condition, ResponseCode code, String detail, Object value, String path, String constraint) {
        return checkWithPathAndConstraint(condition, code, detail, value, path, constraint, "chain");
    }

    private Object resolveInvalidValue(Supplier<Object> invalidValueSupplier) {
        if (invalidValueSupplier == null) return null;
        FailureContext ctx = Ex.getContext();
        ErrorPolicy policy = ctx != null ? Objects.requireNonNullElse(ctx.getErrorPolicy(), DefaultErrorPolicy.INSTANCE) : DefaultErrorPolicy.INSTANCE;
        if (!policy.captureInvalidValue(ctx)) return null;
        return invalidValueSupplier.get();
    }

    /**
     * No configuration validation - Use default error.
     *
     * @param condition Validation condition
     * @return Current chain instance
     */
    public S check(boolean condition) {
        return check(condition, null, null);
    }

    protected void addError(ResponseCode code, String detail, Object value) {
        addError(code, detail, value, null, null, "chain");
    }

    protected void addError(ResponseCode code, String detail, Object value, String path, String constraint) {
        addError(code, detail, value, path, constraint, "chain");
    }

    protected void addError(ResponseCode code, String detail, Object value, String path, String constraint, String source) {
        if (hasReachedErrorLimit()) {
            markErrorLimitReached();
            return;
        }
        Business business = buildBusiness(code, detail, value, path, constraint);
        Consumer<Business> consumer = this.errorConsumer;
        if (consumer != null) {
            consumer.accept(business);
        }

        if (context != null) {
            context.reportError(business);
            if (failFast) context.stop();
            if (hasReachedErrorLimit()) {
                markErrorLimitReached();
            }
        } else {
            errors.add(business);
            if (hasReachedErrorLimit()) {
                markErrorLimitReached();
            }
        }

        // Notify observer of violation
        if (constraint != null) {
            ValidationEventManager.notifyViolation(source, constraint);
        }
    }


    private Business buildBusiness(ResponseCode code, String detail, Object value, String path, String constraint) {
        FailureContext ctx = Ex.getContext();
        ErrorPolicy policy = ctx != null ? Objects.requireNonNullElse(ctx.getErrorPolicy(), DefaultErrorPolicy.INSTANCE) : DefaultErrorPolicy.INSTANCE;

        Business.Fabricator fabricator = Business.compose();
        if (value != null && policy.captureInvalidValue(ctx)) {
            fabricator.invalidValue(value);
        }
        if (path != null) {
            fabricator.path(path);
        }
        if (code != null && detail != null) {
            return fabricator.responseCode(code).detail(detail).materialize();
        }
        return fabricator.responseCode(Objects.requireNonNullElse(code, policy.defaultCode())).materialize();
    }

    private Business buildBusiness(ResponseCode code, String detail, Object value, String path) {
        return buildBusiness(code, detail, value, path, null);
    }

    private String getSceneName() {
        if (context == null) {
            return Scenario.DEFAULT.name();
        }
        Scenario[] scenes = context.getScenes();
        if (scenes == null || scenes.length == 0) {
            return Scenario.DEFAULT.name();
        }
        if (scenes.length == 1) {
            return scenes[0].name();
        }
        return "MULTI";
    }

    /**
     * Get core instance (for interface default methods).
     *
     * @return Core instance
     */
    public S core() {
        return self();
    }

    /**
     * Get list of business causes.
     *
     * @return List of Business objects containing all error info
     */
    public List<Business> getCauses() {
        if (context != null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(errors);
    }

    public Business latestCause() {
        List<Business> list = context != null ? context.hasCauses() : errors;
        if (list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    private boolean hasReachedErrorLimit() {
        if (failFast) return false;
        int limit = resolveStrictMaxErrors();
        if (limit <= 0) return false;
        return errorSize() >= limit;
    }

    private int resolveStrictMaxErrors() {
        FailureContext ctx = Ex.getContext();
        if (ctx == null) return DEFAULT_STRICT_MAX_ERRORS;
        return ctx.getStrictMaxErrors();
    }

    private void markErrorLimitReached() {
        this.errorsTruncated = true;
        this.alive = false;
        this.conditionState = false;
        if (context != null) {
            context.stop();
        }
    }

    /**
     * Check if current object is valid.
     *
     * @return True if error collection is empty and object is alive, false otherwise
     */
    public boolean isValid() {
        if (context != null) {
            return context.isValid() && alive;
        }
        return errors.isEmpty() && alive;
    }

    /**
     * Stop subsequent validation if current has errors (set conditionState to false).
     *
     * @return Current chain instance
     */
    public S stopOnFail() {
        if (!conditionState) return self();
        if (isValid()) return self();
        return when(false);
    }

    /**
     * Resume validation (set conditionState to true).
     *
     * @return Current chain instance
     */
    public S resume() {
        return when(true);
    }

    /**
     * Validate only when specified scene is active.
     *
     * @param scene Scene to check
     * @return Current chain instance
     */
    public S whenScene(Scenario scene) {
        return when(hasScene(scene));
    }

    /**
     * Validate only when any of the specified scenes is active.
     *
     * @param scenes Scenes to check
     * @return Current chain instance
     */
    public S whenScene(Scenario... scenes) {
        return when(hasAnyScene(scenes));
    }

    /**
     * Validate within a scene-specific block.
     *
     * @param scene Scene to check
     * @param block Validation block
     * @return Current chain instance
     */
    public S inScene(Scenario scene, Consumer<S> block) {
        boolean originalState = conditionState;
        try {
            whenScene(scene);
            block.accept(self());
        } finally {
            conditionState = originalState;
        }
        return self();
    }

    /**
     * Validate within a scene-specific block.
     *
     * @param scenes Scenes to check
     * @param block  Validation block
     * @return Current chain instance
     */
    public S inScene(Scenario[] scenes, Consumer<S> block) {
        boolean originalState = conditionState;
        try {
            whenScene(scenes);
            block.accept(self());
        } finally {
            conditionState = originalState;
        }
        return self();
    }

    /**
     * Validate only when specified group is active.
     *
     * @param group Group to check
     * @return Current chain instance
     */
    public S whenGroup(Class<?> group) {
        return when(hasGroup(group));
    }

    /**
     * Validate only when any of the specified groups is active.
     *
     * @param groups Groups to check
     * @return Current chain instance
     */
    public S whenGroup(Class<?>... groups) {
        return when(hasAnyGroup(groups));
    }

    /**
     * Validate within a group-specific block.
     *
     * @param group Group to check
     * @param block Validation block
     * @return Current chain instance
     */
    public S inGroup(Class<?> group, Consumer<S> block) {
        boolean originalState = conditionState;
        try {
            whenGroup(group);
            block.accept(self());
        } finally {
            conditionState = originalState;
        }
        return self();
    }

    /**
     * Validate within a group-specific block.
     *
     * @param groups Groups to check
     * @param block  Validation block
     * @return Current chain instance
     */
    public S inGroup(Class<?>[] groups, Consumer<S> block) {
        boolean originalState = conditionState;
        try {
            whenGroup(groups);
            block.accept(self());
        } finally {
            conditionState = originalState;
        }
        return self();
    }

    // Helper methods for scene and group checking
    private boolean hasScene(Scenario scene) {
        if (context == null) {
            return scene == Scenario.DEFAULT;
        }
        Scenario[] scenes = context.getScenes();
        if (scenes == null || scenes.length == 0) {
            return scene == Scenario.DEFAULT;
        }
        for (Scenario s : scenes) {
            if (s == scene) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyScene(Scenario[] scenes) {
        if (context == null || scenes == null || scenes.length == 0) {
            return true;
        }
        Scenario[] contextScenes = context.getScenes();
        if (contextScenes == null || contextScenes.length == 0) {
            for (Scenario s : scenes) {
                if (s == Scenario.DEFAULT) {
                    return true;
                }
            }
            return false;
        }
        for (Scenario s1 : scenes) {
            for (Scenario s2 : contextScenes) {
                if (s1 == s2) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasGroup(Class<?> group) {
        if (context == null) {
            return false;
        }
        Class<?>[] groups = context.getGroups();
        if (groups == null || groups.length == 0) {
            return false;
        }
        for (Class<?> g : groups) {
            if (g == group) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyGroup(Class<?>[] groups) {
        if (context == null || groups == null || groups.length == 0) {
            return false;
        }
        Class<?>[] contextGroups = context.getGroups();
        if (contextGroups == null || contextGroups.length == 0) {
            return false;
        }
        for (Class<?> g1 : groups) {
            for (Class<?> g2 : contextGroups) {
                if (g1 == g2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Recursive validation with configurable options.
     *
     * @param object          Object to validate
     * @param typedValidator  TypedValidator instance
     * @param options         Recursive options
     * @return Current chain instance
     */
    public S recursive(Object object, TypedValidator typedValidator, RecursiveOption options) {
        if (shouldSkip()) return self();

        // Create validation context if not provided
        ValidationContext validationContext = context != null ? context : new ValidationContext(failFast);

        // Start recursive validation
        IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
        ObjectGraphWalker.walk(object, "", typedValidator, validationContext, options, 0, visited);

        if (validationContext.isFailed()) {
            if (context == null) {
                errors.addAll(validationContext.hasCauses());
                if (failFast && !errors.isEmpty()) {
                    alive = false;
                }
            }
        }

        return self();
    }

    /**
     * Print the current state of the chain to the standard logger.
     * Useful for debugging complex chains.
     *
     * @param msg A custom message to prefix the log
     * @return Current chain instance
     */
    public S console(String msg) {
        if (log.isDebugEnabled()) {
            log.debug("[FailFast] {}: valid={}, errorCount={}", msg, errors.isEmpty(), errors.size());
            for (Business err : errors) {
                log.debug("  -> {}", err.getMessage());
            }
        }
        return self();
    }

    /**
     * Expose the current state of the chain to a custom printer.
     *
     * @param printer Consumer to handle the status string
     * @return Current chain instance
     */
    public S print(Consumer<String> printer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chain status: ").append(errors.isEmpty() ? "VALID" : "INVALID").append("\n");
        sb.append("Error count: ").append(errors.size()).append("\n");
        for (Business err : errors) {
            sb.append("  - ").append(err.getDetail() != null ? err.getDetail() : err.getMessage()).append("\n");
        }
        printer.accept(sb.toString());
        return self();
    }
}
