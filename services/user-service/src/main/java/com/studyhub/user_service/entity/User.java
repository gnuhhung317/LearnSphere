package com.studyhub.user_service.entity;

import com.studyhub.common.constant.enums.SupportedLanguage;
import com.studyhub.common.constant.enums.Theme;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * User entity representing user profile data synced from Keycloak Passwords are
 * managed by Keycloak, not stored here
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "keycloak_user_id", unique = true, nullable = false, length = 255)
    private String keycloakUserId;

    @Column(name = "username", unique = true, length = 50)
    private String username;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(unique = true, nullable = false)
    @Email(message = "Email should be valid")
    private String email;

    @Column(name = "location", length = 100)
    private String location;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Theme theme = Theme.AUTO;

    @Column(name = "language", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SupportedLanguage language = SupportedLanguage.ENGLISH;

    /**
     * Notification preferences stored as JSONB
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notifications", columnDefinition = "jsonb")
    private Map<String, Boolean> notifications;

    /**
     * Accessibility settings stored as JSONB
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "accessibility", columnDefinition = "jsonb")
    private Map<String, Boolean> accessibility;

    /**
     * Privacy settings stored as JSONB
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "privacy", columnDefinition = "jsonb")
    private Map<String, Object> privacy;

    @Column(name = "status", length = 20)
    private String status = "active";

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserProfile userProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserStats userStats;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<LearningPath> learningPaths;
}
