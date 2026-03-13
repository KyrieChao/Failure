package com.chao.failfast.result;

import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.model.TestResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Results 补充覆盖测试")
class ResultsCoverageTest {

    private static final ResponseCode CODE = TestResponseCode.PARAM_ERROR;

    @Test
    @DisplayName("tryOf: 异常无消息且无detail时使用错误码描述")
    void tryOf_exceptionNoMessageNoDetail() {
        Result<String> result = Results.tryOf(() -> {
            throw new RuntimeException(); // message is null
        }, CODE);

        assertTrue(result.isFail());
        assertEquals(CODE.getDescription(), result.getError().getDetail());
    }

    @Test
    @DisplayName("tryRun: 异常无消息且无detail时使用错误码描述")
    void tryRun_exceptionNoMessageNoDetail() {
        Result<Void> result = Results.tryRun(() -> {
            throw new RuntimeException(); // message is null
        }, CODE);

        assertTrue(result.isFail());
        assertEquals(CODE.getDescription(), result.getError().getDetail());
    }

    @Test
    @DisplayName("fromOptional: null optional 使用自定义detail")
    void fromOptional_nullWithDetail() {
        Result<String> result = Results.fromOptional(null, CODE, "custom detail");

        assertTrue(result.isFail());
        assertEquals("custom detail", result.getError().getDetail());
    }

    @Test
    @DisplayName("whenOrFail: 异常且无detail时使用异常消息")
    void whenOrFail_exceptionNoDetail() {
        Result<String> result = Results.whenOrFail(true, () -> {
            throw new RuntimeException();
        }, CODE);

        assertTrue(result.isFail());
        assertEquals("参数错误", result.getError().getDetail());
    }
}
