package com.chao.failfast.integration.aot;

import com.chao.failfast.Failure;
import com.chao.failfast.autoconfigure.FailFastAutoConfiguration;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.exception.Business;
import com.chao.failfast.exception.MultiBusiness;
import com.chao.failfast.internal.core.Chain;
import com.chao.failfast.internal.core.Ex;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.result.Result;
import com.chao.failfast.util.ReflectionCache;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import java.util.List;

public class FailFastRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        registerTypes(hints);
        registerResources(hints);
    }

    private void registerTypes(RuntimeHints hints) {
        List<TypeReference> types = List.of(
                TypeReference.of(Failure.class),
                TypeReference.of(Chain.class),
                TypeReference.of(Ex.class),
                TypeReference.of(Result.class),
                TypeReference.of(Business.class),
                TypeReference.of(MultiBusiness.class),
                TypeReference.of(FailureContext.class),
                TypeReference.of(FailureProperties.class),
                TypeReference.of(ReflectionCache.class),
                TypeReference.of(FailFastAutoConfiguration.class)
        );
        for (TypeReference type : types) {
            hints.reflection().registerType(type);
        }
    }

    private void registerResources(RuntimeHints hints) {
        hints.resources().registerPattern("i18n/*");
        hints.resources().registerPattern("META-INF/spring/*");
    }
}

