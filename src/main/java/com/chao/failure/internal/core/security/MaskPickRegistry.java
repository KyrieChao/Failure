package com.chao.failure.internal.core.security;

import com.chao.failure.spi.security.MaskPick;

public final class MaskPickRegistry {
    private static final MaskPick FALLBACK = new NoopMaskPick();
    private static volatile MaskPick resolver = FALLBACK;

    private MaskPickRegistry() {
    }

    public static void setDefault(MaskPick maskPick) {
        resolver = maskPick != null ? maskPick : FALLBACK;
    }

    public static MaskPick getDefault() {
        return resolver != null ? resolver : FALLBACK;
    }
}
