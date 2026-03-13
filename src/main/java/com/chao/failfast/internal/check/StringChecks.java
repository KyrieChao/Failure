package com.chao.failfast.internal.check;

import com.chao.failfast.constant.FailureConst;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Base64;

/**
 * Utility class for string validation.
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public final class StringChecks {

    private StringChecks() {
    }

    // Lazy initialization holder for Jackson ObjectMapper
    private static class JsonHolder {
        static final ObjectMapper MAPPER = new ObjectMapper();
    }

    /**
     * Checks if the string is blank (null or whitespace only).
     *
     * @param str the string to check
     * @return true if the string is blank, false otherwise
     */
    public static boolean blank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Checks if the string is not blank.
     *
     * @param str the string to check
     * @return true if the string is not blank, false otherwise
     */
    public static boolean notBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Checks if the string length is within the specified range.
     *
     * @param str the string to check
     * @param min the minimum length (inclusive)
     * @param max the maximum length (inclusive)
     * @return true if the string length is within [min, max], false otherwise
     */
    public static boolean lengthBetween(String str, int min, int max) {
        return str != null && str.length() >= min && str.length() <= max;
    }

    /**
     * Checks if the string matches the regular expression.
     *
     * @param str   the string to check
     * @param regex the regular expression
     * @return true if the string matches the regex, false otherwise
     */
    public static boolean match(String str, String regex) {
        return str != null && str.matches(regex);
    }

    /**
     * Checks if the string is a valid email address.
     *
     * @param email the email string to check
     * @return true if the string is a valid email, false otherwise
     */
    public static boolean email(String email) {
        return email != null && FailureConst.Email_Pattern.matcher(email).matches();
    }

    /**
     * Checks if two strings are equal, ignoring case.
     *
     * @param str1 the first string
     * @param str2 the second string
     * @return true if the strings are equal ignoring case, false otherwise
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 != null && str1.equalsIgnoreCase(str2);
    }

    /**
     * Checks if the string starts with the specified prefix.
     *
     * @param str    the string to check
     * @param prefix the prefix string
     * @return true if the string starts with the prefix, false otherwise
     */
    public static boolean startsWith(String str, String prefix) {
        return str != null && str.startsWith(prefix);
    }

    /**
     * Checks if the string ends with the specified suffix.
     *
     * @param str    the string to check
     * @param suffix the suffix string
     * @return true if the string ends with the suffix, false otherwise
     */
    public static boolean endsWith(String str, String suffix) {
        return str != null && str.endsWith(suffix);
    }

    /**
     * Checks if the string contains the specified substring.
     *
     * @param str       the string to check
     * @param substring the substring to find
     * @return true if the string contains the substring, false otherwise
     */
    public static boolean contains(String str, String substring) {
        return str != null && substring != null && str.contains(substring);
    }

    /**
     * Checks if the string does not contain the specified substring.
     *
     * @param str       the string to check
     * @param substring the substring to exclude
     * @return true if the string does not contain the substring, false otherwise
     */
    public static boolean notContains(String str, String substring) {
        return str == null || substring == null || !str.contains(substring);
    }

    /**
     * Checks if the string length is greater than or equal to the minimum.
     *
     * @param str the string to check
     * @param min the minimum length
     * @return true if the string length is >= min, false otherwise
     */
    public static boolean lengthMin(String str, int min) {
        return str != null && str.length() >= min;
    }

    /**
     * Checks if the string length is less than or equal to the maximum.
     *
     * @param str the string to check
     * @param max the maximum length
     * @return true if the string length is <= max, false otherwise
     */
    public static boolean lengthMax(String str, int max) {
        return str != null && str.length() <= max;
    }

    /**
     * Checks if the string contains only numeric characters.
     *
     * @param str the string to check
     * @return true if the string contains only numeric characters, false otherwise
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the string contains only letters.
     *
     * @param str the string to check
     * @return true if the string contains only letters, false otherwise
     */
    public static boolean isAlpha(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the string contains only alphanumeric characters.
     *
     * @param str the string to check
     * @return true if the string contains only alphanumeric characters, false otherwise
     */
    public static boolean isAlphanumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the string contains only lowercase letters.
     *
     * @param str the string to check
     * @return true if the string contains only lowercase letters, false otherwise
     */
    public static boolean isLowerCase(String str) {
        return str != null && str.equals(str.toLowerCase());
    }

    /**
     * Checks if the string contains only uppercase letters.
     *
     * @param str the string to check
     * @return true if the string contains only uppercase letters, false otherwise
     */
    public static boolean isUpperCase(String str) {
        return str != null && str.equals(str.toUpperCase());
    }

    /**
     * Checks if the string is a valid mobile phone number.
     *
     * @param str the string to check
     * @return true if the string is a valid mobile number, false otherwise
     */
    public static boolean mobile(String str) {
        return str != null && FailureConst.Mobile.matcher(str).matches();
    }

    /**
     * Checks if the string is a valid URL.
     *
     * @param str the string to check
     * @return true if the string is a valid URL, false otherwise
     */
    public static boolean url(String str) {
        if (str == null || str.isBlank()) return false;
        try {
            URI uri = new URI(str);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the string is a valid JSON.
     *
     * @param str the string to check
     * @return true if the string is a valid JSON, false otherwise
     */
    public static boolean isJson(String str) {
        if (str == null || str.isBlank()) return false;
        try {
            JsonHolder.MAPPER.readTree(str);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Checks if the string is a valid Base64 encoded string.
     *
     * @param str the string to check
     * @return true if the string is a valid Base64 string, false otherwise
     */
    public static boolean isBase64(String str) {
        if (str == null || str.isBlank()) return false;
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if the string is a valid credit card number (Luhn algorithm).
     *
     * @param str the string to check
     * @return true if the string is a valid credit card number, false otherwise
     */
    public static boolean isCreditCard(String str) {
        if (str == null || !str.matches("\\d+")) return false;
        int sum = 0;
        boolean alternate = false;
        for (int i = str.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(str.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    /**
     * Checks if the string is a valid IPv4 address.
     *
     * @param str the string to check
     * @return true if the string is a valid IPv4 address, false otherwise
     */
    public static boolean ipAddress(String str) {
        return str != null && FailureConst.IP4.matcher(str).matches();
    }

    /**
     * Checks if the string is a valid UUID.
     *
     * @param str the string to check
     * @return true if the string is a valid UUID, false otherwise
     */
    public static boolean uuid(String str) {
        return str != null && FailureConst.UUID.matcher(str).matches();
    }
}
