package com.ygy.tcc.core.aop;

import com.ygy.tcc.core.*;
import com.ygy.tcc.core.aop.annotation.TccMethod;
import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.enums.TccResourceType;
import com.ygy.tcc.core.enums.TccStatus;
import com.ygy.tcc.core.enums.TransactionRole;
import com.ygy.tcc.core.exception.TccException;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.util.UuidGenerator;
import org.apache.commons.lang3.StringUtils;
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
public class LocalTccMethodAop implements Ordered {


    @Pointcut("@annotation(com.ygy.tcc.core.aop.annotation.TccMethod)")
    private void pointcut() {
    }

    @Resource
    private TccTransactionManager tccTransactionManager;

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        TccTransaction transaction = TccHolder.getTransaction();
        if (transaction != null && Objects.equals(transaction.getRole(), TransactionRole.Initiator) && Objects.equals(transaction.getStatus(), TccStatus.TRYING)) {
            TccParticipant participant = addLocalParticipant(jp, transaction);
            try {
                Object result = jp.proceed();
                participant.setStatus(TccParticipantStatus.TRY_SUCCESS);
                return result;
            } catch (Throwable e) {
                participant.setStatus(TccParticipantStatus.TRY_FAIL);
                throw e;
            }
        }
        return jp.proceed();
    }

    private TccParticipant addLocalParticipant(ProceedingJoinPoint jp, TccTransaction transaction) {
        TccParticipant tccParticipant = new TccParticipant();
        tccParticipant.setTccId(transaction.getTccId());
        tccParticipant.setParticipantId(UuidGenerator.generateParticipantId());
        tccParticipant.setStatus(TccParticipantStatus.TRYING);
        tccParticipant.setArgs(jp.getArgs());
        TccResource resource = parseAndGetResourceFromLocal(jp);
        if (resource == null) {
            throw new TccException("resource is null");
        }
        tccParticipant.setResource(resource);
        tccTransactionManager.addParticipant(tccParticipant);
        return tccParticipant;
    }

    private TccResource parseAndGetResourceFromLocal(ProceedingJoinPoint jp) {
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        TccMethod tccMethod = method.getAnnotation(TccMethod.class);
        String resourceId = StringUtils.isEmpty(tccMethod.resourceId()) ? method.getName() : tccMethod.resourceId();
        return TccHolder.getTccResource(resourceId, TccResourceType.LOCAL);
    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
