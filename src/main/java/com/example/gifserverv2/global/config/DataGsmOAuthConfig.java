package com.example.gifserverv2.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient;

@Configuration
public class DataGsmOAuthConfig {

    @Bean
    public DataGsmOAuthClient dataGsmOAuthClient(@Value("${oauth.datagsm.client-id}") String clientId,
                                                 @Value("${oauth.datagsm.client-secret}") String clientSecret) {
        return DataGsmOAuthClient.builder(clientId, clientSecret).build();
    }
}
