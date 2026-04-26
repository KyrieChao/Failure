package com.chao.failfast.test;

import com.chao.failfast.Failure;
import com.chao.failfast.validator.FastValidator.ValidationContext;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.validator.TypedValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 模拟 Failure-in-Action 示例中的最佳实践用�?
 * 验证 TypedValidator + Failure.with(ctx) + 手动中断模式
 */
public class FailFastInActionTest {

    // 模拟 DTO
    static class UserDTO {
        String username;
        String email;

        public UserDTO(String username, String email) {
            this.username = username;
            this.email = email;
        }
    }

    // 模拟 ResponseCode
    enum UserCode implements ResponseCode {
        USERNAME_BLANK(1001, "Username blank"),
        EMAIL_INVALID(1002, "Email invalid"),
        USER_EXISTS(1003, "User exists");

        private final int code;
        private final String message;

        UserCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getDescription() {
            return message;
        }
    }

    // 模拟 Validator
    static class UserValidator extends TypedValidator {
        
        public UserValidator() {
            // 在构造函数中注册校验逻辑
            register(UserDTO.class, this::validateUser);
        }

        // 模拟数据库查重逻辑
        private boolean userExists(String username) {
            return "existUser".equals(username);
        }

        private void validateUser(UserDTO dto, ValidationContext ctx) {
            // 1. 基础格式校验 (Fail-Fast within chain)
            Failure.with(ctx)
                    .notBlank(dto.username, UserCode.USERNAME_BLANK)
                    .email(dto.email, UserCode.EMAIL_INVALID)
                    .verify();

            // 2. 检查是否需要继�?(关键点：避免昂贵的业务校�?
            if (ctx.isFailed()) {
                return;
            }

            // 3. 业务逻辑校验 (Expensive operation)
            if (userExists(dto.username)) {
                ctx.reportError(UserCode.USER_EXISTS);
            }
        }
    }

    @Test
    public void testValidationSuccess() {
        UserDTO validUser = new UserDTO("newUser", "test@example.com");
        ValidationContext ctx = new ValidationContext(true); // fast=true
        
        new UserValidator().validate(validUser, ctx);

        assertFalse(ctx.isFailed());
        assertTrue(ctx.isValid());
    }

    @Test
    public void testFormatFailureStopsBusinessCheck() {
        // Username is blank -> format error
        UserDTO invalidUser = new UserDTO("", "test@example.com");
        ValidationContext ctx = new ValidationContext(true); // fast=true
        
        new UserValidator().validate(invalidUser, ctx);

        assertTrue(ctx.isFailed());
        assertEquals(1, ctx.hasCauses().size());
        assertEquals(UserCode.USERNAME_BLANK.getCode(), ctx.hasCauses().get(0).getResponseCode().getCode());
        
        // Ensure business check was skipped (though here business check wouldn't add error anyway, 
        // but logic flow stopped at step 2)
    }

    @Test
    public void testBusinessFailure() {
        // Format is valid, but user exists
        UserDTO existUser = new UserDTO("existUser", "test@example.com");
        ValidationContext ctx = new ValidationContext(true); // fast=true
        
        new UserValidator().validate(existUser, ctx);

        assertTrue(ctx.isFailed());
        assertEquals(1, ctx.hasCauses().size());
        assertEquals(UserCode.USER_EXISTS.getCode(), ctx.hasCauses().get(0).getResponseCode().getCode());
    }

    @Test
    public void testStrictFormatFailure() {
        // In strict mode (fast=false), all format errors are collected
        UserDTO invalidUser = new UserDTO("", "invalid-email");
        ValidationContext ctx = new ValidationContext(false); // fast=false
        
        new UserValidator().validate(invalidUser, ctx);

        assertTrue(ctx.isFailed());
        // Expecting 2 errors: username blank AND email invalid
        assertEquals(2, ctx.hasCauses().size());
        assertEquals(UserCode.USERNAME_BLANK.getCode(), ctx.hasCauses().get(0).getResponseCode().getCode());
        assertEquals(UserCode.EMAIL_INVALID.getCode(), ctx.hasCauses().get(1).getResponseCode().getCode());
        
        // Business check should strictly be skipped if ctx.isFailed() check is present
        // But if user exists check was run, it might add another error.
        // In our UserValidator, we check `if (ctx.isFailed()) return;`
        // So business check is skipped even in strict mode if format check failed.
        // This is efficient behavior.
    }
}
