package com.ygy.tcc.notification;

import com.ygy.tcc.core.logger.TccLogger;
import com.ygy.tcc.notification.annotation.BestEffortNotification;
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


@Aspect
public class BestEffortNotificationAop implements Ordered {


    @Pointcut("@annotation(com.ygy.tcc.annotation.BestEffortNotification)")
    private void pointcut() {
    }

    @Resource
    private BestEffortNotificationTransactionManager bestEffortNotificationTransactionManager;

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        BestEffortNotification notification = method.getAnnotation(BestEffortNotification.class);
        BestEffortNotificationTransaction transaction = bestEffortNotificationTransactionManager.newTransaction(notification.resourceId(), jp.getArgs());
        //入库
        if (notification.afterCommitSynchronization()) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        try {
                            doProceed(transaction, jp);
                        } catch (Throwable ex) {
                            TccLogger.error("notification execute fail", ex);
                        }
                    }
                });
                return null;
            }
        }
        return doProceed(transaction, jp);
    }

    private Object doProceed(BestEffortNotificationTransaction transaction, ProceedingJoinPoint jp) throws Throwable {
        try {
            return jp.proceed();
        } finally {
            bestEffortNotificationTransactionManager.checkMethod(transaction);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
