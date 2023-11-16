package com.ygy.tcc.notification;

import com.ygy.tcc.notification.enums.BestEffortNotificationStatus;
import lombok.Data;

import java.io.Serializable;


@Data
public class BestEffortNotificationTransaction implements Serializable {

    private String notificationId;

    private String resourceId;

    private int checkTimes;

    private int version;

    private long createTime;

    private long updateTime;

    private long nextCheckTime;

    private boolean retryWhenCheckFailed;

    private Object[] args;

    private BestEffortNotificationStatus status;

    private BestEffortNotificationStatus preStatus;

    private String remark;

}
