package com.chao.failfast.config;

import com.chao.failfast.internal.core.FailureProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.Locale;

/**
 * Internationalization configuration class.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
@Configuration
public class I18nConfig {

    private final FailureProperties properties;

    public I18nConfig(FailureProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "failFastMessageSource")
    @ConditionalOnMissingBean(name = "failFastMessageSource")
    public MessageSource failFastMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename(properties.getI18n().getBasename());
        messageSource.setDefaultEncoding(properties.getI18n().getEncoding());
        messageSource.setCacheSeconds(properties.getI18n().getCacheSeconds());
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean
    @ConditionalOnMissingBean(LocaleResolver.class)
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();

        String defaultLocaleStr = properties.getI18n().getDefaultLocale();
        Locale defaultLocale = null;

        // 1. 只有当配置有值时，才尝试解析
        if (StringUtils.hasText(defaultLocaleStr)) {
            defaultLocale = StringUtils.parseLocaleString(defaultLocaleStr);
        }
        // 2. 统一设置默认值逻辑
        // 策略：如果解析成功，用解析的；如果解析失败(为null)，则使用硬编码默认值(如 Locale.CHINA)
        // 注意：如果你希望配置为空时不设置默认值(跟随请求头)，则去掉 ": Locale.CHINA" 部分
        localeResolver.setDefaultLocale(defaultLocale != null ? defaultLocale : Locale.CHINA);

        // 3. 设置支持的语言列表
        localeResolver.setSupportedLocales(Arrays.asList(
                Locale.CHINA,
                Locale.US,
                Locale.ENGLISH,
                Locale.SIMPLIFIED_CHINESE,
                Locale.TRADITIONAL_CHINESE
        ));

        return localeResolver;
    }
}
