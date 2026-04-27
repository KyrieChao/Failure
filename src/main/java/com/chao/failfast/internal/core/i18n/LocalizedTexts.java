package com.chao.failfast.internal.core.i18n;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.util.I18n;

import java.util.Locale;


/**
 * LocalizedTexts class provides internationalization text processing functionality, used to obtain localized messages and details.
 * This class contains two static methods for getting localized text based on response code and locale.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class LocalizedTexts {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private LocalizedTexts() {
    }

    /**
     * Get localized message text based on response code.
     *
     * @param code Response code object containing message key
     * @return Returns localized message text, returns default error message if unable to get
     */
    public static String message(ResponseCode code) {
        // If response code is empty, return unknown error message
        if (code == null) {
            return I18n.get(FailureConst.UNKNOWN_ERROR);
        }
        // Get current locale setting
        Locale locale = I18n.getLocale();
        // Try to resolve and get localized message
        String localized = LocaleRouter.resolveMessage(code, locale);
        // If the obtained localized message is not null and not blank, return the message
        if (localized != null && !localized.isBlank()) {
            return localized;
        }
        // Otherwise return the default message defined in the response code
        return I18n.get(code.getMessage());
    }

    /**
     * Get localized detail text based on response code and detail information.
     *
     * @param code Response code object containing message key
     * @param detail Key value of detail information
     * @return Returns localized detail text, returns original detail information if unable to get
     */
    public static String detail(ResponseCode code, String detail) {
        // Get current locale setting
        Locale locale = I18n.getLocale();
        // Try to resolve and get localized detail information
        String localized = LocaleRouter.resolveDetail(code, detail, locale);
        // If the obtained localized detail information is not null and not blank, return the information
        if (localized != null && !localized.isBlank()) {
            return localized;
        }
        // Otherwise return the original detail information
        return I18n.get(detail);
    }
}