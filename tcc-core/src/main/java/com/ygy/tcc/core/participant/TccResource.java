package com.ygy.tcc.core.participant;

import com.ygy.tcc.core.enums.TccResourceType;
import lombok.Data;

import java.lang.reflect.Method;


@Data
public class TccResource {

    private String resourceId;

    private TccResourceType resourceType;

    private Class<?> targetClass;

    private String confirmMethodName;

    private String rollbackMethodName;

    private Method confirmMethod;

    private Method rollbackMethod;

    private Class<?>[] parameterTypes;

    private Object targetBean;

}
