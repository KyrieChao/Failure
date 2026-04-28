package com.chao.failure.internal.core;

import com.chao.failure.internal.core.i18n.LocaleRouter;
import com.chao.failure.internal.core.i18n.LocalizedTexts;
import com.chao.failure.spi.i18n.LocalizedResponseResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class LocalizedTextsTest {

    @AfterEach
    void tearDown() {
        LocaleRouter.setDefault(new LocalizedResponseResolver() {
        });
    }

    @Test
    void shouldUseResolverMessageWhenProvided() {
        LocaleRouter.setDefault(new LocalizedResponseResolver() {
            @Override
            public String resolveMessage(ResponseCode code, Locale locale) {
                return "RESOLVED_MESSAGE";
            }
        });
        String text = LocalizedTexts.message(ResponseCode.of(40001, "{x}"));
        assertThat(text).isEqualTo("RESOLVED_MESSAGE");
    }

    @Test
    void shouldUseResolverDetailWhenProvided() {
        LocaleRouter.setDefault(new LocalizedResponseResolver() {
            @Override
            public String resolveDetail(ResponseCode code, String detail, Locale locale) {
                return "RESOLVED_DETAIL";
            }
        });
        String text = LocalizedTexts.detail(ResponseCode.of(40001, "{x}"), "detail");
        assertThat(text).isEqualTo("RESOLVED_DETAIL");
    }
}
