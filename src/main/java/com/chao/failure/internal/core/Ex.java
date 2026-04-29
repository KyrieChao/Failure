package com.chao.failure.internal.core;

import com.chao.failure.constant.FailureConst;
import com.chao.failure.internal.core.observability.TraceInfoExtractor;
import com.chao.failure.spi.filter.SkipPrefixRegistry;
import com.chao.failure.spi.filter.SkipTypeRegistry;
import lombok.Getter;

/**
 * Exception builder utility class - Enhanced thread-safe version.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public final class Ex {
    /**
     * StackWalker instance for traversing call stack.
     */
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    
    @Getter
    private static SkipPrefixRegistry skipPrefixRegistry;
    @Getter
    private static SkipTypeRegistry skipTypeRegistry;
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

    public static void setSkipPrefixRegistry(SkipPrefixRegistry registry) {
        Ex.skipPrefixRegistry = registry;
    }

    public static void setSkipTypeRegistry(SkipTypeRegistry registry) {
        Ex.skipTypeRegistry = registry;
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
    public static String location() {
        return isShadowTrace() ? captureLocation() : null;
    }

    /**
     * Convenient method to get current call method info.
     *
     * @return Formatted method name string, or null if not enabled
     */
    public static String method() {
        return isShadowTrace() ? captureMethodName() : null;
    }

    /**
     * Internal helper to check if method info should be printed.
     *
     * @return True if context exists and method printing is enabled, false otherwise
     */
    private static boolean isShadowTrace() {
        if (context == null) return false;
        return TraceInfoExtractor.shadowTrace(context, null);
    }

    /**
     * Capture and format current call location info.
     *
     * @return Formatted location string, or null if failed
     */
    public static String captureLocation() {
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
    public static String captureMethodName() {
        if (!isShadowTrace()) return null;

        return WALKER.walk(stream -> stream
                .filter(Ex::isNotSkipped)
                .filter(f -> !f.getClassName().startsWith("com.chao.failfast.validator")
                        && !f.getClassName().endsWith("Validator")
                        && !f.getClassName().endsWith("Validators")
                        && !f.getClassName().equals(Ex.class.getName()))
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
        if (skipPrefixRegistry == null) {
            return true;
        }
        String cls = f.getClassName();
        return !skipPrefixRegistry.shouldSkip(cls);
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
