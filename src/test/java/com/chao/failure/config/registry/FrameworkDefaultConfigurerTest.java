package com.chao.failure.config.registry;

import com.chao.failure.spi.filter.SkipPrefixRegistry;
import com.chao.failure.spi.filter.SkipTypeRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;

class FrameworkDefaultConfigurerTest {

    @Test
    void testAddExceptionSkipPrefixes() {
        // 准备
        FrameworkDefaultConfigurer configurer = new FrameworkDefaultConfigurer();
        SkipPrefixRegistry registry = Mockito.mock(SkipPrefixRegistry.class);
        
        // 执行
        configurer.addExceptionSkipPrefixes(registry);
        
        // 验证
        verify(registry).add(
                "com.chao.failure.annotation",
                "com.chao.failure.aspect",
                "com.chao.failure.autoconfigure",
                "com.chao.failure.config",
                "com.chao.failure.constant",
                "com.chao.failure.exception",
                "com.chao.failure.integration",
                "com.chao.failure.internal",
                "com.chao.failure.result",
                "com.chao.failure.spi",
                "com.chao.failure.util",
                "com.chao.failure.Failure",
                "org.springframework",
                "org.apache",
                "jakarta",
                "java.",
                "jdk.",
                "sun."
        );
    }

    @Test
    void testAddValidationSkipTypes() {
        // 准备
        FrameworkDefaultConfigurer configurer = new FrameworkDefaultConfigurer();
        SkipTypeRegistry registry = Mockito.mock(SkipTypeRegistry.class);
        
        // 执行
        configurer.addValidationSkipTypes(registry);
        
        // 验证
        verify(registry).add(
                jakarta.servlet.ServletRequest.class,
                jakarta.servlet.ServletResponse.class,
                org.springframework.web.multipart.MultipartFile.class,
                org.springframework.validation.BindingResult.class,
                jakarta.validation.Validator.class,
                org.springframework.web.server.ServerWebExchange.class
        );
    }
}
