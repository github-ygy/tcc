package com.ygy.tcc.notification.repository;

import com.ygy.tcc.core.recovery.TccTransactionDoQueryResult;
import com.ygy.tcc.core.repository.TccTransactionDO;

public interface BestEffortNotificationTransactionMapper {


    void create(TccTransactionDO toTccTransactionDO);

    void update(TccTransactionDO toTccTransactionDO);

    TccTransactionDoQueryResult loadNeedRecoverTransactions(String tccAppId, String cursor);
}
