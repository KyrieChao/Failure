package com.chao.failfast.internal.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fail-Fast 配置属性 - 增强版
 * 通过@ConfigurationProperties自动绑定application.yml中的配置
 * 支持框架启用控制、调试信息输出和错误码映射等核心功能
 */
@Data
@ConfigurationProperties(prefix = "fail-fast")
public class FailureProperties {
    /**
     * 是否打印方法名（调试用，生产环境建议关闭）
     * 启用后会在异常信息中包含方法名和位置信息，便于调试
     */
    private boolean shadowTrace;

    /**
     * 是否启用详细错误响应模式
     * 启用后会在批量错误响应中包含具体的errors列表字段
     * 默认为false，仅返回description汇总描述
     */
    private boolean verbose = false;

    /**
     * 是否启用调试快照（DX-2）
     * 启用后会在异常信息中记录导致失败的参数值（自动脱敏）
     * 默认为 false
     */
    private boolean debugSnapshot = false;

    /**
     * 错误码映射配置
     * 包含HTTP状态码映射、分组配置和国际化设置
     */
    private CodeMapping codeMapping = new CodeMapping();

    /**
     * 国际化配置
     * 包含默认语言、资源文件路径等配置
     */
    private I18n i18n = new I18n();

    /**
     * 国际化配置类
     */
    @Data
    public static class I18n {
        /**
         * 是否启用国际化支持
         * 默认为 true
         */
        private boolean enabled = true;

        /**
         * 默认语言环境
         * 默认为 zh_CN (中文)
         * 可选值: en_US, zh_CN 等标准 Locale 字符串
         */
        private String defaultLocale = "zh_CN";

        /**
         * 国际化资源文件基础路径
         * 默认为 classpath:i18n/messages
         * 多个路径用逗号分隔
         */
        private String basename = "classpath:i18n/messages";

        /**
         * 资源文件编码
         * 默认为 UTF-8
         */
        private String encoding = "UTF-8";

        /**
         * 资源文件缓存时间（秒）
         * 默认为 3600 秒
         * -1 表示永久缓存
         */
        private int cacheSeconds = 3600;
    }

    /**
     * 错误码映射配置类
     * 负责管理错误码与HTTP状态码的映射关系
     */
    @Data
    public static class CodeMapping {
        /**
         * HTTP状态码映射：错误码 -> HTTP状态码
         * 支持自定义错误码到标准HTTP状态码的映射
         */
        private Map<String, Integer> httpStatus = new HashMap<>();

        /**
         * 错误码分组
         * 用于将相关错误码组织成逻辑组，便于批量处理
         */
        private Map<String, List<Object>> groups = new HashMap<>();
    }
}
