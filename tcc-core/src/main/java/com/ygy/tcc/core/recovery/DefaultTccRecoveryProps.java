package com.ygy.tcc.core.recovery;


public final class DefaultTccRecoveryProps {

    public static final long DEFAULT_VALID_RECOVERY_TIME_SPAN = 5 * 60 * 1000L;
    public static final long DEFAULT_TRYING_STATUS_TRANSFER_TO_ROLLBACK_TIME_SPAN = 10 * 60 * 1000L;
    public static final int DEFAULT_MAX_RECOVERY_TIMES = 1;
    public static final int DEFAULT_TCC_RECOVERY_PERIOD_SECONDS = 60;

    public static final int TCC_RECOVERY_INIT_DELAY_SECONDS = 10;
    public static final boolean TCC_RECOVERY_START_SWITCH = true;


}
