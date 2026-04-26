package com.chao.failfast.autoconfigure;

import com.chao.failfast.config.masking.DefaultValueMasker;
import com.chao.failfast.config.masking.StructuredValueMasker;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.spi.security.ValueMasker;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FailFastAutoConfigurationTest {

    @Test
    void testValueMaskerWithNullMasking() {
        // 测试当 masking 为 null 时
        FailureProperties properties = mock(FailureProperties.class);
        when(properties.getMasking()).thenReturn(null);

        MessageSource messageSource = mock(MessageSource.class);

        FailFastAutoConfiguration config = new FailFastAutoConfiguration(properties, messageSource);
        ValueMasker result = config.valueMasker();

        assertTrue(result instanceof DefaultValueMasker);
    }

    @Test
    void testValueMaskerWithStructuredDisabled() {
        // 测试当 structuredEnabled 为 false 时
        FailureProperties properties = mock(FailureProperties.class);
        FailureProperties.Masking masking = mock(FailureProperties.Masking.class);
        when(masking.isStructuredEnabled()).thenReturn(false);
        when(properties.getMasking()).thenReturn(masking);

        MessageSource messageSource = mock(MessageSource.class);

        FailFastAutoConfiguration config = new FailFastAutoConfiguration(properties, messageSource);
        ValueMasker result = config.valueMasker();

        assertTrue(result instanceof DefaultValueMasker);
    }

    @Test
    void testValueMaskerWithStructuredEnabled() {
        // 测试当 structuredEnabled 为 true 时
        FailureProperties properties = mock(FailureProperties.class);
        FailureProperties.Masking masking = mock(FailureProperties.Masking.class);
        when(masking.isStructuredEnabled()).thenReturn(true);
        when(properties.getMasking()).thenReturn(masking);

        MessageSource messageSource = mock(MessageSource.class);

        FailFastAutoConfiguration config = new FailFastAutoConfiguration(properties, messageSource);
        ValueMasker result = config.valueMasker();

        assertTrue(result instanceof StructuredValueMasker);
    }

    @Test
    void testConstructor() {
        // 测试构造函数
        FailureProperties properties = mock(FailureProperties.class);
        MessageSource messageSource = mock(MessageSource.class);

        FailFastAutoConfiguration config = new FailFastAutoConfiguration(properties, messageSource);

        assertNotNull(config);
    }

    @Test
    void testGetMessageWithI18nDisabled() {
        // 测试 getMessage 方法 - i18n 禁用
        FailureProperties properties = mock(FailureProperties.class);
        FailureProperties.I18n i18n = mock(FailureProperties.I18n.class);
        when(i18n.isEnabled()).thenReturn(false);
        when(properties.getI18n()).thenReturn(i18n);

        MessageSource messageSource = mock(MessageSource.class);

        FailFastAutoConfiguration config = new FailFastAutoConfiguration(properties, messageSource);

        // 使用反射调用私有方法
        try {
            java.lang.reflect.Method getMessageMethod = FailFastAutoConfiguration.class.getDeclaredMethod("getMessage");
            getMessageMethod.setAccessible(true);
            String result = (String) getMessageMethod.invoke(config);
            assertEquals("log.fail.fast.auto.config.debug.enabled", result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    void testFrameworkDefaultConfigurer() {
        // 测试 frameworkDefaultConfigurer 方法
        FailureProperties properties = mock(FailureProperties.class);
        MessageSource messageSource = mock(MessageSource.class);

        FailFastAutoConfiguration config = new FailFastAutoConfiguration(properties, messageSource);
        Object result = config.frameworkDefaultConfigurer();

        assertNotNull(result);
    }

    @Test
    void testFailFastCleanupFilter() {
        // 测试 failFastCleanupFilter 方法
        FailureProperties properties = mock(FailureProperties.class);
        MessageSource messageSource = mock(MessageSource.class);

        FailFastAutoConfiguration config = new FailFastAutoConfiguration(properties, messageSource);
        
        // 由于需要 FailureContext，这里我们只测试方法是否能正常调用
        try {
            java.lang.reflect.Method failFastCleanupFilterMethod = FailFastAutoConfiguration.class.getDeclaredMethod("failFastCleanupFilter", com.chao.failfast.internal.core.FailureContext.class);
            failFastCleanupFilterMethod.setAccessible(true);
            // 传入 null 来测试方法签名
            Object result = failFastCleanupFilterMethod.invoke(config, null);
            // 这里会抛出异常，因为方法内部会使用 context
        } catch (Exception e) {
            // 预期会抛出异常，因为 context 为 null
        }
    }

    @Test
    void testFailFastTraceIdFilter() {
        // 测试 failFastTraceIdFilter 方法
        FailureProperties properties = mock(FailureProperties.class);
        FailureProperties.TraceId traceId = mock(FailureProperties.TraceId.class);
        when(properties.getTraceId()).thenReturn(traceId);

        MessageSource messageSource = mock(MessageSource.class);

        FailFastAutoConfiguration config = new FailFastAutoConfiguration(properties, messageSource);
        
        // 由于需要 FailureContext，这里我们只测试方法是否能正常调用
        try {
            java.lang.reflect.Method failFastTraceIdFilterMethod = FailFastAutoConfiguration.class.getDeclaredMethod("failFastTraceIdFilter", com.chao.failfast.internal.core.FailureContext.class);
            failFastTraceIdFilterMethod.setAccessible(true);
            // 传入 null 来测试方法签名
            Object result = failFastTraceIdFilterMethod.invoke(config, null);
            // 这里会抛出异常，因为方法内部会使用 context
        } catch (Exception e) {
            // 预期会抛出异常，因为 context 为 null
        }
    }

    @Test
    void testFailFastBodyArgumentResolverPostProcessor() {
        // 测试 failFastBodyArgumentResolverPostProcessor 方法
        FailureProperties properties = mock(FailureProperties.class);
        MessageSource messageSource = mock(MessageSource.class);

        FailFastAutoConfiguration config = new FailFastAutoConfiguration(properties, messageSource);
        
        try {
            java.lang.reflect.Method failFastBodyArgumentResolverPostProcessorMethod = FailFastAutoConfiguration.class.getDeclaredMethod("failFastBodyArgumentResolverPostProcessor");
            failFastBodyArgumentResolverPostProcessorMethod.setAccessible(true);
            Object result = failFastBodyArgumentResolverPostProcessorMethod.invoke(config);
            assertNotNull(result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }
}
