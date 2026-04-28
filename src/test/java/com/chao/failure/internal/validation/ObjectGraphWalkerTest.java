package com.chao.failure.internal.validation;

import com.chao.failure.validator.FastValidator;
import com.chao.failure.annotation.Scene;
import com.chao.failure.constant.Scenario;
import com.chao.failure.internal.core.ResponseCode;
import com.chao.failure.validator.TypedValidator;
import org.junit.jupiter.api.Test;

import java.util.IdentityHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectGraphWalkerTest {

    @Test
    void should_skipSceneRestrictedField_when_contextScenesAreNull() {
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true, (Scenario[]) null, new Class<?>[0]);
        RecursiveOption options = RecursiveOption.builder().build();

        ObjectGraphWalker.walk(
                new SceneRestrictedHolder(),
                "",
                new StringFailingTypedValidator(),
                context,
                options,
                0,
                new IdentityHashMap<>()
        );

        assertThat(context.isValid()).isTrue();
        assertThat(context.errorSize()).isZero();
    }

    @Test
    void should_validateSceneRestrictedField_when_contextSceneMatches() {
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true, new Scenario[]{Scenario.CREATE}, new Class<?>[0]);
        RecursiveOption options = RecursiveOption.builder().build();

        ObjectGraphWalker.walk(
                new SceneRestrictedHolder(),
                "",
                new StringFailingTypedValidator(),
                context,
                options,
                0,
                new IdentityHashMap<>()
        );

        assertThat(context.isValid()).isFalse();
        assertThat(context.errorSize()).isEqualTo(1);
    }

    private static final class StringFailingTypedValidator extends TypedValidator {
        StringFailingTypedValidator() {
            register(String.class, (value, validationContext) ->
                    validationContext.reportError(ResponseCode.VALIDATION_ERROR_400, "boom"));
        }
    }

    private static final class SceneRestrictedHolder {
        @Scene({Scenario.CREATE})
        private final String onlyForCreate = "secret";
    }
}
