package com.studyhub.user_service.service.impl;

import com.studyhub.common.constant.enums.SupportedLanguage;
import com.studyhub.common.constant.enums.Theme;
import com.studyhub.common.exception.BusinessException;
import com.studyhub.user_service.dto.CreateUserRequest;
import com.studyhub.user_service.dto.UserResponse;
import com.studyhub.user_service.dto.UserSummaryDto;
import com.studyhub.user_service.entity.User;
import com.studyhub.user_service.entity.UserFollower;
import com.studyhub.user_service.exception.UserAlreadyExistsException;
import com.studyhub.user_service.exception.UserNotFoundException;
import com.studyhub.user_service.mapper.UserMapper;
import com.studyhub.user_service.repository.UserFollowerRepository;
import com.studyhub.user_service.repository.UserRepository;
import com.studyhub.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation for user operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserFollowerRepository userFollowerRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        log.info("Creating user profile from Keycloak: {} ({})", createUserRequest.getEmail(), createUserRequest.getKeycloakUserId());

        // Check if user already exists by Keycloak ID
        boolean userExists = userRepository.existsByKeycloakUserIdOrEmail(createUserRequest.getKeycloakUserId(), createUserRequest.getEmail());

        if (userExists) {
            throw new UserAlreadyExistsException(createUserRequest.getEmail());
        } else {
            User user = new User();
            user.setKeycloakUserId(createUserRequest.getKeycloakUserId());
            user.setEmail(createUserRequest.getEmail());
            user.setFullName(createUserRequest.getFullName());
            user.setLanguage(createUserRequest.getLanguage()); // Default, will be updated from preferences
            user.setTheme(createUserRequest.getTheme()); // Default
//            user.setStatus();
            user.setIsVerified(false);
            user.setIsActive(true);

            log.info("Creating new user profile for: {}", createUserRequest.getEmail());
            user = userRepository.save(user);
            log.info("Successfully synced user: {} (ID: {})", createUserRequest.getEmail(), user.getKeycloakUserId());
            return userMapper.toResponse(user);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByKeycloakId(String keycloakUserId) {
        return userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new UserNotFoundException("keycloakUserId", keycloakUserId));
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

        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void followUser(String followerKeycloakId, Long followedUserId) {
        User follower = getUserByKeycloakId(followerKeycloakId);
        User followed = getUserById(followedUserId);

        // Check if already following
        if (userFollowerRepository.existsByFollowerUserIdAndFollowedUserId(follower.getUserId(), followedUserId)) {
            throw new BusinessException("Already following this user");
        }

        // Cannot follow yourself
        if (follower.getUserId().equals(followedUserId)) {
            throw new BusinessException("Cannot follow yourself");
        }

        UserFollower userFollower = new UserFollower();
        userFollower.setFollower(follower);
        userFollower.setFollowed(followed);
        userFollowerRepository.save(userFollower);

        log.info("User {} followed user {}", follower.getUserId(), followedUserId);
    }

    @Override
    @Transactional
    public void unfollowUser(String followerKeycloakId, Long followedUserId) {
        User follower = getUserByKeycloakId(followerKeycloakId);

        if (!userFollowerRepository.existsByFollowerUserIdAndFollowedUserId(follower.getUserId(), followedUserId)) {
            throw new BusinessException("Not following this user");
        }

        userFollowerRepository.deleteByFollowerUserIdAndFollowedUserId(follower.getUserId(), followedUserId);
        log.info("User {} unfollowed user {}", follower.getUserId(), followedUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followedId) {
        return userFollowerRepository.existsByFollowerUserIdAndFollowedUserId(followerId, followedId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDto> searchUsers(String query, String currentUserKeycloakId) {
        User currentUser = getUserByKeycloakId(currentUserKeycloakId);

        // Search by username, fullName, or location
        String searchPattern = "%" + query.toLowerCase() + "%";
        List<User> users = userRepository.searchUsers(searchPattern);

        return users.stream()
                .map(user -> mapToUserSummary(user, currentUser.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getFollowers(Long userId, String currentUserKeycloakId) {
        User currentUser = getUserByKeycloakId(currentUserKeycloakId);
        List<User> followers = userFollowerRepository.findFollowersByUserId(userId);

        return followers.stream()
                .map(user -> mapToUserSummary(user, currentUser.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getFollowing(Long userId, String currentUserKeycloakId) {
        User currentUser = getUserByKeycloakId(currentUserKeycloakId);
        List<User> following = userFollowerRepository.findFollowingByUserId(userId);

        return following.stream()
                .map(user -> mapToUserSummary(user, currentUser.getUserId()))
                .collect(Collectors.toList());
    }

    private UserSummaryDto mapToUserSummary(User user, Long currentUserId) {
        UserSummaryDto dto = new UserSummaryDto();
        dto.setId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setBio(user.getUserProfile() != null ? user.getUserProfile().getBio() : null);
        dto.setAvatarUrl(user.getUserProfile() != null ? user.getUserProfile().getAvatarUrl() : null);
        dto.setLocation(user.getLocation());
        dto.setFollowersCount((int) userFollowerRepository.countFollowersByUserId(user.getUserId()));
        dto.setFollowingCount((int) userFollowerRepository.countFollowingByUserId(user.getUserId()));
        dto.setIsFollowing(userFollowerRepository.existsByFollowerUserIdAndFollowedUserId(currentUserId, user.getUserId()));
        dto.setJoinedAt(user.getCreatedAt());
        return dto;
    }
}
