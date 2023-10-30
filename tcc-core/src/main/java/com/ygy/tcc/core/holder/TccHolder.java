package com.ygy.tcc.core.holder;


import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ygy.tcc.core.participant.TccParticipantContext;
import com.ygy.tcc.core.participant.TccParticipantHook;
import com.ygy.tcc.core.participant.TccResource;
import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.enums.TccResourceType;
import com.ygy.tcc.core.spring.SpringBeanContext;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class TccHolder {

    public static final String TCC_TRANSACTION_FIELD = "tcc_transaction_field";
    public static final String TCC_PARTICIPANT_CONTEXT_FIELD = "tcc_participant_context_field";

    private static SpringBeanContext context = null;

    private TccHolder() {
    }

    private static final Map<Class, SingleSpringBeanHolder> BEAN_HOLDER_MAP = Maps.newConcurrentMap();

    private static final Set<Class<?>> LOCAL_TCC_BEAN_CLASS_SET = Sets.newConcurrentHashSet();

    private static final Map<TccResourceType, Map<String, TccResource>> TCC_RESOURCE_MAP = Maps.newConcurrentMap();

    private static final ThreadLocal<Map<String, Object>> CORE_DATA = ThreadLocal.withInitial(HashMap::new);


    public static void bindTransaction(TccTransaction transaction) {
        CORE_DATA.get().put(TCC_TRANSACTION_FIELD, transaction);
    }

    public static TccTransaction getTransaction() {
        Object obj = CORE_DATA.get().get(TCC_TRANSACTION_FIELD);
        if (obj == null) {
            return null;
        }
        return (TccTransaction) obj;
    }

    public static TccResource getTccResource(String resourceId, TccResourceType tccResourceType) {
        Map<String, TccResource> tccResourceMap = TCC_RESOURCE_MAP.get(tccResourceType);
        if (MapUtils.isNotEmpty(tccResourceMap)) {
            return tccResourceMap.get(resourceId);
        }
        return null;
    }

    public static void addTccResource(TccResource resource) {
        Map<String, TccResource> tccResourceMap = TCC_RESOURCE_MAP.computeIfAbsent(resource.getResourceType(), k -> Maps.newConcurrentMap());
        tccResourceMap.put(resource.getResourceId(), resource);
    }

    public static void clearTccTransaction() {
        CORE_DATA.get().remove(TCC_TRANSACTION_FIELD);
    }

    public static <T> T getHoldBean(Class<T> beanClass) {
        if (BEAN_HOLDER_MAP.containsKey(beanClass)) {
            SingleSpringBeanHolder<T> singleSpringBeanHolder = BEAN_HOLDER_MAP.get(beanClass);
            return singleSpringBeanHolder.getHoldBean();
        }
        T bean = context.getBean(beanClass);
        BEAN_HOLDER_MAP.putIfAbsent(beanClass, new SingleSpringBeanHolder<>(beanClass, bean));
        return bean;
    }

    public static void holderSpringBeanContext(SpringBeanContext springBeanContext) {
        context = springBeanContext;
    }

    public static void holdLocalClass(Class<?> beanClass) {
        LOCAL_TCC_BEAN_CLASS_SET.add(beanClass);
    }

    public static boolean checkIsLocalBean(Class<?> beanClass) {
        return LOCAL_TCC_BEAN_CLASS_SET.contains(beanClass);
    }

    public static void bindParticipantContext(TccParticipantContext tccParticipantContext) {
        CORE_DATA.get().put(TCC_PARTICIPANT_CONTEXT_FIELD, tccParticipantContext);
    }

    public static void clearParticipantContext() {
        CORE_DATA.get().remove(TCC_PARTICIPANT_CONTEXT_FIELD);
    }

    public static TccParticipantContext getParticipantContext() {
        Object obj = CORE_DATA.get().get(TCC_PARTICIPANT_CONTEXT_FIELD);
        if (obj == null) {
            return null;
        }
        return (TccParticipantContext) obj;
    }

    public static class SingleSpringBeanHolder<T>{

        private volatile T holdBean;

        private Class<T> beanClass;

        public SingleSpringBeanHolder(Class<T> beanClass, T holdBean) {
            this.beanClass = beanClass;
            this.holdBean = holdBean;
        }

        public T getHoldBean() {
            return holdBean;
        }
    }
}
