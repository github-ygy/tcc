package com.ygy.tcc.core.listener;


import com.ygy.tcc.annotation.TccMethod;
import com.ygy.tcc.core.aop.annotation.LocalTcc;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.logger.TccLogger;
import com.ygy.tcc.core.participant.TccResource;
import com.ygy.tcc.core.enums.TccResourceType;
import com.ygy.tcc.core.util.ResourceUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class LocalTccResourceFindListener implements ApplicationListener<ContextRefreshedEvent> {


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();

        TccLogger.info("TccResourceFindListener onApplicationEvent start");

        findLocalTccMethodBean(context);
    }

    private void findLocalTccMethodBean(ApplicationContext context) {
        String[] beanNamesForType = context.getBeanNamesForType(Object.class);
        if (ArrayUtils.isEmpty(beanNamesForType)) {
            return;
        }
        for (String beanName : beanNamesForType) {
            Object bean = context.getBean(beanName);
            addTccResourceFromLocalSpringBean(bean);
        }

    }

    private void addTccResourceFromLocalSpringBean(Object bean) {
        Object targetBean = null;
        try {
            targetBean = getSpringTargetBean(bean);
        } catch (Exception exception) {
            TccLogger.error("getSpringTargetBean error", exception);
            return;
        }
        Class<?> beanClass = targetBean.getClass();
        LocalTcc localTcc = beanClass.getAnnotation(LocalTcc.class);
        if (localTcc == null) {
            return;
        }
        TccHolder.holdLocalClass(beanClass);
        for (Method method : beanClass.getMethods()) {
            TccMethod annotation = method.getAnnotation(TccMethod.class);
            if (annotation == null) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            TccResource resource = new TccResource();
            resource.setResourceId(ResourceUtil.getResourceId(annotation,beanClass,method));
            resource.setResourceType(TccResourceType.LOCAL);
            resource.setParameterTypes(parameterTypes);
            resource.setTargetClass(beanClass);
            resource.setConfirmMethodName(annotation.commitMethod());
            resource.setRollbackMethodName(annotation.rollBackMethod());
            resource.setTargetBean(bean);
            try {
                Method confirmMethod = beanClass.getMethod(annotation.commitMethod(), parameterTypes);
                resource.setConfirmMethod(confirmMethod);
            } catch (Exception exception) {
                TccLogger.error("not find confirmMethod:" + annotation.commitMethod(), exception);
                continue;
            }
            try {
                Method rollbackMethod = beanClass.getMethod(annotation.rollBackMethod(), parameterTypes);
                resource.setRollbackMethod(rollbackMethod);
            } catch (Exception exception) {
                TccLogger.error("not find rollbackMethod:" + annotation.rollBackMethod(), exception);
                continue;
            }
            TccLogger.info("add local resource:" + resource.getResourceId());
            TccHolder.addTccResource(resource);
        }
    }

    public static Object getSpringTargetBean(Object proxy) throws Exception {
        if (!AopUtils.isAopProxy(proxy)) {
            return proxy;
        }
        if (AopUtils.isCglibProxy(proxy)) {
            Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
            h.setAccessible(true);
            Object dynamicAdvisedInterceptor = h.get(proxy);
            Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
            advised.setAccessible(true);
            Object target = ((org.springframework.aop.framework.AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
            return target;
        }
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);
        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        Object target = ((org.springframework.aop.framework.AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
        return target;
    }


}
