package com.chao.failure.internal.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ValidationObservers 100% 覆盖率测试
 */
@DisplayName("ValidationObservers 完整覆盖测试")
public class ValidationEventManagerTest {

    // 测试用的 ValidationObserver 实现
    static class TestEventListener implements ValidationEventListener {
        public String lastSource;
        public String lastScene;
        public long lastDurationNanos;
        public boolean lastSuccess;
        public String lastErrorCode;
        public String lastConstraint;

        @Override
        public void onValidationStart(String source, String scene) {
            this.lastSource = source;
            this.lastScene = scene;
        }

        @Override
        public void onValidationEnd(String source, long durationNanos, boolean success) {
            this.lastSource = source;
            this.lastDurationNanos = durationNanos;
            this.lastSuccess = success;
        }

        @Override
        public void onValidationFailure(String source, String errorCode) {
            this.lastSource = source;
            this.lastErrorCode = errorCode;
        }

        @Override
        public void onViolation(String source, String constraint) {
            this.lastSource = source;
            this.lastConstraint = constraint;
        }
    }

    // 测试用的 ValidationObserver 实现，会抛出异常
    static class ErrorEventListener implements ValidationEventListener {
        @Override
        public void onValidationStart(String source, String scene) {
            throw new RuntimeException("Test exception");
        }

        @Override
        public void onValidationEnd(String source, long durationNanos, boolean success) {
            throw new RuntimeException("Test exception");
        }

        @Override
        public void onValidationFailure(String source, String errorCode) {
            throw new RuntimeException("Test exception");
        }

        @Override
        public void onViolation(String source, String constraint) {
            throw new RuntimeException("Test exception");
        }
    }

    @BeforeEach
    void setUp() {
        // 重置静态状态
        ValidationEventManager.setObserver(ValidationEventListener.NO_OP);
    }

    @Test
    @DisplayName("测试 setObserver 方法 - 传入非 null 值")
    void testSetObserverWithNonNull() {
        TestEventListener testObserver = new TestEventListener();
        ValidationEventManager.setObserver(testObserver);
        assertEquals(testObserver, ValidationEventManager.getObserver());
    }

    @Test
    @DisplayName("测试 setObserver 方法 - 传入 null 值")
    void testSetObserverWithNull() {
        ValidationEventListener originalObserver = ValidationEventManager.getObserver();
        ValidationEventManager.setObserver(null);
        assertEquals(originalObserver, ValidationEventManager.getObserver());
    }

    @Test
    @DisplayName("测试 notifyStart 方法 - 正常情况")
    void testNotifyStartNormal() {
        TestEventListener testObserver = new TestEventListener();
        ValidationEventManager.setObserver(testObserver);
        String source = "test-source";
        String scene = "test-scene";

        ValidationEventManager.notifyStart(source, scene);
        assertEquals(source, testObserver.lastSource);
        assertEquals(scene, testObserver.lastScene);
    }

    @Test
    @DisplayName("测试 notifyStart 方法 - 异常情况")
    void testNotifyStartException() {
        ErrorEventListener errorObserver = new ErrorEventListener();
        ValidationEventManager.setObserver(errorObserver);

        // 不应该抛出异常
        ValidationEventManager.notifyStart("test-source", "test-scene");
        // 验证没有抛出异常
        assertTrue(true);
    }

    @Test
    @DisplayName("测试 notifyStart 方法 - 异常情况（DEBUG 级别）")
    void testNotifyStartExceptionWithDebug() {
        // 获取 logger 并保存原始级别
        Logger logger = LoggerFactory.getLogger(ValidationEventManager.class);
        ch.qos.logback.classic.Logger logbackLogger = null;
        ch.qos.logback.classic.Level originalLevel = null;
        
        // 尝试设置日志级别为 DEBUG
        try {
            if (logger instanceof ch.qos.logback.classic.Logger) {
                logbackLogger = (ch.qos.logback.classic.Logger) logger;
                originalLevel = logbackLogger.getLevel();
                logbackLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
            }
            
            ErrorEventListener errorObserver = new ErrorEventListener();
            ValidationEventManager.setObserver(errorObserver);

            // 不应该抛出异常
            ValidationEventManager.notifyStart("test-source", "test-scene");
            // 验证没有抛出异常
            assertTrue(true);
        } finally {
            // 恢复原始日志级别
            if (logbackLogger != null && originalLevel != null) {
                logbackLogger.setLevel(originalLevel);
            }
        }
    }

    @Test
    @DisplayName("测试 notifyStart 方法 - 异常情况（INFO 级别）")
    void testNotifyStartExceptionWithInfo() {
        // 获取 logger 并保存原始级别
        Logger logger = LoggerFactory.getLogger(ValidationEventManager.class);
        ch.qos.logback.classic.Logger logbackLogger = null;
        ch.qos.logback.classic.Level originalLevel = null;
        
        // 尝试设置日志级别为 INFO
        try {
            if (logger instanceof ch.qos.logback.classic.Logger) {
                logbackLogger = (ch.qos.logback.classic.Logger) logger;
                originalLevel = logbackLogger.getLevel();
                logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);
            }
            
            ErrorEventListener errorObserver = new ErrorEventListener();
            ValidationEventManager.setObserver(errorObserver);

            // 不应该抛出异常
            ValidationEventManager.notifyStart("test-source", "test-scene");
            // 验证没有抛出异常
            assertTrue(true);
        } finally {
            // 恢复原始日志级别
            if (logbackLogger != null && originalLevel != null) {
                logbackLogger.setLevel(originalLevel);
            }
        }
    }

    @Test
    @DisplayName("测试 notifyEnd 方法 - 正常情况")
    void testNotifyEndNormal() {
        TestEventListener testObserver = new TestEventListener();
        ValidationEventManager.setObserver(testObserver);
        String source = "test-source";
        long durationNanos = 1000L;
        boolean success = true;

        ValidationEventManager.notifyEnd(source, durationNanos, success);
        assertEquals(source, testObserver.lastSource);
        assertEquals(durationNanos, testObserver.lastDurationNanos);
        assertEquals(success, testObserver.lastSuccess);
    }

    @Test
    @DisplayName("测试 notifyEnd 方法 - 异常情况")
    void testNotifyEndException() {
        ErrorEventListener errorObserver = new ErrorEventListener();
        ValidationEventManager.setObserver(errorObserver);

        // 不应该抛出异常
        ValidationEventManager.notifyEnd("test-source", 1000L, true);
        // 验证没有抛出异常
        assertTrue(true);
    }

    @Test
    @DisplayName("测试 notifyEnd 方法 - 异常情况（DEBUG 级别）")
    void testNotifyEndExceptionWithDebug() {
        // 获取 logger 并保存原始级别
        Logger logger = LoggerFactory.getLogger(ValidationEventManager.class);
        ch.qos.logback.classic.Logger logbackLogger = null;
        ch.qos.logback.classic.Level originalLevel = null;
        
        // 尝试设置日志级别为 DEBUG
        try {
            if (logger instanceof ch.qos.logback.classic.Logger) {
                logbackLogger = (ch.qos.logback.classic.Logger) logger;
                originalLevel = logbackLogger.getLevel();
                logbackLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
            }
            
            ErrorEventListener errorObserver = new ErrorEventListener();
            ValidationEventManager.setObserver(errorObserver);

            // 不应该抛出异常
            ValidationEventManager.notifyEnd("test-source", 1000L, true);
            // 验证没有抛出异常
            assertTrue(true);
        } finally {
            // 恢复原始日志级别
            if (logbackLogger != null && originalLevel != null) {
                logbackLogger.setLevel(originalLevel);
            }
        }
    }

    @Test
    @DisplayName("测试 notifyEnd 方法 - 异常情况（INFO 级别）")
    void testNotifyEndExceptionWithInfo() {
        // 获取 logger 并保存原始级别
        Logger logger = LoggerFactory.getLogger(ValidationEventManager.class);
        ch.qos.logback.classic.Logger logbackLogger = null;
        ch.qos.logback.classic.Level originalLevel = null;
        
        // 尝试设置日志级别为 INFO
        try {
            if (logger instanceof ch.qos.logback.classic.Logger) {
                logbackLogger = (ch.qos.logback.classic.Logger) logger;
                originalLevel = logbackLogger.getLevel();
                logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);
            }
            
            ErrorEventListener errorObserver = new ErrorEventListener();
            ValidationEventManager.setObserver(errorObserver);

            // 不应该抛出异常
            ValidationEventManager.notifyEnd("test-source", 1000L, true);
            // 验证没有抛出异常
            assertTrue(true);
        } finally {
            // 恢复原始日志级别
            if (logbackLogger != null && originalLevel != null) {
                logbackLogger.setLevel(originalLevel);
            }
        }
    }

    @Test
    @DisplayName("测试 notifyFailure 方法 - 正常情况")
    void testNotifyFailureNormal() {
        TestEventListener testObserver = new TestEventListener();
        ValidationEventManager.setObserver(testObserver);
        String source = "test-source";
        String errorCode = "400";

        ValidationEventManager.notifyFailure(source, errorCode);
        assertEquals(source, testObserver.lastSource);
        assertEquals(errorCode, testObserver.lastErrorCode);
    }

    @Test
    @DisplayName("测试 notifyFailure 方法 - 异常情况")
    void testNotifyFailureException() {
        ErrorEventListener errorObserver = new ErrorEventListener();
        ValidationEventManager.setObserver(errorObserver);

        // 不应该抛出异常
        ValidationEventManager.notifyFailure("test-source", "400");
        // 验证没有抛出异常
        assertTrue(true);
    }

    @Test
    @DisplayName("测试 notifyFailure 方法 - 异常情况（DEBUG 级别）")
    void testNotifyFailureExceptionWithDebug() {
        // 获取 logger 并保存原始级别
        Logger logger = LoggerFactory.getLogger(ValidationEventManager.class);
        ch.qos.logback.classic.Logger logbackLogger = null;
        ch.qos.logback.classic.Level originalLevel = null;
        
        // 尝试设置日志级别为 DEBUG
        try {
            if (logger instanceof ch.qos.logback.classic.Logger) {
                logbackLogger = (ch.qos.logback.classic.Logger) logger;
                originalLevel = logbackLogger.getLevel();
                logbackLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
            }
            
            ErrorEventListener errorObserver = new ErrorEventListener();
            ValidationEventManager.setObserver(errorObserver);

            // 不应该抛出异常
            ValidationEventManager.notifyFailure("test-source", "400");
            // 验证没有抛出异常
            assertTrue(true);
        } finally {
            // 恢复原始日志级别
            if (logbackLogger != null && originalLevel != null) {
                logbackLogger.setLevel(originalLevel);
            }
        }
    }

    @Test
    @DisplayName("测试 notifyFailure 方法 - 异常情况（INFO 级别）")
    void testNotifyFailureExceptionWithInfo() {
        // 获取 logger 并保存原始级别
        Logger logger = LoggerFactory.getLogger(ValidationEventManager.class);
        ch.qos.logback.classic.Logger logbackLogger = null;
        ch.qos.logback.classic.Level originalLevel = null;
        
        // 尝试设置日志级别为 INFO
        try {
            if (logger instanceof ch.qos.logback.classic.Logger) {
                logbackLogger = (ch.qos.logback.classic.Logger) logger;
                originalLevel = logbackLogger.getLevel();
                logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);
            }
            
            ErrorEventListener errorObserver = new ErrorEventListener();
            ValidationEventManager.setObserver(errorObserver);

            // 不应该抛出异常
            ValidationEventManager.notifyFailure("test-source", "400");
            // 验证没有抛出异常
            assertTrue(true);
        } finally {
            // 恢复原始日志级别
            if (logbackLogger != null && originalLevel != null) {
                logbackLogger.setLevel(originalLevel);
            }
        }
    }

    @Test
    @DisplayName("测试 notifyViolation 方法 - 正常情况")
    void testNotifyViolationNormal() {
        TestEventListener testObserver = new TestEventListener();
        ValidationEventManager.setObserver(testObserver);
        String source = "test-source";
        String constraint = "NotBlank";

        ValidationEventManager.notifyViolation(source, constraint);
        assertEquals(source, testObserver.lastSource);
        assertEquals(constraint, testObserver.lastConstraint);
    }

    @Test
    @DisplayName("测试 notifyViolation 方法 - 异常情况")
    void testNotifyViolationException() {
        ErrorEventListener errorObserver = new ErrorEventListener();
        ValidationEventManager.setObserver(errorObserver);

        // 不应该抛出异常
        ValidationEventManager.notifyViolation("test-source", "NotBlank");
        // 验证没有抛出异常
        assertTrue(true);
    }

    @Test
    @DisplayName("测试 notifyViolation 方法 - 异常情况（DEBUG 级别）")
    void testNotifyViolationExceptionWithDebug() {
        // 获取 logger 并保存原始级别
        Logger logger = LoggerFactory.getLogger(ValidationEventManager.class);
        ch.qos.logback.classic.Logger logbackLogger = null;
        ch.qos.logback.classic.Level originalLevel = null;
        
        // 尝试设置日志级别为 DEBUG
        try {
            if (logger instanceof ch.qos.logback.classic.Logger) {
                logbackLogger = (ch.qos.logback.classic.Logger) logger;
                originalLevel = logbackLogger.getLevel();
                logbackLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
            }
            
            ErrorEventListener errorObserver = new ErrorEventListener();
            ValidationEventManager.setObserver(errorObserver);

            // 不应该抛出异常
            ValidationEventManager.notifyViolation("test-source", "NotBlank");
            // 验证没有抛出异常
            assertTrue(true);
        } finally {
            // 恢复原始日志级别
            if (logbackLogger != null && originalLevel != null) {
                logbackLogger.setLevel(originalLevel);
            }
        }
    }

    @Test
    @DisplayName("测试 notifyViolation 方法 - 异常情况（INFO 级别）")
    void testNotifyViolationExceptionWithInfo() {
        // 获取 logger 并保存原始级别
        Logger logger = LoggerFactory.getLogger(ValidationEventManager.class);
        ch.qos.logback.classic.Logger logbackLogger = null;
        ch.qos.logback.classic.Level originalLevel = null;
        
        // 尝试设置日志级别为 INFO
        try {
            if (logger instanceof ch.qos.logback.classic.Logger) {
                logbackLogger = (ch.qos.logback.classic.Logger) logger;
                originalLevel = logbackLogger.getLevel();
                logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);
            }
            
            ErrorEventListener errorObserver = new ErrorEventListener();
            ValidationEventManager.setObserver(errorObserver);

            // 不应该抛出异常
            ValidationEventManager.notifyViolation("test-source", "NotBlank");
            // 验证没有抛出异常
            assertTrue(true);
        } finally {
            // 恢复原始日志级别
            if (logbackLogger != null && originalLevel != null) {
                logbackLogger.setLevel(originalLevel);
            }
        }
    }

    @Test
    @DisplayName("测试默认观察者 NO_OP")
    void testDefaultObserver() {
        ValidationEventManager.setObserver(ValidationEventListener.NO_OP);

        // 调用所有方法，应该不会抛出异常
        ValidationEventManager.notifyStart("test-source", "test-scene");
        ValidationEventManager.notifyEnd("test-source", 1000L, true);
        ValidationEventManager.notifyFailure("test-source", "400");
        ValidationEventManager.notifyViolation("test-source", "NotBlank");

        // 验证没有抛出异常
        assertTrue(true);
    }

    @Test
    @DisplayName("ValidationObservers")
    void ValidationObservers() {
        ValidationEventManager observers = new ValidationEventManager();

    }
}
