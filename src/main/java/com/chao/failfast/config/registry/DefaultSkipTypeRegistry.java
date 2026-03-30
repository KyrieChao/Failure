package com.chao.failfast.config.registry;

import com.chao.failfast.spi.SkipTypeRegistry;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of SkipTypeRegistry.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
public class DefaultSkipTypeRegistry implements SkipTypeRegistry {

    private final Set<Class<?>> skipTypes = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ConcurrentHashMap<Class<?>, Boolean> cache = new ConcurrentHashMap<>();

    @Override
    public SkipTypeRegistry add(Class<?>... types) {
        if (types != null) {
            Collections.addAll(skipTypes, types);
        }
        cache.clear();
        return this;
    }

    @Override
    public boolean shouldSkip(Class<?> type) {
        if (type == null) return true;
        Boolean cached = cache.get(type);
        if (cached != null) {
            return cached;
        }
        boolean result = skipTypes.contains(type);
        if (!result) {
            for (Class<?> skipType : skipTypes) {
                if (skipType.isAssignableFrom(type)) {
                    result = true;
                    break;
                }
            }
        }
        cache.put(type, result);
        return result;
    }
}
