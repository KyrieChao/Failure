package com.chao.failfast.config;

import com.chao.failfast.spi.FailFastConfigurer;
import org.springframework.core.Ordered;

public abstract class FailFastConfigurerSupport implements FailFastConfigurer, Ordered {
    @Override
    public int getOrder() {
        return 0;
    }
}
