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
                "com.chao.failfast.advice",     // 异常处理切面包
                "com.chao.failfast.annotation", // 内部工具包
                "com.chao.failfast.aspect",     // 切面包
                "com.chao.failfast.config",     // 配置类
                "com.chao.failfast.integration",// 集成包
                "com.chao.failfast.internal",   // 内部工具包
                "com.chao.failfast.result",     // 响应结果类
                "com.chao.failfast.Failure",    // 失败处理类
                "org.springframework",          // Spring框架
                "org.apache",                   // Apache相关组件
                "jakarta",                      // Jakarta EE规范
                "java.",                        // Java标准库
                "jdk.",                         // JDK内部类
                "sun."                          // Sun Microsystems遗留类
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
