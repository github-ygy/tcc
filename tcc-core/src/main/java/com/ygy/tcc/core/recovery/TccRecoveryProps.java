package com.ygy.tcc.core.recovery;



import lombok.Data;

import java.io.Serializable;

@Data
public class TccRecoveryProps implements Serializable {

    private static final long DEFAULT_VALID_RECOVERY_TIME_SPAN = 5 * 60 * 1000L;
    private static final long DEFAULT_TRYING_STATUS_TRANSFER_TO_ROLLBACK_TIME_SPAN = 10 * 60 * 1000L;

    private long validRecoveryTimeSpan = DEFAULT_VALID_RECOVERY_TIME_SPAN;

    private long tryingStatusTransferToRollBackTimeSpan = DEFAULT_TRYING_STATUS_TRANSFER_TO_ROLLBACK_TIME_SPAN;

    private Integer maxRecoveryTimes = 1;

}
