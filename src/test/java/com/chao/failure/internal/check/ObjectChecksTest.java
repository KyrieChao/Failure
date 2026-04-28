package com.chao.failure.internal.check;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ObjectChecks 100% 覆盖率测试
 */
@DisplayName("ObjectChecks 完整覆盖测试")
public class ObjectChecksTest {

    @Test
    @DisplayName("测试 exists 方法")
    void testExists() {
        // 测试对象不为 null 的情况
        Object obj = new Object();
        assertTrue(ObjectChecks.exists(obj));

        // 测试对象为 null 的情况
        assertFalse(ObjectChecks.exists(null));
    }

    @Test
    @DisplayName("测试 notNull 方法")
    void testNotNull() {
        // 测试对象不为 null 的情况
        Object obj = new Object();
        assertTrue(ObjectChecks.notNull(obj));

        // 测试对象为 null 的情况
        assertFalse(ObjectChecks.notNull(null));
    }

    @Test
    @DisplayName("测试 isNull 方法")
    void testIsNull() {
        // 测试对象为 null 的情况
        assertTrue(ObjectChecks.isNull(null));

        // 测试对象不为 null 的情况
        Object obj = new Object();
        assertFalse(ObjectChecks.isNull(obj));
    }

    @Test
    @DisplayName("测试 instanceOf 方法")
    void testInstanceOf() {
        // 测试类型为 null 的情况
        Object obj = new Object();
        assertFalse(ObjectChecks.instanceOf(obj, null));

        // 测试对象为 null 的情况
        assertFalse(ObjectChecks.instanceOf(null, Object.class));

        // 测试对象是指定类型的实例的情况
        Object obj2 = new Object();
        assertTrue(ObjectChecks.instanceOf(obj2, Object.class));

        // 测试对象不是指定类型的实例的情况
        Object obj3 = new Object();
        assertFalse(ObjectChecks.instanceOf(obj3, String.class));
    }

    @Test
    @DisplayName("测试 notInstanceOf 方法")
    void testNotInstanceOf() {
        // 测试类型为 null 的情况
        Object obj = new Object();
        assertFalse(ObjectChecks.notInstanceOf(obj, null));

        // 测试对象为 null 的情况
        assertTrue(ObjectChecks.notInstanceOf(null, Object.class));

        // 测试对象不是指定类型的实例的情况
        Object obj3 = new Object();
        assertTrue(ObjectChecks.notInstanceOf(obj3, String.class));

        // 测试对象是指定类型的实例的情况
        Object obj2 = new Object();
        assertFalse(ObjectChecks.notInstanceOf(obj2, Object.class));
    }

    @Test
    @DisplayName("测试 allNotNull 方法")
    void testAllNotNull() {
        // 测试参数为 null 的情况
        assertFalse(ObjectChecks.allNotNull((Object[]) null));

        // 测试参数数组中有 null 的情况
        assertFalse(ObjectChecks.allNotNull(new Object[]{new Object(), null}));

        // 测试参数数组中没有 null 的情况
        assertTrue(ObjectChecks.allNotNull(new Object[]{new Object(), new Object()}));
    }

    @Test
    @DisplayName("测试 notEmpty 方法 (Collection)")
    void testNotEmptyCollection() {
        // 测试集合为 null 的情况
        assertFalse(ObjectChecks.notEmpty((Collection<?>) null));

        // 测试集合为空的情况
        Collection<Object> emptyCollection = new ArrayList<>();
        assertFalse(ObjectChecks.notEmpty(emptyCollection));

        // 测试集合不为空的情况
        Collection<Object> nonEmptyCollection = new ArrayList<>();
        nonEmptyCollection.add(new Object());
        assertTrue(ObjectChecks.notEmpty(nonEmptyCollection));
    }

    @Test
    @DisplayName("测试 notEmpty 方法 (Map)")
    void testNotEmptyMap() {
        // 测试映射为 null 的情况
        assertFalse(ObjectChecks.notEmpty((Map<?, ?>) null));

        // 测试映射为空的情况
        Map<Object, Object> emptyMap = new HashMap<>();
        assertFalse(ObjectChecks.notEmpty(emptyMap));

        // 测试映射不为空的情况
        Map<Object, Object> nonEmptyMap = new HashMap<>();
        nonEmptyMap.put("key", "value");
        assertTrue(ObjectChecks.notEmpty(nonEmptyMap));
    }
}
