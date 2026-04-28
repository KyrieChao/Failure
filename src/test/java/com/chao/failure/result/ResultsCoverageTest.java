package com.chao.failure.result;

import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.model.TestResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
@DisplayName("display")
    void whenOrFail_exceptionNoDetail() {
        Result<String> result = Results.whenOrFail(true, () -> {
            throw new RuntimeException();
        }, CODE);

        assertTrue(result.isFail());
        assertEquals("参数错误", result.getError().getDetail());
    }

    @Test
    @DisplayName("tryOf: 无detail且错误码无description时使用错误码message")
    void tryOf_noDetailNoDescriptionUsesMessage() {
        ResponseCode code = ResponseCode.of(400, "only-message");
        Result<String> result = Results.tryOf(() -> {
            throw new RuntimeException("boom");
        }, code);

        assertTrue(result.isFail());
        assertEquals("only-message", result.getError().getDetail());
    }
}
