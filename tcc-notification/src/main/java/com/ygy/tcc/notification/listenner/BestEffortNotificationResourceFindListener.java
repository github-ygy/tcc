package com.ygy.tcc.notification.listenner;


import com.ygy.tcc.core.aop.annotation.Local;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.logger.TccLogger;
import com.ygy.tcc.core.util.TccUtil;
import com.ygy.tcc.notification.result.BestEffortNotificationDoneResult;
import com.ygy.tcc.notification.BestEffortNotificationResource;
import com.ygy.tcc.notification.annotation.BestEffortNotification;
import com.ygy.tcc.notification.enums.BestEffortNotificationDoneStatus;
import com.ygy.tcc.notification.holder.BestEffortNotificationHolder;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class BestEffortNotificationResourceFindListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();

        TccLogger.info("TccResourceFindListener onApplicationEvent start");

        findLocalBestEffortNotificationMethodBean(context);
    }

    private void findLocalBestEffortNotificationMethodBean(ApplicationContext context) {
        String[] beanNamesForType = context.getBeanNamesForType(Object.class);
        if (ArrayUtils.isEmpty(beanNamesForType)) {
            return;
        }
        for (String beanName : beanNamesForType) {
            Object bean = context.getBean(beanName);
            addBestEffortNotificationResourceFromLocalSpringBean(bean);
        }
    }

    private void addBestEffortNotificationResourceFromLocalSpringBean(Object bean) {
        Object targetBean = null;
        try {
            targetBean = getSpringTargetBean(bean);
        } catch (Exception exception) {
            TccLogger.error("getSpringTargetBean error", exception);
            return;
        }
        Class<?> beanClass = targetBean.getClass();
        Local localTcc = beanClass.getAnnotation(Local.class);
        if (localTcc == null) {
            return;
        }
        TccHolder.holdLocalClass(beanClass);
        for (Method method : beanClass.getMethods()) {
            BestEffortNotification annotation = method.getAnnotation(BestEffortNotification.class);
            if (annotation == null) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            BestEffortNotificationResource resource = new BestEffortNotificationResource();
            resource.setResourceId(TccUtil.getResourceId(annotation.resourceId(), beanClass, method));
            resource.setParameterTypes(parameterTypes);
            resource.setTargetClass(beanClass);
            if (annotation.maxCheckTimes() < 0 || annotation.delayCheckSpanSeconds() < 0) {
                TccLogger.error("maxCheckTimes or delayCheckSpanSeconds must be > 0 :" + resource.getResourceId());
                continue;
            }
            resource.setMaxCheckTimes(annotation.maxCheckTimes());
            resource.setDelayCheckSpanSeconds(annotation.delayCheckSpanSeconds());
            resource.setTargetBean(bean);
            if (annotation.customNotificationMethod()) {
                try {
                    Method notificationMethod = beanClass.getMethod(annotation.notificationMethod(), parameterTypes);
                    resource.setNotificationMethod(notificationMethod);
                } catch (Exception exception) {
                    TccLogger.error("not find notificationMethod:" + annotation.notificationMethod(), exception);
                    continue;
                }
            }else {
                resource.setNotificationMethod(method);
            }
            try {
                Method checkMethod = beanClass.getMethod(annotation.checkMethod(), parameterTypes);
                BestEffortNotification checkMethodAnnotation = checkMethod.getAnnotation(BestEffortNotification.class);
                if (checkMethodAnnotation != null) {
                    TccLogger.error("checkMethod:" + annotation.checkMethod() + " is also a notification method,ignore");
                    continue;
                }
                Class<?> returnType = checkMethod.getReturnType();
                if (returnType != BestEffortNotificationDoneResult.class && returnType != BestEffortNotificationDoneStatus.class) {
                    TccLogger.error("checkMethod:" + annotation.checkMethod() + " return type is not  BestEffortNotificationDoneResult or BestEffortNotificationDoneStatus,ignore");
                    continue;
                }
                resource.setCheckMethod(checkMethod);
            } catch (Exception exception) {
                TccLogger.error("not find checkMethod:" + annotation.checkMethod(), exception);
                continue;
            }
            TccLogger.info("add bestEffortNotification resource:" + resource.getResourceId());
            BestEffortNotificationResource existResource = BestEffortNotificationHolder.getResource(resource.getResourceId());
            if (existResource != null) {
                TccLogger.error("exist bestEffortNotification resource:" + resource.getResourceId());
                continue;
            }
            TccLogger.info("add local bestEffortNotification resource:" + resource.getResourceId());
            BestEffortNotificationHolder.addResource(resource);
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
