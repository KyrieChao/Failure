package com.chao.failfast.config.registry;

import com.chao.failfast.spi.SkipPrefixRegistry;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of SkipPrefixRegistry.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
public class DefaultSkipPrefixRegistry implements SkipPrefixRegistry {

    private final Set<String> skipPrefixes = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<>();

    @Override
    public SkipPrefixRegistry add(String... prefixes) {
        if (prefixes != null) {
            Collections.addAll(skipPrefixes, prefixes);
        }
        cache.clear();
        return this;
    }

    @Override
    public boolean shouldSkip(String className) {
        if (className == null) return false;
        Boolean cached = cache.get(className);
        if (cached != null) {
            return cached;
        }
        boolean result = false;
        for (String prefix : skipPrefixes) {
            if (className.startsWith(prefix)) {
                result = true;
                break;
            }
        }
        cache.put(className, result);
        return result;
    }
}
