package com.chao.failfast.config.registry;

import com.chao.failfast.spi.FailFastConfigurer;
import com.chao.failfast.spi.SkipPrefixRegistry;
import com.chao.failfast.spi.SkipTypeRegistry;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.validation.Validator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerWebExchange;

/**
 * Internal default configurer for framework baseline.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FrameworkDefaultConfigurer implements FailFastConfigurer {

    @Override
    public void addExceptionSkipPrefixes(SkipPrefixRegistry registry) {
        registry.add(
                "com.chao.failfast.annotation",   // 注解类
                "com.chao.failfast.aspect",       // 切面类
                "com.chao.failfast.autoconfigure",// 自动配置类
                "com.chao.failfast.config",       // 配置类
                "com.chao.failfast.constant",     // 常量类
                "com.chao.failfast.exception",    // 异常类
                "com.chao.failfast.integration",  // 集成类
                "com.chao.failfast.internal",     // 内部类
                "com.chao.failfast.result",       // Result类
                "com.chao.failfast.spi",          // SPI接口
                "com.chao.failfast.util",         // 工具类
                "com.chao.failfast.Failure",      // 入口
                "org.springframework",            // Spring框架
                "org.apache",                     // Apache相关组件
                "jakarta",                        // Jakarta EE规范
                "java.",                          // Java标准库
                "jdk.",                           // JDK内部类
                "sun."                            // Sun Microsystems遗留类
        );
    }

    @Override
    public void addValidationSkipTypes(SkipTypeRegistry registry) {
        registry.add(
                ServletRequest.class,
                ServletResponse.class,
                MultipartFile.class,
                BindingResult.class,
                Validator.class,
                ServerWebExchange.class
        );
    }
}
