package com.chao.failure.internal.core.security;

import com.chao.failure.spi.security.Mask;
import com.chao.failure.spi.security.MaskPick;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompositeMaskPickTest {

    @Test
    void resolveWithNullDelegates() {
        CompositeMaskPick resolver = new CompositeMaskPick(null);
        
        Mask result = resolver.resolve("test.field");
        
        assertNull(result);
    }

    @Test
    void resolveWithEmptyDelegates() {
        CompositeMaskPick resolver = new CompositeMaskPick(List.of());
        
        Mask result = resolver.resolve("test.field");
        
        assertNull(result);
    }

    @Test
    void resolveWithNullElementInDelegates() {
        MaskPick delegate = mock(MaskPick.class);
        when(delegate.resolve("test.field")).thenReturn(mock(Mask.class));
        
        List<MaskPick> delegates = new ArrayList<>();
        delegates.add(null);
        delegates.add(delegate);
        CompositeMaskPick resolver = new CompositeMaskPick(delegates);
        
        assertNotNull(resolver.resolve("test.field"));
        verify(delegate, times(1)).resolve("test.field");
    }

    @Test
    void resolveWithFirstDelegateReturningMask() {
        Mask mask1 = mock(Mask.class);
        MaskPick delegate1 = mock(MaskPick.class);
        when(delegate1.resolve("test.field")).thenReturn(mask1);
        
        MaskPick delegate2 = mock(MaskPick.class);
        
        CompositeMaskPick resolver = new CompositeMaskPick(List.of(delegate1, delegate2));
        
        Mask result = resolver.resolve("test.field");
        
        assertSame(mask1, result);
        verify(delegate1, times(1)).resolve("test.field");
        verify(delegate2, never()).resolve(any());
    }

    @Test
    void resolveWithSecondDelegateReturningMask() {
        MaskPick delegate1 = mock(MaskPick.class);
        when(delegate1.resolve("test.field")).thenReturn(null);
        
        Mask mask2 = mock(Mask.class);
        MaskPick delegate2 = mock(MaskPick.class);
        when(delegate2.resolve("test.field")).thenReturn(mask2);
        
        CompositeMaskPick resolver = new CompositeMaskPick(List.of(delegate1, delegate2));
        
        Mask result = resolver.resolve("test.field");
        
        assertSame(mask2, result);
        verify(delegate1, times(1)).resolve("test.field");
        verify(delegate2, times(1)).resolve("test.field");
    }

    @Test
    void resolveWithAllDelegatesReturningNull() {
        MaskPick delegate1 = mock(MaskPick.class);
        when(delegate1.resolve("test.field")).thenReturn(null);
        
        MaskPick delegate2 = mock(MaskPick.class);
        when(delegate2.resolve("test.field")).thenReturn(null);
        
        CompositeMaskPick resolver = new CompositeMaskPick(List.of(delegate1, delegate2));
        
        Mask result = resolver.resolve("test.field");
        
        assertNull(result);
        verify(delegate1, times(1)).resolve("test.field");
        verify(delegate2, times(1)).resolve("test.field");
    }

    @Test
    void resolveWithMixedNullAndNonNullDelegates() {
        MaskPick delegate1 = mock(MaskPick.class);
        when(delegate1.resolve("test.field")).thenReturn(null);
        
        MaskPick delegate2 = null;
        
        Mask mask3 = mock(Mask.class);
        MaskPick delegate3 = mock(MaskPick.class);
        when(delegate3.resolve("test.field")).thenReturn(mask3);
        
        List<MaskPick> delegates = new ArrayList<>();
        delegates.add(delegate1);
        delegates.add(delegate2);
        delegates.add(delegate3);
        CompositeMaskPick resolver = new CompositeMaskPick(delegates);
        
        Mask result = resolver.resolve("test.field");
        
        assertSame(mask3, result);
        verify(delegate1, times(1)).resolve("test.field");
        verify(delegate3, times(1)).resolve("test.field");
    }

    @Test
    void constructorWithNonNullDelegates() {
        MaskPick delegate = mock(MaskPick.class);
        CompositeMaskPick resolver = new CompositeMaskPick(List.of(delegate));
        
        assertNotNull(resolver);
    }
}
