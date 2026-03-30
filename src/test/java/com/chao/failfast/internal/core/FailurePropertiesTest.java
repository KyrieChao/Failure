package com.chao.failfast.internal.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.chao.failfast.config.properties.FailureProperties;
@DisplayName("FailureProperties 测试")
class FailurePropertiesTest {

    @Test
    @DisplayName("默认构造应使用默认值")
    void defaultConstructorShouldUseDefaultValues() {
        FailureProperties properties = new FailureProperties();
        
        assertThat(properties.isShadowTrace()).isFalse();
        assertThat(properties.isVerbose()).isFalse();
        assertThat(properties.isDebugSnapshot()).isFalse();
        assertThat(properties.isMethodValidationEnabled()).isFalse();
        assertThat(properties.getCodeMapping()).isNotNull();
        assertThat(properties.getI18n()).isNotNull();
        assertThat(properties.getTraceId()).isNotNull();
    }

    @Test
    @DisplayName("I18n 默认值应正确")
    void i18nDefaultValuesShouldBeCorrect() {
        FailureProperties properties = new FailureProperties();
        FailureProperties.I18n i18n = properties.getI18n();
        
        assertThat(i18n.isEnabled()).isTrue();
        assertThat(i18n.getDefaultLocale()).isEqualTo("zh_CN");
        assertThat(i18n.getBasename()).isEqualTo("classpath:i18n/messages");
        assertThat(i18n.getEncoding()).isEqualTo("UTF-8");
        assertThat(i18n.getCacheSeconds()).isEqualTo(3600);
    }

    @Test
    @DisplayName("CodeMapping 默认值应正确")
    void codeMappingDefaultValuesShouldBeCorrect() {
        FailureProperties properties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = properties.getCodeMapping();
        
        assertThat(codeMapping.getHttpStatus()).isNotNull().isEmpty();
        assertThat(codeMapping.getGroups()).isNotNull().isEmpty();
        assertThat(codeMapping.getConstraintMapping()).isNotNull().isEmpty();
        assertThat(codeMapping.getConstraintPathMapping()).isNotNull().isEmpty();
        assertThat(codeMapping.getConstraintBeanMapping()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("TraceId 默认值应正确")
    void traceIdDefaultValuesShouldBeCorrect() {
        FailureProperties properties = new FailureProperties();
        FailureProperties.TraceId traceId = properties.getTraceId();
        
        assertThat(traceId.isEnabled()).isFalse();
        assertThat(traceId.getHeaderName()).isEqualTo("X-Trace-Id");
        assertThat(traceId.isGenerateIfMissing()).isFalse();
        assertThat(traceId.isResponseHeader()).isFalse();
        assertThat(traceId.getResponseHeaderName()).isEqualTo("X-Trace-Id");
        assertThat(traceId.isMdcEnabled()).isFalse();
        assertThat(traceId.getMdcKey()).isEqualTo("traceId");
    }

    @Test
    @DisplayName("应能正确设置和获取属性值")
    void shouldBeAbleToSetAndGetPropertyValues() {
        FailureProperties properties = new FailureProperties();
        
        // 设置属性
        properties.setShadowTrace(true);
        properties.setVerbose(true);
        properties.setDebugSnapshot(true);
        properties.setMethodValidationEnabled(true);
        
        // 验证设置
        assertThat(properties.isShadowTrace()).isTrue();
        assertThat(properties.isVerbose()).isTrue();
        assertThat(properties.isDebugSnapshot()).isTrue();
        assertThat(properties.isMethodValidationEnabled()).isTrue();
    }

    @Test
    @DisplayName("ConstraintPathMapping 应正确设置和获取值")
    void constraintPathMappingShouldSetAndGetValueCorrectly() {
        FailureProperties.CodeMapping.ConstraintPathMapping mapping = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping.setConstraint("NotNull");
        mapping.setPath("user.name");
        mapping.setCode(400);
        
        assertThat(mapping.getConstraint()).isEqualTo("NotNull");
        assertThat(mapping.getPath()).isEqualTo("user.name");
        assertThat(mapping.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("ConstraintBeanMapping 应正确设置和获取值")
    void constraintBeanMappingShouldSetAndGetValueCorrectly() {
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping.setConstraint("NotNull");
        mapping.setBean("User");
        mapping.setCode(400);
        
        assertThat(mapping.getConstraint()).isEqualTo("NotNull");
        assertThat(mapping.getBean()).isEqualTo("User");
        assertThat(mapping.getCode()).isEqualTo(400);
    }
}
