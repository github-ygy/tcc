package com.ygy.tcc.core.aop.annotation;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TccMethod {

    String resourceId() default "";

    String commitMethod() default "commit";

    String rollBackMethod() default "rollBack";


}
