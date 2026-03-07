package com.chao.failfast.annotation;

import java.lang.annotation.*;

/**
 * To be implemented: Annotation to explicitly mark parameters to skip validation.
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SkipValidation {
}
