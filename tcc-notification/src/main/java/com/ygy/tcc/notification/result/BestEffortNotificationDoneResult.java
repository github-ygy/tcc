package com.ygy.tcc.notification.result;


import com.ygy.tcc.notification.enums.BestEffortNotificationDoneStatus;
import lombok.Data;
import org.omg.CORBA.Object;

import java.io.Serializable;

@Data
public class BestEffortNotificationDoneResult implements Serializable {

    private BestEffortNotificationDoneStatus doneStatus;

    private Object customResult;

    private String notificationId;

}
