package com.chao.failfast.web;

import com.chao.failfast.Failure;
import com.chao.failfast.annotation.FailFastBody;
import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.annotation.Validate;
import com.chao.failfast.autoconfigure.FailFastAutoConfiguration;
import com.chao.failfast.internal.core.ResponseCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring Web 集成示例
 * 展示如何�?Controller 层使�?Fail-Fast 进行请求参数验证
 */
@WebMvcTest(WebIntegrationTest.ExampleController.class)
@Import(FailFastAutoConfiguration.class) // 导入 Fail-Fast 自动配置
@EnableAspectJAutoProxy
@org.springframework.test.context.TestPropertySource(properties = "fail-fast.i18n.default-locale=zh_CN")
class WebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
@DisplayName("display")
    void testFailFast() throws Exception {
        // 请求体：用户名为�?(触发第一个错�?
        String json = """
                    {
                        "username": "",
                        "age": 15
                    }
                """;

        mockMvc.perform(post("/api/example/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isInternalServerError()) // 500 Internal Server Error (Custom code 1001 maps to 500 by default)
                .andExpect(jsonPath("$.code").value(1001)) // 对应 UserValidator 中的错误�?
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
@DisplayName("display")
    void testStrictCollect() throws Exception {
        // 请求体：用户名为空且年龄小于18 (触发两个错误)
        String json = """
                    {
                        "username": "",
                        "age": 15
                    }
                """;

        mockMvc.perform(post("/api/example/register-strict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500)) // MultiBusiness 默认映射�?500
                // 验证 description 包含了所有错误信�?
                .andExpect(jsonPath("$.description").isNotEmpty());
        // 验证 errors 字段已被移除
        // .andExpect(jsonPath("$.errors").doesNotExist()); // 确保没有 errors 字段
    }

    @Test
@DisplayName("display")
    void testStandardValidFailFast() throws Exception {
        // 请求体：用户名为�?(触发唯一标准校验错误，确保测试确定�?
        String json = """
                    {
                        "username": "",
                        "age": 18
                    }
                """;

        mockMvc.perform(post("/api/example/standard-valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest()) // 400 Bad Request
                // 验证只返回了一个错�?(Fail Fast)
                .andExpect(jsonPath("$.code").value(400)) // 默认标准校验错误�?(Validation Error maps to 400)
                .andExpect(jsonPath("$.description").value("Standard: Username cannot be blank"));
        // 确保没有返回数组形式�?errors
    }

    @Test
    @DisplayName("测试 @Validate(fast=false) + @Valid - 收集模式")
    void testStandardValidCollect() throws Exception {
        // 请求体：用户名为空且年龄小于18 (触发两个标准校验错误)
        String json = """
                    {
                        "username": "",
                        "age": 15
                    }
                """;

        mockMvc.perform(post("/api/example/enhanced-valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                // 验证 description 包含了所有错误信�?
                .andExpect(jsonPath("$.description").isNotEmpty());
    }

    @Test
    @DisplayName("测试 @FailFastBody - 正常绑定 JSON")
    void testFailFastBodyBinding() throws Exception {
        String json = """
                    {
                        "username": "u1",
                        "age": 18
                    }
                """;

        mockMvc.perform(post("/api/example/failfast-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("u1"))
                .andExpect(jsonPath("$.age").value(18));
    }

    // ================== 示例 Controller �?DTO ==================

    @SpringBootApplication
    @RestController
    @RequestMapping("/api/example")
    static class ExampleController {

        /**
         * 方式一：注解驱动验�?(推荐)
         * 这种方式最简洁，验证逻辑与业务逻辑分离
         */
        @PostMapping("/register")
        @Validate(value = UserValidator.class) // 默认 fast=true
        public UserRegisterRequest register(@RequestBody UserRegisterRequest request) {
            // 如果验证通过，才会执行到这里
            return request;
        }

        /**
         * 方式二：注解驱动验证 (收集模式)
         * 设置 fast=false，收集所有错误一次性返�?
         */
        @PostMapping("/register-strict")
        @Validate(value = UserValidator.class, fast = false)
        public UserRegisterRequest registerStrict(@RequestBody UserRegisterRequest request) {
            return request;
        }

        /**
         * 方式三：编程式验�?(Chain API)
         * 适用于复杂的条件验证，或者不想写单独 Validator 类的场景
         */
        @PostMapping("/manual")
        public UserRegisterRequest manualCheck(@RequestBody UserRegisterRequest request) {
            Failure.begin().notBlank(request.getUsername(), ResponseCode.of(1001, "username cannot be blank"))
                    .positive(request.getAge(), ResponseCode.of(1003, "年龄必须大于0"))
                    .failAll(); // 如果有错，这里会抛出异常，由全局处理器捕�?

            return request;
        }

        /**
         * 方式四：标准 @Valid 验证 (默认 Fail Fast)
         */
        @PostMapping("/standard-valid")
        public UserRegisterRequest standardValid(@RequestBody @Valid UserRegisterRequest request) {
            return request;
        }

        /**
         * 方式五：@Validate(fast=false) + @Valid (收集模式)
         */
        @PostMapping("/enhanced-valid")
        @Validate(fast = false)
        public UserRegisterRequest enhancedValid(@RequestBody @Valid UserRegisterRequest request) {
            return request;
        }

        @PostMapping("/failfast-body")
        public UserRegisterRequest failFastBody(@FailFastBody UserRegisterRequest request) {
            return request;
        }
    }


    @Data
    public static class UserRegisterRequest {
        @NotBlank(message = "Standard: Username cannot be blank")
        private String username;

        @Min(value = 18, message = "Standard: Must be at least 18")
        private Integer age;

        private String email;

        public UserRegisterRequest() {
        }

    }

    /**
     * 自定义验证器
     * 可以注入 Spring Bean (�?Service, Repository) 进行数据库查重等操作
     */
    public static class UserValidator implements FastValidator<UserRegisterRequest> {
        @Override
        public void validate(UserRegisterRequest target, ValidationContext context) {
            // 1. 验证用户�?
            if (target.getUsername() == null || target.getUsername().isBlank()) {
                context.reportError(ResponseCode.of(1001, "username cannot be blank"));
                // 如果�?fast 模式，context.isStopped() 会自动变�?true
                // 你也可以手动调用 context.stop() �?addErrorAndHalt()
            }

            // 2. 验证年龄
            if (target.getAge() != null && target.getAge() < 18) {
                context.reportError(ResponseCode.of(1002, "must be >= 18"));
            }
        }
    }
}
