package com.ygy.tcc.core.recovery;


import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.TccTransactionManager;
import com.ygy.tcc.core.configration.TccProperties;
import com.ygy.tcc.core.enums.TccStatus;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.logger.TccLogger;
import com.ygy.tcc.core.util.TimeUtil;

public class TccRecoveryJobTask implements Runnable {

    private TccTransaction tccTransaction;

    private TccTransactionManager tccTransactionManager;


    public TccRecoveryJobTask(TccTransaction tccTransaction, TccTransactionManager tccTransactionManager) {
        this.tccTransaction = tccTransaction;
        this.tccTransactionManager = tccTransactionManager;
    }

    @Override
    public void run() {
        long timeSpan = TimeUtil.getCurrentTime() - tccTransaction.getUpdateTime();
        if (timeSpan <= getValidRecoveryTimeSpan()) {
            return;
        }
        if (tccTransaction.getRecoveryTimes() >= getMaxRecoveryTimes()) {
            return;
        }
        try {
            TccStatus status = tccTransaction.getStatus();
            switch (status) {
                case CONFIRM:
                case CONFIRM_FAIL:
                    tccTransaction.setRecoveryTimes(tccTransaction.getRecoveryTimes() + 1);
                    tccTransactionManager.commitTransaction(tccTransaction);
                    break;
                case TRYING:
                case ROLLBACK:
                case ROLLBACK_FAIL:
                    if (status == TccStatus.TRYING) {
                        if (timeSpan <= getTryingStatusTransferToRollBackTimeSpan()) {
                            return;
                        }
                    }
                    tccTransaction.setRecoveryTimes(tccTransaction.getRecoveryTimes() + 1);
                    tccTransactionManager.rollbackTransaction(tccTransaction);
                    break;
            }
        } catch (Exception exception) {
            TccLogger.error("recovery task fail:" + tccTransaction.getTccId(), exception);
        }

    }

    private long getTryingStatusTransferToRollBackTimeSpan() {
        return TccProperties.getLongPropOrDefault(TccProperties.TRYING_STATUS_TRANSFER_TO_ROLL_BACK_TIME_SPAN_FIELD, DefaultTccRecoveryProps.DEFAULT_TRYING_STATUS_TRANSFER_TO_ROLLBACK_TIME_SPAN);
    }

    private int getMaxRecoveryTimes() {
        return TccProperties.getIntPropOrDefault(TccProperties.MAX_RECOVERY_TIMES_FIELD, DefaultTccRecoveryProps.DEFAULT_MAX_RECOVERY_TIMES);
    }

    public long getValidRecoveryTimeSpan() {
        return TccProperties.getLongPropOrDefault(TccProperties.VALID_RECOVERY_TIME_SPAN_FIELD, DefaultTccRecoveryProps.DEFAULT_VALID_RECOVERY_TIME_SPAN);
    }

}
