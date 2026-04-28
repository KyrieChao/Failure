package com.chao.failure.spi;

import com.chao.failure.validator.FastValidator;
import com.chao.failure.spi.config.FailFastConfigurer;
import com.chao.failure.spi.filter.SkipPrefixRegistry;
import com.chao.failure.spi.filter.SkipTypeRegistry;
import com.chao.failure.spi.i18n.LocalizedResponseResolver;
import com.chao.failure.spi.validation.ValidatorRegistry;
import com.chao.failure.spi.validation.ValidatorWhitelistRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class FailFastConfigurerTest {

    @Test
    void testDefaultAddValidationSkipTypes() {
        FailFastConfigurer configurer = new FailFastConfigurer() {
        };
        SkipTypeRegistry registry = Mockito.mock(SkipTypeRegistry.class);
        configurer.addValidationSkipTypes(registry);
        verifyNoInteractions(registry);
    }

    @Test
    void testDefaultAddExceptionSkipPrefixes() {
        FailFastConfigurer configurer = new FailFastConfigurer() {
        };
        SkipPrefixRegistry registry = Mockito.mock(SkipPrefixRegistry.class);
        configurer.addExceptionSkipPrefixes(registry);
        verifyNoInteractions(registry);
    }

    @Test
    void testDefaultAddCustomValidators() {
        FailFastConfigurer configurer = new FailFastConfigurer() {
        };
        ValidatorRegistry registry = Mockito.mock(ValidatorRegistry.class);
        configurer.addCustomValidators(registry);
        verifyNoInteractions(registry);
    }

    @Test
    void testDefaultAddValidatorWhitelist() {
        FailFastConfigurer configurer = new FailFastConfigurer() {
        };
        ValidatorWhitelistRegistry registry = Mockito.mock(ValidatorWhitelistRegistry.class);
        configurer.addValidatorWhitelist(registry);
        verifyNoInteractions(registry);
    }

    @Test
    void testDefaultCustomizeLocalizedResponseResolver() {
        FailFastConfigurer configurer = new FailFastConfigurer() {
        };
        LocalizedResponseResolver resolver = Mockito.mock(LocalizedResponseResolver.class);
        configurer.customizeLocalizedResponseResolver(resolver);
        verifyNoInteractions(resolver);
    }

    @Test
    void testCustomImplementation() {
        SkipTypeRegistry skipTypeRegistry = Mockito.mock(SkipTypeRegistry.class);
        SkipPrefixRegistry skipPrefixRegistry = Mockito.mock(SkipPrefixRegistry.class);
        ValidatorRegistry validatorRegistry = Mockito.mock(ValidatorRegistry.class);
        ValidatorWhitelistRegistry validatorWhitelistRegistry = Mockito.mock(ValidatorWhitelistRegistry.class);
        LocalizedResponseResolver localizedResponseResolver = Mockito.mock(LocalizedResponseResolver.class);

        FailFastConfigurer configurer = new FailFastConfigurer() {
            @Override
            public void addValidationSkipTypes(SkipTypeRegistry registry) {
                registry.add(String.class);
            }

            @Override
            public void addExceptionSkipPrefixes(SkipPrefixRegistry registry) {
                registry.add("com.example");
            }

            @Override
            public void addCustomValidators(ValidatorRegistry registry) {
                // No implementation needed for test
            }

            @Override
            public void addValidatorWhitelist(ValidatorWhitelistRegistry registry) {
                registry.add(GenericTestValidator.class);
            }

            @Override
            public void customizeLocalizedResponseResolver(LocalizedResponseResolver resolver) {
                // Custom implementation for test
                resolver.resolveMessage(null, null);
            }
        };

        configurer.addValidationSkipTypes(skipTypeRegistry);
        configurer.addExceptionSkipPrefixes(skipPrefixRegistry);
        configurer.addCustomValidators(validatorRegistry);
        configurer.addValidatorWhitelist(validatorWhitelistRegistry);
        configurer.customizeLocalizedResponseResolver(localizedResponseResolver);

        verify(skipTypeRegistry).add(String.class);
        verify(skipPrefixRegistry).add("com.example");
        verify(validatorWhitelistRegistry).add(GenericTestValidator.class);
        verify(localizedResponseResolver).resolveMessage(null, null);
        verifyNoInteractions(validatorRegistry);
    }

    static class GenericTestValidator implements FastValidator<String> {
        @Override
        public void validate(String value, ValidationContext ctx) {
        }

        @Override
        public Class<?> getSupportedType() {
            return String.class;
        }
    }
}
