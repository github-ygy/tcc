package com.ygy.tcc.core.util;

import com.google.common.collect.Lists;
import com.ygy.tcc.annotation.TccMethod;
import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.enums.TccResourceType;
import com.ygy.tcc.core.enums.TccStatus;
import com.ygy.tcc.core.enums.TransactionRole;
import com.ygy.tcc.core.exception.TccException;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.participant.TccParticipant;
import com.ygy.tcc.core.participant.TccResource;
import com.ygy.tcc.core.repository.TccParticipantDO;
import com.ygy.tcc.core.repository.TccTransactionDO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.List;


public class TccUtil {

    public static String getResourceId(TccMethod annotation, Class<?> interfaceClass, Method method) {
        return StringUtils.isEmpty(annotation.resourceId()) ? interfaceClass.getName() + "#" + method.getName() : annotation.resourceId();
    }

    public static TccTransactionDO toTccTransactionDO(TccTransaction transaction) {
        TccTransactionDO transactionDO = new TccTransactionDO();
        transactionDO.setParentTccAppId(transaction.getParentTccAppId());
        transactionDO.setParentTccId(transaction.getParentTccId());
        transactionDO.setParentParticipantId(transaction.getParentParticipantId());
        transactionDO.setTccId(transaction.getTccId());
        transactionDO.setTccStatus(transaction.getStatus().name());
        transactionDO.setTccRole(transaction.getRole().name());
        transactionDO.setTccAppId(transaction.getTccAppId());
        transactionDO.setVersion(transaction.getVersion());
        transactionDO.setCreateTime(transaction.getCreateTime());
        transactionDO.setUpdateTime(transaction.getUpdateTime());
        transactionDO.setRecoveryTimes(transaction.getRecoveryTimes());
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

    public static TccTransaction toTccTransaction(TccTransactionDO tccTransactionDO) {
        TccTransaction transaction = new TccTransaction(TransactionRole.valueOf(tccTransactionDO.getTccRole()));
        transaction.setParentTccAppId(tccTransactionDO.getParentTccAppId());
        transaction.setParentTccId(tccTransactionDO.getParentTccId());
        transaction.setParentParticipantId(tccTransactionDO.getParentParticipantId());
        transaction.setTccId(tccTransactionDO.getTccId());
        transaction.setStatus(TccStatus.valueOf(tccTransactionDO.getTccStatus()));
        transaction.setTccAppId(tccTransactionDO.getTccAppId());
        transaction.setVersion(tccTransactionDO.getVersion());
        transaction.setCreateTime(tccTransactionDO.getCreateTime());
        transaction.setUpdateTime(tccTransactionDO.getUpdateTime());
        transaction.setRecoveryTimes(tccTransactionDO.getRecoveryTimes());
        transaction.setParticipants(Lists.newArrayList());
        if (StringUtils.isEmpty(tccTransactionDO.getParticipantsJson())) {
            return transaction;
        }
        List<TccParticipantDO> participantDos = GsonUtil.parseList(tccTransactionDO.getParticipantsJson(), TccParticipantDO.class);
        if (CollectionUtils.isNotEmpty(participantDos)) {
            List<TccParticipant> participants = Lists.newArrayList();
            for (TccParticipantDO participantDO : participantDos) {
                TccParticipant participant = new TccParticipant();
                participant.setTccId(participantDO.getTccId());
                participant.setParticipantId(participantDO.getParticipantId());
                participant.setStatus(TccParticipantStatus.valueOf(participantDO.getStatus()));
                TccResource tccResource = TccHolder.getTccResource(participantDO.getResourceId(), TccResourceType.valueOf(participantDO.getResourceType()));
                if (tccResource == null) {
                    throw new TccException("tccResource is null");
                }
                participant.setResource(tccResource);
                participant.setArgs(participantDO.getArgs());
                participant.setRetryTimes(participantDO.getRetryTimes());
                participants.add(participant);
            }
            transaction.setParticipants(participants);
        }
        return transaction;
    }
}
