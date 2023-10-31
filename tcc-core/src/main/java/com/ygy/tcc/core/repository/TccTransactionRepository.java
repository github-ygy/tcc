package com.ygy.tcc.core.repository;

import com.google.common.collect.Lists;
import com.ygy.tcc.core.participant.TccParticipant;
import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.util.GsonUtil;
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
        tccTransactionMapper.create(toTccTransactionDO(transaction));
    }

    private TccTransactionDO toTccTransactionDO(TccTransaction transaction) {
        TccTransactionDO transactionDO = new TccTransactionDO();
        transactionDO.setParentTccAppId(transaction.getParentTccAppId());
        transactionDO.setParentTccId(transaction.getParentTccId());
        transactionDO.setParentParticipantId(transaction.getParentParticipantId());
        transactionDO.setTccId(transaction.getTccId());
        transactionDO.setTccStatus(transaction.getStatus().name());
        transactionDO.setTccAppId(transaction.getTccAppId());
        transactionDO.setVersion(transaction.getVersion());
        transactionDO.setCreateTime(transaction.getCreateTime());
        transactionDO.setUpdateTime(transaction.getUpdateTime());
        List<TccParticipant> participants = transaction.getParticipants();
        if (CollectionUtils.isNotEmpty(participants)) {
            List<TccParticipantDO> participantDOS = Lists.newArrayList();
            for (TccParticipant tccParticipant : participants) {
                TccParticipantDO participantDO = new TccParticipantDO();
                participantDO.setTccId(tccParticipant.getTccId());
                participantDO.setParticipantId(tccParticipant.getParticipantId());
                participantDO.setStatus(tccParticipant.getStatus().name());
                participantDO.setResourceId(tccParticipant.getResource().getResourceId());
                participantDO.setResourceType(tccParticipant.getResource().getResourceType().name());
                participantDO.setArgs(tccParticipant.getArgs());
                participantDO.setRetryTimes(tccParticipant.getRetryTimes());
                participantDOS.add(participantDO);
            }
            transactionDO.setParticipantsJson(GsonUtil.toJson(participantDOS));
        }
        return transactionDO;
    }

    public void update(TccTransaction transaction) {
        transaction.setVersion(transaction.getVersion() + 1);
        transaction.setUpdateTime(System.currentTimeMillis());
        tccTransactionMapper.update(toTccTransactionDO(transaction));

    }
}
