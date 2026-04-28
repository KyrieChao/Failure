package com.chao.failure.config.registry;

import com.chao.failure.validator.FastValidator;
import com.chao.failure.spi.validation.ValidatorRegistry;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of ValidatorRegistry.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public class DefaultValidatorRegistry implements ValidatorRegistry {

    private final Map<Class<?>, FastValidator<?>> validators = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Object> resolvedHandlers = new ConcurrentHashMap<>();
    private static final Object NO_HANDLER = new Object();

    @Override
    public <T> ValidatorRegistry register(Class<T> type, FastValidator<T> validator) {
        validators.put(type, validator);
        resolvedHandlers.clear();
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public FastValidator<Object> getValidator(Class<?> type) {
        if (type == null) return null;
        Object cached = resolvedHandlers.get(type);
        if (cached != null) {
            return cached == NO_HANDLER ? null : (FastValidator<Object>) cached;
        }
        FastValidator<?> resolved = computeBestHandler(type);
        resolvedHandlers.put(type, resolved == null ? NO_HANDLER : resolved);
        return (FastValidator<Object>) resolved;
    }

    private FastValidator<?> computeBestHandler(Class<?> runtimeType) {
        FastValidator<?> exact = validators.get(runtimeType);
        if (exact != null) return exact;

        Class<?> bestType = null;
        FastValidator<?> bestHandler = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Map.Entry<Class<?>, FastValidator<?>> e : validators.entrySet()) {
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
