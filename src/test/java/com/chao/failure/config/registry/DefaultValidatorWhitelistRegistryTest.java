package com.chao.failure.config.registry;

import com.chao.failure.validator.FastValidator;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.spi.validation.ValidatorWhitelistRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultValidatorWhitelistRegistryTest {

    @Test
    void should_returnSameRegistry_when_validatorTypesArrayIsNull() {
        DefaultValidatorWhitelistRegistry registry = new DefaultValidatorWhitelistRegistry();

        ValidatorWhitelistRegistry result = registry.add((Class<? extends FastValidator<?>>[]) null);

        assertSame(registry, result);
    }

    @Test
    void should_ignoreNullValidatorType_when_mixedArgumentsProvided() {
        DefaultValidatorWhitelistRegistry registry = new DefaultValidatorWhitelistRegistry();

        ValidatorWhitelistRegistry result = registry.add(SampleValidator.class, null);

        assertSame(registry, result);
        assertTrue(registry.isWhitelisted(SampleValidator.class));
    }

    @Test
    void should_returnFalse_when_validatorTypeIsNull() {
        DefaultValidatorWhitelistRegistry registry = new DefaultValidatorWhitelistRegistry();

        boolean result = registry.isWhitelisted(null);

        assertFalse(result);
    }

    static class SampleValidator implements FastValidator<String> {
        @Override
        public void validate(String target, ValidationContext context) {
            if (target == null) {
                context.reportError(ResponseCode.VALIDATION_ERROR_400, "x");
            }
        }
    }
}
