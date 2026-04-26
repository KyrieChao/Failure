package com.chao.failfast.spi;

import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.spi.i18n.LocalizedResponseResolver;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNull;

class LocalizedResponseResolverTest {

    @Test
    void testDefaultMethods() {
        // 创建一个实现 LocalizedResponseResolver 接口的匿名类
        LocalizedResponseResolver resolver = new LocalizedResponseResolver() {
            // 所有方法都使用默认实现
        };
        
        // 测试 resolveMessage 方法
        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        Locale locale = Locale.CHINA;
        assertNull(resolver.resolveMessage(code, locale));
        
        // 测试 resolveDetail 方法
        String detail = "Test detail";
        assertNull(resolver.resolveDetail(code, detail, locale));
        
        // 所有方法都应该返回 null，使用框架默认值
    }
}
