package com.chao.failfast.internal.core;

import com.chao.failfast.config.mapping.CodeMappingConfig;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.constant.Severity;
import com.chao.failfast.internal.policy.ErrorPolicy;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FailureContextTest {

    @Test
    void testConstructorWithErrorPolicy() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        ErrorPolicy errorPolicy = Mockito.mock(ErrorPolicy.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, errorPolicy);
        assertSame(codeMappingConfig, context.getCodeMappingConfig());
        assertSame(errorPolicy, context.getErrorPolicy());
    }

    @Test
    void testConstructorWithNullErrorPolicy() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        assertSame(codeMappingConfig, context.getCodeMappingConfig());
        assertNotNull(context.getErrorPolicy());
    }

    @Test
    void testIsShadowTraceWithOverride() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        when(properties.isShadowTrace()).thenReturn(false);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        context.setShadowTrace(true);
        assertTrue(context.isShadowTrace());
    }

    @Test
    void testIsShadowTraceWithoutOverride() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        when(properties.isShadowTrace()).thenReturn(true);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        assertTrue(context.isShadowTrace());
    }

    @Test
    void testIsTrimStackTrace() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        when(properties.isTrimStackTrace()).thenReturn(true);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        assertTrue(context.isTrimStackTrace());
    }

    @Test
    void testIsMethodEnabledWithOverride() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        when(properties.isMethodValidationEnabled()).thenReturn(false);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        context.setMethodEnabled(true);
        assertTrue(context.isMethodEnabled());
    }

    @Test
    void testIsMethodEnabledWithoutOverride() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        when(properties.isMethodValidationEnabled()).thenReturn(true);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        assertTrue(context.isMethodEnabled());
    }

    @Test
    void testIsDebugSnapshot() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        when(properties.isDebugSnapshot()).thenReturn(true);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        assertTrue(context.isDebugSnapshot());
    }

    @Test
    void testIsReactiveContextFirst() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        FailureProperties.Reactive reactive = Mockito.mock(FailureProperties.Reactive.class);
        when(properties.getReactive()).thenReturn(reactive);
        when(reactive.isContextFirst()).thenReturn(true);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        assertTrue(context.isReactiveContextFirst());
    }

    @Test
    void testIsReactiveContextFirstWithNullReactive() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        when(properties.getReactive()).thenReturn(null);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        assertFalse(context.isReactiveContextFirst());
    }

    @Test
    void testClearThreadContext() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        context.setTraceId("test-trace-id");
        context.setScene("test-scene");
        context.setMethodEnabled(true);
        context.setShadowTrace(true);
        context.clearThreadContext();
        assertNull(context.getTraceId());
        assertEquals("DEFAULT", context.getScene());
    }

    @Test
    void testTraceId() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        context.setTraceId("test-trace-id");
        assertEquals("test-trace-id", context.getTraceId());
    }

    @Test
    void testScene() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        context.setScene("test-scene");
        assertEquals("test-scene", context.getScene());
    }

    @Test
    void testSetMethodEnabledWithNull() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        context.setMethodEnabled(true);
        context.setMethodEnabled(null);
        // 应该使用默认值
    }

    @Test
    void testSetShadowTraceWithNull() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        context.setShadowTrace(true);
        context.setShadowTrace(null);
        // 应该使用默认值
    }

    @Test
    void testWithPrintMethodWithRunnable() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        boolean[] executed = {false};
        context.withPrintMethod(true, () -> { executed[0] = true; });
        assertTrue(executed[0]);
    }

    @Test
    void testWithPrintMethodWithSupplier() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        String result = context.withPrintMethod(true, () -> "test");
        assertEquals("test", result);
    }

    @Test
    void testWithPrintMethodWithMono() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        Mono<String> mono = context.withPrintMethod(true, () -> Mono.just("test"));
        assertNotNull(mono);
    }

    @Test
    void testWithPrintMethodWithFlux() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        Flux<String> flux = context.withPrintMethod(true, () -> Flux.just("test"));
        assertNotNull(flux);
    }

    @Test
    void testWithMethodEnabledWithSupplier() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        String result = context.withMethodEnabled(true, () -> "test");
        assertEquals("test", result);
    }

    @Test
    void testWithMethodEnabledWithMono() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        Mono<String> mono = context.withMethodEnabled(true, () -> Mono.just("test"));
        assertNotNull(mono);
    }

    @Test
    void testWithMethodEnabledWithFlux() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        Flux<String> flux = context.withMethodEnabled(true, () -> Flux.just("test"));
        assertNotNull(flux);
    }

    @Test
    void testGetStrictMaxErrorsFromProperties() {
        FailureProperties properties = new FailureProperties();
        properties.getChain().setMaxErrors(12);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        assertEquals(12, context.getStrictMaxErrors());
    }

    @Test
    void testWithThreadScopeShouldRestoreSnapshot() {
        FailureProperties properties = new FailureProperties();
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        context.setTraceId("origin");
        context.setScene("origin-scene");

        context.withThreadScope("tmp-trace", "tmp-scene", () -> {
            assertEquals("tmp-trace", context.getTraceId());
            assertEquals("tmp-scene", context.getScene());
        });

        assertEquals("origin", context.getTraceId());
        assertEquals("origin-scene", context.getScene());
    }

    @Test
    void testResolveSeverityDefaultAndMapping() {
        FailureProperties properties = new FailureProperties();
        properties.getLogging().setDefaultSeverity("WARNING");
        properties.getLogging().getSeverityMapping().put("40010", "DEBUG");
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);

        assertEquals(Severity.DEBUG, context.resolveSeverity(ResponseCode.of(40010, "M")));
        assertEquals(Severity.WARNING, context.resolveSeverity(ResponseCode.of(50010, "M")));
    }

    @Test
    void should_returnDefaultStrictMaxErrors_when_chainConfigIsMissingOrNonPositive() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        when(properties.getChain()).thenReturn(null);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);

        assertEquals(50, context.getStrictMaxErrors());
    }

    @Test
    void should_resolveInfoSeverity_when_loggingConfigIsMissing() {
        FailureProperties properties = Mockito.mock(FailureProperties.class);
        when(properties.getLogging()).thenReturn(null);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);

        assertEquals(Severity.INFO, context.resolveSeverity(ResponseCode.of(40010, "M")));
    }

    @Test
    void should_useFallbackSeverity_when_codeIsNullOrMappingIsEmpty() {
        FailureProperties properties = new FailureProperties();
        properties.getLogging().setDefaultSeverity("ERROR");
        properties.getLogging().setSeverityMapping(null);
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);

        assertEquals(Severity.ERROR, context.resolveSeverity(null));
        assertEquals(Severity.ERROR, context.resolveSeverity(ResponseCode.of(40010, "M")));
    }

    @Test
    void should_restoreThreadLocals_when_scopeIsClosedTwice() {
        FailureProperties properties = new FailureProperties();
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        context.setTraceId("origin-trace");
        context.setScene("origin-scene");
        context.setMethodEnabled(true);
        context.setShadowTrace(true);

        FailureContext.ContextScope scope = context.openScope("scoped-trace", "scoped-scene", false, false);
        assertEquals("scoped-trace", context.getTraceId());
        assertEquals("scoped-scene", context.getScene());
        assertFalse(context.isMethodEnabled());
        assertFalse(context.isShadowTrace());

        scope.close();
        scope.close();

        assertEquals("origin-trace", context.getTraceId());
        assertEquals("origin-scene", context.getScene());
        assertTrue(context.isMethodEnabled());
        assertTrue(context.isShadowTrace());
    }

    @Test
    void should_removeTraceIdAndRestoreSnapshot_when_scopedTraceIdIsNull() {
        FailureProperties properties = new FailureProperties();
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        context.setTraceId("origin-trace");
        context.setScene("origin-scene");

        try (FailureContext.ContextScope ignored = context.openScope(null, null, null, null)) {
            assertNull(context.getTraceId());
            assertEquals("origin-scene", context.getScene());
        }

        assertEquals("origin-trace", context.getTraceId());
        assertEquals("origin-scene", context.getScene());
    }

    @Test
    void should_executeRunnableAndRestoreScope_when_withThreadScopeRunnableInvoked() {
        FailureProperties properties = new FailureProperties();
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        context.setTraceId("origin-trace");
        context.setScene("origin-scene");

        context.withThreadScope("new-trace", "new-scene", () -> {
            assertEquals("new-trace", context.getTraceId());
            assertEquals("new-scene", context.getScene());
        });

        assertEquals("origin-trace", context.getTraceId());
        assertEquals("origin-scene", context.getScene());
    }

    @Test
    void should_createAndRestoreSnapshot_when_openScopeWithoutOverridesInvoked() {
        FailureProperties properties = new FailureProperties();
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);

        try (FailureContext.ContextScope ignored = context.openScope()) {
            assertNull(context.getTraceId());
            assertEquals("DEFAULT", context.getScene());
        }

        assertNull(context.getTraceId());
        assertEquals("DEFAULT", context.getScene());
    }

    @Test
    void should_returnSupplierValueAndRestoreSnapshot_when_withThreadScopeSupplierInvoked() {
        FailureProperties properties = new FailureProperties();
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        context.setTraceId("origin-trace");
        context.setScene("origin-scene");

        String result = context.withThreadScope("temp-trace", "temp-scene", () -> {
            assertEquals("temp-trace", context.getTraceId());
            assertEquals("temp-scene", context.getScene());
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals("origin-trace", context.getTraceId());
        assertEquals("origin-scene", context.getScene());
    }

    @Test
    void should_removeNullSceneAndRestoreSnapshot_when_scopeCloses() {
        FailureProperties properties = new FailureProperties();
        CodeMappingConfig codeMappingConfig = Mockito.mock(CodeMappingConfig.class);
        FailureContext context = new FailureContext(properties, codeMappingConfig, null);
        context.setScene(null);

        try (FailureContext.ContextScope ignored = context.openScope("trace-x", "scene-x", null, null)) {
            assertEquals("scene-x", context.getScene());
        }

        assertEquals("DEFAULT", context.getScene());
    }
}
