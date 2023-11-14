package com.ygy.tcc.core.participant;

import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.enums.TccResourceType;
import com.ygy.tcc.core.enums.TccStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;



@Data
public class TccPropagationContext implements Serializable {

    private String tccAppId;

    private String tccId;

    private TccStatus tccStatus;

    private String participantId;

    private TccParticipantStatus participantStatus;

    private String resourceId;

    public TccPropagationContext(String tccAppId, String tccId, TccStatus tccStatus, String participantId, TccParticipantStatus participantStatus, String resourceId) {
        this.tccAppId = tccAppId;
        this.tccId = tccId;
        this.tccStatus = tccStatus;
        this.participantId = participantId;
        this.participantStatus = participantStatus;
        this.resourceId = resourceId;
    }


}
