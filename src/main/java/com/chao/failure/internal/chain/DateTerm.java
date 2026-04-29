package com.chao.failure.internal.chain;

import com.chao.failure.constant.FailureConst;
import com.chao.failure.internal.chain.pipeline.ChainCore;
import com.chao.failure.internal.check.DateChecks;
import com.chao.failure.internal.core.ResponseCode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Date;

/**
 * Date validation interface.
 *
 * @param <S> Subclass type of ChainCore
 * @author Kyrie Chao
 * @version 1.3.1
 */
public interface DateTerm<S extends ChainCore<S>> {

    /**
     * Get chain core.
     *
     * @return Chain core instance
     */
    S core();

    default S after(Date date1, Date date2) {
        return after(date1, date2, FailureConst.AFTER_ERROR, null);
    }

    default S after(Date date1, Date date2, ResponseCode code) {
        return after(date1, date2, code, null);
    }

    default S after(Date date1, Date date2, ResponseCode code, String detail) {
        return core().check(DateChecks.after(date1, date2), code, detail);
    }

    default S before(Date date1, Date date2) {
        return before(date1, date2, FailureConst.BEFORE_ERROR, null);
    }

    default S before(Date date1, Date date2, ResponseCode code) {
        return before(date1, date2, code, null);
    }

    default S before(Date date1, Date date2, ResponseCode code, String detail) {
        return core().check(DateChecks.before(date1, date2), code, detail);
    }

    default <T extends Comparable<T>> S after(T t1, T t2) {
        return after(t1, t2, FailureConst.AFTER_ERROR, null);
    }

    default <T extends Comparable<T>> S after(T t1, T t2, ResponseCode code) {
        return after(t1, t2, code, null);
    }

    default <T extends Comparable<T>> S after(T t1, T t2, ResponseCode code, String detail) {
        return core().check(DateChecks.after(t1, t2), code, detail);
    }

    default <T extends Comparable<T>> S afterOrEqual(T t1, T t2) {
        return afterOrEqual(t1, t2, FailureConst.AFTER_OR_EQUAL_ERROR, null);
    }

    default <T extends Comparable<T>> S afterOrEqual(T t1, T t2, ResponseCode code) {
        return afterOrEqual(t1, t2, code, null);
    }

    default <T extends Comparable<T>> S afterOrEqual(T t1, T t2, ResponseCode code, String detail) {
        return core().check(DateChecks.afterOrEqual(t1, t2), code, detail);
    }

    default <T extends Comparable<T>> S before(T t1, T t2) {
        return before(t1, t2, FailureConst.BEFORE_ERROR, null);
    }

    default <T extends Comparable<T>> S before(T t1, T t2, ResponseCode code) {
        return before(t1, t2, code, null);
    }

    default <T extends Comparable<T>> S before(T t1, T t2, ResponseCode code, String detail) {
        return core().check(DateChecks.before(t1, t2), code, detail);
    }

    default <T extends Comparable<T>> S beforeOrEqual(T t1, T t2) {
        return beforeOrEqual(t1, t2, FailureConst.BEFORE_OR_EQUAL_ERROR, null);
    }

    default <T extends Comparable<T>> S beforeOrEqual(T t1, T t2, ResponseCode code) {
        return beforeOrEqual(t1, t2, code, null);
    }

    default <T extends Comparable<T>> S beforeOrEqual(T t1, T t2, ResponseCode code, String detail) {
        return core().check(DateChecks.beforeOrEqual(t1, t2), code, detail);
    }

    default <T extends Comparable<T>> S between(T value, T start, T end) {
        return between(value, start, end, FailureConst.BETWEEN_ERROR, null);
    }

    default <T extends Comparable<T>> S between(T value, T start, T end, ResponseCode code) {
        return between(value, start, end, code, null);
    }

    default <T extends Comparable<T>> S between(T value, T start, T end, ResponseCode code, String detail) {
        return core().check(DateChecks.between(value, start, end), code, detail);
    }

    default S isPast(Date date) {
        return isPast(date, FailureConst.IS_PAST_ERROR, null);
    }

    default S isPast(Date date, ResponseCode code) {
        return isPast(date, code, null);
    }

    default S isPast(Date date, ResponseCode code, String detail) {
        return core().check(DateChecks.isPast(date), code, detail);
    }

    default S isFuture(Date date) {
        return isFuture(date, FailureConst.IS_FUTURE_ERROR, null);
    }

    default S isFuture(Date date, ResponseCode code) {
        return isFuture(date, code, null);
    }

    default S isFuture(Date date, ResponseCode code, String detail) {
        return core().check(DateChecks.isFuture(date), code, detail);
    }

    default S isPast(ChronoLocalDate date) {
        return isPast(date, FailureConst.IS_PAST_ERROR, null);
    }

    default S isPast(ChronoLocalDate date, ResponseCode code) {
        return isPast(date, code, null);
    }

    default S isPast(ChronoLocalDate date, ResponseCode code, String detail) {
        return core().check(DateChecks.isPast(date), code, detail);
    }

    default S isFuture(ChronoLocalDate date) {
        return isFuture(date, FailureConst.IS_FUTURE_ERROR, null);
    }

    default S isFuture(ChronoLocalDate date, ResponseCode code) {
        return isFuture(date, code, null);
    }

    default S isFuture(ChronoLocalDate date, ResponseCode code, String detail) {
        return core().check(DateChecks.isFuture(date), code, detail);
    }

    default S isPast(ChronoLocalDateTime<?> dateTime) {
        return isPast(dateTime, FailureConst.IS_PAST_ERROR, null);
    }

    default S isPast(ChronoLocalDateTime<?> dateTime, ResponseCode code) {
        return isPast(dateTime, code, null);
    }

    default S isPast(ChronoLocalDateTime<?> dateTime, ResponseCode code, String detail) {
        return core().check(DateChecks.isPast(dateTime), code, detail);
    }

    default S isFuture(ChronoLocalDateTime<?> dateTime) {
        return isFuture(dateTime, FailureConst.IS_FUTURE_ERROR, null);
    }

    default S isFuture(ChronoLocalDateTime<?> dateTime, ResponseCode code) {
        return isFuture(dateTime, code, null);
    }

    default S isFuture(ChronoLocalDateTime<?> dateTime, ResponseCode code, String detail) {
        return core().check(DateChecks.isFuture(dateTime), code, detail);
    }

    default S isPast(Instant instant) {
        return isPast(instant, FailureConst.IS_PAST_ERROR, null);
    }

    default S isPast(Instant instant, ResponseCode code) {
        return isPast(instant, code, null);
    }

    default S isPast(Instant instant, ResponseCode code, String detail) {
        return core().check(DateChecks.isPast(instant), code, detail);
    }

    default S isFuture(Instant instant) {
        return isFuture(instant, FailureConst.IS_FUTURE_ERROR, null);
    }

    default S isFuture(Instant instant, ResponseCode code) {
        return isFuture(instant, code, null);
    }

    default S isFuture(Instant instant, ResponseCode code, String detail) {
        return core().check(DateChecks.isFuture(instant), code, detail);
    }

    default S isPast(ChronoZonedDateTime<?> zonedDateTime) {
        return isPast(zonedDateTime, FailureConst.IS_PAST_ERROR, null);
    }

    default S isPast(ChronoZonedDateTime<?> zonedDateTime, ResponseCode code) {
        return isPast(zonedDateTime, code, null);
    }

    default S isPast(ChronoZonedDateTime<?> zonedDateTime, ResponseCode code, String detail) {
        return core().check(DateChecks.isPast(zonedDateTime), code, detail);
    }

    default S isFuture(ChronoZonedDateTime<?> zonedDateTime) {
        return isFuture(zonedDateTime, FailureConst.IS_FUTURE_ERROR, null);
    }

    default S isFuture(ChronoZonedDateTime<?> zonedDateTime, ResponseCode code) {
        return isFuture(zonedDateTime, code, null);
    }

    default S isFuture(ChronoZonedDateTime<?> zonedDateTime, ResponseCode code, String detail) {
        return core().check(DateChecks.isFuture(zonedDateTime), code, detail);
    }

    default S isToday(LocalDate date) {
        return isToday(date, FailureConst.IS_TODAY_ERROR, null);
    }

    default S isToday(LocalDate date, ResponseCode code) {
        return isToday(date, code, null);
    }

    default S isToday(LocalDate date, ResponseCode code, String detail) {
        return core().check(DateChecks.isToday(date), code, detail);
    }

}
