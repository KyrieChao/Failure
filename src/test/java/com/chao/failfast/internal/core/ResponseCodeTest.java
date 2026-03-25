package com.chao.failfast.internal.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResponseCode 测试")
class ResponseCodeTest {

    @Test
    @DisplayName("of(int): 应创建只包含错误码的 ResponseCode")
    void ofWithCodeShouldCreateResponseCodeWithOnlyCode() {
        ResponseCode code = ResponseCode.of(400);
        assertThat(code.getCode()).isEqualTo(400);
        assertThat(code.getMessage()).isNull();
        assertThat(code.getDescription()).isNull();
    }

    @Test
    @DisplayName("of(int, String): 应创建包含错误码和消息的 ResponseCode")
    void ofWithCodeAndMessageShouldCreateResponseCodeWithCodeAndMessage() {
        ResponseCode code = ResponseCode.of(400, "Bad Request");
        assertThat(code.getCode()).isEqualTo(400);
        assertThat(code.getMessage()).isEqualTo("Bad Request");
        assertThat(code.getDescription()).isNull();
    }

    @Test
    @DisplayName("of(int, String, String): 应创建包含错误码、消息和描述的 ResponseCode")
    void ofWithCodeMessageAndDescriptionShouldCreateCompleteResponseCode() {
        ResponseCode code = ResponseCode.of(400, "Bad Request", "Invalid request parameters");
        assertThat(code.getCode()).isEqualTo(400);
        assertThat(code.getMessage()).isEqualTo("Bad Request");
        assertThat(code.getDescription()).isEqualTo("Invalid request parameters");
    }

    @Test
    @DisplayName("formatMessage: 应正确格式化消息")
    void formatMessageShouldFormatMessageCorrectly() {
        ResponseCode code = ResponseCode.of(400, "Error: %s", "Invalid parameter");
        String formatted = code.formatMessage("test");
        assertThat(formatted).isEqualTo("Error: test");
    }

    @Test
    @DisplayName("formatMessage: 当消息为 null 时应返回 null")
    void formatMessageShouldReturnNullWhenMessageIsNull() {
        ResponseCode code = ResponseCode.of(400);
        String formatted = code.formatMessage("test");
        assertThat(formatted).isNull();
    }

    @Test
    @DisplayName("内置错误码应存在且有效")
    void builtInCodesShouldExistAndBeValid() {
        assertThat(ResponseCode.VALIDATION_ERROR).isNotNull();
        assertThat(ResponseCode.VALIDATION_ERROR_400).isNotNull();
        assertThat(ResponseCode.VALIDATION_ERROR_NULL).isNotNull();
        assertThat(ResponseCode.INTERRUPTED_ERROR).isNotNull();
        assertThat(ResponseCode.ILLEGAL_ARGUMENT).isNotNull();
        assertThat(ResponseCode.VALIDATION_ERROR_500).isNotNull();
        assertThat(ResponseCode.VALIDATION_ERROR_500_DYNAMIC).isNotNull();
    }

    @Test
    @DisplayName("Simple 实现应正确实现 ResponseCode 接口")
    void simpleImplementationShouldCorrectlyImplementResponseCode() {
        ResponseCode.Simple simple = new ResponseCode.Simple(500, "Error", "Detailed error");
        assertThat(simple.code()).isEqualTo(500);
        assertThat(simple.message()).isEqualTo("Error");
        assertThat(simple.description()).isEqualTo("Detailed error");
        assertThat(simple.getCode()).isEqualTo(500);
        assertThat(simple.getMessage()).isEqualTo("Error");
        assertThat(simple.getDescription()).isEqualTo("Detailed error");
    }
}
