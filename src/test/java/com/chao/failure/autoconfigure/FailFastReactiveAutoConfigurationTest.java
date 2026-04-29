package com.chao.failure.autoconfigure;

import com.chao.failure.config.masking.DefaultValueMasker;
import com.chao.failure.config.masking.StructuredValueMasker;
import com.chao.failure.config.mapping.CodeMappingConfig;
import com.chao.failure.config.mapping.CodeLocator;
import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.spi.config.FailFastConfigurer;
import com.chao.failure.spi.i18n.LocalizedResponseResolver;
import com.chao.failure.spi.security.MaskPick;
import com.chao.failure.spi.security.ValueMasker;
import com.chao.failure.spi.validation.ValidatorWhitelistRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FailFastReactiveAutoConfigurationTest {

    @Test
    void testValueMaskerWithNullMasking() {
        // 测试当 masking 为 null 时
        FailureProperties properties = mock(FailureProperties.class);
        when(properties.getMasking()).thenReturn(null);

        FailFastReactiveAutoConfiguration config = new FailFastReactiveAutoConfiguration();
        ValueMasker result = config.valueMasker(properties);

        assertTrue(result instanceof DefaultValueMasker);
    }

    @Test
    void testValueMaskerWithStructuredDisabled() {
        // 测试当 structuredEnabled 为 false 时
        FailureProperties properties = mock(FailureProperties.class);
        FailureProperties.Masking masking = mock(FailureProperties.Masking.class);
        when(masking.isStructuredEnabled()).thenReturn(false);
        when(properties.getMasking()).thenReturn(masking);

        FailFastReactiveAutoConfiguration config = new FailFastReactiveAutoConfiguration();
        ValueMasker result = config.valueMasker(properties);

        assertTrue(result instanceof DefaultValueMasker);
    }

    @Test
    void testValueMaskerWithStructuredEnabled() {
        // 测试当 structuredEnabled 为 true 时
        FailureProperties properties = mock(FailureProperties.class);
        FailureProperties.Masking masking = mock(FailureProperties.Masking.class);
        when(masking.isStructuredEnabled()).thenReturn(true);
        when(properties.getMasking()).thenReturn(masking);

        FailFastReactiveAutoConfiguration config = new FailFastReactiveAutoConfiguration();
        ValueMasker result = config.valueMasker(properties);

        assertTrue(result instanceof StructuredValueMasker);
    }

    @Test
    void testValidatorWhitelistRegistryWithNoConfigurers() {
        // 测试没有配置器的情况
        ObjectProvider<FailFastConfigurer> configurers = mock(ObjectProvider.class);
        when(configurers.orderedStream()).thenReturn(Stream.empty());

        FailFastReactiveAutoConfiguration config = new FailFastReactiveAutoConfiguration();
        ValidatorWhitelistRegistry result = config.validatorWhitelistRegistry(configurers);

        assertNotNull(result);
    }

    @Test
    void testValidatorWhitelistRegistryWithConfigurers() {
        // 测试有配置器的情况
        ObjectProvider<FailFastConfigurer> configurers = mock(ObjectProvider.class);
        FailFastConfigurer configurer = mock(FailFastConfigurer.class);
        when(configurers.orderedStream()).thenReturn(Stream.of(configurer));

        FailFastReactiveAutoConfiguration config = new FailFastReactiveAutoConfiguration();
        ValidatorWhitelistRegistry result = config.validatorWhitelistRegistry(configurers);

        assertNotNull(result);
        verify(configurer, times(1)).addValidatorWhitelist(any());
    }

    @Test
    void testCodeMappingConfig() {
        // 测试 codeMappingConfig 方法
        FailureProperties properties = mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = mock(FailureProperties.CodeMapping.class);
        when(codeMapping.getHttpStatus()).thenReturn(new java.util.HashMap<>());
        when(properties.getCodeMapping()).thenReturn(codeMapping);

        FailFastReactiveAutoConfiguration config = new FailFastReactiveAutoConfiguration();
        CodeMappingConfig result = config.codeMappingConfig(properties);

        assertNotNull(result);
    }

    @Test
    void testCodeGroups() {
        // 测试 codeGroups 方法
        FailureProperties properties = mock(FailureProperties.class);
        FailureProperties.CodeMapping codeMapping = mock(FailureProperties.CodeMapping.class);
        when(properties.getCodeMapping()).thenReturn(codeMapping);
        when(codeMapping.getGroups()).thenReturn(java.util.Map.of("client", java.util.List.of("40000-40001", 40010)));

        FailFastReactiveAutoConfiguration config = new FailFastReactiveAutoConfiguration();
        CodeLocator groups = config.codeGroups(properties);

        assertNotNull(groups);
        assertTrue(groups.isInGroup(40001, "client"));
        assertEquals("client", groups.getGroupForCode(40010));
        assertEquals(java.util.List.of(40010), groups.getExactCodes("client"));
    }

    @Test
    void testCodeGroupsWithNullCodeMapping() {
        // 测试 codeGroups 方法 - codeMapping 为 null
        FailureProperties properties = mock(FailureProperties.class);
        when(properties.getCodeMapping()).thenReturn(null);

        FailFastReactiveAutoConfiguration config = new FailFastReactiveAutoConfiguration();
        CodeLocator groups = config.codeGroups(properties);

        assertNotNull(groups);
    }

    @Test
    void testCodeGroupsWithNullProperties() {
        // 测试 codeGroups 方法 - properties 为 null
        FailFastReactiveAutoConfiguration config = new FailFastReactiveAutoConfiguration();
        CodeLocator groups = config.codeGroups(null);

        assertNotNull(groups);
    }

    @Test
    void testLocalizedResponseResolver() {
        // 测试 localizedResponseResolver 方法
        FailFastReactiveAutoConfiguration config = new FailFastReactiveAutoConfiguration();
        ObjectProvider<FailFastConfigurer> configurers = mock(ObjectProvider.class);
        when(configurers.orderedStream()).thenReturn(Stream.empty());
        LocalizedResponseResolver result = config.localizedResponseResolver(configurers);

        assertNotNull(result);
    }

    @Test
    void testLocalizedResponseResolverWithConfigurers() {
        // 测试 localizedResponseResolver 方法使用配置器
        FailFastReactiveAutoConfiguration config = new FailFastReactiveAutoConfiguration();
        ObjectProvider<FailFastConfigurer> configurers = mock(ObjectProvider.class);
        FailFastConfigurer configurer = mock(FailFastConfigurer.class);
        when(configurers.orderedStream()).thenReturn(Stream.of(configurer));

        LocalizedResponseResolver result = config.localizedResponseResolver(configurers);

        assertNotNull(result);
        verify(configurer, times(1)).customizeLocalizedResponseResolver(any());
    }

    @Test
    void testFailureContext() {
        // 测试 failureContext 方法
        FailureProperties properties = mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = mock(CodeMappingConfig.class);
        ValueMasker valueMasker = mock(ValueMasker.class);
        LocalizedResponseResolver localizedResponseResolver = mock(LocalizedResponseResolver.class);
        ObjectProvider<MaskPick> maskResolvers = mock(ObjectProvider.class);
        when(maskResolvers.orderedStream()).thenReturn(Stream.empty());

        FailFastReactiveAutoConfiguration config = new FailFastReactiveAutoConfiguration();
        FailureContext result = config.failureContext(properties, codeMappingConfig, valueMasker, localizedResponseResolver, maskResolvers);

        assertNotNull(result);
    }

    @Test
    void testFailFastReactiveCleanupFilter() {
        // 测试 FailFastReactiveCleanupFilter 内部类
        FailureContext context = mock(FailureContext.class);

        FailFastReactiveAutoConfiguration.FailFastReactiveCleanupFilter filter = 
            new FailFastReactiveAutoConfiguration.FailFastReactiveCleanupFilter(context);

        assertNotNull(filter);
        assertEquals(Ordered.HIGHEST_PRECEDENCE + 10, filter.getOrder());
    }

    @Test
    void testFailFastReactiveTraceIdFilter() {
        // 测试 FailFastReactiveTraceIdFilter 内部类
        FailureProperties.TraceId traceId = mock(FailureProperties.TraceId.class);
        when(traceId.getHeaderName()).thenReturn("X-Trace-Id");
        when(traceId.isGenerateIfMissing()).thenReturn(true);
        when(traceId.isResponseHeader()).thenReturn(true);
        when(traceId.getResponseHeaderName()).thenReturn("X-Response-Trace-Id");

        FailureContext context = mock(FailureContext.class);

        FailFastReactiveAutoConfiguration.FailFastReactiveTraceIdFilter filter = 
            new FailFastReactiveAutoConfiguration.FailFastReactiveTraceIdFilter(context, traceId);

        assertNotNull(filter);
        assertEquals(Ordered.HIGHEST_PRECEDENCE + 5, filter.getOrder());
    }

    @Test
    void testReactiveBeans() {
        // 测试 ReactiveBeans 内部类
        FailFastReactiveAutoConfiguration.ReactiveBeans reactiveBeans = 
            new FailFastReactiveAutoConfiguration.ReactiveBeans();

        assertNotNull(reactiveBeans);
    }
}
