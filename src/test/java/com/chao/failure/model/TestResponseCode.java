package com.chao.failure.model;

import com.chao.failure.internal.core.ResponseCode;

/**
 * 测试用响应码
 */
public enum TestResponseCode implements ResponseCode {
    SUCCESS(200, "成功"),
    ERROR(50000, "通用错误"),
    ROOT_CAUSE(50001, "根本原因"),
    PARAM_ERROR(40001, "参数错误"),
    PARAM_REQUIRED(40002, "参数缺失"),
    PARAM_INVALID(40003, "参数无效"),
    UNAUTHORIZED(40101, "未授权"),
    SYSTEM_ERROR(50002, "系统错误"); // 新增

    private final int code;
    private final String message;

    TestResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

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
        return message;
    }
}
