package com.chao.failfast.config;

import com.chao.failfast.config.i18n.I18nConfig;
import com.chao.failfast.config.properties.FailureProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.LocaleResolver;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("I18nConfig测试")
class I18nConfigTest {

    private I18nConfig i18nConfig;

    @BeforeEach
    void setUp() {
        FailureProperties properties = mock(FailureProperties.class);
        FailureProperties.I18n i18n = mock(FailureProperties.I18n.class);
        
        when(properties.getI18n()).thenReturn(i18n);
        when(i18n.getBasename()).thenReturn("messages");
        when(i18n.getEncoding()).thenReturn("UTF-8");
        when(i18n.getCacheSeconds()).thenReturn(3600);
        when(i18n.getDefaultLocale()).thenReturn("zh_CN");

        i18nConfig = new I18nConfig(properties);
    }

    @Test
    @DisplayName("构造函数 - 初始化配置")
    void testConstructor() {
        assertThat(i18nConfig).isNotNull();
    }

    @Test
    @DisplayName("failFastMessageSource方法 - 创建消息源")
    void testFailFastMessageSource() {
        MessageSource messageSource = i18nConfig.failFastMessageSource();
        assertThat(messageSource).isNotNull();
    }

    @Test
    @DisplayName("localeResolver方法 - 创建区域解析器")
    void testLocaleResolver() {
        LocaleResolver localeResolver = i18nConfig.localeResolver();
        assertThat(localeResolver).isNotNull();
    }

    @Test
    @DisplayName("localeResolver方法 - 默认区域设置")
    void testLocaleResolverDefaultLocale() {
        // 测试默认区域设置为null的情况
        FailureProperties properties = mock(FailureProperties.class);
        FailureProperties.I18n i18n = mock(FailureProperties.I18n.class);
        
        when(properties.getI18n()).thenReturn(i18n);
        when(i18n.getBasename()).thenReturn("messages");
        when(i18n.getEncoding()).thenReturn("UTF-8");
        when(i18n.getCacheSeconds()).thenReturn(3600);
        when(i18n.getDefaultLocale()).thenReturn(null);

        I18nConfig config = new I18nConfig(properties);
        LocaleResolver localeResolver = config.localeResolver();
        assertThat(localeResolver).isNotNull();
    }
}
