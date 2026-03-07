package com.chao.failfast.internal;

import com.chao.failfast.constant.FailureConst;

import java.util.Set;

/**
 * Exception builder utility class - Enhanced thread-safe version.
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public final class Ex {
    /**
     * StackWalker instance for traversing call stack.
     */
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    /**
     * Set of package prefixes to skip.
     */
    private static final Set<String> SKIP_PREFIXES = Set.of(
            "com.chao.failfast.advice",     // 异常处理切面包
            "com.chao.failfast.annotation", // 内部工具包
            "com.chao.failfast.aspect",     // 切面包
            "com.chao.failfast.config",     // 配置类
            "com.chao.failfast.integration",// 集成包
            "com.chao.failfast.internal",   // 内部工具包
            "com.chao.failfast.result",     // 响应结果类
            "com.chao.failfast.Failure",    // 失败处理类
            "org.springframework",          // Spring框架
            "org.apache",                   // Apache相关组件
            "jakarta",                      // Jakarta EE规范
            "java.",                        // Java标准库
            "jdk.",                         // JDK内部类
            "sun."                          // Sun Microsystems遗留类
    );

    /**
     * FailFast context object.
     */
    private static FailureContext context;

    /**
     * Set FailFast context.
     *
     * @param ctx FailFast context object containing configuration
     */
    public static void setContext(FailureContext ctx) {
        Ex.context = ctx;
    }


    /**
     * Get current FailFast context.
     *
     * @return Current FailFast context object, may be null
     */
    static FailureContext getContext() {
        return context;
    }


    /**
     * Private constructor to prevent instantiation.
     */
    private Ex() {
    }

    /**
     * Convenient method to get current call location info.
     *
     * @return Formatted location string, or null if not enabled
     */
    static String location() {
        return isShadowTrace() ? captureLocation() : null;
    }

    /**
     * Convenient method to get current call method info.
     *
     * @return Formatted method name string, or null if not enabled
     */
    static String method() {
        return isShadowTrace() ? captureMethodName() : null;
    }

    /**
     * Internal helper to check if method info should be printed.
     *
     * @return True if context exists and method printing is enabled, false otherwise
     */
    private static boolean isShadowTrace() {
        return context != null && context.isShadowTrace();
    }

    /**
     * Capture and format current call location info.
     *
     * @return Formatted location string, or null if failed
     */
    static String captureLocation() {
        // 首先检查是否启用方法打印功能
        if (!isShadowTrace()) return null;

        // 使用StackWalker遍历调用栈
        return WALKER.walk(stream -> stream
                // 过滤掉不需要的调用帧（框架、系统类等）
                .filter(Ex::isNotSkipped)
                // 获取第一个有效的业务调用帧
                .findFirst()
                // 将栈帧格式化为位置字符串
                .map(Ex::formatLocation)
                // 如果找不到有效帧，返回默认值
                .orElse(FailureConst.UNKNOWN));
    }

    /**
     * Static method to get method name.
     *
     * @return Method name string, or null if condition not met
     */
    static String captureMethodName() {
        if (!isShadowTrace()) return null;

        return WALKER.walk(stream -> stream
                .filter(Ex::isNotSkipped)
                // 额外过滤验证器类，以便定位到调用验证器的业务方法 (e.g. Controller/Service)
                // 1. 过滤 com.chao.failfast.validator 包下的类
                // 2. 过滤类名以 Validator 或 Validators 结尾的类
                .filter(f -> !f.getClassName().startsWith("com.chao.failfast.validator")
                        && !f.getClassName().endsWith("Validator")
                        && !f.getClassName().endsWith("Validators"))
                .findFirst()
                .map(Ex::formatMethodName)
                .orElse("unknown"));
    }

    /**
     * Check if given stack frame should not be skipped.
     *
     * @param f Stack frame object to check
     * @return True if class name does not start with any skip prefixes, false otherwise
     */
    private static boolean isNotSkipped(StackWalker.StackFrame f) {
        String cls = f.getClassName();
        return SKIP_PREFIXES.stream().noneMatch(cls::startsWith);
    }

    /**
     * Format stack frame info into readable string.
     *
     * @param f StackWalker.StackFrame object
     * @return Formatted string
     */
    private static String formatLocation(StackWalker.StackFrame f) {
        String full = f.getClassName();
        String simple = full.substring(full.lastIndexOf('.') + 1);
        int line = f.getLineNumber();
        // 如果行号有效(>0)，显示文件名:行号；否则仅显示文件名（避免出现 -1 导致无法跳转）
        // CGLIB 代理类或 Native 方法通常没有行号信息
        String fileInfo = (line > 0) ? simple + ".java:" + line : simple + ".java";
        return simple + "." + f.getMethodName() + "(" + fileInfo + ")";
    }

    /**
     * Format method name.
     *
     * @param f StackWalker.StackFrame object
     * @return Formatted string
     */
    private static String formatMethodName(StackWalker.StackFrame f) {
        String cls = f.getClassName();
        String simple = cls.substring(cls.lastIndexOf('.') + 1);
        String methodName = f.getMethodName();

        // 处理 Lambda 表达式生成的方法名: lambda$resultsUtils$9 -> resultsUtils
        if (methodName.startsWith("lambda$")) {
            int firstDollar = methodName.indexOf('$');
            int lastDollar = methodName.lastIndexOf('$');
            if (firstDollar != -1 && lastDollar > firstDollar) {
                methodName = methodName.substring(firstDollar + 1, lastDollar);
            }
        }
        return simple + "#" + methodName;
    }
}
