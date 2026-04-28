package com.chao.failure.internal.chain.pipeline;

import com.chao.failure.internal.core.ResponseCode;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

class ScopeAdditionalTest {

    @Test
    void testGreaterThan() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem(10);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<Integer> pathEntry = scope.fieldEntry(TestItem::getNumber);
        Scope<TestItem> result = scope.greaterThan(pathEntry, 5, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testGreaterThanFieldRefOverload() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem(10);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<Integer> ref = scope.field(TestItem::getNumber);
        Scope<TestItem> result = scope.greaterThan(ref, 5, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testGreaterOrEqual() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem(10);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<Integer> pathEntry = scope.fieldEntry(TestItem::getNumber);
        Scope<TestItem> result = scope.greaterOrEqual(pathEntry, 10, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testGreaterOrEqualFieldRefOverload() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem(10);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<Integer> ref = scope.field(TestItem::getNumber);
        Scope<TestItem> result = scope.greaterOrEqual(ref, 10, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testLessThan() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem(5);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<Integer> pathEntry = scope.fieldEntry(TestItem::getNumber);
        Scope<TestItem> result = scope.lessThan(pathEntry, 10, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testLessThanFieldRefOverload() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem(5);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<Integer> ref = scope.field(TestItem::getNumber);
        Scope<TestItem> result = scope.lessThan(ref, 10, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testLessOrEqual() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem(5);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<Integer> pathEntry = scope.fieldEntry(TestItem::getNumber);
        Scope<TestItem> result = scope.lessOrEqual(pathEntry, 5, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testLessOrEqualFieldRefOverload() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem(5);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<Integer> ref = scope.field(TestItem::getNumber);
        Scope<TestItem> result = scope.lessOrEqual(ref, 5, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testLengthBetween() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.lengthBetween(pathEntry, 1, 10, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testLengthBetweenFieldRefOverload() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<String> ref = scope.field(TestItem::getValue);
        Scope<TestItem> result = scope.lengthBetween(ref, 1, 10, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testLengthMin() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.lengthMin(pathEntry, 2, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testLengthMinFieldRefOverload() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<String> ref = scope.field(TestItem::getValue);
        Scope<TestItem> result = scope.lengthMin(ref, 2, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testLengthMax() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.lengthMax(pathEntry, 10, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testLengthMaxFieldRefOverload() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<String> ref = scope.field(TestItem::getValue);
        Scope<TestItem> result = scope.lengthMax(ref, 10, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testIsCreditCard() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("4111111111111111");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.isCreditCard(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testIsCreditCardFieldRefOverload() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("4111111111111111");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<String> ref = scope.field(TestItem::getValue);
        Scope<TestItem> result = scope.isCreditCard(ref, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testUrl() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("https://www.example.com");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.url(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testUrlFieldRefOverload() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("https://www.example.com");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<String> ref = scope.field(TestItem::getValue);
        Scope<TestItem> result = scope.url(ref, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testIpAddress() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("192.168.1.1");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.ipAddress(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testIpAddressFieldRefOverload() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("192.168.1.1");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<String> ref = scope.field(TestItem::getValue);
        Scope<TestItem> result = scope.ipAddress(ref, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testUuid() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("550e8400-e29b-41d4-a716-446655440000");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.uuid(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testUuidFieldRefOverload() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("550e8400-e29b-41d4-a716-446655440000");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<String> ref = scope.field(TestItem::getValue);
        Scope<TestItem> result = scope.uuid(ref, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    // 测试辅助类
    private static class TestItem {
        private final String value;
        private final Integer number;

        public TestItem(String value) {
            this.value = value;
            this.number = null;
        }

        public TestItem(Integer number) {
            this.value = null;
            this.number = number;
        }

        public String getValue() {
            return value;
        }

        public Integer getNumber() {
            return number;
        }
    }
}
