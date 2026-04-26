package com.chao.failfast.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.chao.failfast.autoconfigure.FailFastAutoConfiguration;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.spi.config.FailFastConfigurer;
import com.chao.failfast.spi.filter.SkipPrefixRegistry;
import com.chao.failfast.spi.filter.SkipTypeRegistry;
import com.chao.failfast.spi.validation.ValidatorRegistry;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.MessageSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for registry debug log coverage.
 */
class RegistryDebugLogTest {

    @Test
    void skipTypeRegistryDebugLogCoverage() {
        FailureProperties props = new FailureProperties();
        MessageSource messageSource = mock(MessageSource.class);
        FailFastAutoConfiguration config = new FailFastAutoConfiguration(props, messageSource);

        @SuppressWarnings("unchecked")
        ObjectProvider<FailFastConfigurer> provider = mock(ObjectProvider.class);
        when(provider.orderedStream()).thenAnswer(inv -> java.util.stream.Stream.empty());

        Logger logger = (Logger) LoggerFactory.getLogger(FailFastAutoConfiguration.class);
        Level oldLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        try {
            SkipTypeRegistry registry = config.skipTypeRegistry(provider);
            assertNotNull(registry);
        } finally {
            logger.setLevel(oldLevel);
        }
    }

    @Test
    void skipPrefixRegistryDebugLogCoverage() {
        FailureProperties props = new FailureProperties();
        MessageSource messageSource = mock(MessageSource.class);
        FailFastAutoConfiguration config = new FailFastAutoConfiguration(props, messageSource);

        @SuppressWarnings("unchecked")
        ObjectProvider<FailFastConfigurer> provider = mock(ObjectProvider.class);
        when(provider.orderedStream()).thenAnswer(inv -> java.util.stream.Stream.empty());

        Logger logger = (Logger) LoggerFactory.getLogger(FailFastAutoConfiguration.class);
        Level oldLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        try {
            SkipPrefixRegistry registry = config.skipPrefixRegistry(provider);
            assertNotNull(registry);
        } finally {
            logger.setLevel(oldLevel);
        }
    }

    @Test
    void validatorRegistryDebugLogCoverage() {
        FailureProperties props = new FailureProperties();
        MessageSource messageSource = mock(MessageSource.class);
        FailFastAutoConfiguration config = new FailFastAutoConfiguration(props, messageSource);

        @SuppressWarnings("unchecked")
        ObjectProvider<FailFastConfigurer> provider = mock(ObjectProvider.class);
        when(provider.orderedStream()).thenAnswer(inv -> java.util.stream.Stream.empty());

        Logger logger = (Logger) LoggerFactory.getLogger(FailFastAutoConfiguration.class);
        Level oldLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        try {
            ValidatorRegistry registry = config.validatorRegistry(provider);
            assertNotNull(registry);
        } finally {
            logger.setLevel(oldLevel);
        }
    }
}
