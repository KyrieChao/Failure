package com.chao.failure.internal.validation;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Validation observer manager class
 * Used to manage and notify various events during validation process
 * Uses singleton pattern to ensure only one observer instance globally
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
@Slf4j
public class ValidationEventManager {

    /**
     * Validation observer instance
     * Uses volatile keyword to ensure visibility in multi-threaded environment
     * Initialized as NO_OP (no operation) observer
     */
    @Getter
    private static volatile ValidationEventListener observer = ValidationEventListener.NO_OP;

    /**
     * Set validation observer
     * @param o New validation observer, cannot be null
     */
    public static void setObserver(ValidationEventListener o) {
        if (o != null) {
            observer = o;
        }
    }

    /**
     * Notify validation start
     * @param source Validation source
     * @param scene Validation scene
     */
    public static void notifyStart(String source, String scene) {
        try {
            observer.onValidationStart(source, scene);
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
        try {
            observer.onValidationEnd(source, durationNanos, success);
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
        try {
            observer.onValidationFailure(source, errorCode);
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
        try {
            observer.onViolation(source, constraint);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Observer onViolation failed: {}", e.getMessage(), e);
            }
        }
    }
}
