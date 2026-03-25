package com.chao.failfast.annotation;

import com.chao.failfast.constant.Scenario;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.core.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validator interface - Support custom validation logic.
 *
 * @param <T> Target type
 * @author Kyrie Chao
 * @version 1.2.0
 */
@FunctionalInterface
public interface FastValidator<T> {

    /**
     * Validation method for validating target object.
     *
     * @param target  Target object to be validated
     * @param context Validation context containing rules and conditions
     */
    void validate(T target, ValidationContext context);

    /**
     * Get default type supported by current handler.
     *
     * @return Return Object.class as default supported type
     */
    default Class<?> getSupportedType() {
        return Object.class;
    }

    @RequiredArgsConstructor
    class ValidationContext {
        @Getter
        private final boolean fast;
        @Getter
        private final Scenario[] scenes;
        @Getter
        private final Class<?>[] groups;
        private final List<Business> errors = new ArrayList<>();
        @Getter
        private boolean stopped;

        public ValidationContext(boolean fast) {
            this(fast, new Scenario[]{Scenario.DEFAULT}, new Class<?>[0]);
        }

        public ValidationContext(boolean fast, Scenario scene, Class<?>[] groups) {
            this(fast, new Scenario[]{scene}, groups);
        }

        public void reportError(ResponseCode code) {
            reportError(Business.of(code));
        }

        public void reportError(ResponseCode code, String detail) {
            reportError(Business.of(code, detail));
        }

        public void reportError(Business error) {
            if (stopped) return;
            errors.add(error);
            if (fast) stopped = true;
        }

        public void stop() {
            this.stopped = true;
        }

        public boolean isFailed() {
            return !isValid() || stopped;
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public int errorSize() {
            return errors.size();
        }

        /**
         * Get list of business objects containing causes.
         *
         * @return List of business objects containing causes, type is List<Business>
         */
        public List<Business> hasCauses() {
            return Collections.unmodifiableList(errors);
        }

        /**
         * Get the first error message.
         *
         * @return Business type error object, or null if no error
         */
        public Business getFirstError() {
            return errors.isEmpty() ? null : errors.get(0);
        }

        /**
         * Reset the validation context to its initial state.
         */
        public void reset() {
            errors.clear();
            stopped = false;
        }
    }
}
