package com.chao.failure.internal.core;

import com.chao.failure.config.properties.FailureProperties;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChainJsrValidatorMappingCoverageTest {

    static class BeanA {
        @NotNull
        String field;
    }

    static class BeanB {
        @NotNull
        String field;
    }

    @AfterEach
    void tearDown() {
        Chain.setValidator(null);
        Chain.setFailureProperties(null);
    }

    @Test
    void jsrProcessViolationsCoversCodeMappingBranches() throws Exception {
        FailureProperties props = new FailureProperties();
        FailureProperties.CodeMapping mapping = new FailureProperties.CodeMapping();

        FailureProperties.CodeMapping.ConstraintPathMapping rule1 = new FailureProperties.CodeMapping.ConstraintPathMapping();
        rule1.setConstraint("NotNull");
        rule1.setPath("field");
        rule1.setCode(40001);

        FailureProperties.CodeMapping.ConstraintBeanMapping rule2 = new FailureProperties.CodeMapping.ConstraintBeanMapping();
        rule2.setConstraint("NotNull");
        rule2.setBean(BeanA.class.getName());
        rule2.setCode(40002);

        mapping.setConstraintPathMapping(List.of(rule1));
        mapping.setConstraintBeanMapping(List.of(rule2));
        mapping.setConstraintMapping(new HashMap<>(java.util.Map.of("NotNull", 40003)));
        props.setCodeMapping(mapping);
        Chain.setFailureProperties(props);

        Validator validator = mock(Validator.class);
        Chain.setValidator(validator);

        NotNull notNull = getNotNullAnnotation(BeanA.class, "field");
        ConstraintViolation<?> v1 = violation("field", BeanA.class, notNull, "m1");
        ConstraintViolation<?> v2 = violation("other", BeanA.class, notNull, "m2");
        ConstraintViolation<?> v3 = violation("another", BeanB.class, notNull, "m3");

        @SuppressWarnings({"rawtypes", "unchecked"})
        Set raw = Set.of(v1, v2, v3);
        when(validator.validate(org.mockito.ArgumentMatchers.any())).thenReturn(raw);

        Chain chain = Chain.begin(false);
        chain.jsr(new BeanA()).validate();

        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses()).hasSize(3);
        assertThat(chain.getCauses().get(0).getResponseCode().getCode()).isIn(40001, 40002, 40003);
    }

    @Test
    void jsrProcessViolationsCoversNullMappingsBranches() throws Exception {
        FailureProperties props = new FailureProperties();
        FailureProperties.CodeMapping mapping = new FailureProperties.CodeMapping();
        mapping.setConstraintPathMapping(null);
        mapping.setConstraintBeanMapping(null);
        mapping.setConstraintMapping(null);
        props.setCodeMapping(mapping);
        Chain.setFailureProperties(props);

        Validator validator = mock(Validator.class);
        Chain.setValidator(validator);

        NotNull notNull = getNotNullAnnotation(BeanA.class, "field");
        ConstraintViolation<?> v1 = violation("field", BeanA.class, notNull, "m1");

        @SuppressWarnings({"rawtypes", "unchecked"})
        Set raw = Set.of(v1);
        when(validator.validate(org.mockito.ArgumentMatchers.any())).thenReturn(raw);

        Chain chain = Chain.begin(false);
        chain.jsr(new BeanA()).validate();

        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses()).hasSize(1);
        assertThat(chain.getCauses().get(0).getResponseCode().getCode()).isEqualTo(400);
    }

    @Test
    void jsrProcessViolationsCoversNullMappingObjectBranch() throws Exception {
        FailureProperties props = new FailureProperties();
        props.setCodeMapping(null);
        Chain.setFailureProperties(props);

        Validator validator = mock(Validator.class);
        Chain.setValidator(validator);

        NotNull notNull = getNotNullAnnotation(BeanA.class, "field");
        ConstraintViolation<?> v1 = violation("field", BeanA.class, notNull, "m1");

        @SuppressWarnings({"rawtypes", "unchecked"})
        Set raw = Set.of(v1);
        when(validator.validate(org.mockito.ArgumentMatchers.any())).thenReturn(raw);

        Chain chain = Chain.begin(false);
        chain.jsr(new BeanA()).validate();
        assertThat(chain.getCauses()).hasSize(1);
    }

    private static NotNull getNotNullAnnotation(Class<?> bean, String fieldName) throws Exception {
        Field f = bean.getDeclaredField(fieldName);
        return f.getAnnotation(NotNull.class);
    }

    private static ConstraintViolation<?> violation(String path, Class<?> rootBeanClass, NotNull annotation, String message) {
        @SuppressWarnings("rawtypes")
        ConstraintViolation v = mock(ConstraintViolation.class);
        Path p = mock(Path.class);
        when(p.toString()).thenReturn(path);
        when(v.getPropertyPath()).thenReturn(p);
        when(v.getMessage()).thenReturn(message);
        when(v.getInvalidValue()).thenReturn(null);
        when(v.getRootBeanClass()).thenReturn((Class) rootBeanClass);

        @SuppressWarnings("rawtypes")
        ConstraintDescriptor d = mock(ConstraintDescriptor.class);
        when(d.getAnnotation()).thenReturn(annotation);
        when(v.getConstraintDescriptor()).thenReturn(d);

        return v;
    }
}

