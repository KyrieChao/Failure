package com.chao.failfast.i18n;

import com.chao.failfast.annotation.Validate;
import com.chao.failfast.autoconfigure.FailFastAutoConfiguration;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {FailFastAutoConfiguration.class, I18nIntegrationTest.TestController.class})
@TestPropertySource(properties = "fail-fast.i18n.default-locale=zh_CN")
@EnableAutoConfiguration
@AutoConfigureMockMvc
@Slf4j
public class I18nIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testDefaultLocaleIsChinese() throws Exception {
        // Default locale should be Chinese as per I18nConfig
        mockMvc.perform(get("/test/validate")
                        .param("code", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败")) // response.code.validation.error_400 in Chinese
                .andExpect(jsonPath("$.description").value(containsString("当前值不能为空"))); // response.code.not.blank in Chinese
    }

    @Test
    public void testEnglishLocale() throws Exception {
        mockMvc.perform(get("/test/validate")
                        .param("code", "")
                        .header("Accept-Language", "en-US")
                        .locale(Locale.US)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Error")) // response.code.validation.error_400 in English
                .andExpect(jsonPath("$.description").value(containsString("Current value must not be blank"))); // response.code.not.blank in English
    }

    @RestController
    @Validated
    public static class TestController {

        @GetMapping("/test/validate")
        @Validate
        public void validate(@RequestParam @NotBlank(message = "{response.code.not.blank}") String code) {
        }
    }
}
