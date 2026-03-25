package com.chao.failfast.internal;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.core.FailureContext;
import lombok.Getter;

import java.util.Set;

/**
 * Exception builder utility class - Enhanced thread-safe version.
 *
 * @author Kyrie Chao
 * @version 1.2.0
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

    @Getter
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
        if (!isShadowTrace()) return null;

        return WALKER.walk(stream -> stream
                .filter(Ex::isNotSkipped)
                .findFirst()
                .map(Ex::formatLocation)
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
        return (line > 0) ? simple + ".java:" + line : simple + ".java";
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

        if (methodName.startsWith("lambda$")) {
            int firstDollar = methodName.indexOf('$');
            int lastDollar = methodName.lastIndexOf('$');
            if (lastDollar > firstDollar) {
                methodName = methodName.substring(firstDollar + 1, lastDollar);
            }
        }
        return simple + "#" + methodName;
    }
}
