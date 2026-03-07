package com.chao.failfast.i18n;

import com.chao.failfast.Failure;
import com.chao.failfast.annotation.Validate;
import com.chao.failfast.config.FailFastAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {FailFastAutoConfiguration.class, I18nPropertiesIntegrationTest.TestController.class},
        properties = {
                "fail-fast.i18n.default-locale=en_US",
                "fail-fast.i18n.basename=classpath:i18n/messages"
        }
)
@EnableAutoConfiguration
@AutoConfigureMockMvc
public class I18nPropertiesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testDefaultLocaleIsEnglishViaProperties() throws Exception {
        // Without Accept-Language header, should use default locale from properties (en_US)
        mockMvc.perform(get("/test/validate-props")
                        .param("code", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Current value notBlank")) // response.code.validation.error_400 in English
                .andExpect(jsonPath("$.description").value(containsString("Current value must not be blank"))); // response.code.not.blank in English
    }

    @RestController
    @Validated
    public static class TestController {

        @GetMapping("/test/validate-props")
        @Validate
        public void validate(@RequestParam String code) {

            Failure.begin()
                    .notBlank(code)
                    .fail();
        }
    }
}
