package com.chao.failure.spi.i18n;

import com.chao.failure.internal.core.ResponseCode;

import java.util.Locale;

/**
 * Resolve localized text by response code and locale.
 */
public interface LocalizedResponseResolver {

    /**
     * Resolve message text for the given response code.
     *
     * @param code   response code
     * @param locale locale
     * @return localized message, or null to use framework fallback
     */
    default String resolveMessage(ResponseCode code, Locale locale) {
        return null;
    }

    /**
     * Resolve detail/description text for the given response code.
     *
     * @param code   response code
     * @param detail default detail
     * @param locale locale
     * @return localized detail, or null to use framework fallback
     */
    default String resolveDetail(ResponseCode code, String detail, Locale locale) {
        return null;
    }
}