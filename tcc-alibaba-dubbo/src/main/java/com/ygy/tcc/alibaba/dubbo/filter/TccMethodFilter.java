package com.ygy.tcc.alibaba.dubbo.filter;


import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.ygy.tcc.annotation.TccMethod;
import com.ygy.tcc.core.TccParticipant;
import com.ygy.tcc.core.TccResource;
import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.TccTransactionManager;
import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.enums.TccResourceType;
import com.ygy.tcc.core.enums.TccStatus;
import com.ygy.tcc.core.enums.TransactionRole;
import com.ygy.tcc.core.exception.TccException;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.util.ResourceUtil;
import com.ygy.tcc.core.util.UuidGenerator;

import java.lang.reflect.Method;
import java.util.Objects;

@Activate(group = {"consumer"}, order = 0)
public class TccMethodFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        try {
            Method method = invoker.getInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
            TccMethod tccMethod = method.getAnnotation(TccMethod.class);
            if (tccMethod != null) {
                TccTransaction transaction = TccHolder.getTransaction();
                if (transaction != null && Objects.equals(transaction.getRole(), TransactionRole.Initiator) && Objects.equals(transaction.getStatus(), TccStatus.TRYING)) {
                    TccParticipant participant = addDubboParticipant(ResourceUtil.getResourceId(tccMethod, invoker.getInterface(), method), transaction, invocation);
                    try {
                        Result result = invoker.invoke(invocation);
                        participant.setStatus(TccParticipantStatus.TRY_SUCCESS);
                        return result;
                    } catch (Throwable e) {
                        participant.setStatus(TccParticipantStatus.TRY_FAIL);
                        throw e;
                    }
                }
            }
            return invoker.invoke(invocation);
        } catch (Exception ex) {
            throw new RpcException(ex);
        }
    }

    private TccParticipant addDubboParticipant(String resourceId, TccTransaction transaction, Invocation invocation) {
        TccParticipant tccParticipant = new TccParticipant();
        tccParticipant.setTccId(transaction.getTccId());
        tccParticipant.setParticipantId(UuidGenerator.generateParticipantId());
        tccParticipant.setStatus(TccParticipantStatus.TRYING);
        tccParticipant.setArgs(invocation.getArguments());
        TccResource resource = TccHolder.getTccResource(resourceId, TccResourceType.DUBBO_REFERENCE);
        if (resource == null) {
            throw new TccException("resource is null");
        }
        tccParticipant.setResource(resource);
        TccHolder.getHoldBean(TccTransactionManager.class).addParticipant(tccParticipant);
        return tccParticipant;
    }
}
