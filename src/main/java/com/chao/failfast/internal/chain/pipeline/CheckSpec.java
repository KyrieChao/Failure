package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.internal.core.ResponseCode;

/**
 * 检查规范记录类，用于表示检查结果的信息
 * 包含响应码、详细信息和无效值三个属性
 */
public record CheckSpec(ResponseCode code, String detail, Object invalidValue) {
    /**
     * 创建一个不包含无效值的检查规范实例
     * @param code 响应码，表示检查结果的状态
     * @param detail 详细信息，描述检查结果的具体内容
     * @return 返回一个新的CheckSpec实例，invalidValue为null
     */
    public static CheckSpec of(ResponseCode code, String detail) {
        return new CheckSpec(code, detail, null);
    }

    /**
     * 创建一个包含无效值的检查规范实例
     * @param code 响应码，表示检查结果的状态
     * @param detail 详细信息，描述检查结果的具体内容
     * @param invalidValue 导致检查失败的无效值
     * @return 返回一个新的CheckSpec实例，包含指定的invalidValue
     */
    public static CheckSpec of(ResponseCode code, String detail, Object invalidValue) {
        return new CheckSpec(code, detail, invalidValue);
    }
}

