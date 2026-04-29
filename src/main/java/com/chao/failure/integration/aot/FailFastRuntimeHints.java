package com.chao.failure.integration.aot;

/**
 * FailFast runtime hints for AOT compilation.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */

import com.chao.failure.Failure;
import com.chao.failure.autoconfigure.FailFastAutoConfiguration;
import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.exception.Business;
import com.chao.failure.exception.MultiBusiness;
import com.chao.failure.internal.core.Chain;
import com.chao.failure.internal.core.Ex;
import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.result.Result;
import com.chao.failure.util.ReflectionCache;
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

