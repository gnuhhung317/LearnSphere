package com.studyhub.auth_service.config;

import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.context.annotation.Bean;

public class KeycloakFeignConfig {

    @Bean
    public Encoder feignFormEncoder() {
        return new FormEncoder();
    }

    @Bean
    feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }

    /*
     * @Bean
     * public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
     * return new BasicAuthRequestInterceptor(clientId, clientSecret);
     * }
     */
}
