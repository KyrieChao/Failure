package com.chao.failfast.internal;

import com.chao.failfast.config.CodeMappingConfig;
import com.chao.failfast.i18n.I18nExtension;
import com.chao.failfast.internal.core.FailureProperties;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.model.TestResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@DisplayName("Business 异常类测试")
@ExtendWith(I18nExtension.class)
class BusinessTest {

    @AfterEach
    void tearDown() {
        Ex.setContext(null);
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTest {
        @Test
        @DisplayName("of(ResponseCode) 应创建基本异常")
        void shouldCreateBasicException() {
            Business business = Business.of(TestResponseCode.PARAM_ERROR);
            assertThat(business.getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
            assertThat(business.getMessage()).isEqualTo(TestResponseCode.PARAM_ERROR.getMessage());
        }

        @Test
        @DisplayName("of(ResponseCode, String) 应创建带详情异常")
        void shouldCreateExceptionWithDetail() {
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail");
            assertThat(business.getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
            assertThat(business.getDetail()).isEqualTo("detail");
        }

        @Test
        @DisplayName("of(ResponseCode, String, Object...) 应创建带格式化详情异常")
        void shouldCreateExceptionWithFormattedDetail() {
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail %s", "123");
            assertThat(business.getDetail()).isEqualTo("detail 123");
        }
    }

    @Nested
    @DisplayName("toString 格式测试")
    class ToStringTest {
        @Test
        @DisplayName("toString 应包含错误码和消息")
        void toStringShouldContainCodeAndMessage() {
            Business business = Business.of(TestResponseCode.PARAM_ERROR);
            String str = business.toString();
            // Code 40001 -> 400_01
            assertThat(str).contains("code=400_01");
            assertThat(str).contains("mes=" + TestResponseCode.PARAM_ERROR.getMessage());
        }
    }

    @Nested
    @DisplayName("fillInStackTrace 测试")
    class FillInStackTraceTest {
        @Test
        @DisplayName("当 code 为 null 时应填充堆栈")
        void shouldFillStackTraceWhenCodeIsNull() {
            // responseCode is null
            Business business = new Business(null, null, null, null, null, null);
            // Stack trace should be present
            assertThat(business.getStackTrace()).isNotEmpty();
        }

        @Test
        @DisplayName("当启用 shadowTrace 时应填充堆栈")
        void shouldFillStackTraceWhenShadowTraceEnabled() {
            FailureContext context = mock(FailureContext.class);
            when(context.isShadowTrace()).thenReturn(true);
            Ex.setContext(context);

            Business business = Business.of(TestResponseCode.PARAM_ERROR);
            assertThat(business.getStackTrace()).isNotEmpty();
        }

        @Test
        @DisplayName("当禁用 shadowTrace 且非 5xx 错误时应跳过堆栈填充")
        void shouldSkipStackTraceWhenShadowTraceDisabledAndNot5xx() {
            FailureContext context = mock(FailureContext.class);
            when(context.isShadowTrace()).thenReturn(false);
            CodeMappingConfig config = mock(CodeMappingConfig.class);
            when(context.getCodeMappingConfig()).thenReturn(config);
            // 400 is not 5xx
            when(config.resolveHttpStatus(anyInt())).thenReturn(HttpStatus.BAD_REQUEST);
            Ex.setContext(context);

            Business business = Business.of(TestResponseCode.PARAM_ERROR);
            // Stack trace should be empty (suppressed)
            assertThat(business.getStackTrace()).isEmpty();
        }

        @Test
        @DisplayName("当禁用 shadowTrace 但为 5xx 错误时应填充堆栈")
        void shouldFillStackTraceWhen5xx() {
            FailureContext context = mock(FailureContext.class);
            when(context.isShadowTrace()).thenReturn(false);
            CodeMappingConfig config = mock(CodeMappingConfig.class);
            when(context.getCodeMappingConfig()).thenReturn(config);
            when(config.resolveHttpStatus(anyInt())).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
            Ex.setContext(context);

            Business business = Business.of(TestResponseCode.ERROR);
            assertThat(business.getStackTrace()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("materialize 默认值测试")
    class MaterializeDefaultTest {

        @Test
        @DisplayName("当 detail、description、message 都为 null 时应使用默认描述")
        void shouldUseDefaultDescriptionWhenAllNull() {
            // 创建一个 description 和 message 都为 null 的 ResponseCode
            ResponseCode code = ResponseCode.of(500, null, null);
            Business business = Business.of(code);

            assertThat(business.getDetail()).isIn("message 或 description 至少一个不能为 null", "{failure.const.message.description.required}");
        }

        @Test
        @DisplayName("当 detail 为 null 但 message 不为 null 时应使用 message")
        void shouldUseMessageWhenDetailNull() {
            ResponseCode code = ResponseCode.of(500, "错误消息", null);
            Business business = Business.of(code);

            assertThat(business.getDetail()).isEqualTo("错误消息");
        }
    }

    @Nested
    @DisplayName("extractFileLine 测试")
    class ExtractFileLineTest {

        @Test
        @DisplayName("当 location 不包含括号时应返回原字符串")
        void shouldReturnOriginalWhenNoParenthesis() {
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail", "method", "NoParenthesis");
            String str = business.toString();
            assertThat(str).contains("NoParenthesis");
        }

        @Test
        @DisplayName("当 location 包含内部类 $ 时应正确处理")
        void shouldHandleInnerClass() {
            String location = "TestController$InnerClass.method(TestController$InnerClass.java:100)";
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail", "method", location);
            String str = business.toString();
            // 应该提取为 TestController.java:100
            assertThat(str).contains("TestController.java:100");
        }
    }

    @Nested
    @DisplayName("内部类方法名处理测试")
    class InnerClassMethodTest {

        @Test
        @DisplayName("toString 应处理内部类方法名中的 $")
        void shouldHandleDollarInMethodName() {
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail",
                    "TestController$Validator#validate", "location");
            String str = business.toString();
            // 应该显示为 TestController#validate
            assertThat(str).contains("TestController#validate");
            assertThat(str).doesNotContain("$Validator");
        }
    }

    @Nested
    @DisplayName("compose 构建器完整测试")
    class ComposeBuilderTest {

        @Test
        @DisplayName("应支持完整的链式调用")
        void shouldSupportFullChaining() {
            Business business = Business.compose()
                    .responseCode(TestResponseCode.PARAM_ERROR)
                    .detail("自定义详情")
                    .materialize();

            assertThat(business.getResponseCode().getCode()).isEqualTo(TestResponseCode.PARAM_ERROR.getCode());
            assertThat(business.getDetail()).isEqualTo("自定义详情");
        }

        @Test
        @DisplayName("当 responseCode 为 null 时应抛出异常")
        void shouldThrowWhenResponseCodeIsNull() {
            org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                            Business.compose().materialize()
                    ).isInstanceOf(IllegalArgumentException.class)
                    // Accept both translated and raw key to handle different test environments
                    .message().isIn("code 不能为空", "{failure.const.code.required}");
        }
    }

    @Nested
    @DisplayName("of 方法重载测试")
    class OfOverloadTest {

        @Test
        @DisplayName("of(int, String) 应创建异常")
        void shouldCreateWithIntCode() {
            Business business = Business.of(40001, "参数错误");
            assertThat(business.getResponseCode().getCode()).isEqualTo(40001);
            assertThat(business.getResponseCode().getMessage()).isEqualTo("参数错误");
        }

        @Test
        @DisplayName("of(int, String, String) 应创建带详情异常")
        void shouldCreateWithIntCodeAndDetail() {
            Business business = Business.of(40001, "参数错误", "手机号格式不正确");
            assertThat(business.getDetail()).isEqualTo("手机号格式不正确");
        }

        @Test
        @DisplayName("of(int, String, String, Object...) 应创建带格式化详情异常")
        void shouldCreateWithFormattedDetail() {
            Business business = Business.of(40001, "参数错误", "手机号 %s 格式不正确", "13800138000");
            assertThat(business.getDetail()).isEqualTo("手机号 13800138000 格式不正确");
        }
    }

    @Nested
    @DisplayName("httpStatus 测试")
    class HttpStatusTest {

        @Test
        @DisplayName("当 CodeMappingConfig 返回特定状态码时应使用")
        void shouldUseStatusFromConfig() {
            FailureContext context = mock(FailureContext.class);
            CodeMappingConfig config = mock(CodeMappingConfig.class);
            when(context.getCodeMappingConfig()).thenReturn(config);
            when(config.resolveHttpStatus(40001)).thenReturn(HttpStatus.BAD_REQUEST);
            Ex.setContext(context);

            Business business = Business.of(TestResponseCode.PARAM_ERROR);
            assertThat(business.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("当 CodeMappingConfig 为 null 时应默认 500")
        void shouldDefaultTo500WhenNoConfig() {
            Ex.setContext(null);
            Business business = Business.of(TestResponseCode.PARAM_ERROR);
            assertThat(business.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("toString 方法完整覆盖")
    class ToStringFullTest {

        @Test
        @DisplayName("method 为 null 且有 location 时应显示 location")
        void shouldShowLocationWhenMethodIsNull() {
            String location = "TestController.method(TestController.java:100)";
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail", null, location);
            String str = business.toString();

            assertThat(str).contains("TestController.java:100");
            assertThat(str).doesNotContain("[");  // 没有 method，不应该有方括号
        }

        @Test
        @DisplayName("method 为 null 且 location 为 null 时应只显示 base")
        void shouldShowBaseOnlyWhenNoMethodAndNoLocation() {
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail", null, null);
            String str = business.toString();

            assertThat(str).startsWith("{code=");
            assertThat(str).doesNotContain("(");  // 没有 location，不应该有括号
        }

        @Test
        @DisplayName("method 不为 null 且不含 $ 时应直接显示 method")
        void shouldShowMethodDirectly() {
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail",
                    "TestController#validate", "location");
            String str = business.toString();

            assertThat(str).contains("[TestController#validate]");
        }

        @Test
        @DisplayName("method 包含 $ 但没有 # 时应显示原 method")
        void shouldShowOriginalMethodWhenNoHash() {
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail",
                    "TestController$InnerClass", "location");
            String str = business.toString();

            // 有 $ 但没有 #，所以 displayMethod 不变
            assertThat(str).contains("[TestController$InnerClass]");
        }

        @Test
        @DisplayName("method 包含 $ 且 # 在 $ 之后时应处理内部类")
        void shouldHandleInnerClassMethod() {
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail",
                    "TestController$InnerClass#validate", "location");
            String str = business.toString();

            // 应该显示为 TestController#validate
            assertThat(str).contains("[TestController#validate]");
            assertThat(str).doesNotContain("$InnerClass");
        }

        @Test
        @DisplayName("method 包含 $ 但 # 在 $ 之前时不应处理")
        void shouldNotProcessWhenHashBeforeDollar() {
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail",
                    "#validate$Test", "location");
            String str = business.toString();

            // # 在 $ 之前，所以不处理
            assertThat(str).contains("[#validate$Test]");
        }
    }

    @Nested
    @DisplayName("extractFileLine 完整覆盖")
    class ExtractFileLineFullTest {

        @Test
        @DisplayName("location 为 null 时应返回空字符串")
        void shouldReturnEmptyWhenLocIsNull() {
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail", null, null);
            // 通过 toString 间接测试
            String str = business.toString();
            assertThat(str).doesNotContain("()");  // 没有 location，不应该有空的括号
        }

        @Test
        @DisplayName("location 不包含 ( 时应返回原字符串")
        void shouldReturnOriginalWhenNoParenthesis() {
            String location = "NoParenthesisHere";
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail", null, location);
            String str = business.toString();

            assertThat(str).contains("NoParenthesisHere");
        }

        @Test
        @DisplayName("location 包含 ( 但 content 不包含 $ 时应返回 content")
        void shouldReturnContentWhenNoDollar() {
            String location = "TestController.method(TestController.java:100)";
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail", null, location);
            String str = business.toString();

            assertThat(str).contains("TestController.java:100");
        }

        @Test
        @DisplayName("content 包含 $ 但没有 . 时应返回原 content")
        void shouldReturnOriginalWhenNoDotAfterDollar() {
            String location = "Test$NoDot(Test$NoDot:100)";
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail", null, location);
            String str = business.toString();

            // 有 $ 但没有 . 在 $ 之后，所以返回原 content
            assertThat(str).contains("Test$NoDot:100");
        }

        @Test
        @DisplayName("content 包含 $ 且 . 在 $ 之后时应处理内部类文件名")
        void shouldHandleInnerClassFileName() {
            String location = "TestController$InnerClass.method(TestController$InnerClass.java:100)";
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail", null, location);
            String str = business.toString();

            // 应该提取为 TestController.java:100
            assertThat(str).contains("TestController.java:100");
            assertThat(str).doesNotContain("$InnerClass");
        }

        @Test
        @DisplayName("content 包含多个 $ 时应处理第一个 $")
        void shouldHandleFirstDollar() {
            String location = "A$B$C.method(A$B$C.java:100)";
            Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail", null, location);
            String str = business.toString();

            // 第一个 $ 在 index=1，. 在 $ 之后，所以提取为 A.java:100
            assertThat(str).contains("A.java:100");
        }
    }

    @Test
    @DisplayName("Card")
    void testCard() {
        FailureProperties props = new FailureProperties();
        props.setDebugSnapshot(true);

        FailureContext ctx = new FailureContext(props, new CodeMappingConfig(props), null);
        Ex.setContext(ctx);

        Business business = Business.compose()
                .responseCode(TestResponseCode.PARAM_ERROR)
                .invalidValue("152222200501154332")
                .detail("Invalid card number")
                .materialize();
        assertThat(business.getInvalidValue()).isEqualTo("152222200501154332");
        log.info(business.toString());
    }
}
