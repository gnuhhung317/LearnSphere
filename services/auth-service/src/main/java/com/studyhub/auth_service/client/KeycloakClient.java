package com.studyhub.auth_service.client;

import com.studyhub.auth_service.config.KeycloakFeignConfig;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@FeignClient(name = "keycloakTokenClient", url = "${keycloak.server-url}", configuration = KeycloakFeignConfig.class)
public interface KeycloakClient {

        @PostMapping(value = "/realms/master/protocol/openid-connect/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        AccessTokenResponse login(Map<String, ?> formParameters);

        @PostMapping(value = "/realms/master/protocol/openid-connect/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        AccessTokenResponse refreshToken(Map<String, ?> formParameters);
}
