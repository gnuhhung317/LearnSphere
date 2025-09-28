package com.studyhub.common.util;

/**
 * Common constants used across all microservices
 */
public final class Constants {

    private Constants() {
        // Private constructor to prevent instantiation
    }

    // HTTP Headers
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    // JWT Claims
    public static final String JWT_CLAIM_SUB = "sub";
    public static final String JWT_CLAIM_EMAIL = "email";
    public static final String JWT_CLAIM_NAME = "name";
    public static final String JWT_CLAIM_ROLES = "roles";

    // Error Codes
    public static final String ERROR_UNAUTHORIZED = "UNAUTHORIZED";
    public static final String ERROR_FORBIDDEN = "FORBIDDEN";
    public static final String ERROR_RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String ERROR_VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String ERROR_BUSINESS_LOGIC = "BUSINESS_ERROR";
    public static final String ERROR_INTERNAL_SERVER = "INTERNAL_SERVER_ERROR";

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    // Date/Time Formats
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";

    // Security
    public static final String BEARER_TOKEN_PREFIX = "Bearer ";
    public static final int TOKEN_EXPIRY_HOURS = 24;

    // File Upload
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif"};
    public static final String[] ALLOWED_VIDEO_TYPES = {"video/mp4", "video/avi", "video/mov"};

    // Cache Keys
    public static final String CACHE_USER_PREFIX = "user:";
    public static final String CACHE_SESSION_PREFIX = "session:";
    public static final int CACHE_DEFAULT_TTL_MINUTES = 30;
}
