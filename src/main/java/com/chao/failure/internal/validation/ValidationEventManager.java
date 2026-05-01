package com.chao.failure.internal.validation;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Validation observer manager class
 * Used to manage and notify various events during validation process
 * Uses singleton pattern to ensure only one observer instance globally
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
@Slf4j
public class ValidationEventManager {
    private ValidationEventManager() {
        /* This utility class should not be instantiated */
    }

    private static final AtomicReference<ValidationEventListener> OBSERVER = new AtomicReference<>(ValidationEventListener.NO_OP);

    public static ValidationEventListener getObserver() {
        ValidationEventListener current = OBSERVER.get();
        return current != null ? current : ValidationEventListener.NO_OP;
    }

    /**
     * Set validation observer
     * @param o New validation observer, cannot be null
     */
    public static void setObserver(ValidationEventListener o) {
        if (o != null) {
            OBSERVER.set(o);
        }
    }

    /**
     * Notify validation start
     * @param source Validation source
     * @param scene Validation scene
     */
    public static void notifyStart(String source, String scene) {
        ValidationEventListener current = getObserver();
        try {
            current.onValidationStart(source, scene);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Observer onValidationStart failed: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Notify validation end
     * @param source Validation source
     * @param durationNanos Validation duration (nanoseconds)
     * @param success Whether validation was successful
     */
    public static void notifyEnd(String source, long durationNanos, boolean success) {
        ValidationEventListener current = getObserver();
        try {
            current.onValidationEnd(source, durationNanos, success);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Observer onValidationEnd failed: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Notify validation failure
     * @param source Validation source
     * @param errorCode Error code
     */
    public static void notifyFailure(String source, String errorCode) {
        ValidationEventListener current = getObserver();
        try {
            current.onValidationFailure(source, errorCode);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Observer onValidationFailure failed: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Notify validation violation
     * @param source Validation source
     * @param constraint Violated constraint
     */
    public static void notifyViolation(String source, String constraint) {
        ValidationEventListener current = getObserver();
        try {
            current.onViolation(source, constraint);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Observer onViolation failed: {}", e.getMessage(), e);
            }
        }
    }
}
