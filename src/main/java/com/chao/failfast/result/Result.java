package com.chao.failfast.result;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.util.I18n;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Functional result encapsulation - Avoid using exceptions as control flow.
 *
 * @param <T> Return value type on success
 * @author Kyrie Chao
 * @version 1.2.0
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed class Result<T> permits Result.Success, Result.Fail {

    protected int code;
    protected String message;
    protected String description;
    protected String timestamp;

    /**
     * Private constructor to prevent external instantiation.
     */
    private Result(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
        this.timestamp = ZonedDateTime.now(FailureConst.CST).format(FailureConst.DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * Create successful Result.
     *
     * @param value Success value
     * @param <T>   Value type
     * @return Success result
     */
    public static <T> Result<T> ok(T value) {
        return new Success<>(value);
    }

    /**
     * Create failed Result (using response code).
     *
     * @param code Response code
     * @param <T>  Value type
     * @return Failure result
     */
    public static <T> Result<T> fail(ResponseCode code) {
        return new Fail<>(Business.of(code));
    }

    /**
     * Create failed Result (using response code and detailed description).
     *
     * @param code   Response code
     * @param detail Detailed description
     * @param <T>    Value type
     * @return Failure result
     */
    public static <T> Result<T> fail(ResponseCode code, String detail) {
        return new Fail<>(Business.of(code, detail));
    }

    /**
     * Create failed Result (using Business exception).
     *
     * @param business Business exception
     * @param <T>      Value type
     * @return Failure result
     */
    public static <T> Result<T> fail(Business business) {
        return new Fail<>(business);
    }

    /**
     * Create Result based on whether value is null.
     *
     * @param value Value
     * @param code  Response code when failed
     * @param <T>   Value type
     * @return Result object
     */
    public static <T> Result<T> ofNullable(T value, ResponseCode code) {
        return value != null ? ok(value) : fail(code);
    }

    /**
     * Create Result based on whether value is null (with detailed description).
     *
     * @param value  Value
     * @param code   Response code when failed
     * @param detail Detailed description when failed
     * @param <T>    Value type
     * @return Result object
     */
    public static <T> Result<T> ofNullable(T value, ResponseCode code, String detail) {
        return value != null ? ok(value) : fail(code, detail);
    }

    /**
     * Check if it is success state.
     *
     * @return True if success, false if failed
     */
    @JsonIgnore
    public boolean isSuccess() {
        return this instanceof Success;
    }

    /**
     * Check if it is failure state.
     *
     * @return True if failed, false if success
     */
    @JsonIgnore
    public boolean isFail() {
        return this instanceof Result.Fail;
    }

    /**
     * Get success value.
     *
     * @return Success value
     * @throws IllegalStateException Thrown when Result is failure state
     */
    @JsonIgnore
    public T get() {
        if (this instanceof Success<T> s) return s.data;
        throw new IllegalStateException("Result is fail");
    }


    /**
     * Get data in success response.
     *
     * @return Data in Success instance, or null if Fail instance
     */
    @JsonIgnore
    public T getOrNull() {
        if (this instanceof Success<T> s) return s.data;
        return null;
    }

    /**
     * Get error info.
     *
     * @return Business exception
     * @throws IllegalStateException Thrown when Result is success state
     */
    @JsonIgnore
    public Business getError() {
        if (this instanceof Result.Fail<?> f) {
            return f.error;
        }
        throw new IllegalStateException("Result is success");
    }

    // ============ Functional Operations ============

    /**
     * Map success value to new type.
     *
     * @param mapper Mapping function
     * @param <R>    Target type
     * @return Mapped Result
     */
    public <R> Result<R> map(Function<T, R> mapper) {
        if (this instanceof Success<T> s) {
            try {
                return Result.ok(mapper.apply(s.data));
            } catch (Exception e) {
                if (e instanceof Business b) {
                    return Result.fail(b);
                }
                throw e;
            }
        }
        @SuppressWarnings("unchecked")
        Result<R> failResult = (Result<R>) this;
        return failResult;
    }

    /**
     * Flat map success value to new Result.
     *
     * @param mapper Flat mapping function
     * @param <R>    Target type
     * @return Mapped Result
     */
    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        if (this instanceof Success<T> s) {
            return mapper.apply(s.data);
        }
        @SuppressWarnings("unchecked")
        Result<R> failResult = (Result<R>) this;
        return failResult;
    }

    /**
     * Execute side effect on success value.
     *
     * @param action Side effect action
     * @return Original Result
     */
    public Result<T> peek(Consumer<T> action) {
        if (this instanceof Success<T> s) {
            action.accept(s.data);
        }
        return this;
    }

    /**
     * Execute side effect on error.
     *
     * @param action Side effect action
     * @return Original Result
     */
    public Result<T> peekError(Consumer<Business> action) {
        if (this instanceof Result.Fail<T> f) {
            action.accept(f.error);
        }
        return this;
    }

    /**
     * Filter success value.
     *
     * @param predicate Filter condition
     * @param code      Error code when condition not met
     * @return Filtered Result
     */
    public Result<T> filter(Function<T, Boolean> predicate, ResponseCode code) {
        if (this instanceof Success<T> s) {
            if (!predicate.apply(s.data)) {
                return Result.fail(code);
            }
        }
        return this;
    }

    public Result<T> filter(Function<T, Boolean> predicate, ResponseCode code, String detail) {
        if (this instanceof Success<T> s) {
            if (!predicate.apply(s.data)) {
                return Result.fail(code, detail);
            }
        }
        return this;
    }

    // ============ Recovery Operations ============

    /**
     * Recover from error to success value.
     *
     * @param recovery Recovery function
     * @return Recovered Result
     */
    public Result<T> recover(Function<Business, T> recovery) {
        if (this instanceof Result.Fail<T> f) {
            return Result.ok(recovery.apply(f.error));
        }
        return this;
    }

    /**
     * Recover from error to new Result.
     *
     * @param recovery Recovery function
     * @return Recovered Result
     */
    public Result<T> recoverWith(Function<Business, Result<T>> recovery) {
        if (this instanceof Result.Fail<T> f) {
            return recovery.apply(f.error);
        }
        return this;
    }

    // ============ Terminal Operations ============

    /**
     * Get value or default value provided by Supplier.
     *
     * @param supplier Default value provider
     * @return Success value or default value
     */
    public T onFailGet(Supplier<T> supplier) {
        return isSuccess() ? get() : supplier.get();
    }

    /**
     * Get value or throw Business exception.
     *
     * @return Success value
     * @throws Business Thrown when Result is failure state
     */
    public T failNow() {
        if (this instanceof Result.Fail<T> f) throw f.error;
        return get();
    }

    /**
     * Get value or default value.
     *
     * @param defaultValue Default value
     * @return Success value or default value
     */
    public T failNow(T defaultValue) {
        return isSuccess() ? get() : defaultValue;
    }

    /**
     * Get value or throw custom exception.
     *
     * @param exceptionProvider Exception provider
     * @param <X>               Exception type
     * @return Success value
     * @throws X Thrown when Result is failure state
     */
    public <X extends Throwable> T failNow(Function<Business, X> exceptionProvider) throws X {
        if (this instanceof Result.Fail<T> f) {
            throw exceptionProvider.apply(f.error);
        }
        return get();
    }


    /**
     * Combine two Results.
     *
     * @param other    Another Result
     * @param combiner Combiner function
     * @param <U>      Type of another Result
     * @param <R>      Combined type
     * @return Combined Result
     */
    public <U, R> Result<R> combine(Result<U> other, BiFunction<T, U, R> combiner) {
        if (this.isFail()) {
            @SuppressWarnings("unchecked")
            Result<R> failResult = (Result<R>) this;
            return failResult;
        }
        if (other.isFail()) {
            @SuppressWarnings("unchecked")
            Result<R> failResult = (Result<R>) other;
            return failResult;
        }
        return Result.ok(combiner.apply(this.get(), other.get()));
    }

// ============ Conversion Operations ============

    /**
     * Convert to Optional (value present on success, empty on failure).
     */
    public Optional<T> toOptional() {
        return isSuccess() ? Optional.ofNullable(get()) : Optional.empty();
    }

    /**
     * Convert to Stream (single element stream on success, empty stream on failure).
     */
    public Stream<T> stream() {
        return isSuccess() ? Stream.ofNullable(get()) : Stream.empty();
    }

    /**
     * Get value or default value (regardless of success/failure, return default on failure).
     */
    public T getOrElse(T defaultValue) {
        return isSuccess() ? get() : defaultValue;
    }

    /**
     * Get value or calculate from error (functional default value).
     */
    public T getOrElseGet(Function<Business, T> errorHandler) {
        return isSuccess() ? get() : errorHandler.apply(getError());
    }

    /**
     * Convert to another type, regardless of success/failure (similar to bimap).
     */
    public <R> Result<R> fold(Function<T, R> successFn, Function<Business, R> failureFn) {
        return isSuccess()
                ? Result.ok(successFn.apply(get()))
                : Result.ok(failureFn.apply(getError()));
    }

    /**
     * Swap success and failure (success becomes failure, failure becomes success).
     */
    public Result<T> swap(ResponseCode successAsError) {
        return isSuccess()
                ? Result.fail(successAsError, "Success result swapped to failure")
                : Result.ok(null);
    }

    /**
     * Check if contains specific value (success and value equals).
     */
    public boolean contains(T value) {
        return isSuccess() && java.util.Objects.equals(get(), value);
    }

    /**
     * Existence check (success and has value).
     */
    public boolean exists() {
        return isSuccess() && get() != null;
    }

    // ============ Inner Classes ============

    /**
     * Result implementation for success state.
     *
     * @param <T> Data type
     */
    @Getter
    public static final class Success<T> extends Result<T> {
        /**
         * Success data.
         */
        private final T data;

        /**
         * Constructor.
         *
         * @param data Success data
         */
        public Success(T data) {
            super(200, "Success", "操作成功");
            this.data = data;
        }
    }

    /**
     * Result implementation for failure state.
     *
     * @param <T> Data type
     */
    public static final class Fail<T> extends Result<T> {
        /**
         * Error info.
         */
        @JsonIgnore
        private final Business error;

        /**
         * Constructor.
         *
         * @param error Error info
         */
        public Fail(Business error) {
            super(error.getResponseCode().getCode(), I18n.get(error.getResponseCode().getMessage()), I18n.get(error.getDetail()));
            this.error = error;
        }
    }
}
