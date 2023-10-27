package com.ygy.tcc.apache.dubbo.configuration;

import com.ygy.tcc.apache.dubbo.ApacheDubboTccResourceFindListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TccApacheDubboConfiguration {

    @Bean
    public ApacheDubboTccResourceFindListener apacheDubboTccResourceFindListener(){
        return new ApacheDubboTccResourceFindListener();
    }



}
