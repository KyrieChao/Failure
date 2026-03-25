package com.chao.failfast.validator;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.core.ResponseCode;

import java.util.Map;
import java.util.Set;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * TypedValidator - Abstract generic validator class.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
public abstract class TypedValidator implements FastValidator<Object> {

    /**
     * Store validators for different types using ConcurrentHashMap.
     */
    private final Map<Class<?>, BiConsumer<Object, ValidationContext>> validators = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Object> resolvedHandlers = new ConcurrentHashMap<>();
    private static final Object NO_HANDLER = new Object();
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
        resolvedHandlers.clear();
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
        BiConsumer<Object, ValidationContext> handler = resolveHandler(object.getClass());
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
            context = new FastValidator.ValidationContext(true);
        }
        if (object == null) {
            context.reportError(ResponseCode.VALIDATION_ERROR_NULL);
            return;
        }
        BiConsumer<Object, ValidationContext> handler = resolveHandler(object.getClass());
        if (handler != null) {
            handler.accept(object, context);
        } else {
            String s = FailureConst.UNSUPPORTED_VALIDATION_TYPE + object.getClass().getSimpleName();
            context.reportError(ResponseCode.of(400, s));
        }
    }

    @SuppressWarnings("unchecked")
    private BiConsumer<Object, ValidationContext> resolveHandler(Class<?> runtimeType) {
        Object cached = resolvedHandlers.get(runtimeType);
        if (cached != null) {
            return cached == NO_HANDLER ? null : (BiConsumer<Object, ValidationContext>) cached;
        }
        BiConsumer<Object, ValidationContext> resolved = computeBestHandler(runtimeType);
        resolvedHandlers.put(runtimeType, resolved == null ? NO_HANDLER : resolved);
        return resolved;
    }

    private BiConsumer<Object, ValidationContext> computeBestHandler(Class<?> runtimeType) {
        BiConsumer<Object, ValidationContext> exact = validators.get(runtimeType);
        if (exact != null) return exact;

        Class<?> bestType = null;
        BiConsumer<Object, ValidationContext> bestHandler = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Map.Entry<Class<?>, BiConsumer<Object, ValidationContext>> e : validators.entrySet()) {
            Class<?> registeredType = e.getKey();
            if (!registeredType.isAssignableFrom(runtimeType)) continue;
            int distance = distance(runtimeType, registeredType);
            if (bestHandler == null) {
                bestDistance = distance;
                bestType = registeredType;
                bestHandler = e.getValue();
                continue;
            }
            if (distance < bestDistance) {
                bestDistance = distance;
                bestType = registeredType;
                bestHandler = e.getValue();
            } else if (distance == bestDistance) {
                boolean bestIsInterface = bestType.isInterface();
                boolean curIsInterface = registeredType.isInterface();

                if (bestIsInterface != curIsInterface) {
                    if (bestIsInterface) {
                        bestType = registeredType;
                        bestHandler = e.getValue();
                    }
                } else {
                    if (registeredType.getName().compareTo(bestType.getName()) < 0) {
                        bestType = registeredType;
                        bestHandler = e.getValue();
                    }
                }
            }
        }

        return bestHandler;
    }

    private static int distance(Class<?> from, Class<?> to) {
        if (from == null || to == null) return Integer.MAX_VALUE;
        if (from.equals(to)) return 0;

        ArrayDeque<Class<?>> q = new ArrayDeque<>();
        HashMap<Class<?>, Integer> dist = new HashMap<>();
        q.add(from);
        dist.put(from, 0);

        while (!q.isEmpty()) {
            Class<?> cur = q.poll();
            int d = dist.get(cur);

            Class<?> sup = cur.getSuperclass();
            if (sup != null) {
                if (sup.equals(to)) return d + 1;
                dist.putIfAbsent(sup, d + 1);
                q.add(sup);
            }
            for (Class<?> i : cur.getInterfaces()) {
                if (i.equals(to)) return d + 1;
                dist.putIfAbsent(i, d + 1);
                q.add(i);
            }
        }

        return Integer.MAX_VALUE;
    }
}
