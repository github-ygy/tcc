package com.ygy.tcc.core;


import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.enums.TccStatus;
import com.ygy.tcc.core.enums.TransactionRole;
import com.ygy.tcc.core.exception.TccException;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.logger.TccLogger;
import com.ygy.tcc.core.repository.TccTransactionRepository;
import com.ygy.tcc.core.util.UuidGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;


public class TccTransactionManager {

    @Resource
    private TccTransactionRepository tccTransactionRepository;


    public void begin(TccTransaction transaction) {
        if (transaction.getRole() != TransactionRole.Initiator) {
            if (StringUtils.isEmpty(transaction.getTccId())) {
                throw new TccException("tccId is empty");
            }
            return;
        }
        if (StringUtils.isNotEmpty(transaction.getTccId())) {
            throw new TccException("tccId must empty");
        }
        TccTransaction current = TccHolder.getTransaction();
        if (current != null) {
            throw new TccException("current tccTransaction must empty");
        }
        String tccId = UuidGenerator.generateTccId();
        transaction.setTccId(tccId);
        transaction.setStatus(TccStatus.TRYING);
        tccTransactionRepository.create(transaction);
        TccHolder.bindTransaction(transaction);
    }

    public void rollBack() {
        TccTransaction transaction = TccHolder.getTransaction();
        if (transaction == null) {
            throw new TccException("no transaction");
        }
        if (transaction.getRole() != TransactionRole.Initiator) {
            return;
        }
        transaction.setStatus(TccStatus.ROLLBACK);
        tccTransactionRepository.update(transaction);
        rollbackTransaction(transaction);
    }

    private void rollbackTransaction(TccTransaction transaction) {
        try {
            boolean isAllRollback = true;
            if (CollectionUtils.isNotEmpty(transaction.getParticipants())) {
                for (TccParticipant participant : transaction.getParticipants()) {
                    if (!participant.getStatus().equals(TccParticipantStatus.ROLLBACK_SUCCESS)) {
                        try {
                            participant.rollback();
                            participant.setStatus(TccParticipantStatus.ROLLBACK_SUCCESS);
                        } catch (TccException exception) {
                            TccLogger.error("rollback fail", exception);
                            participant.setStatus(TccParticipantStatus.ROLLBACK_FAIL);
                            isAllRollback = false;
                            break;
                        }
                    }
                }
            }
            if (isAllRollback) {
                transaction.setStatus(TccStatus.ROLLBACK_SUCCESS);
            }else {
                transaction.setStatus(TccStatus.ROLLBACK_FAIL);
            }
            tccTransactionRepository.update(transaction);
        } catch (Throwable throwable) {
            TccLogger.error("commit fail", throwable);
            throw new TccException("commit fail", throwable);
        }
    }

    public void commit() {
        TccTransaction transaction = TccHolder.getTransaction();
        if (transaction == null) {
            throw new TccException("no transaction");
        }
        if (transaction.getRole() != TransactionRole.Initiator) {
            return;
        }
        transaction.setStatus(TccStatus.CONFIRM);
        tccTransactionRepository.update(transaction);
        commitTransaction(transaction);
    }

    private void commitTransaction(TccTransaction transaction) {
        try {
            boolean isAllConfirm = true;
            if (CollectionUtils.isNotEmpty(transaction.getParticipants())) {
                for (TccParticipant participant : transaction.getParticipants()) {
                    if (!participant.getStatus().equals(TccParticipantStatus.CONFIRM_SUCCESS)) {
                        try {
                            participant.commit();
                            participant.setStatus(TccParticipantStatus.CONFIRM_SUCCESS);
                        } catch (TccException tccException) {
                            TccLogger.error("commit fail", tccException);
                            participant.setStatus(TccParticipantStatus.CONFIRM_FAIL);
                            isAllConfirm = false;
                            break;
                        }
                    }
                }
            }
            if (isAllConfirm) {
                transaction.setStatus(TccStatus.CONFIRM_SUCCESS);
            } else {
                transaction.setStatus(TccStatus.CONFIRM_FAIL);
            }
            tccTransactionRepository.update(transaction);
        } catch (Throwable throwable) {
            TccLogger.error("commit fail", throwable);
            throw new TccException("commit fail", throwable);
        }
    }


    public void completion() {
        TccTransaction transaction = TccHolder.getTransaction();
        if (transaction != null && transaction.getRole() == TransactionRole.Initiator) {
            TccHolder.clearTccTransaction();
        }
    }

    public void addParticipant(TccParticipant tccParticipant) {
        TccTransaction transaction = TccHolder.getTransaction();
        if (transaction.getRole() != TransactionRole.Initiator) {
            return;
        }
        transaction.getParticipants().add(tccParticipant);
        tccTransactionRepository.update(transaction);
    }
}
