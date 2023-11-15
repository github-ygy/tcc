package com.ygy.tcc.alibaba.dubbo.filter;


import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.ygy.tcc.alibaba.dubbo.constants.TccDubboConstants;
import com.ygy.tcc.annotation.TccMethod;
import com.ygy.tcc.core.participant.TccParticipant;
import com.ygy.tcc.core.participant.TccPropagationContext;
import com.ygy.tcc.core.participant.TccResource;
import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.TccTransactionManager;
import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.enums.TccResourceType;
import com.ygy.tcc.core.enums.TccStatus;
import com.ygy.tcc.core.enums.TransactionRole;
import com.ygy.tcc.core.exception.TccException;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.util.GsonUtil;
import com.ygy.tcc.core.util.TccUtil;

import java.lang.reflect.Method;
import java.util.Objects;

@Activate(group = {"consumer"}, order = 0)
public class TccMethodFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        TccTransaction transaction = TccHolder.getTransaction();
        if (transaction == null) {
            return invoker.invoke(invocation);
        }
        Method method = null;
        try {
            method = invoker.getInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
        } catch (Exception exception) {
            throw new RpcException(exception);
        }
        try {
            TccMethod tccMethod = method.getAnnotation(TccMethod.class);
            if (tccMethod != null) {
                if (Objects.equals(transaction.getRole(), TransactionRole.Initiator) && Objects.equals(transaction.getStatus(), TccStatus.TRYING)) {
                    TccParticipant participant = addDubboParticipant(TccUtil.getResourceId(tccMethod.resourceId(), invoker.getInterface(), method), transaction, invocation);
                    try {
                        RpcContext.getContext().setAttachment(TccDubboConstants.TCC_PROPAGATION_CONTEXT_DUBBO_KEY, GsonUtil.toJson(new TccPropagationContext(transaction.getTccAppId(), transaction.getTccId(), transaction.getStatus(), participant.getParticipantId(), participant.getStatus(), participant.getResource().getResourceId())));
                        Result result = invoker.invoke(invocation);
                        participant.setStatus(TccParticipantStatus.TRY_SUCCESS);
                        return result;
                    } catch (Throwable e) {
                        participant.setStatus(TccParticipantStatus.TRY_FAIL);
                        throw e;
                    }
                }
            }
            TccPropagationContext propagationContext = TccHolder.getPropagationContext();
            if (propagationContext != null && TccUtil.checkIsParticipantMethod(propagationContext, method, TccResourceType.DUBBO_REFERENCE)) {
                RpcContext.getContext().setAttachment(TccDubboConstants.TCC_PROPAGATION_CONTEXT_DUBBO_KEY, GsonUtil.toJson(propagationContext));
            } else {
                RpcContext.getContext().setAttachment(TccDubboConstants.TCC_PROPAGATION_CONTEXT_DUBBO_KEY, GsonUtil.toJson(new TccPropagationContext(transaction.getTccAppId(), transaction.getTccId(), transaction.getStatus(), null, null, null)));
            }
            return invoker.invoke(invocation);
        } catch (Exception ex) {
            throw new RpcException(ex);
        }
    }

    private TccParticipant addDubboParticipant(String resourceId, TccTransaction transaction, Invocation invocation) {
        TccParticipant tccParticipant = new TccParticipant();
        tccParticipant.setTccId(transaction.getTccId());
        tccParticipant.setParticipantId(TccHolder.getHoldBean(TccTransactionManager.class).generateParticipantId());
        tccParticipant.setStatus(TccParticipantStatus.TRYING);
        tccParticipant.setArgs(invocation.getArguments());
        TccResource resource = TccHolder.getTccResource(resourceId, TccResourceType.DUBBO_REFERENCE);
        if (resource == null) {
            throw new TccException("resource is null");
        }
        tccParticipant.setResource(resource);
        boolean addResult = TccHolder.getHoldBean(TccTransactionManager.class).addParticipant(transaction, tccParticipant);
        if (!addResult) {
            throw new TccException("add participant error");
        }
        return tccParticipant;
    }
}
