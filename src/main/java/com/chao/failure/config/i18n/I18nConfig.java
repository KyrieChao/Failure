package com.chao.failure.config.i18n;

import com.chao.failure.config.properties.FailureProperties;
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
 * @version 1.3.0
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

        // 1. Only try to parse when configuration has value
        if (StringUtils.hasText(defaultLocaleStr)) {
            defaultLocale = StringUtils.parseLocaleString(defaultLocaleStr);
        }
        // 2. Unified default value setting logic
        // Strategy: If parsing succeeds, use the parsed one; if parsing fails (null), use hardcoded default value (e.g., Locale.CHINA)
        // Note: If you want to not set default value when configuration is empty (follow request header), remove the ": Locale.CHINA" part
        localeResolver.setDefaultLocale(defaultLocale != null ? defaultLocale : Locale.CHINA);

        // 3. Set supported language list
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
