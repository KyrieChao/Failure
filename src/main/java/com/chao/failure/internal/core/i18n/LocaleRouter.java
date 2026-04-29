package com.chao.failure.internal.core.i18n;

import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.spi.i18n.LocalizedResponseResolver;

import java.util.Locale;


/**
 * LocaleRouter class is a utility class for handling localized message routing
 * It provides functionality to resolve messages and details based on different locales and response codes
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public final class LocaleRouter {

    /**
     * Default localized response resolver as fallback
     */
    private static final LocalizedResponseResolver FALLBACK = new LocalizedResponseResolver() {
    };
    /**
     * Currently used localized response resolver, using volatile keyword to ensure visibility in multi-threaded environment
     */
    private static volatile LocalizedResponseResolver resolver = FALLBACK;

    /**
     * Private constructor to prevent external instantiation of this class
     */
    private LocaleRouter() {
    }

    /**
     * Set default localized response resolver
     * @param localizedResponseResolver Resolver to set, uses default fallback resolver if null
     */
    public static void setDefault(LocalizedResponseResolver localizedResponseResolver) {
        resolver = localizedResponseResolver != null ? localizedResponseResolver : FALLBACK;
    }

    /**
     * Resolve message based on response code and locale
     * @param code Response code
     * @param locale Locale information
     * @return Resolved message string
     */
    public static String resolveMessage(ResponseCode code, Locale locale) {
        return getDefault().resolveMessage(code, locale);
    }

    /**
     * Resolve complete message based on response code, detail, and locale
     * @param code Response code
     * @param detail Detailed information
     * @param locale Locale information
     * @return Resolved complete message string
     */
    public static String resolveDetail(ResponseCode code, String detail, Locale locale) {
        return getDefault().resolveDetail(code, detail, locale);
    }

    /**
     * Get currently used localized response resolver
     * @return Currently used resolver, returns default fallback resolver if null
     */
    private static LocalizedResponseResolver getDefault() {
        return resolver != null ? resolver : FALLBACK;
    }
}
