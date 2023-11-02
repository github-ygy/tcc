package com.ygy.tcc.core.repository;

import com.ygy.tcc.core.recovery.TccTransactionDoQueryResult;

public interface TccTransactionMapper {


    void create(TccTransactionDO toTccTransactionDO);

    void update(TccTransactionDO toTccTransactionDO);

    TccTransactionDoQueryResult loadNeedRecoverTransactions(String tccAppId, String cursor);
}
