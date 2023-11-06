package com.ygy.tcc.notification.repository;

import com.google.common.collect.Lists;
import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.recovery.TccRecoveryLoadResult;
import com.ygy.tcc.core.recovery.TccTransactionDoQueryResult;
import com.ygy.tcc.core.repository.TccTransactionDO;
import com.ygy.tcc.core.repository.TccTransactionMapper;
import com.ygy.tcc.core.util.TccUtil;
import com.ygy.tcc.notification.BestEffortNotificationTransaction;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;


public class BestEffortNotificationTransactionRepository {

    private BestEffortNotificationTransactionMapper bestEffortNotificationTransactionMapper;

    public BestEffortNotificationTransactionRepository(BestEffortNotificationTransactionMapper bestEffortNotificationTransactionMapper) {
        this.bestEffortNotificationTransactionMapper = bestEffortNotificationTransactionMapper;
    }


    public void create(BestEffortNotificationTransaction transaction) {
        transaction.setVersion(1);
        long now = System.currentTimeMillis();
        transaction.setCreateTime(now);
        transaction.setUpdateTime(now);
        bestEffortNotificationTransactionMapper.create(transaction);
    }

    public void update(BestEffortNotificationTransaction transaction) {
        transaction.setVersion(transaction.getVersion() + 1);
        transaction.setUpdateTime(System.currentTimeMillis());
        bestEffortNotificationTransactionMapper.update(transaction);
    }

}
