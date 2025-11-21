package com.studyhub.common.exception;

/**
 * Base exception class for all StudyHub custom exceptions Provides consistent
 * error handling across microservices
 */
public abstract class StudyHubException extends RuntimeException {

    private final String errorCode;

    protected StudyHubException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected StudyHubException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
