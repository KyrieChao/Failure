package com.chao.failure.web;

import com.chao.failure.validator.FastValidator;
import com.chao.failure.annotation.Validate;
import com.chao.failure.autoconfigure.FailFastAutoConfiguration;
import com.chao.failure.internal.core.ResponseCode;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.chao.failure.spi.config.FailFastConfigurer;
import com.chao.failure.spi.validation.ValidatorWhitelistRegistry;

@WebMvcTest(VerboseModeTest.VerboseController.class)
@Import({FailFastAutoConfiguration.class, VerboseModeTest.WhitelistConfig.class})
@EnableAspectJAutoProxy
@TestPropertySource(properties = "fail-fast.verbose=true")
@DisplayName("Verbose模式测试")
class VerboseModeTest {

    @Autowired
    private MockMvc mockMvc;



    @Test
    @DisplayName("开启Verbose模式应返回errors数组")
    void shouldReturnErrorsArrayWhenVerboseEnabled() throws Exception {
        String json = "{}"; // Empty body to trigger validation errors

        mockMvc.perform(post("/api/verbose/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                // MultiBusiness 继承第一个 Business 对象的 HTTP 状态码 (400)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpect(jsonPath("$.errors[0].message").value("Error 1"))
                .andExpect(jsonPath("$.errors[1].message").value("Error 2"));
    }

    @SpringBootApplication
    @RestController
    public static class VerboseController {
        @PostMapping("/api/verbose/test")
        @Validate(value = VerboseValidator.class, fast = false)
        public void test(@RequestBody VerboseRequest request) {}
    }

    @Data
    public static class VerboseRequest {
        private String f1;
        private String f2;
    }

    public static class VerboseValidator implements FastValidator<VerboseRequest> {
        @Override
        public void validate(VerboseRequest target, ValidationContext context) {
            context.reportError(ResponseCode.of(400, "Error 1"));
            context.reportError(ResponseCode.of(400, "Error 2"));
        }
    }

    @TestConfiguration
    static class WhitelistConfig {
        @org.springframework.context.annotation.Bean
        FailFastConfigurer failFastConfigurer() {
            return new FailFastConfigurer() {
                @Override
                public void addValidatorWhitelist(ValidatorWhitelistRegistry registry) {
                    registry.add(VerboseValidator.class);
                }
            };
        }
    }
}
