package com.chao.failure.internal.core;

import com.chao.failure.config.mapping.CodeMappingConfig;
import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.integration.webflux.ReactiveTrace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import static org.assertj.core.api.Assertions.assertThat;

class FailureContextReactiveContextTest {

    private final FailureProperties props = new FailureProperties();
    private final FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);

    @AfterEach
    void tearDown() {
        ctx.clearThreadContext();
    }

    @Test
    void withPrintMethodAddsReactiveContextForMono() {
        ctx.setTraceId("t1");
        ctx.setScene("CREATE");

        Mono<ContextView> mono = ctx.withPrintMethod(true, () -> Mono.deferContextual(Mono::just));
        ContextView view = mono.block();

        assertThat((Boolean) view.get(ReactiveTrace.SHADOW_TRACE_KEY)).isEqualTo(true);
        assertThat((String) view.get(ReactiveTrace.TRACE_ID_KEY)).isEqualTo("t1");
        assertThat((String) view.get(ReactiveTrace.SCENE_KEY)).isEqualTo("CREATE");
    }

    @Test
    void withPrintMethodAddsReactiveContextForFlux() {
        ctx.setTraceId("t1");
        ctx.setScene("CREATE");

        Flux<ContextView> flux = ctx.withPrintMethod(true, () -> Flux.deferContextual(Flux::just));
        ContextView view = flux.blockFirst();

        assertThat((Boolean) view.get(ReactiveTrace.SHADOW_TRACE_KEY)).isEqualTo(true);
        assertThat((String) view.get(ReactiveTrace.TRACE_ID_KEY)).isEqualTo("t1");
        assertThat((String) view.get(ReactiveTrace.SCENE_KEY)).isEqualTo("CREATE");
    }

    @Test
    void withMethodEnabledRestoresOriginalOverrideWhenAlreadySet() {
        ctx.setMethodEnabled(true);
        String out = ctx.withMethodEnabled(false, () -> "ok");
        assertThat(out).isEqualTo("ok");
        assertThat(ctx.isMethodEnabled()).isTrue();
    }

    @Test
    void withMethodEnabledAddsReactiveContextForMono() {
        ctx.setTraceId("t1");
        ctx.setScene("CREATE");

        Mono<ContextView> mono = ctx.withMethodEnabled(true, () -> Mono.deferContextual(Mono::just));
        ContextView view = mono.block();

        assertThat((Boolean) view.get(ReactiveTrace.METHOD_ENABLED_KEY)).isEqualTo(true);
        assertThat((String) view.get(ReactiveTrace.TRACE_ID_KEY)).isEqualTo("t1");
        assertThat((String) view.get(ReactiveTrace.SCENE_KEY)).isEqualTo("CREATE");
    }

    @Test
    void withMethodEnabledAddsReactiveContextForFlux() {
        ctx.setTraceId("t1");
        ctx.setScene("CREATE");

        Flux<ContextView> flux = ctx.withMethodEnabled(true, () -> Flux.deferContextual(Flux::just));
        ContextView view = flux.blockFirst();

        assertThat((Boolean) view.get(ReactiveTrace.METHOD_ENABLED_KEY)).isEqualTo(true);
        assertThat((String) view.get(ReactiveTrace.TRACE_ID_KEY)).isEqualTo("t1");
        assertThat((String) view.get(ReactiveTrace.SCENE_KEY)).isEqualTo("CREATE");
    }

    @Test
    void withPrintMethodSkipsBlankTraceIdAndScene() {
        ctx.setTraceId(" ");
        ctx.setScene("");

        Mono<ContextView> mono = ctx.withPrintMethod(true, () -> Mono.deferContextual(Mono::just));
        ContextView view = mono.block();

        assertThat((Boolean) view.get(ReactiveTrace.SHADOW_TRACE_KEY)).isEqualTo(true);
        assertThat(view.hasKey(ReactiveTrace.TRACE_ID_KEY)).isFalse();
        assertThat(view.hasKey(ReactiveTrace.SCENE_KEY)).isFalse();
    }

    @Test
    void withMethodEnabledSkipsBlankTraceIdAndScene() {
        ctx.setTraceId(" ");
        ctx.setScene("");

        Mono<ContextView> mono = ctx.withMethodEnabled(true, () -> Mono.deferContextual(Mono::just));
        ContextView view = mono.block();

        assertThat((Boolean) view.get(ReactiveTrace.METHOD_ENABLED_KEY)).isEqualTo(true);
        assertThat(view.hasKey(ReactiveTrace.TRACE_ID_KEY)).isFalse();
        assertThat(view.hasKey(ReactiveTrace.SCENE_KEY)).isFalse();
    }

    @Test
    void isReactiveContextFirstCoversTrueAndFalse() {
        props.getReactive().setContextFirst(false);
        assertThat(ctx.isReactiveContextFirst()).isFalse();
        props.getReactive().setContextFirst(true);
        assertThat(ctx.isReactiveContextFirst()).isTrue();
    }

    @Test
    void withPrintMethodSkipsNullTraceIdAndScene() {
        ctx.setTraceId(null);
        ctx.setScene(null);

        Mono<ContextView> mono = ctx.withPrintMethod(true, () -> Mono.deferContextual(Mono::just));
        ContextView view = mono.block();

        assertThat((Boolean) view.get(ReactiveTrace.SHADOW_TRACE_KEY)).isEqualTo(true);
        assertThat(view.hasKey(ReactiveTrace.TRACE_ID_KEY)).isFalse();
        assertThat(view.hasKey(ReactiveTrace.SCENE_KEY)).isFalse();
    }

    @Test
    void withMethodEnabledSkipsNullTraceIdAndScene() {
        ctx.setTraceId(null);
        ctx.setScene(null);

        Mono<ContextView> mono = ctx.withMethodEnabled(true, () -> Mono.deferContextual(Mono::just));
        ContextView view = mono.block();

        assertThat((Boolean) view.get(ReactiveTrace.METHOD_ENABLED_KEY)).isEqualTo(true);
        assertThat(view.hasKey(ReactiveTrace.TRACE_ID_KEY)).isFalse();
        assertThat(view.hasKey(ReactiveTrace.SCENE_KEY)).isFalse();
    }

    @Test
    void withPrintMethodSkipsBlankTraceIdAndSceneForFlux() {
        ctx.setTraceId(" ");
        ctx.setScene("");

        Flux<ContextView> flux = ctx.withPrintMethod(true, () -> Flux.deferContextual(Flux::just));
        ContextView view = flux.blockFirst();

        assertThat((Boolean) view.get(ReactiveTrace.SHADOW_TRACE_KEY)).isEqualTo(true);
        assertThat(view.hasKey(ReactiveTrace.TRACE_ID_KEY)).isFalse();
        assertThat(view.hasKey(ReactiveTrace.SCENE_KEY)).isFalse();
    }

    @Test
    void withMethodEnabledSkipsBlankTraceIdAndSceneForFlux() {
        ctx.setTraceId(" ");
        ctx.setScene("");

        Flux<ContextView> flux = ctx.withMethodEnabled(true, () -> Flux.deferContextual(Flux::just));
        ContextView view = flux.blockFirst();

        assertThat((Boolean) view.get(ReactiveTrace.METHOD_ENABLED_KEY)).isEqualTo(true);
        assertThat(view.hasKey(ReactiveTrace.TRACE_ID_KEY)).isFalse();
        assertThat(view.hasKey(ReactiveTrace.SCENE_KEY)).isFalse();
    }

    @Test
    void withPrintMethodSkipsNullTraceIdAndSceneForFlux() {
        ctx.setTraceId(null);
        ctx.setScene(null);

        Flux<ContextView> flux = ctx.withPrintMethod(true, () -> Flux.deferContextual(Flux::just));
        ContextView view = flux.blockFirst();

        assertThat((Boolean) view.get(ReactiveTrace.SHADOW_TRACE_KEY)).isEqualTo(true);
        assertThat(view.hasKey(ReactiveTrace.TRACE_ID_KEY)).isFalse();
        assertThat(view.hasKey(ReactiveTrace.SCENE_KEY)).isFalse();
    }

    @Test
    void withMethodEnabledSkipsNullTraceIdAndSceneForFlux() {
        ctx.setTraceId(null);
        ctx.setScene(null);

        Flux<ContextView> flux = ctx.withMethodEnabled(true, () -> Flux.deferContextual(Flux::just));
        ContextView view = flux.blockFirst();

        assertThat((Boolean) view.get(ReactiveTrace.METHOD_ENABLED_KEY)).isEqualTo(true);
        assertThat(view.hasKey(ReactiveTrace.TRACE_ID_KEY)).isFalse();
        assertThat(view.hasKey(ReactiveTrace.SCENE_KEY)).isFalse();
    }
}

