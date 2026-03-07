package com.chao.failfast.internal.chain;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.core.ResponseCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Validation chain core - Manage state (failFast, alive, errors, context).
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.0.0
 */
public abstract class ChainCore<S extends ChainCore<S>> {
    @Getter
    protected final boolean failFast;
    @Getter
    protected boolean alive = true;
    // Dynamic skip state (true=execute, false=skip)
    private boolean conditionState = true;
    // OR state
    private boolean orMode = false;
    private boolean orHasSuccess = false;
    protected final ValidationContext context;
    protected final List<Business> errors = new ArrayList<>();

    protected ChainCore(boolean failFast, ValidationContext context) {
        this.failFast = failFast;
        this.context = context;
    }

    /**
     * Dynamically control whether to execute subsequent validation.
     *
     * @param condition If false, skip subsequent check/or operations until when(true) is called again
     * @return Current chain instance
     */
    public S when(boolean condition) {
        this.conditionState = condition;
        return self();
    }

    public S or() {
        if (!conditionState) return self();
        if (context != null && context.isStopped()) return self();
        this.orMode = true;
        this.orHasSuccess = isValid();   // If current valid, mark success

        // If failFast and already failed, or gives a second chance, reset alive
        if (failFast && !alive) {
            alive = true;
        }
        // Clear current errors, prepare to collect errors on the right side of or
        if (!orHasSuccess) {
            errors.clear();
        }
        return self();
    }

    public boolean shouldSkip() {
        // If currently dynamically skipped, or context stopped, then skip
        if (!conditionState) return true;
        if (context != null && context.isStopped()) return true;
        return (!alive && failFast);
    }

    @SuppressWarnings("unchecked")
    protected S self() {
        return (S) this;
    }

    /**
     * Lazy calculation validation - Support Supplier.
     *
     * @param conditionSupplier Condition supplier
     * @param code              Response code
     * @param detail            Detailed description
     * @return Current chain instance
     */
    public S check(Supplier<Boolean> conditionSupplier, ResponseCode code, String detail) {
        if (shouldSkip()) return self();
        return check(conditionSupplier.get(), code, detail);
    }

    /**
     * Unified validation entry - Support configuration.
     *
     * @param condition Validation condition
     * @param code      Response code
     * @param detail    Detailed description
     * @return Current chain instance
     */
    public S check(boolean condition, ResponseCode code, String detail) {
        return check(condition, code, detail, null);
    }

    /**
     * Unified validation entry - Support configuration and value snapshot.
     *
     * @param condition Validation condition
     * @param code      Response code
     * @param detail    Detailed description
     * @param value     Value snapshot
     * @return Current chain instance
     */
    public S check(boolean condition, ResponseCode code, String detail, Object value) {
        if (shouldSkip()) return self();

        if (orMode) {
            // OR mode: calculate combined result
            orMode = false;  // Consume or state
            boolean finalSuccess = orHasSuccess || condition;

            if (!finalSuccess) {
                // Both failed, report error
                addError(code, detail, value);
                if (failFast) alive = false;
            } else {
                // One success, whole or passed, clear errors
                alive = true;
                // errors already cleared in or()
            }
        } else {
            // Normal mode
            if (!condition) {
                addError(code, detail, value);
                if (failFast) alive = false;
            }
        }
        return self();
    }

    /**
     * No configuration validation - Use default error.
     *
     * @param condition Validation condition
     * @return Current chain instance
     */
    public S check(boolean condition) {
        return check(condition, null, null);
    }

    private void addError(ResponseCode code, String detail, Object value) {
        Business business = buildBusiness(code, detail, value);

        if (context != null) {
            context.reportError(business);
            if (failFast) context.stop();
        } else {
            errors.add(business);
        }
    }

    private Business buildBusiness(ResponseCode code, String detail, Object value) {
        Business.Fabricator fabricator = Business.compose().invalidValue(value);
        if (code != null && detail != null) {
            return fabricator.responseCode(code).detail(detail).materialize();
        }
        return fabricator.responseCode(Objects.requireNonNullElse(code, ResponseCode.VALIDATION_ERROR_500_DYNAMIC)).materialize();
    }

    /**
     * Get core instance (for interface default methods).
     *
     * @return Core instance
     */
    public S core() {
        return self();
    }

    /**
     * Get list of business causes.
     *
     * @return List of Business objects containing all error info
     */
    public List<Business> getCauses() {
        return new ArrayList<>(errors);
    }

    /**
     * Check if current object is valid.
     *
     * @return True if error collection is empty and object is alive, false otherwise
     */
    public boolean isValid() {
        if (context != null) {
            return context.isValid() && alive;
        }
        return errors.isEmpty() && alive;
    }

    /**
     * Stop subsequent validation if current has errors (set conditionState to false).
     *
     * @return Current chain instance
     */
    public S stopOnFail() {
        if (!conditionState) return self();
        if (isValid()) return self();
        return when(false);
    }

    /**
     * Resume validation (set conditionState to true).
     *
     * @return Current chain instance
     */
    public S resume() {
        return when(true);
    }
}
