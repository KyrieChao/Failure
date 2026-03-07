package com.chao.failfast.annotation;

import java.lang.annotation.*;

/**
 * Validation annotation.
 *
 * @author Kyrie Chao
 * @version 1.0.0
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
     * @return True: fail-fast mode, do not throw exception immediately
     */
    boolean fast() default true;
}
