package com.chao.failure.internal.core.security;

import com.chao.failure.spi.security.MaskPick;

import java.util.concurrent.atomic.AtomicReference;

public final class MaskPickRegistry {
    private static final MaskPick FALLBACK = new NoopMaskPick();
    private static final AtomicReference<MaskPick> RESOLVER = new AtomicReference<>(FALLBACK);

    private MaskPickRegistry() {
    }

    public static void setDefault(MaskPick maskPick) {
        RESOLVER.set(maskPick != null ? maskPick : FALLBACK);
    }

    public static MaskPick getDefault() {
        MaskPick current = RESOLVER.get();
        return current != null ? current : FALLBACK;
    }
}
