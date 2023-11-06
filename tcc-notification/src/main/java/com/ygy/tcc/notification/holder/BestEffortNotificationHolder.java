package com.ygy.tcc.notification.holder;


import com.google.common.collect.Maps;
import com.ygy.tcc.core.enums.TccResourceType;
import com.ygy.tcc.core.participant.TccResource;
import com.ygy.tcc.notification.BestEffortNotificationResource;

import java.util.Map;

public class BestEffortNotificationHolder {

    private BestEffortNotificationHolder() {
    }

    private static final Map<String, BestEffortNotificationResource> BEST_EFFORT_NOTIFICATION_RESOURCE_MAP = Maps.newConcurrentMap();


    public static BestEffortNotificationResource getResource(String resourceId) {
        return BEST_EFFORT_NOTIFICATION_RESOURCE_MAP.get(resourceId);
    }

    public static void addResource(BestEffortNotificationResource resource) {
        BEST_EFFORT_NOTIFICATION_RESOURCE_MAP.put(resource.getResourceId(), resource);
    }



}
