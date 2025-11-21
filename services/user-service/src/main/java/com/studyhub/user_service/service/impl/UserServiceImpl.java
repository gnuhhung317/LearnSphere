package com.studyhub.user_service.service.impl;

import com.studyhub.common.constant.enums.SupportedLanguage;
import com.studyhub.common.constant.enums.Theme;
import com.studyhub.user_service.dto.CreateUserRequest;
import com.studyhub.user_service.dto.UserResponse;
import com.studyhub.user_service.entity.User;
import com.studyhub.user_service.exception.UserAlreadyExistsException;
import com.studyhub.user_service.exception.UserNotFoundException;
import com.studyhub.user_service.mapper.UserMapper;
import com.studyhub.user_service.repository.UserRepository;
import com.studyhub.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service implementation for user operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        log.info("Creating user profile from Keycloak: {} ({})", createUserRequest.getEmail(), createUserRequest.getKeycloakUserId());

        // Check if user already exists by Keycloak ID
        boolean userExists = userRepository.existsByKeycloakUserIdOrEmail(createUserRequest.getKeycloakUserId(), createUserRequest.getEmail());

        if (userExists) {
            throw new UserAlreadyExistsException(createUserRequest.getEmail());
        }
        else {
            User user = new User();
            user.setKeycloakUserId(createUserRequest.getKeycloakUserId());
            user.setEmail(createUserRequest.getEmail());
            user.setFullName(createUserRequest.getFullName());
            user.setLanguage(createUserRequest.getLanguage()); // Default, will be updated from preferences
            user.setTheme(createUserRequest.getTheme()); // Default
//            user.setStatus();
            user.setIsVerified(false);
            user.setIsActive(true);
            user.setProfileVisibility("organization");

            log.info("Creating new user profile for: {}", createUserRequest.getEmail());
            user = userRepository.save(user);
            log.info("Successfully synced user: {} (ID: {})", createUserRequest.getEmail(), user.getId());
            return userMapper.toResponse(user);
        }
    }

    @Override
    public User getUserByKeycloakId(String keycloakUserId) {
        return null;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("email", email));
    }

    @Transactional
    public User updateUserPreferences(String keycloakUserId, Map<String, Object> preferences) {
        User user = getUserByKeycloakId(keycloakUserId);

        // Update preferences from registration data
        if (preferences.containsKey("language")) {
            String lang = (String) preferences.get("language");
            user.setLanguage("vi".equals(lang) ? SupportedLanguage.VIETNAMESE : SupportedLanguage.ENGLISH);
        }

        if (preferences.containsKey("theme")) {
            String theme = (String) preferences.get("theme");
            user.setTheme(Theme.valueOf(theme.toUpperCase()));
        }

        if (preferences.containsKey("notifications")) {
            user.setNotifications((Map<String, Boolean>) preferences.get("notifications"));
        }

        if (preferences.containsKey("accessibility")) {
            user.setAccessibility((Map<String, Boolean>) preferences.get("accessibility"));
        }

        if (preferences.containsKey("privacy")) {
            user.setPrivacy((Map<String, Object>) preferences.get("privacy"));

            // Extract profile visibility from privacy settings
            Map<String, Object> privacy = (Map<String, Object>) preferences.get("privacy");
            if (privacy.containsKey("profileVisibility")) {
                user.setProfileVisibility((String) privacy.get("profileVisibility"));
            }
        }

        return userRepository.save(user);
    }
}
