package com.chao.failfast.config.registry;

import com.chao.failfast.validator.FastValidator;
import com.chao.failfast.spi.validation.ValidatorRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultValidatorRegistryTest {

    @Test
    void testRegister() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<String> validator = (value, ctx) -> { };
        ValidatorRegistry result = registry.register(String.class, validator);
        assertSame(registry, result);
        assertNotNull(registry.getValidator(String.class));
    }

    @Test
    void testGetValidatorWithNullType() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        assertNull(registry.getValidator(null));
    }

    @Test
    void testGetValidatorWithNoValidators() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        assertNull(registry.getValidator(String.class));
    }

    @Test
    void testGetValidatorWithExactType() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<String> validator = (value, ctx) -> { };
        registry.register(String.class, validator);
        assertNotNull(registry.getValidator(String.class));
        assertNull(registry.getValidator(Integer.class));
    }

    @Test
    void testGetValidatorWithAssignableType() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<Number> validator = (value, ctx) -> { };
        registry.register(Number.class, validator);
        assertNotNull(registry.getValidator(Integer.class));
        assertNotNull(registry.getValidator(Double.class));
        assertNull(registry.getValidator(String.class));
    }

    @Test
    void testGetValidatorWithCache() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<String> validator = (value, ctx) -> { };
        registry.register(String.class, validator);
        // First call to populate cache
        FastValidator<Object> firstResult = registry.getValidator(String.class);
        assertNotNull(firstResult);
        // Second call should use cache
        FastValidator<Object> secondResult = registry.getValidator(String.class);
        assertSame(firstResult, secondResult);
    }

    @Test
    void testRegisterClearsCache() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<String> stringValidator = (value, ctx) -> { };
        registry.register(String.class, stringValidator);
        // Populate cache
        assertNotNull(registry.getValidator(String.class));
        // Add new validator
        FastValidator<Integer> integerValidator = (value, ctx) -> { };
        registry.register(Integer.class, integerValidator);
        // Cache should be cleared, so should still work
        assertNotNull(registry.getValidator(Integer.class));
    }

    @Test
    void testComputeBestHandlerWithExactMatch() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<String> stringValidator = (value, ctx) -> { };
        registry.register(String.class, stringValidator);
        FastValidator<Object> validator = registry.getValidator(String.class);
        assertNotNull(validator);
    }

    @Test
    void testComputeBestHandlerWithSuperclass() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<Number> numberValidator = (value, ctx) -> { };
        registry.register(Number.class, numberValidator);
        FastValidator<Object> validator = registry.getValidator(Integer.class);
        assertNotNull(validator);
    }

    @Test
    void testComputeBestHandlerWithInterface() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<CharSequence> charSequenceValidator = (value, ctx) -> { };
        registry.register(CharSequence.class, charSequenceValidator);
        FastValidator<Object> validator = registry.getValidator(String.class);
        assertNotNull(validator);
    }

    @Test
    void testComputeBestHandlerWithMultipleMatches() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<Number> numberValidator = (value, ctx) -> { };
        FastValidator<Object> objectValidator = (value, ctx) -> { };
        registry.register(Number.class, numberValidator);
        registry.register(Object.class, objectValidator);
        FastValidator<Object> validator = registry.getValidator(Integer.class);
        assertNotNull(validator);
    }
}
