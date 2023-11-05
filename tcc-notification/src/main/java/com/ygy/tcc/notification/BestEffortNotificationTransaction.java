package com.ygy.tcc.notification;

import lombok.Data;


@Data
public class BestEffortNotificationTransaction {

    private BestEffortNotificationStatus status;

    private String notificationId;

    private String resourceId;

    private int checkTimes;

    private int version;

    private long createTime;

    private long updateTime;

    private long lastCheckTime;

    private Object[] args;



}
