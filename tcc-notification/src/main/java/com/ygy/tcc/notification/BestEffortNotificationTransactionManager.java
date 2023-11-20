package com.ygy.tcc.notification;


import com.ygy.tcc.core.exception.TccException;
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
import org.apache.commons.lang3.BooleanUtils;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.Callable;


public class BestEffortNotificationTransactionManager {


    private IdGenerator idGenerator;

    private BestEffortNotificationDelayTaskJob bestEffortNotificationDelayTaskJob;

    public BestEffortNotificationTransactionManager(IdGenerator idGenerator, BestEffortNotificationDelayTaskJob bestEffortNotificationDelayTaskJob) {
        this.idGenerator = idGenerator;
        this.bestEffortNotificationDelayTaskJob = bestEffortNotificationDelayTaskJob;
    }

    @Resource
    private BestEffortNotificationTransactionRepository bestEffortNotificationTransactionRepository;


    public BestEffortNotificationTransaction newTransaction(String resourceId, Object[] args) {
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


    public String generateNotificationId(String resourceId, Object[] args) {
        return idGenerator.generateNotificationId(resourceId, args);
    }

    public void doCheckMethod(BestEffortNotificationTransaction transaction) {
        if (transaction.getStatus() == BestEffortNotificationStatus.PREPARE) {
            BestEffortNotificationResource resource = BestEffortNotificationHolder.getResource(transaction.getResourceId());
            Object invoke = null;
            try {
                invoke = doAroundTransactionContext(transaction, () -> {
                    return resource.getCheckMethod().invoke(resource.getTargetBean(), transaction.getArgs());
                });
            } catch (Exception exception) {
                TccLogger.error("check method execute fail", exception);
            }
            if (invoke != null) {
                BestEffortNotificationDoneStatus doneStatus = checkResult(invoke, transaction);
                if (isDone(transaction.getStatus())) {
                    bestEffortNotificationTransactionRepository.update(transaction);
                    return;
                }
                if (doneStatus == BestEffortNotificationDoneStatus.RETRY) {
                    try {
                        doRetryNotificationMethod(transaction);
                    } catch (Exception exception) {
                        TccLogger.warn("retry notification method fail", exception);
                    }
                    return;
                }
            }
            addDelayCheckTask(transaction);
        }
    }

    public void doRetryNotificationMethod(BestEffortNotificationTransaction transaction) {
        BestEffortNotificationResource resource = BestEffortNotificationHolder.getResource(transaction.getResourceId());
        Object invoke = null;
        try {
            invoke = doAroundTransactionContext(transaction, () -> {
                return resource.getNotificationMethod().invoke(resource.getTargetBean(), transaction.getArgs());
            });
        } catch (Exception exception) {
            TccLogger.error("retry notification method fail", exception);
        }
        doAfterNotification(transaction, invoke);
    }

    public Object doProceed(BestEffortNotificationTransaction transaction, ProceedingJoinPoint jp) throws Exception {
        Object proceed = null;
        try {
            proceed = doAroundTransactionContext(transaction, () -> {
                try {
                    return jp.proceed();
                } catch (Throwable throwable) {
                    throw new TccException("proceed error", throwable);
                }
            });
        } catch (Exception exception) {
            TccLogger.error("proceed fail", exception);
        }
        doAfterNotification(transaction, proceed);
        return proceed;
    }

    private void doAfterNotification(BestEffortNotificationTransaction transaction, Object proceed) {
        if (proceed instanceof BestEffortNotificationDoneStatus || proceed instanceof BestEffortNotificationDoneResult) {
            checkResult(proceed, transaction);
            if (isDone(transaction.getStatus())) {
                bestEffortNotificationTransactionRepository.update(transaction);
                return;
            }
        }
        this.addDelayCheckTask(transaction);
    }

    private boolean isDone(BestEffortNotificationStatus status) {
        return Objects.equals(status, BestEffortNotificationStatus.SUCCESS) || Objects.equals(status, BestEffortNotificationStatus.CANCEL);
    }

    public void addDelayCheckTask(BestEffortNotificationTransaction transaction) {
        long now = TimeUtil.getCurrentTime();
        long nextDelaySpanMillis = -1;
        if (transaction.getNextCheckTime() > now) {
            return;
        }
        BestEffortNotificationResource resource = BestEffortNotificationHolder.getResource(transaction.getResourceId());
        if (resource.getMaxCheckTimes() > transaction.getCheckTimes()) {
            transaction.setCheckTimes(transaction.getCheckTimes() + 1);
            nextDelaySpanMillis = resource.getDelayCheckSpanMillis();
        }
        if (nextDelaySpanMillis < 0) {
            transaction.setStatus(BestEffortNotificationStatus.CANCEL);
            transaction.setRemark("delay time end");
            bestEffortNotificationTransactionRepository.update(transaction);
            return;
        }
        try {
            transaction.setNextCheckTime(TimeUtil.getCurrentTime() + nextDelaySpanMillis);
            bestEffortNotificationTransactionRepository.update(transaction);
        } catch (Exception exception) {
            TccLogger.warn("add delay check task fail", exception);
        } finally {
            bestEffortNotificationDelayTaskJob.delayCheck(transaction, nextDelaySpanMillis);
        }
    }


    private BestEffortNotificationDoneStatus checkResult(Object result, BestEffortNotificationTransaction transaction) {
        if (result instanceof BestEffortNotificationDoneStatus) {
            BestEffortNotificationDoneStatus doneStatus = (BestEffortNotificationDoneStatus) result;
            doAfterDoneStatus(doneStatus, transaction);
            return doneStatus;
        } else if (result instanceof BestEffortNotificationDoneResult) {
            ((BestEffortNotificationDoneResult) result).setNotificationId(transaction.getNotificationId());
            BestEffortNotificationDoneStatus doneStatus = ((BestEffortNotificationDoneResult) result).getDoneStatus();
            doAfterDoneStatus(doneStatus, transaction);
            return doneStatus;
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

    private Object doAroundTransactionContext(BestEffortNotificationTransaction transaction, Callable<Object> callable) throws Exception {
        Object invoke = null;
        BestEffortNotificationTransactionContext suspendTransactionContext = BestEffortNotificationHolder.getTransactionContext();
        try {
            BestEffortNotificationHolder.bindTransactionContext(new BestEffortNotificationTransactionContext(transaction.getNotificationId(), transaction.getResourceId()));
            invoke = callable.call();
        } finally {
            BestEffortNotificationTransactionContext existTransactionContext = BestEffortNotificationHolder.getTransactionContext();
            if (Objects.equals(existTransactionContext.getNotificationId(), transaction.getNotificationId())) {
                BestEffortNotificationHolder.clearTransactionContext();
            }
            if (suspendTransactionContext != null) {
                BestEffortNotificationHolder.bindTransactionContext(suspendTransactionContext);
            }
        }
        return invoke;
    }

}
