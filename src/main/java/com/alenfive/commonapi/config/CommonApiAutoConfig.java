package com.alenfive.commonapi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties({CommonApiProperties.class})
public class CommonApiAutoConfig {
    @Bean
    @ConditionalOnMissingBean
    public CommonController getCommonController(){
        return new CommonController();
    }

    @Bean
    @ConditionalOnMissingBean
    public CommonService getCommonService(){
        return new CommonService();
    }

    @Bean
    @ConditionalOnMissingBean
    public CommonMapper getCommonMapper(){
        return new CommonMapper();
    }

}
