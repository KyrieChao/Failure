package com.chao.failure.internal;

import com.chao.failure.internal.core.Chain;
import com.chao.failure.internal.core.Ex;
import com.chao.failure.exception.Business;
import com.chao.failure.config.mapping.CodeMappingConfig;
import com.chao.failure.internal.core.FailureContext;
import com.chao.failure.config.properties.FailureProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class DebugSnapshotTest {

    private FailureContext originalContext;

    @BeforeEach
    void setUp() {
        originalContext = Ex.getContext();
        FailureProperties properties = new FailureProperties();
        properties.setDebugSnapshot(true); // Enable debug snapshot
        FailureContext context = new FailureContext(properties, new CodeMappingConfig(properties), null);
        Ex.setContext(context);
    }

    @AfterEach
    void tearDown() {
        Ex.setContext(originalContext);
    }

    @Test
    void shouldIncludeValueSnapshotWhenDebugEnabled() {
        Business ex = assertThrows(Business.class, () -> {
            Chain.begin(true).notBlank("", null, "Test failure").fail();
        });

        System.out.println("Exception message: " + ex.toString());
        assertThat(ex.toString()).contains("val=");
    }

    @Test
    void shouldMaskMobile() {
        String mobile = "13800138000";
        Business ex = assertThrows(Business.class, () -> {
            Chain.begin(true).check(false, null, "Mobile error", mobile).fail();
        });

        assertThat(ex.toString()).contains("val=138****8000");
    }

    @Test
    void shouldMaskEmail() {
        String email = "test@example.com";
        Business ex = assertThrows(Business.class, () -> {
            Chain.begin(true).check(false, null, "Email error", email).fail();
        });
        assertThat(ex.toString()).contains("val=t****@example.com");
    }

    @Test
    void shouldTruncateLongValue() {
        String longStr = "a".repeat(100);
        Business ex = assertThrows(Business.class, () -> {
            Chain.begin(true).check(false, null, "Long error", longStr).fail();
        });

        assertThat(ex.toString()).contains("...");
        assertThat(ex.toString().length()).isLessThan(longStr.length() + 100); // Rough check
    }
}
