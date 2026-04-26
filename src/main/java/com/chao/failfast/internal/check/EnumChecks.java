package com.chao.failfast.internal.check;

/**
 * Utility class for enum validation.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class EnumChecks {

    private EnumChecks() {}

    /**
     * Checks if the string value is a valid enum constant.
     *
     * @param <E>      the enum type
     * @param enumType the enum class
     * @param value    the string value to check
     * @return true if the value is a valid enum constant, false otherwise
     */
    public static <E extends Enum<E>> boolean enumValue(Class<E> enumType, String value) {
        if (enumType == null || value == null) {
            return false;
        }
        try {
            Enum.valueOf(enumType, value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if the object is an instance of the enum type.
     *
     * @param <E>   the enum type
     * @param value the object to check
     * @param type  the enum class
     * @return true if the object is an instance of the enum type, false otherwise
     */
    public static <E extends Enum<E>> boolean enumConstant(E value, Class<E> type) {
        return type != null && type.isInstance(value);
    }
}
