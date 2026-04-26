package com.chao.failfast.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TypedValidatorOptimizationTest {

    static class OneTypeValidator extends TypedValidator {
        final AtomicInteger calls = new AtomicInteger();

        @Override
        protected void registerValidators() {
            register(String.class, (s, ctx) -> calls.incrementAndGet());
        }
    }

    static class ExtensibleValidator extends TypedValidator {
        @Override
        protected void registerValidators() {
            register(String.class, (s, ctx) -> {});
            register(Character.class, (i, ctx) -> {});
            register(Boolean.class, (b, ctx) -> {});
            register(Byte.class, (b, ctx) -> {});
            register(Short.class, (s, ctx) -> {});
            register(Long.class, (l, ctx) -> {});
            register(Float.class, (f, ctx) -> {});
            register(Double.class, (d, ctx) -> {});
            register(CharSequence.class, (cs, ctx) -> {});
            register(Number.class, (n, ctx) -> {});
            register(Iterable.class, (i, ctx) -> {});
            register(Map.class, (m, ctx) -> {});
        }

        public void addInteger() {
            register(Integer.class, (i, ctx) -> {
            });
        }
    }

    @Test
    void shouldCacheRegisteredTypes() {
        OneTypeValidator v = new OneTypeValidator();
        Set<Class<?>> a = v.getRegisteredTypes();
        Set<Class<?>> b = v.getRegisteredTypes();
        assertThat(a).isSameAs(b);
        assertThat(a).containsExactly(String.class);
    }

    @Test
    void shouldInvalidateRegisteredTypesCacheOnRegister() {
        ExtensibleValidator v = new ExtensibleValidator();
        Set<Class<?>> a = v.getRegisteredTypes();
        v.addInteger();
        Set<Class<?>> b = v.getRegisteredTypes();
        assertThat(b).contains(String.class, Integer.class);
        Assertions.assertEquals(13, v.size());
        assertThat(b).isNotSameAs(a);
    }

    @Test
    void shouldValidateOnlyWhenRegistered() {
        OneTypeValidator v = new OneTypeValidator();
        FastValidator.ValidationContext ctx = new FastValidator.ValidationContext(true);

        boolean ok1 = v.validateIfRegistered("x", ctx);
        boolean ok2 = v.validateIfRegistered(1, ctx);
        boolean ok3 = v.validateIfRegistered(null, ctx);

        assertThat(ok1).isTrue();
        assertThat(ok2).isFalse();
        assertThat(ok3).isFalse();
        assertThat(v.calls.get()).isEqualTo(1);
        assertThat(ctx.isValid()).isTrue();
    }

    @Test
    void shouldReportRegisteredType() {
        OneTypeValidator v = new OneTypeValidator();
        assertThat(v.isRegisteredType(String.class)).isTrue();
        assertThat(v.isRegisteredType(Integer.class)).isFalse();
    }
}

