package com.ygy.tcc.recovery;


import com.ygy.tcc.core.TccTransaction;
import com.ygy.tcc.core.TccTransactionManager;
import com.ygy.tcc.core.configration.TccProperties;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.logger.TccLogger;
import com.ygy.tcc.core.recovery.TccRecoveryJobTask;
import com.ygy.tcc.core.recovery.TccRecoveryLoadResult;
import com.ygy.tcc.core.recovery.DefaultTccRecoveryProps;
import com.ygy.tcc.core.repository.TccTransactionRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TccRecoveryJob {

    private static final ScheduledExecutorService RECOVERY_JOB = Executors.newSingleThreadScheduledExecutor();

    private static final int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;
    private static final int queueSize = 1024;

    private final ThreadPoolExecutor recoveryPool = new ThreadPoolExecutor(1, threadPoolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueSize), new ThreadPoolExecutor.CallerRunsPolicy());


    public TccRecoveryJob() {
        start();
    }

    private final AtomicBoolean RECOVERY_ING = new AtomicBoolean();

    private void start() {
        String tccAppId = TccProperties.getTccAppId();
        RECOVERY_JOB.scheduleAtFixedRate(() -> {
            if (!tccRecoveryStartSwitch()) {
                TccLogger.warn("tcc transaction recovery job is stop");
                return;
            }
            if (!RECOVERY_ING.compareAndSet(false, true)) {
                TccLogger.warn("tcc transaction is recovering, please wait...");
                return;
            }
            try {
                String cursor = "";
                do {
                    TccRecoveryLoadResult loadResult = TccHolder.getHoldBean(TccTransactionRepository.class).loadNeedRecoverTransactions(tccAppId, cursor);
                    if (CollectionUtils.isEmpty(loadResult.getNeedRecoverTransactions())) {
                        break;
                    }
                    cursor = loadResult.getNextCursor();
                    for (TccTransaction transaction : loadResult.getNeedRecoverTransactions()) {
                        getRecoveryPool().submit(new TccRecoveryJobTask(transaction, TccHolder.getHoldBean(TccTransactionManager.class)));
                    }
                } while (StringUtils.isNotEmpty(cursor));
            } catch (Exception e) {
                TccLogger.error("recover tcc transaction error:", e);
            }
            RECOVERY_ING.compareAndSet(true, false);
        }, tccRecoveryInitDelaySeconds(), tccRecoveryPeriod(), java.util.concurrent.TimeUnit.SECONDS);
    }

    private int tccRecoveryInitDelaySeconds() {
        return TccProperties.getIntPropOrDefault(TccProperties.TCC_RECOVERY_INIT_DELAY_SECONDS_FIELD, DefaultTccRecoveryProps.TCC_RECOVERY_INIT_DELAY_SECONDS);
    }

    private boolean tccRecoveryStartSwitch() {
        return TccProperties.getBooleanPropOrDefault(TccProperties.TCC_RECOVERY_START_SWITCH_FIELD, DefaultTccRecoveryProps.TCC_RECOVERY_START_SWITCH);
    }

    private int tccRecoveryPeriod() {
        return TccProperties.getIntPropOrDefault(TccProperties.TCC_RECOVERY_PERIOD_SECONDS_FIELD, DefaultTccRecoveryProps.DEFAULT_TCC_RECOVERY_PERIOD_SECONDS);
    }

    public ThreadPoolExecutor getRecoveryPool() {
        return recoveryPool;
    }
}
