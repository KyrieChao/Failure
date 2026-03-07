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
    }


    /**
     * Get all registered types.
     *
     * @return Set of registered types
     */
    public Set<Class<?>> getRegisteredTypes() {
        return Set.copyOf(validators.keySet());
    }

    @Override
    public Class<?> getSupportedType() {
        // Return type directly if single type, otherwise return Object
        return validators.size() == 1 ? validators.keySet().iterator().next() : Object.class;
    }

    /**
     * Execute object validation.
     *
     * @param object  Target object to be validated
     * @param context Validation context
     */
    @Override
    public final void validate(Object object, ValidationContext context) {
        if (object == null) {
            context.reportError(ResponseCode.VALIDATION_ERROR_NULL);
            return;
        }
        // Find and execute validation handler for corresponding type
        BiConsumer<Object, ValidationContext> handler = validators.get(object.getClass());
        if (handler != null) {
            handler.accept(object, context);
        } else {
            // Handle unregistered types
            String s = FailureConst.UNSUPPORTED_VALIDATION_TYPE + object.getClass().getSimpleName();
            context.reportError(ResponseCode.of(400, s));
        }
    }
}
