package com.chao.failfast.internal.core.observability;

import java.lang.reflect.Method;

/**
 * OpenTelemetryBridge is a utility class for bridging OpenTelemetry tracing functionality
 * This class provides static methods to get current trace ID and span ID, interacting with OpenTelemetry API through reflection
 */
public final class OpenTelemetryBridge {

    // Volatile boolean variable to mark whether initialized, ensuring visibility in multi-threaded environment
    private static volatile boolean initialized = false;
    // Method object for OpenTelemetry Span class's current method
    private static Method spanCurrent;
    // Method object for OpenTelemetry Span class's getSpanContext method
    private static Method spanGetContext;
    // Method object for OpenTelemetry SpanContext class's isValid method
    private static Method spanContextIsValid;
    // Method object for OpenTelemetry SpanContext class's getTraceId method
    private static Method spanContextGetTraceId;
    // Method object for OpenTelemetry SpanContext class's getSpanId method
    private static Method spanContextGetSpanId;

    // Private constructor to prevent instantiation of this utility class
    private OpenTelemetryBridge() {
    }

    /**
     * Get current trace ID
     * @return Current trace ID, returns null if not exists
     */
    public static String currentTraceId() {
        TracePair pair = currentPair();
        return pair != null ? pair.traceId : null;
    }

    /**
     * Get current span ID
     * @return Current span ID, returns null if not exists
     */
    public static String currentSpanId() {
        TracePair pair = currentPair();
        return pair != null ? pair.spanId : null;
    }

    /**
     * Get current trace ID and span ID pair
     * @return TracePair object containing trace ID and span ID, returns null if not exists
     */
    private static TracePair currentPair() {
        try {
            init(); // Ensure initialized
            // spanCurrent is null means OpenTelemetry is not available (e.g., missing dependencies), return null directly
            if (spanCurrent == null) return null;
            // Get current span
            Object span = spanCurrent.invoke(null);
            if (span == null) return null;
            // Get span context
            Object spanCtx = spanGetContext.invoke(span);
            if (spanCtx == null) return null;
            // Check if span context is valid (isValid returns false means span has ended or is invalid)
            Object valid = spanContextIsValid.invoke(spanCtx);
            if (!(valid instanceof Boolean b) || !b) return null;
            // Get trace ID and span ID
            String traceId = (String) spanContextGetTraceId.invoke(spanCtx);
            String spanId = (String) spanContextGetSpanId.invoke(spanCtx);
            // If both are blank, return null
            if (isBlank(traceId) && isBlank(spanId)) return null;
            // Create and return TracePair object
            return new TracePair(blankToNull(traceId), blankToNull(spanId));
        } catch (Throwable ignored) {
            // Return null for any exception
            return null;
        }
    }

    /**
     * Initialization method for loading OpenTelemetry-related classes and methods
     * This method is synchronized to ensure thread safety
     */
    private static synchronized void init() {
        // Check if already initialized and necessary methods loaded successfully
        if (initialized && spanCurrent != null) return;
        try {
            // Load OpenTelemetry Span class
            Class<?> spanClass = Class.forName("io.opentelemetry.api.trace.Span");
            // Load OpenTelemetry SpanContext class
            Class<?> spanContextClass = Class.forName("io.opentelemetry.api.trace.SpanContext");
            // Get Span class's current method for getting current span
            spanCurrent = spanClass.getMethod("current");
            // Get Span class's getSpanContext method for getting span context
            spanGetContext = spanClass.getMethod("getSpanContext");
            // Get SpanContext class's isValid method for validating span context
            spanContextIsValid = spanContextClass.getMethod("isValid");
            // Get SpanContext class's getTraceId method for getting trace ID
            spanContextGetTraceId = spanContextClass.getMethod("getTraceId");
            // Get SpanContext class's getSpanId method for getting span ID
            spanContextGetSpanId = spanContextClass.getMethod("getSpanId");
            // Mark as initialized
            initialized = true;
        } catch (Throwable ignored) {
            // When OpenTelemetry is not available (dependencies not present or version incompatible), all reflection-acquired method references are null
            // Set all these references to null as a marker for "OpenTelemetry not available"
            // This way subsequent calls to currentTraceId()/currentSpanId() will return null quickly without trying reflection every time
            spanCurrent = null;
            spanGetContext = null;
            spanContextIsValid = null;
            spanContextGetTraceId = null;
            spanContextGetSpanId = null;
            // initialized is set to false to allow re-initialization in the future (e.g., OpenTelemetry added dynamically at runtime)
            // But current implementation doesn't retry after first initialization
            initialized = false;
        }
    }

    /**
     * Check if string is blank
     * @param value String to check
     * @return true if string is null or blank, false otherwise
     */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Convert blank string to null
     * @param value Input string
     * @return null if input string is blank, otherwise return original string
     */
    private static String blankToNull(String value) {
        return isBlank(value) ? null : value;
    }

    /**
     * Private record class for storing trace ID and span ID pair
     * @param traceId Trace ID
     * @param spanId Span ID
     */
    private record TracePair(String traceId, String spanId) {
    }
}
