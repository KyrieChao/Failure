package com.chao.failure.internal.policy;

import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DefaultErrorPolicy 测试")
class DefaultErrorPolicyTest {

    private final DefaultErrorPolicy policy = DefaultErrorPolicy.INSTANCE;

    @Test
@DisplayName("display")
    void instanceShouldReturnSingletonInstance() {
        assertThat(policy).isSameAs(DefaultErrorPolicy.INSTANCE);
    }

    @Test
@DisplayName("display")
    void defaultCodeShouldReturnValidationError500Dynamic() {
        ResponseCode code = policy.defaultCode();
        assertThat(code).isEqualTo(ResponseCode.VALIDATION_ERROR_500_DYNAMIC);
    }

    @Test
@DisplayName("display")
    void defaultDetailShouldReturnDescriptionWhenAvailable() {
        ResponseCode code = ResponseCode.of(400, "Message", "Description");
        String detail = policy.defaultDetail(code);
        assertThat(detail).isEqualTo("Description");
    }

    @Test
@DisplayName("display")
    void defaultDetailShouldReturnMessageWhenDescriptionNotAvailable() {
        ResponseCode code = ResponseCode.of(400, "Message");
        String detail = policy.defaultDetail(code);
        assertThat(detail).isEqualTo("Message");
    }

    @Test
@DisplayName("display")
    void defaultDetailShouldReturnNullWhenNeitherDescriptionNorMessageAvailable() {
        ResponseCode code = ResponseCode.of(400);
        String detail = policy.defaultDetail(code);
        assertThat(detail).isNull();
    }

    @Test
@DisplayName("display")
    void captureInvalidValueShouldReturnTrueWhenContextIsNull() {
        boolean result = policy.captureInvalidValue(null);
        assertThat(result).isTrue();
    }

    @Test
@DisplayName("display")
    void captureInvalidValueShouldReturnTrueWhenDebugSnapshotIsTrue() {
        FailureContext context = Mockito.mock(FailureContext.class);
        Mockito.when(context.isDebugSnapshot()).thenReturn(true);
        
        boolean result = policy.captureInvalidValue(context);
        assertThat(result).isTrue();
    }

    @Test
@DisplayName("display")
    void captureInvalidValueShouldReturnFalseWhenDebugSnapshotIsFalse() {
        FailureContext context = Mockito.mock(FailureContext.class);
        Mockito.when(context.isDebugSnapshot()).thenReturn(false);
        
        boolean result = policy.captureInvalidValue(context);
        assertThat(result).isFalse();
    }
}
