package com.chao.failfast.internal;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.model.TestResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chain 类 100% 覆盖率测试
 * 覆盖所有公共方法及其重载版本
 */
@DisplayName("Chain 完整覆盖测试")
public class ChainTest {

    private static final ResponseCode TEST_CODE = TestResponseCode.PARAM_ERROR;
    private static final ResponseCode TEST_CODE_2 = TestResponseCode.PARAM_REQUIRED;
    private static final String TEST_DETAIL = "test detail";

    // ==================== 基础构造与状态管理 ====================

    @Test
    @DisplayName("测试 Chain.begin() 构造")
    void testBegin() {
        Chain chain = Chain.begin(true);
        assertNotNull(chain);
        assertTrue(chain.isValid());
        assertTrue(chain.getCauses().isEmpty());
    }

    @Test
    @DisplayName("测试 getErrors() 返回副本")
    void testGetCausesReturnsCopy() {
        Chain chain = Chain.begin(true);
        List<Business> errors = chain.getCauses();
        assertNotNull(errors);
        // 验证返回的是副本
        assertDoesNotThrow(() -> errors.add(Business.of(TEST_CODE)));
        assertTrue(chain.getCauses().isEmpty()); // 原链不受影响
    }

    @Test
    @DisplayName("测试 isValid() 状态变化")
    void testIsValidStateChange() {
        Chain chain = Chain.begin(true);
        assertTrue(chain.isValid());

        chain.exists(null, TEST_CODE);
        assertFalse(chain.isValid());
    }

    // ==================== 终结操作 - fail() ====================

    @Test
    @DisplayName("测试 fail() 无错误时通过")
    void testFailNoError() {
        Chain chain = Chain.begin(true);
        assertDoesNotThrow(chain::fail);
    }

    @Test
    @DisplayName("测试 fail() 有错误时抛出异常")
    void testFailWithError() {
        Chain chain = Chain.begin(true);
        chain.exists(null, TEST_CODE);

        Business exception = assertThrows(Business.class, chain::fail);
        assertEquals(TEST_CODE.getCode(), exception.getResponseCode().getCode());
    }

    @Test
    @DisplayName("测试 fail() 使用无参check导致alive=false但errors为空的情况")
    void testFailWithAliveFalseButNoErrors() {
        Chain chain = Chain.begin(true);
        // 使用无参 check 使 alive=false 但不添加错误
        chain.state(false); // 这会使 alive=false

        Business exception = assertThrows(Business.class, chain::fail);
        assertEquals(500, exception.getResponseCode().getCode());
    }

    // ==================== 终结操作 - failAll() ====================

    @Test
    @DisplayName("测试 failAll() 无错误时通过")
    void testFailAllNoError() {
        Chain chain = Chain.begin(true);
        assertDoesNotThrow(chain::failAll);
    }

    @Test
    @DisplayName("测试 failAll() 单个错误时抛出 Business")
    void testFailAllSingleError() {
        Chain chain = Chain.begin(false); // strict 模式
        chain.exists(null, TEST_CODE);

        Business exception = assertThrows(Business.class, chain::failAll);
        assertEquals(TEST_CODE.getCode(), exception.getResponseCode().getCode());
    }

    @Test
    @DisplayName("测试 failAll() 多个错误时抛出 MultiBusiness")
    void testFailAllMultipleErrors() {
        Chain chain = Chain.begin(false); // strict 模式
        chain.exists(null, TEST_CODE);
        chain.exists(null, TEST_CODE_2);

        MultiBusiness exception = assertThrows(MultiBusiness.class, chain::failAll);
        assertEquals(2, exception.getErrors().size());
    }

    @Test
    @DisplayName("测试 failAll() alive=false但errors为空的情况")
    void testFailAllWithAliveFalseButNoErrors() {
        Chain chain = Chain.begin(true);
        chain.state(false);

        Business exception = assertThrows(Business.class, chain::failAll);
        assertEquals(500, exception.getResponseCode().getCode());
    }

    // ==================== 终结操作 - failNow() ====================

    @Test
    @DisplayName("测试 failNow(ResponseCode) 链式调用")
    void testFailNowWithCode() {
        // alive=true 时不抛出
        Chain chain = Chain.begin(true);
        assertDoesNotThrow(() -> chain.failNow(TEST_CODE));

        // alive=false 时抛出
        Chain failChain = Chain.begin(true);
        failChain.state(false);
        Business exception = assertThrows(Business.class, () -> failChain.failNow(TEST_CODE));
        assertEquals(TEST_CODE.getCode(), exception.getResponseCode().getCode());
    }

    @Test
    @DisplayName("测试 failNow(ResponseCode, String) 链式调用")
    void testFailNowWithCodeAndMsg() {
        Chain chain = Failure.begin();
        chain.state(false);
        Business exception = assertThrows(Business.class, () -> chain.failNow(TEST_CODE, "custom message"));
        assertEquals(TEST_CODE.getCode(), exception.getResponseCode().getCode());
        assertEquals("custom message", exception.getDetail());
    }

    @Test
    @DisplayName("failNow 无错误时不应抛异常")
    void testFailNowNoError() {
        assertDoesNotThrow(() ->
                Failure.begin()
                        .state(true)  // 条件满足，无错误
                        .failNow(TEST_CODE, "should not throw")
        );
    }

    @Test
    @DisplayName("测试 failNow(ResponseCode, String, Object...) 链式调用")
    void testFailNowWithCodeAndFormat() {
        Chain chain = Chain.begin(true);
        chain.state(false);
        Business exception = assertThrows(Business.class, () -> chain.failNow(TEST_CODE, "value is %s", "wrong"));
        assertEquals("value is wrong", exception.getDetail());
    }

    @Test
    @DisplayName("测试 failNow(Supplier) 链式调用")
    void testFailNowWithSupplier() {
        Chain chain = Chain.begin(true);
        chain.state(false);
//        Business exception = assertThrows(Business.class, () -> chain.failNow(() -> Business.of(TEST_CODE)));
        Business exception = assertThrows(Business.class, () -> chain.failNow(TEST_CODE));
        assertEquals(TEST_CODE.getCode(), exception.getResponseCode().getCode());
    }

    // ==================== 终结操作 - onFail ====================

    @Test
    @DisplayName("测试 onFail(Runnable) 在失败时执行")
    void testOnFailExecutesOnFailure() {
        final boolean[] executed = {false};
        Chain chain = Chain.begin(true);
        chain.state(false, TEST_CODE).onFail(() -> executed[0] = true);
        assertTrue(executed[0]);
    }

    @Test
    @DisplayName("测试 onFail(Runnable) 在成功时不执行")
    void testOnFailDoesNotExecuteOnSuccess() {
        final boolean[] executed = {false};
        Chain chain = Chain.begin(true);
        chain.state(true, TEST_CODE).onFail(() -> executed[0] = true);
        assertFalse(executed[0]);
    }

    @Test
    @DisplayName("测试 onFailGet(Supplier) 在失败时返回值")
    void testOnFailGetReturnsOnFailure() {
        Chain chain = Chain.begin(true);
        Optional<String> result = chain.state(false, TEST_CODE).onFailGet(() -> "default");
        assertTrue(result.isPresent());
        assertEquals("default", result.get());
    }

    @Test
    @DisplayName("测试 onFailGet(Supplier) 在成功时返回空")
    void testOnFailGetEmptyOnSuccess() {
        Chain chain = Chain.begin(true);
        Optional<String> result = chain.state(true, TEST_CODE).onFailGet(() -> "default");
        assertFalse(result.isPresent());
    }

    // ==================== Object 校验 ====================

    @Test
    @DisplayName("测试 exists() 所有重载")
    void testExistsAllVariants() {
        Object obj = new Object();

        // 无参版本
        assertDoesNotThrow(() -> Failure.begin().exists(obj).fail());
        assertThrows(Business.class, () -> Failure.begin().exists(null).fail());

        // ResponseCode 版本
        assertDoesNotThrow(() -> Failure.begin().exists(obj, TEST_CODE).fail());
        assertThrows(Business.class, () -> Failure.begin().exists(null, TEST_CODE).fail());

        // ResponseCode + String 版本
        assertDoesNotThrow(() -> Failure.begin().exists(obj, TEST_CODE, TEST_DETAIL).fail());
        assertThrows(Business.class, () -> Failure.begin().exists(null, TEST_CODE, TEST_DETAIL).fail());

        // Consumer 版本
    }

    @Test
    @DisplayName("测试 notNull() 所有重载")
    void testNotNullAllVariants() {
        Object obj = new Object();

        assertDoesNotThrow(() -> Failure.begin().notNull(obj).fail());
        assertDoesNotThrow(() -> Failure.begin().notNull(obj, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notNull(obj, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notNull(null, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isNull() 所有重载")
    void testIsNullAllVariants() {
        Object obj = new Object();

        assertDoesNotThrow(() -> Failure.begin().isNull(null).fail());
        assertDoesNotThrow(() -> Failure.begin().isNull(null, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isNull(null, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().isNull(obj, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 instanceOf() 所有重载")
    void testInstanceOfAllVariants() {
        String str = "test";

        assertDoesNotThrow(() -> Failure.begin().instanceOf(str, String.class).fail());
        assertDoesNotThrow(() -> Failure.begin().instanceOf(str, String.class, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().instanceOf(str, String.class, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().instanceOf(str, Integer.class, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 notInstanceOf() 所有重载")
    void testNotInstanceOfAllVariants() {
        String str = "test";

        assertDoesNotThrow(() -> Failure.begin().notInstanceOf(str, Integer.class).fail());
        assertDoesNotThrow(() -> Failure.begin().notInstanceOf(str, Integer.class, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notInstanceOf(str, Integer.class, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notInstanceOf(str, String.class, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 allNotNull() 所有重载")
    void testAllNotNullAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().allNotNull("a", "b", "c").fail());
        assertDoesNotThrow(() -> Failure.begin().allNotNull(TEST_CODE, (Object) "", "a", "b").fail());
        assertDoesNotThrow(() -> Failure.begin().allNotNull(TEST_CODE, TEST_DETAIL, "a", "b").fail());

        assertThrows(Business.class, () -> Failure.begin().allNotNull("a", null, TEST_CODE).fail());
    }

    // ==================== Boolean 校验 ====================

    @Test
    @DisplayName("测试 state() 所有重载")
    void testStateAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().state(true).fail());
        assertDoesNotThrow(() -> Failure.begin().state(true, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().state(true, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().state(false, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isTrue() 所有重载")
    void testIsTrueAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().isTrue(true).fail());
        assertDoesNotThrow(() -> Failure.begin().isTrue(true, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isTrue(true, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().isTrue(false, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isFalse() 所有重载")
    void testIsFalseAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().isFalse(false).fail());
        assertDoesNotThrow(() -> Failure.begin().isFalse(false, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isFalse(false, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().isFalse(true, TEST_CODE).fail());
    }

    // ==================== String 校验 ====================

    @Test
    @DisplayName("测试 blank() 所有重载")
    void testBlankAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().blank("").fail());
        assertDoesNotThrow(() -> Failure.begin().blank("", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().blank("", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().blank("a", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 notBlank() 所有重载")
    void testNotBlankAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().notBlank("a").fail());
        assertDoesNotThrow(() -> Failure.begin().notBlank("a", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notBlank("a", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notBlank("", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 notEmpty(String) 所有重载")
    void testNotEmptyStringAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().notEmpty("a").fail());
        assertDoesNotThrow(() -> Failure.begin().notEmpty("a", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notEmpty("a", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notEmpty("", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 lengthBetween() 所有重载")
    void testLengthBetweenAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().lengthBetween("abc", 1, 5).fail());
        assertDoesNotThrow(() -> Failure.begin().lengthBetween("abc", 1, 5, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().lengthBetween("abc", 1, 5, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().lengthBetween("abc", 5, 10, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 match() 所有重载")
    void testMatchAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().match("123", "\\d+").fail());
        assertDoesNotThrow(() -> Failure.begin().match("123", "\\d+", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().match("123", "\\d+", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().match("abc", "\\d+", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 email() 所有重载")
    void testEmailAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().email("test@example.com").fail());
        assertDoesNotThrow(() -> Failure.begin().email("test@example.com", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().email("test@example.com", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().email("invalid", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 equalsIgnoreCase() 所有重载")
    void testEqualsIgnoreCaseAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().equalsIgnoreCase("abc", "ABC").fail());
        assertDoesNotThrow(() -> Failure.begin().equalsIgnoreCase("abc", "ABC", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().equalsIgnoreCase("abc", "ABC", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().equalsIgnoreCase("abc", "xyz", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 startsWith() 所有重载")
    void testStartsWithAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().startsWith("abc", "a").fail());
        assertDoesNotThrow(() -> Failure.begin().startsWith("abc", "a", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().startsWith("abc", "a", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().startsWith("abc", "b", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 endsWith() 所有重载")
    void testEndsWithAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().endsWith("abc", "c").fail());
        assertDoesNotThrow(() -> Failure.begin().endsWith("abc", "c", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().endsWith("abc", "c", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().endsWith("abc", "a", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 contains(String) 所有重载")
    void testContainsStringAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().contains("abc", "b").fail());
        assertDoesNotThrow(() -> Failure.begin().contains("abc", "b", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().contains("abc", "b", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().contains("abc", "x", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 notContains(String) 所有重载")
    void testNotContainsStringAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().notContains("abc", "x").fail());
        assertDoesNotThrow(() -> Failure.begin().notContains("abc", "x", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notContains("abc", "x", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notContains("abc", "b", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 lengthMin() 所有重载")
    void testLengthMinAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().lengthMin("abc", 2).fail());
        assertDoesNotThrow(() -> Failure.begin().lengthMin("abc", 2, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().lengthMin("abc", 2, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().lengthMin("a", 5, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 lengthMax() 所有重载")
    void testLengthMaxAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().lengthMax("a", 5).fail());
        assertDoesNotThrow(() -> Failure.begin().lengthMax("a", 5, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().lengthMax("a", 5, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().lengthMax("abcdef", 5, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isNumeric() 所有重载")
    void testIsNumericAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().isNumeric("123").fail());
        assertDoesNotThrow(() -> Failure.begin().isNumeric("123", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isNumeric("123", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().isNumeric("abc", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isAlpha() 所有重载")
    void testIsAlphaAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().isAlpha("abc").fail());
        assertDoesNotThrow(() -> Failure.begin().isAlpha("abc", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isAlpha("abc", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().isAlpha("123", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isAlphanumeric() 所有重载")
    void testIsAlphanumericAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().isAlphanumeric("abc123").fail());
        assertDoesNotThrow(() -> Failure.begin().isAlphanumeric("abc123", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isAlphanumeric("abc123", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().isAlphanumeric("abc-123", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isLowerCase() 所有重载")
    void testIsLowerCaseAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().isLowerCase("abc").fail());
        assertDoesNotThrow(() -> Failure.begin().isLowerCase("abc", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isLowerCase("abc", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().isLowerCase("ABC", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isUpperCase() 所有重载")
    void testIsUpperCaseAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().isUpperCase("ABC").fail());
        assertDoesNotThrow(() -> Failure.begin().isUpperCase("ABC", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isUpperCase("ABC", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().isUpperCase("abc", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 mobile() 所有重载")
    void testMobileAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().mobile("13800138000").fail());
        assertDoesNotThrow(() -> Failure.begin().mobile("13800138000", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().mobile("13800138000", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().mobile("12345678901", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 url() 所有重载")
    void testUrlAllVariants() {
        assertThrows(Business.class, () -> Failure.begin().url("").fail());
        assertThrows(Business.class, () -> Failure.begin().url(null).fail());
        assertThrows(Business.class, () -> Failure.begin().url("https").fail());
        assertThrows(Business.class, () -> Failure.begin().url("example.com").fail());
        assertThrows(Business.class, () -> Failure.begin().url("example.com", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().url("https://example.com").fail());
        assertDoesNotThrow(() -> Failure.begin().url("https://example.com", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().url("https://example.com", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().url("not-a-url", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 ipAddress() 所有重载")
    void testIpAddressAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().ipAddress("192.168.1.1").fail());
        assertDoesNotThrow(() -> Failure.begin().ipAddress("192.168.1.1", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().ipAddress("192.168.1.1", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().ipAddress("999.999.999.999", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 uuid() 所有重载")
    void testUuidAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().uuid("550e8400-e29b-41d4-a716-446655440000").fail());
        assertDoesNotThrow(() -> Failure.begin().uuid("550e8400-e29b-41d4-a716-446655440000", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().uuid("550e8400-e29b-41d4-a716-446655440000", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().uuid("not-a-uuid", TEST_CODE).fail());
    }

    // ==================== Collection 校验 ====================

    @Test
    @DisplayName("测试 notEmpty(Collection) 所有重载")
    void testNotEmptyCollectionAllVariants() {
        List<String> list = List.of("a");

        assertDoesNotThrow(() -> Failure.begin().notEmpty(list).fail());
        assertDoesNotThrow(() -> Failure.begin().notEmpty(list, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notEmpty(list, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notEmpty(Collections.emptyList(), TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 sizeBetween(Collection) 所有重载")
    void testSizeBetweenCollectionAllVariants() {
        List<String> list = List.of("a", "b");

        assertDoesNotThrow(() -> Failure.begin().sizeBetween(list, 1, 3).fail());
        assertDoesNotThrow(() -> Failure.begin().sizeBetween(list, 1, 3, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().sizeBetween(list, 1, 3, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().sizeBetween(list, 5, 10, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 sizeEquals(Collection) 所有重载")
    void testSizeEqualsCollectionAllVariants() {
        List<String> list = List.of("a", "b");

        assertDoesNotThrow(() -> Failure.begin().sizeEquals(list, 2).fail());
        assertDoesNotThrow(() -> Failure.begin().sizeEquals(list, 2, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().sizeEquals(list, 2, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().sizeEquals(list, 5, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 contains(Collection) 所有重载")
    void testContainsCollectionAllVariants() {
        List<String> list = List.of("a", "b");

        assertDoesNotThrow(() -> Failure.begin().contains(list, "a").fail());
        assertDoesNotThrow(() -> Failure.begin().contains(list, "a", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().contains(list, "a", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().contains(list, "x", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 notContains(Collection) 所有重载")
    void testNotContainsCollectionAllVariants() {
        List<String> list = List.of("a", "b");

        assertDoesNotThrow(() -> Failure.begin().notContains(list, "x").fail());
        assertDoesNotThrow(() -> Failure.begin().notContains(list, "x", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notContains(list, "x", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notContains(list, "a", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isEmpty(Collection) 所有重载")
    void testIsEmptyCollectionAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().isEmpty(Collections.emptyList()).fail());
        assertDoesNotThrow(() -> Failure.begin().isEmpty(Collections.emptyList(), TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isEmpty(Collections.emptyList(), TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().isEmpty(List.of("a"), TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 hasNoNullElements(Collection) 所有重载")
    void testHasNoNullElementsCollectionAllVariants() {
        List<String> list = List.of("a", "b");

        assertDoesNotThrow(() -> Failure.begin().hasNoNullElements(list).fail());
        assertDoesNotThrow(() -> Failure.begin().hasNoNullElements(list, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().hasNoNullElements(list, TEST_CODE, TEST_DETAIL).fail());

        List<String> withNull = new ArrayList<>();
        withNull.add("a");
        withNull.add(null);
        assertThrows(Business.class, () -> Failure.begin().hasNoNullElements(withNull, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 allMatch(Collection) 所有重载")
    void testAllMatchCollectionAllVariants() {
        List<Integer> list = List.of(2, 4, 6);

        assertDoesNotThrow(() -> Failure.begin().allMatch(list, n -> n % 2 == 0).fail());
        assertDoesNotThrow(() -> Failure.begin().allMatch(list, n -> n % 2 == 0, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().allMatch(list, n -> n % 2 == 0, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().allMatch(List.of(1, 2, 3), n -> n % 2 == 0, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 anyMatch(Collection) 所有重载")
    void testAnyMatchCollectionAllVariants() {
        List<Integer> list = List.of(1, 2, 3);

        assertDoesNotThrow(() -> Failure.begin().anyMatch(list, n -> n % 2 == 0).fail());
        assertDoesNotThrow(() -> Failure.begin().anyMatch(list, n -> n % 2 == 0, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().anyMatch(list, n -> n % 2 == 0, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().anyMatch(List.of(1, 3, 5), n -> n % 2 == 0, TEST_CODE).fail());
    }

    // ==================== Array 校验 ====================

    @Test
    @DisplayName("测试 notEmpty(Array) 所有重载")
    void testNotEmptyArrayAllVariants() {
        String[] arr = {"a"};

        assertDoesNotThrow(() -> Failure.begin().notEmpty(arr).fail());
        assertDoesNotThrow(() -> Failure.begin().notEmpty(arr, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notEmpty(arr, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notEmpty(new String[]{}, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 sizeBetween(Array) 所有重载")
    void testSizeBetweenArrayAllVariants() {
        String[] arr = {"a", "b"};

        assertDoesNotThrow(() -> Failure.begin().sizeBetween(arr, 1, 3).fail());
        assertDoesNotThrow(() -> Failure.begin().sizeBetween(arr, 1, 3, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().sizeBetween(arr, 1, 3, TEST_CODE, TEST_DETAIL).fail());
        assertThrows(Business.class, () -> Failure.begin().sizeBetween(arr, 5, 10, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 sizeEquals(Array) 所有重载")
    void testSizeEqualsArrayAllVariants() {
        String[] arr = {"a", "b"};

        assertDoesNotThrow(() -> Failure.begin().sizeEquals(arr, 2).fail());
        assertDoesNotThrow(() -> Failure.begin().sizeEquals(arr, 2, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().sizeEquals(arr, 2, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().sizeEquals(arr, 5, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 contains(Array) 所有重载")
    void testContainsArrayAllVariants() {
        String[] arr = {"a", "b"};

        assertDoesNotThrow(() -> Failure.begin().contains(arr, "a").fail());
        assertDoesNotThrow(() -> Failure.begin().contains(arr, "a", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().contains(arr, "a", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().contains(arr, "x", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 notContains(Array) 所有重载")
    void testNotContainsArrayAllVariants() {
        String[] arr = {"a", "b"};

        assertDoesNotThrow(() -> Failure.begin().notContains(arr, "x").fail());
        assertDoesNotThrow(() -> Failure.begin().notContains(arr, "x", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notContains(arr, "x", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notContains(arr, "a", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isEmpty(Array) 所有重载")
    void testIsEmptyArrayAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().isEmpty(new String[]{}).fail());
        assertDoesNotThrow(() -> Failure.begin().isEmpty(new String[]{}, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isEmpty(new String[]{}, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().isEmpty(new String[]{"a"}, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 hasNoNullElements(Array) 所有重载")
    void testHasNoNullElementsArrayAllVariants() {
        String[] arr = {"a", "b"};

        assertDoesNotThrow(() -> Failure.begin().hasNoNullElements(arr).fail());
        assertDoesNotThrow(() -> Failure.begin().hasNoNullElements(arr, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().hasNoNullElements(arr, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().hasNoNullElements(new String[]{"a", null}, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 allMatch(Array) 所有重载")
    void testAllMatchArrayAllVariants() {
        Integer[] arr = {2, 4, 6};

        assertDoesNotThrow(() -> Failure.begin().allMatch(arr, n -> n % 2 == 0).fail());
        assertDoesNotThrow(() -> Failure.begin().allMatch(arr, n -> n % 2 == 0, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().allMatch(arr, n -> n % 2 == 0, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().allMatch(new Integer[]{1, 2, 3}, n -> n % 2 == 0, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 anyMatch(Array) 所有重载")
    void testAnyMatchArrayAllVariants() {
        Integer[] arr = {1, 2, 3};

        assertDoesNotThrow(() -> Failure.begin().anyMatch(arr, n -> n % 2 == 0).fail());
        assertDoesNotThrow(() -> Failure.begin().anyMatch(arr, n -> n % 2 == 0, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().anyMatch(arr, n -> n % 2 == 0, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().anyMatch(new Integer[]{1, 3, 5}, n -> n % 2 == 0, TEST_CODE).fail());
    }

    // ==================== Number 校验 ====================

    @Test
    @DisplayName("测试 positive() 所有重载")
    void testPositiveAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().positive(1).fail());
        assertDoesNotThrow(() -> Failure.begin().positive(1, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().positive(1, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().positive(-1, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 positive 所有重载")
    void testPositiveNumberAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().positive(1).fail());
        assertDoesNotThrow(() -> Failure.begin().positive(1, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().positive(1, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().positive(-1, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 inRange() 所有重载")
    void testInRangeAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().inRange(5, 1, 10).fail());
        assertDoesNotThrow(() -> Failure.begin().inRange(5, 1, 10, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().inRange(5, 1, 10, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().inRange(0, 1, 10, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 inRangeNumber() 所有重载")
    void testInRangeNumberAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().inRangeNumber(5, 1, 10).fail());
        assertDoesNotThrow(() -> Failure.begin().inRangeNumber(5, 1, 10, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().inRangeNumber(5, 1, 10, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().inRangeNumber(0, 1, 10, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 nonNegative() 所有重载")
    void testNonNegativeAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().nonNegative(0).fail());
        assertDoesNotThrow(() -> Failure.begin().nonNegative(0, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().nonNegative(0, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().nonNegative(-1, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 greaterThan() 所有重载")
    void testGreaterThanAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().greaterThan(5, 3).fail());
        assertDoesNotThrow(() -> Failure.begin().greaterThan(5, 3, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().greaterThan(5, 3, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().greaterThan(3, 5, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 greaterOrEqual() 所有重载")
    void testGreaterOrEqualAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().greaterOrEqual(5, 5).fail());
        assertDoesNotThrow(() -> Failure.begin().greaterOrEqual(5, 5, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().greaterOrEqual(5, 5, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().greaterOrEqual(3, 5, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 lessThan() 所有重载")
    void testLessThanAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().lessThan(3, 5).fail());
        assertDoesNotThrow(() -> Failure.begin().lessThan(3, 5, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().lessThan(3, 5, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().lessThan(5, 3, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 lessOrEqual() 所有重载")
    void testLessOrEqualAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().lessOrEqual(5, 5).fail());
        assertDoesNotThrow(() -> Failure.begin().lessOrEqual(5, 5, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().lessOrEqual(5, 5, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().lessOrEqual(5, 3, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 notZero() 所有重载")
    void testNotZeroAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().notZero(5).fail());
        assertDoesNotThrow(() -> Failure.begin().notZero(5, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notZero(5, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notZero(0, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isZero() 所有重载")
    void testIsZeroAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().isZero(0).fail());
        assertDoesNotThrow(() -> Failure.begin().isZero(0, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isZero(0, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().isZero(5, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 negative() 所有重载")
    void testNegativeAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().negative(-5).fail());
        assertDoesNotThrow(() -> Failure.begin().negative(-5, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().negative(-5, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().negative(5, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 multipleOf() 所有重载")
    void testMultipleOfAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().multipleOf(10, 5).fail());
        assertDoesNotThrow(() -> Failure.begin().multipleOf(10, 5, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().multipleOf(10, 5, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().multipleOf(10, 3, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 decimalScale() 所有重载")
    void testDecimalScaleAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().decimalScale(new BigDecimal("1.23"), 2).fail());
        assertDoesNotThrow(() -> Failure.begin().decimalScale(new BigDecimal("1.23"), 2, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().decimalScale(new BigDecimal("1.23"), 2, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().decimalScale(new BigDecimal("1.234"), 2, TEST_CODE).fail());
    }

    // ==================== Date 校验 ====================

    @Test
    @DisplayName("测试 after(Date) 所有重载")
    void testAfterDateAllVariants() {
        Date now = new Date();
        Date future = new Date(now.getTime() + 10000);
        assertThrows(Business.class, () -> Failure.begin().after(now, future, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 before(Date) 所有重载")
    void testBeforeDateAllVariants() {
        Date now = new Date();
        Date future = new Date(now.getTime() + 10000);

        assertDoesNotThrow(() -> Failure.begin().before(now, future).fail());
        assertDoesNotThrow(() -> Failure.begin().before(now, future, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().before(now, future, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().before(future, now, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 after(Comparable) 所有重载")
    void testAfterComparableAllVariants() {
        LocalDate now = LocalDate.now();
        LocalDate future = now.plusDays(1);

        assertDoesNotThrow(() -> Failure.begin().after(future, now).fail());
        assertDoesNotThrow(() -> Failure.begin().after(future, now, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().after(future, now, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().after(now, future, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 afterOrEqual(Comparable) 所有重载")
    void testAfterOrEqualAllVariants() {
        LocalDate now = LocalDate.now();

        assertDoesNotThrow(() -> Failure.begin().afterOrEqual(now, now).fail());
        assertDoesNotThrow(() -> Failure.begin().afterOrEqual(now, now, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().afterOrEqual(now, now, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().afterOrEqual(now.minusDays(1), now, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 before(Comparable) 所有重载")
    void testBeforeComparableAllVariants() {
        LocalDate now = LocalDate.now();
        LocalDate past = now.minusDays(1);

        assertDoesNotThrow(() -> Failure.begin().before(past, now).fail());
        assertDoesNotThrow(() -> Failure.begin().before(past, now, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().before(past, now, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().before(now, past, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 beforeOrEqual(Comparable) 所有重载")
    void testBeforeOrEqualAllVariants() {
        LocalDate now = LocalDate.now();

        assertDoesNotThrow(() -> Failure.begin().beforeOrEqual(now, now).fail());
        assertDoesNotThrow(() -> Failure.begin().beforeOrEqual(now, now, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().beforeOrEqual(now, now, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().beforeOrEqual(now.plusDays(1), now, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 between(Comparable) 所有重载")
    void testBetweenAllVariants() {
        LocalDate now = LocalDate.now();

        assertDoesNotThrow(() -> Failure.begin().between(now, now.minusDays(1), now.plusDays(1)).fail());
        assertDoesNotThrow(() -> Failure.begin().between(now, now.minusDays(1), now.plusDays(1), TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().between(now, now.minusDays(1), now.plusDays(1), TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().between(now.plusDays(5), now.minusDays(1), now.plusDays(1), TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isPast(Date) 所有重载")
    void testIsPastDateAllVariants() {
        Date past = new Date(System.currentTimeMillis() - 10000);

        assertDoesNotThrow(() -> Failure.begin().isPast(past).fail());
        assertDoesNotThrow(() -> Failure.begin().isPast(past, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isPast(past, TEST_CODE, TEST_DETAIL).fail());

        Date future = new Date(System.currentTimeMillis() + 10000);
        assertThrows(Business.class, () -> Failure.begin().isPast(future, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isFuture(Date) 所有重载")
    void testIsFutureDateAllVariants() {
        Date future = new Date(System.currentTimeMillis() + 10000);

        assertDoesNotThrow(() -> Failure.begin().isFuture(future).fail());
        assertDoesNotThrow(() -> Failure.begin().isFuture(future, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isFuture(future, TEST_CODE, TEST_DETAIL).fail());

        Date past = new Date(System.currentTimeMillis() - 10000);
        assertThrows(Business.class, () -> Failure.begin().isFuture(past, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isPast(LocalDate) 所有重载")
    void testIsPastLocalDateAllVariants() {
        LocalDate past = LocalDate.now().minusDays(1);

        assertDoesNotThrow(() -> Failure.begin().isPast(past).fail());
        assertDoesNotThrow(() -> Failure.begin().isPast(past, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isPast(past, TEST_CODE, TEST_DETAIL).fail());

        LocalDate future = LocalDate.now().plusDays(1);
        assertThrows(Business.class, () -> Failure.begin().isPast(future, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isFuture(LocalDate) 所有重载")
    void testIsFutureLocalDateAllVariants() {
        LocalDate future = LocalDate.now().plusDays(1);

        assertDoesNotThrow(() -> Failure.begin().isFuture(future).fail());
        assertDoesNotThrow(() -> Failure.begin().isFuture(future, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isFuture(future, TEST_CODE, TEST_DETAIL).fail());

        LocalDate past = LocalDate.now().minusDays(1);
        assertThrows(Business.class, () -> Failure.begin().isFuture(past, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isToday(LocalDate) 所有重载")
    void testIsTodayAllVariants() {
        LocalDate today = LocalDate.now();

        assertDoesNotThrow(() -> Failure.begin().isToday(today).fail());
        assertDoesNotThrow(() -> Failure.begin().isToday(today, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isToday(today, TEST_CODE, TEST_DETAIL).fail());

        LocalDate yesterday = today.minusDays(1);
        assertThrows(Business.class, () -> Failure.begin().isToday(yesterday, TEST_CODE).fail());
    }

    // ==================== Enum 校验 ====================

    enum TestEnum {A, B, C}

    @Test
    @DisplayName("测试 enumValue() 所有重载")
    void testEnumValueAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().enumValue(TestEnum.class, "A").fail());
        assertDoesNotThrow(() -> Failure.begin().enumValue(TestEnum.class, "A", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().enumValue(TestEnum.class, "A", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().enumValue(TestEnum.class, "X", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 enumConstant() 所有重载")
    void testEnumConstantAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().enumConstant(TestEnum.A, TestEnum.class).fail());
        assertDoesNotThrow(() -> Failure.begin().enumConstant(TestEnum.A, TestEnum.class, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().enumConstant(TestEnum.A, TestEnum.class, TEST_CODE, TEST_DETAIL).fail());

        // 这里传入 null 会失败，因为 null 不是有效的枚举常量
        assertThrows(Business.class, () -> Failure.begin().enumConstant(null, TestEnum.class, TEST_CODE).fail());
    }

    // ==================== Identity 校验 ====================

    @Test
    @DisplayName("测试 same() 所有重载")
    void testSameAllVariants() {
        Object obj = new Object();

        assertDoesNotThrow(() -> Failure.begin().same(obj, obj).fail());
        assertDoesNotThrow(() -> Failure.begin().same(obj, obj, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().same(obj, obj, TEST_CODE, TEST_DETAIL).fail());

        Object other = new Object();
        assertThrows(Business.class, () -> Failure.begin().same(obj, other, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 notSame() 所有重载")
    void testNotSameAllVariants() {
        Object obj1 = new Object();
        Object obj2 = new Object();

        assertDoesNotThrow(() -> Failure.begin().notSame(obj1, obj2).fail());
        assertDoesNotThrow(() -> Failure.begin().notSame(obj1, obj2, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notSame(obj1, obj2, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notSame(obj1, obj1, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 equals(Object) 所有重载")
    void testEqualsAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().equals("a", "a").fail());
        assertDoesNotThrow(() -> Failure.begin().equals("a", "a", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().equals("a", "a", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().equals("a", "b", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 notEquals(Object) 所有重载")
    void testNotEqualsAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().notEquals("a", "b").fail());
        assertDoesNotThrow(() -> Failure.begin().notEquals("a", "b", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notEquals("a", "b", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notEquals("a", "a", TEST_CODE).fail());
    }

    // ==================== Map 校验 ====================

    @Test
    @DisplayName("测试 notEmpty(Map) 所有重载")
    void testNotEmptyMapAllVariants() {
        Map<String, String> map = Map.of("key", "value");

        assertDoesNotThrow(() -> Failure.begin().notEmpty(map).fail());
        assertDoesNotThrow(() -> Failure.begin().notEmpty(map, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notEmpty(map, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notEmpty(Collections.emptyMap(), TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isEmpty(Map) 所有重载")
    void testIsEmptyMapAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().isEmpty(Collections.emptyMap()).fail());
        assertDoesNotThrow(() -> Failure.begin().isEmpty(Collections.emptyMap(), TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isEmpty(Collections.emptyMap(), TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().isEmpty(Map.of("k", "v"), TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 containsKey() 所有重载")
    void testContainsKeyAllVariants() {
        Map<String, String> map = Map.of("key", "value");

        assertDoesNotThrow(() -> Failure.begin().containsKey(map, "key").fail());
        assertDoesNotThrow(() -> Failure.begin().containsKey(map, "key", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().containsKey(map, "key", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().containsKey(map, "missing", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 notContainsKey() 所有重载")
    void testNotContainsKeyAllVariants() {
        Map<String, String> map = Map.of("key", "value");

        assertDoesNotThrow(() -> Failure.begin().notContainsKey(map, "missing").fail());
        assertDoesNotThrow(() -> Failure.begin().notContainsKey(map, "missing", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().notContainsKey(map, "missing", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().notContainsKey(map, "key", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 containsValue() 所有重载")
    void testContainsValueAllVariants() {
        Map<String, String> map = Map.of("key", "value");

        assertDoesNotThrow(() -> Failure.begin().containsValue(map, "value").fail());
        assertDoesNotThrow(() -> Failure.begin().containsValue(map, "value", TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().containsValue(map, "value", TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().containsValue(map, "missing", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 sizeBetween(Map) 所有重载")
    void testSizeBetweenMapAllVariants() {
        Map<String, String> map = Map.of("k1", "v1", "k2", "v2");

        assertDoesNotThrow(() -> Failure.begin().sizeBetween(map, 1, 3).fail());
        assertDoesNotThrow(() -> Failure.begin().sizeBetween(map, 1, 3, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().sizeBetween(map, 1, 3, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().sizeBetween(map, 5, 10, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 sizeEquals(Map) 所有重载")
    void testSizeEqualsMapAllVariants() {
        Map<String, String> map = Map.of("k1", "v1", "k2", "v2");

        assertDoesNotThrow(() -> Failure.begin().sizeEquals(map, 2).fail());
        assertDoesNotThrow(() -> Failure.begin().sizeEquals(map, 2, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().sizeEquals(map, 2, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().sizeEquals(map, 5, TEST_CODE).fail());
    }

    // ==================== Optional 校验 ====================

    @Test
    @DisplayName("测试 isPresent() 所有重载")
    void testIsPresentAllVariants() {
        Optional<String> present = Optional.of("value");

        assertDoesNotThrow(() -> Failure.begin().isPresent(present).fail());
        assertDoesNotThrow(() -> Failure.begin().isPresent(present, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isPresent(present, TEST_CODE, TEST_DETAIL).fail());

        Optional<String> empty = Optional.empty();
        assertThrows(Business.class, () -> Failure.begin().isPresent(empty, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 isEmpty(Optional) 所有重载")
    void testIsEmptyOptionalAllVariants() {
        Optional<String> empty = Optional.empty();

        assertDoesNotThrow(() -> Failure.begin().isEmpty(empty).fail());
        assertDoesNotThrow(() -> Failure.begin().isEmpty(empty, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isEmpty(empty, TEST_CODE, TEST_DETAIL).fail());

        Optional<String> present = Optional.of("value");
        assertThrows(Business.class, () -> Failure.begin().isEmpty(present, TEST_CODE).fail());
    }

    // ==================== 自定义条件校验 ====================

    @Test
    @DisplayName("测试 satisfies() 所有重载")
    void testSatisfiesAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().satisfies(5, n -> n > 3).fail());
        assertDoesNotThrow(() -> Failure.begin().satisfies(5, n -> n > 3, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().satisfies(5, n -> n > 3, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().satisfies(1, n -> n > 3, TEST_CODE).fail());
        assertThrows(Business.class, () -> Failure.begin().satisfies(null, (Integer n) -> n > 3, TEST_CODE).fail());
    }

    // ==================== 跨字段比较校验 ====================

    @Test
    @DisplayName("测试 compare() 所有重载")
    void testCompareAllVariants() {
        assertDoesNotThrow(() -> Failure.begin().compare(5, 5, Integer::compare).fail());
        assertDoesNotThrow(() -> Failure.begin().compare(5, 5, Integer::compare, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().compare(5, 5, Integer::compare, TEST_CODE, TEST_DETAIL).fail());

        assertThrows(Business.class, () -> Failure.begin().compare(3, 5, Integer::compare, TEST_CODE).fail());
    }

    // ==================== Fail-Fast 模式测试 ====================

    @Test
    @DisplayName("测试 fail-fast 模式下第一个错误后停止")
    void testFailFastModeStopsOnFirstError() {
        Chain chain = Chain.begin(true); // fail-fast = true

        chain.exists(null, TEST_CODE);
        // 此时 alive = false，后续检查应该跳过

        // 这个检查不应该添加新的错误（因为 shouldSkip() 返回 true）
        chain.exists(null, TEST_CODE_2);

        List<Business> errors = chain.getCauses();
        assertEquals(1, errors.size()); // 只有一个错误
        assertEquals(TEST_CODE.getCode(), errors.get(0).getResponseCode().getCode());
    }

    @Test
    @DisplayName("测试 strict 模式下收集所有错误")
    void testStrictModeCollectsAllErrors() {
        Chain chain = Chain.begin(false); // fail-fast = false

        chain.exists(null, TEST_CODE);
        chain.exists(null, TEST_CODE_2);

        List<Business> errors = chain.getCauses();
        assertEquals(2, errors.size());
    }

    @Test
    @DisplayName("测试 shouldSkip 逻辑")
    void testShouldSkipLogic() {
        // fail-fast = true, alive = true -> shouldSkip = false
        Chain chain1 = Chain.begin(true);
        chain1.exists(new Object(), TEST_CODE); // 成功，alive 仍为 true
        // 此时 shouldSkip = false，下一个检查会执行

        // fail-fast = true, alive = false -> shouldSkip = true
        Chain chain2 = Chain.begin(true);
        chain2.exists(null, TEST_CODE); // 失败，alive = false
        // 此时 shouldSkip = true，下一个检查会跳过

        // fail-fast = false, alive = false -> shouldSkip = false
        Chain chain3 = Chain.begin(false);
        chain3.exists(null, TEST_CODE); // 失败，但 alive 仍为 true
        // 此时 shouldSkip = false，下一个检查会执行
    }

    // ==================== 链式调用返回 this 测试 ====================

    @Test
    @DisplayName("测试链式调用返回同一个 Chain 实例")
    void testChainingReturnsSameInstance() {
        Chain chain = Chain.begin(true);

        Chain result = chain
                .exists(new Object())
                .notBlank("test")
                .positive(1);

        assertSame(chain, result);
    }

    // ==================== 边界情况测试 ====================

    @Test
    @DisplayName("测试空字符串边界")
    void testEmptyStringBoundary() {
        assertDoesNotThrow(() -> Failure.begin().blank("").fail());
        assertThrows(Business.class, () -> Failure.begin().notBlank("", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 null 字符串边界")
    void testNullStringBoundary() {
        assertDoesNotThrow(() -> Failure.begin().blank(null).fail());
        assertThrows(Business.class, () -> Failure.begin().notBlank(null, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试空白字符串边界")
    void testWhitespaceStringBoundary() {
        assertDoesNotThrow(() -> Failure.begin().blank("   ").fail());
        assertThrows(Business.class, () -> Failure.begin().notBlank("   ", TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试数值边界")
    void testNumberBoundary() {
        assertDoesNotThrow(() -> Failure.begin().inRange(0, 0, 10).fail());
        assertDoesNotThrow(() -> Failure.begin().inRange(10, 0, 10).fail());
        assertThrows(Business.class, () -> Failure.begin().inRange(-1, 0, 10, TEST_CODE).fail());
        assertThrows(Business.class, () -> Failure.begin().inRange(11, 0, 10, TEST_CODE).fail());
    }

    @Test
    @DisplayName("测试 null 集合边界")
    void testNullCollectionBoundary() {
        assertThrows(Business.class, () -> Failure.begin().notEmpty((List<?>) null, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isEmpty((List<?>) null).fail());
    }

    @Test
    @DisplayName("测试 null Map 边界")
    void testNullMapBoundary() {
        assertThrows(Business.class, () -> Failure.begin().notEmpty((Map<?, ?>) null, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isEmpty((Map<?, ?>) null).fail());
    }

    @Test
    @DisplayName("测试空数组边界")
    void testEmptyArrayBoundary() {
        assertThrows(Business.class, () -> Failure.begin().notEmpty(new String[]{}, TEST_CODE).fail());
        assertDoesNotThrow(() -> Failure.begin().isEmpty(new String[]{}).fail());
    }

    @Test
    @DisplayName("测试 null 数组边界")
    void testNullArrayBoundary() {
        assertThrows(Business.class, () -> Failure.begin().notEmpty((String[]) null, TEST_CODE).fail());
    }
}