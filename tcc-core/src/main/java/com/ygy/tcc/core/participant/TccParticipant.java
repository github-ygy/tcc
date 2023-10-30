package com.ygy.tcc.core.participant;

import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.exception.TccException;
import com.ygy.tcc.core.holder.TccHolder;
import lombok.Data;

import java.lang.reflect.Method;


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
        try {
            TccParticipantContext tccParticipantContext = new TccParticipantContext(tccId, participantId, status, resource.getResourceId(), resource.getResourceType());
            TccHolder.bindParticipantContext(tccParticipantContext);
            method.invoke(targetBean, args);
        } catch (Exception exception) {
            throw new TccException("invoke error:" + errorMsg, exception);
        }finally {
            TccParticipantContext participantContext = TccHolder.getParticipantContext();
            if (participantContext != null && participantContext.getParticipantId().equals(participantId)) {
                TccHolder.clearParticipantContext();
            }
        }
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }
}
