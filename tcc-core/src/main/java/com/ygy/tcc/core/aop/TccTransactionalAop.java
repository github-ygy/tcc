package com.ygy.tcc.core.aop;

import com.ygy.tcc.core.enums.TransactionRole;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.TccTransactionManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;

import javax.annotation.Resource;


@Aspect
public class TccTransactionalAop implements Ordered {


    @Pointcut("@annotation(com.ygy.tcc.core.aop.annotation.TccTransactional)")
    private void pointcut() {
    }

    @Resource
    private TccTransactionManager tccTransactionManager;

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        TccTransaction transaction = TccHolder.getTransaction();
        if (transaction != null) {
            return jp.proceed();
        }
        transaction = new TccTransaction(TransactionRole.Initiator);
        try {
            tccTransactionManager.begin(transaction);
            Object result;
            try {
                result = jp.proceed();
            } catch (Throwable throwable) {
                tccTransactionManager.rollBack();
                throw throwable;
            }
            tccTransactionManager.commit();
            return result;
        } finally {
            tccTransactionManager.completion();
        }
    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
