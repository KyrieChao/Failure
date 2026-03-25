package com.chao.failfast.annotation;

import com.chao.failfast.constant.Scenario;
import java.lang.annotation.*;

/**
 * Validation annotation with scene and groups support.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Validate {
    /**
     * Validator classes.
     *
     * @return Validator classes
     */
    Class<? extends FastValidator>[] value() default {};

    /**
     * Whether to enable fail-fast mode.
     *
     * @return True: fail-fast mode, throw exception immediately on first error
     */
    boolean fast() default true;

    /**
     * Business scene classification.
     *
     * @return Scene enum array
     */
    Scenario[] scene() default {Scenario.DEFAULT};

    /**
     * Validation groups (JSR-303 groups).
     *
     * @return Groups classes
     */
    Class<?>[] groups() default {};
}
