package com.ygy.tcc.alibaba.dubbo.configuration;

import com.ygy.tcc.alibaba.dubbo.AlibabaDubboTccResourceFindListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TccAlibabaDubboConfiguration {


    @Bean
    public AlibabaDubboTccResourceFindListener alibabaDubboTccResourceFindListener(){
        return new AlibabaDubboTccResourceFindListener();
    }



}
