package com.ygy.tcc.apache.dubbo;


import com.ygy.tcc.annotation.TccMethod;
import com.ygy.tcc.core.participant.TccResource;
import com.ygy.tcc.core.enums.TccResourceType;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.logger.TccLogger;
import com.ygy.tcc.core.util.ResourceUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.dubbo.config.spring.ReferenceBean;
import org.apache.dubbo.config.spring.reference.ReferenceBeanManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Method;
import java.util.Collection;


public class ApacheDubboTccResourceFindListener implements ApplicationListener<ContextRefreshedEvent> {


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();

        TccLogger.info("AlibabaDubboTccResourceFindListener onApplicationEvent start");

        findDubboReferenceBean(context);
    }


    private void findDubboReferenceBean(ApplicationContext context) {
        ReferenceBeanManager manager = context.getBean(ReferenceBeanManager.class);
        Collection<ReferenceBean> references = manager.getReferences();
        if (CollectionUtils.isNotEmpty(references)) {
            for (ReferenceBean<?> referenceBean : references) {
                addTccResourceFromDubboInterface(referenceBean);
            }
        }else {
            String[] beanNamesForType = context.getBeanNamesForType(Object.class);
            if (ArrayUtils.isNotEmpty(beanNamesForType)) {
                for (String beanName : beanNamesForType) {
                    Object bean = context.getBean(beanName);
                    if (bean instanceof ReferenceBean) {
                        addTccResourceFromDubboInterface((ReferenceBean) bean);
                    }
                }
            }else {
                TccLogger.warn("find ReferenceBean empty");
            }
        }
    }

    private void addTccResourceFromDubboInterface(ReferenceBean<?> referenceBean) {
        Class<?> interfaceClass = referenceBean.getInterfaceClass();
        if (ArrayUtils.isEmpty(interfaceClass.getMethods())) {
            return;
        }
        Object targetBean = null;
        try {
            targetBean = referenceBean.getObject();
        } catch (Exception exception) {
            TccLogger.error("not find target bean:" + interfaceClass.getName(), exception);
            return;
        }
        for (Method method : interfaceClass.getMethods()) {
            TccMethod annotation = method.getAnnotation(TccMethod.class);
            if (annotation == null) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            TccResource resource = new TccResource();
            resource.setResourceId(ResourceUtil.getResourceId(annotation,interfaceClass,method));
            resource.setResourceType(TccResourceType.DUBBO_REFERENCE);
            resource.setParameterTypes(parameterTypes);
            resource.setTargetClass(interfaceClass);
            resource.setConfirmMethodName(annotation.commitMethod());
            resource.setRollbackMethodName(annotation.rollBackMethod());
            resource.setTargetBean(targetBean);
            try {
                Method confirmMethod = interfaceClass.getMethod(annotation.commitMethod(), parameterTypes);
                resource.setConfirmMethod(confirmMethod);
            } catch (Exception exception) {
                TccLogger.error("not find confirmMethod:" + annotation.commitMethod(), exception);
                continue;
            }
            try {
                Method rollbackMethod = interfaceClass.getMethod(annotation.rollBackMethod(), parameterTypes);
                resource.setRollbackMethod(rollbackMethod);
            } catch (Exception exception) {
                TccLogger.error("not find rollbackMethod:" + annotation.rollBackMethod(), exception);
                continue;
            }
            TccResource existResource = TccHolder.getTccResource(resource.getResourceId(), resource.getResourceType());
            if (existResource != null) {
                TccLogger.error("exist resource:" + resource.getResourceId());
                continue;
            }
            TccLogger.info("add apache dubbo resource:" + resource.getResourceId());
            TccHolder.addTccResource(resource);
        }
    }

}
