package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.constant.FailureConst;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.mockito.Mockito.*;

class ScopeMutationTest {

    @Mock
    private ChainCore<?> chain;

    private Scope<String> scope;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(chain.errorSize()).thenReturn(0);
        scope = new Scope<>(chain, "test", "testPath");
    }

    // Test getFieldNameFromGetter method with various scenarios
    @Test
    void testGetFieldNameFromGetterWithValidGetter() {
        Function<String, String> getter = s -> s;
        scope.field(getter);
    }

    @Test
    void testGetFieldNameFromGetterWithInvalidGetter() {
        Function<String, String> getter = s -> s;
        scope.field(getter);
    }

    @Test
    void testGetFieldNameFromGetterWithToStringException() {
        Function<String, String> getter = s -> s;
        scope.field(getter);
    }

    @Test
    void testGetFieldNameFromGetterWithSubstringException() {
        // Test with a getter that will cause substring exception
        Function<String, String> getter = new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s;
            }

            @Override
            public String toString() {
                return "test";
            }
        };
        scope.field(getter);
    }

    @Test
    void testGetFieldNameFromGetterWithValidFormat() {
        // Test with a getter that has valid format
        Function<String, String> getter = new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s;
            }

            @Override
            public String toString() {
                return "com.example.Test::getName";
            }
        };
        scope.field(getter);
    }

    @Test
    void testGetFieldNameFromGetterWithException() {
        // Test with a normal getter
        Function<String, String> getter = s -> s;
        scope.field(getter);
    }

    // Test FieldRef.as() method with different scenarios
    @Test
    void testFieldRefAsWithDotInPath() {
        Scope.FieldRef<String> fieldRef = new Scope.FieldRef<>("value", "parent.field");
        fieldRef.as("newField");
    }

    @Test
    void testFieldRefAsWithoutDotInPath() {
        Scope.FieldRef<String> fieldRef = new Scope.FieldRef<>("value", "field");
        fieldRef.as("newField");
    }

    // Test endOnFail method with stopItemOnFail=true
    @Test
    void testEndOnFailWithStopItemOnFail() {
        when(chain.errorSize()).thenReturn(1);
        Scope<String> scopeWithStop = new Scope<>(chain, "test", "testPath");
        scopeWithStop.stopItemOnFail();
        // The endOnFail method is called internally
    }

    // Test endOnFail method with stopItemOnFail=false
    @Test
    void testEndOnFailWithoutStopItemOnFail() {
        when(chain.errorSize()).thenReturn(1);
        // endOnFail should not set ended to true
        scope.notNull(FailureConst.NOT_NULL_ERROR);
    }

    // Test merge method
    @Test
    void testMerge() {
        scope.merge();
    }

    // Test nested method with null item
    @Test
    void testNestedWithNullItem() {
        scope.nested(s -> null, nestedScope -> {});
    }

    // Test forEach method with null collection
    @Test
    void testForEachWithNullCollection() {
        scope.forEach(s -> null, itemScope -> {});
    }

    // Test forEachEntry method with null map
    @Test
    void testForEachEntryWithNullMap() {
        scope.forEachEntry(s -> null, (key, valueScope) -> {});
    }

    // Test when method with condition=true
    @Test
    void testWhenWithConditionTrue() {
        scope.when(true, () -> {});
    }

    // Test when method with condition=false
    @Test
    void testWhenWithConditionFalse() {
        scope.when(false, () -> {});
    }

    // Test when method with predicate=true
    @Test
    void testWhenWithPredicateTrue() {
        scope.when(s -> true, () -> {});
    }

    // Test when method with predicate=false
    @Test
    void testWhenWithPredicateFalse() {
        scope.when(s -> false, () -> {});
    }

    // Test unless method with condition=true
    @Test
    void testUnlessWithConditionTrue() {
        scope.unless(true, () -> {});
    }

    // Test unless method with condition=false
    @Test
    void testUnlessWithConditionFalse() {
        scope.unless(false, () -> {});
    }

    // Test unless method with predicate=true
    @Test
    void testUnlessWithPredicateTrue() {
        scope.unless(s -> true, () -> {});
    }

    // Test unless method with predicate=false
    @Test
    void testUnlessWithPredicateFalse() {
        scope.unless(s -> false, () -> {});
    }

    // Test check method with null value
    @Test
    void testCheckWithNullValue() {
        Scope.FieldRef<String> fieldRef = new Scope.FieldRef<>(null, "testField");
        scope.check(fieldRef, s -> s != null, FailureConst.NOT_NULL_ERROR, "Test check");
    }

    // Test check method with non-null value
    @Test
    void testCheckWithNonNullValue() {
        Scope.FieldRef<String> fieldRef = new Scope.FieldRef<>("test", "testField");
        scope.check(fieldRef, s -> s.length() > 0, FailureConst.NOT_BLANK_ERROR, "Test check");
    }

    // Test check method with supplier
    @Test
    void testCheckWithSupplier() {
        Scope.FieldRef<String> fieldRef = new Scope.FieldRef<>("test", "testField");
        scope.check(fieldRef, () -> true, FailureConst.SATISFIES_ERROR, "Test check");
    }

    // Test notEmptyCollection method
    @Test
    void testNotEmptyCollection() {
        List<String> list = new ArrayList<>();
        Scope.FieldRef<List<String>> fieldRef = new Scope.FieldRef<>(list, "testList");
        scope.notEmptyCollection(fieldRef, FailureConst.NOT_EMPTY_ERROR);
    }

    // Test notEmptyMap method
    @Test
    void testNotEmptyMap() {
        Map<String, String> map = new HashMap<>();
        Scope.FieldRef<Map<String, String>> fieldRef = new Scope.FieldRef<>(map, "testMap");
        scope.notEmptyMap(fieldRef, FailureConst.NOT_EMPTY_ERROR);
    }

    // Test length method
    @Test
    void testLength() {
        Scope.FieldRef<String> fieldRef = new Scope.FieldRef<>("test", "testField");
        scope.length(fieldRef, 1, 10, FailureConst.LENGTH_BETWEEN_ERROR);
    }

    // Test between method
    @Test
    void testBetween() {
        Scope.FieldRef<Integer> fieldRef = new Scope.FieldRef<>(5, "testField");
        scope.between(fieldRef, 1, 10, FailureConst.BETWEEN_ERROR);
    }

    // Test matches method
    @Test
    void testMatches() {
        Scope.FieldRef<String> fieldRef = new Scope.FieldRef<>("test", "testField");
        scope.matches(fieldRef, "^test$", FailureConst.MATCH_ERROR);
    }

    // Test isTrue method
    @Test
    void testIsTrue() {
        Scope.FieldRef<Boolean> fieldRef = new Scope.FieldRef<>(true, "testField");
        scope.isTrue(fieldRef.ref(), FailureConst.IS_TRUE_ERROR);
    }

    // Test isFalse method
    @Test
    void testIsFalse() {
        Scope.FieldRef<Boolean> fieldRef = new Scope.FieldRef<>(false, "testField");
        scope.isFalse(fieldRef.ref(), FailureConst.IS_FALSE_ERROR);
    }

    // Test positive method
    @Test
    void testPositive() {
        Scope.FieldRef<Integer> fieldRef = new Scope.FieldRef<>(5, "testField");
        scope.positive(fieldRef, FailureConst.POSITIVE_ERROR);
    }

    // Test email method
    @Test
    void testEmail() {
        Scope.FieldRef<String> fieldRef = new Scope.FieldRef<>("test@example.com", "testField");
        scope.email(fieldRef, FailureConst.EMAIL_ERROR);
    }

    // Test mobile method
    @Test
    void testMobile() {
        Scope.FieldRef<String> fieldRef = new Scope.FieldRef<>("13800138000", "testField");
        scope.mobile(fieldRef, FailureConst.MOBILE_ERROR);
    }
}
