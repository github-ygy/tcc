package com.ygy.tcc.notification;


import com.ygy.tcc.core.logger.TccLogger;
import com.ygy.tcc.core.util.TimeUtil;
import com.ygy.tcc.notification.delay.BestEffortNotificationDelayTaskJob;
import com.ygy.tcc.notification.enums.BestEffortNotificationDoneStatus;
import com.ygy.tcc.notification.enums.BestEffortNotificationStatus;
import com.ygy.tcc.notification.exception.BestEffortNotificationException;
import com.ygy.tcc.notification.generator.IdGenerator;
import com.ygy.tcc.notification.holder.BestEffortNotificationHolder;
import com.ygy.tcc.notification.repository.BestEffortNotificationTransactionRepository;
import com.ygy.tcc.notification.result.BestEffortNotificationDoneResult;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.annotation.Resource;
import java.util.Objects;


public class BestEffortNotificationTransactionManager {


    private IdGenerator idGenerator;

    private BestEffortNotificationDelayTaskJob bestEffortNotificationDelayTaskJob;

    public BestEffortNotificationTransactionManager(IdGenerator idGenerator, BestEffortNotificationDelayTaskJob bestEffortNotificationDelayTaskJob) {
        this.idGenerator = idGenerator;
        this.bestEffortNotificationDelayTaskJob = bestEffortNotificationDelayTaskJob;
    }

    @Resource
    private BestEffortNotificationTransactionRepository bestEffortNotificationTransactionRepository;


    public BestEffortNotificationTransaction newTransaction(String resourceId, Object... args) {
        BestEffortNotificationResource resource = BestEffortNotificationHolder.getResource(resourceId);
        if (resource == null) {
            throw new BestEffortNotificationException("resource not found:" + resourceId);
        }
        BestEffortNotificationTransaction bestEffortNotificationTransaction = new BestEffortNotificationTransaction();
        bestEffortNotificationTransaction.setResourceId(resourceId);
        bestEffortNotificationTransaction.setStatus(BestEffortNotificationStatus.PREPARE);
        bestEffortNotificationTransaction.setArgs(args);
        bestEffortNotificationTransaction.setNotificationId(generateNotificationId(resourceId, args));
        bestEffortNotificationTransactionRepository.create(bestEffortNotificationTransaction);
        return bestEffortNotificationTransaction;
    }


    public String generateNotificationId(String resourceId, Object... args) {
        return idGenerator.generateNotificationId(resourceId, args);
    }

    public void checkMethod(BestEffortNotificationTransaction transaction) {
        if (transaction.getStatus() == BestEffortNotificationStatus.PREPARE) {
            BestEffortNotificationResource resource = BestEffortNotificationHolder.getResource(transaction.getResourceId());
            try {
                Object invoke = null;
                BestEffortNotificationTransactionContext suspendTransactionContext = BestEffortNotificationHolder.getTransactionContext();
                try {
                    BestEffortNotificationHolder.bindTransactionContext(new BestEffortNotificationTransactionContext(transaction.getNotificationId()));
                    invoke = resource.getCheckMethod().invoke(resource.getTargetBean(), transaction.getArgs());
                } finally {
                    BestEffortNotificationTransactionContext existTransactionContext = BestEffortNotificationHolder.getTransactionContext();
                    if (Objects.equals(existTransactionContext.getNotificationId(), transaction.getNotificationId())) {
                        BestEffortNotificationHolder.clearTransactionContext();
                    }
                    if (suspendTransactionContext != null) {
                        BestEffortNotificationHolder.bindTransactionContext(suspendTransactionContext);
                    }
                }
                checkResult(invoke, transaction);
            } catch (Exception exception) {
                TccLogger.error("check method execute fail", exception);
            }
            if (isDone(transaction.getStatus())) {
                bestEffortNotificationTransactionRepository.update(transaction);
            } else {
                addDelayCheckTask(transaction);
            }
        }
    }

    private boolean isDone(BestEffortNotificationStatus status) {
        return Objects.equals(status, BestEffortNotificationStatus.SUCCESS) || Objects.equals(status, BestEffortNotificationStatus.CANCEL);
    }

    public void addDelayCheckTask(BestEffortNotificationTransaction transaction) {
        long now = TimeUtil.getCurrentTime();
        long nextDelaySpanSeconds = -1;
        if (transaction.getNextCheckTime() > now) {
            return;
        }
        BestEffortNotificationResource resource = BestEffortNotificationHolder.getResource(transaction.getResourceId());
        if (resource.getMaxCheckTimes() > transaction.getCheckTimes()) {
            transaction.setCheckTimes(transaction.getCheckTimes() + 1);
            nextDelaySpanSeconds = resource.getDelayCheckSpanSeconds();
        }
        if (nextDelaySpanSeconds > 0) {
            try {
                transaction.setNextCheckTime(TimeUtil.getCurrentTime() + nextDelaySpanSeconds * 1000);
                bestEffortNotificationDelayTaskJob.delayCheck(transaction, nextDelaySpanSeconds);
            } catch (Exception exception) {
                TccLogger.warn("add delay check task fail", exception);
            }
        } else {
            transaction.setStatus(BestEffortNotificationStatus.CANCEL);
            transaction.setRemark("delay time end");
        }
        bestEffortNotificationTransactionRepository.update(transaction);
    }


    public Object doProceed(BestEffortNotificationTransaction transaction, ProceedingJoinPoint jp) throws Throwable {
        Object proceed = null;
        BestEffortNotificationTransactionContext suspendTransactionContext = BestEffortNotificationHolder.getTransactionContext();
        try {
            BestEffortNotificationHolder.bindTransactionContext(new BestEffortNotificationTransactionContext(transaction.getNotificationId()));
            proceed = jp.proceed();
        } finally {
            BestEffortNotificationTransactionContext existTransactionContext = BestEffortNotificationHolder.getTransactionContext();
            if (Objects.equals(existTransactionContext.getNotificationId(), transaction.getNotificationId())) {
                BestEffortNotificationHolder.clearTransactionContext();
            }
            if (suspendTransactionContext != null) {
                BestEffortNotificationHolder.bindTransactionContext(suspendTransactionContext);
            }
        }

        if (proceed instanceof BestEffortNotificationDoneStatus || proceed instanceof BestEffortNotificationDoneResult) {
            checkResult(proceed, transaction);
            if (isDone(transaction.getStatus())) {
                bestEffortNotificationTransactionRepository.update(transaction);
                return proceed;
            }
        }
        this.addDelayCheckTask(transaction);
        return proceed;
    }

    private void checkResult(Object result, BestEffortNotificationTransaction transaction) {
        if (result instanceof BestEffortNotificationDoneStatus) {
            doAfterDoneStatus((BestEffortNotificationDoneStatus) result, transaction);
        } else if (result instanceof BestEffortNotificationDoneResult) {
            ((BestEffortNotificationDoneResult) result).setNotificationId(transaction.getNotificationId());
            BestEffortNotificationDoneStatus doneStatus = ((BestEffortNotificationDoneResult) result).getDoneStatus();
            doAfterDoneStatus(doneStatus, transaction);
        } else {
            throw new BestEffortNotificationException("check method return type error");
        }
    }

    private void doAfterDoneStatus(BestEffortNotificationDoneStatus doneStatus, BestEffortNotificationTransaction transaction) {
        if (doneStatus == null) {
            throw new BestEffortNotificationException("doneStatus is null");
        }
        switch (doneStatus) {
            case SUCCESS:
                transaction.setPreStatus(transaction.getStatus());
                transaction.setStatus(BestEffortNotificationStatus.SUCCESS);
                break;
            case CANCEL:
                transaction.setPreStatus(transaction.getStatus());
                transaction.setStatus(BestEffortNotificationStatus.CANCEL);
                transaction.setRemark("cancel by check method");
                break;
        }
    }

}
