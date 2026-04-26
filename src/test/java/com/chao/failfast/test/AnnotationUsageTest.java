package com.chao.failfast.test;

import com.chao.failfast.validator.FastValidator;
import com.chao.failfast.annotation.Validate;
import com.chao.failfast.aspect.ValidationAspect;
import com.chao.failfast.exception.Business;
import com.chao.failfast.exception.MultiBusiness;
import com.chao.failfast.internal.core.ResponseCode;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 注解驱动验证示例 (Annotation Validation)
 * 展示如何在 Spring 环境中使用 @Validate 注解和自定义 FastValidator
 */
@SpringJUnitConfig(AnnotationUsageTest.TestConfig.class)
class AnnotationUsageTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("测试注解驱动的快速失败模式")
    void testFailFast() {
        UserDTO user = new UserDTO("", "invalid-email"); // 用户名为空，邮箱格式错误

        // 在 fast=true (默认) 模式下，应该只抛出第一个错误（用户名为空）
        Business ex = assertThrows(Business.class, () -> userService.register(user));

        assertEquals(1001, ex.getResponseCode().getCode());
        System.out.println("注解 Fail-Fast 演示成功: " + ex.getMessage());
    }

    @Test
    @DisplayName("测试注解驱动的收集模式")
    void testStrictCollect() {
        UserDTO user = new UserDTO("", "invalid-email");

        // 在 fast=false 模式下，应该抛出 MultiBusiness 包含所有错误
        MultiBusiness ex = assertThrows(MultiBusiness.class, () -> userService.registerStrict(user));
        assertEquals(2, ex.getErrors().size());

        assertEquals(1001, ex.getErrors().get(0).getResponseCode().getCode());
        assertEquals(1002, ex.getErrors().get(1).getResponseCode().getCode());

        System.out.println("注解 Strict 模式演示成功，收集到 " + ex.getErrors().size() + " 个错误");
    }

    // ================== 模拟业务代码 ==================
    @Data
    static class UserDTO {
        private String username;
        private String email;

        public UserDTO(String username, String email) {
            this.username = username;
            this.email = email;
        }

        @Override
        public String toString() {
            return "UserDTO{" +
                    "username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }

    // 自定义验证器
    static class UserValidator implements FastValidator<UserDTO> {
        @Override
        public void validate(UserDTO target, ValidationContext context) {
            if (target.getUsername() == null || target.getUsername().isBlank()) {
                context.reportError(ResponseCode.of(1001, "用户名不能为空"));
                // 在快速失败模式下，addError 后 context.isStopped() 会变为 true
                // Aspect 会检测到并停止后续验证
            }

            if (target.getEmail() == null || !target.getEmail().contains("@")) {
                context.reportError(ResponseCode.of(1002, "邮箱格式错误"));
            }
        }
    }

    // 业务服务类
    static class UserService {

        // 默认 fast=true
        @Validate(value = UserValidator.class)
        public void register(UserDTO user) {
            System.out.println("注册用户: " + user);
        }

        // 显式指定 fast=false (收集模式)
        @Validate(value = UserValidator.class, fast = false)
        public void registerStrict(UserDTO user) {
            System.out.println("注册用户(严格模式): " + user);
        }
    }

    // ================== Spring 配置 ==================

    @Configuration
    @EnableAspectJAutoProxy // 启用 AOP 代理
    static class TestConfig {

        @Bean
        public ValidationAspect validationAspect() {
            return new ValidationAspect();
        }

        @Bean
        public UserService userService() {
            return new UserService();
        }

        // 注册验证器 Bean (可选，Aspect 支持从 Bean 获取或反射创建)
        @Bean
        public UserValidator userValidator() {
            return new UserValidator();
        }
    }
}
