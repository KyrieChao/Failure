package com.chao.failfast.spi;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
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
    void testCustomImplementation() {
        SkipTypeRegistry skipTypeRegistry = Mockito.mock(SkipTypeRegistry.class);
        SkipPrefixRegistry skipPrefixRegistry = Mockito.mock(SkipPrefixRegistry.class);
        ValidatorRegistry validatorRegistry = Mockito.mock(ValidatorRegistry.class);

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
        };

        configurer.addValidationSkipTypes(skipTypeRegistry);
        configurer.addExceptionSkipPrefixes(skipPrefixRegistry);
        configurer.addCustomValidators(validatorRegistry);

        verify(skipTypeRegistry).add(String.class);
        verify(skipPrefixRegistry).add("com.example");
        verifyNoInteractions(validatorRegistry);
    }
}
