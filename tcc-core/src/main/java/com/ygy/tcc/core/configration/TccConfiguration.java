package com.ygy.tcc.core.configration;

import com.ygy.tcc.core.TccTransactionManager;
import com.ygy.tcc.core.aop.LocalTccMethodAop;
import com.ygy.tcc.core.aop.TccTransactionalAop;
import com.ygy.tcc.core.listener.LocalTccResourceFindListener;
import com.ygy.tcc.core.repository.TccTransactionRepository;
import com.ygy.tcc.core.spring.SpringBeanContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;


@Configuration
public class TccConfiguration {

    @Bean
    public TccTransactionRepository tccTransactionRepository(){
        return new TccTransactionRepository();
    }

    @Bean
    @DependsOn({"tccTransactionRepository"})
    public TccTransactionManager tccTransactionManager(){
        return new TccTransactionManager();
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
