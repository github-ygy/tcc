package com.ygy.tcc.core.aop.annotation;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TccTransactional {

    boolean asyncCommit() default false;

    boolean asyncRollback() default false;



}
