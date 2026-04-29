package com.chao.failure.internal.check;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Date;

/**
 * Utility class for date and time validation.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public final class DateChecks {


    private DateChecks() {}

    // Original Date methods

    /**
     * Checks if date1 is after date2.
     *
     * @param date1 the first date
     * @param date2 the second date
     * @return true if date1 is after date2, false otherwise
     */
    public static boolean after(Date date1, Date date2) {
        return date1 != null && date2 != null && date1.after(date2);
    }
    /**
     * Checks if date1 is before date2.
     *
     * @param date1 the first date
     * @param date2 the second date
     * @return true if date1 is before date2, false otherwise
     */
    public static boolean before(Date date1, Date date2) {
        return date1 != null && date2 != null && date1.before(date2);
    }

    // Generic Comparable methods for Time API (and others)
    /**
     * Checks if t1 is after t2.
     *
     * @param t1 the first comparable object
     * @param t2 the second comparable object
     * @return true if t1 is after t2, false otherwise
     */
    public static <T extends Comparable<T>> boolean after(T t1, T t2) {
        return t1 != null && t2 != null && t1.compareTo(t2) > 0;
    }

    /**
     * Checks if t1 is after or equal to t2.
     *
     * @param t1 the first comparable object
     * @param t2 the second comparable object
     * @return true if t1 is after or equal to t2, false otherwise
     */
    public static <T extends Comparable<T>> boolean afterOrEqual(T t1, T t2) {
        return t1 != null && t2 != null && t1.compareTo(t2) >= 0;
    }

    /**
     * Checks if t1 is before t2.
     *
     * @param t1 the first comparable object
     * @param t2 the second comparable object
     * @return true if t1 is before t2, false otherwise
     */
    public static <T extends Comparable<T>> boolean before(T t1, T t2) {
        return t1 != null && t2 != null && t1.compareTo(t2) < 0;
    }

    /**
     * Checks if t1 is before or equal to t2.
     *
     * @param t1 the first comparable object
     * @param t2 the second comparable object
     * @return true if t1 is before or equal to t2, false otherwise
     */
    public static <T extends Comparable<T>> boolean beforeOrEqual(T t1, T t2) {
        return t1 != null && t2 != null && t1.compareTo(t2) <= 0;
    }

    /**
     * Checks if value is between start and end (inclusive).
     *
     * @param value the value to check
     * @param start the start value
     * @param end   the end value
     * @return true if value is between start and end, false otherwise
     */
    public static <T extends Comparable<T>> boolean between(T value, T start, T end) {
        return value != null && start != null && end != null
                && value.compareTo(start) >= 0 && value.compareTo(end) <= 0;
    }

    // isPast / isFuture implementations
    /**
     * Checks if the date is in the past.
     *
     * @param date the date to check
     * @return true if the date is in the past, false otherwise
     */
    public static boolean isPast(Date date) {
        return date != null && date.before(new Date());
    }

    /**
     * Checks if the date is in the future.
     *
     * @param date the date to check
     * @return true if the date is in the future, false otherwise
     */
    public static boolean isFuture(Date date) {
        return date != null && date.after(new Date());
    }

    /**
     * Checks if the date is in the past.
     *
     * @param date the date to check
     * @return true if the date is in the past, false otherwise
     */
    public static boolean isPast(ChronoLocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Checks if the date is in the future.
     *
     * @param date the date to check
     * @return true if the date is in the future, false otherwise
     */
    public static boolean isFuture(ChronoLocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Checks if the date-time is in the past.
     *
     * @param dateTime the date-time to check
     * @return true if the date-time is in the past, false otherwise
     */
    public static boolean isPast(ChronoLocalDateTime<?> dateTime) {
        return dateTime != null && dateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Checks if the date-time is in the future.
     *
     * @param dateTime the date-time to check
     * @return true if the date-time is in the future, false otherwise
     */
    public static boolean isFuture(ChronoLocalDateTime<?> dateTime) {
        return dateTime != null && dateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Checks if the instant is in the past.
     *
     * @param instant the instant to check
     * @return true if the instant is in the past, false otherwise
     */
    public static boolean isPast(Instant instant) {
        return instant != null && instant.isBefore(Instant.now());
    }

    /**
     * Checks if the instant is in the future.
     *
     * @param instant the instant to check
     * @return true if the instant is in the future, false otherwise
     */
    public static boolean isFuture(Instant instant) {
        return instant != null && instant.isAfter(Instant.now());
    }

    /**
     * Checks if the zoned date-time is in the past.
     *
     * @param zonedDateTime the zoned date-time to check
     * @return true if the zoned date-time is in the past, false otherwise
     */
    public static boolean isPast(ChronoZonedDateTime<?> zonedDateTime) {
        return zonedDateTime != null && zonedDateTime.isBefore(ZonedDateTime.now());
    }

    /**
     * Checks if the zoned date-time is in the future.
     *
     * @param zonedDateTime the zoned date-time to check
     * @return true if the zoned date-time is in the future, false otherwise
     */
    public static boolean isFuture(ChronoZonedDateTime<?> zonedDateTime) {
        return zonedDateTime != null && zonedDateTime.isAfter(ZonedDateTime.now());
    }

    /**
     * Checks if the date is today.
     *
     * @param date the date to check
     * @return true if the date is today, false otherwise
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.isEqual(LocalDate.now());
    }
}
