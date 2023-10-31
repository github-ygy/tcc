package com.ygy.tcc.core.configration;

import com.ygy.tcc.core.TccTransactionManager;
import com.ygy.tcc.core.aop.LocalTccMethodAop;
import com.ygy.tcc.core.aop.TccTransactionalAop;
import com.ygy.tcc.core.generator.DefaultUuidGenerator;
import com.ygy.tcc.core.listener.LocalTccResourceFindListener;
import com.ygy.tcc.core.repository.TccTransactionMapper;
import com.ygy.tcc.core.repository.TccTransactionRepository;
import com.ygy.tcc.core.spring.SpringBeanContext;
import com.ygy.tcc.core.generator.UuidGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;


@Configuration
public class TccConfiguration {

    @Value("${tcc.app.id}")
    private String tccAppId;

    @Bean
    public TccTransactionRepository tccTransactionRepository(TccTransactionMapper tccTransactionMapper){
        return new TccTransactionRepository(tccTransactionMapper);
    }

    @Bean
    @ConditionalOnMissingBean(UuidGenerator.class)
    public UuidGenerator uuidGenerator() {
        return new DefaultUuidGenerator();
    }

    @Bean
    @DependsOn({"tccTransactionRepository"})
    public TccTransactionManager tccTransactionManager(UuidGenerator uuidGenerator) {
        TccConfigProps tccConfigProps = new TccConfigProps();
        tccConfigProps.setTccAppId(tccAppId);
        return new TccTransactionManager(tccConfigProps, uuidGenerator);
    }

    @Bean
    @DependsOn({"tccTransactionManager"})
    public TccTransactionalAop tccTransactionalAop() {
        return new TccTransactionalAop();
    }

    @Bean
    @DependsOn({"tccTransactionManager"})
    public LocalTccMethodAop localTccMethodAop() {
        return new LocalTccMethodAop();
    }

    @Bean
    public LocalTccResourceFindListener tccResourceFindListener(){
        return new LocalTccResourceFindListener();
    }

    @Bean
    @DependsOn({"tccTransactionManager"})
    public SpringBeanContext springBeanContext(){
        return new SpringBeanContext();
    }

}
