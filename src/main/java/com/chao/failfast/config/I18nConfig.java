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
 * @version 1.0.0
 */
@Configuration
public class I18nConfig {

    private final FailureProperties properties;

    public I18nConfig(FailureProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // 指定资源文件路径
        messageSource.setBasename(properties.getI18n().getBasename());
        // 设置默认编码
        messageSource.setDefaultEncoding(properties.getI18n().getEncoding());
        // 缓存时间
        messageSource.setCacheSeconds(properties.getI18n().getCacheSeconds());
        // 如果找不到 key，直接返回 key 本身，而不是抛出异常
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean
    @ConditionalOnMissingBean(LocaleResolver.class)
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        // 设置默认语言
        Locale defaultLocale = StringUtils.parseLocaleString(properties.getI18n().getDefaultLocale());
        localeResolver.setDefaultLocale(defaultLocale != null ? defaultLocale : Locale.CHINA);

        // 设置支持的语言列表 (这里简单列举常用的，也可以做成配置)
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
