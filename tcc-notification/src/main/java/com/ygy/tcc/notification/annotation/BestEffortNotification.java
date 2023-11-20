package com.ygy.tcc.notification.annotation;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface BestEffortNotification {

    String resourceId() default "";

    boolean afterCommitSynchronization() default false;

    long delayCheckSpanSeconds() default 10;

    int maxCheckTimes() default 3;

    String checkMethod() default "checkMethod";

    boolean customNotificationMethod() default false;

    String notificationMethod() default "notificationMethod";

}
