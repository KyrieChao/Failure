package com.chao.failfast.config;

import com.chao.failfast.advice.DefaultExceptionHandler;
import com.chao.failfast.advice.FailFastExceptionHandler;
import com.chao.failfast.aspect.ValidationAspect;
import com.chao.failfast.internal.Ex;
import com.chao.failfast.internal.FailureContext;
import com.chao.failfast.internal.core.FailureProperties;
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
 * Fail-Fast 自动配置类 - 增强版
 * 负责自动装配FailFast框架的所有核心组件
 * 通过条件注解确保只在需要时加载相关Bean
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(FailureProperties.class)
@ConditionalOnClass(Validator.class)
@Import({I18nConfig.class, I18n.class})
public class FailFastAutoConfiguration {

    /**
     * FailFast配置属性
     */
    private final FailureProperties properties;

    /**
     * 构造函数
     *
     * @param properties FailFast配置属性
     */
    public FailFastAutoConfiguration(FailureProperties properties) {
        this.properties = properties;
    }

    /**
     * 创建FailFast上下文Bean
     * 提供全局配置管理和线程级配置覆盖功能
     *
     * @return FailFastContext实例
     */
    @Bean
    @ConditionalOnMissingBean
    public FailureContext failFastContext() {
        return new FailureContext(properties, codeMappingConfig());
    }

    /**
     * 创建错误码映射配置Bean
     * 负责HTTP状态码与业务错误码之间的映射
     *
     * @return CodeMappingConfig实例
     */
    @Bean
    @ConditionalOnMissingBean
    public CodeMappingConfig codeMappingConfig() {
        return new CodeMappingConfig(properties);
    }


    /**
     * 创建默认异常处理器Bean
     * 当用户没有自定义异常处理器时自动生效
     *
     * @return DefaultExceptionHandler实例
     */
    @Bean
    @ConditionalOnMissingBean(FailFastExceptionHandler.class)
    public DefaultExceptionHandler defaultFailFastExceptionHandler() {
        return new DefaultExceptionHandler();
    }


    /**
     * 创建验证切面Bean
     * 处理@Validate注解的自定义验证逻辑
     * 仅在AspectJ可用时创建
     *
     * @return ValidationAspect实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    public ValidationAspect validationAspect() {
        return new ValidationAspect();
    }

    // ============ 内部组件 ============

    /**
     * 创建异常工具初始化器Bean
     * 负责将FailFastContext注入到静态工具类中
     *
     * @param context FailFast上下文
     * @return ExInitializer实例
     */
    @Bean
    public ExInitializer exInitializer(FailureContext context) {
        return new ExInitializer(context);
    }

    /**
     * 异常工具初始化器
     * 通过构造函数注入的方式初始化Ex工具类的上下文
     */
    public static class ExInitializer {
        /**
         * 构造函数
         * 将FailFastContext设置到Ex工具类中
         *
         * @param context FailFast上下文
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
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10); // 尽量早执行，但不干扰其他高优先级 filter
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
                context.clearThreadContext();  // 保证每次请求结束都清理
            }
        }
    }
    // ============ 内部配置类 ============

    /**
     * 调试配置类
     * 当启用方法打印时激活，提供额外的调试信息
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "fail-fast", name = "shadow-trace", havingValue = "true")
    static class DebugConfiguration {
        /**
         * 调试模式初始化
         * 打印调试模式启用信息
         */
        @PostConstruct
        public void init() {
            log.info(I18n.get("log.fail.fast.auto.config.debug.enabled"));
        }
    }
}
