package com.ygy.tcc.core.repository;


import lombok.Data;

import java.io.Serializable;

@Data
public class TccParticipantDO implements Serializable {


    private String tccId;

    private String participantId;

    private String status;

    private String resourceId;

    private String resourceType;

    private Object[] args;

    private int retryTimes;
}
