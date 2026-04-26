package com.chao.failfast.condition;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class PredicateTest {

    @Test
    void testDefaultConstructor() {
        Predicate predicate = new Predicate();
        assertTrue(predicate.evaluate());
    }

    @Test
    void testOfTrue() {
        Predicate predicate = Predicate.of(true);
        assertTrue(predicate.evaluate());
    }

    @Test
    void testOfFalse() {
        Predicate predicate = Predicate.of(false);
        assertFalse(predicate.evaluate());
    }

    @Test
    void testAllOfNull() {
        Predicate predicate = Predicate.allOf(null);
        assertTrue(predicate.evaluate());
    }

    @Test
    void testAllOfEmpty() {
        Predicate predicate = Predicate.allOf();
        assertTrue(predicate.evaluate());
    }

    @Test
    void testAllOfSingleTrue() {
        Predicate predicate = Predicate.allOf(true);
        assertTrue(predicate.evaluate());
    }

    @Test
    void testAllOfSingleFalse() {
        Predicate predicate = Predicate.allOf(false);
        assertFalse(predicate.evaluate());
    }

    @Test
    void testAllOfMultipleAllTrue() {
        Predicate predicate = Predicate.allOf(true, true, true);
        assertTrue(predicate.evaluate());
    }

    @Test
    void testAllOfMultipleWithFalse() {
        Predicate predicate = Predicate.allOf(true, false, true);
        assertFalse(predicate.evaluate());
    }

    @Test
    void testAnyOfNull() {
        Predicate predicate = Predicate.anyOf(null);
        assertTrue(predicate.evaluate());
    }

    @Test
    void testAnyOfEmpty() {
        Predicate predicate = Predicate.anyOf();
        assertTrue(predicate.evaluate());
    }

    @Test
    void testAnyOfSingleTrue() {
        Predicate predicate = Predicate.anyOf(true);
        assertTrue(predicate.evaluate());
    }

    @Test
    void testAnyOfSingleFalse() {
        Predicate predicate = Predicate.anyOf(false);
        assertFalse(predicate.evaluate());
    }

    @Test
    void testAnyOfMultipleWithTrue() {
        Predicate predicate = Predicate.anyOf(false, true, false);
        assertTrue(predicate.evaluate());
    }

    @Test
    void testAnyOfMultipleAllFalse() {
        Predicate predicate = Predicate.anyOf(false, false, false);
        assertFalse(predicate.evaluate());
    }

    @Test
    void testGroupNullBuilder() {
        Predicate predicate = Predicate.group(null);
        assertTrue(predicate.evaluate());
    }

    @Test
    void testGroupWithBuilder() {
        Predicate predicate = Predicate.group(p -> p.and(true).or(false));
        assertTrue(predicate.evaluate());
    }

    @Test
    void testAndWithBooleanTrue() {
        Predicate predicate = new Predicate();
        Predicate result = predicate.and(true);
        assertTrue(result.evaluate());
    }

    @Test
    void testAndWithBooleanFalse() {
        Predicate predicate = new Predicate();
        Predicate result = predicate.and(false);
        assertFalse(result.evaluate());
    }

    @Test
    void testOrWithBooleanTrue() {
        Predicate predicate = new Predicate();
        Predicate result = predicate.or(true);
        assertTrue(result.evaluate());
    }

    @Test
    void testOrWithBooleanFalse() {
        Predicate predicate = new Predicate();
        Predicate result = predicate.or(false);
        assertFalse(result.evaluate());
    }

    @Test
    void testAndWithNullPredicate() {
        Predicate predicate = Predicate.of(true);
        Predicate result = predicate.and(null);
        assertTrue(result.evaluate());
    }

    @Test
    void testAndWithEmptyPredicate() {
        Predicate predicate = Predicate.of(true);
        Predicate emptyPredicate = new Predicate();
        Predicate result = predicate.and(emptyPredicate);
        assertTrue(result.evaluate());
    }

    @Test
    void testAndWithNonEmptyPredicate() {
        Predicate predicate1 = Predicate.of(true);
        Predicate predicate2 = Predicate.of(true);
        Predicate result = predicate1.and(predicate2);
        assertTrue(result.evaluate());

        Predicate predicate3 = Predicate.of(false);
        Predicate result2 = predicate1.and(predicate3);
        assertFalse(result2.evaluate());
    }

    @Test
    void testOrWithNullPredicate() {
        Predicate predicate = Predicate.of(false);
        Predicate result = predicate.or(null);
        assertFalse(result.evaluate());
    }

    @Test
    void testOrWithEmptyPredicate() {
        Predicate predicate = Predicate.of(false);
        Predicate emptyPredicate = new Predicate();
        Predicate result = predicate.or(emptyPredicate);
        assertFalse(result.evaluate());
    }

    @Test
    void testOrWithNonEmptyPredicate() {
        Predicate predicate1 = Predicate.of(false);
        Predicate predicate2 = Predicate.of(true);
        Predicate result = predicate1.or(predicate2);
        assertTrue(result.evaluate());

        Predicate predicate3 = Predicate.of(false);
        Predicate result2 = predicate1.or(predicate3);
        assertFalse(result2.evaluate());
    }

    @Test
    void testEvaluateWithNullRoot() {
        Predicate predicate = new Predicate();
        assertTrue(predicate.evaluate());
    }

    @Test
    void testEvaluateWithNonNullRoot() {
        Predicate predicate = Predicate.of(true);
        assertTrue(predicate.evaluate());

        Predicate predicate2 = Predicate.of(false);
        assertFalse(predicate2.evaluate());
    }

    @Test
    void testComplexExpressions() {
        Predicate complex1 = Predicate.of(true).and(true).or(false);
        assertTrue(complex1.evaluate());

        Predicate complex2 = Predicate.of(false).and(true).or(true);
        assertTrue(complex2.evaluate());

        Predicate complex3 = Predicate.of(false).and(false).or(false);
        assertFalse(complex3.evaluate());

        Predicate nested = Predicate.of(true).and(
                Predicate.of(false).or(true)
        );
        assertTrue(nested.evaluate());

        Predicate nested2 = Predicate.of(true).and(
                Predicate.of(false).and(false)
        );
        assertFalse(nested2.evaluate());
    }

    @Test
    void testAndWithNullNode() {
        // 通过反射调用私有方法and(Node node)来测试null节点情况
        try {
            Predicate predicate = new Predicate();
            // 使用Class.forName获取Node类
            Class<?> nodeClass = Class.forName("com.chao.failfast.condition.Predicate$Node");
            java.lang.reflect.Method andMethod = Predicate.class.getDeclaredMethod("and", nodeClass);
            andMethod.setAccessible(true);
            Predicate result = (Predicate) andMethod.invoke(predicate, (Object) null);
            assertSame(predicate, result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    void testOrWithNullNode() {
        // 通过反射调用私有方法or(Node node)来测试null节点情况
        try {
            Predicate predicate = new Predicate();
            // 使用Class.forName获取Node类
            Class<?> nodeClass = Class.forName("com.chao.failfast.condition.Predicate$Node");
            java.lang.reflect.Method orMethod = Predicate.class.getDeclaredMethod("or", nodeClass);
            orMethod.setAccessible(true);
            Predicate result = (Predicate) orMethod.invoke(predicate, (Object) null);
            assertSame(predicate, result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

}
