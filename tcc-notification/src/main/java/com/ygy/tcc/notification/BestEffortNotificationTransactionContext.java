package com.ygy.tcc.notification;

import com.ygy.tcc.notification.enums.BestEffortNotificationStatus;
import lombok.Data;

import java.io.Serializable;


@Data
public class BestEffortNotificationTransactionContext implements Serializable {

    private String notificationId;

    public BestEffortNotificationTransactionContext(String notificationId) {
        this.notificationId = notificationId;
    }

}
