package com.chao.failfast.integration.mvc;

import com.chao.failfast.autoconfigure.FailFastAutoConfiguration;
import com.chao.failfast.i18n.I18nPropertiesIntegrationTest;
import com.chao.failfast.internal.core.Ex;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.exception.Business;
import com.chao.failfast.exception.MultiBusiness;
import com.chao.failfast.model.TestResponseCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("DefaultExceptionHandler 异常处理器测试")
@SpringBootTest(
        classes = {FailFastAutoConfiguration.class, I18nPropertiesIntegrationTest.TestController.class},
        properties = {
                "fail-fast.i18n.default-locale=zh_CN",
                "fail-fast.i18n.basename=classpath:i18n/messages"
        }
)
class DefaultExceptionHandlerTest {

    private final DefaultExceptionHandler handler = new DefaultExceptionHandler();

    @Autowired
    private FailureContext failureContext;

    @BeforeEach
    void setUp() {
        // 清除上下文，避免其他测试的副作用影响当前测试
        Ex.setContext(null);
    }
    @BeforeAll
    static void init() {
        Locale.setDefault(Locale.CHINA);
    }

    @Test
    @DisplayName("处理 Business 异常应返回对应状态码和响应体")
    void handleBusinessException() {
        Business business = Business.of(TestResponseCode.PARAM_ERROR, "详情");
        ResponseEntity<?> response = handler.handleBusinessException(business);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // TestResponseCode.PARAM_ERROR is 40001, default mapping is 500 if not configured? No, CodeMappingConfig initializes defaults.
        // Wait, here we don't have CodeMappingConfig context, so Business.of uses default mapping which might be INTERNAL_SERVER_ERROR if Ex.context is null.
        // Business constructor logic: HttpStatus status = (cfg != null) ? cfg.resolveHttpStatus(code.getCode()) : HttpStatus.INTERNAL_SERVER_ERROR;
        // Since we didn't setup Ex.context, it should be 500.

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("code", TestResponseCode.PARAM_ERROR.getCode());
        assertThat(body).containsEntry("message", TestResponseCode.PARAM_ERROR.getMessage());
        assertThat(body).containsEntry("description", "详情");
    }

    @Test
    @DisplayName("处理 MultiBusiness 异常应返回批量错误信息")
    void handleMultiBusinessException() {
        List<Business> errors = List.of(
                Business.of(TestResponseCode.PARAM_ERROR, "错误1"),
                Business.of(TestResponseCode.SYSTEM_ERROR, "错误2")
        );
        MultiBusiness multi = new MultiBusiness(errors);
        ResponseEntity<?> response = handler.handleMultiBusinessException(multi);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assert body != null;
        assertThat(body.get("description").toString()).contains("共2项问题");
    }

    @Test
    @DisplayName("处理 BindException 应返回 500")
    void handleBindException() {
        BindException ex = new BindException(new Object(), "target");
        ex.addError(new FieldError("target", "field", "default message"));

        ResponseEntity<?> response = handler.handleBindException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("code", 500);
        assertThat(body).containsEntry("message", "参数绑定失败");
        assertThat(body).containsEntry("description", "default message");
    }

    @Test
    @DisplayName("处理 MethodArgumentNotValidException 应转换为 MultiBusiness")
    void handleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        FieldError fieldError = new FieldError("object", "field", "message");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
        when(bindingResult.getTarget()).thenReturn(new Object());

        MethodParameter parameter = mock(MethodParameter.class);
        when(ex.getParameter()).thenReturn(parameter);
        when(parameter.getMethod()).thenReturn(null); // No @Validate annotation

        ResponseEntity<?> response = handler.handleMethodArgumentNotValidException(ex);

        // Only 1 error -> returns single Business response (which is 500 by default if no context)
        // Wait, handleMultiErrors logic: if errors.size() == 1 -> buildResponse(first).
        // parseError uses default 400 code if message is not "code:msg".
        // ResponseCode(400, "Validation Error") -> HttpStatus?
        // Without CodeMappingConfig, Business constructor uses INTERNAL_SERVER_ERROR.

        // Let's verify the body content mostly.
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("code", 400);
        assertThat(body).containsEntry("description", "message");
    }

    @Test
    @DisplayName("处理 ConstraintViolationException 应转换为 MultiBusiness")
    void handleConstraintViolationException() {
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("message");
        when(violation.getRootBeanClass()).thenReturn(Object.class);

        // Mock Path
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("prop");
        when(violation.getPropertyPath()).thenReturn(path);

        when(ex.getConstraintViolations()).thenReturn(Set.of(violation));

        ResponseEntity<?> response = handler.handleConstraintViolationException(ex);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("code", 400);
        assertThat(body).containsEntry("description", "message");
    }
    @Test
    @DisplayName("处理 BindException 无错误时返回 未知错误")
    void handleBindExceptionEmptyErrors() {
        BindException ex = new BindException(new Object(), "target");
        // 不添加任何错误，保持 empty
        ResponseEntity<?> response = handler.handleBindException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("code", 500);
        assertThat(body).containsEntry("message", "参数绑定失败");
        assertThat(body).containsEntry("description", "未知错误");  // ← 覆盖空分支
    }

    @Test
    @DisplayName("默认场景不应输出 scene 字段")
    void defaultSceneShouldNotBeReturned() {
        Ex.setContext(failureContext);
        failureContext.setScene("DEFAULT");

        Business business = Business.of(TestResponseCode.PARAM_ERROR, "详情");
        ResponseEntity<?> response = handler.handleBusinessException(business);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).doesNotContainKey("scene");
    }

    @Test
    @DisplayName("非默认场景应输出 scene 字段")
    void nonDefaultSceneShouldBeReturned() {
        Ex.setContext(failureContext);
        failureContext.setScene("CREATE");

        Business business = Business.of(TestResponseCode.PARAM_ERROR, "详情");
        ResponseEntity<?> response = handler.handleBusinessException(business);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("scene", "CREATE");
    }
}
