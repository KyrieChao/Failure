package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.internal.validation.ValidationObservers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChainCore Pipeline 测试")
class ChainCorePipelineTest {

    // 最小化实现，用于测试核心逻辑
    static class TestChain extends ChainCore<TestChain> {
        protected TestChain(boolean failFast, FastValidator.ValidationContext context) {
            super(failFast, context);
        }

        public static TestChain create(boolean failFast) {
            return new TestChain(failFast, null);
        }

        public static TestChain create(FastValidator.ValidationContext context) {
            return new TestChain(context.isFast(), context);
        }

        // 暴露 protected 方法用于测试
        public TestChain publicCheck(boolean condition, ResponseCode code, String detail) {
            return check(condition, code, detail);
        }

        public TestChain publicCheckRef(boolean condition, ResponseCode code, PathEntry<?> valueRef) {
            return checkRef(condition, code, valueRef);
        }

        public TestChain publicCheckWithPathAndConstraint(boolean condition, ResponseCode code, String detail, Object value, String path, String constraint, String source) {
            return checkWithPathAndConstraint(condition, code, detail, value, path, constraint, source);
        }

        public void publicNotifyViolation(String source, String constraint) {
            notifyViolation(source, constraint);
        }

        public void publicNotifyValidationStart(String source, String scene) {
            notifyValidationStart(source, scene);
        }

        public void publicNotifyValidationEnd(String source, long durationNanos, boolean success) {
            notifyValidationEnd(source, durationNanos, success);
        }

        public void publicNotifyValidationFailure(String source, String errorCode) {
            notifyValidationFailure(source, errorCode);
        }
    }

    @Test
@DisplayName("display")
    void notifyViolationShouldCallValidationObservers() {
        TestChain chain = TestChain.create(true);

        try (MockedStatic<ValidationObservers> mocked = Mockito.mockStatic(ValidationObservers.class)) {
            // 执行测试
            chain.publicNotifyViolation("test-source", "test-constraint");

            // 验证调用
            mocked.verify(() -> ValidationObservers.notifyViolation("test-source", "test-constraint"));
        }
    }

    @Test
@DisplayName("display")
    void notifyValidationStartShouldCallValidationObservers() {
        TestChain chain = TestChain.create(true);

        try (MockedStatic<ValidationObservers> mocked = Mockito.mockStatic(ValidationObservers.class)) {
            // 执行测试
            chain.publicNotifyValidationStart("test-source", "test-scene");

            // 验证调用
            mocked.verify(() -> ValidationObservers.notifyStart("test-source", "test-scene"));
        }
    }

    @Test
@DisplayName("display")
    void notifyValidationEndShouldCallValidationObservers() {
        TestChain chain = TestChain.create(true);

        try (MockedStatic<ValidationObservers> mocked = Mockito.mockStatic(ValidationObservers.class)) {
            // 执行测试
            chain.publicNotifyValidationEnd("test-source", 1000L, true);

            // 验证调用
            mocked.verify(() -> ValidationObservers.notifyEnd("test-source", 1000L, true));
        }
    }

    @Test
@DisplayName("display")
    void notifyValidationFailureShouldCallValidationObservers() {
        TestChain chain = TestChain.create(true);

        try (MockedStatic<ValidationObservers> mocked = Mockito.mockStatic(ValidationObservers.class)) {
            // 执行测试
            chain.publicNotifyValidationFailure("test-source", "test-error-code");

            // 验证调用
            mocked.verify(() -> ValidationObservers.notifyFailure("test-source", "test-error-code"));
        }
    }

    @Test
    @DisplayName("checkWithPathAndConstraint: 当约束不为空时应调用 notifyViolation")
    void checkWithPathAndConstraintShouldCallNotifyViolationWhenConstraintNotNull() {
        TestChain chain = TestChain.create(true);

        try (MockedStatic<ValidationObservers> mocked = Mockito.mockStatic(ValidationObservers.class)) {
            // 执行测试 - 条件�?false，会触发错误
            chain.publicCheckWithPathAndConstraint(false, ResponseCode.VALIDATION_ERROR_400, "Test error", "test-value", "test-path", "test-constraint", "test-source");

            // 验证调用
            mocked.verify(() -> ValidationObservers.notifyViolation("test-source", "test-constraint"));
        }
    }

    @Test
    @DisplayName("CheckSpec: of 方法应创建正确的实例")
    void checkSpecOfShouldCreateCorrectInstance() {
        // 测试无无效值的情况
        CheckSpec spec1 = CheckSpec.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        assertThat(spec1.code()).isEqualTo(ResponseCode.VALIDATION_ERROR_400);
        assertThat(spec1.detail()).isEqualTo("Test error");
        assertThat(spec1.invalidValue()).isNull();

        // 测试有无效值的情况
        CheckSpec spec2 = CheckSpec.of(ResponseCode.VALIDATION_ERROR_400, "Test error", "test-value");
        assertThat(spec2.code()).isEqualTo(ResponseCode.VALIDATION_ERROR_400);
        assertThat(spec2.detail()).isEqualTo("Test error");
        assertThat(spec2.invalidValue()).isEqualTo("test-value");
    }

    @Test
    @DisplayName("PathEntry: 应正确存储值和路径")
    void pathEntryShouldStoreValueAndPath() {
        PathEntry<String> entry = new PathEntry<>("test-value", "test-path");
        assertThat(entry.value()).isEqualTo("test-value");
        assertThat(entry.path()).isEqualTo("test-path");
    }
}
