package com.studyhub.user_service.exception;

import com.studyhub.common.exception.BusinessException;

/**
 * Exception thrown when attempting to create a user that already exists
 */
public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String message) {
        super(message, "USER_ALREADY_EXISTS");
    }

    public UserAlreadyExistsException(String email, String keycloakUserId) {
        super("User with email '" + email + "' or Keycloak ID '" + keycloakUserId + "' already exists",
                "USER_ALREADY_EXISTS");
    }
}
