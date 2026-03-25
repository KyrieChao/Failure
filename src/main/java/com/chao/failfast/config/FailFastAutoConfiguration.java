package com.chao.failfast.config;

import com.chao.failfast.advice.DefaultExceptionHandler;
import com.chao.failfast.advice.FailFastExceptionHandler;
import com.chao.failfast.aspect.ValidationAspect;
import com.chao.failfast.internal.Ex;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.FailureProperties;
import com.chao.failfast.internal.policy.DefaultErrorPolicy;
import com.chao.failfast.internal.policy.ErrorPolicy;
import com.chao.failfast.util.I18n;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Failure auto-configuration class - Enhanced version.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(FailureProperties.class)
@Import({I18nConfig.class, I18n.class})
public class FailFastAutoConfiguration {

    /**
     * FailFast configuration properties.
     */
    private final FailureProperties properties;
    private final MessageSource messageSource;

    /**
     * Constructor.
     *
     * @param properties FailFast configuration properties
     */
    public FailFastAutoConfiguration(FailureProperties properties, @Qualifier("failFastMessageSource") MessageSource messageSource) {
        this.properties = properties;
        this.messageSource = messageSource;
    }

    @PostConstruct
    public void init() {
        log.info("====================================================================");
        log.info(getMessage());
        log.info("Shadow Trace: {}", properties.isShadowTrace());
        log.info("Debug Snapshot: {}", properties.isDebugSnapshot());
        log.info("Method Validation Enabled: {}", properties.isMethodValidationEnabled());
        log.info("====================================================================");
    }

    private String getMessage() {
        String mess = "log.fail.fast.auto.config.debug.enabled";
        if (!properties.getI18n().isEnabled()) {
            return mess;
        }
        Locale locale = StringUtils.parseLocaleString(properties.getI18n().getDefaultLocale());
        return messageSource.getMessage(mess, null, mess, locale);
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

    /**
     * Create method validation post processor Bean.
     *
     * @param validator Validator instance
     * @return MethodValidationPostProcessor instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "fail-fast", name = "method-validation-enabled", havingValue = "true", matchIfMissing = false)
    @ConditionalOnBean(Validator.class)
    public MethodValidationPostProcessor methodValidationPostProcessor(Validator validator) {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator);
        return processor;
    }

    // ============ 内部组件 ============

    /**
     * Create exception utility initializer Bean.
     *
     * @param context FailFast context
     * @return ExInitializer instance
     */
    @Bean
    @ConditionalOnMissingBean(name = "exInitializer")
    public ExInitializer exInitializer(FailureContext context, ObjectProvider<Validator> validatorProvider) {
        return new ExInitializer(context, validatorProvider.getIfAvailable());
    }

    /**
     * Exception utility initializer.
     */
    public class ExInitializer {
        /**
         * Constructor.
         *
         * @param context FailFast context
         * @param validator Validator instance (optional)
         */
        ExInitializer(FailureContext context, Validator validator) {
            Ex.setContext(context);
            if (validator != null) {
                com.chao.failfast.internal.Chain.setValidator(validator);
            }
            // Set failure properties to Chain
            com.chao.failfast.internal.Chain.setFailureProperties(properties);
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

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "fail-fast.trace-id", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<FailFastTraceIdFilter> failFastTraceIdFilter(FailureContext context) {
        FilterRegistrationBean<FailFastTraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new FailFastTraceIdFilter(context, properties.getTraceId()));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
        registration.addUrlPatterns("/*");
        return registration;
    }

    public static class FailFastTraceIdFilter implements Filter {
        private final FailureContext context;
        private final FailureProperties.TraceId traceId;

        public FailFastTraceIdFilter(FailureContext context, FailureProperties.TraceId traceId) {
            this.context = context;
            this.traceId = traceId;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            if (!(request instanceof HttpServletRequest httpRequest)) {
                chain.doFilter(request, response);
                return;
            }

            String headerName = traceId.getHeaderName();
            String traceIdValue = StringUtils.hasText(headerName) ? httpRequest.getHeader(headerName) : null;
            if (!StringUtils.hasText(traceIdValue) && traceId.isGenerateIfMissing()) {
                traceIdValue = UUID.randomUUID().toString();
            }

            if (StringUtils.hasText(traceIdValue)) {
                context.setTraceId(traceIdValue);
                if (traceId.isMdcEnabled() && StringUtils.hasText(traceId.getMdcKey())) {
                    MDC.put(traceId.getMdcKey(), traceIdValue);
                }
                if (traceId.isResponseHeader() && response instanceof HttpServletResponse httpResponse) {
                    String responseHeaderName = StringUtils.hasText(traceId.getResponseHeaderName()) ? traceId.getResponseHeaderName() : headerName;
                    if (StringUtils.hasText(responseHeaderName)) {
                        httpResponse.setHeader(responseHeaderName, traceIdValue);
                    }
                }
            }

            try {
                chain.doFilter(request, response);
            } finally {
                if (traceId.isMdcEnabled() && StringUtils.hasText(traceId.getMdcKey())) {
                    MDC.remove(traceId.getMdcKey());
                }
            }
        }
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static BeanPostProcessor failFastBodyArgumentResolverPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) {
                if (!(bean instanceof RequestMappingHandlerAdapter adapter)) {
                    return bean;
                }

                List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers = adapter.getArgumentResolvers();
                if (resolvers == null) {
                    return bean;
                }
                for (org.springframework.web.method.support.HandlerMethodArgumentResolver resolver : resolvers) {
                    if (resolver instanceof OptionalBodyResolver) {
                        return bean;
                    }
                }

                for (org.springframework.web.method.support.HandlerMethodArgumentResolver resolver : resolvers) {
                    if (resolver instanceof RequestResponseBodyMethodProcessor processor) {
                        List<org.springframework.web.method.support.HandlerMethodArgumentResolver> newResolvers = new ArrayList<>(resolvers);
                        newResolvers.add(0, new OptionalBodyResolver(processor));
                        adapter.setArgumentResolvers(newResolvers);
                        break;
                    }
                }

                return bean;
            }
        };
    }

}
