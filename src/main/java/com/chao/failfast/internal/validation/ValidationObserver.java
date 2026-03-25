package com.chao.failfast.internal.validation;

/**
 * Validation observer interface for observability.
 * Core module uses this to notify validation events without depending on Micrometer.
 */
public interface ValidationObserver {

    /**
     * Called when validation starts.
     *
     * @param source validation source (chain|jsr|method)
     * @param scene validation scene
     */
    void onValidationStart(String source, String scene);

    /**
     * Called when validation ends.
     *
     * @param source validation source (chain|jsr|method)
     * @param durationNanos duration in nanoseconds
     * @param success whether validation was successful
     */
    void onValidationEnd(String source, long durationNanos, boolean success);

    /**
     * Called when validation fails.
     *
     * @param source validation source (chain|jsr|method)
     * @param errorCode error code
     */
    void onValidationFailure(String source, String errorCode);

    /**
     * Called when a violation occurs.
     *
     * @param source validation source (chain|jsr|method)
     * @param constraint constraint name
     */
    void onViolation(String source, String constraint);

    /**
     * No-op implementation for default case.
     */
    ValidationObserver NO_OP = new ValidationObserver() {
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
