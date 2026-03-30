package com.chao.failfast.config;

import com.chao.failfast.autoconfigure.FailFastAutoConfiguration;
import com.chao.failfast.autoconfigure.FailFastReactiveAutoConfiguration;
import com.chao.failfast.integration.webflux.FailFastWebExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FailFast Reactive 自动配置测试")
class FailFastReactiveAutoConfigurationTest {

    @Test
    @DisplayName("Reactive 模式下加载 WebFilter 与 WebExceptionHandler")
    void testReactiveBeans() {
        new ReactiveWebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        FailFastAutoConfiguration.class,
                        FailFastReactiveAutoConfiguration.class
                ))
                .withPropertyValues(
                        "fail-fast.verbose=true",
                        "fail-fast.trace-id.enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasBean("failFastReactiveCleanupFilter");
                    assertThat(context).hasBean("failFastReactiveTraceIdFilter");
                    assertThat(context).hasSingleBean(FailFastWebExceptionHandler.class);
                });
    }
}
