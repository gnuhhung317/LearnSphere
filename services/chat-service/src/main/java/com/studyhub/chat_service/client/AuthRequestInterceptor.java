package com.studyhub.chat_service.client;

import com.studyhub.chat_service.util.JwtUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class AuthRequestInterceptor implements RequestInterceptor{

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("Authorization", "Bearer " + JwtUtil.getJwt());
    }
}
