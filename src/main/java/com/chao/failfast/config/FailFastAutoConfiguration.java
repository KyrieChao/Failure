package com.chao.failfast.config;

import com.chao.failfast.advice.DefaultExceptionHandler;
import com.chao.failfast.advice.FailFastExceptionHandler;
import com.chao.failfast.aspect.ValidationAspect;
import com.chao.failfast.internal.Ex;
import com.chao.failfast.internal.FailureContext;
import com.chao.failfast.internal.core.FailureProperties;
import com.chao.failfast.internal.policy.DefaultErrorPolicy;
import com.chao.failfast.internal.policy.ErrorPolicy;
import com.chao.failfast.util.I18n;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.*;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.io.IOException;

/**
 * Fail-Fast auto-configuration class - Enhanced version.
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(FailureProperties.class)
@ConditionalOnClass(Validator.class)
@Import({I18nConfig.class, I18n.class})
public class FailFastAutoConfiguration {

    /**
     * FailFast configuration properties.
     */
    private final FailureProperties properties;

    /**
     * Constructor.
     *
     * @param properties FailFast configuration properties
     */
    public FailFastAutoConfiguration(FailureProperties properties) {
        this.properties = properties;
    }

    /**
     * Create FailFast context Bean.
     *
     * @return FailFastContext instance
     */
    @Bean
    @ConditionalOnMissingBean
    public FailureContext failFastContext() {
        return new FailureContext(properties, codeMappingConfig(), errorPolicy());
    }

    /**
     * Create error code mapping configuration Bean.
     *
     * @return CodeMappingConfig instance
     */
    @Bean
    @ConditionalOnMissingBean
    public CodeMappingConfig codeMappingConfig() {
        return new CodeMappingConfig(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorPolicy errorPolicy() {
        return DefaultErrorPolicy.INSTANCE;
    }


    /**
     * Create default exception handler Bean.
     *
     * @return DefaultExceptionHandler instance
     */
    @Bean
    @ConditionalOnMissingBean(FailFastExceptionHandler.class)
    public DefaultExceptionHandler defaultFailFastExceptionHandler() {
        return new DefaultExceptionHandler();
    }


    /**
     * Create validation aspect Bean.
     *
     * @return ValidationAspect instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    public ValidationAspect validationAspect() {
        return new ValidationAspect();
    }

    // ============ 内部组件 ============

    /**
     * Create exception utility initializer Bean.
     *
     * @param context FailFast context
     * @return ExInitializer instance
     */
    @Bean
    public ExInitializer exInitializer(FailureContext context) {
        return new ExInitializer(context);
    }

    /**
     * Exception utility initializer.
     */
    public static class ExInitializer {
        /**
         * Constructor.
         *
         * @param context FailFast context
         */
        ExInitializer(FailureContext context) {
            Ex.setContext(context);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public FilterRegistrationBean<FailFastCleanupFilter> failFastCleanupFilter(FailureContext context) {
        FilterRegistrationBean<FailFastCleanupFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new FailFastCleanupFilter(context));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.addUrlPatterns("/*");
        return registration;
    }

    public static class FailFastCleanupFilter implements Filter {
        private final FailureContext context;

        public FailFastCleanupFilter(FailureContext context) {
            this.context = context;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            try {
                chain.doFilter(request, response);
            } finally {
                context.clearThreadContext();
            }
        }
    }

    /**
     * Debug configuration class.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "fail-fast", name = "shadow-trace", havingValue = "true")
    static class DebugConfiguration {
        /**
         * Debug mode initialization.
         */
        @PostConstruct
        public void init() {
            log.info(I18n.get("log.fail.fast.auto.config.debug.enabled"));
        }
    }
}
