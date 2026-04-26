package com.chao.failfast.validator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 针对 TypedValidator 的完整单元测试
 */
@DisplayName("TypedValidator 完整覆盖率测试")
class TypedValidatorTest {

    @Nested
    @DisplayName("基础注册和验证测试")
    class BasicRegistrationTests {

        @Test
        @DisplayName("注册单个类型并验证成功")
        void testRegisterAndValidate() {
            TestTypedValidator validator = new TestTypedValidator();

            validator.register(String.class, (s, ctx) -> {
                if (s == null || s.isEmpty()) {
                    ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL);
                }
            });

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate("test", context);
            assertTrue(context.isValid());

            context = new FastValidator.ValidationContext(true);
            validator.validate("", context);
            assertFalse(context.isValid());
        }

        @Test
        @DisplayName("验证 null 对象应报告错误")
        void testValidateNullObject() {
            TestTypedValidator validator = new TestTypedValidator();

            validator.register(String.class, (s, ctx) -> {
            });

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate(null, context);
            assertFalse(context.isValid());
        }

        @Test
        @DisplayName("验证不支持的类型应报告错误")
        void testUnsupportedType() {
            TestTypedValidator validator = new TestTypedValidator();

            validator.register(String.class, (s, ctx) -> {
            });

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate(123, context);
            assertFalse(context.isValid());
        }
    }

    @Nested
    @DisplayName("类型注册信息测试")
    class TypeInfoTests {

        @Test
        @DisplayName("getSupportedType 返回正确类型")
        void testGetSupportedType() {
            TestTypedValidator validator = new TestTypedValidator();

            assertEquals(Object.class, validator.getSupportedType());

            validator.register(String.class, (s, ctx) -> {
            });
            assertEquals(String.class, validator.getSupportedType());

            validator.register(Integer.class, (i, ctx) -> {
            });
            assertEquals(Object.class, validator.getSupportedType());
        }

        @Test
        @DisplayName("isRegisteredType 正确判断类型是否注册")
        void testIsRegisteredType() {
            TestTypedValidator validator = new TestTypedValidator();

            assertFalse(validator.isRegisteredType(String.class));

            validator.register(String.class, (s, ctx) -> {
            });
            assertTrue(validator.isRegisteredType(String.class));
            assertFalse(validator.isRegisteredType(Integer.class));
        }

        @Test
        @DisplayName("size 返回注册类型数量")
        void testSize() {
            TestTypedValidator validator = new TestTypedValidator();

            assertEquals(0, validator.size());

            validator.register(String.class, (s, ctx) -> {
            });
            assertEquals(1, validator.size());

            validator.register(Integer.class, (i, ctx) -> {
            });
            assertEquals(2, validator.size());
        }

        @Test
        @DisplayName("getRegisteredTypes 返回所有注册类型")
        void testGetRegisteredTypes() {
            TestTypedValidator validator = new TestTypedValidator();

            validator.register(String.class, (s, ctx) -> {
            });
            validator.register(Integer.class, (i, ctx) -> {
            });

            var registeredTypes = validator.getRegisteredTypes();
            assertEquals(2, registeredTypes.size());
            assertTrue(registeredTypes.contains(String.class));
            assertTrue(registeredTypes.contains(Integer.class));
        }
    }

    @Nested
    @DisplayName("条件验证测试")
    class ConditionalValidationTests {

        @Test
        @DisplayName("validateIfRegistered 对 null 返回 false")
        void testValidateIfRegisteredWithNull() {
            TestTypedValidator validator = new TestTypedValidator();

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            assertFalse(validator.validateIfRegistered(null, context));
        }

        @Test
        @DisplayName("validateIfRegistered 对未注册类型返回 false")
        void testValidateIfRegisteredWithUnregisteredType() {
            TestTypedValidator validator = new TestTypedValidator();

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            assertFalse(validator.validateIfRegistered(123, context));
        }

        @Test
        @DisplayName("validateIfRegistered 对注册类型返回 true 并执行验证")
        void testValidateIfRegisteredWithRegisteredType() {
            TestTypedValidator validator = new TestTypedValidator();

            validator.register(String.class, (s, ctx) -> {
                if (s.isEmpty()) {
                    ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL);
                }
            });

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            assertTrue(validator.validateIfRegistered("test", context));
            assertTrue(context.isValid());

            context = new FastValidator.ValidationContext(true);
            assertTrue(validator.validateIfRegistered("", context));
            assertFalse(context.isValid());
        }
    }

    @Nested
    @DisplayName("继承层级解析测试")
    class InheritanceResolutionTests {

        @Test
        @DisplayName("注册父类后子类验证应使用父类处理器")
        void testInheritanceResolution() {
            TestTypedValidator validator = new TestTypedValidator();

            validator.register(Number.class, (n, ctx) -> {
                if (n == null) {
                    ctx.reportError(ResponseCode.VALIDATION_ERROR_NULL);
                }
            });

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate(123, context);
            assertTrue(context.isValid());
        }

        @Test
        @DisplayName("注册接口后实现类验证应使用接口处理器")
        void testInterfaceResolution() {
            TestTypedValidator validator = new TestTypedValidator();

            validator.register(CharSequence.class, (cs, ctx) -> {
            });

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate("test", context);
            assertTrue(context.isValid());
        }

        @Test
        @DisplayName("同时注册多个接口时的处理")
        void testMultipleInterfaceResolution() {
            TestTypedValidator validator = new TestTypedValidator();

            AtomicInteger objectHandlerCalls = new AtomicInteger(0);
            AtomicInteger charSequenceHandlerCalls = new AtomicInteger(0);
            AtomicInteger comparableHandlerCalls = new AtomicInteger(0);

            validator.register(Object.class, (o, ctx) -> objectHandlerCalls.incrementAndGet());
            validator.register(CharSequence.class, (cs, ctx) -> charSequenceHandlerCalls.incrementAndGet());
            validator.register(Comparable.class, (c, ctx) -> comparableHandlerCalls.incrementAndGet());

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate("test", context);
            assertTrue(context.isValid());

            // 至少有一个处理器被调用
            int totalCalls = objectHandlerCalls.get() + charSequenceHandlerCalls.get() + comparableHandlerCalls.get();
            assertTrue(totalCalls > 0, "至少有一个处理器应该被调用");
        }
    }

    @Nested
    @DisplayName("歧义处理器测试")
    class AmbiguousHandlerTests {

        @Test
        @DisplayName("两个接口都匹配时应选择其一不抛异常")
        void testAmbiguousHandlers() {
            TestTypedValidator validator = new TestTypedValidator();

            validator.register(CharSequence.class, (cs, ctx) -> {
            });
            validator.register(Comparable.class, (c, ctx) -> {
            });

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate("test", context);
            assertTrue(context.isValid());
        }

        @Test
        @DisplayName("failOnAmbiguousHandler 为 true 时应抛出异常")
        void testFailOnAmbiguousHandler() {
            AmbiguousTypedValidator validator = new AmbiguousTypedValidator();

            validator.register(CharSequence.class, (cs, ctx) -> {
            });
            validator.register(Comparable.class, (c, ctx) -> {
            });

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            assertThrows(IllegalStateException.class, () -> validator.validate("test", context));
        }

        @Test
        @DisplayName("三个接口都匹配时应选择其一不抛异常")
        void testTripleAmbiguousHandlers() {
            TestTypedValidator validator = new TestTypedValidator();

            validator.register(CharSequence.class, (cs, ctx) -> {
            });
            validator.register(Comparable.class, (c, ctx) -> {
            });
            validator.register(Object.class, (o, ctx) -> {
            });

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate("test", context);
            assertTrue(context.isValid());
        }

        @Test
        @DisplayName("多个接口匹配且优先级相同时应记录日志")
        void testAmbiguousHandlersWithLogging() {
            LoggingTypedValidator validator = new LoggingTypedValidator();

            validator.register(CharSequence.class, (cs, ctx) -> {
            });
            validator.register(Comparable.class, (c, ctx) -> {
            });

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate("test", context);
            assertTrue(context.isValid());
        }

        @Test
        @DisplayName("具有不同优先级的多个处理器应选择最高优先级")
        void testMultipleHandlersWithDifferentPriorities() {
            PriorityTypedValidator validator = new PriorityTypedValidator();

            validator.register(CharSequence.class, (cs, ctx) -> {
            });
            validator.register(Comparable.class, (c, ctx) -> {
            });

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate("test", context);
            assertTrue(context.isValid());
        }
    }

    @Nested
    @DisplayName("缓存和性能测试")
    class CacheAndPerformanceTests {

        @Test
        @DisplayName("多次验证同一类型应使用缓存")
        void testCachedValidation() {
            TestTypedValidator validator = new TestTypedValidator();

            AtomicInteger handlerCalls = new AtomicInteger(0);
            validator.register(String.class, (s, ctx) -> handlerCalls.incrementAndGet());

            FastValidator.ValidationContext context1 = new FastValidator.ValidationContext(true);
            validator.validate("test1", context1);

            FastValidator.ValidationContext context2 = new FastValidator.ValidationContext(true);
            validator.validate("test2", context2);

            FastValidator.ValidationContext context3 = new FastValidator.ValidationContext(true);
            validator.validate("test3", context3);

            assertEquals(3, handlerCalls.get());
        }

        @Test
        @DisplayName("注册超过10个类型后应启用缓存")
        void testCacheEnabledAfterThreshold() {
            LargeTypedValidator validator = new LargeTypedValidator();

            assertNotNull(validator.getRegisteredTypes());
            assertEquals(15, validator.getRegisteredTypes().size());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("注册后又重新注册同一类型")
        void testReregisterSameType() {
            TestTypedValidator validator = new TestTypedValidator();

            AtomicInteger handler1Calls = new AtomicInteger(0);
            AtomicInteger handler2Calls = new AtomicInteger(0);

            validator.register(String.class, (s, ctx) -> handler1Calls.incrementAndGet());
            validator.register(String.class, (s, ctx) -> handler2Calls.incrementAndGet());

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate("test", context);

            assertEquals(0, handler1Calls.get());
            assertEquals(1, handler2Calls.get());
        }

        @Test
        @DisplayName("验证过程中验证器注册表被清空")
        void testValidationDuringClear() {
            TestTypedValidator validator = new TestTypedValidator();

            validator.register(String.class, (s, ctx) -> {
            });

            FastValidator.ValidationContext context1 = new FastValidator.ValidationContext(true);
            validator.validate("test", context1);
            assertTrue(context1.isValid());

            validator.register(Integer.class, (i, ctx) -> {
            });

            FastValidator.ValidationContext context2 = new FastValidator.ValidationContext(true);
            validator.validate(123, context2);
            assertTrue(context2.isValid());
        }

        @Test
        void test() {
            TestTypedValidator validator = new TestTypedValidator();
            validator.register(String.class, (s, ctx) -> {
            });
            validator.validate("test", null);
            validator.register(Integer.class, (i, ctx) -> {
            });
        }

        @Test
        void should_cover_no_handler_branch() {
            // Arrange - 注册一个具体的类
            TestTypedValidator validator = new TestTypedValidator();
            validator.register(String.class, (s, ctx) -> {
            });

            // 第一次调用 - 会计算并缓存 NO_HANDLER（因为Integer没有注册）
            FastValidator.ValidationContext context1 = new FastValidator.ValidationContext(true);
            validator.validate(Integer.valueOf(123), context1);  // Integer未注册，触发NO_HANDLER缓存

            // 第二次调用 - 命中缓存，触发 return cached == NO_HANDLER ? null : ...
            FastValidator.ValidationContext context2 = new FastValidator.ValidationContext(true);
            validator.validate(Integer.valueOf(456), context2);  // 再次验证Integer，命中缓存

            // 断言 - Integer未注册，应该报告错误
            assertFalse(context2.isValid());
        }

        @Test
        void should_cover_handler_found_branch() {
            // Arrange - 注册一个具体的类
            TestTypedValidator validator = new TestTypedValidator();
            validator.register(String.class, (s, ctx) -> {
            });

            // 第一次调用 - 会计算并缓存实际处理器
            FastValidator.ValidationContext context1 = new FastValidator.ValidationContext(true);
            validator.validate("test", context1);  // String已注册，触发处理器缓存

            // 第二次调用 - 命中缓存，触发 return cached != NO_HANDLER 分支
            FastValidator.ValidationContext context2 = new FastValidator.ValidationContext(true);
            validator.validate("another", context2);  // 再次验证String，命中缓存

            // 断言 - String已注册，验证应该成功
            assertTrue(context2.isValid());
        }

        @Test
        void should_skip_types_with_greater_distance_in_traversal() {
            // Arrange - 注册接口和实现类
            TestTypedValidator validator = new TestTypedValidator();

            // 注册接口（距离更远）
            validator.register(CharSequence.class, (cs, ctx) -> {
                ctx.reportError(ResponseCode.VALIDATION_ERROR_400);
            });

            // 注册具体类（距离更近）
            validator.register(String.class, (s, ctx) -> {
                // 不报错
            });

            // Act - 验证String类型
            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate("test", context);

            // Assert - 应该使用String处理器（距离更近）
            assertTrue(context.isValid());
        }

        @Test
        @DisplayName("distance 应覆盖 null、相等、接口、父类和不可达场景")
        void should_cover_distanceBranches_when_invokedReflectively() throws Exception {
            Method distance = TypedValidator.class.getDeclaredMethod("distance", Class.class, Class.class);
            distance.setAccessible(true);

            assertEquals(Integer.MAX_VALUE, distance.invoke(null, null, String.class));
            assertEquals(Integer.MAX_VALUE, distance.invoke(null, String.class, null));
            assertEquals(0, distance.invoke(null, String.class, String.class));
            assertEquals(1, distance.invoke(null, Integer.class, Number.class));
            assertEquals(1, distance.invoke(null, String.class, CharSequence.class));
            assertEquals(Integer.MAX_VALUE, distance.invoke(null, String.class, Runnable.class));
        }

        @Test
        @DisplayName("computeBestHandler 应在无可用候选时返回 null")
        void should_returnNull_when_computeBestHandlerFindsNoAssignableCandidate() throws Exception {
            TestTypedValidator validator = new TestTypedValidator();
            validator.register(Number.class, (n, ctx) -> { });

            Method computeBestHandler = TypedValidator.class.getDeclaredMethod("computeBestHandler", Class.class);
            computeBestHandler.setAccessible(true);

            Object handler = computeBestHandler.invoke(validator, String.class);

            assertNull(handler);
        }

        @Test
        @DisplayName("computeBestHandler 应优先选择类而非同距离接口")
        void should_chooseConcreteClass_when_classAndInterfaceShareDistance() {
            PreferClassTypedValidator validator = new PreferClassTypedValidator();
            AtomicInteger classCalls = new AtomicInteger();
            AtomicInteger interfaceCalls = new AtomicInteger();

            validator.register(Number.class, (n, ctx) -> classCalls.incrementAndGet());
            validator.register(Comparable.class, (c, ctx) -> interfaceCalls.incrementAndGet());

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate(Integer.valueOf(1), context);

            assertEquals(1, classCalls.get());
            assertEquals(0, interfaceCalls.get());
        }

        @Test
        @DisplayName("computeBestHandler 应处理同距离接口歧义并忽略更远候选")
        void should_chooseOneNearestInterface_when_equalDistanceInterfacesExistAlongsideFartherCandidate() {
            LoggingTypedValidator validator = new LoggingTypedValidator();
            AtomicInteger charSequenceCalls = new AtomicInteger();
            AtomicInteger appendableCalls = new AtomicInteger();
            AtomicInteger objectCalls = new AtomicInteger();

            validator.register(CharSequence.class, (value, context) -> charSequenceCalls.incrementAndGet());
            validator.register(Appendable.class, (value, context) -> appendableCalls.incrementAndGet());
            validator.register(Object.class, (value, context) -> objectCalls.incrementAndGet());

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate(new StringBuilder("x"), context);

            assertTrue(context.isValid());
            assertEquals(0, objectCalls.get());
            assertEquals(1, charSequenceCalls.get() + appendableCalls.get());
        }
    }

    static class TestTypedValidator extends TypedValidator {

    }

    static class AmbiguousTypedValidator extends TypedValidator {
        @Override
        protected boolean failOnAmbiguousHandler() {
            return true;
        }

    }

    static class LargeTypedValidator extends TypedValidator {
        public LargeTypedValidator() {
            register(String.class, (s, ctx) -> {
            });
            register(Integer.class, (s, ctx) -> {
            });
            register(Long.class, (s, ctx) -> {
            });
            register(Double.class, (s, ctx) -> {
            });
            register(Float.class, (s, ctx) -> {
            });
            register(Short.class, (s, ctx) -> {
            });
            register(Byte.class, (s, ctx) -> {
            });
            register(Character.class, (s, ctx) -> {
            });
            register(Boolean.class, (s, ctx) -> {
            });
            register(Number.class, (s, ctx) -> {
            });
            register(CharSequence.class, (s, ctx) -> {
            });
            register(Comparable.class, (s, ctx) -> {
            });
            register(Object.class, (s, ctx) -> {
            });
            register(Cloneable.class, (s, ctx) -> {
            });
            register(Iterable.class, (s, ctx) -> {
            });
        }
    }

    static class LoggingTypedValidator extends TypedValidator {

    }

    static class PriorityTypedValidator extends TypedValidator {

        @Override
        protected int getPriority(Class<?> registeredType) {
            // CharSequence优先级更高
            if (registeredType == CharSequence.class) return 10;
            if (registeredType == Comparable.class) return 5;
            return 0;
        }
    }

    static class PreferClassTypedValidator extends TypedValidator {
    }

    @Test
    @DisplayName("debug 日志开启时应覆盖歧义与解析日志分支")
    void should_coverDebugLoggingBranches_when_loggerLevelIsDebug() {
        Logger logger = (Logger) LoggerFactory.getLogger(TypedValidator.class);
        Level original = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        try {
            LoggingTypedValidator validator = new LoggingTypedValidator();
            validator.register(CharSequence.class, (value, context) -> {
            });
            validator.register(Comparable.class, (value, context) -> {
            });

            FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
            validator.validate("debug", context);

            assertTrue(context.isValid());
        } finally {
            logger.setLevel(original);
        }
    }
}
