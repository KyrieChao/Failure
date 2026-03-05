package com.chao.failfast.constant;

import com.chao.failfast.internal.core.ResponseCode;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Failure 框架全局常量定义。
 *
 * <p>包含验证、配置、响应等模块的共享常量，统一维护避免魔法值散落。</p>
 *
 * @author KyrieChao
 * @since 1.3.1
 */
public final class FailureConst {

    /**
     * 私有构造方法，防止该工具类被实例化
     */
    private FailureConst() {
    }

    // ==================== 响应字段名（JSON 字段） ====================

    /**
     * 错误码字段
     */
    public static final String FIELD_CODE = "code";

    /**
     * 错误消息字段
     */
    public static final String FIELD_MESSAGE = "message";

    /**
     * 错误详情字段
     */
    public static final String FIELD_DESCRIPTION = "description";

    /**
     * 错误明细字段
     */
    public static final String FIELD_DETAIL = "detail";

    /**
     * 时间戳字段
     */
    public static final String FIELD_TIMESTAMP = "timestamp";

    /**
     * 错误列表字段（详细模式）
     */
    public static final String FIELD_ERRORS = "errors";


    // ==================== 通用错误消息（英文） ====================

    /**
     * 参数无效
     */
    public static final String INVALID_PARAMETER = "Invalid parameter";

    /**
     * 未知错误
     */
    public static final String UNKNOWN_ERROR = "Unknown error";

    /**
     * 未知
     */
    public static final String UNKNOWN = "Unknown";

    /**
     * 多重校验错误
     */
    public static final String MULTIPLE_VALIDATION_ERRORS = "Multiple validation errors";

    /**
     * 位置分隔符
     */
    public static final String AT = " at ";

    /**
     * 校验错误
     */
    public static final String VALIDATION_ERROR = "Unknown validation error";

    /**
     * 无法缩减空列表
     */
    public static final String CANNOT_REDUCE_EMPTY_LIST = "Cannot reduce empty list";


    // ==================== 错误提示模板（中文） ====================

    /**
     * code 不能为空
     */
    public static final String CODE_REQUIRED = "code 不能为空";

    /**
     * message 或 description 至少一个不能为 null
     */
    public static final String MESSAGE_OR_DESCRIPTION_REQUIRED = "message 或 description 至少一个不能为 null";

    /**
     * 不支持的校验类型前缀
     */
    public static final String UNSUPPORTED_VALIDATION_TYPE = "不支持的校验类型: ";

    /**
     * 校验失败前缀
     */
    public static final String VALIDATION_ERROR_PREFIX = "校验失败,共";

    /**
     * 错误项后缀
     */
    public static final String ERROR_ITEM_SUFFIX = " 项问题";

    /**
     * 错误过多提示
     */
    public static final String TOO_MANY_ERRORS = "校验失败，错误过多";


    // ==================== 系统默认值 ====================

    /**
     * 系统默认错误码
     */
    public static final Integer SYSTEM_CODE = 500;

    /**
     * 默认错误消息
     */
    public static final String DEFAULT_MESSAGE = "参数绑定失败";


    // ==================== 时间相关 ====================

    /**
     * 中国标准时间（Asia/Shanghai）
     */
    public static final ZoneId CST = ZoneId.of("Asia/Shanghai");

    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认日期时间格式化器
     */
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);


    // ==================== 自动生成的方法默认错误码 ====================

    /**
     * notBlank 默认错误
     */
    public static final ResponseCode NOT_BLANK_ERROR = ResponseCode.of(500, "Current value notBlank", "当前值 不能为空");
    /**
     * notEmpty 默认错误
     */
    public static final ResponseCode NOT_EMPTY_ERROR = ResponseCode.of(500, "Current value notEmpty", "当前值 不能为空");
    /**
     * blank 默认错误
     */
    public static final ResponseCode BLANK_ERROR = ResponseCode.of(500, "Current value blank", "当前值 必须为空");
    /**
     * lengthBetween 默认错误
     */
    public static final ResponseCode LENGTH_BETWEEN_ERROR = ResponseCode.of(500, "Current value lengthBetween", "当前值 长度必须在指定范围内");
    /**
     * lengthMin 默认错误
     */
    public static final ResponseCode LENGTH_MIN_ERROR = ResponseCode.of(500, "Current value lengthMin", "当前值 长度不能小于指定值");
    /**
     * lengthMax 默认错误
     */
    public static final ResponseCode LENGTH_MAX_ERROR = ResponseCode.of(500, "Current value lengthMax", "当前值 长度不能大于指定值");
    /**
     * match 默认错误
     */
    public static final ResponseCode MATCH_ERROR = ResponseCode.of(500, "Current value match", "当前值 格式不匹配");
    /**
     * email 默认错误
     */
    public static final ResponseCode EMAIL_ERROR = ResponseCode.of(500, "Current value email", "当前值 不是有效的邮箱格式");
    /**
     * mobile 默认错误
     */
    public static final ResponseCode MOBILE_ERROR = ResponseCode.of(500, "Current value mobile", "当前值 不是有效的手机号格式");
    /**
     * url 默认错误
     */
    public static final ResponseCode URL_ERROR = ResponseCode.of(500, "Current value url", "当前值 不是有效的URL格式");
    /**
     * ipAddress 默认错误
     */
    public static final ResponseCode IP_ADDRESS_ERROR = ResponseCode.of(500, "Current value ipAddress", "当前值 不是有效的IP地址");
    /**
     * uuid 默认错误
     */
    public static final ResponseCode UUID_ERROR = ResponseCode.of(500, "Current value uuid", "当前值 不是有效的UUID");
    /**
     * isNumeric 默认错误
     */
    public static final ResponseCode IS_NUMERIC_ERROR = ResponseCode.of(500, "Current value isNumeric", "当前值 必须是数字");
    /**
     * isAlpha 默认错误
     */
    public static final ResponseCode IS_ALPHA_ERROR = ResponseCode.of(500, "Current value isAlpha", "当前值 必须是字母");
    /**
     * isAlphanumeric 默认错误
     */
    public static final ResponseCode IS_ALPHANUMERIC_ERROR = ResponseCode.of(500, "Current value isAlphanumeric", "当前值 必须是字母或数字");
    /**
     * startsWith 默认错误
     */
    public static final ResponseCode STARTS_WITH_ERROR = ResponseCode.of(500, "Current value startsWith", "当前值 必须以指定前缀开头");
    /**
     * endsWith 默认错误
     */
    public static final ResponseCode ENDS_WITH_ERROR = ResponseCode.of(500, "Current value endsWith", "当前值 必须以指定后缀结尾");
    /**
     * contains 默认错误
     */
    public static final ResponseCode CONTAINS_ERROR = ResponseCode.of(500, "Current value contains", "当前值 必须包含指定内容");
    /**
     * notContains 默认错误
     */
    public static final ResponseCode NOT_CONTAINS_ERROR = ResponseCode.of(500, "Current value notContains", "当前值 不能包含指定内容");
    /**
     * isLowerCase 默认错误
     */
    public static final ResponseCode IS_LOWER_CASE_ERROR = ResponseCode.of(500, "Current value isLowerCase", "当前值 必须是小写");
    /**
     * isUpperCase 默认错误
     */
    public static final ResponseCode IS_UPPER_CASE_ERROR = ResponseCode.of(500, "Current value isUpperCase", "当前值 必须是大写");
    /**
     * equalsIgnoreCase 默认错误
     */
    public static final ResponseCode EQUALS_IGNORE_CASE_ERROR = ResponseCode.of(500, "Current value equalsIgnoreCase", "当前值 必须相等(忽略大小写)");
    /**
     * positive 默认错误
     */
    public static final ResponseCode POSITIVE_ERROR = ResponseCode.of(500, "Current value positive", "当前值 必须是正数");
    /**
     * positiveNumber 默认错误
     */
    public static final ResponseCode POSITIVE_NUMBER_ERROR = ResponseCode.of(500, "Current value positiveNumber", "当前值 必须是正数");
    /**
     * inRange 默认错误
     */
    public static final ResponseCode IN_RANGE_ERROR = ResponseCode.of(500, "Current value inRange", "当前值 必须在指定范围内");
    /**
     * inRangeNumber 默认错误
     */
    public static final ResponseCode IN_RANGE_NUMBER_ERROR = ResponseCode.of(500, "Current value inRangeNumber", "当前值 必须在指定范围内");
    /**
     * nonNegative 默认错误
     */
    public static final ResponseCode NON_NEGATIVE_ERROR = ResponseCode.of(500, "Current value nonNegative", "当前值 必须是非负数");
    /**
     * greaterThan 默认错误
     */
    public static final ResponseCode GREATER_THAN_ERROR = ResponseCode.of(500, "Current value greaterThan", "当前值 必须大于指定值");
    /**
     * greaterOrEqual 默认错误
     */
    public static final ResponseCode GREATER_OR_EQUAL_ERROR = ResponseCode.of(500, "Current value greaterOrEqual", "当前值 必须大于或等于指定值");
    /**
     * lessThan 默认错误
     */
    public static final ResponseCode LESS_THAN_ERROR = ResponseCode.of(500, "Current value lessThan", "当前值 必须小于指定值");
    /**
     * lessOrEqual 默认错误
     */
    public static final ResponseCode LESS_OR_EQUAL_ERROR = ResponseCode.of(500, "Current value lessOrEqual", "当前值 必须小于或等于指定值");
    /**
     * notZero 默认错误
     */
    public static final ResponseCode NOT_ZERO_ERROR = ResponseCode.of(500, "Current value notZero", "当前值 不能为零");
    /**
     * isZero 默认错误
     */
    public static final ResponseCode IS_ZERO_ERROR = ResponseCode.of(500, "Current value isZero", "当前值 必须为零");
    /**
     * negative 默认错误
     */
    public static final ResponseCode NEGATIVE_ERROR = ResponseCode.of(500, "Current value negative", "当前值 必须是负数");
    /**
     * multipleOf 默认错误
     */
    public static final ResponseCode MULTIPLE_OF_ERROR = ResponseCode.of(500, "Current value multipleOf", "当前值 必须是指定值的倍数");
    /**
     * decimalScale 默认错误
     */
    public static final ResponseCode DECIMAL_SCALE_ERROR = ResponseCode.of(500, "Current value decimalScale", "当前值 小数位不符合要求");
    /**
     * notNull 默认错误
     */
    public static final ResponseCode NOT_NULL_ERROR = ResponseCode.of(500, "Current value notNull", "当前值 不能为空");
    /**
     * exists 默认错误
     */
    public static final ResponseCode EXISTS_ERROR = ResponseCode.of(500, "Current value exists", "当前值 不能为空");
    /**
     * isNull 默认错误
     */
    public static final ResponseCode IS_NULL_ERROR = ResponseCode.of(500, "Current value isNull", "当前值 必须为空");
    /**
     * instanceOf 默认错误
     */
    public static final ResponseCode INSTANCE_OF_ERROR = ResponseCode.of(500, "Current value instanceOf", "当前值 必须是指定类型");
    /**
     * notInstanceOf 默认错误
     */
    public static final ResponseCode NOT_INSTANCE_OF_ERROR = ResponseCode.of(500, "Current value notInstanceOf", "当前值 不能是指定类型");
    /**
     * allNotNull 默认错误
     */
    public static final ResponseCode ALL_NOT_NULL_ERROR = ResponseCode.of(500, "Current value allNotNull", "当前值 所有元素都不能为空");
    /**
     * state 默认错误
     */
    public static final ResponseCode STATE_ERROR = ResponseCode.of(500, "Current value state", "当前值 状态必须符合要求");
    /**
     * isTrue 默认错误
     */
    public static final ResponseCode IS_TRUE_ERROR = ResponseCode.of(500, "Current value isTrue", "当前值 必须为真");
    /**
     * isFalse 默认错误
     */
    public static final ResponseCode IS_FALSE_ERROR = ResponseCode.of(500, "Current value isFalse", "当前值 必须为假");
    /**
     * hasSize 默认错误
     */
    public static final ResponseCode HAS_SIZE_ERROR = ResponseCode.of(500, "Current value hasSize", "当前值 大小必须符合要求");
    /**
     * containsKey 默认错误
     */
    public static final ResponseCode CONTAINS_KEY_ERROR = ResponseCode.of(500, "Current value containsKey", "当前值 必须包含指定键");
    /**
     * containsValue 默认错误
     */
    public static final ResponseCode CONTAINS_VALUE_ERROR = ResponseCode.of(500, "Current value containsValue", "当前值 必须包含指定值");
    /**
     * before 默认错误
     */
    public static final ResponseCode BEFORE_ERROR = ResponseCode.of(500, "Current value before", "当前值 必须在指定日期之前");
    /**
     * after 默认错误
     */
    public static final ResponseCode AFTER_ERROR = ResponseCode.of(500, "Current value after", "当前值 必须在指定日期之后");
    /**
     * present 默认错误
     */
    public static final ResponseCode PRESENT_ERROR = ResponseCode.of(500, "Current value present", "当前值 必须存在");
    /**
     * notPresent 默认错误
     */
    public static final ResponseCode NOT_PRESENT_ERROR = ResponseCode.of(500, "Current value notPresent", "当前值 必须不存在");
    /**
     * satisfies 默认错误
     */
    public static final ResponseCode SATISFIES_ERROR = ResponseCode.of(500, "Current value satisfies", "当前值 必须满足条件");
    /**
     * compare 默认错误
     */
    public static final ResponseCode COMPARE_ERROR = ResponseCode.of(500, "Current value compare", "当前值 比较结果必须符合要求");
    /**
     * defer 默认错误
     */
    public static final ResponseCode DEFER_ERROR = ResponseCode.of(500, "Current value defer", "当前值 延迟校验未通过");

    /**
     * isEmpty 默认错误
     */
    public static final ResponseCode IS_EMPTY_ERROR = ResponseCode.of(500, "Current value isEmpty", "当前值 必须为空");
    /**
     * sizeBetween 默认错误
     */
    public static final ResponseCode SIZE_BETWEEN_ERROR = ResponseCode.of(500, "Current value sizeBetween", "当前值 长度必须在指定范围内");
    /**
     * sizeEquals 默认错误
     */
    public static final ResponseCode SIZE_EQUALS_ERROR = ResponseCode.of(500, "Current value sizeEquals", "当前值 长度必须等于指定值");
    /**
     * hasNoNullElements 默认错误
     */
    public static final ResponseCode HAS_NO_NULL_ELEMENTS_ERROR = ResponseCode.of(500, "Current value hasNoNullElements", "当前值 不能包含空元素");
    /**
     * allMatch 默认错误
     */
    public static final ResponseCode ALL_MATCH_ERROR = ResponseCode.of(500, "Current value allMatch", "当前值 所有元素必须满足条件");
    /**
     * anyMatch 默认错误
     */
    public static final ResponseCode ANY_MATCH_ERROR = ResponseCode.of(500, "Current value anyMatch", "当前值 必须至少有一个元素满足条件");
    /**
     * notContainsKey 默认错误
     */
    public static final ResponseCode NOT_CONTAINS_KEY_ERROR = ResponseCode.of(500, "Current value notContainsKey", "当前值 不能包含指定键");
    /**
     * enumValue 默认错误
     */
    public static final ResponseCode ENUM_VALUE_ERROR = ResponseCode.of(500, "Current value enumValue", "当前值 必须是有效的枚举值");
    /**
     * enumConstant 默认错误
     */
    public static final ResponseCode ENUM_CONSTANT_ERROR = ResponseCode.of(500, "Current value enumConstant", "当前值 必须是指定的枚举常量");
    /**
     * same 默认错误
     */
    public static final ResponseCode SAME_ERROR = ResponseCode.of(500, "Current value same", "当前值 必须是同一个对象");
    /**
     * notSame 默认错误
     */
    public static final ResponseCode NOT_SAME_ERROR = ResponseCode.of(500, "Current value notSame", "当前值 不能是同一个对象");
    /**
     * equals 默认错误
     */
    public static final ResponseCode EQUALS_ERROR = ResponseCode.of(500, "Current value equals", "当前值 必须相等");
    /**
     * notEquals 默认错误
     */
    public static final ResponseCode NOT_EQUALS_ERROR = ResponseCode.of(500, "Current value notEquals", "当前值 不能相等");
    /**
     * isPresent 默认错误
     */
    public static final ResponseCode IS_PRESENT_ERROR = ResponseCode.of(500, "Current value isPresent", "当前值 必须存在");
    /**
     * afterOrEqual 默认错误
     */
    public static final ResponseCode AFTER_OR_EQUAL_ERROR = ResponseCode.of(500, "Current value afterOrEqual", "当前值 必须在指定日期之后或相等");
    /**
     * beforeOrEqual 默认错误
     */
    public static final ResponseCode BEFORE_OR_EQUAL_ERROR = ResponseCode.of(500, "Current value beforeOrEqual", "当前值 必须在指定日期之前或相等");
    /**
     * between 默认错误
     */
    public static final ResponseCode BETWEEN_ERROR = ResponseCode.of(500, "Current value between", "当前值 必须在指定日期之间");
    /**
     * isPast 默认错误
     */
    public static final ResponseCode IS_PAST_ERROR = ResponseCode.of(500, "Current value isPast", "当前值 必须是过去的时间");
    /**
     * isFuture 默认错误
     */
    public static final ResponseCode IS_FUTURE_ERROR = ResponseCode.of(500, "Current value isFuture", "当前值 必须是未来的时间");
    /**
     * isToday 默认错误
     */
    public static final ResponseCode IS_TODAY_ERROR = ResponseCode.of(500, "Current value isToday", "当前值 必须是今天");
}