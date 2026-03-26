package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.internal.core.ResponseCode;
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
class ScopeComprehensiveTest {

    @Mock
    private ChainCore<?> chain;

    private Scope<String> scope;

    @BeforeEach
    void setUp() {
        when(chain.errorSize()).thenReturn(0);
        scope = new Scope<>(chain, "test", "testPath");
    }

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
    void testAssertionMethodsWithEnded() {
        // Set ended to true using reflection
        try {
            var field = Scope.class.getDeclaredField("ended");
            field.setAccessible(true);
            field.set(scope, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Test all assertion methods with ended = true
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
             .matches(stringRef, ".*", code)
             .check(stringRef, s -> true, code, "detail")
             .check(stringRef, () -> true, code, "detail");

        // Verify no checks were called
        verify(chain, never()).checkRef(anyBoolean(), any(), any());
    }

    @Test
    void testCheckWithNullValue() {
        PathEntry<String> nullRef = new PathEntry<>(null, "testPath");
        scope.check(nullRef, s -> true, ResponseCode.VALIDATION_ERROR_400, "detail");
        verify(chain).checkRef(false, ResponseCode.VALIDATION_ERROR_400, nullRef);
    }

    @Test
    void testConditionalMethodsWithEnded() {
        // Set ended to true using reflection
        try {
            var field = Scope.class.getDeclaredField("ended");
            field.setAccessible(true);
            field.set(scope, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Test conditional methods with ended = true
        Runnable action = mock(Runnable.class);
        scope.when(true, action)
             .when(s -> true, action)
             .unless(false, action)
             .unless(s -> false, action);

        // Verify action was not called
        verify(action, never()).run();
    }

    @Test
    void testNestedWithNullItem() {
        scope.nested(s -> null, mock(Consumer.class));
        scope.nested("field", s -> null, mock(Consumer.class));
    }

    @Test
    void testForEachWithNullCollection() {
        scope.forEach(s -> null, mock(Consumer.class));
        scope.forEach("field", s -> null, mock(Consumer.class));
    }

    @Test
    void testForEachEntryWithNullMap() {
        scope.forEachEntry(s -> null, mock(BiConsumer.class));
        scope.forEachEntry("field", s -> null, mock(BiConsumer.class));
    }

    @Test
    void testGetFieldNameFromGetterWithValidGetter() {
        // Test getFieldNameFromGetter with a valid getter
        Function<String, String> getter = s -> s;
        try {
            var method = Scope.class.getDeclaredMethod("getFieldNameFromGetter", Function.class);
            method.setAccessible(true);
            method.invoke(scope, getter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetFieldNameFromGetterWithInvalidGetter() {
        // Test getFieldNameFromGetter with an invalid getter that throws exception
        Function<String, String> getter = s -> {
            throw new RuntimeException();
        };
        try {
            var method = Scope.class.getDeclaredMethod("getFieldNameFromGetter", Function.class);
            method.setAccessible(true);
            method.invoke(scope, getter);
        } catch (Exception e) {
            e.printStackTrace();
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
    void testForEachWithFieldNameAndEnded() {
        // Set ended to true using reflection
        try {
            var field = Scope.class.getDeclaredField("ended");
            field.setAccessible(true);
            field.set(scope, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        scope.forEach("field", s -> List.of("a", "b"), mock(Consumer.class));
    }

    @Test
    void testForEachEntryWithFieldNameAndEnded() {
        // Set ended to true using reflection
        try {
            var field = Scope.class.getDeclaredField("ended");
            field.setAccessible(true);
            field.set(scope, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        scope.forEachEntry("field", s -> Map.of("key", "value"), mock(BiConsumer.class));
    }

    @Test
    void testStopItemOnFail() {
        when(chain.errorSize()).thenReturn(1);
        scope.stopItemOnFail();
    }

    @Test
    void testMerge() {
        scope.merge();
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
}
