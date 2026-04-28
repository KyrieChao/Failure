package com.chao.failure.internal.chain;

import com.chao.failure.constant.FailureConst;
import com.chao.failure.internal.chain.pipeline.ChainCore;
import com.chao.failure.internal.check.StringChecks;
import com.chao.failure.internal.core.ResponseCode;

/**
 * String validation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.3.0
 */
public interface StringTerm<S extends ChainCore<S>> {

    /**
     * Get chain core.
     *
     * @return Chain core instance
     */
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
        return blank(str, FailureConst.BLANK_ERROR, null);
    }

    default S blank(String str, ResponseCode code) {
        return blank(str, code, null);
    }

    default S blank(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.blank(str), code, detail, str);
    }

    default S notBlank(String str) {
        return notBlank(str, FailureConst.NOT_BLANK_ERROR, null);
    }

    default S notBlank(String str, ResponseCode code) {
        return notBlank(str, code, null);
    }

    default S notBlank(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.notBlank(str), code, detail, str);
    }

    default S lengthBetween(String str, int min, int max) {
        return lengthBetween(str, min, max, FailureConst.LENGTH_BETWEEN_ERROR, null);
    }

    default S lengthBetween(String str, int min, int max, ResponseCode code) {
        return lengthBetween(str, min, max, code, null);
    }

    default S lengthBetween(String str, int min, int max, ResponseCode code, String detail) {
        return core().check(StringChecks.lengthBetween(str, min, max), code, detail);
    }

    default S match(String str, String regex) {
        return match(str, regex, FailureConst.MATCH_ERROR, null);
    }

    default S match(String str, String regex, ResponseCode code) {
        return match(str, regex, code, null);
    }

    default S match(String str, String regex, ResponseCode code, String detail) {
        return core().check(StringChecks.match(str, regex), code, detail);
    }

    default S email(String email) {
        return email(email, FailureConst.EMAIL_ERROR, null);
    }

    default S email(String email, ResponseCode code) {
        return email(email, code, null);
    }

    default S email(String email, ResponseCode code, String detail) {
        return core().check(StringChecks.email(email), code, detail, email);
    }

    default S equalsIgnoreCase(String str1, String str2) {
        return equalsIgnoreCase(str1, str2, FailureConst.EQUALS_IGNORE_CASE_ERROR, null);
    }

    default S equalsIgnoreCase(String str1, String str2, ResponseCode code) {
        return equalsIgnoreCase(str1, str2, code, null);
    }

    default S equalsIgnoreCase(String str1, String str2, ResponseCode code, String detail) {
        return core().check(StringChecks.equalsIgnoreCase(str1, str2), code, detail);
    }

    default S startsWith(String str, String prefix) {
        return startsWith(str, prefix, FailureConst.STARTS_WITH_ERROR, null);
    }

    default S startsWith(String str, String prefix, ResponseCode code) {
        return startsWith(str, prefix, code, null);
    }

    default S startsWith(String str, String prefix, ResponseCode code, String detail) {
        return core().check(StringChecks.startsWith(str, prefix), code, detail);
    }

    default S endsWith(String str, String suffix) {
        return endsWith(str, suffix, FailureConst.ENDS_WITH_ERROR, null);
    }

    default S endsWith(String str, String suffix, ResponseCode code) {
        return endsWith(str, suffix, code, null);
    }

    default S endsWith(String str, String suffix, ResponseCode code, String detail) {
        return core().check(StringChecks.endsWith(str, suffix), code, detail);
    }

    default S contains(String str, String substring) {
        return contains(str, substring, FailureConst.CONTAINS_ERROR, null);
    }

    default S contains(String str, String substring, ResponseCode code) {
        return contains(str, substring, code, null);
    }

    default S contains(String str, String substring, ResponseCode code, String detail) {
        return core().check(StringChecks.contains(str, substring), code, detail);
    }

    default S notContains(String str, String substring) {
        return notContains(str, substring, FailureConst.NOT_CONTAINS_ERROR, null);
    }

    default S notContains(String str, String substring, ResponseCode code) {
        return notContains(str, substring, code, null);
    }

    default S notContains(String str, String substring, ResponseCode code, String detail) {
        return core().check(StringChecks.notContains(str, substring), code, detail);
    }

    default S lengthMin(String str, int min) {
        return lengthMin(str, min, FailureConst.LENGTH_MIN_ERROR, null);
    }

    default S lengthMin(String str, int min, ResponseCode code) {
        return lengthMin(str, min, code, null);
    }

    default S lengthMin(String str, int min, ResponseCode code, String detail) {
        return core().check(StringChecks.lengthMin(str, min), code, detail);
    }

    default S lengthMax(String str, int max) {
        return lengthMax(str, max, FailureConst.LENGTH_MAX_ERROR, null);
    }

    default S lengthMax(String str, int max, ResponseCode code) {
        return lengthMax(str, max, code, null);
    }

    default S lengthMax(String str, int max, ResponseCode code, String detail) {
        return core().check(StringChecks.lengthMax(str, max), code, detail);
    }

    default S isNumeric(String str) {
        return isNumeric(str, FailureConst.IS_NUMERIC_ERROR, null);
    }

    default S isNumeric(String str, ResponseCode code) {
        return isNumeric(str, code, null);
    }

    default S isNumeric(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isNumeric(str), code, detail);
    }

    default S isAlpha(String str) {
        return isAlpha(str, FailureConst.IS_ALPHA_ERROR, null);
    }

    default S isAlpha(String str, ResponseCode code) {
        return isAlpha(str, code, null);
    }

    default S isAlpha(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isAlpha(str), code, detail);
    }

    default S isAlphanumeric(String str) {
        return isAlphanumeric(str, FailureConst.IS_ALPHANUMERIC_ERROR, null);
    }

    default S isAlphanumeric(String str, ResponseCode code) {
        return isAlphanumeric(str, code, null);
    }

    default S isAlphanumeric(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isAlphanumeric(str), code, detail);
    }

    default S isLowerCase(String str) {
        return isLowerCase(str, FailureConst.IS_LOWER_CASE_ERROR, null);
    }

    default S isLowerCase(String str, ResponseCode code) {
        return isLowerCase(str, code, null);
    }

    default S isLowerCase(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isLowerCase(str), code, detail);
    }

    default S isUpperCase(String str) {
        return isUpperCase(str, FailureConst.IS_UPPER_CASE_ERROR, null);
    }

    default S isUpperCase(String str, ResponseCode code) {
        return isUpperCase(str, code, null);
    }

    default S isUpperCase(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isUpperCase(str), code, detail);
    }

    default S mobile(String str) {
        return mobile(str, FailureConst.MOBILE_ERROR, null);
    }

    default S mobile(String str, ResponseCode code) {
        return mobile(str, code, null);
    }

    default S mobile(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.mobile(str), code, detail, str);
    }

    default S url(String str) {
        return url(str, FailureConst.URL_ERROR, null);
    }

    default S url(String str, ResponseCode code) {
        return url(str, code, null);
    }

    default S url(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.url(str), code, detail, str);
    }

    default S ipAddress(String str) {
        return ipAddress(str, FailureConst.IP_ADDRESS_ERROR, null);
    }

    default S ipAddress(String str, ResponseCode code) {
        return ipAddress(str, code, null);
    }

    default S ipAddress(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.ipAddress(str), code, detail);
    }

    default S uuid(String str) {
        return uuid(str, FailureConst.UUID_ERROR, null);
    }

    default S uuid(String str, ResponseCode code) {
        return uuid(str, code, null);
    }

    default S uuid(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.uuid(str), code, detail);
    }

    default S isJson(String str) {
        return isJson(str, FailureConst.IS_JSON_ERROR, null);
    }

    default S isJson(String str, ResponseCode code) {
        return isJson(str, code, null);
    }

    default S isJson(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isJson(str), code, detail, str);
    }


    default S isCreditCard(String str) {
        return isCreditCard(str, FailureConst.IS_CREDIT_CARD_ERROR, null);
    }

    default S isCreditCard(String str, ResponseCode code) {
        return isCreditCard(str, code, null);
    }

    default S isCreditCard(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isCreditCard(str), code, detail, str);
    }

    default S isBase64(String str) {
        return isBase64(str, FailureConst.IS_BASE64_ERROR, null);
    }

    default S isBase64(String str, ResponseCode code) {
        return isBase64(str, code, null);
    }

    default S isBase64(String str, ResponseCode code, String detail) {
        return core().check(StringChecks.isBase64(str), code, detail, str);
    }

}
