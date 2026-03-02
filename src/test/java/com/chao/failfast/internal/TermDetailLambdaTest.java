package com.chao.failfast.internal;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.model.TestResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 补充测试：覆盖所有 Term 接口中 (args, code, detail) 重载版本的 lambda 表达式。
 * 这些 lambda 只有在校验失败时才会执行。
 */
@DisplayName("Term 接口 Lambda 覆盖测试")
class TermDetailLambdaTest {

    private static final ResponseCode CODE = TestResponseCode.PARAM_ERROR;
    private static final String DETAIL = "detail";

    @Test
    @DisplayName("StringTerm detail lambda coverage")
    void testStringTerm() {
        Chain chain = Failure.strict();
        String str = "abc";

        chain.notBlank("", CODE, DETAIL)
                .blank("a", CODE, DETAIL)
                .lengthBetween(str, 5, 10, CODE, DETAIL)
                .match(str, "\\d+", CODE, DETAIL)
                .email("invalid", CODE, DETAIL)
                .mobile("123", CODE, DETAIL)
                .url("invalid", CODE, DETAIL)
                .ipAddress("999.9.9.9", CODE, DETAIL)
                .uuid("invalid", CODE, DETAIL)
                .isNumeric("a", CODE, DETAIL)
                .isAlpha("1", CODE, DETAIL)
                .isAlphanumeric("$", CODE, DETAIL)
                .startsWith(str, "b", CODE, DETAIL)
                .endsWith(str, "a", CODE, DETAIL)
                .contains(str, "d", CODE, DETAIL)
                .notContains(str, "b", CODE, DETAIL)
                .isLowerCase("A", CODE, DETAIL)
                .isUpperCase("a", CODE, DETAIL)
                .equalsIgnoreCase("a", "b", CODE, DETAIL)
                .lengthMin(str, 5, CODE, DETAIL)
                .lengthMax(str, 2, CODE, DETAIL);

        assertAllFailedWithDetail(chain, 21);
    }

    @Test
    @DisplayName("CollectionTerm detail lambda coverage")
    void testCollectionTerm() {
        Chain chain = Failure.strict();
        List<String> list = List.of("a");

        chain.notEmpty(Collections.emptyList(), CODE, DETAIL)
                .isEmpty(list, CODE, DETAIL)
                .sizeBetween(list, 2, 3, CODE, DETAIL)
                .sizeEquals(list, 2, CODE, DETAIL)
                .contains(list, "b", CODE, DETAIL)
                .notContains(list, "a", CODE, DETAIL)
                .hasNoNullElements(Arrays.asList("a", null), CODE, DETAIL)
                .allMatch(list, s -> false, CODE, DETAIL)
                .anyMatch(list, s -> false, CODE, DETAIL);

        assertAllFailedWithDetail(chain, 9);
    }

    @Test
    @DisplayName("ArrayTerm detail lambda coverage")
    void testArrayTerm() {
        Chain chain = Failure.strict();
        String[] arr = {"a"};

        chain.notEmpty(new String[]{}, CODE, DETAIL)
                .isEmpty(arr, CODE, DETAIL)
                .sizeBetween(arr, 2, 3, CODE, DETAIL)
                .sizeEquals(arr, 2, CODE, DETAIL)
                .contains(arr, "b", CODE, DETAIL)
                .notContains(arr, "a", CODE, DETAIL)
                .hasNoNullElements(new String[]{"a", null}, CODE, DETAIL)
                .allMatch(arr, s -> false, CODE, DETAIL)
                .anyMatch(arr, s -> false, CODE, DETAIL);

        assertAllFailedWithDetail(chain, 9);
    }

    @Test
    @DisplayName("MapTerm detail lambda coverage")
    void testMapTerm() {
        Chain chain = Failure.strict();
        Map<String, String> map = Map.of("k", "v");

        chain.notEmpty(Collections.emptyMap(), CODE, DETAIL)
                .isEmpty(map, CODE, DETAIL)
                .containsKey(map, "x", CODE, DETAIL)
                .notContainsKey(map, "k", CODE, DETAIL)
                .containsValue(map, "x", CODE, DETAIL)
                .sizeBetween(map, 2, 3, CODE, DETAIL)
                .sizeEquals(map, 2, CODE, DETAIL);

        assertAllFailedWithDetail(chain, 7);
    }

    @Test
    @DisplayName("NumberTerm detail lambda coverage")
    void testNumberTerm() {
        Chain chain = Failure.strict();

        chain.positive(-1, CODE, DETAIL)
                .positiveNumber(-1, CODE, DETAIL)
                .inRange(0, 5, 10, CODE, DETAIL)
                .inRangeNumber(0, 5, 10, CODE, DETAIL)
                .nonNegative(-1, CODE, DETAIL)
                .greaterThan(1, 2, CODE, DETAIL)
                .greaterOrEqual(1, 2, CODE, DETAIL)
                .lessThan(2, 1, CODE, DETAIL)
                .lessOrEqual(2, 1, CODE, DETAIL)
                .notZero(0, CODE, DETAIL)
                .isZero(1, CODE, DETAIL)
                .negative(1, CODE, DETAIL)
                .multipleOf(10, 3, CODE, DETAIL)
                .decimalScale(new BigDecimal("1.234"), 2, CODE, DETAIL);

        assertAllFailedWithDetail(chain, 14);
    }

    @Test
    @DisplayName("DateTerm detail lambda coverage")
    void testDateTerm() {
        Chain chain = Failure.strict();
        Date now = new Date();
        LocalDate localDate = LocalDate.now();
        LocalDateTime localDateTime = LocalDateTime.now();
        Instant instant = Instant.now();
        ZonedDateTime zonedDateTime = ZonedDateTime.now();

        chain.after(now, now, CODE, DETAIL)
                .before(now, now, CODE, DETAIL)
//             .afterOrEqual(now.minusDays(1).getTime(), now.getTime(), CODE, DETAIL) // Date is not Comparable directly in afterOrEqual? No, Date implements Comparable.
                .afterOrEqual(new Date(now.getTime() - 86400000L), now, CODE, DETAIL)
                // Wait, Date implements Comparable<Date>.
                // afterOrEqual(T, T) where T extends Comparable<T>.
                .isPast(new Date(now.getTime() + 10000), CODE, DETAIL)
                .isFuture(new Date(now.getTime() - 10000), CODE, DETAIL)

                .isPast(localDate.plusDays(1), CODE, DETAIL)
                .isFuture(localDate.minusDays(1), CODE, DETAIL)
                .isToday(localDate.minusDays(1), CODE, DETAIL)

                .isPast(localDateTime.plusDays(1), CODE, DETAIL)
                .isFuture(localDateTime.minusDays(1), CODE, DETAIL)

                .isPast(instant.plusSeconds(10), CODE, DETAIL)
                .isFuture(instant.minusSeconds(10), CODE, DETAIL)

                .isPast(zonedDateTime.plusDays(1), CODE, DETAIL)
                .isFuture(zonedDateTime.minusDays(1), CODE, DETAIL)

                .after(2, 1, CODE, DETAIL) // 2 after 1 is true (valid). Wait, I want failure.
                // after(t1, t2) checks t1 > t2?
                // DateChecks.after(d1, d2) -> d1.after(d2).
                // If I want failure: after(1, 2) -> 1 is after 2? False.
                .after(1, 2, CODE, DETAIL)
                .before(2, 1, CODE, DETAIL)
                .afterOrEqual(1, 2, CODE, DETAIL)
                .beforeOrEqual(2, 1, CODE, DETAIL)
                .between(5, 1, 3, CODE, DETAIL);

        // Count:
        // 1. after(Date) -> fail
        // 2. before(Date) -> fail
        // 3. afterOrEqual(Date, Date) -> 1 < 0 ? Fail. (Wait, now-1 vs now. (now-1).compareTo(now) < 0. afterOrEqual requires >= 0.)
        // 4. isPast(Date future) -> fail
        // 5. isFuture(Date past) -> fail
        // 6. isPast(LocalDate future) -> fail
        // 7. isFuture(LocalDate past) -> fail
        // 8. isToday(yesterday) -> fail
        // 9. isPast(LocalDateTime future) -> fail
        // 10. isFuture(LocalDateTime past) -> fail
        // 11. isPast(Instant future) -> fail
        // 12. isFuture(Instant past) -> fail
        // 13. isPast(ZonedDateTime future) -> fail
        // 14. isFuture(ZonedDateTime past) -> fail
        // 15. after(1, 2) -> fail
        // 16. before(2, 1) -> fail
        // 17. afterOrEqual(1, 2) -> fail
        // 18. beforeOrEqual(2, 1) -> fail
        // 19. between(5, 1, 3) -> fail

        assertAllFailedWithDetail(chain, 19);
    }

    @Test
    @DisplayName("BooleanTerm detail lambda coverage")
    void testBooleanTerm() {
        Chain chain = Failure.strict();
        chain.state(false, CODE, DETAIL)
                .isTrue(false, CODE, DETAIL)
                .isFalse(true, CODE, DETAIL);
        assertAllFailedWithDetail(chain, 3);
    }

    @Test
    @DisplayName("ObjectTerm detail lambda coverage")
    void testObjectTerm() {
        Chain chain = Failure.strict();
        chain.exists(null, CODE, DETAIL)
                .notNull(null, CODE, DETAIL)
                .isNull(new Object(), CODE, DETAIL)
                .instanceOf(new Object(), String.class, CODE, DETAIL)
                .notInstanceOf("s", String.class, CODE, DETAIL)
                .allNotNull(CODE, DETAIL, (Object) null);
        assertAllFailedWithDetail(chain, 6);
    }

    @Test
    @DisplayName("OptionalTerm detail lambda coverage")
    void testOptionalTerm() {
        Chain chain = Failure.strict();
        chain.isPresent(Optional.empty(), CODE, DETAIL)
                .isEmpty(Optional.of("a"), CODE, DETAIL);
        assertAllFailedWithDetail(chain, 2);
    }

    @Test
    @DisplayName("EnumTerm detail lambda coverage")
    void testEnumTerm() {
        Chain chain = Failure.strict();
        chain.enumValue(TestEnum.class, "X", CODE, DETAIL)
                .enumConstant(null, TestEnum.class, CODE, DETAIL);
        assertAllFailedWithDetail(chain, 2);
    }

    @Test
    @DisplayName("IdentityTerm detail lambda coverage")
    void testIdentityTerm() {
        Chain chain = Failure.strict();
        Object o1 = new Object();
        Object o2 = new Object();
        chain.same(o1, o2, CODE, DETAIL)
                .notSame(o1, o1, CODE, DETAIL)
                .equals(o1, o2, CODE, DETAIL)
                .notEquals(o1, o1, CODE, DETAIL);
        assertAllFailedWithDetail(chain, 4);
    }

    private void assertAllFailedWithDetail(Chain chain, int expectedCount) {
        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses()).hasSize(expectedCount);
        assertThat(chain.getCauses()).allSatisfy(error -> {
            assertThat(error.getResponseCode().getCode()).isEqualTo(CODE.getCode());
            assertThat(error.getDetail()).isEqualTo(DETAIL);
        });
    }

    enum TestEnum {A, B}
}
