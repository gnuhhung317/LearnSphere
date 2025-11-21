package com.studyhub.user_service.exception;

import com.studyhub.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND");
    }

    public UserNotFoundException(Long userId) {
        super("User with ID " + userId + " not found", "USER_NOT_FOUND");
    }

    public UserNotFoundException(String field, String value) {
        super("User with " + field + " '" + value + "' not found", "USER_NOT_FOUND");
    }
}
