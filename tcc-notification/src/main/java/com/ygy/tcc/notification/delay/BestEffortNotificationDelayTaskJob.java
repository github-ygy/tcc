package com.ygy.tcc.notification.delay;


import com.ygy.tcc.notification.BestEffortNotificationTransaction;

public interface BestEffortNotificationDelayTaskJob {


    void delayCheck(BestEffortNotificationTransaction transaction, long delaySeconds);
}
