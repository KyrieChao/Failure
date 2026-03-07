package com.chao.failfast.annotation;

import java.lang.annotation.*;

/**
 * Indicates that this method/class/field needs refactoring or optimization.
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface ToImprove {

    /**
     * Reason for refactoring or plan.
     */
    String value() default "";

    /**
     * Estimated completion version.
     */
    String inVersion() default "";

    /**
     * Related Issue / PR link.
     */
    String issue() default "";
}
