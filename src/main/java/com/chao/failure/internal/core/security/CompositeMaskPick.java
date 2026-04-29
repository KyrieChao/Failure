package com.chao.failure.internal.core.security;

import com.chao.failure.spi.security.Mask;
import com.chao.failure.spi.security.MaskPick;

import java.util.List;

public class CompositeMaskPick implements MaskPick {
    private final List<MaskPick> delegates;

    public CompositeMaskPick(List<MaskPick> delegates) {
        this.delegates = delegates;
    }

    @Override
    public Mask resolve(String pick) {
        if (delegates == null || delegates.isEmpty()) return null;
        for (MaskPick resolver : delegates) {
            if (resolver == null) continue;
            Mask mask = resolver.resolve(pick);
            if (mask != null) return mask;
        }
        return null;
    }
}
