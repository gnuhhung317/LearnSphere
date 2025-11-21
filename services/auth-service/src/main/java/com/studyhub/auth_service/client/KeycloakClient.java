package com.studyhub.auth_service.client;

import com.studyhub.auth_service.config.KeycloakFeignConfig;
import com.studyhub.auth_service.dto.internal_response.KeycloakTokenResponse;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "keycloakTokenClient",
        url = "${keycloak.server-url}",
        configuration = KeycloakFeignConfig.class
)
public interface KeycloakClient {

    @PostMapping(
            value = "/realms/studyhub/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    AccessTokenResponse login(Map<String, String> formParameters);
}
