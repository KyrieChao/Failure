package com.chao.failure.exception;

import com.chao.failure.config.mapping.CodeMappingConfig;
import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.constant.Severity;
import com.chao.failure.internal.core.Ex;
import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.spi.filter.SkipPrefixRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class BusinessTrimStackTraceTest {

    @AfterEach
    void tearDown() {
        Ex.setContext(null);
        Ex.setSkipPrefixRegistry(null);
    }

    @Test
    void materializeTrimsStackTraceWhenEnabled() {
        FailureProperties props = new FailureProperties();
        props.setShadowTrace(true);
        props.setTrimStackTrace(true);
        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(ctx);

        SkipPrefixRegistry registry = mock(SkipPrefixRegistry.class);
        when(registry.add(anyString())).thenReturn(registry);
        when(registry.shouldSkip(anyString())).thenAnswer(inv -> {
            String cls = inv.getArgument(0, String.class);
            return cls != null && cls.startsWith("com.chao.failfast.exception.Business");
        });
        Ex.setSkipPrefixRegistry(registry);

        Business b = Business.compose()
                .responseCode(ResponseCode.VALIDATION_ERROR_400)
                .detail("x")
                .materialize();

        assertThat(b.getStackTrace()).isNotEmpty();
        assertThat(b.getStackTrace()[0].getClassName()).doesNotStartWith("com.chao.failfast.exception.Business");
    }

    @Test
    void trimStackTraceCoversNullEmptyAndRegistryBranches() throws Exception {
        Method m = Business.class.getDeclaredMethod("trimStackTrace", StackTraceElement[].class);
        m.setAccessible(true);

        Ex.setSkipPrefixRegistry(null);
        assertThat(m.invoke(null, new Object[]{null})).isNull();

        StackTraceElement[] empty = new StackTraceElement[0];
        assertThat(m.invoke(null, new Object[]{empty})).isSameAs(empty);

        StackTraceElement[] stack = new StackTraceElement[]{
                new StackTraceElement("A", "m", "A.java", 1),
                new StackTraceElement("B", "m", "B.java", 1)
        };
        Ex.setSkipPrefixRegistry(null);
        assertThat(m.invoke(null, new Object[]{stack})).isSameAs(stack);

        SkipPrefixRegistry noneSkip = mock(SkipPrefixRegistry.class);
        when(noneSkip.shouldSkip(anyString())).thenReturn(false);
        Ex.setSkipPrefixRegistry(noneSkip);
        assertThat(m.invoke(null, new Object[]{stack})).isSameAs(stack);

        SkipPrefixRegistry allSkip = mock(SkipPrefixRegistry.class);
        when(allSkip.shouldSkip(anyString())).thenReturn(true);
        Ex.setSkipPrefixRegistry(allSkip);
        assertThat(m.invoke(null, new Object[]{stack})).isSameAs(stack);
    }

    @Test
    void should_returnTrue_when_shouldFillStackTraceInvokedWithNullCodeAndSeverity() throws Exception {
        Method method = Business.class.getDeclaredMethod("shouldFillStackTrace", ResponseCode.class, Severity.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, null, null);

        assertThat(result).isTrue();
    }

    @Test
    void should_returnNull_when_firstNonBlankReceivesOnlyBlankValues() throws Exception {
        Method method = Business.class.getDeclaredMethod("firstNonBlank", String[].class);
        method.setAccessible(true);

        Object result = method.invoke(null, (Object) new String[]{" ", "\t", ""});

        assertThat(result).isNull();
    }

    @Test
    void should_returnFalse_when_shouldFillStackTraceHasCodeButNoContext() throws Exception {
        Method method = Business.class.getDeclaredMethod("shouldFillStackTrace", ResponseCode.class, Severity.class);
        method.setAccessible(true);

        try (var ex = mockStatic(Ex.class)) {
            ex.when(Ex::getContext).thenReturn(null);

            boolean result = (boolean) method.invoke(null, ResponseCode.VALIDATION_ERROR_400, null);

            assertThat(result).isFalse();
        }
    }

    @Test
    void should_returnNull_when_firstNonBlankReceivesNullArray() throws Exception {
        Method method = Business.class.getDeclaredMethod("firstNonBlank", String[].class);
        method.setAccessible(true);

        Object result = method.invoke(null, new Object[]{null});

        assertThat(result).isNull();
    }
}

