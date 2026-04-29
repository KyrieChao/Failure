package com.chao.failure.config.masking;

import com.chao.failure.constant.FailureConst;
import com.chao.failure.spi.security.Mask;
import com.chao.failure.spi.security.ValueMasker;

import java.util.Locale;
import java.util.regex.Matcher;


/**
 * DefaultValueMasker is a class that implements ValueMasker interface, used for masking sensitive data
 * This class can determine if a field is sensitive based on its path, and perform appropriate masking or truncation formatting for different types of data
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public class DefaultValueMasker implements ValueMasker {

    @Override
    public Object mask(Object value) {
        if (value == null) {
            return null;
        }
        return truncateAndFormat(value);
    }

    @Override
    public Object mask(Object value, Mask mask) {
        if (value == null) return null;
        if (mask == null) return truncateAndFormat(value);

        String code = mask.type();
        if (code == null || code.isBlank()) {
            return truncateAndFormat(value);
        }
        String type = code.toLowerCase(Locale.ROOT);
        String str = value.toString();
        if (str.isEmpty()) {
            return str;
        }
        switch (type) {
            case "phone" -> {
                if (FailureConst.Mobile.matcher(str).matches()) {
                    return str.substring(0, 3) + "****" + str.substring(7);
                }
                return "***[MASKED]***";
            }
            case "email" -> {
                Matcher emailMatcher = FailureConst.Email.matcher(str);
                if (emailMatcher.matches()) {
                    return emailMatcher.group(1) + "****" + emailMatcher.group(3);
                }
                return "***[MASKED]***";
            }
            case "bankcard", "creditcard", "card" -> {
                if (FailureConst.Card.matcher(str).matches()) {
                    return str.substring(0, 4) + "****" + str.substring(str.length() - 4);
                }
                return "***[MASKED]***";
            }
        }
        return "***[MASKED]***";
    }

    /**
     * Truncate and format non-sensitive field values, applying different masking rules based on data type
     * Supports automatic identification and masking of phone numbers, emails, bank card numbers, and truncation of long strings
     *
     * @param value Original value object to format
     * @return Formatted string:
     *         - Phone number: Keep first 3 and last 4 digits, replace middle with **** (e.g., 138****5678)
     *         - Email: Keep first character of username and domain part, replace middle with **** (e.g., a****example.com)
     *         - Bank card number: Keep first 4 and last 4 digits, replace middle with **** (e.g., 6222****1234)
     *         - Long string (>50 characters): Keep first 5 and last 5 characters, show length information in the middle
     *         - Other strings: Return as-is
     */
    private Object truncateAndFormat(Object value) {
        String str = value.toString();
        if (str.isEmpty()) {
            return str;
        }
        if (FailureConst.Mobile.matcher(str).matches()) {
            return str.substring(0, 3) + "****" + str.substring(7);
        }

        Matcher emailMatcher = FailureConst.Email.matcher(str);
        if (emailMatcher.matches()) {
            return emailMatcher.group(1) + "****" + emailMatcher.group(3);
        }
        if (FailureConst.Card.matcher(str).matches()) {
            return str.substring(0, 4) + "****" + str.substring(str.length() - 4);
        }
        if (str.length() > 50) {
            return str.substring(0, 5) + "...(" + str.length() + "char)..." + str.substring(str.length() - 5);
        }
        return str;
    }
}
