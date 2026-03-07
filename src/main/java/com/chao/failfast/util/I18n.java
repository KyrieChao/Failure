package com.chao.failfast.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.util.Locale;

/**
 * Internationalization message utility class.
 *
 * @author Kyrie Chao
 * @version 1.0.0
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
     * Get message.
     *
     * @param code Message key
     * @return Internationalized message
     */
    public static String get(String code) {
        return get(code, (Object[]) null);
    }

    /**
     * Get message (with arguments).
     *
     * @param code Message key
     * @param args Arguments
     * @return Internationalized message
     */
    public static String get(String code, Object... args) {
        if (instance == null || !StringUtils.hasText(code)) {
            // System.out.println("DEBUG: I18n.get(" + code + ") instance is NULL or code empty");
            return code;
        }
        // If code is wrapped in {}, unwrap it
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
     * Get current Locale.
     *
     * @return Locale object
     */
    public static Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }
}
