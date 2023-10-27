package com.ygy.tcc.core.holder;


import com.google.common.collect.Maps;
import com.ygy.tcc.core.TccResource;
import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.enums.TccResourceType;
import com.ygy.tcc.core.enums.TccStatus;
import com.ygy.tcc.core.enums.TransactionRole;
import com.ygy.tcc.core.spring.SpringBeanContext;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;


public class TccHolder {

    public static final String TCC_TRANSACTION_FIELD = "tcc_transaction_field";

    private static SpringBeanContext context = null;

    private TccHolder() {
    }

    private static final Map<Class, SingleSpringBeanHolder> BEAN_HOLDER_MAP = Maps.newConcurrentMap();

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

    public static <T> T getHolderBean(Class<T> beanClass) {
        if (BEAN_HOLDER_MAP.containsKey(beanClass)) {
            SingleSpringBeanHolder<T> singleSpringBeanHolder = BEAN_HOLDER_MAP.get(beanClass);
            return singleSpringBeanHolder.getHolderBean();
        }
        T bean = context.getBean(beanClass);
        BEAN_HOLDER_MAP.putIfAbsent(beanClass, new SingleSpringBeanHolder<>(beanClass, bean));
        return bean;
    }

    public static void holderSpringBeanContext(SpringBeanContext springBeanContext) {
        context = springBeanContext;
    }

    public static class SingleSpringBeanHolder<T>{

        private volatile T holderBean;

        private Class<T> beanClass;

        public SingleSpringBeanHolder(Class<T> beanClass, T holderBean) {
            this.beanClass = beanClass;
            this.holderBean = holderBean;
        }

        public T getHolderBean() {
            return holderBean;
        }
    }
}
