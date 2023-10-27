package com.ygy.tcc.core;

import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.exception.TccException;
import lombok.Data;


@Data
public class TccParticipant {

    private String tccId;

    private String participantId;

    private TccParticipantStatus status;

    private TccResource resource;

    private Object[] args;

    private int retryTimes;

    public void commit() {
        try {
            resource.getConfirmMethod().invoke(resource.getTargetBean(), args);
        } catch (Exception exception) {
            throw new TccException("commit fail", exception);
        }

    }

    public void rollback() {
        try {
            resource.getRollbackMethod().invoke(resource.getTargetBean(), args);
        } catch (Exception exception) {
            throw new TccException("rollback fail", exception);
        }
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }
}
