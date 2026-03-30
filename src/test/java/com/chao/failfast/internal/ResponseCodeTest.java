package com.chao.failfast.internal;

import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResponseCode 接口测试")
class ResponseCodeTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTest {

        @Test
        @DisplayName("of(int) 应创建只有错误码的响应码")
        void shouldCreateWithCodeOnly() {
            ResponseCode code = ResponseCode.of(40001);
            
            assertThat(code.getCode()).isEqualTo(40001);
            assertThat(code.getMessage()).isNull();
            assertThat(code.getDescription()).isNull();
        }

        @Test
        @DisplayName("of(int, String) 应创建错误码和消息的响应码")
        void shouldCreateWithCodeAndMessage() {
            ResponseCode code = ResponseCode.of(40001, "参数错误");
            
            assertThat(code.getCode()).isEqualTo(40001);
            assertThat(code.getMessage()).isEqualTo("参数错误");
            assertThat(code.getDescription()).isNull();
        }

        @Test
        @DisplayName("of(int, String, String) 应创建完整的响应码")
        void shouldCreateWithAllFields() {
            ResponseCode code = ResponseCode.of(40001, "参数错误", "请求参数校验失败");
            
            assertThat(code.getCode()).isEqualTo(40001);
            assertThat(code.getMessage()).isEqualTo("参数错误");
            assertThat(code.getDescription()).isEqualTo("请求参数校验失败");
        }
    }

    @Nested
    @DisplayName("formatMessage 方法测试")
    class FormatMessageTest {

        @Test
        @DisplayName("应支持字符串格式化")
        void shouldFormatMessageWithArgs() {
            ResponseCode code = ResponseCode.of(40001, "参数 %s 不能为空");
            
            String formatted = code.formatMessage("username");
            
            assertThat(formatted).isEqualTo("参数 username 不能为空");
        }

        @Test
        @DisplayName("应支持多个格式化参数")
        void shouldFormatWithMultipleArgs() {
            ResponseCode code = ResponseCode.of(40001, "参数 %s 的值 %s 不合法");
            
            String formatted = code.formatMessage("age", "abc");
            
            assertThat(formatted).isEqualTo("参数 age 的值 abc 不合法");
        }

        @Test
        @DisplayName("当 message 为 null 时应返回 null")
        void shouldReturnNullWhenMessageIsNull() {
            ResponseCode code = ResponseCode.of(40001);
            
            String formatted = code.formatMessage("arg");
            
            assertThat(formatted).isEqualTo(null);
        }

        @Test
        @DisplayName("当 message 不包含占位符时应返回原字符串")
        void shouldReturnOriginalWhenNoPlaceholder() {
            ResponseCode code = ResponseCode.of(40001, "参数错误");
            
            String formatted = code.formatMessage("arg");
            
            assertThat(formatted).isEqualTo("参数错误");
        }
    }

    @Nested
    @DisplayName("Simple record 测试")
    class SimpleRecordTest {

        @Test
        @DisplayName("record 应正确实现 equals 和 hashCode")
        void shouldImplementEqualsAndHashCode() {
            ResponseCode code1 = ResponseCode.of(40001, "参数错误", "描述");
            ResponseCode code2 = ResponseCode.of(40001, "参数错误", "描述");
            ResponseCode code3 = ResponseCode.of(40002, "参数错误", "描述");
            
            assertThat(code1).isEqualTo(code2);
            assertThat(code1.hashCode()).isEqualTo(code2.hashCode());
            assertThat(code1).isNotEqualTo(code3);
        }

        @Test
        @DisplayName("record 应正确实现 toString")
        void shouldImplementToString() {
            ResponseCode code = ResponseCode.of(40001, "参数错误", "描述");
            
            String str = code.toString();
            
            assertThat(str).contains("40001");
            assertThat(str).contains("参数错误");
            assertThat(str).contains("描述");
        }
    }
}
