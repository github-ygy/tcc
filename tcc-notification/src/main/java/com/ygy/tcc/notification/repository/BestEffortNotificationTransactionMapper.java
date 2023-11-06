package com.ygy.tcc.notification.repository;

import com.ygy.tcc.core.recovery.TccTransactionDoQueryResult;
import com.ygy.tcc.core.repository.TccTransactionDO;
import com.ygy.tcc.notification.BestEffortNotificationTransaction;

public interface BestEffortNotificationTransactionMapper {


    void create(BestEffortNotificationTransaction bestEffortNotificationTransaction);

    void update(BestEffortNotificationTransaction bestEffortNotificationTransaction);

}
