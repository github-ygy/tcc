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

    public void commit() {
        invoke(resource.getConfirmMethod(), resource.getTargetBean(), args, " commit fail");
    }

    public void rollback() {
        invoke(resource.getRollbackMethod(), resource.getTargetBean(), args, " rollback fail");
    }

    private void invoke(Method method, Object targetBean, Object[] args, String errorMsg) {
        TccTransaction transaction = TccHolder.getTransaction();
        if (transaction == null) {
            throw new TccException("transaction is null");
        }
        if (Objects.equals(resource.getResourceType(), TccResourceType.LOCAL)) {
            try {
                method.invoke(targetBean, args);
            } catch (Exception exception) {
                throw new TccException("invoke error:" + errorMsg, exception);
            }
            return;
        }
        TccPropagationContext suspendPropagationContext = TccHolder.getPropagationContext();
        try {
            TccPropagationContext tccPropagationContext = new TccPropagationContext(transaction.getTccAppId(), transaction.getTccId(), transaction.getStatus(), participantId);
            TccHolder.bindPropagationContext(tccPropagationContext);
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
