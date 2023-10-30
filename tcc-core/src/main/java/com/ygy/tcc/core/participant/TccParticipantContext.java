package com.ygy.tcc.core.participant;

import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.enums.TccResourceType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;



@AllArgsConstructor
@Data
public class TccParticipantContext implements Serializable {

    private String tccId;

    private String participantId;

    private TccParticipantStatus status;

    private String resourceId;

    private TccResourceType resourceType;

}
