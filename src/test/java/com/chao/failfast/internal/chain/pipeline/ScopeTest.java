package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScopeTest {

    @Test
    void testConstructor() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        String item = "test item";
        String path = "test.path";
        Scope<String> scope = new Scope<>(chain, item, path);
        assertNotNull(scope);
    }

    @Test
    void testIt() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        String item = "test item";
        String path = "test.path";
        Scope<String> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.it();
        assertNotNull(pathEntry);
        assertEquals(item, pathEntry.value());
        assertEquals(path, pathEntry.path());
    }

    @Test
    void testField() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test value");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<String> fieldRef = scope.field(TestItem::getValue);
        assertNotNull(fieldRef);
        assertEquals("test value", fieldRef.value());
    }

    @Test
    void testFieldEntry() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test value");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        assertNotNull(pathEntry);
        assertEquals("test value", pathEntry.value());
    }

    @Test
    void testFieldWithFieldName() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test value");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<String> fieldRef = scope.field("customField", TestItem::getValue);
        assertNotNull(fieldRef);
        assertEquals("test value", fieldRef.value());
    }

    @Test
    void testFieldRefAs() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test value");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<String> fieldRef = scope.field("customField", TestItem::getValue);
        PathEntry<String> pathEntry = fieldRef.as("alias");
        assertNotNull(pathEntry);
        assertEquals("test value", pathEntry.value());
    }

    @Test
    void testFieldRefRef() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test value");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<String> fieldRef = scope.field("customField", TestItem::getValue);
        PathEntry<String> pathEntry = fieldRef.ref();
        assertNotNull(pathEntry);
        assertEquals("test value", pathEntry.value());
    }

    @Test
    void testNotNull() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        String item = "test item";
        String path = "test.path";
        Scope<String> scope = new Scope<>(chain, item, path);
        Scope<String> result = scope.notNull(ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testNotBlank() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test value");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.notBlank(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testNotBlankFieldRefOverload() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test value");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope.FieldRef<String> ref = scope.field(TestItem::getValue);
        Scope<TestItem> result = scope.notBlank(ref, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testPositive() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem(10);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<Integer> pathEntry = scope.fieldEntry(TestItem::getNumber);
        Scope<TestItem> result = scope.positive(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testEmail() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test@example.com");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.email(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testMobile() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("13800138000");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.mobile(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testIsTrue() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem(true);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<Boolean> pathEntry = scope.fieldEntry(TestItem::isFlag);
        Scope<TestItem> result = scope.isTrue(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testIsFalse() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem(false);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<Boolean> pathEntry = scope.fieldEntry(TestItem::isFlag);
        Scope<TestItem> result = scope.isFalse(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testNotEmptyCollection() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        List<String> list = new ArrayList<>();
        list.add("test");
        TestItem item = new TestItem(list);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<List<String>> pathEntry = scope.fieldEntry(TestItem::getList);
        Scope<TestItem> result = scope.notEmptyCollection(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testNotEmptyMap() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        TestItem item = new TestItem(map);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<Map<String, String>> pathEntry = scope.fieldEntry(TestItem::getMap);
        Scope<TestItem> result = scope.notEmptyMap(pathEntry, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testLength() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test value");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.length(pathEntry, 1, 10, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testBetween() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem(5);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<Integer> pathEntry = scope.fieldEntry(TestItem::getNumber);
        Scope<TestItem> result = scope.between(pathEntry, 1, 10, ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testMatches() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.matches(pathEntry, "^test$", ResponseCode.VALIDATION_ERROR_400);
        assertSame(scope, result);
    }

    @Test
    void testCheck() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.check(pathEntry, s -> s.equals("test"), ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertSame(scope, result);
    }

    @Test
    void testCheckWithSupplier() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope<TestItem> result = scope.check(pathEntry, () -> true, ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertSame(scope, result);
    }

    @Test
    void testWhen() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        boolean[] executed = {false};
        Scope<TestItem> result = scope.when(true, () -> { executed[0] = true; });
        assertSame(scope, result);
        assertTrue(executed[0]);
    }

    @Test
    void testWhenWithPredicate() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        boolean[] executed = {false};
        Scope<TestItem> result = scope.when(s -> s.getValue().equals("test"), () -> { executed[0] = true; });
        assertSame(scope, result);
        assertTrue(executed[0]);
    }

    @Test
    void testUnless() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        boolean[] executed = {false};
        Scope<TestItem> result = scope.unless(false, () -> { executed[0] = true; });
        assertSame(scope, result);
        assertTrue(executed[0]);
    }

    @Test
    void testUnlessWithPredicate() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        boolean[] executed = {false};
        Scope<TestItem> result = scope.unless(s -> s.getValue().equals("other"), () -> { executed[0] = true; });
        assertSame(scope, result);
        assertTrue(executed[0]);
    }

    @Test
    void testNested() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem nestedItem = new TestItem("nested");
        TestItem item = new TestItem(nestedItem);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        boolean[] executed = {false};
        Scope<TestItem> result = scope.nested(TestItem::getNested, nestedScope -> { executed[0] = true; });
        assertSame(scope, result);
        assertTrue(executed[0]);
    }

    @Test
    void testForEach() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");
        TestItem item = new TestItem(list);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        int[] count = {0};
        Scope<TestItem> result = scope.forEach(TestItem::getList, itemScope -> { count[0]++; });
        assertSame(scope, result);
        assertEquals(2, count[0]);
    }

    @Test
    void testForEachEntry() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        TestItem item = new TestItem(map);
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        int[] count = {0};
        Scope<TestItem> result = scope.forEachEntry(TestItem::getMap, (key, valueScope) -> { count[0]++; });
        assertSame(scope, result);
        assertEquals(2, count[0]);
    }

    @Test
    void testStopItemOnFail() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        Scope<TestItem> result = scope.stopItemOnFail();
        assertSame(scope, result);
    }

    @Test
    void testMerge() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        scope.merge();
        // 验证方法执行完成
    }

    // 测试辅助类
    private static class TestItem {
        private final String value;
        private final Integer number;
        private final Boolean flag;
        private final List<String> list;
        private final Map<String, String> map;
        private final TestItem nested;

        public TestItem(String value) {
            this.value = value;
            this.number = null;
            this.flag = null;
            this.list = null;
            this.map = null;
            this.nested = null;
        }

        public TestItem(Integer number) {
            this.value = null;
            this.number = number;
            this.flag = null;
            this.list = null;
            this.map = null;
            this.nested = null;
        }

        public TestItem(Boolean flag) {
            this.value = null;
            this.number = null;
            this.flag = flag;
            this.list = null;
            this.map = null;
            this.nested = null;
        }

        public TestItem(List<String> list) {
            this.value = null;
            this.number = null;
            this.flag = null;
            this.list = list;
            this.map = null;
            this.nested = null;
        }

        public TestItem(Map<String, String> map) {
            this.value = null;
            this.number = null;
            this.flag = null;
            this.list = null;
            this.map = map;
            this.nested = null;
        }

        public TestItem(TestItem nested) {
            this.value = null;
            this.number = null;
            this.flag = null;
            this.list = null;
            this.map = null;
            this.nested = nested;
        }

        public String getValue() {
            return value;
        }

        public Integer getNumber() {
            return number;
        }

        public Boolean isFlag() {
            return flag;
        }

        public List<String> getList() {
            return list;
        }

        public Map<String, String> getMap() {
            return map;
        }

        public TestItem getNested() {
            return nested;
        }
    }
}
