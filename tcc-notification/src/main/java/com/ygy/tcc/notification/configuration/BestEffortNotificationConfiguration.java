package com.ygy.tcc.notification.configuration;

import com.ygy.tcc.notification.BestEffortNotificationAop;
import com.ygy.tcc.notification.BestEffortNotificationTransactionManager;
import com.ygy.tcc.notification.delay.BestEffortNotificationDelayTaskJob;
import com.ygy.tcc.notification.delay.DefaultBestEffortNotificationDelayTaskJob;
import com.ygy.tcc.notification.generator.DefaultIdGenerator;
import com.ygy.tcc.notification.generator.IdGenerator;
import com.ygy.tcc.notification.listenner.BestEffortNotificationResourceFindListener;
import com.ygy.tcc.notification.repository.BestEffortNotificationTransactionMapper;
import com.ygy.tcc.notification.repository.BestEffortNotificationTransactionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;


@Configuration
public class BestEffortNotificationConfiguration {


    @Bean
    public BestEffortNotificationTransactionRepository bestEffortNotificationTransactionRepository(BestEffortNotificationTransactionMapper bestEffortNotificationTransactionMapper) {
        return new BestEffortNotificationTransactionRepository(bestEffortNotificationTransactionMapper);
    }

    @Bean
    @ConditionalOnMissingBean(IdGenerator.class)
    public IdGenerator idGenerator() {
        return new DefaultIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean(BestEffortNotificationDelayTaskJob.class)
    public BestEffortNotificationDelayTaskJob bestEffortNotificationDelayTaskJob() {
        return new DefaultBestEffortNotificationDelayTaskJob();
    }

    @Bean
    @DependsOn({"bestEffortNotificationTransactionRepository"})
    public BestEffortNotificationTransactionManager bestEffortNotificationTransactionManager(IdGenerator idGenerator, BestEffortNotificationDelayTaskJob bestEffortNotificationDelayTaskJob) {
        return new BestEffortNotificationTransactionManager(idGenerator, bestEffortNotificationDelayTaskJob);
    }

    @Bean
    @DependsOn({"bestEffortNotificationTransactionManager"})
    public BestEffortNotificationAop bestEffortNotificationAop() {
        return new BestEffortNotificationAop();
    }


    @Bean
    public BestEffortNotificationResourceFindListener bestEffortNotificationResourceFindListener() {
        return new BestEffortNotificationResourceFindListener();
    }

}
