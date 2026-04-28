package com.chao.failure.internal.core;

import com.chao.failure.config.mapping.CodeMappingConfig;
import com.chao.failure.config.properties.FailureProperties;
import com.chao.failure.validator.ValidatorPackageValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExCaptureMethodNameBranchesTest {

    static final class InternalSuffixValidator {
        private InternalSuffixValidator() {
        }

        static String capture() {
            return Ex.captureMethodName();
        }
    }

    @AfterEach
    void tearDown() {
        Ex.setContext(null);
        Ex.setSkipPrefixRegistry(null);
    }

    @Test
    void captureMethodNameCoversValidatorPackageFilterBranch() {
        FailureProperties props = mock(FailureProperties.class);
        when(props.isShadowTrace()).thenReturn(true);
        when(props.isTrimStackTrace()).thenReturn(false);
        when(props.isMethodValidationEnabled()).thenReturn(false);
        when(props.isDebugSnapshot()).thenReturn(false);

        FailureContext ctx = new FailureContext(props, mock(CodeMappingConfig.class), null);
        Ex.setContext(ctx);

        String method = ValidatorPackageValidator.capture();
        assertThat(method).isNotBlank();
    }

    @Test
    void captureMethodNameCoversValidatorSuffixFilterBranch() {
        FailureProperties props = mock(FailureProperties.class);
        when(props.isShadowTrace()).thenReturn(true);
        when(props.isTrimStackTrace()).thenReturn(false);
        when(props.isMethodValidationEnabled()).thenReturn(false);
        when(props.isDebugSnapshot()).thenReturn(false);

        FailureContext ctx = new FailureContext(props, mock(CodeMappingConfig.class), null);
        Ex.setContext(ctx);

        String method = InternalSuffixValidator.capture();
        assertThat(method).isNotBlank();
    }

    @Test
    void captureMethodNameCoversEndsWithValidatorFilterBranch() {
        FailureProperties props = mock(FailureProperties.class);
        when(props.isShadowTrace()).thenReturn(true);
        when(props.isTrimStackTrace()).thenReturn(false);
        when(props.isMethodValidationEnabled()).thenReturn(false);
        when(props.isDebugSnapshot()).thenReturn(false);

        FailureContext ctx = new FailureContext(props, mock(CodeMappingConfig.class), null);
        Ex.setContext(ctx);

        String method = SomeValidator.capture();
        assertThat(method).isNotBlank();
    }
}

