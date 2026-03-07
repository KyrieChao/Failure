package com.chao.failfast.internal.chain;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.check.StringChecks;
import com.chao.failfast.internal.core.ResponseCode;

public interface StringTerm<S extends ChainCore<S>> {

    S core();

    // Alias: notEmpty -> notBlank
    default S notEmpty(String str) {
        return notBlank(str, FailureConst.NOT_EMPTY_ERROR);
    }
    default S notEmpty(String str, ResponseCode code) {
        return notBlank(str, code);
    }
    default S notEmpty(String str, ResponseCode code, String detail) {
        return notBlank(str, code, detail);
    }

    default S blank(String str) {
        return core().check(StringChecks.blank(str), FailureConst.BLANK_ERROR, null, str);
    }

    default S blank(String str, ResponseCode code) {
        return core().check(StringChecks.blank(str), code, null, str);
    }

    default S blank(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.blank(str), code, detail, str);
    }

    default S notBlank(String str) {
        return core().check(StringChecks.notBlank(str), FailureConst.NOT_BLANK_ERROR, null, str);
    }

    default S notBlank(String str, ResponseCode code) {
        return core().check(StringChecks.notBlank(str), code, null, str);
    }

    default S notBlank(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.notBlank(str), code, detail, str);
    }

    default S lengthBetween(String str, int min, int max) {
        return core().check(StringChecks.lengthBetween(str, min, max), FailureConst.LENGTH_BETWEEN_ERROR, null);
    }

    default S lengthBetween(String str, int min, int max, ResponseCode code) {
        return core().check(StringChecks.lengthBetween(str, min, max), code, null);
    }

    default S lengthBetween(String str, int min, int max, ResponseCode code, String detail) {
        return core().check(StringChecks.lengthBetween(str, min, max), code, detail);
    }

    default S match(String str, String regex) {
        return core().check(StringChecks.match(str, regex), FailureConst.MATCH_ERROR, null);
    }

    default S match(String str, String regex, ResponseCode code) {
        return core().check(StringChecks.match(str, regex), code, null);
    }

    default S match(String str, String regex, ResponseCode code, String detail) {
        return core().check(StringChecks.match(str, regex), code, detail);
    }

    default S email(String email) {
        return core().check(StringChecks.email(email), FailureConst.EMAIL_ERROR, null, email);
    }

    default S email(String email, ResponseCode code) {
        return core().check(StringChecks.email(email), code, null, email);
    }

    default S email(String email, ResponseCode code, String detail) {
        return core().check(StringChecks.email(email), code, detail, email);
    }

    default S equalsIgnoreCase(String str1, String str2) {
        return core().check(StringChecks.equalsIgnoreCase(str1, str2), FailureConst.EQUALS_IGNORE_CASE_ERROR, null);
    }

    default S equalsIgnoreCase(String str1, String str2, ResponseCode code) {
        return core().check(StringChecks.equalsIgnoreCase(str1, str2), code, null);
    }

    default S equalsIgnoreCase(String str1, String str2, ResponseCode code, String detail) {
        return core().check(StringChecks.equalsIgnoreCase(str1, str2), code, detail);
    }

    default S startsWith(String str, String prefix) {
        return core().check(StringChecks.startsWith(str, prefix), FailureConst.STARTS_WITH_ERROR, null);
    }

    default S startsWith(String str, String prefix, ResponseCode code) {
        return core().check(StringChecks.startsWith(str, prefix), code, null);
    }

    default S startsWith(String str, String prefix, ResponseCode code, String detail) {
        return core().check(StringChecks.startsWith(str, prefix), code, detail);
    }

    default S endsWith(String str, String suffix) {
        return core().check(StringChecks.endsWith(str, suffix), FailureConst.ENDS_WITH_ERROR, null);
    }

    default S endsWith(String str, String suffix, ResponseCode code) {
        return core().check(StringChecks.endsWith(str, suffix), code, null);
    }

    default S endsWith(String str, String suffix, ResponseCode code, String detail) {
        return core().check(StringChecks.endsWith(str, suffix), code, detail);
    }

    default S contains(String str, String substring) {
        return core().check(StringChecks.contains(str, substring), FailureConst.CONTAINS_ERROR, null);
    }

    default S contains(String str, String substring, ResponseCode code) {
        return core().check(StringChecks.contains(str, substring), code, null);
    }

    default S contains(String str, String substring, ResponseCode code, String detail) {
        return core().check(StringChecks.contains(str, substring), code, detail);
    }

    default S notContains(String str, String substring) {
        return core().check(StringChecks.notContains(str, substring), FailureConst.NOT_CONTAINS_ERROR, null);
    }

    default S notContains(String str, String substring, ResponseCode code) {
        return core().check(StringChecks.notContains(str, substring), code, null);
    }

    default S notContains(String str, String substring, ResponseCode code, String detail) {
        return core().check(StringChecks.notContains(str, substring), code, detail);
    }

    default S lengthMin(String str, int min) {
        return core().check(StringChecks.lengthMin(str, min), FailureConst.LENGTH_MIN_ERROR, null);
    }

    default S lengthMin(String str, int min, ResponseCode code) {
        return core().check(StringChecks.lengthMin(str, min), code, null);
    }

    default S lengthMin(String str, int min, ResponseCode code, String detail) {
        return core().check(StringChecks.lengthMin(str, min), code, detail);
    }

    default S lengthMax(String str, int max) {
        return core().check(StringChecks.lengthMax(str, max), FailureConst.LENGTH_MAX_ERROR, null);
    }

    default S lengthMax(String str, int max, ResponseCode code) {
        return core().check(StringChecks.lengthMax(str, max), code, null);
    }

    default S lengthMax(String str, int max, ResponseCode code, String detail) {
        return core().check(StringChecks.lengthMax(str, max), code, detail);
    }

    default S isNumeric(String str) {
        return core().check(StringChecks.isNumeric(str), FailureConst.IS_NUMERIC_ERROR, null);
    }

    default S isNumeric(String str, ResponseCode code) {
        return core().check(StringChecks.isNumeric(str), code, null);
    }

    default S isNumeric(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isNumeric(str), code, detail);
    }

    default S isAlpha(String str) {
        return core().check(StringChecks.isAlpha(str), FailureConst.IS_ALPHA_ERROR, null);
    }

    default S isAlpha(String str, ResponseCode code) {
        return core().check(StringChecks.isAlpha(str), code, null);
    }

    default S isAlpha(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isAlpha(str), code, detail);
    }

    default S isAlphanumeric(String str) {
        return core().check(StringChecks.isAlphanumeric(str), FailureConst.IS_ALPHANUMERIC_ERROR, null);
    }

    default S isAlphanumeric(String str, ResponseCode code) {
        return core().check(StringChecks.isAlphanumeric(str), code, null);
    }

    default S isAlphanumeric(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isAlphanumeric(str), code, detail);
    }

    default S isLowerCase(String str) {
        return core().check(StringChecks.isLowerCase(str), FailureConst.IS_LOWER_CASE_ERROR, null);
    }

    default S isLowerCase(String str, ResponseCode code) {
        return core().check(StringChecks.isLowerCase(str), code, null);
    }

    default S isLowerCase(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isLowerCase(str), code, detail);
    }

    default S isUpperCase(String str) {
        return core().check(StringChecks.isUpperCase(str), FailureConst.IS_UPPER_CASE_ERROR, null);
    }

    default S isUpperCase(String str, ResponseCode code) {
        return core().check(StringChecks.isUpperCase(str), code, null);
    }

    default S isUpperCase(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isUpperCase(str), code, detail);
    }

    default S mobile(String str) {
        return core().check(StringChecks.mobile(str), FailureConst.MOBILE_ERROR, null, str);
    }

    default S mobile(String str, ResponseCode code) {
        return core().check(StringChecks.mobile(str), code, null, str);
    }

    default S mobile(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.mobile(str), code, detail, str);
    }

    default S url(String str) {
        return core().check(StringChecks.url(str), FailureConst.URL_ERROR, null, str);
    }

    default S url(String str, ResponseCode code) {
        return core().check(StringChecks.url(str), code, null, str);
    }

    default S url(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.url(str), code, detail, str);
    }

    default S ipAddress(String str) {
        return core().check(StringChecks.ipAddress(str), FailureConst.IP_ADDRESS_ERROR, null);
    }

    default S ipAddress(String str, ResponseCode code) {
        return core().check(StringChecks.ipAddress(str), code, null);
    }

    default S ipAddress(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.ipAddress(str), code, detail);
    }

    default S uuid(String str) {
        return core().check(StringChecks.uuid(str), FailureConst.UUID_ERROR, null);
    }

    default S uuid(String str, ResponseCode code) {
        return core().check(StringChecks.uuid(str), code, null);
    }

    default S uuid(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.uuid(str), code, detail);
    }

    default S isJson(String str) {
        return core().check(StringChecks.isJson(str), FailureConst.IS_JSON_ERROR, null, str);
    }

    default S isJson(String str, ResponseCode code) {
        return core().check(StringChecks.isJson(str), code, null, str);
    }

    default S isJson(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isJson(str), code, detail, str);
    }

    default S isCreditCard(String str) {
        return core().check(StringChecks.isCreditCard(str), FailureConst.IS_CREDIT_CARD_ERROR, null, str);
    }

    default S isCreditCard(String str, ResponseCode code) {
        return core().check(StringChecks.isCreditCard(str), code, null, str);
    }

    default S isCreditCard(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isCreditCard(str), code, detail, str);
    }

    default S isBase64(String str) {
        return core().check(StringChecks.isBase64(str), FailureConst.IS_BASE64_ERROR, null, str);
    }

    default S isBase64(String str, ResponseCode code) {
        return core().check(StringChecks.isBase64(str), code, null, str);
    }

    default S isBase64(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isBase64(str), code, detail, str);
    }

}
