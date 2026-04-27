package com.chao.failfast.config.registry;

import com.chao.failfast.spi.config.FailFastConfigurer;
import com.chao.failfast.spi.filter.SkipPrefixRegistry;
import com.chao.failfast.spi.filter.SkipTypeRegistry;
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
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FrameworkDefaultConfigurer implements FailFastConfigurer {

    @Override
    public void addExceptionSkipPrefixes(SkipPrefixRegistry registry) {
        registry.add(
                "com.chao.failfast.annotation",   // Annotation classes
                "com.chao.failfast.aspect",       // Aspect classes
                "com.chao.failfast.autoconfigure",// Auto-configuration classes
                "com.chao.failfast.config",       // Configuration classes
                "com.chao.failfast.constant",     // Constant classes
                "com.chao.failfast.exception",    // Exception classes
                "com.chao.failfast.integration",  // Integration classes
                "com.chao.failfast.internal",     // Internal classes
                "com.chao.failfast.result",       // Result classes
                "com.chao.failfast.spi",          // SPI interfaces
                "com.chao.failfast.util",         // Utility classes
                "com.chao.failfast.Failure",      // Entry point
                "org.springframework",            // Spring framework
                "org.apache",                     // Apache components
                "jakarta",                        // Jakarta EE spec
                "java.",                          // Java standard library
                "jdk.",                           // JDK internal classes
                "sun."                            // Sun Microsystems legacy classes
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
