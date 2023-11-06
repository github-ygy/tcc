package com.ygy.tcc.notification.holder;


import com.google.common.collect.Maps;
import com.ygy.tcc.core.enums.TccResourceType;
import com.ygy.tcc.core.participant.TccResource;
import com.ygy.tcc.notification.BestEffortNotificationResource;
import com.ygy.tcc.notification.BestEffortNotificationTransaction;
import com.ygy.tcc.notification.BestEffortNotificationTransactionContext;

import java.util.HashMap;
import java.util.Map;

public class BestEffortNotificationHolder {

    private BestEffortNotificationHolder() {
    }

    public static final String BEST_EFFORT_NOTIFICATION_TRANSACTION_CONTEXT_FIELD = "best_effort_notification_transaction_context_field";


    private static final Map<String, BestEffortNotificationResource> BEST_EFFORT_NOTIFICATION_RESOURCE_MAP = Maps.newConcurrentMap();

    private static final ThreadLocal<Map<String, Object>> CORE_DATA = ThreadLocal.withInitial(HashMap::new);

    public static BestEffortNotificationTransactionContext getTransactionContext() {
        return (BestEffortNotificationTransactionContext) CORE_DATA.get().get(BEST_EFFORT_NOTIFICATION_TRANSACTION_CONTEXT_FIELD);
    }

    public static void bindTransactionContext(BestEffortNotificationTransactionContext transactionContext) {
        CORE_DATA.get().put(BEST_EFFORT_NOTIFICATION_TRANSACTION_CONTEXT_FIELD, transactionContext);
    }

    public static void clearTransactionContext() {
        CORE_DATA.get().remove(BEST_EFFORT_NOTIFICATION_TRANSACTION_CONTEXT_FIELD);
    }

    public static BestEffortNotificationResource getResource(String resourceId) {
        return BEST_EFFORT_NOTIFICATION_RESOURCE_MAP.get(resourceId);
    }

    public static void addResource(BestEffortNotificationResource resource) {
        BEST_EFFORT_NOTIFICATION_RESOURCE_MAP.put(resource.getResourceId(), resource);
    }



}
