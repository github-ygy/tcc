package com.ygy.tcc.core.util;

import com.ygy.tcc.core.aop.annotation.TccMethod;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;


public class ResourceUtil {

    public static String getResourceId(TccMethod annotation, Class<?> interfaceClass, Method method) {
        return StringUtils.isEmpty(annotation.resourceId()) ? interfaceClass.getName() + "#" + method.getName() : annotation.resourceId();

    }
}
