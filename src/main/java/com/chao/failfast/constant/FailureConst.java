package com.chao.failfast.constant;

import com.chao.failfast.internal.core.ResponseCode;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

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

    // ==================== 正则表达式 ====================
    public static final Pattern Card = Pattern.compile("^\\d{15,19}$");
    public static final Pattern Email = Pattern.compile("^(.)(.+)(@.+)$");
    public static final Pattern Mobile = Pattern.compile("^1[3-9]\\d{9}$");
    public static final Pattern Email_Pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    public static final Pattern IP4 = Pattern.compile("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
    public static final Pattern UUID = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    public static final Pattern Range = Pattern.compile("^\\s*(\\d+)\\s*\\.\\.\\s*(\\d+)\\s*$|^\\s*(\\d+)\\s*-\\s*(\\d+)\\s*$");


    // ==================== 响应码 ====================
    /**
     * isJson 默认错误
     */
    public static final ResponseCode IS_JSON_ERROR = ResponseCode.of(500, "Current value isJson", "{response.code.is.json}");
    /**
     * isCreditCard 默认错误
     */
    public static final ResponseCode IS_CREDIT_CARD_ERROR = ResponseCode.of(500, "Current value isCreditCard", "{response.code.is.credit.card}");
    /**
     * isBase64 默认错误
     */
    public static final ResponseCode IS_BASE64_ERROR = ResponseCode.of(500, "Current value isBase64", "{response.code.is.base64}");


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
    public static final String INVALID_PARAMETER = "{failure.const.invalid.parameter}";

    /**
     * 未知错误
     */
    public static final String UNKNOWN_ERROR = "{failure.const.unknown.error}";

    /**
     * 未知
     */
    public static final String UNKNOWN = "{failure.const.unknown}";

    /**
     * 多重校验错误
     */
    public static final String MULTIPLE_VALIDATION_ERRORS = "{failure.const.multiple.validation.errors}";

    /**
     * 位置分隔符
     */
    public static final String AT = "{failure.const.at}";

    /**
     * 校验错误
     */
    public static final String VALIDATION_ERROR = "{failure.const.validation.error}";

    /**
     * 无法缩减空列表
     */
    public static final String CANNOT_REDUCE_EMPTY_LIST = "{failure.const.cannot.reduce.empty.list}";


    // ==================== 错误提示模板（中文） ====================

    /**
     * code 不能为空
     */
    public static final String CODE_REQUIRED = "{failure.const.code.required}";

    /**
     * message 或 description 至少一个不能为 null
     */
    public static final String MESSAGE_OR_DESCRIPTION_REQUIRED = "{failure.const.message.description.required}";

    /**
     * 不支持的校验类型前缀
     */
    public static final String UNSUPPORTED_VALIDATION_TYPE = "{failure.const.unsupported.validation.type}";

    /**
     * 校验失败前缀
     */
    public static final String VALIDATION_ERROR_PREFIX = "{failure.const.validation.error.prefix}";

    /**
     * 错误项后缀
     */
    public static final String ERROR_ITEM_SUFFIX = "{failure.const.error.item.suffix}";

    /**
     * 错误过多提示
     */
    public static final String TOO_MANY_ERRORS = "{failure.const.too.many.errors}";


    // ==================== 系统默认值 ====================

    /**
     * 系统默认错误码
     */
    public static final Integer SYSTEM_CODE = 500;

    /**
     * 默认错误消息
     */
    public static final String DEFAULT_MESSAGE = "{failure.const.default.message}";


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
    public static final ResponseCode NOT_BLANK_ERROR = ResponseCode.of(500, "Current value notBlank", "{response.code.not.blank}");
    /**
     * notEmpty 默认错误
     */
    public static final ResponseCode NOT_EMPTY_ERROR = ResponseCode.of(500, "Current value notEmpty", "{response.code.not.empty}");
    /**
     * blank 默认错误
     */
    public static final ResponseCode BLANK_ERROR = ResponseCode.of(500, "Current value blank", "{response.code.blank}");
    /**
     * lengthBetween 默认错误
     */
    public static final ResponseCode LENGTH_BETWEEN_ERROR = ResponseCode.of(500, "Current value lengthBetween", "{response.code.length.between}");
    /**
     * lengthMin 默认错误
     */
    public static final ResponseCode LENGTH_MIN_ERROR = ResponseCode.of(500, "Current value lengthMin", "{response.code.length.min}");
    /**
     * lengthMax 默认错误
     */
    public static final ResponseCode LENGTH_MAX_ERROR = ResponseCode.of(500, "Current value lengthMax", "{response.code.length.max}");
    /**
     * match 默认错误
     */
    public static final ResponseCode MATCH_ERROR = ResponseCode.of(500, "Current value match", "{response.code.match}");
    /**
     * email 默认错误
     */
    public static final ResponseCode EMAIL_ERROR = ResponseCode.of(500, "Current value email", "{response.code.email}");
    /**
     * mobile 默认错误
     */
    public static final ResponseCode MOBILE_ERROR = ResponseCode.of(500, "Current value mobile", "{response.code.mobile}");
    /**
     * url 默认错误
     */
    public static final ResponseCode URL_ERROR = ResponseCode.of(500, "Current value url", "{response.code.url}");
    /**
     * ipAddress 默认错误
     */
    public static final ResponseCode IP_ADDRESS_ERROR = ResponseCode.of(500, "Current value ipAddress", "{response.code.ip.address}");
    /**
     * uuid 默认错误
     */
    public static final ResponseCode UUID_ERROR = ResponseCode.of(500, "Current value uuid", "{response.code.uuid}");
    /**
     * isNumeric 默认错误
     */
    public static final ResponseCode IS_NUMERIC_ERROR = ResponseCode.of(500, "Current value isNumeric", "{response.code.is.numeric}");
    /**
     * isAlpha 默认错误
     */
    public static final ResponseCode IS_ALPHA_ERROR = ResponseCode.of(500, "Current value isAlpha", "{response.code.is.alpha}");
    /**
     * isAlphanumeric 默认错误
     */
    public static final ResponseCode IS_ALPHANUMERIC_ERROR = ResponseCode.of(500, "Current value isAlphanumeric", "{response.code.is.alphanumeric}");
    /**
     * startsWith 默认错误
     */
    public static final ResponseCode STARTS_WITH_ERROR = ResponseCode.of(500, "Current value startsWith", "{response.code.starts.with}");
    /**
     * endsWith 默认错误
     */
    public static final ResponseCode ENDS_WITH_ERROR = ResponseCode.of(500, "Current value endsWith", "{response.code.ends.with}");
    /**
     * contains 默认错误
     */
    public static final ResponseCode CONTAINS_ERROR = ResponseCode.of(500, "Current value contains", "{response.code.contains}");
    /**
     * notContains 默认错误
     */
    public static final ResponseCode NOT_CONTAINS_ERROR = ResponseCode.of(500, "Current value notContains", "{response.code.not.contains}");
    /**
     * isLowerCase 默认错误
     */
    public static final ResponseCode IS_LOWER_CASE_ERROR = ResponseCode.of(500, "Current value isLowerCase", "{response.code.is.lower.case}");
    /**
     * isUpperCase 默认错误
     */
    public static final ResponseCode IS_UPPER_CASE_ERROR = ResponseCode.of(500, "Current value isUpperCase", "{response.code.is.upper.case}");
    /**
     * equalsIgnoreCase 默认错误
     */
    public static final ResponseCode EQUALS_IGNORE_CASE_ERROR = ResponseCode.of(500, "Current value equalsIgnoreCase", "{response.code.equals.ignore.case}");
    /**
     * positive 默认错误
     */
    public static final ResponseCode POSITIVE_ERROR = ResponseCode.of(500, "Current value positive", "{response.code.positive}");

    /**
     * inRange 默认错误
     */
    public static final ResponseCode IN_RANGE_ERROR = ResponseCode.of(500, "Current value inRange", "{response.code.in.range}");
    /**
     * inRangeNumber 默认错误
     */
    public static final ResponseCode IN_RANGE_NUMBER_ERROR = ResponseCode.of(500, "Current value inRangeNumber", "{response.code.in.range.number}");
    /**
     * nonNegative 默认错误
     */
    public static final ResponseCode NON_NEGATIVE_ERROR = ResponseCode.of(500, "Current value nonNegative", "{response.code.non.negative}");
    /**
     * greaterThan 默认错误
     */
    public static final ResponseCode GREATER_THAN_ERROR = ResponseCode.of(500, "Current value greaterThan", "{response.code.greater.than}");
    /**
     * greaterOrEqual 默认错误
     */
    public static final ResponseCode GREATER_OR_EQUAL_ERROR = ResponseCode.of(500, "Current value greaterOrEqual", "{response.code.greater.or.equal}");
    /**
     * lessThan 默认错误
     */
    public static final ResponseCode LESS_THAN_ERROR = ResponseCode.of(500, "Current value lessThan", "{response.code.less.than}");
    /**
     * lessOrEqual 默认错误
     */
    public static final ResponseCode LESS_OR_EQUAL_ERROR = ResponseCode.of(500, "Current value lessOrEqual", "{response.code.less.or.equal}");
    /**
     * notZero 默认错误
     */
    public static final ResponseCode NOT_ZERO_ERROR = ResponseCode.of(500, "Current value notZero", "{response.code.not.zero}");
    /**
     * isZero 默认错误
     */
    public static final ResponseCode IS_ZERO_ERROR = ResponseCode.of(500, "Current value isZero", "{response.code.is.zero}");
    /**
     * negative 默认错误
     */
    public static final ResponseCode NEGATIVE_ERROR = ResponseCode.of(500, "Current value negative", "{response.code.negative}");
    /**
     * multipleOf 默认错误
     */
    public static final ResponseCode MULTIPLE_OF_ERROR = ResponseCode.of(500, "Current value multipleOf", "{response.code.multiple.of}");
    /**
     * decimalScale 默认错误
     */
    public static final ResponseCode DECIMAL_SCALE_ERROR = ResponseCode.of(500, "Current value decimalScale", "{response.code.decimal.scale}");
    /**
     * notNull 默认错误
     */
    public static final ResponseCode NOT_NULL_ERROR = ResponseCode.of(500, "Current value notNull", "{response.code.not.null}");
    /**
     * exists 默认错误
     */
    public static final ResponseCode EXISTS_ERROR = ResponseCode.of(500, "Current value exists", "{response.code.exists}");
    /**
     * isNull 默认错误
     */
    public static final ResponseCode IS_NULL_ERROR = ResponseCode.of(500, "Current value isNull", "{response.code.is.null}");
    /**
     * instanceOf 默认错误
     */
    public static final ResponseCode INSTANCE_OF_ERROR = ResponseCode.of(500, "Current value instanceOf", "{response.code.instance.of}");
    /**
     * notInstanceOf 默认错误
     */
    public static final ResponseCode NOT_INSTANCE_OF_ERROR = ResponseCode.of(500, "Current value notInstanceOf", "{response.code.not.instance.of}");
    /**
     * allNotNull 默认错误
     */
    public static final ResponseCode ALL_NOT_NULL_ERROR = ResponseCode.of(500, "Current value allNotNull", "{response.code.all.not.null}");
    /**
     * state 默认错误
     */
    public static final ResponseCode STATE_ERROR = ResponseCode.of(500, "Current value state", "{response.code.state}");
    /**
     * isTrue 默认错误
     */
    public static final ResponseCode IS_TRUE_ERROR = ResponseCode.of(500, "Current value isTrue", "{response.code.is.true}");
    /**
     * isFalse 默认错误
     */
    public static final ResponseCode IS_FALSE_ERROR = ResponseCode.of(500, "Current value isFalse", "{response.code.is.false}");
    /**
     * containsKey 默认错误
     */
    public static final ResponseCode CONTAINS_KEY_ERROR = ResponseCode.of(500, "Current value containsKey", "{response.code.contains.key}");
    /**
     * containsValue 默认错误
     */
    public static final ResponseCode CONTAINS_VALUE_ERROR = ResponseCode.of(500, "Current value containsValue", "{response.code.contains.value}");
    /**
     * before 默认错误
     */
    public static final ResponseCode BEFORE_ERROR = ResponseCode.of(500, "Current value before", "{response.code.before}");
    /**
     * after 默认错误
     */
    public static final ResponseCode AFTER_ERROR = ResponseCode.of(500, "Current value after", "{response.code.after}");

    /**
     * satisfies 默认错误
     */
    public static final ResponseCode SATISFIES_ERROR = ResponseCode.of(500, "Current value satisfies", "{response.code.satisfies}");
    /**
     * compare 默认错误
     */
    public static final ResponseCode COMPARE_ERROR = ResponseCode.of(500, "Current value compare", "{response.code.compare}");
    /**
     * defer 默认错误
     */
    public static final ResponseCode DEFER_ERROR = ResponseCode.of(500, "Current value defer", "{response.code.defer}");

    /**
     * isEmpty 默认错误
     */
    public static final ResponseCode IS_EMPTY_ERROR = ResponseCode.of(500, "Current value isEmpty", "{response.code.is.empty}");
    /**
     * sizeBetween 默认错误
     */
    public static final ResponseCode SIZE_BETWEEN_ERROR = ResponseCode.of(500, "Current value sizeBetween", "{response.code.size.between}");
    /**
     * sizeEquals 默认错误
     */
    public static final ResponseCode SIZE_EQUALS_ERROR = ResponseCode.of(500, "Current value sizeEquals", "{response.code.size.equals}");
    /**
     * hasNoNullElements 默认错误
     */
    public static final ResponseCode HAS_NO_NULL_ELEMENTS_ERROR = ResponseCode.of(500, "Current value hasNoNullElements", "{response.code.has.no.null.elements}");
    /**
     * allMatch 默认错误
     */
    public static final ResponseCode ALL_MATCH_ERROR = ResponseCode.of(500, "Current value allMatch", "{response.code.all.match}");
    /**
     * anyMatch 默认错误
     */
    public static final ResponseCode ANY_MATCH_ERROR = ResponseCode.of(500, "Current value anyMatch", "{response.code.any.match}");
    /**
     * notContainsKey 默认错误
     */
    public static final ResponseCode NOT_CONTAINS_KEY_ERROR = ResponseCode.of(500, "Current value notContainsKey", "{response.code.not.contains.key}");
    /**
     * enumValue 默认错误
     */
    public static final ResponseCode ENUM_VALUE_ERROR = ResponseCode.of(500, "Current value enumValue", "{response.code.enum.value}");
    /**
     * enumConstant 默认错误
     */
    public static final ResponseCode ENUM_CONSTANT_ERROR = ResponseCode.of(500, "Current value enumConstant", "{response.code.enum.constant}");
    /**
     * same 默认错误
     */
    public static final ResponseCode SAME_ERROR = ResponseCode.of(500, "Current value same", "{response.code.same}");
    /**
     * notSame 默认错误
     */
    public static final ResponseCode NOT_SAME_ERROR = ResponseCode.of(500, "Current value notSame", "{response.code.not.same}");
    /**
     * equals 默认错误
     */
    public static final ResponseCode EQUALS_ERROR = ResponseCode.of(500, "Current value equals", "{response.code.equals}");
    /**
     * notEquals 默认错误
     */
    public static final ResponseCode NOT_EQUALS_ERROR = ResponseCode.of(500, "Current value notEquals", "{response.code.not.equals}");
    /**
     * isPresent 默认错误
     */
    public static final ResponseCode IS_PRESENT_ERROR = ResponseCode.of(500, "Current value isPresent", "{response.code.is.present}");
    /**
     * afterOrEqual 默认错误
     */
    public static final ResponseCode AFTER_OR_EQUAL_ERROR = ResponseCode.of(500, "Current value afterOrEqual", "{response.code.after.or.equal}");
    /**
     * beforeOrEqual 默认错误
     */
    public static final ResponseCode BEFORE_OR_EQUAL_ERROR = ResponseCode.of(500, "Current value beforeOrEqual", "{response.code.before.or.equal}");
    /**
     * between 默认错误
     */
    public static final ResponseCode BETWEEN_ERROR = ResponseCode.of(500, "Current value between", "{response.code.between}");
    /**
     * isPast 默认错误
     */
    public static final ResponseCode IS_PAST_ERROR = ResponseCode.of(500, "Current value isPast", "{response.code.is.past}");
    /**
     * isFuture 默认错误
     */
    public static final ResponseCode IS_FUTURE_ERROR = ResponseCode.of(500, "Current value isFuture", "{response.code.is.future}");
    /**
     * isToday 默认错误
     */
    public static final ResponseCode IS_TODAY_ERROR = ResponseCode.of(500, "Current value isToday", "{response.code.is.today}");
}