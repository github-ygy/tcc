package com.ygy.tcc.core.repository;

import com.google.common.collect.Lists;
import com.ygy.tcc.core.participant.TccParticipant;
import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.recovery.TccRecoveryLoadResult;
import com.ygy.tcc.core.recovery.TccTransactionDoQueryResult;
import com.ygy.tcc.core.util.GsonUtil;
import com.ygy.tcc.core.util.TccUtil;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;


public class TccTransactionRepository {

    private TccTransactionMapper tccTransactionMapper;

    public TccTransactionRepository(TccTransactionMapper tccTransactionMapper) {
        this.tccTransactionMapper = tccTransactionMapper;
    }


    public void create(TccTransaction transaction) {
        transaction.setVersion(1);
        long now = System.currentTimeMillis();
        transaction.setCreateTime(now);
        transaction.setUpdateTime(now);
        tccTransactionMapper.create(TccUtil.toTccTransactionDO(transaction));
    }

    public void update(TccTransaction transaction) {
        transaction.setVersion(transaction.getVersion() + 1);
        transaction.setUpdateTime(System.currentTimeMillis());
        tccTransactionMapper.update(TccUtil.toTccTransactionDO(transaction));
    }

    public TccRecoveryLoadResult loadNeedRecoverTransactions(String tccAppId, String cursor) {
        TccTransactionDoQueryResult tccTransactionDoQueryResult = tccTransactionMapper.loadNeedRecoverTransactions(tccAppId, cursor);
        if (tccTransactionDoQueryResult == null || CollectionUtils.isEmpty(tccTransactionDoQueryResult.getNeedRecoverTransactionDos())) {
            return new TccRecoveryLoadResult(Lists.newArrayList(), "");
        }
        List<TccTransaction> tccTransactions = Lists.newArrayList();
        for (TccTransactionDO tccTransactionDO : tccTransactionDoQueryResult.getNeedRecoverTransactionDos()) {
            tccTransactions.add(TccUtil.toTccTransaction(tccTransactionDO));
        }
        return new TccRecoveryLoadResult(tccTransactions, tccTransactionDoQueryResult.getNextCursor());
    }

}
