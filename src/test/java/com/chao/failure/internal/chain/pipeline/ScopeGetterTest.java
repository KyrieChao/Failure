package com.chao.failure.internal.chain.pipeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScopeGetterTest {

    @Mock
    private ChainCore<?> chain;

    private Scope<String> scope;

    @BeforeEach
    void setUp() {
        when(chain.errorSize()).thenReturn(0);
        scope = new Scope<>(chain, "test", "testPath");
    }

    @Test
    void testGetFieldNameFromGetterWithValidGetter() {
        try {
            var method = Scope.class.getDeclaredMethod("getFieldNameFromGetter", Function.class);
            method.setAccessible(true);
            
            // Create a getter that will produce a string with dot and parenthesis
            Function<String, String> getter = new Function<String, String>() {
                @Override
                public String apply(String s) {
                    return s;
                }
                @Override
                public String toString() {
                    return "com.example.Test::getField";
                }
            };
            
            method.invoke(scope, getter);
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
    void testGetFieldNameFromGetterWithNoDot() {
        try {
            var method = Scope.class.getDeclaredMethod("getFieldNameFromGetter", Function.class);
            method.setAccessible(true);
            
            // Create a getter that will produce a string without dot
            Function<String, String> getter = new Function<String, String>() {
                @Override
                public String apply(String s) {
                    return s;
                }
                @Override
                public String toString() {
                    return "getField()";
                }
            };
            
            method.invoke(scope, getter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetFieldNameFromGetterWithDotButNoParenthesis() {
        try {
            var method = Scope.class.getDeclaredMethod("getFieldNameFromGetter", Function.class);
            method.setAccessible(true);
            
            // Create a getter that will produce a string with dot but no parenthesis
            Function<String, String> getter = new Function<String, String>() {
                @Override
                public String apply(String s) {
                    return s;
                }
                @Override
                public String toString() {
                    return "com.example.Test.getField";
                }
            };
            
            method.invoke(scope, getter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetFieldNameFromGetterWithParenthesisBeforeDot() {
        try {
            var method = Scope.class.getDeclaredMethod("getFieldNameFromGetter", Function.class);
            method.setAccessible(true);
            
            // Create a getter that will produce a string with parenthesis before dot
            Function<String, String> getter = new Function<String, String>() {
                @Override
                public String apply(String s) {
                    return s;
                }
                @Override
                public String toString() {
                    return "getField().com.example.Test";
                }
            };
            
            method.invoke(scope, getter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
