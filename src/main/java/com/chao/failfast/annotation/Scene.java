package com.chao.failfast.annotation;

import com.chao.failfast.constant.Scenario;

import java.lang.annotation.*;

/**
 * Scene annotation for enum grouping.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scene {

    /**
     * Scene name.
     *
     * @return Scene name
     */
    Scenario[] value() default Scenario.DEFAULT;
}