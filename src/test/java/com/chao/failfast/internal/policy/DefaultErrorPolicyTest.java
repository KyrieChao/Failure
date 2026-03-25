package com.chao.failfast.internal.policy;

import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DefaultErrorPolicy 测试")
class DefaultErrorPolicyTest {

    private final DefaultErrorPolicy policy = DefaultErrorPolicy.INSTANCE;

    @Test
    @DisplayName("INSTANCE 应返回单例实例")
    void instanceShouldReturnSingletonInstance() {
        assertThat(DefaultErrorPolicy.INSTANCE).isSameAs(DefaultErrorPolicy.INSTANCE);
    }

    @Test
    @DisplayName("defaultCode: 应返回 VALIDATION_ERROR_500_DYNAMIC")
    void defaultCodeShouldReturnValidationError500Dynamic() {
        ResponseCode code = policy.defaultCode();
        assertThat(code).isEqualTo(ResponseCode.VALIDATION_ERROR_500_DYNAMIC);
    }

    @Test
    @DisplayName("defaultDetail: 当 code 有描述时应返回描述")
    void defaultDetailShouldReturnDescriptionWhenAvailable() {
        ResponseCode code = ResponseCode.of(400, "Message", "Description");
        String detail = policy.defaultDetail(code);
        assertThat(detail).isEqualTo("Description");
    }

    @Test
    @DisplayName("defaultDetail: 当 code 没有描述但有消息时应返回消息")
    void defaultDetailShouldReturnMessageWhenDescriptionNotAvailable() {
        ResponseCode code = ResponseCode.of(400, "Message");
        String detail = policy.defaultDetail(code);
        assertThat(detail).isEqualTo("Message");
    }

    @Test
    @DisplayName("defaultDetail: 当 code 既没有描述也没有消息时应返回 null")
    void defaultDetailShouldReturnNullWhenNeitherDescriptionNorMessageAvailable() {
        ResponseCode code = ResponseCode.of(400);
        String detail = policy.defaultDetail(code);
        assertThat(detail).isNull();
    }

    @Test
    @DisplayName("captureInvalidValue: 当 context 为 null 时应返回 true")
    void captureInvalidValueShouldReturnTrueWhenContextIsNull() {
        boolean result = policy.captureInvalidValue(null);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("captureInvalidValue: 当 context 的 debugSnapshot 为 true 时应返回 true")
    void captureInvalidValueShouldReturnTrueWhenDebugSnapshotIsTrue() {
        FailureContext context = Mockito.mock(FailureContext.class);
        Mockito.when(context.isDebugSnapshot()).thenReturn(true);
        
        boolean result = policy.captureInvalidValue(context);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("captureInvalidValue: 当 context 的 debugSnapshot 为 false 时应返回 false")
    void captureInvalidValueShouldReturnFalseWhenDebugSnapshotIsFalse() {
        FailureContext context = Mockito.mock(FailureContext.class);
        Mockito.when(context.isDebugSnapshot()).thenReturn(false);
        
        boolean result = policy.captureInvalidValue(context);
        assertThat(result).isFalse();
    }
}
