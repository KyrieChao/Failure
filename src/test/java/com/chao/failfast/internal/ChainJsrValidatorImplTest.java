package com.chao.failfast.internal;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.constant.Scenario;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.internal.core.Chain;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.exception.Business;
import com.chao.failfast.internal.chain.pipeline.ChainCore;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Chain.JsrValidatorImpl 100% 覆盖率测试
 */
@DisplayName("Chain.JsrValidatorImpl 完整覆盖测试")
public class ChainJsrValidatorImplTest {

    @BeforeEach
    void setUp() {
        // 重置静态状态
        Chain.setValidator(null);
        Chain.setFailureProperties(null);
    }

    // 测试模型类
    static class TestModel {
        @NotNull(message = "Name cannot be null")
        @NotBlank(message = "Name cannot be blank")
        private String name;
        
        @NotNull(message = "Age cannot be null")
        private Integer age;

        public TestModel(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }
    }

    // 测试组
    interface TestGroup {}

    @Test
    @DisplayName("测试 jsr(Object) 方法 - 目标对象为null")
    void testJsrWithNullTarget() {
        Chain chain = Chain.begin(true);
        chain.jsr(null).validate();
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 jsr(Object) 方法 - 跳过验证")
    void testJsrWithSkipValidation() {
        // 移除验证器，这样会跳过验证
        Chain.setValidator(null);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("test", 18);
        chain.jsr(model).validate();
        // 应该跳过验证，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 jsr(Object) 方法 - 无验证器")
    void testJsrWithNoValidator() {
        // 移除验证器
        Chain.setValidator(null);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("test", 18);
        chain.jsr(model).validate();
        // 应该跳过验证，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 jsr(Class) 方法 - 跳过验证")
    void testJsrWithClassAndSkipValidation() {
        // 移除验证器，这样会跳过验证
        Chain.setValidator(null);
        
        Chain chain = Chain.begin(true);
        chain.jsr(TestModel.class).value("name", "test");
        // 应该跳过验证，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 jsr(Class) 方法 - 无验证器")
    void testJsrWithClassAndNoValidator() {
        // 移除验证器
        Chain.setValidator(null);
        
        Chain chain = Chain.begin(true);
        chain.jsr(TestModel.class).value("name", "test");
        // 应该跳过验证，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 jsr(Class) 方法 - beanClass为null")
    void testJsrWithClassNullBeanClass() {
        Chain chain = Chain.begin(true);
        chain.jsr(null).value("name", "test");
        // 应该跳过验证，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 jsr(Class) 方法 - propertyName为null")
    void testJsrWithClassNullPropertyName() {
        Chain chain = Chain.begin(true);
        chain.jsr(TestModel.class).value(null, "test");
        // 应该跳过验证，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 pathPrefix 方法")
    void testPathPrefix() {
        // 移除验证器，这样会跳过验证
        Chain.setValidator(null);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("test", 18);
        // 测试pathPrefix方法的链式调用
        chain.jsr(model).pathPrefix("user").validate();
        // 应该跳过验证，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 pathPrefix 方法 - 空路径前缀")
    void testPathPrefixEmpty() {
        // 移除验证器，这样会跳过验证
        Chain.setValidator(null);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("test", 18);
        // 测试空路径前缀
        chain.jsr(model).pathPrefix("").validate();
        // 应该跳过验证，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 jsr(Object) 方法 - 带groups验证")
    void testJsrWithGroups() {
        // 移除验证器，这样会跳过验证
        Chain.setValidator(null);
        
        // 使用正确的构造器创建ValidationContext
        ValidationContext context = new ValidationContext(true, new Scenario[0], new Class[]{TestGroup.class});
        
        Chain chain = Chain.begin(context);
        TestModel model = new TestModel("test", 18);
        chain.jsr(model).validate();
        // 由于没有实际的验证器实现，这里应该跳过验证
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 jsr(Class) 方法 - 验证属性值")
    void testJsrWithClassAndValue() {
        // 移除验证器，这样会跳过验证
        Chain.setValidator(null);
        
        Chain chain = Chain.begin(true);
        chain.jsr(TestModel.class).value("name", "test");
        // 由于没有实际的验证器实现，这里应该跳过验证
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 jsr(Class) 方法 - 验证属性值失败")
    void testJsrWithClassAndValueFailure() {
        // 移除验证器，这样会跳过验证
        Chain.setValidator(null);
        
        Chain chain = Chain.begin(true);
        chain.jsr(TestModel.class).value("name", "");
        // 由于没有实际的验证器实现，这里应该跳过验证
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 shouldSkip 为 true 的情况")
    void testShouldSkip() {
        // 移除验证器，这样会跳过验证
        Chain.setValidator(null);
        
        Chain chain = Chain.begin(true);
        // 使 shouldSkip() 返回 true
        chain.when(false);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 应该跳过验证，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 validate() 方法 - 有验证器且验证通过的情况")
    void testValidateWithValidatorAndPass() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证通过的情况
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(Collections.emptySet());
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("test", 18);
        chain.jsr(model).validate();
        // 验证通过，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 validate() 方法 - 有验证器且验证失败的情况")
    void testValidateWithValidatorAndFail() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 validate() 方法 - 有验证器且有 groups 的情况")
    void testValidateWithValidatorAndGroups() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证通过的情况
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(Collections.emptySet());
        Chain.setValidator(mockValidator);
        
        // 使用带 groups 的 ValidationContext
        ValidationContext context = new ValidationContext(true, new Scenario[0], new Class[]{TestGroup.class});
        Chain chain = Chain.begin(context);
        TestModel model = new TestModel("test", 18);
        chain.jsr(model).validate();
        // 验证通过，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 value() 方法 - 有验证器且验证通过的情况")
    void testValueWithValidatorAndPass() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证通过的情况
        when(mockValidator.validateValue(any(Class.class), anyString(), any(), any(Class[].class))).thenReturn(Collections.emptySet());
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        chain.jsr(TestModel.class).value("name", "test");
        // 验证通过，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 value() 方法 - 有验证器且验证失败的情况")
    void testValueWithValidatorAndFail() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validateValue(any(Class.class), anyString(), any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        chain.jsr(TestModel.class).value("name", "");
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 value() 方法 - 有验证器且有 groups 的情况")
    void testValueWithValidatorAndGroups() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证通过的情况
        when(mockValidator.validateValue(any(Class.class), anyString(), any(), any(Class[].class))).thenReturn(Collections.emptySet());
        Chain.setValidator(mockValidator);
        
        // 使用带 groups 的 ValidationContext
        ValidationContext context = new ValidationContext(true, new Scenario[0], new Class[]{TestGroup.class});
        Chain chain = Chain.begin(context);
        chain.jsr(TestModel.class).value("name", "test");
        // 验证通过，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - 有 pathPrefix 的情况")
    void testProcessViolationsWithPathPrefix() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).pathPrefix("user").validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - 空 pathPrefix 的情况")
    void testProcessViolationsWithEmptyPathPrefix() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).pathPrefix("").validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - 空 propertyPath 的情况")
    void testProcessViolationsWithEmptyPropertyPath() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl(""));
        when(mockViolation.getMessage()).thenReturn("Validation failed");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).pathPrefix("user").validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - 有 failureProperties 的情况")
    void testProcessViolationsWithFailureProperties() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintMapping
        java.util.Map<String, Integer> constraintMapping = new java.util.HashMap<>();
        constraintMapping.put("NotBlank", 4001);
        codeMapping.setConstraintMapping(constraintMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - 有 constraintPathMapping 的情况")
    void testProcessViolationsWithConstraintPathMapping() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 constraintPathMapping
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintPathMapping
        java.util.List<FailureProperties.CodeMapping.ConstraintPathMapping> constraintPathMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintPathMapping mapping = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping.setConstraint("NotBlank");
        mapping.setPath("name");
        mapping.setCode(4002);
        constraintPathMapping.add(mapping);
        codeMapping.setConstraintPathMapping(constraintPathMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - 有 constraintBeanMapping 的情况")
    void testProcessViolationsWithConstraintBeanMapping() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 constraintBeanMapping
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintBeanMapping
        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping.setConstraint("NotBlank");
        mapping.setBean(TestModel.class.getName());
        mapping.setCode(4003);
        constraintBeanMapping.add(mapping);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - 有 null rule 的情况")
    void testProcessViolationsWithNullRule() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 null rule
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintPathMapping 包含 null rule
        java.util.List<FailureProperties.CodeMapping.ConstraintPathMapping> constraintPathMapping = new java.util.ArrayList<>();
        constraintPathMapping.add(null); // 添加 null rule
        FailureProperties.CodeMapping.ConstraintPathMapping mapping = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping.setConstraint("NotBlank");
        mapping.setPath("name");
        mapping.setCode(4002);
        constraintPathMapping.add(mapping);
        codeMapping.setConstraintPathMapping(constraintPathMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - 有 null code 的 rule 情况")
    void testProcessViolationsWithNullCodeRule() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 null code 的 rule
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintPathMapping 包含 null code 的 rule
        java.util.List<FailureProperties.CodeMapping.ConstraintPathMapping> constraintPathMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintPathMapping mapping = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping.setConstraint("NotBlank");
        mapping.setPath("name");
        // 不设置 code，默认为 null
        constraintPathMapping.add(mapping);
        codeMapping.setConstraintPathMapping(constraintPathMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - 有 null constraint 或 path 的 rule 情况")
    void testProcessViolationsWithNullConstraintOrPathRule() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 null constraint 或 path 的 rule
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintPathMapping 包含 null constraint 的 rule
        java.util.List<FailureProperties.CodeMapping.ConstraintPathMapping> constraintPathMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintPathMapping mapping1 = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping1.setPath("name");
        mapping1.setCode(4002);
        // 不设置 constraint，默认为 null
        constraintPathMapping.add(mapping1);
        
        FailureProperties.CodeMapping.ConstraintPathMapping mapping2 = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping2.setConstraint("NotBlank");
        mapping2.setCode(4002);
        // 不设置 path，默认为 null
        constraintPathMapping.add(mapping2);
        
        codeMapping.setConstraintPathMapping(constraintPathMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - 有 null rootBeanClass 的情况")
    void testProcessViolationsWithNullRootBeanClass() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        // 设置 rootBeanClass 为 null
        doReturn(null).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintBeanMapping
        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping.setConstraint("NotBlank");
        mapping.setBean(TestModel.class.getName());
        mapping.setCode(4003);
        constraintBeanMapping.add(mapping);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }



    @Test
    @DisplayName("测试 validate() 方法 - 无 groups 的情况")
    void testValidateWithNoGroups() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证通过的情况
        when(mockValidator.validate(any())).thenReturn(Collections.emptySet());
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("test", 18);
        chain.jsr(model).validate();
        // 验证通过，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 value() 方法 - 无 groups 的情况")
    void testValueWithNoGroups() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证通过的情况
        when(mockValidator.validateValue(any(Class.class), anyString(), any())).thenReturn(Collections.emptySet());
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        chain.jsr(TestModel.class).value("name", "test");
        // 验证通过，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - failureProperties 为 null 的情况")
    void testProcessViolationsWithNullFailureProperties() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 确保 failureProperties 为 null
        Chain.setFailureProperties(null);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - failureProperties.getCodeMapping() 为 null 的情况")
    void testProcessViolationsWithNullCodeMapping() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 但 codeMapping 为 null
        FailureProperties failureProperties = new FailureProperties();
        // 不设置 codeMapping，默认为 null
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintPathMapping 为 null 的情况")
    void testProcessViolationsWithNullConstraintPathMapping() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 但 constraintPathMapping 为 null
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 不设置 constraintPathMapping，默认为 null
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintBeanMapping 为 null 的情况")
    void testProcessViolationsWithNullConstraintBeanMapping() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 但 constraintBeanMapping 为 null
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 不设置 constraintBeanMapping，默认为 null
        codeMapping.setConstraintPathMapping(new java.util.ArrayList<>());
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintMapping 为 null 的情况")
    void testProcessViolationsWithNullConstraintMapping() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 但 constraintMapping 为 null
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 不设置 constraintMapping，默认为 null
        codeMapping.setConstraintPathMapping(new java.util.ArrayList<>());
        codeMapping.setConstraintBeanMapping(new java.util.ArrayList<>());
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - pathPrefix 为空字符串的情况（新增）")
    void testProcessViolationsWithEmptyPathPrefixNew() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).pathPrefix("").validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - path 为空字符串的情况")
    void testProcessViolationsWithEmptyPath() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl(""));
        when(mockViolation.getMessage()).thenReturn("Validation failed");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).pathPrefix("user").validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintPathMapping 匹配成功的情况")
    void testProcessViolationsWithConstraintPathMappingMatch() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 constraintPathMapping 匹配的情况
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintPathMapping 与 violation 匹配
        java.util.List<FailureProperties.CodeMapping.ConstraintPathMapping> constraintPathMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintPathMapping mapping = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping.setConstraint("NotBlank");
        mapping.setPath("name");
        mapping.setCode(4002);
        constraintPathMapping.add(mapping);
        codeMapping.setConstraintPathMapping(constraintPathMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }
    
    @Test
    @DisplayName("测试 processViolations() 方法 - constraintBeanMapping 匹配成功的情况")
    void testProcessViolationsWithConstraintBeanMappingMatch() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 constraintBeanMapping 匹配的情况
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintBeanMapping 与 violation 匹配
        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping.setConstraint("NotBlank");
        mapping.setBean(TestModel.class.getName());
        mapping.setCode(4003);
        constraintBeanMapping.add(mapping);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }
    
    @Test
    @DisplayName("测试 processViolations() 方法 - constraintMapping 匹配成功的情况")
    void testProcessViolationsWithConstraintMappingMatch() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 constraintMapping 匹配的情况
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintMapping 与 violation 匹配
        java.util.Map<String, Integer> constraintMapping = new java.util.HashMap<>();
        constraintMapping.put("NotBlank", 4001);
        codeMapping.setConstraintMapping(constraintMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }
    
    @Test
    @DisplayName("测试 processViolations() 方法 - 验证成功的情况")
    void testProcessViolationsWithSuccess() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证成功的情况，返回空的 violations 集合
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.emptySet();
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("test", 18);
        chain.jsr(model).validate();
        // 验证成功，所以链有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - 多个 violation 且验证失败的情况")
    void testProcessViolationsWithMultipleViolationsAndFailure() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况，返回两个 violation
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation1 = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation1.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation1.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation1.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor1 = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor1.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation1.getConstraintDescriptor()).thenReturn(mockDescriptor1);
        doReturn(TestModel.class).when(mockViolation1).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation2 = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation2.getPropertyPath()).thenReturn(new PathImpl("age"));
        when(mockViolation2.getMessage()).thenReturn("Age cannot be null");
        when(mockViolation2.getInvalidValue()).thenReturn(null);
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor2 = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotNull mockNotNull = mock(NotNull.class);
        doReturn(NotNull.class).when(mockNotNull).annotationType();
        when(mockDescriptor2.getAnnotation()).thenReturn(mockNotNull);
        when(mockViolation2.getConstraintDescriptor()).thenReturn(mockDescriptor2);
        doReturn(TestModel.class).when(mockViolation2).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        violations.add(mockViolation1);
        violations.add(mockViolation2);
        
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", null);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }





    @Test
    @DisplayName("测试 processViolations() 方法 - 验证过程中 shouldSkip 变为 true 的情况")
    void testProcessViolationsWithShouldSkipDuringValidation() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况，返回两个 violation
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation1 = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation1.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation1.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation1.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor1 = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor1.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation1.getConstraintDescriptor()).thenReturn(mockDescriptor1);
        doReturn(TestModel.class).when(mockViolation1).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation2 = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation2.getPropertyPath()).thenReturn(new PathImpl("age"));
        when(mockViolation2.getMessage()).thenReturn("Age cannot be null");
        when(mockViolation2.getInvalidValue()).thenReturn(null);
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor2 = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotNull mockNotNull = mock(NotNull.class);
        doReturn(NotNull.class).when(mockNotNull).annotationType();
        when(mockDescriptor2.getAnnotation()).thenReturn(mockNotNull);
        when(mockViolation2.getConstraintDescriptor()).thenReturn(mockDescriptor2);
        doReturn(TestModel.class).when(mockViolation2).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        violations.add(mockViolation1);
        violations.add(mockViolation2);
        
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", null);
        // 使用when(false)方法来设置conditionState为false，使shouldSkip()返回true
        chain.when(false);
        chain.jsr(model).validate();
        // 由于 shouldSkip 为 true，应该跳过验证，所以链有效
        assertTrue(chain.isValid());
    }



    @Test
    @DisplayName("测试 processViolations() 方法 - 多个 constraintPathMapping 规则，只有一个匹配的情况")
    void testProcessViolationsWithMultipleConstraintPathMapping() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带多个 constraintPathMapping 规则
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置多个 constraintPathMapping 规则，只有一个匹配
        java.util.List<FailureProperties.CodeMapping.ConstraintPathMapping> constraintPathMapping = new java.util.ArrayList<>();
        
        // 不匹配的规则
        FailureProperties.CodeMapping.ConstraintPathMapping mapping1 = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping1.setConstraint("NotNull");
        mapping1.setPath("name");
        mapping1.setCode(4001);
        constraintPathMapping.add(mapping1);
        
        // 匹配的规则
        FailureProperties.CodeMapping.ConstraintPathMapping mapping2 = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping2.setConstraint("NotBlank");
        mapping2.setPath("name");
        mapping2.setCode(4002);
        constraintPathMapping.add(mapping2);
        
        // 不匹配的规则
        FailureProperties.CodeMapping.ConstraintPathMapping mapping3 = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping3.setConstraint("NotBlank");
        mapping3.setPath("age");
        mapping3.setCode(4003);
        constraintPathMapping.add(mapping3);
        
        codeMapping.setConstraintPathMapping(constraintPathMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }
    

    
    @Test
    @DisplayName("测试 processViolations() 方法 - constraintMapping 不包含约束名称的情况")
    void testProcessViolationsWithConstraintMappingNotContainingConstraint() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 但 constraintMapping 不包含 NotBlank
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintPathMapping 为空列表
        codeMapping.setConstraintPathMapping(new java.util.ArrayList<>());
        // 设置 constraintBeanMapping 为空列表
        codeMapping.setConstraintBeanMapping(new java.util.ArrayList<>());
        // 设置 constraintMapping 但不包含 NotBlank
        java.util.Map<String, Integer> constraintMapping = new java.util.HashMap<>();
        constraintMapping.put("NotNull", 4001);
        codeMapping.setConstraintMapping(constraintMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }
    
    @Test
    @DisplayName("测试 processViolations() 方法 - constraintPathMapping 规则不匹配的情况")
    void testProcessViolationsWithConstraintPathMappingNotMatching() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 constraintPathMapping 但规则不匹配
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintPathMapping 但规则不匹配
        java.util.List<FailureProperties.CodeMapping.ConstraintPathMapping> constraintPathMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintPathMapping mapping = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping.setConstraint("NotNull"); // 不匹配 NotBlank
        mapping.setPath("name");
        mapping.setCode(4002);
        constraintPathMapping.add(mapping);
        codeMapping.setConstraintPathMapping(constraintPathMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }
    
    @Test
    @DisplayName("测试 processViolations() 方法 - constraintBeanMapping 规则不匹配的情况")
    void testProcessViolationsWithConstraintBeanMappingNotMatching() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 constraintBeanMapping 但规则不匹配
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintPathMapping 为空列表
        codeMapping.setConstraintPathMapping(new java.util.ArrayList<>());
        // 设置 constraintBeanMapping 但规则不匹配
        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping.setConstraint("NotNull"); // 不匹配 NotBlank
        mapping.setBean(TestModel.class.getName());
        mapping.setCode(4003);
        constraintBeanMapping.add(mapping);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }
    


    @Test
    @DisplayName("测试 getSceneName() 方法 - scenes 为 null 的情况")
    void testGetSceneNameWithNullScenes() {
        // 使用带 null scenes 的 ValidationContext
        ValidationContext context = new ValidationContext(true, (Scenario[]) null, new Class[0]);
        Chain chain = Chain.begin(context);
        
        // 通过反射调用 getSceneName() 方法
        try {
            java.lang.reflect.Method getSceneNameMethod = Chain.class.getDeclaredMethod("getSceneName");
            getSceneNameMethod.setAccessible(true);
            String sceneName = (String) getSceneNameMethod.invoke(chain);
            assertEquals(Scenario.DEFAULT.name(), sceneName);
        } catch (Exception e) {
            fail("反射调用 getSceneName() 方法失败", e);
        }
    }

    @Test
    @DisplayName("测试 value() 方法 - 跳过验证的情况")
    void testValueWithShouldSkip() {
        Chain chain = Chain.begin(true);
        // 使 shouldSkip() 返回 true
        chain.when(false);
        chain.jsr(TestModel.class).value("name", "");
        // 应该跳过验证，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintMapping 不包含 constraintName 的情况")
    void testProcessViolationsWithConstraintMappingNotContainConstraintName() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 但 constraintMapping 不包含 NotBlank
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        codeMapping.setConstraintPathMapping(new java.util.ArrayList<>());
        codeMapping.setConstraintBeanMapping(new java.util.ArrayList<>());
        // 设置 constraintMapping 但不包含 NotBlank
        java.util.Map<String, Integer> constraintMapping = new java.util.HashMap<>();
        constraintMapping.put("NotNull", 4001);
        codeMapping.setConstraintMapping(constraintMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - failureProperties 不为 null 但 codeMapping 为 null 的情况")
    void testProcessViolationsWithFailurePropertiesNotNullButCodeMappingNull() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 但 codeMapping 为 null
        FailureProperties failureProperties = new FailureProperties();
        // 不设置 codeMapping，默认为 null
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintPathMapping 不为 null 但为空的情况")
    void testProcessViolationsWithConstraintPathMappingNotNullButEmpty() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 但 constraintPathMapping 为空
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        codeMapping.setConstraintPathMapping(new java.util.ArrayList<>());
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintBeanMapping 不为 null 但为空的情况")
    void testProcessViolationsWithConstraintBeanMappingNotNullButEmpty() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 但 constraintBeanMapping 为空
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        codeMapping.setConstraintPathMapping(new java.util.ArrayList<>());
        codeMapping.setConstraintBeanMapping(new java.util.ArrayList<>());
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintBeanMapping 规则为 null 的情况")
    void testProcessViolationsWithNullConstraintBeanMappingRule() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 null 规则
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintBeanMapping 包含 null 规则
        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        constraintBeanMapping.add(null); // 添加 null 规则
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping.setConstraint("NotBlank");
        mapping.setBean(TestModel.class.getName());
        mapping.setCode(4003);
        constraintBeanMapping.add(mapping);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintBeanMapping 规则 code 为 null 的情况")
    void testProcessViolationsWithConstraintBeanMappingRuleCodeNull() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 code 为 null 的规则
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintBeanMapping 包含 code 为 null 的规则
        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping.setConstraint("NotBlank");
        mapping.setBean(TestModel.class.getName());
        // 不设置 code，默认为 null
        constraintBeanMapping.add(mapping);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintBeanMapping 规则 constraint 为 null 的情况")
    void testProcessViolationsWithConstraintBeanMappingRuleConstraintNull() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 constraint 为 null 的规则
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintBeanMapping 包含 constraint 为 null 的规则
        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        // 不设置 constraint，默认为 null
        mapping.setBean(TestModel.class.getName());
        mapping.setCode(4003);
        constraintBeanMapping.add(mapping);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }


    
    @Test
    @DisplayName("测试 processViolations() 方法 - 所有映射规则都匹配的情况")
    void testProcessViolationsWithAllMappingRulesMatch() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带所有类型的映射规则
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        
        // 设置 constraintPathMapping
        java.util.List<FailureProperties.CodeMapping.ConstraintPathMapping> constraintPathMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintPathMapping pathMapping = new FailureProperties.CodeMapping.ConstraintPathMapping();
        pathMapping.setConstraint("NotBlank");
        pathMapping.setPath("name");
        pathMapping.setCode(4002);
        constraintPathMapping.add(pathMapping);
        codeMapping.setConstraintPathMapping(constraintPathMapping);
        
        // 设置 constraintBeanMapping
        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintBeanMapping beanMapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        beanMapping.setConstraint("NotBlank");
        beanMapping.setBean(TestModel.class.getName());
        beanMapping.setCode(4003);
        constraintBeanMapping.add(beanMapping);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);
        
        // 设置 constraintMapping
        java.util.Map<String, Integer> constraintMapping = new java.util.HashMap<>();
        constraintMapping.put("NotBlank", 4001);
        codeMapping.setConstraintMapping(constraintMapping);
        
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }
    

    


    @Test
    @DisplayName("测试 processViolations() 方法 - 多个 constraintBeanMapping 规则，只有一个匹配的情况")
    void testProcessViolationsWithMultipleConstraintBeanMapping() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带多个 constraintBeanMapping 规则
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置多个 constraintBeanMapping 规则，只有一个匹配
        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        
        // 不匹配的规则
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping1 = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping1.setConstraint("NotNull");
        mapping1.setBean(TestModel.class.getName());
        mapping1.setCode(4001);
        constraintBeanMapping.add(mapping1);
        
        // 匹配的规则
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping2 = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping2.setConstraint("NotBlank");
        mapping2.setBean(TestModel.class.getName());
        mapping2.setCode(4002);
        constraintBeanMapping.add(mapping2);
        
        // 不匹配的规则
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping3 = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping3.setConstraint("NotBlank");
        mapping3.setBean("java.lang.String");
        mapping3.setCode(4003);
        constraintBeanMapping.add(mapping3);
        
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintBeanMapping 规则 bean 为 null 的情况")
    void testProcessViolationsWithConstraintBeanMappingRuleBeanNull() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 bean 为 null 的规则
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintBeanMapping 包含 bean 为 null 的规则
        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping.setConstraint("NotBlank");
        // 不设置 bean，默认为 null
        mapping.setCode(4003);
        constraintBeanMapping.add(mapping);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintMapping 包含 constraintName 的情况")
    void testProcessViolationsWithConstraintMappingContainingConstraintName() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 constraintMapping 包含 NotBlank
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        codeMapping.setConstraintPathMapping(new java.util.ArrayList<>());
        codeMapping.setConstraintBeanMapping(new java.util.ArrayList<>());
        // 设置 constraintMapping 包含 NotBlank
        java.util.Map<String, Integer> constraintMapping = new java.util.HashMap<>();
        constraintMapping.put("NotBlank", 4001);
        codeMapping.setConstraintMapping(constraintMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - 所有映射都不匹配的情况")
    void testProcessViolationsWithAllMappingsNotMatching() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 但所有映射都不匹配
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        
        // 设置 constraintPathMapping 但规则不匹配
        java.util.List<FailureProperties.CodeMapping.ConstraintPathMapping> constraintPathMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintPathMapping pathMapping = new FailureProperties.CodeMapping.ConstraintPathMapping();
        pathMapping.setConstraint("NotNull");
        pathMapping.setPath("age");
        pathMapping.setCode(4002);
        constraintPathMapping.add(pathMapping);
        codeMapping.setConstraintPathMapping(constraintPathMapping);
        
        // 设置 constraintBeanMapping 但规则不匹配
        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintBeanMapping beanMapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        beanMapping.setConstraint("NotNull");
        beanMapping.setBean("com.example.OtherModel");
        beanMapping.setCode(4003);
        constraintBeanMapping.add(beanMapping);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);
        
        // 设置 constraintMapping 但不包含 NotBlank
        java.util.Map<String, Integer> constraintMapping = new java.util.HashMap<>();
        constraintMapping.put("NotNull", 4001);
        codeMapping.setConstraintMapping(constraintMapping);
        
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintPathMapping 不为 null 且不为空但规则不匹配的情况")
    void testProcessViolationsWithConstraintPathMappingNotNullNotEmptyButNoMatch() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 constraintPathMapping 但规则不匹配
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintPathMapping 但规则不匹配
        java.util.List<FailureProperties.CodeMapping.ConstraintPathMapping> constraintPathMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintPathMapping mapping = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping.setConstraint("NotNull");
        mapping.setPath("age");
        mapping.setCode(4002);
        constraintPathMapping.add(mapping);
        codeMapping.setConstraintPathMapping(constraintPathMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintBeanMapping 不为 null 且不为空但规则不匹配的情况")
    void testProcessViolationsWithConstraintBeanMappingNotNullNotEmptyButNoMatch() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 带 constraintBeanMapping 但规则不匹配
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        codeMapping.setConstraintPathMapping(new java.util.ArrayList<>());
        // 设置 constraintBeanMapping 但规则不匹配
        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping.setConstraint("NotNull");
        mapping.setBean("com.example.OtherModel");
        mapping.setCode(4003);
        constraintBeanMapping.add(mapping);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }
    
    @Test
    @DisplayName("测试 processViolations() 方法 - 所有映射规则都不匹配的情况")
    void testProcessViolationsWithNoMappingMatch() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        // 创建并设置 FailureProperties 但所有规则都不匹配
        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();
        // 设置 constraintPathMapping 但不匹配
        java.util.List<FailureProperties.CodeMapping.ConstraintPathMapping> constraintPathMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintPathMapping mapping1 = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping1.setConstraint("NotNull");
        mapping1.setPath("age");
        mapping1.setCode(4001);
        constraintPathMapping.add(mapping1);
        codeMapping.setConstraintPathMapping(constraintPathMapping);
        
        // 设置 constraintBeanMapping 但不匹配
        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping2 = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping2.setConstraint("NotNull");
        mapping2.setBean(TestModel.class.getName());
        mapping2.setCode(4002);
        constraintBeanMapping.add(mapping2);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);
        
        // 设置 constraintMapping 但不匹配
        java.util.Map<String, Integer> constraintMapping = new java.util.HashMap<>();
        constraintMapping.put("NotNull", 4003);
        codeMapping.setConstraintMapping(constraintMapping);
        
        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - 多个 violation 的情况")
    void testProcessViolationsWithMultipleViolations() {
        // 创建一个模拟的 Validator
        Validator mockValidator = mock(Validator.class);
        // 模拟验证失败的情况，返回两个 violation
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation1 = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation1.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation1.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation1.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor1 = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor1.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation1.getConstraintDescriptor()).thenReturn(mockDescriptor1);
        doReturn(TestModel.class).when(mockViolation1).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation2 = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation2.getPropertyPath()).thenReturn(new PathImpl("age"));
        when(mockViolation2.getMessage()).thenReturn("Age cannot be null");
        when(mockViolation2.getInvalidValue()).thenReturn(null);
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor2 = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotNull mockNotNull = mock(NotNull.class);
        doReturn(NotNull.class).when(mockNotNull).annotationType();
        when(mockDescriptor2.getAnnotation()).thenReturn(mockNotNull);
        when(mockViolation2.getConstraintDescriptor()).thenReturn(mockDescriptor2);
        doReturn(TestModel.class).when(mockViolation2).getRootBeanClass();
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        violations.add(mockViolation1);
        violations.add(mockViolation2);
        
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);
        
        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", null);
        chain.jsr(model).validate();
        // 验证失败，所以链无效
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 Chain.fail() 方法 - getCauses().isEmpty() 分支")
    void testFailWithEmptyCauses() {
        // 创建一个Chain实例
        Chain chain = Chain.begin(true);
        
        // 使用反射设置alive为false，使isValid()返回false
        try {
            java.lang.reflect.Field aliveField = ChainCore.class.getDeclaredField("alive");
            aliveField.setAccessible(true);
            aliveField.set(chain, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 验证isValid()返回false
        assertFalse(chain.isValid());
        
        // 验证getCauses()返回空列表
        assertTrue(chain.getCauses().isEmpty());
        
        // 调用fail()方法，应该抛出Business异常
        try {
            chain.fail();
            // 如果没有抛出异常，测试失败
            fail("Expected Business exception but none was thrown");
        } catch (Business e) {
            // 验证异常的响应码是VALIDATION_ERROR_500
            assertEquals(ResponseCode.VALIDATION_ERROR_500.getCode(), e.getResponseCode().getCode());
        }
    }

    @Test
    @DisplayName("测试 Chain.failAll() 方法 - getCauses().isEmpty() 分支")
    void testFailAllWithEmptyCauses() {
        // 创建一个Chain实例
        Chain chain = Chain.begin(true);
        
        // 使用反射设置alive为false，使isValid()返回false
        try {
            java.lang.reflect.Field aliveField = ChainCore.class.getDeclaredField("alive");
            aliveField.setAccessible(true);
            aliveField.set(chain, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 验证isValid()返回false
        assertFalse(chain.isValid());
        
        // 验证getCauses()返回空列表
        assertTrue(chain.getCauses().isEmpty());
        
        // 调用failAll()方法，应该抛出Business异常
        try {
            chain.failAll();
            // 如果没有抛出异常，测试失败
            fail("Expected Business exception but none was thrown");
        } catch (Business e) {
            // 验证异常的响应码是VALIDATION_ERROR_500
            assertEquals(ResponseCode.VALIDATION_ERROR_500.getCode(), e.getResponseCode().getCode());
        }
    }

    @Test
    @DisplayName("测试 Chain.getSceneName() 方法 - 多个场景的情况")
    void testGetSceneNameWithMultipleScenes() {
        // 创建一个包含多个场景的ValidationContext
        ValidationContext context = new ValidationContext(true, new Scenario[]{Scenario.CREATE, Scenario.UPDATE}, new Class[0]);
        
        // 创建Chain实例
        Chain chain = Chain.begin(context);
        
        // 使用反射调用getSceneName()方法
        try {
            java.lang.reflect.Method getSceneNameMethod = Chain.class.getDeclaredMethod("getSceneName");
            getSceneNameMethod.setAccessible(true);
            String sceneName = (String) getSceneNameMethod.invoke(chain);
            // 验证getSceneName()返回"MULTI"
            assertEquals("MULTI", sceneName);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to call getSceneName() method");
        }
    }

    @Test
    @DisplayName("测试 JsrValidatorImpl.value() 方法 - beanClass 为 null 的情况")
    void testValueWithNullBeanClass() {
        Chain chain = Chain.begin(true);
        // 调用jsr(null)创建JsrValidatorImpl，然后调用value()方法
        chain.jsr((Class<?>) null).value("name", "test");
        // 应该跳过验证，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 JsrValidatorImpl.value() 方法 - propertyName 为 null 的情况")
    void testValueWithNullPropertyName() {
        Chain chain = Chain.begin(true);
        // 调用jsr(TestModel.class)创建JsrValidatorImpl，然后调用value(null, "test")
        chain.jsr(TestModel.class).value(null, "test");
        // 应该跳过验证，所以链仍然有效
        assertTrue(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintName 匹配但 path 不匹配的情况")
    void testProcessViolationsWithConstraintMatchButPathMismatch() {
        Validator mockValidator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();

        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);

        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();

        java.util.List<FailureProperties.CodeMapping.ConstraintPathMapping> constraintPathMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintPathMapping mapping = new FailureProperties.CodeMapping.ConstraintPathMapping();
        mapping.setConstraint("NotBlank");
        mapping.setPath("age");
        mapping.setCode(4002);
        constraintPathMapping.add(mapping);
        codeMapping.setConstraintPathMapping(constraintPathMapping);

        java.util.Map<String, Integer> constraintMapping = new java.util.HashMap<>();
        constraintMapping.put("NotBlank", 4001);
        codeMapping.setConstraintMapping(constraintMapping);

        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);

        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - constraintName 匹配但 bean 不匹配的情况")
    void testProcessViolationsWithConstraintMatchButBeanMismatch() {
        Validator mockValidator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(TestModel.class).when(mockViolation).getRootBeanClass();

        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);

        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();

        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping.setConstraint("NotBlank");
        mapping.setBean("com.example.OtherModel");
        mapping.setCode(4003);
        constraintBeanMapping.add(mapping);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);

        java.util.Map<String, Integer> constraintMapping = new java.util.HashMap<>();
        constraintMapping.put("NotBlank", 4001);
        codeMapping.setConstraintMapping(constraintMapping);

        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);

        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        assertFalse(chain.isValid());
    }

    @Test
    @DisplayName("测试 processViolations() 方法 - rootBeanClass 为 null 时应跳过 bean 映射")
    void testProcessViolationsWithNullRootBeanClassSkipsBeanMapping() {
        Validator mockValidator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(new PathImpl("name"));
        when(mockViolation.getMessage()).thenReturn("Name cannot be blank");
        when(mockViolation.getInvalidValue()).thenReturn("");
        jakarta.validation.metadata.ConstraintDescriptor mockDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        NotBlank mockNotBlank = mock(NotBlank.class);
        doReturn(NotBlank.class).when(mockNotBlank).annotationType();
        when(mockDescriptor.getAnnotation()).thenReturn(mockNotBlank);
        when(mockViolation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        doReturn(null).when(mockViolation).getRootBeanClass();

        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = Collections.singleton(mockViolation);
        when(mockValidator.validate(any(), any(Class[].class))).thenReturn(violations);
        Chain.setValidator(mockValidator);

        FailureProperties failureProperties = new FailureProperties();
        FailureProperties.CodeMapping codeMapping = new FailureProperties.CodeMapping();

        java.util.List<FailureProperties.CodeMapping.ConstraintBeanMapping> constraintBeanMapping = new java.util.ArrayList<>();
        FailureProperties.CodeMapping.ConstraintBeanMapping mapping = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        mapping.setConstraint("NotBlank");
        mapping.setBean(TestModel.class.getName());
        mapping.setCode(4003);
        constraintBeanMapping.add(mapping);
        codeMapping.setConstraintBeanMapping(constraintBeanMapping);

        java.util.Map<String, Integer> constraintMapping = new java.util.HashMap<>();
        constraintMapping.put("NotBlank", 4001);
        codeMapping.setConstraintMapping(constraintMapping);

        failureProperties.setCodeMapping(codeMapping);
        Chain.setFailureProperties(failureProperties);

        Chain chain = Chain.begin(true);
        TestModel model = new TestModel("", 18);
        chain.jsr(model).validate();
        assertFalse(chain.isValid());
    }
    static class PathImpl implements jakarta.validation.Path {
        private final String path;

        public PathImpl(String path) {
            this.path = path;
        }

        @Override
        public Iterator<Node> iterator() {
            return null;
        }

        @Override
        public String toString() {
            return path;
        }
    }
}  

