package com.chao.failfast.config;

import com.chao.failfast.spi.FailFastConfigurer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FailFastConfigurerSupportTest {

    @Test
    void testDefaultOrder() {
        FailFastConfigurerSupport support = new FailFastConfigurerSupport() {
        };
        assertEquals(0, support.getOrder());
    }

    @Test
    void testCustomOrder() {
        FailFastConfigurerSupport support = new FailFastConfigurerSupport() {
            @Override
            public int getOrder() {
                return 10;
            }
        };
        assertEquals(10, support.getOrder());
    }

    @Test
    void testImplementsFailFastConfigurer() {
        FailFastConfigurerSupport support = new FailFastConfigurerSupport() {
        };
        assertTrue(support instanceof FailFastConfigurer);
    }

    @Test
    void testDefaultMethodsFromFailFastConfigurer() {
        FailFastConfigurerSupport support = new FailFastConfigurerSupport() {
        };
        // These methods should be callable without throwing exceptions
        support.addValidationSkipTypes(null);
        support.addExceptionSkipPrefixes(null);
        support.addCustomValidators(null);
        // If we get here, no exceptions were thrown
        assertTrue(true);
    }
}
