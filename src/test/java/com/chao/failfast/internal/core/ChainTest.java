package com.chao.failfast.internal.core;

import com.chao.failfast.validator.FastValidator.ValidationContext;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.exception.Business;
import com.chao.failfast.exception.MultiBusiness;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChainTest {

    @Test
    void testSetValidator() {
        Validator validator = Mockito.mock(Validator.class);
        Chain.setValidator(validator);
        // 验证设置成功
    }

    @Test
    void testSetFailureProperties() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        Chain.setFailureProperties(properties);
        // 验证设置成功
    }

    @Test
    void testBeginWithFailFast() {
        Chain chain = Chain.begin(true);
        assertNotNull(chain);
    }

    @Test
    void testBeginWithContext() {
        ValidationContext context = Mockito.mock(ValidationContext.class);
        when(context.isFast()).thenReturn(true);
        Chain chain = Chain.begin(context);
        assertNotNull(chain);
    }

    @Test
    void testCore() {
        Chain chain = Chain.begin(true);
        assertSame(chain, chain.core());
    }

    @Test
    void testFailWithValid() {
        Chain chain = Chain.begin(true);
        // 应该不会抛出异常
        chain.fail();
    }

    @Test
    void testFailWithInvalid() {
        Chain chain = Chain.begin(true);
        chain.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThrows(Business.class, chain::fail);
    }

    @Test
    void testFailAllWithMultipleErrors() {
        Chain chain = Chain.begin(false);
        chain.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error 1");
        chain.check(false, ResponseCode.VALIDATION_ERROR_400, "Test error 2");
        assertThrows(MultiBusiness.class, chain::failAll);
    }

    @Test
    void testVerify() {
        Chain chain = Chain.begin(true);
        // 应该不会抛出异常
        chain.verify();
    }

    @Test
    void testFailAsync() {
        Chain chain = Chain.begin(true);
        CompletableFuture<Void> future = chain.failAsync();
        assertNotNull(future);
    }

    @Test
    void testFailAllAsync() {
        Chain chain = Chain.begin(false);
        CompletableFuture<Void> future = chain.failAllAsync();
        assertNotNull(future);
    }

    @Test
    void testVerifyAsync() {
        Chain chain = Chain.begin(true);
        CompletableFuture<Boolean> future = chain.verifyAsync();
        assertNotNull(future);
    }

    @Test
    void testFailMono() {
        Chain chain = Chain.begin(true);
        Mono<Void> mono = chain.failMono();
        assertNotNull(mono);
    }

    @Test
    void testFailAllMono() {
        Chain chain = Chain.begin(false);
        Mono<Void> mono = chain.failAllMono();
        assertNotNull(mono);
    }

    @Test
    void testVerifyMono() {
        Chain chain = Chain.begin(true);
        Mono<Boolean> mono = chain.verifyMono();
        assertNotNull(mono);
    }

    @Test
    void testCheckAsyncWithMono() {
        Chain chain = Chain.begin(true);
        Mono<Boolean> mono = Mono.just(true);
        Chain result = chain.checkAsync(mono, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertSame(chain, result);
    }

    @Test
    void testCheckAsyncWithNullMono() {
        Chain chain = Chain.begin(true);
        Chain result = chain.checkAsync((Mono<Boolean>) null, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertSame(chain, result);
    }

    @Test
    void testJsrWithTarget() {
        Chain chain = Chain.begin(true);
        Object target = new Object();
        Chain.JsrValidator<Chain> jsrValidator = chain.jsr(target);
        assertNotNull(jsrValidator);
    }

    @Test
    void testJsrWithBeanClass() {
        Chain chain = Chain.begin(true);
        Class<?> beanClass = Object.class;
        Chain.JsrValidator<Chain> jsrValidator = chain.jsr(beanClass);
        assertNotNull(jsrValidator);
    }

    @Test
    void testJsrValidatorValidate() {
        Chain chain = Chain.begin(true);
        Object target = new Object();
        Chain.JsrValidator<Chain> jsrValidator = chain.jsr(target);
        Chain result = jsrValidator.validate();
        assertSame(chain, result);
    }

    @Test
    void testJsrValidatorValue() {
        Chain chain = Chain.begin(true);
        Class<?> beanClass = Object.class;
        Chain.JsrValidator<Chain> jsrValidator = chain.jsr(beanClass);
        Chain result = jsrValidator.value("test", "value");
        assertSame(chain, result);
    }

    @Test
    void testJsrValidatorPathPrefix() {
        Chain chain = Chain.begin(true);
        Object target = new Object();
        Chain.JsrValidator<Chain> jsrValidator = chain.jsr(target);
        Chain.JsrValidator<Chain> result = jsrValidator.pathPrefix("prefix");
        assertNotNull(result);
    }
}
