package com.ygy.tcc.notification;

import com.ygy.tcc.notification.enums.BestEffortNotificationStatus;
import lombok.Data;

import java.io.Serializable;


@Data
public class BestEffortNotificationTransactionContext implements Serializable {

    private String notificationId;

    private String resourceId;

    public BestEffortNotificationTransactionContext(String notificationId, String resourceId) {
        this.notificationId = notificationId;
        this.resourceId = resourceId;
    }

}
