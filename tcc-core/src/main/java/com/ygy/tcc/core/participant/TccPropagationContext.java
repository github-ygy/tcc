package com.ygy.tcc.core.participant;

import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.enums.TccResourceType;
import com.ygy.tcc.core.enums.TccStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;



@AllArgsConstructor
@Data
public class TccPropagationContext implements Serializable {

    private String tccAppId;

    private String tccId;

    private TccStatus tccStatus;

    private String participantId;


}
