package com.chao.failfast.config.masking;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.spi.security.ValueMasker;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;


/**
 * DefaultValueMasker is a class that implements ValueMasker interface, used for masking sensitive data
 * This class can determine if a field is sensitive based on its path, and perform appropriate masking or truncation formatting for different types of data
 */
public class DefaultValueMasker implements ValueMasker {

    // Define a set of sensitive data patterns used to determine if a field contains sensitive information
    private static final Set<String> SENSITIVE_PATTERNS = Set.of(
            "password", "token", "secret", "idcard", "ssn",
            "creditcard", "bankcard", "apikey"
    );

    /**
     * Main method for masking input values
     * @param value Original value to process
     * @param fieldPath Field path used to determine if it's a sensitive field
     * @return Processed value, returns masked string for sensitive fields, otherwise returns truncated and formatted value
     */
    @Override
    public Object mask(Object value, String fieldPath) {
        if (value == null) {  // If value is null, return null directly
            return null;
        }
        if (isSensitive(fieldPath)) {  // Check if field path contains sensitive patterns
            return "***[MASKED]***";  // If it's a sensitive field, return masking marker
        }
        return truncateAndFormat(value);  // Otherwise, perform truncation and formatting on the value
    }

    /**
     * Determine if field path contains sensitive patterns
     * @param fieldPath Field path to check
     * @return true if field path contains any sensitive pattern, false otherwise
     */
    private boolean isSensitive(String fieldPath) {
        if (fieldPath == null || fieldPath.isBlank()) {
            return false;
        }
        String path = fieldPath.toLowerCase(Locale.ROOT);
        for (String pattern : SENSITIVE_PATTERNS) {
            if (path.contains(pattern)) {
                return true;
            }
        }
        return false;
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

        // Detect and mask phone numbers
        if (FailureConst.Mobile.matcher(str).matches()) {
            return str.substring(0, 3) + "****" + str.substring(7);
        }

        // Detect and mask email addresses
        Matcher emailMatcher = FailureConst.Email.matcher(str);
        if (emailMatcher.matches()) {
            return emailMatcher.group(1) + "****" + emailMatcher.group(3);
        }

        // Detect and mask bank card numbers
        if (FailureConst.Card.matcher(str).matches()) {
            return str.substring(0, 4) + "****" + str.substring(str.length() - 4);
        }

        // Truncate long strings to avoid excessive log length
        if (str.length() > 50) {
            return str.substring(0, 5) + "...(" + str.length() + "char)..." + str.substring(str.length() - 5);
        }
        return str;
    }
}
