package com.chao.failure.internal.chain.pipeline;

import com.chao.failure.constant.FailureConst;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class ScopeSurvivedMutationsTest {

    @Mock
    private ChainCore<?> chain;

    private Scope<String> scope;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(chain.errorSize()).thenReturn(0);
        scope = new Scope<>(chain, "test", "testPath");
    }

    // Test FieldRef.as() method with edge cases to cover boundary mutations
    @Test
    void testFieldRefAsWithEdgeCases() {
        // Test with empty path
        Scope.FieldRef<String> fieldRefEmptyPath = new Scope.FieldRef<>("value", "");
        fieldRefEmptyPath.as("newField");

        // Test with path ending with dot
        Scope.FieldRef<String> fieldRefEndingWithDot = new Scope.FieldRef<>("value", "parent.");
        fieldRefEndingWithDot.as("newField");

        // Test with path containing multiple dots
        Scope.FieldRef<String> fieldRefMultipleDots = new Scope.FieldRef<>("value", "grandparent.parent.field");
        fieldRefMultipleDots.as("newField");
    }

    // Test assertion methods with stopItemOnFail=true to cover endOnFail() mutations
    @Test
    void testAssertionMethodsWithStopItemOnFail() {
        // Create scope with stopItemOnFail=true
        when(chain.errorSize()).thenReturn(0);
        Scope<String> scopeWithStop = new Scope<>(chain, "test", "testPath");
        scopeWithStop.stopItemOnFail();

        // Test notNull with failure
        when(chain.errorSize()).thenReturn(1);
        scopeWithStop.notNull(FailureConst.NOT_NULL_ERROR);

        // Test notBlank with failure
        Scope.FieldRef<String> blankFieldRef = new Scope.FieldRef<>("", "testField");
        scopeWithStop.notBlank(blankFieldRef.ref(), FailureConst.NOT_BLANK_ERROR);

        // Test positive with failure
        Scope.FieldRef<Integer> negativeFieldRef = new Scope.FieldRef<>(-1, "testField");
        scopeWithStop.positive(negativeFieldRef.ref(), FailureConst.POSITIVE_ERROR);

        // Test email with failure
        Scope.FieldRef<String> invalidEmailFieldRef = new Scope.FieldRef<>("invalid-email", "testField");
        scopeWithStop.email(invalidEmailFieldRef.ref(), FailureConst.EMAIL_ERROR);

        // Test mobile with failure
        Scope.FieldRef<String> invalidMobileFieldRef = new Scope.FieldRef<>("123456", "testField");
        scopeWithStop.mobile(invalidMobileFieldRef.ref(), FailureConst.MOBILE_ERROR);

        // Test isTrue with failure
        Scope.FieldRef<Boolean> falseFieldRef = new Scope.FieldRef<>(false, "testField");
        scopeWithStop.isTrue(falseFieldRef.ref(), FailureConst.IS_TRUE_ERROR);

        // Test isFalse with failure
        Scope.FieldRef<Boolean> trueFieldRef = new Scope.FieldRef<>(true, "testField");
        scopeWithStop.isFalse(trueFieldRef.ref(), FailureConst.IS_FALSE_ERROR);
    }

    // Test assertion methods with ended=true to ensure they return early
    @Test
    void testAssertionMethodsWithEnded() throws Exception {
        // Create scope and set ended=true using reflection
        when(chain.errorSize()).thenReturn(0);
        Scope<String> endedScope = new Scope<>(chain, "test", "testPath");
        
        // Use reflection to set ended field to true
        java.lang.reflect.Field endedField = Scope.class.getDeclaredField("ended");
        endedField.setAccessible(true);
        endedField.set(endedScope, true);

        // Test all assertion methods with ended=true
        endedScope.notNull(FailureConst.NOT_NULL_ERROR);
        
        Scope.FieldRef<String> stringFieldRef = new Scope.FieldRef<>("test", "testField");
        endedScope.notBlank(stringFieldRef.ref(), FailureConst.NOT_BLANK_ERROR);
        endedScope.email(stringFieldRef.ref(), FailureConst.EMAIL_ERROR);
        endedScope.mobile(stringFieldRef.ref(), FailureConst.MOBILE_ERROR);

        Scope.FieldRef<Integer> numberFieldRef = new Scope.FieldRef<>(5, "testField");
        endedScope.positive(numberFieldRef.ref(), FailureConst.POSITIVE_ERROR);

        Scope.FieldRef<Boolean> booleanFieldRef = new Scope.FieldRef<>(true, "testField");
        endedScope.isTrue(booleanFieldRef.ref(), FailureConst.IS_TRUE_ERROR);
        endedScope.isFalse(booleanFieldRef.ref(), FailureConst.IS_FALSE_ERROR);

        // Verify no checkRef calls were made
        verify(chain, never()).checkRef(anyBoolean(), any(), any());
    }
}
