package com.chao.failfast.internal.validation;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validation observers dispatcher - Centralized event distribution for validation events.
 */
public class ValidationObservers {

    private static final Logger logger = LoggerFactory.getLogger(ValidationObservers.class);

    /**
     * Current observer (default to NO_OP).
     * -- GETTER --
     *  Get the current observer.
     */
    @Getter
    private static volatile ValidationObserver observer = ValidationObserver.NO_OP;

    /**
     * Set the validation observer.
     *
     * @param o Validation observer instance
     */
    public static void setObserver(ValidationObserver o) {
        if (o != null) {
            observer = o;
        }
    }

    /**
     * Notify observer of validation start.
     *
     * @param source validation source
     * @param scene validation scene
     */
    public static void notifyStart(String source, String scene) {
        try {
            observer.onValidationStart(source, scene);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Observer onValidationStart failed: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Notify observer of validation end.
     *
     * @param source validation source
     * @param durationNanos duration in nanoseconds
     * @param success whether validation was successful
     */
    public static void notifyEnd(String source, long durationNanos, boolean success) {
        try {
            observer.onValidationEnd(source, durationNanos, success);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Observer onValidationEnd failed: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Notify observer of validation failure.
     *
     * @param source validation source
     * @param errorCode error code
     */
    public static void notifyFailure(String source, String errorCode) {
        try {
            observer.onValidationFailure(source, errorCode);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Observer onValidationFailure failed: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Notify observer of violation.
     *
     * @param source validation source
     * @param constraint constraint name
     */
    public static void notifyViolation(String source, String constraint) {
        try {
            observer.onViolation(source, constraint);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Observer onViolation failed: {}", e.getMessage(), e);
            }
        }
    }
}
