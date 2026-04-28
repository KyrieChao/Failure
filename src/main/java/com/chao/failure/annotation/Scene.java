package com.chao.failure.annotation;

import com.chao.failure.constant.Scenario;

import java.lang.annotation.*;

/**
 * Scene annotation for enum grouping.
 *
 * @author Kyrie Chao
 * @version 1.3.0
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