package com.chao.failfast.validator;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TypedValidatorTest {

    @Test
    void testConstructorAndRegisterValidators() {
        TestTypedValidator validator = new TestTypedValidator();
        assertEquals(0, validator.size());
    }

    @Test
    void testRegister() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(String.class, (s, ctx) -> {
            if (s == null || s.isEmpty()) {
                ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL);
            }
        });
        assertEquals(1, validator.size());
        assertTrue(validator.isRegisteredType(String.class));
    }

    @Test
    void testRegisterMultipleTypes() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(String.class, (s, ctx) -> {});
        validator.register(Integer.class, (i, ctx) -> {});
        validator.register(Double.class, (d, ctx) -> {});
        assertEquals(3, validator.size());
        assertTrue(validator.isRegisteredType(String.class));
        assertTrue(validator.isRegisteredType(Integer.class));
        assertTrue(validator.isRegisteredType(Double.class));
    }

    @Test
    void testRegisterWithMoreThan10Types() {
        TestTypedValidator validator = new TestTypedValidator();
        // 使用不同的类型来注册验证�?
        validator.register(String.class, (s, ctx) -> {});
        validator.register(Integer.class, (i, ctx) -> {});
        validator.register(Double.class, (d, ctx) -> {});
        validator.register(Boolean.class, (b, ctx) -> {});
        validator.register(Long.class, (l, ctx) -> {});
        validator.register(Float.class, (f, ctx) -> {});
        validator.register(Short.class, (s, ctx) -> {});
        validator.register(Byte.class, (b, ctx) -> {});
        validator.register(Character.class, (c, ctx) -> {});
        validator.register(Void.class, (v, ctx) -> {});
        validator.register(Object.class, (o, ctx) -> {});
        assertEquals(11, validator.size());
        assertNotNull(validator.getRegisteredTypes());
    }

    @Test
    void testGetRegisteredTypes() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(String.class, (s, ctx) -> {});
        validator.register(Integer.class, (i, ctx) -> {});
        var types = validator.getRegisteredTypes();
        assertEquals(2, types.size());
        assertTrue(types.contains(String.class));
        assertTrue(types.contains(Integer.class));
    }

    @Test
    void testIsRegisteredType() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(String.class, (s, ctx) -> {});
        assertTrue(validator.isRegisteredType(String.class));
        assertFalse(validator.isRegisteredType(Integer.class));
    }

    @Test
    void testValidateIfRegisteredWithNullObject() {
        TestTypedValidator validator = new TestTypedValidator();
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        assertFalse(validator.validateIfRegistered(null, context));
    }

    @Test
    void testValidateIfRegisteredWithUnregisteredType() {
        TestTypedValidator validator = new TestTypedValidator();
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        assertFalse(validator.validateIfRegistered("test", context));
    }

    @Test
    void testValidateIfRegisteredWithRegisteredType() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(String.class, (s, ctx) -> {
            if (s.isEmpty()) {
                ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL);
            }
        });
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        assertTrue(validator.validateIfRegistered("test", context));
    }

    @Test
    void testGetSupportedTypeWithSingleType() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(String.class, (s, ctx) -> {});
        assertEquals(String.class, validator.getSupportedType());
    }

    @Test
    void testGetSupportedTypeWithMultipleTypes() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(String.class, (s, ctx) -> {});
        validator.register(Integer.class, (i, ctx) -> {});
        assertEquals(Object.class, validator.getSupportedType());
    }

    @Test
    void testSize() {
        TestTypedValidator validator = new TestTypedValidator();
        assertEquals(0, validator.size());
        validator.register(String.class, (s, ctx) -> {});
        assertEquals(1, validator.size());
    }

    @Test
    void testValidateWithNullContext() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(String.class, (s, ctx) -> {});
        validator.validate("test", null);
    }

    @Test
    void testValidateWithNullObject() {
        TestTypedValidator validator = new TestTypedValidator();
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate(null, context);
        assertFalse(context.isValid());
    }

    @Test
    void testValidateWithUnregisteredType() {
        TestTypedValidator validator = new TestTypedValidator();
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate("test", context);
        assertFalse(context.isValid());
    }

    @Test
    void testValidateWithRegisteredType() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(String.class, (s, ctx) -> {
            if (s.isEmpty()) {
                ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL);
            }
        });
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate("test", context);
        assertTrue(context.isValid());
    }

    @Test
    void testResolveHandlerWithCachedHandler() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(String.class, (s, ctx) -> {});
        // First call to cache the handler
        validator.validate("test", new FastValidator.ValidationContext(true));
        // Second call should use cached handler
        validator.validate("test", new FastValidator.ValidationContext(true));
    }

    @Test
    void testResolveHandlerWithNoHandler() {
        TestTypedValidator validator = new TestTypedValidator();
        // First call to cache NO_HANDLER
        validator.validate(123, new FastValidator.ValidationContext(true));
        // Second call should use cached NO_HANDLER
        validator.validate(123, new FastValidator.ValidationContext(true));
    }

    @Test
    void testComputeBestHandlerWithExactMatch() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(String.class, (s, ctx) -> {});
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate("test", context);
        assertTrue(context.isValid());
    }

    @Test
    void testComputeBestHandlerWithSuperclassMatch() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(Number.class, (n, ctx) -> {});
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate(123, context);
        assertTrue(context.isValid());
    }

    @Test
    void testComputeBestHandlerWithInterfaceMatch() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(CharSequence.class, (cs, ctx) -> {});
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate("test", context);
        assertTrue(context.isValid());
    }

    @Test
    void testComputeBestHandlerWithMultipleMatches() {
        TestTypedValidator validator = new TestTypedValidator();
        validator.register(Number.class, (n, ctx) -> {});
        validator.register(Object.class, (o, ctx) -> {});
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate(123, context);
        assertTrue(context.isValid());
    }

    @Test
    void testDistanceWithSameClass() {
        TestTypedValidator validator = new TestTypedValidator();
        assertEquals(0, validator.distance(String.class, String.class));
    }

    @Test
    void testDistanceWithSuperclass() {
        TestTypedValidator validator = new TestTypedValidator();
        assertEquals(1, validator.distance(String.class, Object.class));
    }

    @Test
    void testDistanceWithInterface() {
        TestTypedValidator validator = new TestTypedValidator();
        assertEquals(1, validator.distance(String.class, CharSequence.class));
    }

    @Test
    void testDistanceWithNullClasses() {
        TestTypedValidator validator = new TestTypedValidator();
        assertEquals(Integer.MAX_VALUE, validator.distance(null, String.class));
        assertEquals(Integer.MAX_VALUE, validator.distance(String.class, null));
        assertEquals(Integer.MAX_VALUE, validator.distance(null, null));
    }

    @Test
    void testDistanceWithUnrelatedClasses() {
        TestTypedValidator validator = new TestTypedValidator();
        assertEquals(Integer.MAX_VALUE, validator.distance(String.class, Integer.class));
    }

    @Test
    void testComputeBestHandlerWithSameDistance() {
        TestTypedValidator validator = new TestTypedValidator();
        // Register two interfaces that are both implemented by String
        validator.register(CharSequence.class, (cs, ctx) -> {});
        validator.register(Comparable.class, (c, ctx) -> {});
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate("test", context);
        assertTrue(context.isValid());
    }

    @Test
    void testDistanceWithCachedClasses() {
        TestTypedValidator validator = new TestTypedValidator();
        // This test ensures that the distance method handles cached classes correctly
        assertEquals(1, validator.distance(String.class, Object.class));
    }

    @Test
    void testComputeBestHandlerWithInterfaceVsClass() {
        TestTypedValidator validator = new TestTypedValidator();
        // Register an interface and a class
        validator.register(CharSequence.class, (cs, ctx) -> {});
        validator.register(Object.class, (o, ctx) -> {});
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate("test", context);
        assertTrue(context.isValid());
    }

    @Test
    void testComputeBestHandlerPrefersClassOverInterfaceWhenSameDistance() throws Exception {
        interface AA {
        }
        interface BB {
        }
        class ImplementsBoth implements AA, BB {
        }

        class LocalTypedValidator extends TypedValidator {
            @Override
            protected void registerValidators() {
            }
        }

        LocalTypedValidator validator = new LocalTypedValidator();

        var field = TypedValidator.class.getDeclaredField("validators");
        field.setAccessible(true);
        java.util.LinkedHashMap<Class<?>, java.util.function.BiConsumer<Object, FastValidator.ValidationContext>> ordered = new java.util.LinkedHashMap<>();
        ordered.put(BB.class, (obj, ctx) -> ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL));
        ordered.put(AA.class, (obj, ctx) -> {
        });
        field.set(validator, ordered);
        Object current = field.get(validator);
        assertTrue(current instanceof java.util.LinkedHashMap);
        assertEquals(BB.class, ((java.util.Map<?, ?>) current).keySet().iterator().next());

        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate(new ImplementsBoth(), context);
        assertTrue(context.isValid());
    }

    @Test
    void testDistanceWithDiamondInterfacesCoversDuplicateInterfaceVisit() {
        interface D {
        }
        interface B extends D {
        }
        interface C extends D {
        }
        interface A extends B, C {
        }

        TestTypedValidator validator = new TestTypedValidator();
        assertEquals(2, validator.distance(A.class, D.class));
    }

    @Test
    void testComputeBestHandlerTieBreakByNameForInterfaces() throws Exception {
        interface X {
        }
        interface Y {
        }
        class Impl implements X, Y {
        }

        class LocalTypedValidator extends TypedValidator {
            @Override
            protected void registerValidators() {
            }
        }
        LocalTypedValidator validator = new LocalTypedValidator();

        var field = TypedValidator.class.getDeclaredField("validators");
        field.setAccessible(true);
        java.util.LinkedHashMap<Class<?>, java.util.function.BiConsumer<Object, FastValidator.ValidationContext>> ordered = new java.util.LinkedHashMap<>();
        Class<?> smaller = X.class.getName().compareTo(Y.class.getName()) < 0 ? X.class : Y.class;
        Class<?> larger = smaller == X.class ? Y.class : X.class;
        ordered.put(larger, (obj, ctx) -> ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL));
        ordered.put(smaller, (obj, ctx) -> {
        });
        field.set(validator, ordered);

        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate(new Impl(), context);
        assertTrue(context.isValid());
    }

    @Test
    void testComputeBestHandlerPrefersClassWhenTieWithInterface() throws Exception {
        interface I {
        }
        class Base {
        }
        class Impl extends Base implements I {
        }

        class LocalTypedValidator extends TypedValidator {
            @Override
            protected void registerValidators() {
            }
        }
        LocalTypedValidator validator = new LocalTypedValidator();

        var field = TypedValidator.class.getDeclaredField("validators");
        field.setAccessible(true);
        java.util.LinkedHashMap<Class<?>, java.util.function.BiConsumer<Object, FastValidator.ValidationContext>> ordered = new java.util.LinkedHashMap<>();
        ordered.put(I.class, (obj, ctx) -> ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL));
        ordered.put(Base.class, (obj, ctx) -> {
        });
        field.set(validator, ordered);

        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate(new Impl(), context);
        assertTrue(context.isValid());
    }

    @Test
    void testComputeBestHandlerKeepsClassWhenTieAndClassAlreadyBest() throws Exception {
        interface I {
        }
        class Base {
        }
        class Impl extends Base implements I {
        }

        class LocalTypedValidator extends TypedValidator {
            @Override
            protected void registerValidators() {
            }
        }
        LocalTypedValidator validator = new LocalTypedValidator();

        var field = TypedValidator.class.getDeclaredField("validators");
        field.setAccessible(true);
        java.util.LinkedHashMap<Class<?>, java.util.function.BiConsumer<Object, FastValidator.ValidationContext>> ordered = new java.util.LinkedHashMap<>();
        ordered.put(Base.class, (obj, ctx) -> ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL));
        ordered.put(I.class, (obj, ctx) -> {
        });
        field.set(validator, ordered);

        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate(new Impl(), context);
        assertFalse(context.isValid());
    }

    @Test
    void testComputeBestHandlerSkipsWorseDistanceCandidate() throws Exception {
        class Base {
        }
        class Impl extends Base {
        }

        class LocalTypedValidator extends TypedValidator {
            @Override
            protected void registerValidators() {
            }
        }
        LocalTypedValidator validator = new LocalTypedValidator();

        var field = TypedValidator.class.getDeclaredField("validators");
        field.setAccessible(true);
        java.util.LinkedHashMap<Class<?>, java.util.function.BiConsumer<Object, FastValidator.ValidationContext>> ordered = new java.util.LinkedHashMap<>();
        ordered.put(Base.class, (obj, ctx) -> {
        });
        ordered.put(Object.class, (obj, ctx) -> ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL));
        field.set(validator, ordered);

        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate(new Impl(), context);
        assertTrue(context.isValid());
    }

    @Test
    void testComputeBestHandlerUpdatesWhenFindsCloserTypeLater() throws Exception {
        class Base {
        }
        class Impl extends Base {
        }

        class LocalTypedValidator extends TypedValidator {
            @Override
            protected void registerValidators() {
            }
        }
        LocalTypedValidator validator = new LocalTypedValidator();

        var field = TypedValidator.class.getDeclaredField("validators");
        field.setAccessible(true);
        java.util.LinkedHashMap<Class<?>, java.util.function.BiConsumer<Object, FastValidator.ValidationContext>> ordered = new java.util.LinkedHashMap<>();
        ordered.put(Object.class, (obj, ctx) -> ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL));
        ordered.put(Base.class, (obj, ctx) -> {
        });
        field.set(validator, ordered);

        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate(new Impl(), context);
        assertTrue(context.isValid());
    }

    @Test
    void testComputeBestHandlerTieBreakDoesNotUpdateWhenNameNotSmaller() throws Exception {
        interface X {
        }
        interface Y {
        }
        class Impl implements X, Y {
        }

        class LocalTypedValidator extends TypedValidator {
            @Override
            protected void registerValidators() {
            }
        }
        LocalTypedValidator validator = new LocalTypedValidator();

        var field = TypedValidator.class.getDeclaredField("validators");
        field.setAccessible(true);
        java.util.LinkedHashMap<Class<?>, java.util.function.BiConsumer<Object, FastValidator.ValidationContext>> ordered = new java.util.LinkedHashMap<>();
        Class<?> smaller = X.class.getName().compareTo(Y.class.getName()) < 0 ? X.class : Y.class;
        Class<?> larger = smaller == X.class ? Y.class : X.class;
        ordered.put(smaller, (obj, ctx) -> ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL));
        ordered.put(larger, (obj, ctx) -> {
        });
        field.set(validator, ordered);

        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
        validator.validate(new Impl(), context);
        assertFalse(context.isValid());
    }

    @Test
    void testRegisterWithMoreThan10TypesUpdatesCache() {
        TestTypedValidator validator = new TestTypedValidator();
        // Register 11 types to trigger cache update
        validator.register(String.class, (s, ctx) -> {});
        validator.register(Integer.class, (i, ctx) -> {});
        validator.register(Double.class, (d, ctx) -> {});
        validator.register(Boolean.class, (b, ctx) -> {});
        validator.register(Long.class, (l, ctx) -> {});
        validator.register(Float.class, (f, ctx) -> {});
        validator.register(Short.class, (s, ctx) -> {});
        validator.register(Byte.class, (b, ctx) -> {});
        validator.register(Character.class, (c, ctx) -> {});
        validator.register(Void.class, (v, ctx) -> {});
        validator.register(Object.class, (o, ctx) -> {});
        // Get registered types to ensure cache is used
        Set<Class<?>> types = validator.getRegisteredTypes();
        assertEquals(11, types.size());
    }

    @Test
    void testRegisterWithLessThan11TypesDoesNotCache() {
        TestTypedValidator validator = new TestTypedValidator();
        // Register 10 types (should not trigger cache)
        validator.register(String.class, (s, ctx) -> {});
        validator.register(Integer.class, (i, ctx) -> {});
        validator.register(Double.class, (d, ctx) -> {});
        validator.register(Boolean.class, (b, ctx) -> {});
        validator.register(Long.class, (l, ctx) -> {});
        validator.register(Float.class, (f, ctx) -> {});
        validator.register(Short.class, (s, ctx) -> {});
        validator.register(Byte.class, (b, ctx) -> {});
        validator.register(Character.class, (c, ctx) -> {});
        validator.register(Void.class, (v, ctx) -> {});
        // Get registered types (should compute each time)
        Set<Class<?>> types = validator.getRegisteredTypes();
        assertEquals(10, types.size());
    }

    @Test
    void testValidateWithRegisteredTypeTriggersValidation() {
        TestTypedValidator validator = new TestTypedValidator();
        // Register a validator that reports error for empty strings
        validator.register(String.class, (s, ctx) -> {
            if (s.isEmpty()) {
                ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL);
            }
        });
        // Test with empty string (should fail)
        FastValidator.ValidationContext context1 = new FastValidator.ValidationContext(true);
        validator.validate("", context1);
        assertFalse(context1.isValid());
        // Test with non-empty string (should pass)
        FastValidator.ValidationContext context2 = new FastValidator.ValidationContext(true);
        validator.validate("test", context2);
        assertTrue(context2.isValid());
    }

    @Test
    void testValidateIfRegisteredWithRegisteredTypeTriggersValidation() {
        TestTypedValidator validator = new TestTypedValidator();
        // Register a validator that reports error for empty strings
        validator.register(String.class, (s, ctx) -> {
            if (s.isEmpty()) {
                ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL);
            }
        });
        // Test with empty string (should trigger validation and return true)
        FastValidator.ValidationContext context1 = new FastValidator.ValidationContext(true);
        boolean result1 = validator.validateIfRegistered("", context1);
        assertTrue(result1);
        assertFalse(context1.isValid());
        // Test with non-empty string (should trigger validation and return true)
        FastValidator.ValidationContext context2 = new FastValidator.ValidationContext(true);
        boolean result2 = validator.validateIfRegistered("test", context2);
        assertTrue(result2);
        assertTrue(context2.isValid());
    }

    @Test
    void testDistanceWithDeepInheritance() {
        class Grandparent {
        }
        class Parent extends Grandparent {
        }
        class Child extends Parent {
        }

        TestTypedValidator validator = new TestTypedValidator();
        assertEquals(2, validator.distance(Child.class, Grandparent.class));
    }

    // Test implementation of TypedValidator
    private static class TestTypedValidator extends TypedValidator {
        @Override
        protected void registerValidators() {
            // Default implementation
        }

        // Expose distance method for testing
        public int distance(Class<?> from, Class<?> to) {
            try {
                var method = TypedValidator.class.getDeclaredMethod("distance", Class.class, Class.class);
                method.setAccessible(true);
                return (int) method.invoke(this, from, to);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
