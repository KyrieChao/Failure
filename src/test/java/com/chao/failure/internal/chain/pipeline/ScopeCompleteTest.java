package com.chao.failure.internal.chain.pipeline;

import com.chao.failure.internal.core.ResponseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScopeCompleteTest {

    @Mock
    private ChainCore<?> chain;

    private Scope<String> scope;

    @BeforeEach
    void setUp() {
        when(chain.errorSize()).thenReturn(0);
        scope = new Scope<>(chain, "test", "testPath");
    }

    // FieldRef tests
    @Test
    void testFieldRefAsWithDot() {
        Scope.FieldRef<String> fieldRef = new Scope.FieldRef<>("value", "parent.field");
        fieldRef.as("alias");
    }

    @Test
    void testFieldRefAsWithoutDot() {
        Scope.FieldRef<String> fieldRef = new Scope.FieldRef<>("value", "field");
        fieldRef.as("alias");
    }

    @Test
    void testFieldRefRef() {
        Scope.FieldRef<String> fieldRef = new Scope.FieldRef<>("value", "field");
        fieldRef.ref();
    }

    @Test
    void testFieldRefValue() {
        Scope.FieldRef<String> fieldRef = new Scope.FieldRef<>("value", "field");
        fieldRef.value();
    }

    // Field methods
    @Test
    void testFieldWithGetter() {
        scope.field(String::length);
    }

    @Test
    void testFieldEntryWithGetter() {
        scope.fieldEntry(String::length);
    }

    @Test
    void testFieldWithNameAndGetter() {
        scope.field("testField", String::length);
    }

    // Assertion methods with ended = true
    @Test
    void testAssertionMethodsWithEnded() {
        setEnded(true);

        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        PathEntry<String> stringRef = new PathEntry<>("test", "testPath");
        PathEntry<Integer> numberRef = new PathEntry<>(1, "testPath");
        PathEntry<Boolean> booleanRef = new PathEntry<>(true, "testPath");
        PathEntry<List<String>> collectionRef = new PathEntry<>(List.of(), "testPath");
        PathEntry<Map<String, String>> mapRef = new PathEntry<>(Map.of(), "testPath");

        scope.notNull(code)
             .notBlank(stringRef, code)
             .positive(numberRef, code)
             .email(stringRef, code)
             .mobile(stringRef, code)
             .isTrue(booleanRef, code)
             .isFalse(booleanRef, code)
             .notEmptyCollection(collectionRef, code)
             .notEmptyMap(mapRef, code)
             .length(stringRef, 1, 10, code)
             .between(numberRef, 1, 10, code)
             .matches(stringRef, ".*", code);

        verify(chain, never()).checkRef(anyBoolean(), any(), any());
    }

    // Assertion methods with ended = false
    @Test
    void testAssertionMethodsWithNotEnded() {
        setEnded(false);

        ResponseCode code = ResponseCode.VALIDATION_ERROR_400;
        PathEntry<String> stringRef = new PathEntry<>("test", "testPath");
        PathEntry<Integer> numberRef = new PathEntry<>(1, "testPath");
        PathEntry<Boolean> booleanRef = new PathEntry<>(true, "testPath");
        PathEntry<List<String>> collectionRef = new PathEntry<>(List.of(), "testPath");
        PathEntry<Map<String, String>> mapRef = new PathEntry<>(Map.of(), "testPath");

        scope.notNull(code)
             .notBlank(stringRef, code)
             .positive(numberRef, code)
             .email(stringRef, code)
             .mobile(stringRef, code)
             .isTrue(booleanRef, code)
             .isFalse(booleanRef, code)
             .notEmptyCollection(collectionRef, code)
             .notEmptyMap(mapRef, code)
             .length(stringRef, 1, 10, code)
             .between(numberRef, 1, 10, code)
             .matches(stringRef, ".*", code);

        verify(chain, atLeastOnce()).checkRef(anyBoolean(), any(), any());
    }

    // Check methods
    @Test
    void testCheckWithPredicateAndNullValue() {
        PathEntry<String> nullRef = new PathEntry<>(null, "testPath");
        scope.check(nullRef, s -> true, ResponseCode.VALIDATION_ERROR_400, "detail");
        verify(chain).checkRef(eq(false), any(ResponseCode.class), eq(nullRef));
    }

    @Test
    void testCheckWithPredicateAndNonNullValue() {
        PathEntry<String> nonNullRef = new PathEntry<>("test", "testPath");
        scope.check(nonNullRef, s -> true, ResponseCode.VALIDATION_ERROR_400, "detail");
        verify(chain).checkRef(eq(true), any(ResponseCode.class), eq(nonNullRef));
    }

    @Test
    void testCheckWithSupplierAndEnded() {
        setEnded(true);
        PathEntry<String> ref = new PathEntry<>("test", "testPath");
        scope.check(ref, () -> true, ResponseCode.VALIDATION_ERROR_400, "detail");
        verify(chain, never()).checkRef(anyBoolean(), any(), any());
    }

    @Test
    void testCheckWithSupplierAndNotEnded() {
        setEnded(false);
        PathEntry<String> ref = new PathEntry<>("test", "testPath");
        scope.check(ref, () -> true, ResponseCode.VALIDATION_ERROR_400, "detail");
        verify(chain).checkRef(eq(true), any(ResponseCode.class), eq(ref));
    }

    // Conditional methods
    @Test
    void testWhenWithConditionTrueAndEnded() {
        setEnded(true);
        Runnable action = mock(Runnable.class);
        scope.when(true, action);
        verify(action, never()).run();
    }

    @Test
    void testWhenWithConditionTrueAndNotEnded() {
        setEnded(false);
        Runnable action = mock(Runnable.class);
        scope.when(true, action);
        verify(action).run();
    }

    @Test
    void testWhenWithConditionFalseAndNotEnded() {
        setEnded(false);
        Runnable action = mock(Runnable.class);
        scope.when(false, action);
        verify(action, never()).run();
    }

    @Test
    void testWhenWithPredicateAndEnded() {
        setEnded(true);
        Runnable action = mock(Runnable.class);
        scope.when(s -> true, action);
        verify(action, never()).run();
    }

    @Test
    void testWhenWithPredicateTrueAndNotEnded() {
        setEnded(false);
        Runnable action = mock(Runnable.class);
        scope.when(s -> true, action);
        verify(action).run();
    }

    @Test
    void testWhenWithPredicateFalseAndNotEnded() {
        setEnded(false);
        Runnable action = mock(Runnable.class);
        scope.when(s -> false, action);
        verify(action, never()).run();
    }

    @Test
    void testUnlessWithConditionTrueAndEnded() {
        setEnded(true);
        Runnable action = mock(Runnable.class);
        scope.unless(true, action);
        verify(action, never()).run();
    }

    @Test
    void testUnlessWithConditionTrueAndNotEnded() {
        setEnded(false);
        Runnable action = mock(Runnable.class);
        scope.unless(true, action);
        verify(action, never()).run();
    }

    @Test
    void testUnlessWithConditionFalseAndNotEnded() {
        setEnded(false);
        Runnable action = mock(Runnable.class);
        scope.unless(false, action);
        verify(action).run();
    }

    @Test
    void testUnlessWithPredicateAndEnded() {
        setEnded(true);
        Runnable action = mock(Runnable.class);
        scope.unless(s -> true, action);
        verify(action, never()).run();
    }

    @Test
    void testUnlessWithPredicateTrueAndNotEnded() {
        setEnded(false);
        Runnable action = mock(Runnable.class);
        scope.unless(s -> true, action);
        verify(action, never()).run();
    }

    @Test
    void testUnlessWithPredicateFalseAndNotEnded() {
        setEnded(false);
        Runnable action = mock(Runnable.class);
        scope.unless(s -> false, action);
        verify(action).run();
    }

    // Nested methods
    @Test
    void testNestedWithNullItemAndEnded() {
        setEnded(true);
        Consumer<Scope<String>> action = mock(Consumer.class);
        scope.nested(s -> null, action);
        verify(action, never()).accept(any());
    }

    @Test
    void testNestedWithNullItemAndNotEnded() {
        setEnded(false);
        Consumer<Scope<String>> action = mock(Consumer.class);
        scope.nested(s -> null, action);
        verify(action, never()).accept(any());
    }

    @Test
    void testNestedWithNonNullItemAndNotEnded() {
        setEnded(false);
        Consumer<Scope<String>> action = mock(Consumer.class);
        scope.nested(s -> "nested", action);
        verify(action).accept(any());
    }

    @Test
    void testNestedWithFieldNameAndNullItem() {
        setEnded(false);
        Consumer<Scope<String>> action = mock(Consumer.class);
        scope.nested("field", s -> null, action);
        verify(action, never()).accept(any());
    }

    @Test
    void testNestedWithFieldNameAndNonNullItem() {
        setEnded(false);
        Consumer<Scope<String>> action = mock(Consumer.class);
        scope.nested("field", s -> "nested", action);
        verify(action).accept(any());
    }

    // ForEach methods
    @Test
    void testForEachWithNullCollectionAndEnded() {
        setEnded(true);
        Consumer<Scope<String>> action = mock(Consumer.class);
        scope.forEach(s -> null, action);
        verify(action, never()).accept(any());
    }

    @Test
    void testForEachWithNullCollectionAndNotEnded() {
        setEnded(false);
        Consumer<Scope<String>> action = mock(Consumer.class);
        scope.forEach(s -> null, action);
        verify(action, never()).accept(any());
    }

    @Test
    void testForEachWithNonNullCollectionAndNotEnded() {
        setEnded(false);
        Consumer<Scope<String>> action = mock(Consumer.class);
        scope.forEach(s -> List.of("a", "b"), action);
        verify(action, times(2)).accept(any());
    }

    @Test
    void testForEachWithFieldNameAndNullCollection() {
        setEnded(false);
        Consumer<Scope<String>> action = mock(Consumer.class);
        scope.forEach("field", s -> null, action);
        verify(action, never()).accept(any());
    }

    @Test
    void testForEachWithFieldNameAndNonNullCollection() {
        setEnded(false);
        Consumer<Scope<String>> action = mock(Consumer.class);
        scope.forEach("field", s -> List.of("a", "b"), action);
        verify(action, times(2)).accept(any());
    }

    // ForEachEntry methods
    @Test
    void testForEachEntryWithNullMapAndEnded() {
        setEnded(true);
        BiConsumer<String, Scope<String>> action = mock(BiConsumer.class);
        scope.forEachEntry(s -> null, action);
        verify(action, never()).accept(any(), any());
    }

    @Test
    void testForEachEntryWithNullMapAndNotEnded() {
        setEnded(false);
        BiConsumer<String, Scope<String>> action = mock(BiConsumer.class);
        scope.forEachEntry(s -> null, action);
        verify(action, never()).accept(any(), any());
    }

    @Test
    void testForEachEntryWithNonNullMapAndNotEnded() {
        setEnded(false);
        BiConsumer<String, Scope<String>> action = mock(BiConsumer.class);
        scope.forEachEntry(s -> Map.of("key1", "value1", "key2", "value2"), action);
        verify(action, times(2)).accept(any(), any());
    }

    @Test
    void testForEachEntryWithFieldNameAndNullMap() {
        setEnded(false);
        BiConsumer<String, Scope<String>> action = mock(BiConsumer.class);
        scope.forEachEntry("field", s -> null, action);
        verify(action, never()).accept(any(), any());
    }

    @Test
    void testForEachEntryWithFieldNameAndNonNullMap() {
        setEnded(false);
        BiConsumer<String, Scope<String>> action = mock(BiConsumer.class);
        scope.forEachEntry("field", s -> Map.of("key1", "value1"), action);
        verify(action).accept(any(), any());
    }

    // Helper methods
    @Test
    void testGetFieldNameFromGetterWithValidGetter() {
        try {
            var method = Scope.class.getDeclaredMethod("getFieldNameFromGetter", Function.class);
            method.setAccessible(true);
            method.invoke(scope, (Function<String, String>) s -> s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetFieldNameFromGetterWithInvalidGetter() {
        try {
            var method = Scope.class.getDeclaredMethod("getFieldNameFromGetter", Function.class);
            method.setAccessible(true);
            // Create a getter that will throw an exception when toString() is called
            Function<String, String> problematicGetter = new Function<String, String>() {
                @Override
                public String apply(String s) {
                    return s;
                }
                @Override
                public String toString() {
                    throw new RuntimeException("Test exception");
                }
            };
            method.invoke(scope, problematicGetter);
        } catch (Exception e) {
            // Expected exception
        }
    }

    @Test
    void testJoinPathWithNullParent() {
        try {
            var method = Scope.class.getDeclaredMethod("joinPath", String.class, String.class);
            method.setAccessible(true);
            method.invoke(null, null, "child");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testJoinPathWithBlankParent() {
        try {
            var method = Scope.class.getDeclaredMethod("joinPath", String.class, String.class);
            method.setAccessible(true);
            method.invoke(null, "   ", "child");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testJoinPathWithNonBlankParent() {
        try {
            var method = Scope.class.getDeclaredMethod("joinPath", String.class, String.class);
            method.setAccessible(true);
            method.invoke(null, "parent", "child");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Other methods
    @Test
    void testMerge() {
        scope.merge();
    }

    @Test
    void testStopItemOnFail() {
        when(chain.errorSize()).thenReturn(1);
        scope.stopItemOnFail();
    }

    @Test
    void testEndOnFailWithNoErrors() {
        try {
            var method = Scope.class.getDeclaredMethod("endOnFail");
            method.setAccessible(true);
            method.invoke(scope);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testEndOnFailWithErrors() {
        when(chain.errorSize()).thenReturn(1);
        try {
            var stopItemOnFailField = Scope.class.getDeclaredField("stopItemOnFail");
            stopItemOnFailField.setAccessible(true);
            stopItemOnFailField.set(scope, true);

            var method = Scope.class.getDeclaredMethod("endOnFail");
            method.setAccessible(true);
            method.invoke(scope);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to set ended flag using reflection
    private void setEnded(boolean value) {
        try {
            var field = Scope.class.getDeclaredField("ended");
            field.setAccessible(true);
            field.set(scope, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
