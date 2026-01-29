package com.studyhub.search_service.client;

import com.studyhub.search_service.util.JwtUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = JwtUtil.getJwt();
        if (token != null) {
            requestTemplate.header("Authorization", "Bearer " + token);
        }
    }
}
