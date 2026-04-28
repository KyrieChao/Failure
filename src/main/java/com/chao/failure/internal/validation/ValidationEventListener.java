package com.chao.failure.internal.validation;


/**
 * Validation observer interface for listening and responding to various events during validation process
 * This interface defines callback methods for validation start, validation end, validation failure, and constraint violation
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public interface ValidationEventListener {

    /**
     * Callback method when validation starts
     * @param source Source that triggered the validation
     * @param scene Validation scene
     */
    void onValidationStart(String source, String scene);

    /**
     * Callback method when validation ends
     * @param source Source that triggered the validation
     * @param durationNanos Validation duration (nanoseconds)
     * @param success Whether validation was successful
     */
    void onValidationEnd(String source, long durationNanos, boolean success);

    /**
     * Callback method when validation fails
     * @param source Source that triggered the validation
     * @param errorCode Error code
     */
    void onValidationFailure(String source, String errorCode);

    /**
     * Callback method when constraint is violated
     * @param source Source that triggered the validation
     * @param constraint Violated constraint
     */
    void onViolation(String source, String constraint);


    /**
     * No-operation observer instance, all methods have empty implementations
     * Used for default scenarios that don't need to handle validation events
     */
    ValidationEventListener NO_OP = new ValidationEventListener() {
        @Override
        public void onValidationStart(String source, String scene) {
        }

        @Override
        public void onValidationEnd(String source, long durationNanos, boolean success) {
        }

        @Override
        public void onValidationFailure(String source, String errorCode) {
        }

        @Override
        public void onViolation(String source, String constraint) {
        }
    };
}
