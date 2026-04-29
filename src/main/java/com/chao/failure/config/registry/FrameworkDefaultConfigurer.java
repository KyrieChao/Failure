package com.chao.failure.config.registry;

import com.chao.failure.spi.config.FailFastConfigurer;
import com.chao.failure.spi.filter.SkipPrefixRegistry;
import com.chao.failure.spi.filter.SkipTypeRegistry;
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
 * @version 1.3.1
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FrameworkDefaultConfigurer implements FailFastConfigurer {

    @Override
    public void addExceptionSkipPrefixes(SkipPrefixRegistry registry) {
        registry.add(
                "com.chao.failure.annotation",   // Annotation classes
                "com.chao.failure.aspect",       // Aspect classes
                "com.chao.failure.autoconfigure",// Auto-configuration classes
                "com.chao.failure.config",       // Configuration classes
                "com.chao.failure.constant",     // Constant classes
                "com.chao.failure.exception",    // Exception classes
                "com.chao.failure.integration",  // Integration classes
                "com.chao.failure.internal",     // Internal classes
                "com.chao.failure.result",       // Result classes
                "com.chao.failure.spi",          // SPI interfaces
                "com.chao.failure.util",         // Utility classes
                "com.chao.failure.Failure",      // Entry point
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
