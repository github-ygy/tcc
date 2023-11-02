package com.ygy.tcc.core;


import com.ygy.tcc.core.configration.TccConfigProps;
import com.ygy.tcc.core.configration.TccProperties;
import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.enums.TccStatus;
import com.ygy.tcc.core.enums.TransactionRole;
import com.ygy.tcc.core.exception.TccException;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.logger.TccLogger;
import com.ygy.tcc.core.participant.TccParticipant;
import com.ygy.tcc.core.participant.TccResource;
import com.ygy.tcc.core.repository.TccTransactionRepository;
import com.ygy.tcc.core.generator.UuidGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class TccTransactionManager {


    private UuidGenerator idGenerator;

    public TccTransactionManager(UuidGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Resource
    private TccTransactionRepository tccTransactionRepository;


    private int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;

    private int threadQueueSize = 1024;

    private ThreadPoolExecutor asyncPoolExecutor = new ThreadPoolExecutor(1, threadPoolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(threadQueueSize), new ThreadPoolExecutor.CallerRunsPolicy());


    public void begin(TccTransaction transaction) {
        if (transaction.getRole() != TransactionRole.Initiator) {
            return;
        }
        if (StringUtils.isNotEmpty(transaction.getTccId())) {
            throw new TccException("tccId must empty");
        }
        TccTransaction current = TccHolder.getTransaction();
        if (current != null) {
            throw new TccException("current tccTransaction must empty");
        }
        String tccId = generateTccId();
        transaction.setTccId(tccId);
        transaction.setStatus(TccStatus.TRYING);
        transaction.setTccAppId(TccProperties.getTccAppId());
        tccTransactionRepository.create(transaction);
        TccHolder.bindTransaction(transaction);
    }

    public void rollBack(boolean async) {
        TccTransaction transaction = TccHolder.getTransaction();
        if (transaction == null) {
            throw new TccException("no transaction");
        }
        if (transaction.getRole() != TransactionRole.Initiator) {
            return;
        }
        transaction.setStatus(TccStatus.ROLLBACK);
        tccTransactionRepository.update(transaction);
        if (async) {
            asyncPoolExecutor.submit(() -> {
                try {
                    TccHolder.bindTransaction(transaction);
                    rollbackTransaction(transaction);
                }finally {
                    TccHolder.clearTccTransaction();
                }
            });
        }else {
            rollbackTransaction(transaction);
        }

    }

    public void rollbackTransaction(TccTransaction transaction) {
        try {
            boolean isAllRollback = true;
            if (CollectionUtils.isNotEmpty(transaction.getParticipants())) {
                for (TccParticipant participant : transaction.getParticipants()) {
                    if (!participant.getStatus().equals(TccParticipantStatus.ROLLBACK_SUCCESS)) {
                        try {
                            participant.rollback(transaction);
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

    public void commit(boolean async) {
        TccTransaction transaction = TccHolder.getTransaction();
        if (transaction == null) {
            throw new TccException("no transaction");
        }
        if (transaction.getRole() != TransactionRole.Initiator) {
            return;
        }
        transaction.setStatus(TccStatus.CONFIRM);
        tccTransactionRepository.update(transaction);
        if (async) {
            asyncPoolExecutor.submit(() -> {
                try {
                    TccHolder.bindTransaction(transaction);
                    commitTransaction(transaction);
                }finally {
                    TccHolder.clearTccTransaction();
                }
            });
        }else {
            commitTransaction(transaction);
        }

    }

    public void commitTransaction(TccTransaction transaction) {
        try {
            boolean isAllConfirm = true;
            if (CollectionUtils.isNotEmpty(transaction.getParticipants())) {
                for (TccParticipant participant : transaction.getParticipants()) {
                    if (!participant.getStatus().equals(TccParticipantStatus.CONFIRM_SUCCESS)) {
                        try {
                            participant.commit(transaction);
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
        if (transaction != null) {
            if (transaction.getRole() != TransactionRole.Initiator) {
                return;
            }
            TccHolder.clearTccTransaction();
        }
    }

    public boolean addParticipant(TccTransaction transaction, TccParticipant tccParticipant) {
        if (transaction.getRole() != TransactionRole.Initiator) {
            return false;
        }
        if (transaction.getStatus() != TccStatus.TRYING) {
            return false;
        }
        List<TccParticipant> participants = transaction.getParticipants();
        if (CollectionUtils.isNotEmpty(participants)) {
            for (TccParticipant participant : participants) {
                TccResource resource = participant.getResource();
                if (Objects.equals(resource.getResourceId(), tccParticipant.getResource().getResourceId()) && Objects.equals(resource.getResourceType(), tccParticipant.getResource().getResourceType())) {
                    return false;
                }
            }
        }
        transaction.getParticipants().add(tccParticipant);
        tccTransactionRepository.update(transaction);
        return true;
    }

    public ThreadPoolExecutor getAsyncPoolExecutor() {
        return asyncPoolExecutor;
    }

    public String generateParticipantId() {
        return idGenerator.generateParticipantId();
    }

    private String generateTccId() {
        return idGenerator.generateTccId();
    }
}
