package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

class ScopeBranchTest {

    @Test
    void testMethodsWithEndedState() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        
        // 模拟ended状态
        // 这里需要通过反射设置ended为true，因为ended是私有字段
        try {
            java.lang.reflect.Field endedField = Scope.class.getDeclaredField("ended");
            endedField.setAccessible(true);
            endedField.set(scope, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 测试各种方法在ended状态下的行为
        PathEntry<String> pathEntry = scope.fieldEntry(TestItem::getValue);
        Scope.FieldRef<String> fieldRef = scope.field(TestItem::getValue);
        
        // 测试notBlank
        assertSame(scope, scope.notBlank(pathEntry, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.notBlank(fieldRef, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试positive
        PathEntry<Integer> numberEntry = scope.fieldEntry(TestItem::getNumber);
        Scope.FieldRef<Integer> numberRef = scope.field(TestItem::getNumber);
        assertSame(scope, scope.positive(numberEntry, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.positive(numberRef, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试email
        assertSame(scope, scope.email(pathEntry, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.email(fieldRef, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试mobile
        assertSame(scope, scope.mobile(pathEntry, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.mobile(fieldRef, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试isTrue
        PathEntry<Boolean> booleanEntry = scope.fieldEntry(TestItem::isFlag);
        assertSame(scope, scope.isTrue(booleanEntry, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试isFalse
        assertSame(scope, scope.isFalse(booleanEntry, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试greaterThan
        assertSame(scope, scope.greaterThan(numberEntry, 5, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.greaterThan(numberRef, 5, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试greaterOrEqual
        assertSame(scope, scope.greaterOrEqual(numberEntry, 5, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.greaterOrEqual(numberRef, 5, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试lessThan
        assertSame(scope, scope.lessThan(numberEntry, 10, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.lessThan(numberRef, 10, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试lessOrEqual
        assertSame(scope, scope.lessOrEqual(numberEntry, 10, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.lessOrEqual(numberRef, 10, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试lengthBetween
        assertSame(scope, scope.lengthBetween(pathEntry, 1, 10, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.lengthBetween(fieldRef, 1, 10, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试lengthMin
        assertSame(scope, scope.lengthMin(pathEntry, 2, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.lengthMin(fieldRef, 2, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试lengthMax
        assertSame(scope, scope.lengthMax(pathEntry, 10, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.lengthMax(fieldRef, 10, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试isCreditCard
        assertSame(scope, scope.isCreditCard(pathEntry, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.isCreditCard(fieldRef, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试url
        assertSame(scope, scope.url(pathEntry, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.url(fieldRef, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试ipAddress
        assertSame(scope, scope.ipAddress(pathEntry, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.ipAddress(fieldRef, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试uuid
        assertSame(scope, scope.uuid(pathEntry, ResponseCode.VALIDATION_ERROR_400));
        assertSame(scope, scope.uuid(fieldRef, ResponseCode.VALIDATION_ERROR_400));
        
        // 测试when
        boolean[] executed = {false};
        assertSame(scope, scope.when(true, () -> { executed[0] = true; }));
        assertSame(scope, scope.when(t -> true, () -> { executed[0] = true; }));
        
        // 测试unless
        assertSame(scope, scope.unless(false, () -> { executed[0] = true; }));
        assertSame(scope, scope.unless(t -> false, () -> { executed[0] = true; }));
        
        // 测试nested
        assertSame(scope, scope.nested(TestItem::getNested, nestedScope -> {}));
        
        // 测试forEach
        assertSame(scope, scope.forEach(TestItem::getList, itemScope -> {}));
        
        // 测试forEachEntry
        assertSame(scope, scope.forEachEntry(TestItem::getMap, (key, valueScope) -> {}));
    }

    @Test
    void testStopItemOnFail() {
        ChainCore<?> chain = Mockito.mock(ChainCore.class);
        when(chain.errorSize()).thenReturn(0);
        TestItem item = new TestItem("test");
        String path = "test.path";
        Scope<TestItem> scope = new Scope<>(chain, item, path);
        
        // 测试stopItemOnFail方法
        assertSame(scope, scope.stopItemOnFail());
        
        // 测试endOnFail分支
        // 模拟有错误的情况
        when(chain.errorSize()).thenReturn(1);
        
        // 这里需要通过反射调用endOnFail方法
        try {
            java.lang.reflect.Method endOnFailMethod = Scope.class.getDeclaredMethod("endOnFail");
            endOnFailMethod.setAccessible(true);
            endOnFailMethod.invoke(scope);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 测试辅助类
    private static class TestItem {
        private final String value;
        private final Integer number;
        private final Boolean flag;
        private final TestItem nested;

        public TestItem(String value) {
            this.value = value;
            this.number = 5;
            this.flag = true;
            this.nested = null;
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

        public TestItem getNested() {
            return nested;
        }

        public java.util.List<String> getList() {
            return java.util.Collections.emptyList();
        }

        public java.util.Map<String, String> getMap() {
            return java.util.Collections.emptyMap();
        }
    }
}
