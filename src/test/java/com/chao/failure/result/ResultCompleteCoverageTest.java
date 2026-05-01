package com.chao.failure.result;

import com.chao.failure.exception.Business;
import com.chao.failure.internal.core.ResponseCode;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ResultCompleteCoverageTest {

    // ============================================
    // 构造函数与基础状态测试
    // ============================================

    @Test
    void testSuccessConstructorComplete() {
        String value = "test-value";
        Result.Success<String> success = new Result.Success<>(value);
        assertEquals(200, success.getCode());
        assertEquals("Success", success.getMessage());
        assertNotNull(success.getDescription());
        assertEquals(value, success.getData());
    }

    @Test
    void testFailConstructorComplete() {
        ResponseCode code = ResponseCode.of(500, "Server Error", "Internal Server Error");
        Business business = Business.of(code, "test-detail");
        Result.Fail<String> fail = new Result.Fail<>(business);
        assertEquals(500, fail.getCode());
        assertSame(business, fail.getError());
    }

    // ============================================
    // Result 类型状态检查
    // ============================================

    @Test
    void testIsSuccessWithSuccessResult() {
        Result<String> result = Result.success("test");
        assertTrue(result.isSuccess());
        assertFalse(result.isFail());
    }

    @Test
    void testIsSuccessWithFailResult() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        assertFalse(result.isSuccess());
        assertTrue(result.isFail());
    }

    // ============================================
    // get() 方法完整覆盖
    // ============================================

    @Test
    void testGetOnSuccess() {
        Result<String> result = Result.success("test");
        assertEquals("test", result.get());
    }

    @Test
    void testGetOnFailThrowsException() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        IllegalStateException exception = assertThrows(IllegalStateException.class, result::get);
        assertEquals("Result is fail", exception.getMessage());
    }

    // ============================================
    // getError() 方法完整覆盖
    // ============================================

    @Test
    void testGetErrorOnFail() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "test-detail");
        Result<String> result = Result.fail(business);
        assertSame(business, result.getError());
    }

    @Test
    void testGetErrorOnSuccessThrowsException() {
        Result<String> result = Result.success("test");
        IllegalStateException exception = assertThrows(IllegalStateException.class, result::getError);
        assertEquals("Result is success", exception.getMessage());
    }

    // ============================================
    // getOrNull() 方法完整覆盖
    // ============================================

    @Test
    void testGetOrNullOnSuccess() {
        Result<String> result = Result.success("test");
        assertEquals("test", result.getOrNull());
    }

    @Test
    void testGetOrNullOnFailReturnsNull() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        assertNull(result.getOrNull());
    }

    @Test
    void testGetOrNullOnSuccessWithNullValue() {
        Result<String> result = Result.success(null);
        assertNull(result.getOrNull());
    }

    // ============================================
    // map() 方法完整覆盖
    // ============================================

    @Test
    void testMapOnSuccessTransformsValue() {
        Result<Integer> result = Result.success(42);
        Result<String> mapped = result.map(String::valueOf);
        assertTrue(mapped.isSuccess());
        assertEquals("42", mapped.get());
    }

    @Test
    void testMapOnFailReturnsSameFailure() {
        Result<Integer> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> mapped = result.map(String::valueOf);
        assertTrue(mapped.isFail());
        assertSame(result, mapped);
    }

    @Test
    void testMapOnSuccessThrowsBusinessException() {
        Result<Integer> result = Result.success(42);
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "error");
        Result<String> mapped = result.map(i -> { throw business; });
        assertTrue(mapped.isFail());
        assertEquals(business, mapped.getError());
    }

    @Test
    void testMapOnSuccessThrowsNonBusinessExceptionPropagates() {
        Result<Integer> result = Result.success(42);
        RuntimeException exception = new RuntimeException("test");
        assertThrows(RuntimeException.class, () -> result.map(i -> { throw exception; }));
    }

    // ============================================
    // flatMap() 方法完整覆盖
    // ============================================

    @Test
    void testFlatMapOnSuccessReturnsNewResult() {
        Result<Integer> result = Result.success(42);
        Result<String> flatMapped = result.flatMap(i -> Result.success(String.valueOf(i)));
        assertTrue(flatMapped.isSuccess());
        assertEquals("42", flatMapped.get());
    }

    @Test
    void testFlatMapOnFailReturnsSameFailure() {
        Result<Integer> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> flatMapped = result.flatMap(i -> Result.success(String.valueOf(i)));
        assertTrue(flatMapped.isFail());
        assertSame(result, flatMapped);
    }

    // ============================================
    // peek() 方法完整覆盖
    // ============================================

    @Test
    void testPeekOnSuccessExecutesAction() {
        Result<String> result = Result.success("test");
        StringBuilder sb = new StringBuilder();
        Result<String> returned = result.peek(sb::append);
        assertEquals("test", sb.toString());
        assertSame(result, returned);
    }

    @Test
    void testPeekOnFailDoesNotExecuteAction() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        StringBuilder sb = new StringBuilder();
        Result<String> returned = result.peek(sb::append);
        assertEquals("", sb.toString());
        assertSame(result, returned);
    }

    // ============================================
    // peekError() 方法完整覆盖
    // ============================================

    @Test
    void testPeekErrorOnFailExecutesAction() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        Result<String> result = Result.fail(business);
        StringBuilder sb = new StringBuilder();
        Result<String> returned = result.peekError(e -> sb.append(e.getDetail()));
        assertEquals("detail", sb.toString());
        assertSame(result, returned);
    }

    @Test
    void testPeekErrorOnSuccessDoesNotExecuteAction() {
        Result<String> result = Result.success("test");
        StringBuilder sb = new StringBuilder();
        Result<String> returned = result.peekError(e -> sb.append(e.getDetail()));
        assertEquals("", sb.toString());
        assertSame(result, returned);
    }

    // ============================================
    // filter() 方法完整覆盖
    // ============================================

    @Test
    void testFilterOnSuccessWithTruePredicate() {
        Result<Integer> result = Result.success(42);
        Result<Integer> filtered = result.filter(i -> i > 0, ResponseCode.VALIDATION_ERROR_400);
        assertTrue(filtered.isSuccess());
        assertEquals(42, filtered.get());
    }

    @Test
    void testFilterOnSuccessWithFalsePredicate() {
        Result<Integer> result = Result.success(42);
        Result<Integer> filtered = result.filter(i -> i < 0, ResponseCode.VALIDATION_ERROR_400);
        assertTrue(filtered.isFail());
        assertEquals(ResponseCode.VALIDATION_ERROR_400.getCode(), filtered.getCode());
    }

    @Test
    void testFilterOnFailReturnsSameFailure() {
        Result<Integer> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<Integer> filtered = result.filter(i -> i > 0, ResponseCode.VALIDATION_ERROR_400);
        assertTrue(filtered.isFail());
        assertSame(result, filtered);
    }

    @Test
    void testFilterWithDetailOnSuccessWithTruePredicate() {
        Result<Integer> result = Result.success(42);
        Result<Integer> filtered = result.filter(i -> i > 0, ResponseCode.VALIDATION_ERROR_400, "detail");
        assertTrue(filtered.isSuccess());
        assertEquals(42, filtered.get());
    }

    @Test
    void testFilterWithDetailOnSuccessWithFalsePredicate() {
        Result<Integer> result = Result.success(42);
        Result<Integer> filtered = result.filter(i -> i < 0, ResponseCode.VALIDATION_ERROR_400, "detail");
        assertTrue(filtered.isFail());
        assertEquals(ResponseCode.VALIDATION_ERROR_400.getCode(), filtered.getCode());
    }

    @Test
    void testFilterWithDetailOnFailReturnsSameFailure() {
        Result<Integer> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<Integer> filtered = result.filter(i -> i > 0, ResponseCode.VALIDATION_ERROR_400, "detail");
        assertTrue(filtered.isFail());
        assertSame(result, filtered);
    }

    // ============================================
    // recover() 方法完整覆盖
    // ============================================

    @Test
    void testRecoverOnFailRecoversValue() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> recovered = result.recover(e -> "recovered");
        assertTrue(recovered.isSuccess());
        assertEquals("recovered", recovered.get());
    }

    @Test
    void testRecoverOnSuccessReturnsSameResult() {
        Result<String> result = Result.success("original");
        Result<String> recovered = result.recover(e -> "recovered");
        assertTrue(recovered.isSuccess());
        assertEquals("original", recovered.get());
    }

    // ============================================
    // recoverWith() 方法完整覆盖
    // ============================================

    @Test
    void testRecoverWithOnFailReturnsNewResult() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> recovered = result.recoverWith(e -> Result.success("recovered"));
        assertTrue(recovered.isSuccess());
        assertEquals("recovered", recovered.get());
    }

    @Test
    void testRecoverWithOnFailReturnsNewFailure() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Business newBusiness = Business.of(ResponseCode.VALIDATION_ERROR_400, "new-detail");
        Result<String> recovered = result.recoverWith(e -> Result.fail(newBusiness));
        assertTrue(recovered.isFail());
        assertEquals(newBusiness, recovered.getError());
    }

    @Test
    void testRecoverWithOnSuccessReturnsSameResult() {
        Result<String> result = Result.success("original");
        Result<String> recovered = result.recoverWith(e -> Result.success("recovered"));
        assertTrue(recovered.isSuccess());
        assertEquals("original", recovered.get());
    }

    // ============================================
    // onFailGet() 方法完整覆盖
    // ============================================

    @Test
    void testOnFailGetOnSuccessReturnsValue() {
        Result<String> result = Result.success("success");
        String value = result.onFailGet(() -> "default");
        assertEquals("success", value);
    }

    @Test
    void testOnFailGetOnFailReturnsDefault() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        String value = result.onFailGet(() -> "default");
        assertEquals("default", value);
    }

    // ============================================
    // failNow() 方法完整覆盖
    // ============================================

    @Test
    void testFailNowOnSuccessReturnsValue() {
        Result<String> result = Result.success("success");
        String value = result.failNow();
        assertEquals("success", value);
    }

    @Test
    void testFailNowOnFailThrowsBusinessException() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        Result<String> result = Result.fail(business);
        Business thrown = assertThrows(Business.class, result::failNow);
        assertSame(business, thrown);
    }

    @Test
    void testFailNowWithDefaultOnSuccessReturnsValue() {
        Result<String> result = Result.success("success");
        String value = result.failNow("default");
        assertEquals("success", value);
    }

    @Test
    void testFailNowWithDefaultOnFailReturnsDefault() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        String value = result.failNow("default");
        assertEquals("default", value);
    }

    @Test
    void testFailNowWithExceptionProviderOnSuccessReturnsValue() {
        Result<String> result = Result.success("success");
        String value = result.failNow(e -> new RuntimeException(e.getMessage()));
        assertEquals("success", value);
    }

    @Test
    void testFailNowWithExceptionProviderOnFailThrowsCustomException() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        Result<String> result = Result.fail(business);
        assertThrows(RuntimeException.class, () -> result.failNow(e -> new RuntimeException("custom exception")));
    }

    // ============================================
    // combine() 方法完整覆盖
    // ============================================

    @Test
    void testCombineBothSuccessCombinesValues() {
        Result<Integer> result1 = Result.success(2);
        Result<Integer> result2 = Result.success(3);
        Result<Integer> combined = result1.combine(result2, Integer::sum);
        assertTrue(combined.isSuccess());
        assertEquals(5, combined.get());
    }

    @Test
    void testCombineFirstFailReturnsFirstFailure() {
        Result<Integer> result1 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<Integer> result2 = Result.success(3);
        Result<Integer> combined = result1.combine(result2, Integer::sum);
        assertTrue(combined.isFail());
        assertSame(result1, combined);
    }

    @Test
    void testCombineSecondFailReturnsSecondFailure() {
        Result<Integer> result1 = Result.success(2);
        Result<Integer> result2 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<Integer> combined = result1.combine(result2, Integer::sum);
        assertTrue(combined.isFail());
        assertSame(result2, combined);
    }

    @Test
    void testCombineBothFailReturnsFirstFailure() {
        Result<Integer> result1 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<Integer> result2 = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<Integer> combined = result1.combine(result2, Integer::sum);
        assertTrue(combined.isFail());
        assertSame(result1, combined);
    }

    // ============================================
    // toOptional() 方法完整覆盖
    // ============================================

    @Test
    void testToOptionalOnSuccessReturnsPresent() {
        Result<String> result = Result.success("test");
        Optional<String> optional = result.toOptional();
        assertTrue(optional.isPresent());
        assertEquals("test", optional.get());
    }

    @Test
    void testToOptionalOnFailReturnsEmpty() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Optional<String> optional = result.toOptional();
        assertFalse(optional.isPresent());
    }

    @Test
    void testToOptionalOnSuccessWithNullValueReturnsEmpty() {
        Result<String> result = Result.success(null);
        Optional<String> optional = result.toOptional();
        assertFalse(optional.isPresent());
    }

    // ============================================
    // stream() 方法完整覆盖
    // ============================================

    @Test
    void testStreamOnSuccessContainsValue() {
        Result<String> result = Result.success("test");
        List<String> list = result.stream().collect(Collectors.toList());
        assertEquals(1, list.size());
        assertEquals("test", list.get(0));
    }

    @Test
    void testStreamOnFailIsEmpty() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        List<String> list = result.stream().collect(Collectors.toList());
        assertTrue(list.isEmpty());
    }

    @Test
    void testStreamOnSuccessWithNullValueIsEmpty() {
        Result<String> result = Result.success(null);
        List<String> list = result.stream().collect(Collectors.toList());
        assertTrue(list.isEmpty());
    }

    // ============================================
    // toMono() 方法完整覆盖
    // ============================================

    @Test
    void testToMonoOnSuccessEmitsValue() {
        Result<String> result = Result.success("test");
        Mono<String> mono = result.toMono();
        String value = mono.block();
        assertEquals("test", value);
    }

    @Test
    void testToMonoOnFailEmitsError() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        Result<String> result = Result.fail(business);
        Mono<String> mono = result.toMono();
        assertThrows(Business.class, mono::block);
    }

    @Test
    void testToMonoOnSuccessWithNullValueEmitsEmpty() {
        Result<String> result = Result.success(null);
        Mono<String> mono = result.toMono();
        String value = mono.block();
        assertNull(value);
    }

    // ============================================
    // toFlux() 方法完整覆盖
    // ============================================

    @Test
    void testToFluxOnSuccessEmitsValue() {
        Result<String> result = Result.success("test");
        Flux<String> flux = result.toFlux();
        List<String> values = flux.collectList().block();
        assertEquals(1, values.size());
        assertEquals("test", values.get(0));
    }

    @Test
    void testToFluxOnFailEmitsError() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        Result<String> result = Result.fail(business);
        Flux<String> flux = result.toFlux();
        assertThrows(Business.class, () -> flux.collectList().block());
    }

    @Test
    void testToFluxOnSuccessWithNullValueEmitsEmpty() {
        Result<String> result = Result.success(null);
        Flux<String> flux = result.toFlux();
        List<String> values = flux.collectList().block();
        assertTrue(values.isEmpty());
    }

    // ============================================
    // toFluxElements() 方法完整覆盖
    // ============================================

    @Test
    void testToFluxElementsOnSuccessWithCollectionEmitsElements() {
        List<String> list = Arrays.asList("a", "b", "c");
        Result<List<String>> result = Result.success(list);
        Flux<String> flux = result.toFluxElements();
        List<String> values = flux.collectList().block();
        assertEquals(3, values.size());
        assertEquals("a", values.get(0));
        assertEquals("b", values.get(1));
        assertEquals("c", values.get(2));
    }

    @Test
    void testToFluxElementsOnFailEmitsError() {
        Result<List<String>> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Flux<String> flux = result.toFluxElements();
        assertThrows(RuntimeException.class, () -> flux.collectList().block());
    }

    @Test
    void testToFluxElementsOnSuccessWithNullValueEmitsEmpty() {
        Result<List<String>> result = Result.success(null);
        Flux<String> flux = result.toFluxElements();
        List<String> values = flux.collectList().block();
        assertThat(values).isEmpty();
    }

    @Test
    void testToFluxElementsOnSuccessWithNonCollectionThrowsException() {
        Result<Object> result = Result.success(new Object());
        Flux<Object> flux = result.toFluxElements();
        assertThrows(IllegalStateException.class, () -> flux.collectList().block());
    }

    @Test
    void testToFluxElementsWithGenericType() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        Result<List<Integer>> result = Result.success(list);
        Flux<Integer> flux = result.toFluxElements();
        List<Integer> values = flux.collectList().block();
        assertEquals(3, values.size());
        assertEquals(1, values.get(0));
        assertEquals(2, values.get(1));
        assertEquals(3, values.get(2));
    }

    // ============================================
    // getOrElse() 方法完整覆盖
    // ============================================

    @Test
    void testGetOrElseOnSuccessReturnsValue() {
        Result<String> result = Result.success("success");
        String value = result.getOrElse("default");
        assertEquals("success", value);
    }

    @Test
    void testGetOrElseOnFailReturnsDefault() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        String value = result.getOrElse("default");
        assertEquals("default", value);
    }

    @Test
    void testGetOrElseOnSuccessWithNullValueReturnsNull() {
        Result<String> result = Result.success(null);
        String value = result.getOrElse("default");
        assertNull(value);
    }

    // ============================================
    // getOrElseGet() 方法完整覆盖
    // ============================================

    @Test
    void testGetOrElseGetOnSuccessReturnsValue() {
        Result<String> result = Result.success("success");
        String value = result.getOrElseGet(e -> "default");
        assertEquals("success", value);
    }

    @Test
    void testGetOrElseGetOnFailReturnsComputedValue() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        Result<String> result = Result.fail(business);
        String value = result.getOrElseGet(e -> "default-" + e.getDetail());
        assertEquals("default-detail", value);
    }

    // ============================================
    // fold() 方法完整覆盖
    // ============================================

    @Test
    void testFoldOnSuccessUsesSuccessFunction() {
        Result<Integer> result = Result.success(42);
        Result<String> folded = result.fold(i -> "value: " + i, e -> "error");
        assertTrue(folded.isSuccess());
        assertEquals("value: 42", folded.get());
    }

    @Test
    void testFoldOnFailUsesFailureFunction() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        Result<Integer> result = Result.fail(business);
        Result<String> folded = result.fold(i -> "value: " + i, e -> "error: " + e.getDetail());
        assertTrue(folded.isSuccess());
        assertEquals("error: detail", folded.get());
    }

    // ============================================
    // swap() 方法完整覆盖
    // ============================================

    @Test
    void testSwapOnSuccessReturnsFail() {
        Result<String> result = Result.success("test");
        Result<String> swapped = result.swap(ResponseCode.VALIDATION_ERROR_400);
        assertTrue(swapped.isFail());
        assertEquals(ResponseCode.VALIDATION_ERROR_400.getCode(), swapped.getCode());
    }

    @Test
    void testSwapOnFailReturnsSuccessWithNull() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        Result<String> swapped = result.swap(ResponseCode.VALIDATION_ERROR_400);
        assertTrue(swapped.isSuccess());
        assertNull(swapped.get());
    }

    // ============================================
    // contains() 方法完整覆盖
    // ============================================

    @Test
    void testContainsOnSuccessWithMatchingValueReturnsTrue() {
        Result<String> result = Result.success("test");
        assertTrue(result.contains("test"));
    }

    @Test
    void testContainsOnSuccessWithNonMatchingValueReturnsFalse() {
        Result<String> result = Result.success("test");
        assertFalse(result.contains("other"));
    }

    @Test
    void testContainsOnFailReturnsFalse() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        assertFalse(result.contains("test"));
    }

    @Test
    void testContainsOnSuccessWithNullValueChecksNull() {
        Result<String> result = Result.success(null);
        assertTrue(result.contains(null));
    }

    @Test
    void testContainsOnSuccessWithNullValueChecksNonNullReturnsFalse() {
        Result<String> result = Result.success(null);
        assertFalse(result.contains("test"));
    }

    // ============================================
    // exists() 方法完整覆盖
    // ============================================

    @Test
    void testExistsOnSuccessWithNonNullValueReturnsTrue() {
        Result<String> result = Result.success("test");
        assertTrue(result.exists());
    }

    @Test
    void testExistsOnSuccessWithNullValueReturnsFalse() {
        Result<String> result = Result.success(null);
        assertFalse(result.exists());
    }

    @Test
    void testExistsOnFailReturnsFalse() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        assertFalse(result.exists());
    }

    // ============================================
    // ofNullable() 方法完整覆盖
    // ============================================

    @Test
    void testOfNullableWithNonNullValueReturnsSuccess() {
        Result<String> result = Result.ofNullable("test", ResponseCode.VALIDATION_ERROR_400);
        assertTrue(result.isSuccess());
        assertEquals("test", result.get());
    }

    @Test
    void testOfNullableWithNullValueReturnsFail() {
        Result<String> result = Result.ofNullable(null, ResponseCode.VALIDATION_ERROR_400);
        assertTrue(result.isFail());
        assertEquals(ResponseCode.VALIDATION_ERROR_400.getCode(), result.getCode());
    }

    @Test
    void testOfNullableWithNonNullValueAndDetailReturnsSuccess() {
        Result<String> result = Result.ofNullable("test", ResponseCode.VALIDATION_ERROR_400, "detail");
        assertTrue(result.isSuccess());
        assertEquals("test", result.get());
    }

    @Test
    void testOfNullableWithNullValueAndDetailReturnsFail() {
        Result<String> result = Result.ofNullable(null, ResponseCode.VALIDATION_ERROR_400, "detail");
        assertTrue(result.isFail());
        assertEquals(ResponseCode.VALIDATION_ERROR_400.getCode(), result.getCode());
    }

    // ============================================
    // success() 工厂方法完整覆盖
    // ============================================

    @Test
    void testSuccessWithValue() {
        Result<String> result = Result.success("test");
        assertTrue(result.isSuccess());
        assertEquals("test", result.get());
    }

    @Test
    void testSuccessWithNull() {
        Result<String> result = Result.success(null);
        assertTrue(result.isSuccess());
        assertNull(result.get());
    }

    // ============================================
    // fail() 工厂方法完整覆盖
    // ============================================

    @Test
    void testFailWithResponseCode() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400);
        assertTrue(result.isFail());
        assertEquals(ResponseCode.VALIDATION_ERROR_400.getCode(), result.getCode());
    }

    @Test
    void testFailWithResponseCodeAndDetail() {
        Result<String> result = Result.fail(ResponseCode.VALIDATION_ERROR_400, "detail");
        assertTrue(result.isFail());
        assertEquals(ResponseCode.VALIDATION_ERROR_400.getCode(), result.getCode());
    }

    @Test
    void testFailWithBusiness() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        Result<String> result = Result.fail(business);
        assertTrue(result.isFail());
        assertSame(business, result.getError());
    }

    // ============================================
    // 覆盖 Success 类的完整访问
    // ============================================

    @Test
    void testSuccessGetters() {
        Result.Success<String> success = new Result.Success<>("test");
        assertEquals(200, success.getCode());
        assertEquals("Success", success.getMessage());
        assertNotNull(success.getDescription());
        assertEquals("test", success.getData());
    }

    // ============================================
    // 覆盖 Fail 类的完整访问
    // ============================================

    @Test
    void testFailGetters() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        Result.Fail<String> fail = new Result.Fail<>(business);
        assertEquals(business.getResponseCode().getCode(), fail.getCode());
        assertNotNull(fail.getMessage());
        assertNotNull(fail.getDescription());
        assertSame(business, fail.getError());
    }

    // ============================================
    // 测试内部类型转换
    // ============================================

    @Test
    void testCastingSuccessToResult() {
        Result.Success<String> success = new Result.Success<>("test");
        Result<String> result = success;
        assertTrue(result.isSuccess());
    }

    @Test
    void testCastingFailToResult() {
        Business business = Business.of(ResponseCode.VALIDATION_ERROR_400, "detail");
        Result.Fail<String> fail = new Result.Fail<>(business);
        Result<String> result = fail;
        assertTrue(result.isFail());
    }

    // ============================================
    // 测试 timestamp 字段初始化
    // ============================================

    @Test
    void testTimestampIsSet() {
        Result<String> result = Result.success("test");
        assertNotNull(result.getTimestamp());
    }
}
