package com.ygy.tcc.core.recovery;


import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.TccTransactionManager;
import com.ygy.tcc.core.enums.TccStatus;
import com.ygy.tcc.core.logger.TccLogger;
import com.ygy.tcc.core.util.TimeUtil;

public class TccRecoveryJobTask implements Runnable {

    private TccTransaction tccTransaction;

    private TccTransactionManager tccTransactionManager;

    private TccRecoveryProps tccRecoveryProps;


    public TccRecoveryJobTask(TccRecoveryProps tccRecoveryProps, TccTransaction tccTransaction, TccTransactionManager tccTransactionManager) {
        this.tccRecoveryProps = tccRecoveryProps;
        this.tccTransaction = tccTransaction;
        this.tccTransactionManager = tccTransactionManager;
    }

    @Override
    public void run() {
        long timeSpan = TimeUtil.getCurrentTime() - tccTransaction.getUpdateTime();
        if (timeSpan <= tccRecoveryProps.getValidRecoveryTimeSpan()) {
            return;
        }
        if (tccTransaction.getRecoveryTimes() > tccRecoveryProps.getMaxRecoveryTimes()) {
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
                        if (timeSpan <= tccRecoveryProps.getTryingStatusTransferToRollBackTimeSpan()) {
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
}
