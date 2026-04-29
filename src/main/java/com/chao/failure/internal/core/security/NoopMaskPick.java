package com.chao.failure.internal.core.security;

import com.chao.failure.spi.security.Mask;
import com.chao.failure.spi.security.MaskPick;

public class NoopMaskPick implements MaskPick {
    @Override
    public Mask resolve(String pick) {
        return null;
    }
}
