package com.ygy.tcc.notification.annotation;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface BestEffortNotification {

    String resourceId() default "";

    boolean afterCommitSynchronization() default false;

    String checkMethod()  default "checkMethod";

}
