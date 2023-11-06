package com.ygy.tcc.notification.delay;


import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.logger.TccLogger;
import com.ygy.tcc.notification.BestEffortNotificationTransaction;
import com.ygy.tcc.notification.BestEffortNotificationTransactionManager;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultBestEffortNotificationDelayTaskJob implements BestEffortNotificationDelayTaskJob{

    private static final Timer DELAY = new HashedWheelTimer();

    private int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;

    private int threadQueueSize = 1024;

    private ThreadPoolExecutor asyncPoolExecutor = new ThreadPoolExecutor(1, threadPoolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(threadQueueSize), new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public void delayCheck(BestEffortNotificationTransaction transaction, long delaySeconds){
        DELAY.newTimeout((x) -> {
            asyncPoolExecutor.submit(() -> {
                try {
                    TccHolder.getHoldBean(BestEffortNotificationTransactionManager.class).checkMethod(transaction);
                } catch (Exception exception) {
                    TccLogger.error("check method error", exception);
                }
            });
        }, delaySeconds, TimeUnit.SECONDS);
    }

    public ThreadPoolExecutor getAsyncPoolExecutor() {
        return asyncPoolExecutor;
    }
}