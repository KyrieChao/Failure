package com.chao.failfast.internal.check;

import com.chao.failfast.constant.FailureConst;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Base64;

/**
 * 字符串校验工具类
 * 提供各种常用的字符串校验方法，如空值校验、长度校验、格式校验等
 */
public final class StringChecks {

    // 私有构造方法，防止实例化工具类
    private StringChecks() {
    }

    // Lazy initialization holder for Jackson ObjectMapper
    private static class JsonHolder {
        static final ObjectMapper MAPPER = new ObjectMapper();

    }

    /**
     * 检查字符串是否为空或空白
     *
     * @param str 要检查的字符串
     * @return 如果字符串为null或空白字符串返回true，否则返回false
     */
    public static boolean blank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 检查字符串是否非空且非空白
     *
     * @param str 要检查的字符串
     * @return 如果字符串非null且非空白字符串返回true，否则返回false
     */
    public static boolean notBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 检查字符串长度是否在指定范围内
     *
     * @param str 要检查的字符串
     * @param min 最小长度（包含）
     * @param max 最大长度（包含）
     * @return 如果字符串长度在min和max之间返回true，否则返回false
     */
    public static boolean lengthBetween(String str, int min, int max) {
        return str != null && str.length() >= min && str.length() <= max;
    }

    /**
     * 检查字符串是否匹配指定的正则表达式
     *
     * @param str   要检查的字符串
     * @param regex 正则表达式
     * @return 如果字符串匹配正则表达式返回true，否则返回false
     */
    public static boolean match(String str, String regex) {
        return str != null && str.matches(regex);
    }

    /**
     * 检查字符串是否为有效的邮箱格式
     *
     * @param email 要检查的邮箱字符串
     * @return 如果字符串是有效的邮箱格式返回true，否则返回false
     */
    public static boolean email(String email) {
        return email != null && FailureConst.Email_Pattern.matcher(email).matches();
    }

    /**
     * 比较两个字符串是否相等（忽略大小写）
     *
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 如果两个字符串相等（忽略大小写）返回true，否则返回false
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 != null && str1.equalsIgnoreCase(str2);
    }

    /**
     * 检查字符串是否以指定的前缀开头
     *
     * @param str    要检查的字符串
     * @param prefix 前缀字符串
     * @return 如果字符串以指定前缀开头返回true，否则返回false
     */
    public static boolean startsWith(String str, String prefix) {
        return str != null && str.startsWith(prefix);
    }

    /**
     * 检查字符串是否以指定的后缀结尾
     *
     * @param str    要检查的字符串
     * @param suffix 后缀字符串
     * @return 如果字符串以指定后缀结尾返回true，否则返回false
     */
    public static boolean endsWith(String str, String suffix) {
        return str != null && str.endsWith(suffix);
    }

    /**
     * 检查字符串是否包含指定的子字符串
     *
     * @param str       要检查的字符串
     * @param substring 要查找的子字符串
     * @return 如果字符串包含指定的子字符串返回true，否则返回false
     */
    public static boolean contains(String str, String substring) {
        return str != null && substring != null && str.contains(substring);
    }

    /**
     * 检查字符串是否不包含指定的子字符串
     *
     * @param str       要检查的字符串
     * @param substring 要查找的子字符串
     * @return 如果字符串不包含指定的子字符串返回true，否则返回false
     */
    public static boolean notContains(String str, String substring) {
        return str == null || substring == null || !str.contains(substring);
    }

    /**
     * 检查字符串长度是否大于等于指定值
     *
     * @param str 要检查的字符串
     * @param min 最小长度
     * @return 如果字符串长度大于等于min返回true，否则返回false
     */
    public static boolean lengthMin(String str, int min) {
        return str != null && str.length() >= min;
    }

    /**
     * 检查字符串长度是否小于等于指定值
     *
     * @param str 要检查的字符串
     * @param max 最大长度
     * @return 如果字符串长度小于等于max返回true，否则返回false
     */
    public static boolean lengthMax(String str, int max) {
        return str != null && str.length() <= max;
    }

    /**
     * 检查字符串是否全部由数字组成
     *
     * @param str 要检查的字符串
     * @return 如果字符串全部由数字组成返回true，否则返回false
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
     * 检查字符串是否全部由字母组成
     *
     * @param str 要检查的字符串
     * @return 如果字符串全部由字母组成返回true，否则返回false
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
     * 检查字符串是否全部由字母或数字组成
     *
     * @param str 要检查的字符串
     * @return 如果字符串全部由字母或数字组成返回true，否则返回false
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
     * 检查字符串是否全部由小写字母组成
     *
     * @param str 要检查的字符串
     * @return 如果字符串全部由小写字母组成返回true，否则返回false
     */
    public static boolean isLowerCase(String str) {
        return str != null && str.equals(str.toLowerCase());
    }

    /**
     * 检查字符串是否全部由大写字母组成
     *
     * @param str 要检查的字符串
     * @return 如果字符串全部由大写字母组成返回true，否则返回false
     */
    public static boolean isUpperCase(String str) {
        return str != null && str.equals(str.toUpperCase());
    }

    /**
     * 检查字符串是否为有效的手机号码格式
     *
     * @param str 要检查的字符串
     * @return 如果字符串是有效的手机号码格式返回true，否则返回false
     */
    public static boolean mobile(String str) {
        return str != null && FailureConst.Mobile.matcher(str).matches();
    }

    /**
     * 检查字符串是否为有效的 URL 格式
     * 使用 java.net.URI 进行严格校验
     *
     * @param str 要检查的字符串
     * @return 如果字符串是有效的 URL 格式返回 true，否则返回 false
     */
    public static boolean url(String str) {
        if (str == null || str.isBlank()) return false;
        try {
            URI uri = new URI(str);
            // 验证 URI 可以转换为 URL 且协议有效
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查字符串是否为有效的JSON格式
     *
     * @param str 要检查的字符串
     * @return 如果字符串是有效的JSON格式返回true，否则返回false
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
     * 检查字符串是否为有效的 Base64 编码
     *
     * @param str 要检查的字符串
     * @return 如果字符串是有效的 Base64 编码返回true，否则返回false
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
     * 检查字符串是否为有效的信用卡号 (Luhn 算法)
     *
     * @param str 要检查的字符串
     * @return 如果字符串是有效的信用卡号返回true，否则返回false
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

    public static boolean ipAddress(String str) {
        // Simple regex for IPv4
        return str != null && FailureConst.IP4.matcher(str).matches();
    }

    public static boolean uuid(String str) {
        return str != null && FailureConst.UUID.matcher(str).matches();
    }
}
