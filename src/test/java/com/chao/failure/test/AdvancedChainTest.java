package com.chao.failure.test;

import com.chao.failure.Failure;
import com.chao.failure.validator.FastValidator;
import com.chao.failure.config.mapping.CodeMappingConfig;
import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.exception.Business;
import com.chao.failure.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 进阶链式 API 验证示例
 * 包含枚举、对象相等性以及自定义错误构建器的高级用法
 */
class AdvancedChainTest {

    enum UserRole {
        ADMIN, USER, GUEST
    }

    @Test
    @DisplayName("枚举值验证")
    void testEnumValidation() {
        // 验证字符串是否是有效的枚举值
        assertDoesNotThrow(() ->
                Failure.begin().enumValue(UserRole.class, "ADMIN")
                        .failNow(ResponseCode.of(7001, "无效的角色类型"))
        );

        assertThrows(Business.class, () ->
                Failure.begin().enumValue(UserRole.class, "SUPER_ADMIN", ResponseCode.of(7001, "无效的角色类型"))
                        .fail()
        );
    }

    @Test
    @DisplayName("对象相等性验证 (equals / same)")
    void testEqualityValidation() {
        String s1 = new String("hello");
        String s2 = new String("hello");
        String s3 = "world";

        // equals: 内容相等
        assertDoesNotThrow(() ->
                Failure.begin().equals(s1, s2)
                        .failNow(ResponseCode.of(8001, "内容不相等"))
        );

        // notEquals: 内容不等
        assertDoesNotThrow(() ->
                Failure.begin().notEquals(s1, s3)
                        .failNow(ResponseCode.of(8001, "内容不相等"))
        );

        // same: 引用相同 (==)
        assertThrows(Business.class, () ->
                Failure.begin().same(s1, s2)
                        .failNow(ResponseCode.of(8001, "引用不相同"))
        );

        // notSame: 引用不同 (!=)
        assertDoesNotThrow(() ->
                Failure.begin().notSame(s1, s2)
                        .failNow(ResponseCode.of(8001, "引用不同"))
        );
    }

    @Test
    @DisplayName("自定义错误构建器 (Fabricator)")
    void testCustomErrorBuilder() {
        String input = null;

        Business ex = assertThrows(Business.class, () ->
                Failure.begin()
                        .notNull(input,
                                ResponseCode.of(9001, "动态错误"),
                                ("这里可以放详细的上下文信息，比如输入值为null"))
                        .failAll()
        );

        assertEquals(9001, ex.getResponseCode().getCode());
    }

    @Test
    @DisplayName("ValidationContext: addErrorAndHalt 演示")
    void testAddErrorAndHalt() {
        // 手动创建一个 Context 演示 addErrorAndHalt 的效果
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(false); // 即使不是 fast 模式

        context.reportError(ResponseCode.of(9002, "致命错误"));
        context.stop();

        assertTrue(context.isStopped());
        assertEquals(1, context.hasCauses().size());
        assertEquals(9002, context.hasCauses().get(0).getResponseCode().getCode());
    }

    @Test
    @DisplayName("CodeMappingConfig: getGroupCodesExpanded 演示")
    void testCodeMappingConfig() {
        FailureProperties props = new FailureProperties();
        FailureProperties.CodeMapping mapping = new FailureProperties.CodeMapping();

        // 模拟配置: groupA -> [1000-1010]
        Map<String, List<Object>> groups = new java.util.HashMap<>();
        groups.put("groupA", Collections.singletonList("1010-1000"));
        mapping.setGroups(groups);
        props.setCodeMapping(mapping);

        CodeMappingConfig config = new CodeMappingConfig(props);

        // 验证 getGroupCodesExpanded
        String expanded = config.getGroupCodesExpanded("groupA", 5);
        String expanded2 = config.getGroupCodesExpanded("groupA", 0);
        String expanded3 = config.getGroupCodesExpanded(null);
        System.out.println("Expanded codes: " + expanded);

        // 应该包含省略号，因为 1000-1010 有 11 个数，大于阈值 5
        assertTrue(expanded.contains("..."));
        assertTrue(expanded.startsWith("[1000,"));
        assertTrue(expanded.endsWith("1010]"));
        assertEquals("[]", expanded2);
        assertEquals("[]", expanded3);
    }

    @Test
    @DisplayName("Result.ofNullable 演示")
    void testResultOfNullable() {
        ResponseCode ERR_MISSING = ResponseCode.of(9003, "值缺失");

        // Case 1: Value is present
        Result<String> r1 = Result.ofNullable("Hello", ERR_MISSING);
        assertTrue(r1.isSuccess());
        assertEquals("Hello", r1.get());

        // Case 2: Value is null
        Result<String> r2 = Result.ofNullable(null, ERR_MISSING);
        assertTrue(r2.isFail());
        assertEquals(9003, r2.getError().getResponseCode().getCode());

        // Case 3: Value is null with detail
        Result<String> r3 = Result.ofNullable(null, ERR_MISSING, "详细描述");
        assertTrue(r3.isFail());
        assertEquals("详细描述", r3.getError().getDetail());
    }
}
