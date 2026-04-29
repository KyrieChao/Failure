package com.chao.failure.annotation;

import java.lang.annotation.*;

/**
 * FailFast body binding annotation - only for binding, not for validation.
 * Semantically equivalent to @RequestBody.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ToImprove(value = "Newly added annotation, to be observed", version = "1.2.0", tag = "1.8.0")
public @interface FailFastBody {

    /**
     * Whether body is required.
     */
    boolean required() default true;
}
