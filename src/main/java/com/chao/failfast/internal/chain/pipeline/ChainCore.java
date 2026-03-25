package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.annotation.ToImprove;
import com.chao.failfast.constant.Scenario;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.Ex;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.validation.RecursiveOptions;
import com.chao.failfast.internal.validation.ValidationObservers;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.internal.policy.DefaultErrorPolicy;
import com.chao.failfast.internal.policy.ErrorPolicy;
import com.chao.failfast.validator.TypedValidator;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Validation chain core - Manage state (failFast, alive, errors, context).
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.2.0
 */
@ToImprove(value = "代码过长待优化",version = "1.2.0",tag = "1.8.0")
public abstract class ChainCore<S extends ChainCore<S>> {


    /**
     * Notify observer of validation start.
     *
     * @param source validation source
     * @param scene validation scene
     */
    protected void notifyValidationStart(String source, String scene) {
        ValidationObservers.notifyStart(source, scene);
    }

    /**
     * Notify observer of validation end.
     *
     * @param source validation source
     * @param durationNanos duration in nanoseconds
     * @param success whether validation was successful
     */
    protected void notifyValidationEnd(String source, long durationNanos, boolean success) {
        ValidationObservers.notifyEnd(source, durationNanos, success);
    }

    /**
     * Notify observer of validation failure.
     *
     * @param source validation source
     * @param errorCode error code
     */
    protected void notifyValidationFailure(String source, String errorCode) {
        ValidationObservers.notifyFailure(source, errorCode);
    }

    /**
     * Notify observer of violation.
     *
     * @param source validation source
     * @param constraint constraint name
     */
    protected void notifyViolation(String source, String constraint) {
        ValidationObservers.notifyViolation(source, constraint);
    }

    @Getter
    protected final boolean failFast;
    @Getter
    protected boolean alive = true;
    // Dynamic skip state (true=execute, false=skip)
    private boolean conditionState = true;
    // OR state
    private boolean orMode = false;
    private boolean orHasSuccess = false;
    protected final ValidationContext context;
    protected final List<Business> errors = new ArrayList<>();

    protected ChainCore(boolean failFast, ValidationContext context) {
        this.failFast = failFast;
        this.context = context;
    }

    public int errorSize() {
        if (context != null) return context.errorSize();
        return errors.size();
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
                addError(code, detail, resolveInvalidValue(invalidValueSupplier), null);
                if (failFast) alive = false;
            } else {
                alive = true;
            }
        } else {
            if (!condition) {
                addError(code, detail, resolveInvalidValue(invalidValueSupplier), null);
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

        boolean success = true;
        try {
            if (orMode) {
                // OR mode: calculate combined result
                orMode = false;  // Consume or state
                boolean finalSuccess = orHasSuccess || condition;

                if (!finalSuccess) {
                    // Both failed, report error
                    addError(code, detail, value, null);
                    if (failFast) alive = false;
                    success = false;
                } else {
                    // One success, whole or passed, clear errors
                    alive = true;
                    // errors already cleared in or()
                }
            } else {
                // Normal mode
                if (!condition) {
                    addError(code, detail, value, null);
                    if (failFast) alive = false;
                    success = false;
                }
            }
            return self();
        } finally {
            // Metrics moved to validation action boundaries
        }
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

    protected void addError(ResponseCode code, String detail, Object value, String path) {
        addError(code, detail, value, path, null, "chain");
    }

    protected void addError(ResponseCode code, String detail, Object value, String path, String constraint) {
        addError(code, detail, value, path, constraint, "chain");
    }

    protected void addError(ResponseCode code, String detail, Object value, String path, String constraint, String source) {
        Business business = buildBusiness(code, detail, value, path, constraint);

        if (context != null) {
            context.reportError(business);
            if (failFast) context.stop();
        } else {
            errors.add(business);
        }

        // Notify observer of violation
        if (constraint != null) {
            ValidationObservers.notifyViolation(source, constraint);
        }
    }

    /**
     * Get scene name for observability.
     *
     * @return scene name
     */
    private String getSceneName() {
        if (context != null) {
            Scenario[] scenes = context.getScenes();
            if (scenes != null && scenes.length > 0) {
                if (scenes.length == 1) {
                    return scenes[0].name();
                } else {
                    // Multiple scenes, return "MULTI"
                    return "MULTI";
                }
            }
        }
        return Scenario.DEFAULT.name();
    }

    private Business buildBusiness(ResponseCode code, String detail, Object value, String path) {
        return buildBusiness(code, detail, value, path, null);
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
        return new ArrayList<>(errors);
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
    public S recursive(Object object, TypedValidator typedValidator, RecursiveOptions options) {
        if (shouldSkip()) return self();
        
        // Create validation context if not provided
        ValidationContext validationContext = context != null ? context : new ValidationContext(failFast);
        
        // Start recursive validation
        IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
        recursiveValidate(object, "", typedValidator, validationContext, options, 0, visited);
        
        // Add errors to current chain
        if (validationContext.isFailed()) {
            if (context != null) {
                // Context already has errors
            } else {
                errors.addAll(validationContext.hasCauses());
                if (failFast && !errors.isEmpty()) {
                    alive = false;
                }
            }
        }
        
        return self();
    }

    /**
     * Recursive validation helper method.
     */
    private void recursiveValidate(Object object, String path, TypedValidator typedValidator, 
                                 ValidationContext context, RecursiveOptions options, 
                                 int depth, IdentityHashMap<Object, Boolean> visited) {
        // Check if validation should stop
        if (context.isStopped() || depth > options.getMaxDepth() || 
            (context.errorSize() >= options.getMaxErrors())) {
            return;
        }
        
        // Check for circular reference
        if (object != null && visited.containsKey(object)) {
            return;
        }
        
        // Mark as visited
        if (object != null) {
            visited.put(object, Boolean.TRUE);
        }
        
        try {
            // Check if object is null
            if (object == null) {
                return;
            }
            
            // Check if path is excluded
            if (isExcluded(path, options.getExclude())) {
                return;
            }
            
            // Check if path is included (if include list is specified)
            if (!isIncluded(path, options.getInclude())) {
                return;
            }
            
            // Validate current object if there's a validator for it
            boolean validated = typedValidator.validateIfRegistered(object, context);
            
            // If object was validated, don't recurse further
            if (validated) {
                return;
            }
            
            // Recurse into collections
            if (object instanceof Collection<?> collection) {
                int index = 0;
                for (Object item : collection) {
                    if (index >= options.getMaxItems()) {
                        context.reportError(ResponseCode.VALIDATION_ERROR_400, "Collection size exceeds limit");
                        break;
                    }
                    String itemPath = path.isEmpty() ? "[" + index + "]" : path + "[" + index + "]";
                    recursiveValidate(item, itemPath, typedValidator, context, options, depth + 1, visited);
                    index++;
                }
            }
            // Recurse into maps
            else if (object instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    String entryPath = path.isEmpty() ? "[" + key + "]" : path + "[" + key + "]";
                    recursiveValidate(value, entryPath, typedValidator, context, options, depth + 1, visited);
                }
            }
            // Recurse into arrays
            else if (object.getClass().isArray()) {
                if (object instanceof Object[]) {
                    // Object array
                    Object[] array = (Object[]) object;
                    for (int i = 0; i < array.length; i++) {
                        if (i >= options.getMaxItems()) {
                            context.reportError(ResponseCode.VALIDATION_ERROR_400, "Array size exceeds limit");
                            break;
                        }
                        String itemPath = path.isEmpty() ? "[" + i + "]" : path + "[" + i + "]";
                        recursiveValidate(array[i], itemPath, typedValidator, context, options, depth + 1, visited);
                    }
                } else {
                    // Primitive array (int[], long[], etc.)
                    // Just validate the array itself, don't recurse into primitive elements
                    // as they don't have fields to validate
                }
            }
            // Recurse into POJOs using reflection
            else if (!isPrimitiveOrWrapper(object.getClass()) && !isStringOrEnum(object.getClass())) {
                java.lang.reflect.Field[] fields = object.getClass().getDeclaredFields();
                for (java.lang.reflect.Field field : fields) {
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    Object fieldValue;
                    try {
                        field.setAccessible(true);
                        fieldValue = field.get(object);
                    } catch (Exception e) {
                        continue;
                    }

                    String fieldName = field.getName();
                    String fieldPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                    recursiveValidate(fieldValue, fieldPath, typedValidator, context, options, depth + 1, visited);
                }
            }
        } finally {
            // Remove from visited to allow re-validation in different paths
            if (object != null) {
                visited.remove(object);
            }
        }
    }

    /**
     * Check if a class is a primitive type or its wrapper.
     */
    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() || 
               clazz == Boolean.class ||
               clazz == Byte.class ||
               clazz == Character.class ||
               clazz == Double.class ||
               clazz == Float.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Short.class ||
               clazz == Void.class;
    }

    /**
     * Check if a class is String or Enum.
     */
    private boolean isStringOrEnum(Class<?> clazz) {
        return clazz == String.class || clazz.isEnum();
    }

    /**
     * Check if path is excluded.
     */
    private boolean isExcluded(String path, List<String> exclude) {
        if (exclude == null || exclude.isEmpty()) {
            return false;
        }
        for (String excluded : exclude) {
            if (path.startsWith(excluded)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if path is included (if include list is specified).
     */
    private boolean isIncluded(String path, List<String> include) {
        if (include == null || include.isEmpty()) {
            return true;
        }
        for (String included : include) {
            if (path.startsWith(included)) {
                return true;
            }
        }
        return false;
    }
}
