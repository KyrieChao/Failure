package com.chao.failfast.test;

import com.chao.failfast.Failure;
import com.chao.failfast.exception.Business;
import com.chao.failfast.exception.MultiBusiness;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 链式 API 使用示例 (Programmatic Validation)
 * 展示如何使用 Failure 类提供的流畅 API 进行参数校验
 */
class ChainApiTest {

    // 预定义一些错误码，模拟 ResponseCode
    private static final ResponseCode ERR_NULL = ResponseCode.of(1001, "参数不能为空");
    private static final ResponseCode ERR_FORMAT = ResponseCode.of(1002, "格式错误");
    private static final ResponseCode ERR_RANGE = ResponseCode.of(1003, "范围错误");

    @Test
    @DisplayName("快速失败模式 (Fail-Fast) - 遇到第一个错误立即抛出异常")
    void testFailFastMode() {
        String username = null;
        String email = "invalid-email";

        // Failure.strict() 开启收集模式，Fail-Fast 模式通常通过直接调用校验方法开始（默认为 Fast 模式）
        // 或者使用 begin() (但它是私有的)，所以我们通常直接开始校验
        // 这里演示一个标准的链式调用，如果 username 为空，notNull 会记录错误
        // 在 Fast 模式下，后续的 email 校验应该被跳过

        Business ex = assertThrows(Business.class, () -> {
            Failure.begin().notNull(username, ERR_NULL) // 这是一个开启了 Fast 模式的链
                    .email(email, ERR_FORMAT)    // 这行代码不会被执行
                    .failAll();                  // 抛出记录的错误
        });


        assertEquals(ERR_NULL.getCode(), ex.getResponseCode().getCode());
        System.out.println("Fail-Fast 演示成功: " + ex.getMessage());
    }


    @Test
    @DisplayName("严格模式 (Strict/Collect) - 收集所有错误一次性抛出")
    void testStrictMode() {
        String username = null;
        String email = "invalid-email";
        int age = 150;

        // Failure.strict() 开启收集模式
        MultiBusiness ex = assertThrows(MultiBusiness.class, () -> {
            Failure.strict()
                    .notNull(username, ERR_NULL)      // 记录错误1
                    .email(email, ERR_FORMAT)         // 记录错误2
                    .inRange(age, 0, 100, ERR_RANGE)  // 记录错误3
                    .failAll();                       // 统一抛出
        });

        assertEquals(3, ex.getErrors().size());
        assertEquals(ERR_NULL.getCode(), ex.getErrors().get(0).getResponseCode().getCode());
        assertEquals(ERR_FORMAT.getCode(), ex.getErrors().get(1).getResponseCode().getCode());
        assertEquals(ERR_RANGE.getCode(), ex.getErrors().get(2).getResponseCode().getCode());

        System.out.println("Strict 模式演示成功，收集到 " + ex.getErrors().size() + " 个错误");
    }

    @Test
    @DisplayName("字符串验证示例")
    void testStringValidation() {
        String input = "  ";

        assertThrows(Business.class, () ->
                Failure.begin().notBlank(input, ResponseCode.of(2001, "字符串不能为空白"))
                        .failAll()
        );

        String password = "123";
        assertThrows(Business.class, () ->
                Failure.begin().lengthBetween(password, 6, 20, ResponseCode.of(2002, "密码长度需在6-20之间"))
                        .failAll()
        );

        String phone = "1234567890"; // 10位
        assertThrows(Business.class, () ->
                Failure.begin().match(phone, "^1[3-9]\\d{9}$", ResponseCode.of(2003, "手机号格式错误"))
                        .failAll()
        );
    }

    @Test
    @DisplayName("数值验证示例")
    void testNumberValidation() {
        int count = -5;
        assertThrows(Business.class, () ->
                Failure.begin().positive(count, ResponseCode.of(3001, "数量必须大于0"))
                        .failAll()
        );

        double price = 100.0;
        assertThrows(Business.class, () ->
                Failure.begin().inRangeNumber(price, 0.0, 50.0, ResponseCode.of(3002, "价格不能超过50"))
                        .failAll()
        );

        BigDecimal money = new BigDecimal("100.50");
        assertThrows(Business.class, () ->
                Failure.begin().equals(money, new BigDecimal("100.00"), ResponseCode.of(3003, "金额不匹配"))
                        .failAll()
        );
    }

    @Test
    @DisplayName("集合验证示例")
    void testCollectionValidation() {
        List<String> items = Collections.emptyList();
        assertThrows(Business.class, () ->
                Failure.begin().notEmpty(items, ResponseCode.of(4001, "列表不能为空"))
                        .failAll()
        );

        Map<String, Object> map = null;
        assertThrows(Business.class, () ->
                Failure.begin().notNull(map, ResponseCode.of(4002, "Map不能为null"))
                        .failAll()
        );

        List<Integer> numbers = Arrays.asList(1, 2, 3);
        assertThrows(Business.class, () ->
                Failure.begin().sizeEquals(numbers, 5, ResponseCode.of(4003, "列表长度必须为5"))
                        .failAll()
        );
    }

    @Test
    @DisplayName("日期验证示例")
    void testDateValidation() {
        // 使用 Date 类型演示 before/after
        Date now = new Date();
        Date oldDate = new Date(now.getTime() - 10000); // 10秒前

        // 验证必须是将来时间 (即 oldDate 应该 after now) -> 失败
        assertThrows(Business.class, () ->
                Failure.begin().after(oldDate, now, ResponseCode.of(5001, "必须是将来时间"))
                        .failAll()
        );

        Date futureDate = new Date(now.getTime() + 10000); // 10秒后
        // 验证必须是过去时间 (即 futureDate 应该 before now) -> 失败
        assertThrows(Business.class, () ->
                Failure.begin().before(futureDate, now, ResponseCode.of(5002, "必须是过去时间"))
                        .failAll()
        );
    }

    @Test
    @DisplayName("布尔与对象验证示例")
    void testBooleanAndObject() {
        boolean isAdmin = false;
        assertThrows(Business.class, () ->
                Failure.begin().isTrue(isAdmin, ResponseCode.of(6001, "必须是管理员"))
                        .failAll()
        );

        Object user = null;
        assertThrows(Business.class, () ->
                Failure.begin().notNull(user, ResponseCode.of(6002, "用户对象不能为空"))
                        .failAll()
        );
    }

    @Test
    @DisplayName("测试 TerminatingChain 方法: failNow, onFail, onFailGet")
    void testTerminatingMethods() {
        // failNow(Code): 如果链已失败，抛出指定异常
        // 我们先构造一个失败的链
        assertThrows(Business.class, () ->
                Failure.begin().isTrue(false) // 失败，默认错误码
                        .failNow(ResponseCode.of(9999, "覆盖的错误")) // 抛出这个新错误
        );

        // onFail(Runnable): 失败时执行回调
        final boolean[] executed = {false};
        Failure.begin().isTrue(false)
                .onFail(() -> executed[0] = true);
        assertTrue(executed[0]);

        // onFailGet(Supplier): 失败时获取备用值
        Optional<String> result = Failure.begin().isTrue(false)
                .onFailGet(() -> "Backup Value");
        assertTrue(result.isPresent());
        assertEquals("Backup Value", result.get());

        // 成功情况
        Optional<String> successResult = Failure.begin().isTrue(true)
                .onFailGet(() -> "Backup Value");
        assertFalse(successResult.isPresent());
    }
}
