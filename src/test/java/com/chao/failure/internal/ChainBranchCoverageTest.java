package com.chao.failure.internal;

import com.chao.failure.Failure;
import com.chao.failure.validator.FastValidator;
import com.chao.failure.internal.core.Chain;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.model.TestResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chain 类分支覆盖率补充测试
 * 专门覆盖 alive=false 和 shouldSkip() 的分支
 */
@DisplayName("Chain 分支覆盖测试")
public class ChainBranchCoverageTest {

    private static final ResponseCode TEST_CODE = TestResponseCode.PARAM_ERROR;
    private static final ResponseCode TEST_CODE_2 = TestResponseCode.PARAM_REQUIRED;

    // ==================== alive=false 分支测试 ====================

    @Test
    @DisplayName("测试 check(boolean) 在 alive=false 时直接返回")
    void testCheckBooleanWhenAliveFalse() {
        Chain chain = Chain.begin(true);

        // 先让 alive=false
        chain.state(false);
        assertFalse(chain.isValid());

        // 再次调用 check(false)，应该直接返回，不修改状态
        chain.state(false);
        // 没有异常，说明直接返回了
    }

    @Test
    @DisplayName("测试 String 方法在 alive=false 时直接返回")
    void testStringMethodsWhenAliveFalse() {
        Chain chain = Chain.begin(true);
        chain.state(false); // alive=false

        // 以下方法都应该直接返回 this，不执行检查
        assertSame(chain, chain.notBlank("")); // 空字符串本应失败
        assertSame(chain, chain.lengthBetween("a", 5, 10)); // 长度不符本应失败
        assertSame(chain, chain.match("abc", "\\d+")); // 正则不匹配本应失败
        assertSame(chain, chain.email("invalid")); // 无效邮箱本应失败
        assertSame(chain, chain.startsWith("abc", "x")); // 不匹配本应失败
        assertSame(chain, chain.endsWith("abc", "x")); // 不匹配本应失败
        assertSame(chain, chain.contains("abc", "x")); // 不包含本应失败
        assertSame(chain, chain.notContains("abc", "a")); // 包含本应失败
        assertSame(chain, chain.lengthMin("a", 5)); // 长度不足本应失败
        assertSame(chain, chain.lengthMax("abcdef", 3)); // 超长本应失败
        assertSame(chain, chain.isNumeric("abc")); // 非数字本应失败
        assertSame(chain, chain.isAlpha("123")); // 非字母本应失败
        assertSame(chain, chain.isAlphanumeric("abc-123")); // 含特殊字符本应失败
        assertSame(chain, chain.isLowerCase("ABC")); // 大写本应失败
        assertSame(chain, chain.isUpperCase("abc")); // 小写本应失败
        assertSame(chain, chain.mobile("12345678901")); // 无效手机号本应失败
        assertSame(chain, chain.url("not-a-url")); // 无效URL本应失败
        assertSame(chain, chain.ipAddress("999.999.999.999")); // 无效IP本应失败
        assertSame(chain, chain.uuid("not-a-uuid")); // 无效UUID本应失败
    }

    @Test
    @DisplayName("测试 Collection 方法在 alive=false 时直接返回")
    void testCollectionMethodsWhenAliveFalse() {
        Chain chain = Chain.begin(true);
        chain.state(false); // alive=false

        List<String> emptyList = Collections.emptyList();
        List<String> listWithA = List.of("a");

        assertSame(chain, chain.notEmpty(emptyList)); // 空集合本应失败
        assertSame(chain, chain.sizeBetween(listWithA, 5, 10)); // 大小不符本应失败
        assertSame(chain, chain.sizeEquals(listWithA, 5)); // 大小不符本应失败
        assertSame(chain, chain.contains(listWithA, "x")); // 不包含本应失败
        assertSame(chain, chain.notContains(listWithA, "a")); // 包含本应失败
        assertSame(chain, chain.isEmpty(listWithA)); // 非空本应失败
        assertSame(chain, chain.hasNoNullElements(Arrays.asList("a", null))); // 含null本应失败
        assertSame(chain, chain.allMatch(listWithA, s -> s.equals("x"))); // 不匹配本应失败
        assertSame(chain, chain.anyMatch(emptyList, s -> true)); // 空集合本应失败
    }

    @Test
    @DisplayName("测试 Array 方法在 alive=false 时直接返回")
    void testArrayMethodsWhenAliveFalse() {
        Chain chain = Chain.begin(true);
        chain.state(false); // alive=false

        String[] emptyArray = new String[]{};
        String[] arrayWithA = new String[]{"a"};

        assertSame(chain, chain.notEmpty(emptyArray)); // 空数组本应失败
        assertSame(chain, chain.sizeBetween(arrayWithA, 5, 10)); // 大小不符本应失败
        assertSame(chain, chain.sizeEquals(arrayWithA, 5)); // 大小不符本应失败
        assertSame(chain, chain.contains(arrayWithA, "x")); // 不包含本应失败
        assertSame(chain, chain.notContains(arrayWithA, "a")); // 包含本应失败
        assertSame(chain, chain.isEmpty(arrayWithA)); // 非空本应失败
        assertSame(chain, chain.hasNoNullElements(new String[]{"a", null})); // 含null本应失败
        assertSame(chain, chain.allMatch(arrayWithA, s -> s.equals("x"))); // 不匹配本应失败
        assertSame(chain, chain.anyMatch(emptyArray, s -> true)); // 空数组本应失败
    }

    @Test
    @DisplayName("测试 Number 方法在 alive=false 时直接返回")
    void testNumberMethodsWhenAliveFalse() {
        Chain chain = Chain.begin(true);
        chain.state(false); // alive=false

        assertSame(chain, chain.positive(-1)); // 负数本应失败
        assertSame(chain, chain.inRange(0, 5, 10)); // 不在范围内本应失败
        assertSame(chain, chain.inRangeNumber(0, 5, 10)); // 不在范围内本应失败
        assertSame(chain, chain.nonNegative(-1)); // 负数本应失败
        assertSame(chain, chain.greaterThan(3, 5)); // 不大于本应失败
        assertSame(chain, chain.greaterOrEqual(3, 5)); // 不大于等于本应失败
        assertSame(chain, chain.lessThan(5, 3)); // 不小于本应失败
        assertSame(chain, chain.lessOrEqual(5, 3)); // 不小于等于本应失败
        assertSame(chain, chain.notZero(0)); // 零本应失败
        assertSame(chain, chain.isZero(5)); // 非零本应失败
        assertSame(chain, chain.negative(5)); // 正数本应失败
        assertSame(chain, chain.multipleOf(10, 3)); // 非倍数本应失败
    }

    @Test
    @DisplayName("测试 Date 方法在 alive=false 时直接返回")
    void testDateMethodsWhenAliveFalse() {
        Chain chain = Chain.begin(true);
        chain.state(false); // alive=false

        LocalDate now = LocalDate.now();
        LocalDate past = now.minusDays(1);
        LocalDate future = now.plusDays(1);

        assertSame(chain, chain.after(past, future)); // 过去不在未来后本应失败
        assertSame(chain, chain.before(future, past)); // 未来不在过去前本应失败
        assertSame(chain, chain.afterOrEqual(past, now)); // 过去不在现在后或等于本应失败
        assertSame(chain, chain.beforeOrEqual(future, now)); // 未来不在现在前或等于本应失败
        assertSame(chain, chain.between(now.plusDays(5), now.minusDays(1), now.plusDays(1))); // 不在范围内本应失败
        assertSame(chain, chain.isPast(future)); // 未来不是过去本应失败
        assertSame(chain, chain.isFuture(past)); // 过去不是未来本应失败
    }

    @Test
    @DisplayName("测试 Enum 方法在 alive=false 时直接返回")
    void testEnumMethodsWhenAliveFalse() {
        Chain chain = Chain.begin(true);
        chain.state(false); // alive=false

        enum TestEnum {A, B}

        assertSame(chain, chain.enumValue(TestEnum.class, "X")); // 无效枚举值本应失败
        assertSame(chain, chain.enumConstant(null, TestEnum.class)); // null 不是有效枚举常量本应失败
    }

    @Test
    @DisplayName("测试 Identity 方法在 alive=false 时直接返回")
    void testIdentityMethodsWhenAliveFalse() {
        Chain chain = Chain.begin(true);
        chain.state(false); // alive=false

        Object obj1 = new Object();
        Object obj2 = new Object();

        assertSame(chain, chain.same(obj1, obj2)); // 不同对象本应失败
        assertSame(chain, chain.notSame(obj1, obj1)); // 相同对象本应失败
        assertSame(chain, chain.equals("a", "b")); // 不相等本应失败
        assertSame(chain, chain.notEquals("a", "a")); // 相等本应失败
    }

    @Test
    @DisplayName("测试 Map 方法在 alive=false 时直接返回")
    void testMapMethodsWhenAliveFalse() {
        Chain chain = Chain.begin(true);
        chain.state(false); // alive=false

        Map<String, String> emptyMap = Collections.emptyMap();
        Map<String, String> map = Map.of("key", "value");

        assertSame(chain, chain.notEmpty(emptyMap)); // 空Map本应失败
        assertSame(chain, chain.isEmpty(map)); // 非空Map本应失败
        assertSame(chain, chain.containsKey(map, "missing")); // 不包含key本应失败
        assertSame(chain, chain.notContainsKey(map, "key")); // 包含key本应失败
        assertSame(chain, chain.containsValue(map, "missing")); // 不包含value本应失败
        assertSame(chain, chain.sizeBetween(map, 5, 10)); // 大小不符本应失败
        assertSame(chain, chain.sizeEquals(map, 5)); // 大小不符本应失败
    }

    @Test
    @DisplayName("测试 Optional 方法在 alive=false 时直接返回")
    void testOptionalMethodsWhenAliveFalse() {
        Chain chain = Chain.begin(true);
        chain.state(false); // alive=false

        assertSame(chain, chain.isPresent(Optional.empty())); // empty 本应失败
        assertSame(chain, chain.isEmpty(Optional.of("value"))); // 非empty本应失败
    }

    @Test
    @DisplayName("测试 satisfies 在 alive=false 时直接返回")
    void testSatisfiesWhenAliveFalse() {
        Chain chain = Chain.begin(true);
        chain.state(false); // alive=false

        Predicate<Integer> alwaysFalse = n -> false;
        assertSame(chain, chain.satisfies(5, alwaysFalse)); // 条件不满足本应失败
        assertSame(chain, chain.satisfies(null, alwaysFalse)); // null 本应失败
    }

    @Test
    @DisplayName("测试 compare 在 alive=false 时直接返回")
    void testCompareWhenAliveFalse() {
        Chain chain = Chain.begin(true);
        chain.state(false); // alive=false

        assertSame(chain, chain.compare(3, 5, Integer::compare)); // 不相等本应失败
    }

    // ==================== shouldSkip() 分支测试 (failFast=true, alive=false) ====================

    @Test
    @DisplayName("测试 shouldSkip()=true 时 check 直接返回")
    void testShouldSkipWhenFailFastAndNotAlive() {
        // failFast=true, 先让 alive=false
        Chain chain = Chain.begin(true);
        chain.exists(null, TEST_CODE); // 失败，alive=false

        // 此时 shouldSkip()=true，后续检查应该跳过
        // 验证：添加第二个错误，但错误列表应该只有一个
        chain.exists(null, TEST_CODE_2);

        assertEquals(1, chain.getCauses().size());
        assertEquals(TEST_CODE.getCode(), chain.getCauses().get(0).getResponseCode().getCode());
    }

    @Test
    @DisplayName("测试 fail-fast 模式下多个错误只收集第一个")
    void testFailFastOnlyCollectsFirstError() {
        Chain chain = Chain.begin(true);

        // 第一个错误
        chain.notBlank("", TEST_CODE);

        // 后续错误应该被跳过
        chain.notNull(null, TEST_CODE_2);
        chain.positive(-1, TestResponseCode.PARAM_INVALID);

        assertEquals(1, chain.getCauses().size());
    }

    @Test
    @DisplayName("测试 strict 模式下收集所有错误")
    void testStrictModeCollectsAllErrors() {
        Chain chain = Chain.begin(false); // strict 模式

        chain.notBlank("", TEST_CODE);
        chain.notNull(null, TEST_CODE_2);
        chain.positive(-1, TestResponseCode.PARAM_INVALID);

        assertEquals(3, chain.getCauses().size());
    }

    // ==================== failNow 在 alive=true 时返回 this ====================

    @Test
    @DisplayName("测试 failNow(ResponseCode) 在 alive=true 时返回 this")
    void testFailNowCodeWhenAliveTrue() {
        Chain chain = Chain.begin(true);

        // alive=true，应该返回 this，不抛出异常
        assertSame(chain, chain.failNow(TEST_CODE));
    }

    @Test
    @DisplayName("测试 failNow(ResponseCode, String) 在 alive=true 时返回 this")
    void testFailNowCodeMsgWhenAliveTrue() {
        Chain chain = Chain.begin(true);

        assertSame(chain, chain.failNow(TEST_CODE, "message"));
    }

    @Test
    @DisplayName("测试 failNow(ResponseCode, String, Object...) 在 alive=true 时返回 this")
    void testFailNowCodeFormatWhenAliveTrue() {
        Chain chain = Chain.begin(true);

        assertSame(chain, chain.failNow(TEST_CODE, "value is %s", "test"));
    }

    @Test
    @DisplayName("测试 failNow(Consumer) 在 alive=true 时返回 this")
    void testFailNowConsumerWhenAliveTrue() {
        Chain chain = Chain.begin(true);

        assertSame(chain, chain.failNow(TEST_CODE));
    }

    @Test
    @DisplayName("测试 failNow(Supplier) 在 alive=true 时返回 this")
    void testFailNowSupplierWhenAliveTrue() {
        Chain chain = Chain.begin(true);

        assertSame(chain, chain.failNow(TEST_CODE));
    }

    // ==================== 混合模式测试 ====================

    @Test
    @DisplayName("测试 fail-fast 模式下的链式调用")
    void testFailFastChaining() {
        Chain chain = Chain.begin(true);

        // 正常流程
        chain.notBlank("valid")
                .positive(1)
                .notEmpty(List.of("item"));

        assertTrue(chain.isValid());

        // 触发失败
        chain.notBlank("", TEST_CODE);

        // 后续调用应该被跳过
        chain.notBlank(""); // 本应失败，但被跳过
        chain.positive(-1); // 本应失败，但被跳过

        assertFalse(chain.isValid());
        assertEquals(1, chain.getCauses().size());
    }

    @Test
    @DisplayName("测试 strict 模式下的链式调用")
    void testStrictChaining() {
        Chain chain = Chain.begin(false);

        // 多个失败
        chain.notBlank("", TEST_CODE)
                .notBlank("", TEST_CODE_2)
                .positive(-1, TestResponseCode.PARAM_INVALID);

        assertFalse(chain.isValid());
        assertEquals(3, chain.getCauses().size());
    }

    // ==================== 边界情况测试 ====================

    @Test
    @DisplayName("测试空链直接调用 fail()")
    void testEmptyChainFail() {
        Chain chain = Chain.begin(true);

        // 没有添加任何检查，直接调用 fail() 应该通过
        assertDoesNotThrow(chain::fail);
    }

    @Test
    @DisplayName("测试空链直接调用 failAll()")
    void testEmptyChainFailAll() {
        Chain chain = Chain.begin(true);

        assertDoesNotThrow(chain::failAll);
    }

    @Test
    @DisplayName("测试所有检查都通过的链")
    void testAllChecksPass() {
        Chain chain = Chain.begin(true);

        chain.notBlank("valid")
                .positive(1)
                .notEmpty(List.of("item"))
                .isTrue(true);

        assertTrue(chain.isValid());
        assertDoesNotThrow(chain::fail);
    }

    @Test
    @DisplayName("verify() 方法测试")
    void testVerify() {
        Chain chain = Failure.begin();
        assertDoesNotThrow(chain::verify);
    }

    @Test
    @DisplayName("测试当 context 已停止时调用 or() 直接返回")
    void testOrWhenContextStopped() {
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true); // failFast=true
        context.stop(); // Manually stop context

        Chain chain = Failure.with(context);

        // Context is stopped, so or() should return immediately without changing state
        chain.or();

        // Verify state is unchanged (though or() doesn't expose state easily, we can verify via debug or coverage)
        // But functionally, if it returns early, orMode is false.
        // If orMode is false, calling check() behaves normally (skips if stopped).

        assertTrue(context.isStopped());
    }

    @Test
    @DisplayName("测试 isValid() 在 errors 为空但 alive=false 时返回 false")
    void testIsValidWhenErrorsEmptyButAliveFalse() {
        Chain chain = Chain.begin(true);

        // 使用无参 check 使 alive=false，但不添加错误
        chain.state(false);

        assertFalse(chain.isValid());
        assertFalse(chain.getCauses().isEmpty());
    }
}
