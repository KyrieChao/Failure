package com.chao.failfast.config.i18n;

import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.spi.i18n.LocalizedResponseResolver;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class LocaleResponseResolver implements LocalizedResponseResolver {
    private final Map<Key, String> messages = new ConcurrentHashMap<>();
    private final Map<Key, String> details = new ConcurrentHashMap<>();

    /**
     * Put localized message for specific locale and code.
     *
     * @param locale  Locale object
     * @param code    Response code
     * @param message Localized message
     * @return This resolver instance for method chaining
     */
    public LocaleResponseResolver putMessage(Locale locale, int code, String message) {
        if (locale == null || message == null) {
            return this;
        }
        messages.put(new Key(localeKey(locale), code), message);
        return this;
    }

    /**
     * Put localized detail for specific locale and code.
     *
     * @param locale Locale object
     * @param code   Response code
     * @param detail Localized detail
     * @return This resolver instance for method chaining
     */
    public LocaleResponseResolver putDetail(Locale locale, int code, String detail) {
        if (locale == null || detail == null) {
            return this;
        }
        details.put(new Key(localeKey(locale), code), detail);
        return this;
    }

    /**
     * Resolve localized message for response code and locale.
     *
     * @param code   Response code
     * @param locale Locale object
     * @return Localized message or null if not found
     */
    @Override
    public String resolveMessage(ResponseCode code, Locale locale) {
        if (code == null || locale == null) {
            return null;
        }
        String exact = messages.get(new Key(localeKey(locale), code.getCode()));
        if (exact != null && !exact.isBlank()) {
            return exact;
        }
        String lang = messages.get(new Key(locale.getLanguage(), code.getCode()));
        if (lang != null && !lang.isBlank()) {
            return lang;
        }
        return null;
    }

    /**
     * Resolve localized detail for response code, detail and locale.
     *
     * @param code   Response code
     * @param detail Original detail
     * @param locale Locale object
     * @return Localized detail or null if not found
     */
    @Override
    public String resolveDetail(ResponseCode code, String detail, Locale locale) {
        if (code == null || locale == null) {
            return null;
        }
        String exact = details.get(new Key(localeKey(locale), code.getCode()));
        if (exact != null && !exact.isBlank()) {
            return exact;
        }
        String lang = details.get(new Key(locale.getLanguage(), code.getCode()));
        if (lang != null && !lang.isBlank()) {
            return lang;
        }
        return null;
    }

    private static String localeKey(Locale locale) {
        if (locale == null) {
            return "";
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (country.isBlank()) {
            return language;
        }
        return language + "_" + country;
    }

    private record Key(String locale, int code) {
        private Key {
            Objects.requireNonNull(locale, "locale");
        }
    }
}

