package com.ygy.tcc.core.aop;

import com.ygy.tcc.annotation.TccMethod;
import com.ygy.tcc.core.aop.annotation.TccTransactional;
import com.ygy.tcc.core.enums.TccStatus;
import com.ygy.tcc.core.enums.TransactionRole;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.TccTransactionManager;
import com.ygy.tcc.core.participant.TccPropagationContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Objects;


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
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        TccTransactional tccTransactional = method.getAnnotation(TccTransactional.class);
        transaction = new TccTransaction(TransactionRole.Initiator);
        TccPropagationContext propagationContext = TccHolder.getPropagationContext();
        if (propagationContext != null) {
            transaction.setParentTccId(propagationContext.getTccId());
            transaction.setParentParticipantId(propagationContext.getParticipantId());
            transaction.setParentTccAppId(propagationContext.getTccAppId());
            TccHolder.clearPropagationContext();
            //only try can start new transaction
            if (!Objects.equals(propagationContext.getTccStatus(), TccStatus.TRYING)) {
                return jp.proceed();
            }
        }
        try {
            tccTransactionManager.begin(transaction);
            Object result;
            try {
                result = jp.proceed();
            } catch (Throwable throwable) {
                tccTransactionManager.rollBack(tccTransactional.asyncRollback());
                throw throwable;
            }
            tccTransactionManager.commit(tccTransactional.asyncCommit());
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
