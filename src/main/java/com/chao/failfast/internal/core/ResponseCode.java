package com.chao.failfast.internal.core;

/**
 * 响应码接口 - 支持配置化HTTP状态映射
 * 定义业务错误码的标准结构，支持动态消息格式化和HTTP状态映射
 */
public interface ResponseCode {

    // ==================== 框架内置错误码 ====================

    /**
     * 通用校验错误（500）
     */
    ResponseCode VALIDATION_ERROR = of(500, "{response.code.validation.error}");

    /**
     * 参数校验失败（400）
     */
    ResponseCode VALIDATION_ERROR_400 = of(400, "{response.code.validation.error}", "{response.code.validation.error}");

    /**
     * 校验对象不能为空（500）
     */
    ResponseCode VALIDATION_ERROR_NULL = of(500, "{response.code.validation.error}", "{response.code.validation.error.null}");


    /**
     * 重试被中断（500）
     */
    ResponseCode INTERRUPTED_ERROR = of(500, "{response.code.interrupted.error}", "{response.code.interrupted.error}");

    /**
     * 非法参数（500）
     */
    ResponseCode ILLEGAL_ARGUMENT = of(500, "{response.code.illegal.argument}", "{response.code.illegal.argument}");

    /**
     * 默认校验失败（500）
     */
    ResponseCode VALIDATION_ERROR_500 = of(500, "{response.code.validation.failed}", "{response.code.validation.failed}");
    ResponseCode VALIDATION_ERROR_500_DYNAMIC = of(500, "{response.code.validation.failed}", "{response.code.validation.failed.dynamic}");

    /**
     * 获取错误码数值
     *
     * @return 错误码整数值
     */
    int getCode();

    /**
     * 获取错误消息模板
     *
     * @return 错误消息字符串
     */
    String getMessage();

    /**
     * 获取详细错误描述
     *
     * @return 详细描述信息
     */
    String getDescription();

    /**
     * 支持动态消息模板
     * 使用String.format对消息模板进行参数化
     *
     * @param args 格式化参数
     * @return 格式化后的消息
     */
    default String formatMessage(Object... args) {
        if (getMessage() == null) {
            return null; // 或返回默认值
        }
        return String.format(getMessage(), args);
    }

    /**
     * 创建简单的响应码（只有错误码）
     *
     * @param code 错误码
     * @return ResponseCode实例
     */
    static ResponseCode of(int code) {
        return new Simple(code, null, null);
    }

    /**
     * 创建响应码（错误码+消息）
     *
     * @param code    错误码
     * @param message 错误消息
     * @return ResponseCode实例
     */
    static ResponseCode of(int code, String message) {
        return new Simple(code, message, null);
    }

    /**
     * 创建完整的响应码（错误码+消息+描述）
     *
     * @param code        错误码
     * @param message     错误消息
     * @param description 详细描述
     * @return ResponseCode实例
     */
    static ResponseCode of(int code, String message, String description) {
        return new Simple(code, message, description);
    }

    /**
     * 简单响应码实现类
     * 使用record实现不可变的数据载体
     */
    record Simple(int code, String message, String description) implements ResponseCode {
        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }
}
