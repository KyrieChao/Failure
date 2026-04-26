package com.chao.failfast.aspect;

import com.chao.failfast.validator.FastValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ValidationAspectValidatorFactoryTest {

    @BeforeEach
    void clearCaches() throws Exception {
        Field cache = ValidationAspect.class.getDeclaredField("VALIDATOR_CACHE");
        cache.setAccessible(true);
        ((ConcurrentHashMap<?, ?>) cache.get(null)).clear();

        Field factoryCache = ValidationAspect.class.getDeclaredField("VALIDATOR_FACTORY_CACHE");
        factoryCache.setAccessible(true);
        ((ConcurrentHashMap<?, ?>) factoryCache.get(null)).clear();
    }

    static final class TestProvider implements ObjectProvider<FastValidator<Object>> {
        private final FastValidator<Object> bean;
        private int ifAvailableCalls;
        private int getObjectCalls;

        TestProvider(FastValidator<Object> bean) {
            this.bean = bean;
        }

        int ifAvailableCalls() {
            return ifAvailableCalls;
        }

        int getObjectCalls() {
            return getObjectCalls;
        }

        @Override
        public FastValidator<Object> getObject(Object... args) {
            return getObject();
        }

        @Override
        public FastValidator<Object> getObject() {
            getObjectCalls++;
            if (bean == null) throw new IllegalStateException("no bean");
            return bean;
        }

        @Override
        public FastValidator<Object> getIfAvailable() {
            ifAvailableCalls++;
            return bean;
        }

        @Override
        public FastValidator<Object> getIfUnique() throws BeansException {
            return null;
        }
    }

    static class BeanValidator implements FastValidator<Object> {
        @Override
        public void validate(Object target, ValidationContext context) {
        }
    }

    static class ReflectValidator implements FastValidator<Object> {
        static final AtomicInteger constructed = new AtomicInteger();

        ReflectValidator() {
            constructed.incrementAndGet();
        }

        @Override
        public void validate(Object target, ValidationContext context) {
        }
    }

    @Test
    void shouldCacheSpringBeanDecision() throws Exception {
        ValidationAspect aspect = new ValidationAspect();

        ApplicationContext applicationContext = mock(ApplicationContext.class);

        FastValidator<Object> bean = new BeanValidator();
        TestProvider provider = new TestProvider(bean);
        when(applicationContext.getBeanProvider((Class<?>) BeanValidator.class)).thenReturn((ObjectProvider) provider);

        Field f = ValidationAspect.class.getDeclaredField("applicationContext");
        f.setAccessible(true);
        f.set(aspect, applicationContext);

        Method m = ValidationAspect.class.getDeclaredMethod("getOrCreateValidator", Class.class);
        m.setAccessible(true);

        Object v1 = m.invoke(aspect, BeanValidator.class);
        Object v2 = m.invoke(aspect, BeanValidator.class);

        assertThat(v1).isSameAs(bean);
        assertThat(v2).isSameAs(bean);

        assertThat(provider.ifAvailableCalls()).isEqualTo(1);
        assertThat(provider.getObjectCalls()).isEqualTo(2);
    }

    @Test
    void shouldCacheReflectionInstance() throws Exception {
        ReflectValidator.constructed.set(0);

        ValidationAspect aspect = new ValidationAspect();

        ApplicationContext applicationContext = mock(ApplicationContext.class);
        TestProvider provider = new TestProvider(null);
        when(applicationContext.getBeanProvider((Class<?>) ReflectValidator.class)).thenReturn((ObjectProvider) provider);

        Field f = ValidationAspect.class.getDeclaredField("applicationContext");
        f.setAccessible(true);
        f.set(aspect, applicationContext);

        Method m = ValidationAspect.class.getDeclaredMethod("getOrCreateValidator", Class.class);
        m.setAccessible(true);

        Object v1 = m.invoke(aspect, ReflectValidator.class);
        Object v2 = m.invoke(aspect, ReflectValidator.class);

        assertThat(v1).isSameAs(v2);
        assertThat(ReflectValidator.constructed.get()).isEqualTo(1);

        assertThat(provider.ifAvailableCalls()).isEqualTo(1);
    }
}
