package com.chao.failure.internal.core.security;

import com.chao.failure.spi.security.Mask;
import com.chao.failure.spi.security.MaskPick;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MaskPickRegistryTest {

    @AfterEach
    void tearDown() {
        MaskPickRegistry.setDefault(null);
    }

    @Test
    void getDefaultWithNoSet() {
        MaskPick result = MaskPickRegistry.getDefault();
        
        assertNotNull(result);
        assertNull(result.resolve("test.field"));
    }

    @Test
    void getDefaultAfterSetNonNullResolver() {
        Mask customMask = () -> "custom";
        MaskPick customResolver = fieldPath -> customMask;
        
        MaskPickRegistry.setDefault(customResolver);
        MaskPick result = MaskPickRegistry.getDefault();
        
        assertSame(customResolver, result);
    }

    @Test
    void getDefaultAfterSetNullResolver() {
        Mask customMask = () -> "custom";
        MaskPick customResolver = fieldPath -> customMask;
        
        MaskPickRegistry.setDefault(customResolver);
        MaskPickRegistry.setDefault(null);
        
        MaskPick result = MaskPickRegistry.getDefault();
        
        assertNotNull(result);
        assertNull(result.resolve("test.field"));
    }

    @Test
    void setDefaultWithNonNullResolver() {
        Mask customMask = () -> "custom";
        MaskPick customResolver = fieldPath -> customMask;
        
        MaskPickRegistry.setDefault(customResolver);
        
        MaskPick result = MaskPickRegistry.getDefault();
        assertSame(customResolver, result);
    }

    @Test
    void setDefaultWithNullResolver() {
        MaskPickRegistry.setDefault(null);
        
        MaskPick result = MaskPickRegistry.getDefault();
        assertNotNull(result);
        assertNull(result.resolve("test.field"));
    }

    @Test
    void setDefaultMultipleTimes() {
        Mask mask1 = () -> "type1";
        MaskPick resolver1 = fieldPath -> mask1;
        Mask mask2 = () -> "type2";
        MaskPick resolver2 = fieldPath -> mask2;
        
        MaskPickRegistry.setDefault(resolver1);
        assertSame(resolver1, MaskPickRegistry.getDefault());
        
        MaskPickRegistry.setDefault(resolver2);
        assertSame(resolver2, MaskPickRegistry.getDefault());
        
        MaskPickRegistry.setDefault(null);
        MaskPick result = MaskPickRegistry.getDefault();
        assertNotNull(result);
        assertNull(result.resolve("test.field"));
    }

    @Test
    void getDefaultWhenResolverIsNullViaReflection() throws Exception {
        java.lang.reflect.Field resolverField = MaskPickRegistry.class.getDeclaredField("RESOLVER");
        resolverField.setAccessible(true);
        @SuppressWarnings("unchecked")
        AtomicReference<MaskPick> ref = (AtomicReference<MaskPick>) resolverField.get(null);
        MaskPick previous = ref.get();
        ref.set(null);
        
        try {
            MaskPick result = MaskPickRegistry.getDefault();
            
            assertNotNull(result);
            assertNull(result.resolve("test.field"));
        } finally {
            ref.set(previous);
        }
    }
}
