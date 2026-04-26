package com.chao.failfast.annotation;

import com.chao.failfast.constant.SensitivityLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Sensitive {
    SensitivityLevel level() default SensitivityLevel.MEDIUM;

    String maskedValue() default "***";
}