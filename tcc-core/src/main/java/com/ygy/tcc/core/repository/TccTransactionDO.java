package com.ygy.tcc.core.repository;


import lombok.Data;

import java.io.Serializable;

@Data
public class TccTransactionDO implements Serializable {

    private String parentTccAppId;
    private String parentTccId;
    private String parentParticipantId;
    private String tccRole;
    private String tccAppId;
    private String tccId;
    private String tccStatus;
    private Integer version;
    private Long createTime;
    private Long updateTime;
    private String participantsJson;
    private Integer recoveryTimes;
    private Integer doneStatus;


}
