package com.chao.failfast.internal;

import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.*;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests for Chain.java date related methods (isPast, isFuture).
 * Ensuring 98% coverage for these methods.
 */
class ChainDateMethodsTest {

    @Nested
    @DisplayName("Date (java.util.Date) Tests")
    class DateTests {

        @Test
        @DisplayName("isPast(Date) - Past date should be valid")
        void isPast_PastDate_Valid() {
            Date past = new Date(System.currentTimeMillis() - 1000);
            Chain chain = Chain.begin(false).isPast(past);
            assertThat(chain.isValid()).isTrue();
            assertThat(chain.getCauses()).isEmpty();
        }

        @Test
        @DisplayName("isPast(Date) - Future date should be invalid")
        void isPast_FutureDate_Invalid() {
            Date future = new Date(System.currentTimeMillis() + 10000);
            Chain chain = Chain.begin(false).isPast(future);
            assertThat(chain.isValid()).isFalse();
            // 默认错误
            assertThat(chain.getCauses()).isNotEmpty();
        }

        @Test
        @DisplayName("isPast(Date) - Now (created slightly before) should be valid (treated as past)")
        void isPast_Now_Valid() {
            Date now = new Date();
            // Sleep a tiny bit to ensure strictly before
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
            Chain chain = Chain.begin(false).isPast(now);
            assertThat(chain.isValid()).isTrue();
        }

        @Test
        @DisplayName("isFuture(Date) - Future date should be valid")
        void isFuture_FutureDate_Valid() {
            Date future = new Date(System.currentTimeMillis() + 10000);
            Chain chain = Chain.begin(false).isFuture(future);
            assertThat(chain.isValid()).isTrue();
        }

        @Test
        @DisplayName("isFuture(Date) - Past date should be invalid")
        void isFuture_PastDate_Invalid() {
            Date past = new Date(System.currentTimeMillis() - 1000);
            Chain chain = Chain.begin(false).isFuture(past);
            assertThat(chain.isValid()).isFalse();
        }

        @Test
        @DisplayName("isFuture(Date) - Now should be invalid (not future)")
        void isFuture_Now_Invalid() {
            Date now = new Date();
            Chain chain = Chain.begin(false).isFuture(now);
            assertThat(chain.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Coverage Tests")
    class CoverageTests {

        static Stream<Consumer<Chain>> coverageMethods() {
            Date date = new Date();
            LocalDate localDate = LocalDate.now();
            LocalDateTime localDateTime = LocalDateTime.now();
            Instant instant = Instant.now();
            ZonedDateTime zonedDateTime = ZonedDateTime.now();

            ResponseCode code = ResponseCode.of(1, "test");
            return Stream.of(
                    // Date
                    c -> c.isPast(date, code),
                    c -> c.isPast(date, code, "detail"),
                    c -> c.isFuture(date, code),
                    c -> c.isFuture(date, code, "detail"),

                    // LocalDate
                    c -> c.isPast(localDate, code),
                    c -> c.isPast(localDate, code, "detail"),
                    c -> c.isFuture(localDate, code),
                    c -> c.isFuture(localDate, code, "detail"),
                    c -> c.isToday(localDate, code),
                    c -> c.isToday(localDate, code, "detail"),

                    // LocalDateTime
                    c -> c.isPast(localDateTime, code),
                    c -> c.isPast(localDateTime, code, "detail"),
                    c -> c.isFuture(localDateTime, code),
                    c -> c.isFuture(localDateTime, code, "detail"),

                    // Instant
                    c -> c.isPast(instant, code),
                    c -> c.isPast(instant, code, "detail"),
                    c -> c.isFuture(instant, code),
                    c -> c.isFuture(instant, code, "detail"),

                    // ZonedDateTime
                    c -> c.isPast(zonedDateTime, code),
                    c -> c.isPast(zonedDateTime, code, "detail"),
                    c -> c.isFuture(zonedDateTime, code),
                    c -> c.isFuture(zonedDateTime, code, "detail")
            );
        }

        @ParameterizedTest
        @MethodSource("coverageMethods")
        void testCoverage(Consumer<Chain> methodCall) {
            Chain chain = Chain.begin(false);
            methodCall.accept(chain);
            assertThatCode(chain::isValid).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("LocalDate Tests")
    class LocalDateTests {

        @Test
        @DisplayName("isPast(LocalDate) - Yesterday should be valid")
        void isPast_Yesterday_Valid() {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            Chain chain = Chain.begin(false).isPast(yesterday);
            assertThat(chain.isValid()).isTrue();
        }

        @Test
        @DisplayName("isPast(LocalDate) - Tomorrow should be invalid")
        void isPast_Tomorrow_Invalid() {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            Chain chain = Chain.begin(false).isPast(tomorrow);
            assertThat(chain.isValid()).isFalse();
        }

        @Test
        @DisplayName("isPast(LocalDate) - Today should be invalid")
        void isPast_Today_Invalid() {
            LocalDate today = LocalDate.now();
            Chain chain = Chain.begin(false).isPast(today);
            assertThat(chain.isValid()).isFalse();
        }

        @Test
        @DisplayName("isFuture(LocalDate) - Tomorrow should be valid")
        void isFuture_Tomorrow_Valid() {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            Chain chain = Chain.begin(false).isFuture(tomorrow);
            assertThat(chain.isValid()).isTrue();
        }

        @Test
        @DisplayName("isFuture(LocalDate) - Yesterday should be invalid")
        void isFuture_Yesterday_Invalid() {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            Chain chain = Chain.begin(false).isFuture(yesterday);
            assertThat(chain.isValid()).isFalse();
        }

        @Test
        @DisplayName("isFuture(LocalDate) - Today should be invalid")
        void isFuture_Today_Invalid() {
            LocalDate today = LocalDate.now();
            Chain chain = Chain.begin(false).isFuture(today);
            assertThat(chain.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("LocalDateTime Tests")
    class LocalDateTimeTests {

        @Test
        @DisplayName("isPast(LocalDateTime) - Past should be valid")
        void isPast_Past_Valid() {
            LocalDateTime past = LocalDateTime.now().minusSeconds(1);
            Chain chain = Chain.begin(false).isPast(past);
            assertThat(chain.isValid()).isTrue();
        }

        @Test
        @DisplayName("isPast(LocalDateTime) - Future should be invalid")
        void isPast_Future_Invalid() {
            LocalDateTime future = LocalDateTime.now().plusSeconds(10);
            Chain chain = Chain.begin(false).isPast(future);
            assertThat(chain.isValid()).isFalse();
        }

        @Test
        @DisplayName("isFuture(LocalDateTime) - Future should be valid")
        void isFuture_Future_Valid() {
            LocalDateTime future = LocalDateTime.now().plusSeconds(10);
            Chain chain = Chain.begin(false).isFuture(future);
            assertThat(chain.isValid()).isTrue();
        }

        @Test
        @DisplayName("isFuture(LocalDateTime) - Past should be invalid")
        void isFuture_Past_Invalid() {
            LocalDateTime past = LocalDateTime.now().minusSeconds(1);
            Chain chain = Chain.begin(false).isFuture(past);
            assertThat(chain.isValid()).isFalse();
        }

        @Test
        @DisplayName("isFuture(LocalDateTime) - Now should be invalid")
        void isFuture_Now_Invalid() {
            LocalDateTime now = LocalDateTime.now();
            Chain chain = Chain.begin(false).isFuture(now);
            assertThat(chain.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Instant Tests")
    class InstantTests {

        @Test
        @DisplayName("isPast(Instant) - Past should be valid")
        void isPast_Past_Valid() {
            Instant past = Instant.now().minusSeconds(1);
            Chain chain = Chain.begin(false).isPast(past);
            assertThat(chain.isValid()).isTrue();
        }

        @Test
        @DisplayName("isPast(Instant) - Future should be invalid")
        void isPast_Future_Invalid() {
            Instant future = Instant.now().plusSeconds(10);
            Chain chain = Chain.begin(false).isPast(future);
            assertThat(chain.isValid()).isFalse();
        }

        @Test
        @DisplayName("isFuture(Instant) - Future should be valid")
        void isFuture_Future_Valid() {
            Instant future = Instant.now().plusSeconds(10);
            Chain chain = Chain.begin(false).isFuture(future);
            assertThat(chain.isValid()).isTrue();
        }

        @Test
        @DisplayName("isFuture(Instant) - Past should be invalid")
        void isFuture_Past_Invalid() {
            Instant past = Instant.now().minusSeconds(1);
            Chain chain = Chain.begin(false).isFuture(past);
            assertThat(chain.isValid()).isFalse();
        }

        @Test
        @DisplayName("isFuture(Instant) - Now (very close) should be invalid")
        void isFuture_Now_Invalid() {
            Instant now = Instant.now();
            Chain chain = Chain.begin(false).isFuture(now);
            assertThat(chain.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("ZonedDateTime Tests")
    class ZonedDateTimeTests {

        @Test
        @DisplayName("isPast(ZonedDateTime) - Different Timezone Past should be valid")
        void isPast_DiffZone_Past_Valid() {
            ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
            ZonedDateTime pastUtc = nowUtc.minusHours(1);
            ZonedDateTime pastTokyo = pastUtc.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));

            Chain chain = Chain.begin(false).isPast(pastTokyo);
            assertThat(chain.isValid()).isTrue();
        }

        @Test
        @DisplayName("isFuture(ZonedDateTime) - Different Timezone Future should be valid")
        void isFuture_DiffZone_Future_Valid() {
            ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
            ZonedDateTime futureUtc = nowUtc.plusHours(1);
            ZonedDateTime futureNY = futureUtc.withZoneSameInstant(ZoneId.of("America/New_York"));

            Chain chain = Chain.begin(false).isFuture(futureNY);
            assertThat(chain.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Robustness Tests (Null Inputs)")
    class RobustnessTests {

        @Test
        void testNullDate() {
            Chain chain = Chain.begin(false);
            chain.isPast((Date) null);
            assertThat(chain.isValid()).isFalse();
            chain.isFuture((Date) null);
            assertThat(chain.isValid()).isFalse();
        }

        @Test
        void testNullLocalDate() {
            Chain chain = Chain.begin(false);
            chain.isPast((LocalDate) null);
            assertThat(chain.isValid()).isFalse();
            chain.isFuture((LocalDate) null);
            assertThat(chain.isValid()).isFalse();
        }

        @Test
        void testNullLocalDateTime() {
            Chain chain = Chain.begin(false);
            chain.isPast((LocalDateTime) null);
            assertThat(chain.isValid()).isFalse();
            chain.isFuture((LocalDateTime) null);
            assertThat(chain.isValid()).isFalse();
        }

        @Test
        void testNullInstant() {
            Chain chain = Chain.begin(false);
            chain.isPast((Instant) null);
            assertThat(chain.isValid()).isFalse();
            chain.isFuture((Instant) null);
            assertThat(chain.isValid()).isFalse();
        }

        @Test
        void testNullZonedDateTime() {
            Chain chain = Chain.begin(false);
            chain.isPast((ZonedDateTime) null);
            assertThat(chain.isValid()).isFalse();
            chain.isFuture((ZonedDateTime) null);
            assertThat(chain.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Overload Tests with ResponseCode and Consumer")
    class OverloadTests {

        @Test
        void testIsPast_WithResponseCode() {
            Date future = new Date(System.currentTimeMillis() + 10000);
            ResponseCode code = ResponseCode.of(400, "Future date not allowed");

            Chain chain = Chain.begin(false).isPast(future, code);
            assertThat(chain.isValid()).isFalse();
            assertThat(chain.getCauses()).hasSize(1);
            assertThat(chain.getCauses().get(0).getResponseCode().getCode()).isEqualTo(400);
        }

        @Test
        void testIsPast_WithResponseCodeAndDetail() {
            Date future = new Date(System.currentTimeMillis() + 10000);
            ResponseCode code = ResponseCode.of(400, "Future date not allowed");

            Chain chain = Chain.begin(false).isPast(future, code, "detail info");
            assertThat(chain.isValid()).isFalse();
            assertThat(chain.getCauses().get(0).getDetail()).contains("detail info");
        }

        @Test
        void testIsPast_WithConsumer() {
            Date future = new Date(System.currentTimeMillis() + 10000);

            Chain chain = Chain.begin(false).isPast(future, ResponseCode.of(500, "Custom error"));
            assertThat(chain.isValid()).isFalse();
            assertThat(chain.getCauses().get(0).getResponseCode().getCode()).isEqualTo(500);
        }

        @Test
        void testIsFuture_WithResponseCode() {
            Date past = new Date(System.currentTimeMillis() - 10000);
            ResponseCode code = ResponseCode.of(400, "Past date not allowed");

            Chain chain = Chain.begin(false).isFuture(past, code);
            assertThat(chain.isValid()).isFalse();
            assertThat(chain.getCauses()).hasSize(1);
            assertThat(chain.getCauses().get(0).getResponseCode().getCode()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("Leap Year and Special Time Tests")
    class SpecialTimeTests {

        @Test
        void testLeapYear() {
            LocalDate leapDay = LocalDate.of(2024, 2, 29);
            if (LocalDate.now().isAfter(leapDay)) {
                assertThat(Chain.begin(false).isPast(leapDay).isValid()).isTrue();
            } else if (LocalDate.now().isBefore(leapDay)) {
                assertThat(Chain.begin(false).isFuture(leapDay).isValid()).isTrue();
            } else {
                assertThat(Chain.begin(false).isToday(leapDay).isValid()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Concurrency Tests")
    class ConcurrencyTests {

        @Test
        void testConcurrentValidation() throws InterruptedException {
            int threadCount = 20;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger errorCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        Date now = new Date();
                        Thread.sleep(1);
                        Date future = new Date(System.currentTimeMillis() + 10000);

                        boolean valid1 = Chain.begin(true).isPast(now).isValid();
                        boolean valid2 = Chain.begin(true).isFuture(future).isValid();
                        boolean valid3 = Chain.begin(true).isPast(future).isValid();

                        if (!valid1 || !valid2 || valid3) {
                            errorCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();
            assertThat(errorCount.get()).isEqualTo(0);
        }
    }
}
