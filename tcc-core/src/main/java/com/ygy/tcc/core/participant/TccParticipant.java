package com.ygy.tcc.core.participant;

import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.enums.TccResourceType;
import com.ygy.tcc.core.exception.TccException;
import com.ygy.tcc.core.holder.TccHolder;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Objects;


@Data
public class TccParticipant {

    private String tccId;

    private String participantId;

    private TccParticipantStatus status;

    private TccResource resource;

    private Object[] args;

    private int retryTimes;

    public void commit(TccTransaction transaction ) {
        invoke(transaction, resource.getConfirmMethod(), resource.getTargetBean(), args, " commit fail");
    }

    public void rollback(TccTransaction transaction) {
        invoke(transaction, resource.getRollbackMethod(), resource.getTargetBean(), args, " rollback fail");
    }

    private void invoke(TccTransaction transaction ,Method method, Object targetBean, Object[] args, String errorMsg) {
        TccPropagationContext propagationContext = new TccPropagationContext(transaction.getTccAppId(), transaction.getTccId(), transaction.getStatus(), participantId, status, resource.getResourceId());
        if (Objects.equals(resource.getResourceType(), TccResourceType.LOCAL)) {
            try {
                TccParticipantHookManager.doParticipantHook(propagationContext);
                method.invoke(targetBean, args);
            } catch (Exception exception) {
                throw new TccException("invoke error:" + errorMsg, exception);
            }
            return;
        }
        TccPropagationContext suspendPropagationContext = TccHolder.getPropagationContext();
        try {
            TccHolder.bindPropagationContext(propagationContext);
            method.invoke(targetBean, args);
        } catch (Exception exception) {
            throw new TccException("invoke error:" + errorMsg, exception);
        }finally {
            TccPropagationContext currentPropagationContext = TccHolder.getPropagationContext();
            if (currentPropagationContext != null && Objects.equals(currentPropagationContext.getParticipantId(), participantId)) {
                TccHolder.clearPropagationContext();
            }
            if (suspendPropagationContext != null) {
                TccHolder.bindPropagationContext(suspendPropagationContext);
            }
        }
    }

}
