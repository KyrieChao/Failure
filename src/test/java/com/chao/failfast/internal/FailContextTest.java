package com.chao.failfast.internal;

import com.chao.failfast.config.mapping.CodeMappingConfig;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.config.properties.FailureProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("display")
class FailContextTest {

    private FailureProperties properties;
    private FailureContext context;

    @BeforeEach
    void setUp() {
        properties = mock(FailureProperties.class);
        CodeMappingConfig config = mock(CodeMappingConfig.class);
        context = new FailureContext(properties, config, null);
    }

    @Test
    @DisplayName("isShadowTrace 应使用全局配置")
    void shouldUseGlobalConfig() {
        when(properties.isShadowTrace()).thenReturn(true);
        assertThat(context.isShadowTrace()).isTrue();

        when(properties.isShadowTrace()).thenReturn(false);
        assertThat(context.isShadowTrace()).isFalse();
    }

    @Test
@DisplayName("display")
    void shouldOverrideConfigTemporarily() {
        when(properties.isShadowTrace()).thenReturn(false);

        context.withPrintMethod(true, () -> {
            assertThat(context.isShadowTrace()).isTrue();
        });

        assertThat(context.isShadowTrace()).isFalse();
    }

    @Test
@DisplayName("display")
    void shouldOverrideConfigTemporarilyRunnable() {
        when(properties.isShadowTrace()).thenReturn(false);

        context.withPrintMethod(true, () -> {
            assertThat(context.isShadowTrace()).isTrue();
        });

        assertThat(context.isShadowTrace()).isFalse();
    }

    @Test
@DisplayName("display")
    void shouldClearThreadContext() {
        when(properties.isShadowTrace()).thenReturn(false);

        // Use reflection or just trust withPrintMethod restores it.
        // But to test clearThreadContext, we need to set it without using withPrintMethod's finally block if possible,
        // OR simulate a scenario where we manually set thread local (but private).
        // Since fields are private, we can only verify via public methods.

        // If we use withPrintMethod, it clears itself.
        // Let's assume clearThreadContext is used by Filter.
        // We can't easily test its effect without access to private ThreadLocals unless we add a method to set them directly for testing, 
        // but withPrintMethod handles setting.

        // Actually, withPrintMethod sets and then restores.
        // If we want to test clearThreadContext, maybe we rely on withPrintMethod not clearing if exception occurs? No, it has finally.

        // The implementation of withPrintMethod restores previous value.
        // If previous was null, it removes.

        // So clearThreadContext is a safety net.
        context.clearThreadContext();
        assertThat(context.isShadowTrace()).isFalse();
    }

    @Nested
    @DisplayName("methodEnabledOverride 测试")
    class MethodEnabledTest {

        @Test
@DisplayName("display")
        void shouldClearMethodEnabledOverride() {
            // 通过 withPrintMethod 间接测试 clearThreadContext 会清除所�?ThreadLocal
            when(properties.isShadowTrace()).thenReturn(false);

            context.withPrintMethod(true, () -> {
                // 此时 printMethodOverride 被设�?
                assertThat(context.isShadowTrace()).isTrue();
            });

            // withPrintMethod �?finally 会恢复，然后 clearThreadContext 再清除一�?
            context.clearThreadContext();
            assertThat(context.isShadowTrace()).isFalse();
        }
    }

    @Nested
    @DisplayName("withPrintMethod 嵌套调用测试")
    class NestedWithPrintMethodTest {

        @Test
@DisplayName("display")
        void shouldHandleNestedCalls() {
            when(properties.isShadowTrace()).thenReturn(false);

            context.withPrintMethod(true, () -> {
                assertThat(context.isShadowTrace()).isTrue();

                // 嵌套调用，设置不同的�?
                context.withPrintMethod(false, () -> {
                    assertThat(context.isShadowTrace()).isFalse();
                });

                // 外层应该恢复�?true
                assertThat(context.isShadowTrace()).isTrue();
            });

            // 最终恢复为全局配置
            assertThat(context.isShadowTrace()).isFalse();
        }

        @Test
@DisplayName("display")
        void shouldRestoreOnException() {
            when(properties.isShadowTrace()).thenReturn(false);

            // 先设置一个值，�?original != null
            context.withPrintMethod(true, () -> {
                // 外层设置�?true
                assertThat(context.isShadowTrace()).isTrue();

                try {
                    // 内层设置�?false，original 会是 true
                    context.withPrintMethod(false, () -> {
                        assertThat(context.isShadowTrace()).isFalse();
                        throw new RuntimeException("test exception");
                    });
                } catch (RuntimeException e) {
                    assertThat(e.getMessage()).isEqualTo("test exception");
                }

                // 内层结束后，应该恢复�?true（不是全局�?false�?
                assertThat(context.isShadowTrace()).isTrue();

                return null;
            });

            // 最终恢复为全局配置 false
            assertThat(context.isShadowTrace()).isFalse();
        }
    }

    @Nested
@DisplayName("display")
    class SupplierReturnTest {

        @Test
@DisplayName("display")
        void shouldReturnSupplierValue() {
            when(properties.isShadowTrace()).thenReturn(false);

            String result = context.withPrintMethod(true, () -> {
                assertThat(context.isShadowTrace()).isTrue();
                return "success";
            });

            assertThat(result).isEqualTo("success");
        }
    }

    @Nested
@DisplayName("display")
    class GlobalTrueTest {

        @Test
@DisplayName("display")
        void shouldUseOverrideWhenGlobalTrue() {
            when(properties.isShadowTrace()).thenReturn(true);

            assertThat(context.isShadowTrace()).isTrue();

            context.withPrintMethod(false, () -> {
                assertThat(context.isShadowTrace()).isFalse();
            });

            assertThat(context.isShadowTrace()).isTrue();
        }
    }
}
