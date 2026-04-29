package com.chao.failure.constant;

import com.chao.failure.internal.core.ResponseCode;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Global constant definition for Failure framework.
 *
 * <p>Contains shared constants for validation, configuration, response, etc.</p>
 *
 * @author Kyrie Chao
 * @version 1.3.1
 * @since 1.3.1
 */
public final class FailureConst {

    /**
     * Private constructor to prevent instantiation.
     */
    private FailureConst() {
    }

    // ==================== Regular Expressions ====================
    public static final Pattern Card = Pattern.compile("^\\d{15,19}$");
    public static final Pattern Email = Pattern.compile("^(.)(.+)(@.+)$");
    public static final Pattern Mobile = Pattern.compile("^1[3-9]\\d{9}$");
    public static final Pattern Email_Pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    public static final Pattern IP4 = Pattern.compile("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
    public static final Pattern UUID = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    public static final Pattern Range = Pattern.compile("^\\s*(\\d+)\\s*\\.\\.\\s*(\\d+)\\s*$|^\\s*(\\d+)\\s*-\\s*(\\d+)\\s*$");


    // ==================== Response Codes ====================
    /**
     * isJson default error.
     */
    public static final ResponseCode IS_JSON_ERROR = ResponseCode.of(500, "Current value isJson", "{response.code.is.json}");
    /**
     * isCreditCard default error.
     */
    public static final ResponseCode IS_CREDIT_CARD_ERROR = ResponseCode.of(500, "Current value isCreditCard", "{response.code.is.credit.card}");
    /**
     * isBase64 default error.
     */
    public static final ResponseCode IS_BASE64_ERROR = ResponseCode.of(500, "Current value isBase64", "{response.code.is.base64}");


    // ==================== Response Field Names (JSON Fields) ====================

    /**
     * Error code field.
     */
    public static final String FIELD_CODE = "code";

    /**
     * Error message field.
     */
    public static final String FIELD_MESSAGE = "message";

    /**
     * Error description field.
     */
    public static final String FIELD_DESCRIPTION = "description";

    /**
     * Error detail field.
     */
    public static final String FIELD_DETAIL = "detail";
    /**
     * Error traceId field.
     */
    public static final String FIELD_TRACE_ID = "traceId";
    /**
     * Error spanId field.
     */
    public static final String FIELD_SPAN_ID = "spanId";
    /**
     * Timestamp field.
     */
    public static final String FIELD_TIMESTAMP = "timestamp";

    /**
     * Error list field (verbose mode).
     */
    public static final String FIELD_ERRORS = "errors";


    public static final String FIELD_REJECTED = "rejected";
    public static final String FIELD_PATH = "path";
    public static final String FIELD_SCENE = "scene";
    public static final String FIELD_METHOD = "method";
    public static final String DEFAULT_SCENE = "DEFAULT";

    // ==================== Common Error Messages (English) ====================

    /**
     * Invalid parameter.
     */
    public static final String INVALID_PARAMETER = "{failure.const.invalid.parameter}";

    /**
     * Unknown error.
     */
    public static final String UNKNOWN_ERROR = "{failure.const.unknown.error}";

    /**
     * Unknown.
     */
    public static final String UNKNOWN = "{failure.const.unknown}";

    /**
     * Multiple validation errors.
     */
    public static final String MULTIPLE_VALIDATION_ERRORS = "{failure.const.multiple.validation.errors}";

    /**
     * Multiple validation errors count (e.g., "Total {0} items").
     */
    public static final String MULTIPLE_VALIDATION_ERRORS_COUNT = "{failure.const.multiple.validation.errors.count}";

    /**
     * Location separator.
     */
    public static final String AT = "{failure.const.at}";

    /**
     * Validation error.
     */
    public static final String VALIDATION_ERROR = "{failure.const.validation.error}";

    /**
     * Cannot reduce empty list.
     */
    public static final String CANNOT_REDUCE_EMPTY_LIST = "{failure.const.cannot.reduce.empty.list}";


    // ==================== Error Templates (Chinese) ====================

    /**
     * Code cannot be null.
     */
    public static final String CODE_REQUIRED = "{failure.const.code.required}";

    /**
     * Message or description must not be null.
     */
    public static final String MESSAGE_OR_DESCRIPTION_REQUIRED = "{failure.const.message.description.required}";

    /**
     * Unsupported validation type prefix.
     */
    public static final String UNSUPPORTED_VALIDATION_TYPE = "{failure.const.unsupported.validation.type}";

    /**
     * Validation error prefix.
     */
    public static final String VALIDATION_ERROR_PREFIX = "{failure.const.validation.error.prefix}";

    /**
     * Error item suffix.
     */
    public static final String ERROR_ITEM_SUFFIX = "{failure.const.error.item.suffix}";

    /**
     * Too many errors message.
     */
    public static final String TOO_MANY_ERRORS = "{failure.const.too.many.errors}";


    // ==================== System Defaults ====================

    /**
     * System default error code.
     */
    public static final Integer SYSTEM_CODE = 500;

    /**
     * Default error message.
     */
    public static final String DEFAULT_MESSAGE = "{failure.const.default.message}";


    // ==================== Time Related ====================

    /**
     * China Standard Time (Asia/Shanghai).
     */
    public static final ZoneId CST = ZoneId.of("Asia/Shanghai");

    /**
     * Default datetime pattern.
     */
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * Default datetime formatter.
     */
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);

    // ==================== Auto-generated Method Default Error Codes ====================

    /**
     * notBlank default error.
     */
    public static final ResponseCode NOT_BLANK_ERROR = ResponseCode.of(500, "Current value notBlank", "{response.code.not.blank}");
    /**
     * notEmpty default error.
     */
    public static final ResponseCode NOT_EMPTY_ERROR = ResponseCode.of(500, "Current value notEmpty", "{response.code.not.empty}");
    /**
     * blank default error.
     */
    public static final ResponseCode BLANK_ERROR = ResponseCode.of(500, "Current value blank", "{response.code.blank}");
    /**
     * lengthBetween default error.
     */
    public static final ResponseCode LENGTH_BETWEEN_ERROR = ResponseCode.of(500, "Current value lengthBetween", "{response.code.length.between}");
    /**
     * lengthMin default error.
     */
    public static final ResponseCode LENGTH_MIN_ERROR = ResponseCode.of(500, "Current value lengthMin", "{response.code.length.min}");
    /**
     * lengthMax default error.
     */
    public static final ResponseCode LENGTH_MAX_ERROR = ResponseCode.of(500, "Current value lengthMax", "{response.code.length.max}");
    /**
     * match default error.
     */
    public static final ResponseCode MATCH_ERROR = ResponseCode.of(500, "Current value match", "{response.code.match}");
    /**
     * email default error.
     */
    public static final ResponseCode EMAIL_ERROR = ResponseCode.of(500, "Current value email", "{response.code.email}");
    /**
     * mobile default error.
     */
    public static final ResponseCode MOBILE_ERROR = ResponseCode.of(500, "Current value mobile", "{response.code.mobile}");
    /**
     * url default error.
     */
    public static final ResponseCode URL_ERROR = ResponseCode.of(500, "Current value url", "{response.code.url}");
    /**
     * ipAddress default error.
     */
    public static final ResponseCode IP_ADDRESS_ERROR = ResponseCode.of(500, "Current value ipAddress", "{response.code.ip.address}");
    /**
     * uuid default error.
     */
    public static final ResponseCode UUID_ERROR = ResponseCode.of(500, "Current value uuid", "{response.code.uuid}");
    /**
     * isNumeric default error.
     */
    public static final ResponseCode IS_NUMERIC_ERROR = ResponseCode.of(500, "Current value isNumeric", "{response.code.is.numeric}");
    /**
     * isAlpha default error.
     */
    public static final ResponseCode IS_ALPHA_ERROR = ResponseCode.of(500, "Current value isAlpha", "{response.code.is.alpha}");
    /**
     * isAlphanumeric default error.
     */
    public static final ResponseCode IS_ALPHANUMERIC_ERROR = ResponseCode.of(500, "Current value isAlphanumeric", "{response.code.is.alphanumeric}");
    /**
     * startsWith default error.
     */
    public static final ResponseCode STARTS_WITH_ERROR = ResponseCode.of(500, "Current value startsWith", "{response.code.starts.with}");
    /**
     * endsWith default error.
     */
    public static final ResponseCode ENDS_WITH_ERROR = ResponseCode.of(500, "Current value endsWith", "{response.code.ends.with}");
    /**
     * contains default error.
     */
    public static final ResponseCode CONTAINS_ERROR = ResponseCode.of(500, "Current value contains", "{response.code.contains}");
    /**
     * notContains default error.
     */
    public static final ResponseCode NOT_CONTAINS_ERROR = ResponseCode.of(500, "Current value notContains", "{response.code.not.contains}");
    /**
     * containsAll default error.
     */
    public static final ResponseCode CONTAINS_ALL_ERROR = ResponseCode.of(500, "Current value containsAll", "{response.code.contains.all}");
    /**
     * uniqueElements default error.
     */
    public static final ResponseCode UNIQUE_ELEMENTS_ERROR = ResponseCode.of(500, "Current value uniqueElements", "{response.code.unique.elements}");
    /**
     * noneMatch default error.
     */
    public static final ResponseCode NONE_MATCH_ERROR = ResponseCode.of(500, "Current value noneMatch", "{response.code.none.match}");
    /**
     * isLowerCase default error.
     */
    public static final ResponseCode IS_LOWER_CASE_ERROR = ResponseCode.of(500, "Current value isLowerCase", "{response.code.is.lower.case}");
    /**
     * isUpperCase default error.
     */
    public static final ResponseCode IS_UPPER_CASE_ERROR = ResponseCode.of(500, "Current value isUpperCase", "{response.code.is.upper.case}");
    /**
     * equalsIgnoreCase default error.
     */
    public static final ResponseCode EQUALS_IGNORE_CASE_ERROR = ResponseCode.of(500, "Current value equalsIgnoreCase", "{response.code.equals.ignore.case}");
    /**
     * positive default error.
     */
    public static final ResponseCode POSITIVE_ERROR = ResponseCode.of(500, "Current value positive", "{response.code.positive}");

    /**
     * inRange default error.
     */
    public static final ResponseCode IN_RANGE_ERROR = ResponseCode.of(500, "Current value inRange", "{response.code.in.range}");
    /**
     * inRangeNumber default error.
     */
    public static final ResponseCode IN_RANGE_NUMBER_ERROR = ResponseCode.of(500, "Current value inRangeNumber", "{response.code.in.range.number}");
    /**
     * nonNegative default error.
     */
    public static final ResponseCode NON_NEGATIVE_ERROR = ResponseCode.of(500, "Current value nonNegative", "{response.code.non.negative}");
    /**
     * greaterThan default error.
     */
    public static final ResponseCode GREATER_THAN_ERROR = ResponseCode.of(500, "Current value greaterThan", "{response.code.greater.than}");
    /**
     * greaterOrEqual default error.
     */
    public static final ResponseCode GREATER_OR_EQUAL_ERROR = ResponseCode.of(500, "Current value greaterOrEqual", "{response.code.greater.or.equal}");
    /**
     * lessThan default error.
     */
    public static final ResponseCode LESS_THAN_ERROR = ResponseCode.of(500, "Current value lessThan", "{response.code.less.than}");
    /**
     * lessOrEqual default error.
     */
    public static final ResponseCode LESS_OR_EQUAL_ERROR = ResponseCode.of(500, "Current value lessOrEqual", "{response.code.less.or.equal}");
    /**
     * notZero default error.
     */
    public static final ResponseCode NOT_ZERO_ERROR = ResponseCode.of(500, "Current value notZero", "{response.code.not.zero}");
    /**
     * isZero default error.
     */
    public static final ResponseCode IS_ZERO_ERROR = ResponseCode.of(500, "Current value isZero", "{response.code.is.zero}");
    /**
     * negative default error.
     */
    public static final ResponseCode NEGATIVE_ERROR = ResponseCode.of(500, "Current value negative", "{response.code.negative}");
    /**
     * multipleOf default error.
     */
    public static final ResponseCode MULTIPLE_OF_ERROR = ResponseCode.of(500, "Current value multipleOf", "{response.code.multiple.of}");
    /**
     * decimalScale default error.
     */
    public static final ResponseCode DECIMAL_SCALE_ERROR = ResponseCode.of(500, "Current value decimalScale", "{response.code.decimal.scale}");
    /**
     * notNull default error.
     */
    public static final ResponseCode NOT_NULL_ERROR = ResponseCode.of(500, "Current value notNull", "{response.code.not.null}");
    /**
     * exists default error.
     */
    public static final ResponseCode EXISTS_ERROR = ResponseCode.of(500, "Current value exists", "{response.code.exists}");
    /**
     * isNull default error.
     */
    public static final ResponseCode IS_NULL_ERROR = ResponseCode.of(500, "Current value isNull", "{response.code.is.null}");
    /**
     * instanceOf default error.
     */
    public static final ResponseCode INSTANCE_OF_ERROR = ResponseCode.of(500, "Current value instanceOf", "{response.code.instance.of}");
    /**
     * notInstanceOf default error.
     */
    public static final ResponseCode NOT_INSTANCE_OF_ERROR = ResponseCode.of(500, "Current value notInstanceOf", "{response.code.not.instance.of}");
    /**
     * allNotNull default error.
     */
    public static final ResponseCode ALL_NOT_NULL_ERROR = ResponseCode.of(500, "Current value allNotNull", "{response.code.all.not.null}");
    /**
     * state default error.
     */
    public static final ResponseCode STATE_ERROR = ResponseCode.of(500, "Current value state", "{response.code.state}");
    /**
     * isTrue default error.
     */
    public static final ResponseCode IS_TRUE_ERROR = ResponseCode.of(500, "Current value isTrue", "{response.code.is.true}");
    /**
     * isFalse default error.
     */
    public static final ResponseCode IS_FALSE_ERROR = ResponseCode.of(500, "Current value isFalse", "{response.code.is.false}");
    /**
     * containsKey default error.
     */
    public static final ResponseCode CONTAINS_KEY_ERROR = ResponseCode.of(500, "Current value containsKey", "{response.code.contains.key}");
    /**
     * containsValue default error.
     */
    public static final ResponseCode CONTAINS_VALUE_ERROR = ResponseCode.of(500, "Current value containsValue", "{response.code.contains.value}");
    /**
     * before default error.
     */
    public static final ResponseCode BEFORE_ERROR = ResponseCode.of(500, "Current value before", "{response.code.before}");
    /**
     * after default error.
     */
    public static final ResponseCode AFTER_ERROR = ResponseCode.of(500, "Current value after", "{response.code.after}");

    /**
     * satisfies default error.
     */
    public static final ResponseCode SATISFIES_ERROR = ResponseCode.of(500, "Current value satisfies", "{response.code.satisfies}");
    /**
     * compare default error.
     */
    public static final ResponseCode COMPARE_ERROR = ResponseCode.of(500, "Current value compare", "{response.code.compare}");
    /**
     * defer default error.
     */
    public static final ResponseCode DEFER_ERROR = ResponseCode.of(500, "Current value defer", "{response.code.defer}");

    /**
     * isEmpty default error.
     */
    public static final ResponseCode IS_EMPTY_ERROR = ResponseCode.of(500, "Current value isEmpty", "{response.code.is.empty}");
    /**
     * sizeBetween default error.
     */
    public static final ResponseCode SIZE_BETWEEN_ERROR = ResponseCode.of(500, "Current value sizeBetween", "{response.code.size.between}");
    /**
     * sizeEquals default error.
     */
    public static final ResponseCode SIZE_EQUALS_ERROR = ResponseCode.of(500, "Current value sizeEquals", "{response.code.size.equals}");
    /**
     * hasNoNullElements default error.
     */
    public static final ResponseCode HAS_NO_NULL_ELEMENTS_ERROR = ResponseCode.of(500, "Current value hasNoNullElements", "{response.code.has.no.null.elements}");
    /**
     * allMatch default error.
     */
    public static final ResponseCode ALL_MATCH_ERROR = ResponseCode.of(500, "Current value allMatch", "{response.code.all.match}");
    /**
     * anyMatch default error.
     */
    public static final ResponseCode ANY_MATCH_ERROR = ResponseCode.of(500, "Current value anyMatch", "{response.code.any.match}");
    /**
     * notContainsKey default error.
     */
    public static final ResponseCode NOT_CONTAINS_KEY_ERROR = ResponseCode.of(500, "Current value notContainsKey", "{response.code.not.contains.key}");
    /**
     * enumValue default error.
     */
    public static final ResponseCode ENUM_VALUE_ERROR = ResponseCode.of(500, "Current value enumValue", "{response.code.enum.value}");
    /**
     * enumConstant default error.
     */
    public static final ResponseCode ENUM_CONSTANT_ERROR = ResponseCode.of(500, "Current value enumConstant", "{response.code.enum.constant}");
    /**
     * same default error.
     */
    public static final ResponseCode SAME_ERROR = ResponseCode.of(500, "Current value same", "{response.code.same}");
    /**
     * notSame default error.
     */
    public static final ResponseCode NOT_SAME_ERROR = ResponseCode.of(500, "Current value notSame", "{response.code.not.same}");
    /**
     * equals default error.
     */
    public static final ResponseCode EQUALS_ERROR = ResponseCode.of(500, "Current value equals", "{response.code.equals}");
    /**
     * notEquals default error.
     */
    public static final ResponseCode NOT_EQUALS_ERROR = ResponseCode.of(500, "Current value notEquals", "{response.code.not.equals}");
    /**
     * isPresent default error.
     */
    public static final ResponseCode IS_PRESENT_ERROR = ResponseCode.of(500, "Current value isPresent", "{response.code.is.present}");
    /**
     * afterOrEqual default error.
     */
    public static final ResponseCode AFTER_OR_EQUAL_ERROR = ResponseCode.of(500, "Current value afterOrEqual", "{response.code.after.or.equal}");
    /**
     * beforeOrEqual default error.
     */
    public static final ResponseCode BEFORE_OR_EQUAL_ERROR = ResponseCode.of(500, "Current value beforeOrEqual", "{response.code.before.or.equal}");
    /**
     * between default error.
     */
    public static final ResponseCode BETWEEN_ERROR = ResponseCode.of(500, "Current value between", "{response.code.between}");
    /**
     * isPast default error.
     */
    public static final ResponseCode IS_PAST_ERROR = ResponseCode.of(500, "Current value isPast", "{response.code.is.past}");
    /**
     * isFuture default error.
     */
    public static final ResponseCode IS_FUTURE_ERROR = ResponseCode.of(500, "Current value isFuture", "{response.code.is.future}");
    /**
     * isToday default error.
     */
    public static final ResponseCode IS_TODAY_ERROR = ResponseCode.of(500, "Current value isToday", "{response.code.is.today}");
}
