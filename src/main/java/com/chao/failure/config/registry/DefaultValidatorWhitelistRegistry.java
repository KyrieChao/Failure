package com.chao.failure.config.registry;

import com.chao.failure.validator.FastValidator;
import com.chao.failure.spi.validation.ValidatorWhitelistRegistry;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultValidatorWhitelistRegistry implements ValidatorWhitelistRegistry {

    private final Set<String> whitelist = ConcurrentHashMap.newKeySet();

    @SafeVarargs
    @Override
    public final ValidatorWhitelistRegistry add(Class<? extends FastValidator<?>>... validatorTypes) {
        if (validatorTypes == null) return this;
        for (Class<? extends FastValidator<?>> t : validatorTypes) {
            if (t == null) continue;
            whitelist.add(t.getName());
        }
        return this;
    }

    @Override
    public boolean isWhitelisted(Class<? extends FastValidator<?>> validatorType) {
        if (validatorType == null) return false;
        return whitelist.contains(validatorType.getName());
    }
}
