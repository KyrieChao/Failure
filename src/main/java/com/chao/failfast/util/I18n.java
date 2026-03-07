package com.chao.failfast.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.util.Locale;

/**
 * 国际化消息工具类
 * 提供静态方法获取国际化消息
 */
@Component
public class I18n {

    private final MessageSource messageSource;
    private static I18n instance;

    public I18n(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @PostConstruct
    public void init() {
        instance = this;
    }

    /**
     * 获取消息
     * @param code 消息键
     * @return 国际化消息
     */
    public static String get(String code) {
        return get(code, (Object[]) null);
    }

    /**
     * 获取消息（带参数）
     * @param code 消息键
     * @param args 参数
     * @return 国际化消息
     */
    public static String get(String code, Object... args) {
        if (instance == null || !StringUtils.hasText(code)) {
            // System.out.println("DEBUG: I18n.get(" + code + ") instance is NULL or code empty");
            return code;
        }
        // 如果 code 被 {} 包裹，去除包裹
        String key = code;
        if (code.startsWith("{") && code.endsWith("}")) {
            key = code.substring(1, code.length() - 1);
        }
        
        try {
            // System.out.println("DEBUG: I18n.get(" + key + ") locale=" + LocaleContextHolder.getLocale());
            return instance.messageSource.getMessage(key, args, code, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            // System.out.println("DEBUG: I18n.get exception=" + e);
            return code;
        }
    }

    /**
     * 获取当前 Locale
     * @return Locale
     */
    public static Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }
}
