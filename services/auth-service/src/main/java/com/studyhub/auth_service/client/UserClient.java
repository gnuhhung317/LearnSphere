package com.studyhub.auth_service.client;

import com.studyhub.auth_service.dto.interal_request.CreateUserRequest;
import com.studyhub.auth_service.dto.response.RegisterResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "user-service")
public interface UserClient {

    @PostMapping("api/users")
    RegisterResponse register(CreateUserRequest request);
}
