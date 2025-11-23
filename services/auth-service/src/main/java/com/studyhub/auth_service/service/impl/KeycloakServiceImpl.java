package com.studyhub.auth_service.service.impl;

import com.studyhub.auth_service.client.KeycloakClient;
import com.studyhub.auth_service.dto.interal_request.CreateUserRequest;
import com.studyhub.auth_service.dto.request.LoginRequest;
import com.studyhub.auth_service.dto.request.RegisterRequest;
import com.studyhub.auth_service.dto.response.LoginResponse;
import com.studyhub.auth_service.dto.response.RegisterResponse;
import com.studyhub.auth_service.service.KeycloakService;
import com.studyhub.common.exception.BusinessException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private final Keycloak keycloak;
    private final KeycloakClient keycloakClient;
    private String keycloakRealm = "studyhub";

    @Override
    public CreateUserRequest createUser(RegisterRequest request) {

        UsersResource usersResource = getUsersResource(keycloakRealm);
        if (isUserExists(request.getEmail())) {
            throw new BusinessException("USER_EMAIL_EXISTS", request.getEmail());
        }
        UserRepresentation userRepresentation = getUserRepresentation(request);

        Response response = usersResource.create(userRepresentation);

        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        log.info("Created user in Keycloak: {}", userId);

        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setKeycloakUserId(userId);
        createUserRequest.setTheme(request.getTheme());
        createUserRequest.setLanguage(request.getAiResponseLanguage());
        createUserRequest.setFullName(request.getFullName());
        createUserRequest.setEmail(request.getEmail());
        return createUserRequest;
    }

    private static UserRepresentation getUserRepresentation(RegisterRequest request) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(request.getEmail());
        userRepresentation.setEmail(request.getEmail());
        userRepresentation.setFirstName(request.getFullName().split(" ")[0]);
        userRepresentation.setLastName(request.getFullName().substring(userRepresentation.getFirstName().length() + 1).strip());
        userRepresentation.setEnabled(true);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(request.getPassword());

        userRepresentation.setCredentials(List.of(credentialRepresentation));
        return userRepresentation;
    }

    @Override
    public void deleteUser(String keycloakUserId) {
        UsersResource usersResource = getUsersResource(keycloakRealm);
        usersResource.get(keycloakUserId).remove();
    }

    @Override
    public void updateUser(String userId, RegisterResponse request) {

    }

    private UsersResource getUsersResource(String keycloakRealm) {
        return keycloak.realm(keycloakRealm).users();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Map<String, String> form = Map.of("username", request.getEmail(), "password", request.getPassword(), "grant_type", "password", "scope", "email");
        try {
            AccessTokenResponse accessTokenResponse = keycloakClient.login(form);
            return LoginResponse.builder()
                    .accessToken(accessTokenResponse.getToken())
                    .refreshToken(accessTokenResponse.getRefreshToken())
                    .tokenType(accessTokenResponse.getTokenType())
                    .idToken(accessTokenResponse.getIdToken())
                    .expiresIn(accessTokenResponse.getExpiresIn())
                    .refreshExpiresIn(accessTokenResponse.getRefreshExpiresIn())
                    .notBeforePolicy(accessTokenResponse.getNotBeforePolicy())
                    .scope(accessTokenResponse.getScope())
                    .sessionState(accessTokenResponse.getSessionState())
                    .build();

        } catch (Exception e) {
            throw new BusinessException("KEYCLOAK_INTERNAL_ERROR");
        }
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        Map<String, String> form = Map.of(
                "grant_type", "refresh_token",
                "refresh_token", refreshToken
        );
        try {
            AccessTokenResponse accessTokenResponse = keycloakClient.refreshToken(form);
            return LoginResponse.builder()
                    .accessToken(accessTokenResponse.getToken())
                    .refreshToken(accessTokenResponse.getRefreshToken())
                    .tokenType(accessTokenResponse.getTokenType())
                    .idToken(accessTokenResponse.getIdToken())
                    .expiresIn(accessTokenResponse.getExpiresIn())
                    .refreshExpiresIn(accessTokenResponse.getRefreshExpiresIn())
                    .notBeforePolicy(accessTokenResponse.getNotBeforePolicy())
                    .scope(accessTokenResponse.getScope())
                    .sessionState(accessTokenResponse.getSessionState())
                    .build();
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            throw new BusinessException("REFRESH_TOKEN_EXPIRED");
        }
    }

    private boolean isUserExists(String email) {
        try {
            return !getUsersResource(keycloakRealm).searchByEmail(email, true).isEmpty();
        } catch (Exception e) {
            throw new BusinessException("KEYCLOAK_INTERNAL_ERROR");
        }
    }
}
