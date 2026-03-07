package com.chao.failfast.internal;

import com.chao.failfast.config.CodeMappingConfig;
import com.chao.failfast.internal.core.FailureProperties;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * FailFast 上下文 - 线程安全的配置管理
 * 提供全局配置访问和线程级配置覆盖功能
 * 支持在运行时动态调整框架行为而不影响其他线程
 */
@Component
public class FailureContext {

    /**
     * 全局配置属性
     */
    private final FailureProperties properties;
    /**
     * 全局代码映射配置
     */
    @Getter
    private final CodeMappingConfig codeMappingConfig;

    /**
     * 线程级方法启用覆盖
     * 用于在特定线程中临时修改方法启用状态
     */
    private final ThreadLocal<Boolean> methodEnabledOverride = ThreadLocal.withInitial(() -> null);

    /**
     * 线程级方法打印覆盖
     * 用于在特定线程中临时控制是否打印方法信息
     */
    private final ThreadLocal<Boolean> printMethodOverride = ThreadLocal.withInitial(() -> null);

    /**
     * 构造函数
     *
     * @param properties FailFast配置属性
     */
    public FailureContext(FailureProperties properties, CodeMappingConfig codeMappingConfig) {
        this.properties = properties;
        this.codeMappingConfig = codeMappingConfig;
    }

    /**
     * 检查是否打印方法名信息
     * 支持线程级配置覆盖，优先使用线程局部变量的设置
     *
     * @return true表示打印方法信息，false表示不打印
     */
    boolean isShadowTrace() {
        // 检查线程级覆盖设置
        Boolean override = printMethodOverride.get();
        if (override != null) {
            return override;
        }
        // 使用全局配置
        return properties.isShadowTrace();
    }

    /**
     * 检查是否启用调试快照
     *
     * @return true表示启用快照
     */
    boolean isDebugSnapshot() {
        return properties.isDebugSnapshot();
    }

    /**
     * 清理当前线程的上下文变量
     * 防止ThreadLocal内存泄漏，应在请求处理完成后调用
     */
    public void clearThreadContext() {
        printMethodOverride.remove();
        methodEnabledOverride.remove();
    }

    /**
     * 在指定方法打印配置下执行代码块
     * 自动恢复原有的配置状态，确保线程安全
     *
     * @param printMethod 是否打印方法信息
     * @param action      要执行的代码块
     * @param <T>         返回值类型
     * @return 代码块的执行结果
     */
    public <T> T withPrintMethod(boolean printMethod, Supplier<T> action) {
        Boolean original = printMethodOverride.get();
        try {
            // 设置临时配置
            printMethodOverride.set(printMethod);
            return action.get();
        } finally {
            // 恢复原有配置
            if (original == null) printMethodOverride.remove();
            else printMethodOverride.set(original);
        }
    }

    /**
     * 在指定方法打印配置下执行无返回值的代码块
     * 自动恢复原有的配置状态，确保线程安全
     *
     * @param printMethod 是否打印方法信息
     * @param action      要执行的代码块
     */
    public void withPrintMethod(boolean printMethod, Runnable action) {
        Boolean original = printMethodOverride.get();
        try {
            // 设置临时配置
            printMethodOverride.set(printMethod);
            action.run();
        } finally {
            // 恢复原有配置
            if (original == null) printMethodOverride.remove();
            else printMethodOverride.set(original);
        }
    }
}
