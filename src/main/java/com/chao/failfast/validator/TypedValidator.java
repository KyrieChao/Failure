package com.chao.failfast.validator;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.core.ResponseCode;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * TypedValidator - Abstract generic validator class.
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public abstract class TypedValidator implements FastValidator<Object> {

    /**
     * Store validators for different types using ConcurrentHashMap.
     */
    private final Map<Class<?>, BiConsumer<Object, ValidationContext>> validators = new ConcurrentHashMap<>();
    private volatile Set<Class<?>> registeredTypesCache;

    /**
     * Constructor.
     */
    protected TypedValidator() {
        registerValidators();
    }

    /**
     * Subclasses implement this method to register validation logic.
     */
    protected void registerValidators() {
        // Default empty implementation
    }

    /**
     * Register a validation method for a type.
     *
     * @param type      Type class
     * @param validator Validation logic
     * @param <T>       Type
     */
    protected final <T> void register(Class<T> type, BiConsumer<T, ValidationContext> validator) {
        validators.put(type, (obj, ctx) -> validator.accept(type.cast(obj), ctx));
        if (validators.size() > 10) {
            registeredTypesCache = Set.copyOf(validators.keySet());
        } else {
            registeredTypesCache = null;
        }
    }


    /**
     * Get all registered types.
     *
     * @return Set of registered types
     */
    public Set<Class<?>> getRegisteredTypes() {
        Set<Class<?>> cached = registeredTypesCache;
        if (cached != null) return cached;
        Set<Class<?>> computed = Set.copyOf(validators.keySet());
        registeredTypesCache = computed;
        return computed;
    }

    public final boolean isRegisteredType(Class<?> type) {
        return validators.containsKey(type);
    }


    public final boolean validateIfRegistered(Object object, ValidationContext context) {
        if (object == null) return false;
        BiConsumer<Object, ValidationContext> handler = validators.get(object.getClass());
        if (handler == null) return false;
        handler.accept(object, context);
        return true;
    }

    @Override
    public Class<?> getSupportedType() {
        return validators.size() == 1 ? validators.keySet().iterator().next() : Object.class;
    }

    /**
     * 或直接用 size()
     */
    public int size() {
        return validators.size();
    }

    /**
     * Execute object validation.
     *
     * @param object  Target object to be validated
     * @param context Validation context
     */
    @Override
    public final void validate(Object object, ValidationContext context) {
        if (context == null) {
            context = new ValidationContext(true);
        }
        if (object == null) {
            context.reportError(ResponseCode.VALIDATION_ERROR_NULL);
            return;
        }
        BiConsumer<Object, ValidationContext> handler = validators.get(object.getClass());
        if (handler != null) {
            handler.accept(object, context);
        } else {
            String s = FailureConst.UNSUPPORTED_VALIDATION_TYPE + object.getClass().getSimpleName();
            context.reportError(ResponseCode.of(400, s));
        }
    }
}
