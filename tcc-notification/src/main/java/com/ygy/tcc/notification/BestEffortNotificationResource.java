package com.ygy.tcc.notification;

import com.ygy.tcc.core.enums.TccResourceType;
import lombok.Data;

import java.lang.reflect.Method;


@Data
public class BestEffortNotificationResource {

    private String resourceId;

    private Class<?> targetClass;

    private Method notificationMethod;

    private Method checkMethod;

    private Class<?>[] parameterTypes;

    private Object targetBean;

    private long delayCheckSpanMillis;

    private int maxCheckTimes;

}
