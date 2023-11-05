package com.ygy.tcc.notification;


import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.configration.TccProperties;
import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.enums.TccStatus;
import com.ygy.tcc.core.enums.TransactionRole;
import com.ygy.tcc.core.exception.TccException;
import com.ygy.tcc.core.generator.UuidGenerator;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.logger.TccLogger;
import com.ygy.tcc.core.participant.TccParticipant;
import com.ygy.tcc.core.participant.TccResource;
import com.ygy.tcc.core.repository.TccTransactionRepository;
import com.ygy.tcc.notification.repository.BestEffortNotificationTransactionRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class BestEffortNotificationTransactionManager {


    private UuidGenerator idGenerator;

    public BestEffortNotificationTransactionManager(UuidGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Resource
    private BestEffortNotificationTransactionRepository bestEffortNotificationTransactionRepository;


    public BestEffortNotificationTransaction newTransaction(String resourceId, Object... args) {
        BestEffortNotificationTransaction bestEffortNotificationTransaction = new BestEffortNotificationTransaction();
        bestEffortNotificationTransaction.setResourceId(resourceId);
        bestEffortNotificationTransaction.setStatus(BestEffortNotificationStatus.PREPARE);
        bestEffortNotificationTransaction.setArgs(args);
        return bestEffortNotificationTransaction;
    }


    public String generateNotificationId() {
        return idGenerator.generateParticipantId();
    }

    public void checkMethod(BestEffortNotificationTransaction transaction) {


    }
}
