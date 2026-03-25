package com.chao.failfast.result;

import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.core.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ResultTest {

    @Test
    void testOk() {
        String value = "test";
        Result<String> result = Result.ok(value);
        assertTrue(result.isSuccess());
        assertFalse(result.isFail());
        assertEquals(value, result.get());
        assertEquals(value, result.getOrNull());
    }

    @Test
    void testFailWithResponseCode() {
        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        Result<String> result = Result.fail(code);
        assertFalse(result.isSuccess());
        assertTrue(result.isFail());
        assertEquals(code.getCode(), result.getCode());
    }

    @Test
    void testFailWithResponseCodeAndDetail() {
        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        String detail = "Invalid parameter";
        Result<String> result = Result.fail(code, detail);
        assertFalse(result.isSuccess());
        assertTrue(result.isFail());
        assertEquals(code.getCode(), result.getCode());
    }

    @Test
    void testFailWithBusiness() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Invalid parameter");
        Result<String> result = Result.fail(business);
        assertFalse(result.isSuccess());
        assertTrue(result.isFail());
        assertEquals(business.getResponseCode().getCode(), result.getCode());
    }

    @Test
    void testOfNullableWithNonNullValue() {
        String value = "test";
        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        Result<String> result = Result.ofNullable(value, code);
        assertTrue(result.isSuccess());
        assertEquals(value, result.get());
    }

    @Test
    void testOfNullableWithNullValue() {
        String value = null;
        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        Result<String> result = Result.ofNullable(value, code);
        assertFalse(result.isSuccess());
        assertTrue(result.isFail());
    }

    @Test
    void testOfNullableWithNonNullValueAndDetail() {
        String value = "test";
        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        String detail = "Invalid parameter";
        Result<String> result = Result.ofNullable(value, code, detail);
        assertTrue(result.isSuccess());
        assertEquals(value, result.get());
    }

    @Test
    void testOfNullableWithNullValueAndDetail() {
        String value = null;
        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        String detail = "Invalid parameter";
        Result<String> result = Result.ofNullable(value, code, detail);
        assertFalse(result.isSuccess());
        assertTrue(result.isFail());
    }

    @Test
    void testGetOnSuccess() {
        String value = "test";
        Result<String> result = Result.ok(value);
        assertEquals(value, result.get());
    }

    @Test
    void testGetOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        assertThrows(IllegalStateException.class, result::get);
    }

    @Test
    void testGetOrNullOnSuccess() {
        String value = "test";
        Result<String> result = Result.ok(value);
        assertEquals(value, result.getOrNull());
    }

    @Test
    void testGetOrNullOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        assertNull(result.getOrNull());
    }

    @Test
    void testGetErrorOnFailure() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Invalid parameter");
        Result<String> result = Result.fail(business);
        assertEquals(business, result.getError());
    }

    @Test
    void testGetErrorOnFailureWithResponseCode() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        assertNotNull(result.getError());
        assertEquals(ResponseCode.VALIDATION_ERROR_400.getCode(), result.getError().getResponseCode().getCode());
    }

    @Test
    void testGetErrorOnSuccess() {
        Result<String> result = Result.ok("test");
        assertThrows(IllegalStateException.class, result::getError);
    }

    @Test
    void testMapOnSuccess() {
        Result<Integer> result = Result.ok(5);
        Result<String> mapped = result.map(i -> String.valueOf(i));
        assertTrue(mapped.isSuccess());
        assertEquals("5", mapped.get());
    }

    @Test
    void testMapOnFailure() {
        Result<Integer> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> mapped = result.map(i -> String.valueOf(i));
        assertFalse(mapped.isSuccess());
        assertTrue(mapped.isFail());
    }

    @Test
    void testMapWithBusinessException() {
        Result<Integer> result = Result.ok(5);
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");
        Result<String> mapped = result.map(i -> {
            throw business;
        });
        assertFalse(mapped.isSuccess());
        assertTrue(mapped.isFail());
    }

    @Test
    void testMapWithRuntimeException() {
        Result<Integer> result = Result.ok(5);
        assertThrows(RuntimeException.class, () -> result.map(i -> {
            throw new RuntimeException("Runtime error");
        }));
    }

    @Test
    void testFlatMapOnSuccess() {
        Result<Integer> result = Result.ok(5);
        Result<String> flatMapped = result.flatMap(i -> Result.ok(String.valueOf(i)));
        assertTrue(flatMapped.isSuccess());
        assertEquals("5", flatMapped.get());
    }

    @Test
    void testFlatMapOnFailure() {
        Result<Integer> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> flatMapped = result.flatMap(i -> Result.ok(String.valueOf(i)));
        assertFalse(flatMapped.isSuccess());
        assertTrue(flatMapped.isFail());
    }

    @Test
    void testPeekOnSuccess() {
        StringBuilder sb = new StringBuilder();
        Result<String> result = Result.ok("test");
        Result<String> peeked = result.peek(sb::append);
        assertEquals("test", sb.toString());
        assertSame(result, peeked);
    }

    @Test
    void testPeekOnFailure() {
        StringBuilder sb = new StringBuilder();
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> peeked = result.peek(sb::append);
        assertEquals("", sb.toString());
        assertSame(result, peeked);
    }

    @Test
    void testPeekErrorOnFailure() {
        StringBuilder sb = new StringBuilder();
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");
        Result<String> result = Result.fail(business);
        Result<String> peeked = result.peekError(e -> sb.append(e.getDetail()));
        assertEquals("Error", sb.toString());
        assertSame(result, peeked);
    }

    @Test
    void testPeekErrorOnSuccess() {
        StringBuilder sb = new StringBuilder();
        Result<String> result = Result.ok("test");
        Result<String> peeked = result.peekError(e -> sb.append(e.getDetail()));
        assertEquals("", sb.toString());
        assertSame(result, peeked);
    }

    @Test
    void testFilterOnSuccessWithPassingPredicate() {
        Result<Integer> result = Result.ok(5);
        Result<Integer> filtered = result.filter(i -> i > 0, ResponseCode.VALIDATION_ERROR_400);
        assertTrue(filtered.isSuccess());
        assertEquals(5, filtered.get());
    }

    @Test
    void testFilterOnSuccessWithFailingPredicate() {
        Result<Integer> result = Result.ok(5);
        Result<Integer> filtered = result.filter(i -> i < 0, ResponseCode.VALIDATION_ERROR_400);
        assertFalse(filtered.isSuccess());
        assertTrue(filtered.isFail());
    }

    @Test
    void testFilterOnFailure() {
        Result<Integer> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<Integer> filtered = result.filter(i -> i > 0, ResponseCode.VALIDATION_ERROR_400);
        assertFalse(filtered.isSuccess());
        assertTrue(filtered.isFail());
    }

    @Test
    void testFilterWithDetailOnSuccessWithPassingPredicate() {
        Result<Integer> result = Result.ok(5);
        Result<Integer> filtered = result.filter(i -> i > 0, ResponseCode.VALIDATION_ERROR_400, "Invalid value");
        assertTrue(filtered.isSuccess());
        assertEquals(5, filtered.get());
    }

    @Test
    void testFilterWithDetailOnSuccessWithFailingPredicate() {
        Result<Integer> result = Result.ok(5);
        Result<Integer> filtered = result.filter(i -> i < 0, ResponseCode.VALIDATION_ERROR_400, "Invalid value");
        assertFalse(filtered.isSuccess());
        assertTrue(filtered.isFail());
    }

    @Test
    void testFilterWithDetailOnFailure() {
        Result<Integer> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<Integer> filtered = result.filter(i -> i > 0, ResponseCode.VALIDATION_ERROR_400, "Invalid value");
        assertFalse(filtered.isSuccess());
        assertTrue(filtered.isFail());
    }

    @Test
    void testRecoverOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> recovered = result.recover(e -> "recovered");
        assertTrue(recovered.isSuccess());
        assertEquals("recovered", recovered.get());
    }

    @Test
    void testRecoverOnSuccess() {
        Result<String> result = Result.ok("test");
        Result<String> recovered = result.recover(e -> "recovered");
        assertTrue(recovered.isSuccess());
        assertEquals("test", recovered.get());
    }

    @Test
    void testRecoverWithOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> recovered = result.recoverWith(e -> Result.ok("recovered"));
        assertTrue(recovered.isSuccess());
        assertEquals("recovered", recovered.get());
    }

    @Test
    void testRecoverWithOnSuccess() {
        Result<String> result = Result.ok("test");
        Result<String> recovered = result.recoverWith(e -> Result.ok("recovered"));
        assertTrue(recovered.isSuccess());
        assertEquals("test", recovered.get());
    }

    @Test
    void testOnFailGetOnSuccess() {
        Result<String> result = Result.ok("test");
        String value = result.onFailGet(() -> "default");
        assertEquals("test", value);
    }

    @Test
    void testOnFailGetOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        String value = result.onFailGet(() -> "default");
        assertEquals("default", value);
    }

    @Test
    void testFailNowOnSuccess() {
        Result<String> result = Result.ok("test");
        String value = result.failNow();
        assertEquals("test", value);
    }

    @Test
    void testFailNowOnFailure() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Error");
        Result<String> result = Result.fail(business);
        assertThrows(Business.class, result::failNow);
    }

    @Test
    void testFailNowWithDefaultOnSuccess() {
        Result<String> result = Result.ok("test");
        String value = result.failNow("default");
        assertEquals("test", value);
    }

    @Test
    void testFailNowWithDefaultOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        String value = result.failNow("default");
        assertEquals("default", value);
    }

    @Test
    void testFailNowWithExceptionProviderOnSuccess() {
        Result<String> result = Result.ok("test");
        String value = result.failNow(e -> new RuntimeException("Error"));
        assertEquals("test", value);
    }

    @Test
    void testFailNowWithExceptionProviderOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        assertThrows(RuntimeException.class, () -> result.failNow(e -> new RuntimeException("Error")));
    }

    @Test
    void testCombineBothSuccess() {
        Result<Integer> result1 = Result.ok(5);
        Result<Integer> result2 = Result.ok(10);
        Result<Integer> combined = result1.combine(result2, Integer::sum);
        assertTrue(combined.isSuccess());
        assertEquals(15, combined.get());
    }

    @Test
    void testCombineFirstFailure() {
        Result<Integer> result1 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<Integer> result2 = Result.ok(10);
        Result<Integer> combined = result1.combine(result2, Integer::sum);
        assertFalse(combined.isSuccess());
        assertTrue(combined.isFail());
    }

    @Test
    void testCombineSecondFailure() {
        Result<Integer> result1 = Result.ok(5);
        Result<Integer> result2 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<Integer> combined = result1.combine(result2, Integer::sum);
        assertFalse(combined.isSuccess());
        assertTrue(combined.isFail());
    }

    @Test
    void testToOptionalOnSuccess() {
        Result<String> result = Result.ok("test");
        Optional<String> optional = result.toOptional();
        assertTrue(optional.isPresent());
        assertEquals("test", optional.get());
    }

    @Test
    void testToOptionalOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Optional<String> optional = result.toOptional();
        assertFalse(optional.isPresent());
    }

    @Test
    void testStreamOnSuccess() {
        Result<String> result = Result.ok("test");
        String value = result.stream().collect(Collectors.joining());
        assertEquals("test", value);
    }

    @Test
    void testStreamOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        String value = result.stream().collect(Collectors.joining());
        assertEquals("", value);
    }

    @Test
    void testGetOrElseOnSuccess() {
        Result<String> result = Result.ok("test");
        String value = result.getOrElse("default");
        assertEquals("test", value);
    }

    @Test
    void testGetOrElseOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        String value = result.getOrElse("default");
        assertEquals("default", value);
    }

    @Test
    void testGetOrElseGetOnSuccess() {
        Result<String> result = Result.ok("test");
        String value = result.getOrElseGet(e -> "default");
        assertEquals("test", value);
    }

    @Test
    void testGetOrElseGetOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        String value = result.getOrElseGet(e -> "default");
        assertEquals("default", value);
    }

    @Test
    void testFoldOnSuccess() {
        Result<Integer> result = Result.ok(5);
        Result<String> folded = result.fold(i -> String.valueOf(i), e -> "error");
        assertTrue(folded.isSuccess());
        assertEquals("5", folded.get());
    }

    @Test
    void testFoldOnFailure() {
        Result<Integer> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> folded = result.fold(i -> String.valueOf(i), e -> "error");
        assertTrue(folded.isSuccess());
        assertEquals("error", folded.get());
    }

    @Test
    void testSwapOnSuccess() {
        Result<String> result = Result.ok("test");
        Result<String> swapped = result.swap(ResponseCode.VALIDATION_ERROR_400);
        assertFalse(swapped.isSuccess());
        assertTrue(swapped.isFail());
    }

    @Test
    void testSwapOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> swapped = result.swap(ResponseCode.VALIDATION_ERROR_400);
        assertTrue(swapped.isSuccess());
        assertNull(swapped.get());
    }

    @Test
    void testContainsOnSuccessWithMatchingValue() {
        Result<String> result = Result.ok("test");
        assertTrue(result.contains("test"));
    }

    @Test
    void testContainsOnSuccessWithNonMatchingValue() {
        Result<String> result = Result.ok("test");
        assertFalse(result.contains("other"));
    }

    @Test
    void testContainsOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        assertFalse(result.contains("test"));
    }

    @Test
    void testExistsOnSuccessWithNonNullValue() {
        Result<String> result = Result.ok("test");
        assertTrue(result.exists());
    }

    @Test
    void testExistsOnSuccessWithNullValue() {
        Result<String> result = Result.ok(null);
        assertFalse(result.exists());
    }

    @Test
    void testExistsOnFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        assertFalse(result.exists());
    }

    @Test
    void testSuccessConstructor() {
        String value = "test";
        Result.Success<String> success = new Result.Success<>(value);
        assertEquals(200, success.getCode());
        assertEquals("Success", success.getMessage());
        assertEquals("操作成功", success.getDescription());
        assertEquals(value, success.getData());
    }

    @Test
    void testFailConstructorWithDetail() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "Invalid parameter");
        Result.Fail<String> fail = new Result.Fail<>(business);
        assertEquals(business.getResponseCode().getCode(), fail.getCode());
        assertEquals(business, fail.getError());
    }

    @Test
    void getError() {
        Business business = Business.of(ResponseCode.of(301, "Invalid parameter", "Invalid parameter"));
        Result.Fail<String> fail = new Result.Fail<>(business);
        log.info(fail.getError().toString());
    }

    @Test
    @DisplayName("测试 Success 调用 getError 应抛出 IllegalStateException")
    void testSuccessGetError_ThrowsException() {
        // 准备
        Result.Success<String> success = new Result.Success<>("test");
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                success::getError, // <--- 这里会触发 throw 语句
                "Expected getError() on Success result to throw an exception"
        );

        // 可选：验证异常信息
        assertEquals("Result is success", exception.getMessage());
    }

    @Test
    @DisplayName("覆盖 Fail 构造函数及 error 字段赋值")
    void testFailConstructor_Coverage() {
        // 1. 准备真实的 Business 对象 (不要用 mock(Business.class)，除非你只想测赋值逻辑)
        // 建议使用工厂方法创建一个真实对象，确保 ResponseCode 等内部逻辑也被覆盖
        ResponseCode code = ResponseCode.of(400, "Bad Request", "Invalid input");
        Business business = Business.of(code, "Detail message");

        // 2. 【关键步骤】显式调用构造函数
        // 这一步会强制执行：super(...) 和 this.error = error;
        Result.Fail<String> failResult = new Result.Fail<>(business);

        // 3. 断言：验证字段确实被赋值了
        // 如果这行通过，证明 this.error = error 被执行了
        assertThat(failResult.getError()).isSameAs(business);

        // 4. 额外断言：验证父类构造函数也被执行了 (code, message, description)
        assertThat(failResult.getCode()).isEqualTo(400);
        assertThat(failResult.getMessage()).contains("Bad Request"); // 取决于 I18n 处理
    }

    @Test
    void testFailConstructorWithoutDetail() {
        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        Business business = Business.of(code);
        Result.Fail<String> fail = new Result.Fail<>(business);
        assertEquals(code.getCode(), fail.getCode());
        assertEquals(business, fail.getError());
    }

    @Test
    void testFailConstructorWithNullDetail() {
        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        Business business = Business.of(code, null);
        Result.Fail<String> fail = new Result.Fail<>(business);
        assertEquals(code.getCode(), fail.getCode());
        assertEquals(business, fail.getError());
    }

    @Test
    void testGetErrorOnSuccessBranch() {
        Result<String> result = Result.ok("test");
        try {
            result.getError();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Result is success", e.getMessage());
        }
    }
}
