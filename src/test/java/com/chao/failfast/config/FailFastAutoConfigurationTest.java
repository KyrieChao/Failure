package com.chao.failfast.config;

import com.chao.failfast.integration.mvc.DefaultExceptionHandler;
import com.chao.failfast.integration.mvc.FailFastExceptionHandler;
import com.chao.failfast.aspect.ValidationAspect;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.autoconfigure.FailFastAutoConfiguration;
import com.chao.failfast.integration.mvc.OptionalBodyResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.MessageSource;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FailFastAutoConfigurationTest {

    @Mock
    private FailureProperties properties;

    @Mock
    private MessageSource messageSource;

    @Mock
    private Validator validator;

    @Mock
    private ObjectProvider<Validator> validatorProvider;

    @Mock
    private FailureContext context;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private FilterChain filterChain;

    private FailFastAutoConfiguration configuration;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(validatorProvider.getIfAvailable()).thenReturn(validator);
        configuration = new FailFastAutoConfiguration(properties, messageSource);
    }

    @Test
    void testConstructor() {
        assertNotNull(configuration);
    }

    @Test
    void testInit() {
        // 测试init方法，主要是日志输出
        FailureProperties.I18n i18n = mock(FailureProperties.I18n.class);
        when(properties.getI18n()).thenReturn(i18n);
        when(i18n.isEnabled()).thenReturn(false);
        when(properties.isShadowTrace()).thenReturn(true);
        when(properties.isDebugSnapshot()).thenReturn(true);
        when(properties.isMethodValidationEnabled()).thenReturn(true);

        configuration.init();
        // 验证日志输出，这里主要是确保方法执行不抛出异常
    }

    @Test
    void testGetMessageWithI18nDisabled() {
        FailureProperties.I18n i18n = mock(FailureProperties.I18n.class);
        when(properties.getI18n()).thenReturn(i18n);
        when(i18n.isEnabled()).thenReturn(false);

        // 通过反射测试getMessage方法
        try {
            var method = FailFastAutoConfiguration.class.getDeclaredMethod("getMessage");
            method.setAccessible(true);
            String result = (String) method.invoke(configuration);
            assertEquals("log.fail.fast.auto.config.debug.enabled", result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testGetMessageWithI18nEnabled() {
        FailureProperties.I18n i18n = mock(FailureProperties.I18n.class);
        when(properties.getI18n()).thenReturn(i18n);
        when(i18n.isEnabled()).thenReturn(true);
        when(i18n.getDefaultLocale()).thenReturn("zh-CN");
        when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
                .thenReturn("测试消息");

        // 通过反射测试getMessage方法
        try {
            var method = FailFastAutoConfiguration.class.getDeclaredMethod("getMessage");
            method.setAccessible(true);
            String result = (String) method.invoke(configuration);
            assertEquals("测试消息", result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("测试 FailureContext 加载")
    void testFailFastContext() {
        // 1. 准备配置属性 (替代手动 new FailureProperties())
        String[] activeProperties = new String[] {
                "fail-fast.shadow-trace=true",
                "fail-fast.debug-snapshot=true",
                "fail-fast.method-validation-enabled=true",
                "fail-fast.i18n.default-locale=zh_CN"
                // 根据需要添加其他配置
        };

        new WebApplicationContextRunner()
                // ✅ 修正 1: 使用标准的 AutoConfigurations.of
                .withConfiguration(AutoConfigurations.of(FailFastAutoConfiguration.class))

                // ✅ 修正 2: 移除 .withBean(FailureProperties...)
                // 改用 withPropertyValues，让 @EnableConfigurationProperties 自动创建唯一的 Bean
                .withPropertyValues(activeProperties)

                .run(context -> {
                    // 验证 Properties Bean 只有一个 (确保修复成功)
                    assertThat(context).hasSingleBean(FailureProperties.class);

                    // 验证自动配置类已加载
                    assertThat(context).hasSingleBean(FailFastAutoConfiguration.class);

                    // 你的原始断言逻辑：
                    FailureContext failFastContext = context.getBean(FailureContext.class);
                    assertNotNull(failFastContext);

                    // 可选：验证 context 里的 properties 是否绑定成功
                    // FailureProperties props = context.getBean(FailureProperties.class);
                    // assertTrue(props.isShadowTrace());
                });
    }

    @Test
    @DisplayName("测试错误码映射配置加载")
    void testCodeMappingConfig() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(FailFastAutoConfiguration.class))
                // 【关键】直接通过属性值模拟 application.yml 中的配置
                // 注意：YAML 中的列表 "- " 对应 properties 中的 [0], [1]...
                .withPropertyValues(
                        "fail-fast.shadow-trace=true",
                        "fail-fast.debug-snapshot=true",
                        "fail-fast.method-validation-enabled=true",

                        // Trace ID 配置
                        "fail-fast.trace-id.enabled=true",
                        "fail-fast.trace-id.header-name=X-Trace-Id",
                        "fail-fast.trace-id.mdc-enabled=true",
                        "fail-fast.trace-id.mdc-key=traceId",
                        "fail-fast.trace-id.response-header=true",

                        // 通用约束映射 (Constraint Mapping)
                        "fail-fast.code-mapping.constraint-mapping.NotBlank=40010",
                        "fail-fast.code-mapping.constraint-mapping.Email=40020",
                        "fail-fast.code-mapping.constraint-mapping.Positive=40030",

                        // 路径约束映射 (Constraint Path Mapping) - 列表形式
                        "fail-fast.code-mapping.constraint-path-mapping[0].constraint=NotBlank",
                        "fail-fast.code-mapping.constraint-path-mapping[0].path=user.username",
                        "fail-fast.code-mapping.constraint-path-mapping[0].code=40040",

                        "fail-fast.code-mapping.constraint-path-mapping[1].constraint=NotNull",
                        "fail-fast.code-mapping.constraint-path-mapping[1].path=user.username",
                        "fail-fast.code-mapping.constraint-path-mapping[1].code=40040",

                        "fail-fast.code-mapping.constraint-path-mapping[2].constraint=Email",
                        "fail-fast.code-mapping.constraint-path-mapping[2].path=user.email",
                        "fail-fast.code-mapping.constraint-path-mapping[2].code=40050",

                        // Bean 约束映射 (Constraint Bean Mapping) - 列表形式
                        "fail-fast.code-mapping.constraint-bean-mapping[0].constraint=NotBlank",
                        "fail-fast.code-mapping.constraint-bean-mapping[0].bean=com.chao.failuretest.model.dto.UserJSRDTO",
                        "fail-fast.code-mapping.constraint-bean-mapping[0].code=40060",

                        "fail-fast.code-mapping.constraint-bean-mapping[1].constraint=Email",
                        "fail-fast.code-mapping.constraint-bean-mapping[1].bean=com.chao.failuretest.model.dto.UserDTO",
                        "fail-fast.code-mapping.constraint-bean-mapping[1].code=40070",

                        // I18n 配置
                        "fail-fast.i18n.default-locale=zh_CN"
                )
                .run(context -> {
                    // 1. 验证容器启动成功，且只有一个 FailureProperties Bean
                    assertThat(context).hasSingleBean(FailureProperties.class);
                    assertThat(context).hasSingleBean(FailFastAutoConfiguration.class);

                    // 2. 获取 Bean 进行断言
                    FailureProperties properties = context.getBean(FailureProperties.class);

                    // --- 断言基础配置 ---
                    assertThat(properties.isShadowTrace()).isTrue();
                    assertThat(properties.isDebugSnapshot()).isTrue();
                    assertThat(properties.isMethodValidationEnabled()).isTrue();

                    // --- 断言 Trace ID 配置 ---
                    assertThat(properties.getTraceId().isEnabled()).isTrue();
                    assertThat(properties.getTraceId().getHeaderName()).isEqualTo("X-Trace-Id");
                    assertThat(properties.getTraceId().getMdcKey()).isEqualTo("traceId");

                    // --- 断言 通用约束映射 ---
                    assertThat(properties.getCodeMapping().getConstraintMapping())
                            .containsEntry("NotBlank", 40010)
                            .containsEntry("Email", 40020);

                    // --- 断言 路径约束映射 (列表) ---
                    List<FailureProperties.CodeMapping.ConstraintPathMapping> pathMappings = properties.getCodeMapping().getConstraintPathMapping();
                    assertThat(pathMappings).hasSize(3);
                    assertThat(pathMappings.get(0).getConstraint()).isEqualTo("NotBlank");
                    assertThat(pathMappings.get(0).getPath()).isEqualTo("user.username");
                    assertThat(pathMappings.get(0).getCode()).isEqualTo(40040);

                    // --- 断言 Bean 约束映射 (列表) ---
                    List<FailureProperties.CodeMapping.ConstraintBeanMapping> beanMappings = properties.getCodeMapping().getConstraintBeanMapping();
                    assertThat(beanMappings).hasSize(2);
                    assertThat(beanMappings.get(0).getBean()).isEqualTo("com.chao.failuretest.model.dto.UserJSRDTO");
                    assertThat(beanMappings.get(0).getCode()).isEqualTo(40060);

                    // --- 断言 I18n ---
                    assertThat(properties.getI18n().getDefaultLocale()).isEqualTo("zh_CN");
                });
    }

// 在 FailFastAutoConfigurationTest.java 中

    @Test
    @DisplayName("测试错误策略相关配置加载")
    void testErrorPolicy() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(FailFastAutoConfiguration.class))
                // 替换为你实际 YAML 中的真实配置项
                // 移除了不存在的 "fail-fast.enabled" 和 "fail-fast.error-policy"
                .withPropertyValues(
                        // 核心开关
                        "fail-fast.shadow-trace=true",
                        "fail-fast.debug-snapshot=true",
                        "fail-fast.method-validation-enabled=true",

                        // Trace ID 策略
                        "fail-fast.trace-id.enabled=true",
                        "fail-fast.trace-id.header-name=X-Trace-Id",
                        "fail-fast.trace-id.mdc-enabled=true",
                        "fail-fast.trace-id.mdc-key=traceId",

                        // 简单的错误码映射策略 (示例)
                        "fail-fast.code-mapping.constraint-mapping.NotBlank=40010",
                        "fail-fast.code-mapping.constraint-mapping.Email=40020",

                        // I18n 策略
                        "fail-fast.i18n.default-locale=zh_CN"
                )
                .run(context -> {
                    // 1. 验证容器中没有 Bean 冲突 (只有一个 FailureProperties)
                    assertThat(context).hasSingleBean(FailureProperties.class);

                    // 2. 验证自动配置类已加载
                    assertThat(context).hasSingleBean(FailFastAutoConfiguration.class);

                    // 3. 【可选】获取 Bean 验证具体的策略值是否生效
                    FailureProperties properties = context.getBean(FailureProperties.class);

                    // 断言 shadow-trace 策略
                    assertThat(properties.isShadowTrace()).isTrue();

                    // 断言 debug-snapshot 策略
                    assertThat(properties.isDebugSnapshot()).isTrue();

                    // 断言映射策略
                    assertThat(properties.getCodeMapping().getConstraintMapping())
                            .containsEntry("NotBlank", 40010)
                            .containsEntry("Email", 40020);

                    // 断言 I18n 策略
                    assertThat(properties.getI18n().getDefaultLocale()).isEqualTo("zh_CN");

                    // 如果你的代码中有具体的 "ErrorPolicy" 枚举或字段，在这里断言
                    // assertThat(properties.getErrorPolicy()).isEqualTo(...);
                });
    }

    @Test
    @DisplayName("测试默认异常处理器加载")
    void testDefaultFailFastExceptionHandler() {
        // 1. 准备属性值 (模拟你的 YAML 配置)
        // 不需要手动 new FailureProperties() 对象，直接传字符串配置即可
        String[] activeProperties = new String[] {
                "fail-fast.shadow-trace=true",
                "fail-fast.debug-snapshot=true",
                "fail-fast.method-validation-enabled=true",
                "fail-fast.trace-id.enabled=true",
                "fail-fast.i18n.default-locale=zh_CN"
                // 根据需要添加更多配置...
        };

        new WebApplicationContextRunner()
                // ✅ 修正 1: 使用 AutoConfigurations.of 包裹自动配置类
                .withConfiguration(AutoConfigurations.of(FailFastAutoConfiguration.class))

                // ✅ 修正 2: 移除 .withBean(FailureProperties...)
                // 改用 withPropertyValues，让 Spring 自动绑定生成唯一的 failureProperties Bean
                .withPropertyValues(activeProperties)

                .run(context -> {
                    // 验证没有 Bean 冲突 (关键断言)
                    assertThat(context).hasSingleBean(FailureProperties.class);

                    // 验证自动配置类已加载
                    assertThat(context).hasSingleBean(FailFastAutoConfiguration.class);

                    // 获取并测试你的目标 Bean
                    DefaultExceptionHandler handler = context.getBean(DefaultExceptionHandler.class);
                    assertNotNull(handler);

                    // 可以在这里继续断言 handler 的行为
                });
    }

    @Test
    @DisplayName("测试自定义异常处理器覆盖默认处理器")
    void testDefaultFailFastExceptionHandlerWithCustomHandler() {
        // 1. 准备配置属性 (替代手动 new FailureProperties())
        String[] activeProperties = new String[] {
                "fail-fast.shadow-trace=true",
                "fail-fast.debug-snapshot=true",
                "fail-fast.method-validation-enabled=true",
                "fail-fast.i18n.default-locale=zh_CN"
                // 根据需要添加其他配置
        };

        new WebApplicationContextRunner()
                // ✅ 修正 1: 使用标准的 AutoConfigurations.of
                .withConfiguration(AutoConfigurations.of(FailFastAutoConfiguration.class))

                // ✅ 修正 2: 移除 .withBean(FailureProperties...)
                // 改用 withPropertyValues，让 @EnableConfigurationProperties 自动创建唯一的 Bean
                .withPropertyValues(activeProperties)

                // ✅ 保留: 这里可以手动 Mock 业务 Bean (FailFastExceptionHandler)，这不会冲突
                // 因为 FailFastAutoConfiguration 通常会用 @ConditionalOnMissingBean 来判断是否创建默认实现
                .withBean(FailFastExceptionHandler.class, () -> mock(FailFastExceptionHandler.class))

                .run(context -> {
                    // 验证 Properties Bean 只有一个 (确保修复成功)
                    assertThat(context).hasSingleBean(FailureProperties.class);

                    // 验证自定义 Bean 存在
                    assertThat(context).getBean(FailFastExceptionHandler.class).isNotNull();

                    // 你的原始断言逻辑：
                    // 如果自定义了 FailFastExceptionHandler，默认的 defaultFailFastExceptionHandler 应该不存在
                    // (前提是 FailFastAutoConfiguration 中有 @ConditionalOnMissingBean(FailFastExceptionHandler.class))
                    assertFalse(context.containsBean("defaultFailFastExceptionHandler"));

                    // 或者断言自定义的 Bean 被注入了
                    // FailFastExceptionHandler handler = context.getBean(FailFastExceptionHandler.class);
                    // assertTrue(Mockito.mockingDetails(handler).isMock());
                });
    }

    @Test
    @DisplayName("测试验证切面 ValidationAspect 加载")
    void testValidationAspect() {
        // 1. 准备配置属性 (替代手动 new FailureProperties())
        String[] activeProperties = new String[] {
                "fail-fast.shadow-trace=true",
                "fail-fast.debug-snapshot=true",
                "fail-fast.method-validation-enabled=true", // 确保开启方法验证，否则 Aspect 可能不生效
                "fail-fast.i18n.default-locale=zh_CN"
                // 根据需要添加其他配置
        };

        new WebApplicationContextRunner()
                // ✅ 修正 1: 使用 AutoConfigurations.of 包裹自动配置类
                .withConfiguration(AutoConfigurations.of(FailFastAutoConfiguration.class))

                // ✅ 修正 2: 【关键】彻底删除 .withBean(FailureProperties.class, ...)
                // 改用 withPropertyValues，让 Spring 自动绑定生成唯一的 failureProperties Bean
                .withPropertyValues(activeProperties)

                .run(context -> {
                    // 验证 Properties Bean 只有一个 (确保修复成功)
                    assertThat(context).hasSingleBean(FailureProperties.class);

                    // 验证自动配置类已加载
                    assertThat(context).hasSingleBean(FailFastAutoConfiguration.class);

                    // 你的原始断言逻辑：获取并验证 Aspect
                    ValidationAspect aspect = context.getBean(ValidationAspect.class);
                    assertNotNull(aspect);

                    // 可选：验证 Aspect 是否被正确代理
//                     assertTrue(AopUtils.isAopProxy(aspect));
                });
    }


    @Test
    @DisplayName("测试方法验证后置处理器 MethodValidationPostProcessor 加载")
    void testMethodValidationPostProcessor() {
        String[] activeProperties = new String[] {
                "fail-fast.method-validation-enabled=true",
                "fail-fast.shadow-trace=true",
                "fail-fast.debug-snapshot=true"
        };

        new WebApplicationContextRunner()
                // ✅ 修正关键：同时加载 FailFast 和 Spring 原生的 Validation 自动配置
                // 这样 MethodValidationPostProcessor 和 Validator 才会被自动创建
                .withConfiguration(
                        AutoConfigurations.of(
                                FailFastAutoConfiguration.class,
                                ValidationAutoConfiguration.class // <--- 添加这一行
                        )
                )
                .withPropertyValues(activeProperties)

                // 如果 ValidationAutoConfiguration 创建了默认的 Validator，
                // 而你之前想 Mock 它，这里可以保留 .withBean(Validator.class, ...)
                // 但通常测试 PostProcessor 的存在性不需要 Mock Validator，除非你要测试特定行为
                // .withBean(Validator.class, () -> validator)

                .run(context -> {
                    // 1. 验证 Properties Bean 唯一
                    assertThat(context).hasSingleBean(FailureProperties.class);

                    // 2. 验证 MethodValidationPostProcessor 存在 (现在应该能找到了)
                    assertThat(context).hasSingleBean(MethodValidationPostProcessor.class);

                    // 3. 获取并断言
                    MethodValidationPostProcessor processor = context.getBean(MethodValidationPostProcessor.class);
                    assertNotNull(processor);

                    // 可选：如果你之前 mock 了 validator，可以验证 processor 是否使用了它
                    // Validator validatorInProcessor = processor.getValidator(); // 取决于是否有 getter
                });
    }

    @Test
    @DisplayName("FailFastAutoConfiguration 在存在 Validator Bean 时应创建 MethodValidationPostProcessor")
    void testMethodValidationPostProcessorCreatedByFailFastAutoConfiguration() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(FailFastAutoConfiguration.class))
                .withPropertyValues(
                        "fail-fast.method-validation-enabled=true",
                        "fail-fast.shadow-trace=true",
                        "fail-fast.debug-snapshot=true"
                )
                .withBean(Validator.class, () -> mock(Validator.class))
                .run(context -> assertThat(context).hasSingleBean(MethodValidationPostProcessor.class));
    }

    @Test
    void testExInitializer() {
        // 测试ExInitializer内部类
        com.chao.failfast.spi.SkipPrefixRegistry skipPrefixRegistry = mock(com.chao.failfast.spi.SkipPrefixRegistry.class);
        com.chao.failfast.spi.SkipTypeRegistry skipTypeRegistry = mock(com.chao.failfast.spi.SkipTypeRegistry.class);
        FailFastAutoConfiguration.ExInitializer initializer = configuration.new ExInitializer(context, validator, skipPrefixRegistry, skipTypeRegistry);
        assertNotNull(initializer);
        // 验证Ex.setContext被调用
        // 验证Chain.setValidator被调用
        // 验证Chain.setFailureProperties被调用
    }

    @Test
    void testExInitializerWithoutValidator() {
        // 测试ExInitializer内部类（无validator）
        com.chao.failfast.spi.SkipPrefixRegistry skipPrefixRegistry = mock(com.chao.failfast.spi.SkipPrefixRegistry.class);
        com.chao.failfast.spi.SkipTypeRegistry skipTypeRegistry = mock(com.chao.failfast.spi.SkipTypeRegistry.class);
        FailFastAutoConfiguration.ExInitializer initializer = configuration.new ExInitializer(context, null, skipPrefixRegistry, skipTypeRegistry);
        assertNotNull(initializer);
        // 验证Ex.setContext被调用
        // 验证Chain.setFailureProperties被调用
    }

    @Test
    void testFailFastCleanupFilter() {
        // 测试FailFastCleanupFilter内部类
        FailFastAutoConfiguration.FailFastCleanupFilter filter = new FailFastAutoConfiguration.FailFastCleanupFilter(context);
        assertNotNull(filter);

        try {
            filter.doFilter(mock(HttpServletRequest.class), mock(HttpServletResponse.class), filterChain);
            verify(filterChain).doFilter(any(), any());
            verify(context).clearThreadContext();
        } catch (IOException | ServletException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFailFastTraceIdFilterWithHttpRequest() {
        // 测试FailFastTraceIdFilter内部类（HttpRequest）
        FailureProperties.TraceId traceIdConfig = mock(FailureProperties.TraceId.class);
        when(traceIdConfig.getHeaderName()).thenReturn("X-Trace-Id");
        when(traceIdConfig.isGenerateIfMissing()).thenReturn(true);
        when(traceIdConfig.isMdcEnabled()).thenReturn(true);
        when(traceIdConfig.getMdcKey()).thenReturn("traceId");
        when(traceIdConfig.isResponseHeader()).thenReturn(true);
        when(traceIdConfig.getResponseHeaderName()).thenReturn("X-Response-Trace-Id");
        when(properties.getTraceId()).thenReturn(traceIdConfig);

        FailFastAutoConfiguration.FailFastTraceIdFilter filter = new FailFastAutoConfiguration.FailFastTraceIdFilter(context, traceIdConfig);
        assertNotNull(filter);

        try {
            filter.doFilter(httpRequest, httpResponse, filterChain);
            verify(filterChain).doFilter(any(), any());
            verify(context).setTraceId(anyString());
            verify(httpResponse).setHeader(eq("X-Response-Trace-Id"), anyString());
        } catch (IOException | ServletException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFailFastTraceIdFilterWithHttpRequestAndExistingTraceId() {
        // 测试FailFastTraceIdFilter内部类（HttpRequest且已有traceId）
        String existingTraceId = "existing-trace-id";
        FailureProperties.TraceId traceIdConfig = mock(FailureProperties.TraceId.class);
        when(traceIdConfig.getHeaderName()).thenReturn("X-Trace-Id");
        when(traceIdConfig.isGenerateIfMissing()).thenReturn(true);
        when(traceIdConfig.isMdcEnabled()).thenReturn(true);
        when(traceIdConfig.getMdcKey()).thenReturn("traceId");
        when(traceIdConfig.isResponseHeader()).thenReturn(true);
        when(traceIdConfig.getResponseHeaderName()).thenReturn("X-Response-Trace-Id");
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        when(httpRequest.getHeader("X-Trace-Id")).thenReturn(existingTraceId);

        FailFastAutoConfiguration.FailFastTraceIdFilter filter = new FailFastAutoConfiguration.FailFastTraceIdFilter(context, traceIdConfig);
        assertNotNull(filter);

        try {
            filter.doFilter(httpRequest, httpResponse, filterChain);
            verify(filterChain).doFilter(any(), any());
            verify(context).setTraceId(existingTraceId);
            verify(httpResponse).setHeader(eq("X-Response-Trace-Id"), eq(existingTraceId));
        } catch (IOException | ServletException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFailFastTraceIdFilterWithNonHttpRequest() {
        // 测试FailFastTraceIdFilter内部类（非HttpRequest）
        FailureProperties.TraceId traceIdConfig = mock(FailureProperties.TraceId.class);
        when(properties.getTraceId()).thenReturn(traceIdConfig);

        FailFastAutoConfiguration.FailFastTraceIdFilter filter = new FailFastAutoConfiguration.FailFastTraceIdFilter(context, traceIdConfig);
        assertNotNull(filter);

        try {
            filter.doFilter(mock(jakarta.servlet.ServletRequest.class), mock(jakarta.servlet.ServletResponse.class), filterChain);
            verify(filterChain).doFilter(any(), any());
            verify(context, never()).setTraceId(anyString());
        } catch (IOException | ServletException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFailFastBodyArgumentResolverPostProcessor() {
        // 测试failFastBodyArgumentResolverPostProcessor静态方法
        BeanPostProcessor postProcessor = FailFastAutoConfiguration.failFastBodyArgumentResolverPostProcessor();
        assertNotNull(postProcessor);

        // 测试非RequestMappingHandlerAdapter
        Object nonAdapterBean = new Object();
        Object result = postProcessor.postProcessAfterInitialization(nonAdapterBean, "testBean");
        assertSame(nonAdapterBean, result);

        // 测试RequestMappingHandlerAdapter但无resolvers
        RequestMappingHandlerAdapter adapterWithoutResolvers = mock(RequestMappingHandlerAdapter.class);
        when(adapterWithoutResolvers.getArgumentResolvers()).thenReturn(null);
        result = postProcessor.postProcessAfterInitialization(adapterWithoutResolvers, "adapter");
        assertSame(adapterWithoutResolvers, result);

        // 测试RequestMappingHandlerAdapter已有OptionalBodyResolver
        RequestMappingHandlerAdapter adapterWithOptionalResolver = mock(RequestMappingHandlerAdapter.class);
        List<HandlerMethodArgumentResolver> resolversWithOptional = new ArrayList<>();
        resolversWithOptional.add(mock(OptionalBodyResolver.class));
        when(adapterWithOptionalResolver.getArgumentResolvers()).thenReturn(resolversWithOptional);
        result = postProcessor.postProcessAfterInitialization(adapterWithOptionalResolver, "adapter");
        assertSame(adapterWithOptionalResolver, result);

        // 测试RequestMappingHandlerAdapter添加OptionalBodyResolver
        RequestMappingHandlerAdapter adapter = mock(RequestMappingHandlerAdapter.class);
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        RequestResponseBodyMethodProcessor processor = mock(RequestResponseBodyMethodProcessor.class);
        resolvers.add(processor);
        when(adapter.getArgumentResolvers()).thenReturn(resolvers);
        result = postProcessor.postProcessAfterInitialization(adapter, "adapter");
        assertSame(adapter, result);
        verify(adapter).setArgumentResolvers(anyList());
    }

    @Test
    void testFailFastBodyArgumentResolverPostProcessorWithoutRequestResponseBodyMethodProcessor() {
        // 测试RequestMappingHandlerAdapter但无RequestResponseBodyMethodProcessor
        BeanPostProcessor postProcessor = FailFastAutoConfiguration.failFastBodyArgumentResolverPostProcessor();
        RequestMappingHandlerAdapter adapter = mock(RequestMappingHandlerAdapter.class);
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        resolvers.add(mock(HandlerMethodArgumentResolver.class));
        when(adapter.getArgumentResolvers()).thenReturn(resolvers);
        Object result = postProcessor.postProcessAfterInitialization(adapter, "adapter");
        assertSame(adapter, result);
        verify(adapter, never()).setArgumentResolvers(anyList());
    }

    @Test
    void testFailFastBodyArgumentResolverPostProcessorWithNullInputs() {
        BeanPostProcessor postProcessor = FailFastAutoConfiguration.failFastBodyArgumentResolverPostProcessor();
        assertThrows(NullPointerException.class, () -> postProcessor.postProcessAfterInitialization(null, "bean"));
        assertThrows(NullPointerException.class, () -> postProcessor.postProcessAfterInitialization(new Object(), null));
    }

    @Test
    void testFailFastTraceIdFilterWithEmptyHeaderName() {
        // 测试FailFastTraceIdFilter内部类（空headerName）
        FailureProperties.TraceId traceIdConfig = mock(FailureProperties.TraceId.class);
        when(traceIdConfig.getHeaderName()).thenReturn("");
        when(traceIdConfig.isGenerateIfMissing()).thenReturn(true);
        when(traceIdConfig.isMdcEnabled()).thenReturn(false);
        when(traceIdConfig.isResponseHeader()).thenReturn(false);
        when(properties.getTraceId()).thenReturn(traceIdConfig);

        FailFastAutoConfiguration.FailFastTraceIdFilter filter = new FailFastAutoConfiguration.FailFastTraceIdFilter(context, traceIdConfig);
        assertNotNull(filter);

        try {
            filter.doFilter(httpRequest, httpResponse, filterChain);
            verify(filterChain).doFilter(any(), any());
            verify(context).setTraceId(anyString());
        } catch (IOException | ServletException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFailFastTraceIdFilterWithoutGenerateIfMissing() {
        // 测试FailFastTraceIdFilter内部类（不生成traceId）
        FailureProperties.TraceId traceIdConfig = mock(FailureProperties.TraceId.class);
        when(traceIdConfig.getHeaderName()).thenReturn("X-Trace-Id");
        when(traceIdConfig.isGenerateIfMissing()).thenReturn(false);
        when(traceIdConfig.isMdcEnabled()).thenReturn(false);
        when(traceIdConfig.isResponseHeader()).thenReturn(false);
        when(properties.getTraceId()).thenReturn(traceIdConfig);
        when(httpRequest.getHeader("X-Trace-Id")).thenReturn(null);

        FailFastAutoConfiguration.FailFastTraceIdFilter filter = new FailFastAutoConfiguration.FailFastTraceIdFilter(context, traceIdConfig);
        assertNotNull(filter);

        try {
            filter.doFilter(httpRequest, httpResponse, filterChain);
            verify(filterChain).doFilter(any(), any());
            verify(context, never()).setTraceId(anyString());
        } catch (IOException | ServletException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFailFastTraceIdFilterWithoutMdc() {
        // 测试FailFastTraceIdFilter内部类（不使用MDC）
        FailureProperties.TraceId traceIdConfig = mock(FailureProperties.TraceId.class);
        when(traceIdConfig.getHeaderName()).thenReturn("X-Trace-Id");
        when(traceIdConfig.isGenerateIfMissing()).thenReturn(true);
        when(traceIdConfig.isMdcEnabled()).thenReturn(false);
        when(traceIdConfig.isResponseHeader()).thenReturn(false);
        when(properties.getTraceId()).thenReturn(traceIdConfig);

        FailFastAutoConfiguration.FailFastTraceIdFilter filter = new FailFastAutoConfiguration.FailFastTraceIdFilter(context, traceIdConfig);
        assertNotNull(filter);

        try {
            filter.doFilter(httpRequest, httpResponse, filterChain);
            verify(filterChain).doFilter(any(), any());
            verify(context).setTraceId(anyString());
        } catch (IOException | ServletException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFailFastTraceIdFilterWithoutResponseHeader() {
        // 测试FailFastTraceIdFilter内部类（不设置响应头）
        FailureProperties.TraceId traceIdConfig = mock(FailureProperties.TraceId.class);
        when(traceIdConfig.getHeaderName()).thenReturn("X-Trace-Id");
        when(traceIdConfig.isGenerateIfMissing()).thenReturn(true);
        when(traceIdConfig.isMdcEnabled()).thenReturn(false);
        when(traceIdConfig.isResponseHeader()).thenReturn(false);
        when(properties.getTraceId()).thenReturn(traceIdConfig);

        FailFastAutoConfiguration.FailFastTraceIdFilter filter = new FailFastAutoConfiguration.FailFastTraceIdFilter(context, traceIdConfig);
        assertNotNull(filter);

        try {
            filter.doFilter(httpRequest, httpResponse, filterChain);
            verify(filterChain).doFilter(any(), any());
            verify(context).setTraceId(anyString());
            verify(httpResponse, never()).setHeader(anyString(), anyString());
        } catch (IOException | ServletException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFailFastTraceIdFilterWithEmptyResponseHeaderName() {
        // 测试FailFastTraceIdFilter内部类（空响应头名称）
        FailureProperties.TraceId traceIdConfig = mock(FailureProperties.TraceId.class);
        when(traceIdConfig.getHeaderName()).thenReturn("X-Trace-Id");
        when(traceIdConfig.isGenerateIfMissing()).thenReturn(true);
        when(traceIdConfig.isMdcEnabled()).thenReturn(false);
        when(traceIdConfig.isResponseHeader()).thenReturn(true);
        when(traceIdConfig.getResponseHeaderName()).thenReturn("");
        when(properties.getTraceId()).thenReturn(traceIdConfig);

        FailFastAutoConfiguration.FailFastTraceIdFilter filter = new FailFastAutoConfiguration.FailFastTraceIdFilter(context, traceIdConfig);
        assertNotNull(filter);

        try {
            filter.doFilter(httpRequest, httpResponse, filterChain);
            verify(filterChain).doFilter(any(), any());
            verify(context).setTraceId(anyString());
            verify(httpResponse).setHeader(eq("X-Trace-Id"), anyString());
        } catch (IOException | ServletException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFailFastTraceIdFilterWithResponseHeaderButNonHttpResponse() {
        FailureProperties.TraceId traceIdConfig = mock(FailureProperties.TraceId.class);
        when(traceIdConfig.getHeaderName()).thenReturn("X-Trace-Id");
        when(traceIdConfig.isGenerateIfMissing()).thenReturn(true);
        when(traceIdConfig.isMdcEnabled()).thenReturn(false);
        when(traceIdConfig.isResponseHeader()).thenReturn(true);
        when(traceIdConfig.getResponseHeaderName()).thenReturn("X-Response-Trace-Id");

        FailFastAutoConfiguration.FailFastTraceIdFilter filter = new FailFastAutoConfiguration.FailFastTraceIdFilter(context, traceIdConfig);

        try {
            filter.doFilter(httpRequest, mock(jakarta.servlet.ServletResponse.class), filterChain);
            verify(filterChain).doFilter(any(), any());
            verify(context).setTraceId(anyString());
        } catch (IOException | ServletException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFailFastTraceIdFilterWithMdcEnabledButBlankKey() {
        FailureProperties.TraceId traceIdConfig = mock(FailureProperties.TraceId.class);
        when(traceIdConfig.getHeaderName()).thenReturn("X-Trace-Id");
        when(traceIdConfig.isGenerateIfMissing()).thenReturn(true);
        when(traceIdConfig.isMdcEnabled()).thenReturn(true);
        when(traceIdConfig.getMdcKey()).thenReturn("");
        when(traceIdConfig.isResponseHeader()).thenReturn(false);

        FailFastAutoConfiguration.FailFastTraceIdFilter filter = new FailFastAutoConfiguration.FailFastTraceIdFilter(context, traceIdConfig);

        try {
            filter.doFilter(httpRequest, httpResponse, filterChain);
            verify(filterChain).doFilter(any(), any());
            verify(context).setTraceId(anyString());
            verify(httpResponse, never()).setHeader(anyString(), anyString());
        } catch (IOException | ServletException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFailFastTraceIdFilterWithBlankHeaderNameAndBlankResponseHeaderName() {
        FailureProperties.TraceId traceIdConfig = mock(FailureProperties.TraceId.class);
        when(traceIdConfig.getHeaderName()).thenReturn("");
        when(traceIdConfig.isGenerateIfMissing()).thenReturn(true);
        when(traceIdConfig.isMdcEnabled()).thenReturn(false);
        when(traceIdConfig.isResponseHeader()).thenReturn(true);
        when(traceIdConfig.getResponseHeaderName()).thenReturn("");

        FailFastAutoConfiguration.FailFastTraceIdFilter filter = new FailFastAutoConfiguration.FailFastTraceIdFilter(context, traceIdConfig);

        try {
            filter.doFilter(httpRequest, httpResponse, filterChain);
            verify(filterChain).doFilter(any(), any());
            verify(context).setTraceId(anyString());
            verify(httpResponse, never()).setHeader(anyString(), anyString());
        } catch (IOException | ServletException e) {
            fail(e.getMessage());
        }
    }
}
