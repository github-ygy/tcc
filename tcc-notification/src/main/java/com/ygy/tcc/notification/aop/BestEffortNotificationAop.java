package com.ygy.tcc.notification.aop;

import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.logger.TccLogger;
import com.ygy.tcc.core.util.TccUtil;
import com.ygy.tcc.notification.BestEffortNotificationTransaction;
import com.ygy.tcc.notification.BestEffortNotificationTransactionContext;
import com.ygy.tcc.notification.BestEffortNotificationTransactionManager;
import com.ygy.tcc.notification.annotation.BestEffortNotification;
import com.ygy.tcc.notification.enums.BestEffortNotificationDoneStatus;
import com.ygy.tcc.notification.enums.BestEffortNotificationStatus;
import com.ygy.tcc.notification.holder.BestEffortNotificationHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Objects;


@Aspect
public class BestEffortNotificationAop implements Ordered {


    @Pointcut("@annotation(com.ygy.tcc.notification.annotation.BestEffortNotification)")
    private void pointcut() {
    }

    @Resource
    private BestEffortNotificationTransactionManager bestEffortNotificationTransactionManager;

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        BestEffortNotification notification = method.getAnnotation(BestEffortNotification.class);
        String bestEffortResourceId = TccUtil.getResourceId(notification.resourceId(), jp.getTarget().getClass(), method);
        if (checkMethodIsLoopDo(bestEffortResourceId) || !TccHolder.checkIsLocalBean(jp.getTarget().getClass())) {
            return jp.proceed();
        }
        BestEffortNotificationTransaction transaction = bestEffortNotificationTransactionManager.newTransaction(bestEffortResourceId, jp.getArgs());
        if (notification.customNotificationMethod()) {
            Object result = null;
            try {
                result = jp.proceed();
            } catch (Exception exception) {
                bestEffortNotificationTransactionManager.addDelayCheckTask(transaction);
                throw exception;
            }
            if (notification.afterCommitSynchronization()) {
                if (TransactionSynchronizationManager.isActualTransactionActive()) {
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCommit() {
                            try {
                                bestEffortNotificationTransactionManager.doRetryNotificationMethod(transaction);
                            } catch (Throwable ex) {
                                TccLogger.error("notification execute fail", ex);
                            }
                        }
                    });
                    return result;
                }
            }
            try {
                bestEffortNotificationTransactionManager.doRetryNotificationMethod(transaction);
            } catch (Throwable ex) {
                TccLogger.error("notification execute fail", ex);
            }
            return result;
        }
        if (notification.afterCommitSynchronization()) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        try {
                            bestEffortNotificationTransactionManager.doProceed(transaction, jp);
                        } catch (Throwable ex) {
                            TccLogger.error("notification execute fail", ex);
                        }
                    }
                });
                return null;
            }
        }
        return bestEffortNotificationTransactionManager.doProceed(transaction, jp);
    }

    private boolean checkMethodIsLoopDo(String bestEffortResourceId) {
        BestEffortNotificationTransactionContext existContext = BestEffortNotificationHolder.getTransactionContext();
        if (existContext != null && Objects.equals(bestEffortResourceId, existContext.getResourceId())) {
            return true;
        }
        return false;
    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
