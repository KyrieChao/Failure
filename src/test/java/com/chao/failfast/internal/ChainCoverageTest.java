package com.chao.failfast.internal;

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

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Chain 全面覆盖测试")
class ChainCoverageTest {

    private static final ResponseCode CODE = TestResponseCode.PARAM_ERROR;

    @Test
    @DisplayName("测试所有方法的成功路径 (Fail-Strict 模式)")
    void testAllMethodsSuccess() {
        Chain chain = Chain.begin(false);

        // Map
        Map<String, String> map = Collections.singletonMap("k", "v");
        chain.notEmpty(map)
                .notEmpty(map, CODE)
                .notEmpty(map, CODE, "msg")
                .containsKey(map, "k")
                .containsValue(map, "v")
                .notContainsKey(map, "x")
                .sizeBetween(map, 1, 1)
                .sizeEquals(map, 1)
                .isEmpty(Collections.emptyMap());

        // Optional
        chain.isPresent(Optional.of("v"))
                .isEmpty(Optional.empty());

        // Custom
        chain.satisfies("a", s -> s.equals("a"))
                .compare(1, 1, Integer::compareTo);

        // Object
        Object obj = new Object();
        chain.exists(obj)
                .notNull(obj)
                .isNull(null)
                .instanceOf(obj, Object.class)
                .notInstanceOf(obj, String.class)
                .allNotNull(obj, obj);

        // Boolean
        chain.state(true)
                .isTrue(true)
                .isFalse(false);

        // String
        String str = "abc";
        chain.notBlank(str)
                .notEmpty(str)
                .blank(null)
                .lengthBetween(str, 3, 3)
                .match("123", "\\d+")
                .email("test@example.com")
                .equalsIgnoreCase("a", "A")
                .startsWith(str, "a")
                .endsWith(str, "c")
                .contains(str, "b")
                .notContains(str, "d")
                .lengthMin(str, 3)
                .lengthMax(str, 3)
                .isNumeric("123")
                .isAlpha("abc")
                .isAlphanumeric("a1")
                .isLowerCase("abc")
                .isUpperCase("ABC")
                .mobile("13800138000")
                .url("http://example.com")
                .ipAddress("127.0.0.1")
                .uuid(UUID.randomUUID().toString());

        // Collection
        List<String> list = Collections.singletonList("a");
        chain.notEmpty(list)
                .sizeBetween(list, 1, 1)
                .sizeEquals(list, 1)
                .contains(list, "a")
                .notContains(list, "b")
                .isEmpty(Collections.emptyList())
                .hasNoNullElements(list)
                .allMatch(list, s -> s.equals("a"))
                .anyMatch(list, s -> s.equals("a"));

        // Array
        String[] array = {"a"};
        chain.notEmpty(array)
                .sizeBetween(array, 1, 1)
                .sizeEquals(array, 1)
                .contains(array, "a")
                .notContains(array, "b")
                .isEmpty(new String[]{})
                .hasNoNullElements(array)
                .allMatch(array, s -> s.equals("a"))
                .anyMatch(array, s -> s.equals("a"));

        // Number
        chain.positive(1)
                .inRange(5, 1, 10)
                .inRangeNumber(5, 1, 10)
                .nonNegative(0)
                .greaterThan(2, 1)
                .greaterOrEqual(1, 1)
                .lessThan(1, 2)
                .lessOrEqual(1, 1)
                .notZero(1)
                .isZero(0)
                .negative(-1)
                .multipleOf(10, 5)
                .decimalScale(new BigDecimal("1.23"), 2);

        // Date
        Date now = new Date();
        Date future = new Date(now.getTime() + 10000);
        Date past = new Date(now.getTime() - 10000);
        chain.after(future, now)
                .before(now, future)
                .isPast(past)
                .isFuture(future)
                .isToday(LocalDate.now());

        // Date Generic
        chain.after(2, 1)
                .afterOrEqual(1, 1)
                .before(1, 2)
                .beforeOrEqual(1, 1)
                .between(2, 1, 3);

        // Java 8 Time
        chain.isPast(LocalDate.now().minusDays(1))
                .isFuture(LocalDate.now().plusDays(1))
                .isPast(LocalDateTime.now().minusDays(1))
                .isFuture(LocalDateTime.now().plusDays(1))
                .isPast(Instant.now().minusSeconds(10))
                .isFuture(Instant.now().plusSeconds(10))
                .isPast(ZonedDateTime.now().minusDays(1))
                .isFuture(ZonedDateTime.now().plusDays(1));

        // Enum
        chain.enumValue(TestEnum.class, "A")
                .enumConstant(TestEnum.A, TestEnum.class);

        // Identity
        Object o1 = new Object();
        Object o2 = new Object();
        chain.same(o1, o1)
                .notSame(o1, o2)
                .equals(o1, o1)
                .notEquals(o1, o2);

        assertTrue(chain.isValid(), "Chain should be valid");
        assertEquals(0, chain.getCauses().size());
    }

    @Test
    @DisplayName("测试所有方法的失败路径 (Fail-Strict 模式)")
    void testAllMethodsFailure() {
        Chain chain = Chain.begin(false);
        int expectedErrors = 0;

        // Map
        Map<String, String> map = Collections.singletonMap("k", "v");
        chain.notEmpty(Collections.emptyMap(), CODE);
        expectedErrors++;
        chain.isEmpty(map, CODE);
        expectedErrors++;
        chain.containsKey(map, "x", CODE);
        expectedErrors++;
        chain.containsValue(map, "x", CODE);
        expectedErrors++;
        chain.notContainsKey(map, "k", CODE);
        expectedErrors++;
        chain.sizeBetween(map, 2, 3, CODE);
        expectedErrors++;
        chain.sizeEquals(map, 2, CODE);
        expectedErrors++;
        assertEquals(expectedErrors, chain.getCauses().size(), "Map checks failed");

        // Optional
        chain.isPresent(Optional.empty(), CODE);
        expectedErrors++;
        chain.isEmpty(Optional.of("v"), CODE);
        expectedErrors++;
        assertEquals(expectedErrors, chain.getCauses().size(), "Optional checks failed");

        // Custom
        chain.satisfies("a", s -> false, CODE);
        expectedErrors++;
        chain.compare(1, 2, Integer::compareTo, CODE);
        expectedErrors++;
        assertEquals(expectedErrors, chain.getCauses().size(), "Custom checks failed");

        // Object
        chain.exists(null, CODE);
        expectedErrors++;
        chain.notNull(null, CODE);
        expectedErrors++;
        chain.isNull(new Object(), CODE);
        expectedErrors++;
        chain.instanceOf(new Object(), String.class, CODE);
        expectedErrors++;
        chain.notInstanceOf("s", String.class, CODE);
        expectedErrors++;
        chain.allNotNull(CODE, (Object) null, "a");
        expectedErrors++;
        assertEquals(expectedErrors, chain.getCauses().size(), "Object checks failed");

        // Boolean
        chain.state(false, CODE);
        expectedErrors++;
        chain.isTrue(false, CODE);
        expectedErrors++;
        chain.isFalse(true, CODE);
        expectedErrors++;
        assertEquals(expectedErrors, chain.getCauses().size(), "Boolean checks failed");

        // String
        String str = "abc";
        chain.notBlank("", CODE);
        expectedErrors++;
        chain.blank("a", CODE);
        expectedErrors++;
        chain.lengthBetween(str, 4, 5, CODE);
        expectedErrors++;
        chain.match("abc", "\\d+", CODE);
        expectedErrors++;
        chain.email("invalid", CODE);
        expectedErrors++;
        chain.equalsIgnoreCase("a", "b", CODE);
        expectedErrors++;
        chain.startsWith(str, "b", CODE);
        expectedErrors++;
        chain.endsWith(str, "a", CODE);
        expectedErrors++;
        chain.contains(str, "d", CODE);
        expectedErrors++;
        chain.notContains(str, "b", CODE);
        expectedErrors++;
        chain.lengthMin(str, 4, CODE);
        expectedErrors++;
        chain.lengthMax(str, 2, CODE);
        expectedErrors++;
        chain.isNumeric("a", CODE);
        expectedErrors++;
        chain.isAlpha("1", CODE);
        expectedErrors++;
        chain.isAlphanumeric("$", CODE);
        expectedErrors++;
        chain.isLowerCase("A", CODE);
        expectedErrors++;
        chain.isUpperCase("a", CODE);
        expectedErrors++;
        chain.mobile("123", CODE);
        expectedErrors++;
        chain.url("invalid", CODE);
        expectedErrors++;
        chain.ipAddress("999.9.9.9", CODE);
        expectedErrors++;
        chain.uuid("invalid", CODE);
        expectedErrors++;
        assertEquals(expectedErrors, chain.getCauses().size(), "String checks failed");

        // Collection
        List<String> list = Collections.singletonList("a");
        chain.notEmpty(Collections.emptyList(), CODE);
        expectedErrors++;
        chain.sizeBetween(list, 2, 3, CODE);
        expectedErrors++;
        chain.sizeEquals(list, 2, CODE);
        expectedErrors++;
        chain.contains(list, "b", CODE);
        expectedErrors++;
        chain.notContains(list, "a", CODE);
        expectedErrors++;
        chain.isEmpty(list, CODE);
        expectedErrors++;
        chain.hasNoNullElements(Arrays.asList("a", null), CODE);
        expectedErrors++;
        chain.allMatch(list, s -> false, CODE);
        expectedErrors++;
        chain.anyMatch(list, s -> false, CODE);
        expectedErrors++;
        assertEquals(expectedErrors, chain.getCauses().size(), "Collection checks failed");

        // Array
        String[] array = {"a"};
        chain.notEmpty(new String[]{}, CODE);
        expectedErrors++;
        chain.sizeBetween(array, 2, 3, CODE);
        expectedErrors++;
        chain.sizeEquals(array, 2, CODE);
        expectedErrors++;
        chain.contains(array, "b", CODE);
        expectedErrors++;
        chain.notContains(array, "a", CODE);
        expectedErrors++;
        chain.isEmpty(array, CODE);
        expectedErrors++;
        chain.hasNoNullElements(new String[]{"a", null}, CODE);
        expectedErrors++;
        chain.allMatch(array, s -> false, CODE);
        expectedErrors++;
        chain.anyMatch(array, s -> false, CODE);
        expectedErrors++;
        assertEquals(expectedErrors, chain.getCauses().size(), "Array checks failed");

        // Number
        chain.positive(-1, CODE);
        expectedErrors++;
        chain.inRange(1, 5, 10, CODE);
        expectedErrors++;
        chain.nonNegative(-1, CODE);
        expectedErrors++;
        chain.greaterThan(1, 2, CODE);
        expectedErrors++;
        chain.greaterOrEqual(1, 2, CODE);
        expectedErrors++;
        chain.lessThan(2, 1, CODE);
        expectedErrors++;
        chain.lessOrEqual(2, 1, CODE);
        expectedErrors++;
        chain.notZero(0, CODE);
        expectedErrors++;
        chain.isZero(1, CODE);
        expectedErrors++;
        chain.negative(1, CODE);
        expectedErrors++;
        chain.multipleOf(10, 3, CODE);
        expectedErrors++;
        chain.decimalScale(new BigDecimal("1.234"), 2, CODE);
        expectedErrors++;
        assertEquals(expectedErrors, chain.getCauses().size(), "Number checks failed");

        // Date
        Date now = new Date();
        chain.after(now, now, CODE);
        expectedErrors++;
        chain.before(now, now, CODE);
        expectedErrors++;
        chain.isPast(new Date(now.getTime() + 10000), CODE);
        expectedErrors++;
        chain.isFuture(new Date(now.getTime() - 10000), CODE);
        expectedErrors++;
        chain.isToday(LocalDate.now().minusDays(1), CODE);
        expectedErrors++;
        assertEquals(expectedErrors, chain.getCauses().size(), "Date checks failed");

        // Identity
        Object o1 = new Object();
        Object o2 = new Object();
        chain.same(o1, o2, CODE);
        expectedErrors++;
        chain.notSame(o1, o1, CODE);
        expectedErrors++;
        chain.equals(o1, o2, CODE);
        expectedErrors++;
        chain.notEquals(o1, o1, CODE);
        expectedErrors++;
        assertEquals(expectedErrors, chain.getCauses().size(), "Identity checks failed");

        assertFalse(chain.isValid());
        assertEquals(expectedErrors, chain.getCauses().size());

        // Test failAll
        assertThrows(MultiBusiness.class, chain::failAll);
    }

    @Test
    void testFailLogic() {
        Chain chain = Chain.begin(false);
        chain.isTrue(false, CODE);
        assertThrows(Business.class, chain::fail);

        Chain emptyChain = Chain.begin(false);
        // Testing fail() on empty error list with invalid state (though usually valid state if no errors)
        // To force invalid state without errors, we need manual intervention or specific condition
        // Actually Chain logic: isValid() -> errors.isEmpty() && alive.
        // if failFast=false, alive stays true even if errors added? No.
        // Let's check check(): if (failFast) alive = false;
        // So if failFast=false, alive remains true!
        // So isValid() depends on errors.isEmpty() AND alive.
        // if failFast=false, errors is not empty, so isValid is false.

        // If we want to test the branch where errors is empty but !isValid() in fail()
        // We need alive=false but errors empty.
        // How to set alive=false without adding error?
        // Chain.check(boolean) updates alive.
        // check(condition) -> alive = condition.
        Chain chain2 = Chain.begin(false); // failFast=false
        chain2.isTrue(false); // Calls state(false) -> check(false) -> alive=false. No error added.
        assertFalse(chain2.isValid());
        assertFalse(chain2.getCauses().isEmpty());
        // Now fail() should throw generic Business exception
        assertThrows(Business.class, chain2::fail);
    }

    enum TestEnum {A, B}
}
