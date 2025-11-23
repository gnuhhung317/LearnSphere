package com.studyhub.user_service.service.impl;

import com.studyhub.common.exception.ResourceNotFoundException;
import com.studyhub.user_service.dto.*;
import com.studyhub.user_service.entity.LearningPath;
import com.studyhub.user_service.entity.User;
import com.studyhub.user_service.entity.UserProfile;
import com.studyhub.user_service.entity.UserStats;
import com.studyhub.user_service.mapper.UserMapper;
import com.studyhub.user_service.repository.LearningPathRepository;
import com.studyhub.user_service.repository.UserProfileRepository;
import com.studyhub.user_service.repository.UserStatsRepository;
import com.studyhub.user_service.service.UserProfileService;
import com.studyhub.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of UserProfileService
 */
@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserService userService;
    private final UserProfileRepository userProfileRepository;
    private final UserStatsRepository userStatsRepository;
    private final LearningPathRepository learningPathRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileViewResponse getUserProfileView(String keycloakUserId) {
        log.info("Fetching profile view for keycloakUserId: {}", keycloakUserId);

        User user = userService.getUserByKeycloakId(keycloakUserId);

        // Ensure UserProfile exists
        if (user.getUserProfile() == null) {
            UserProfile userProfile = new UserProfile();
            userProfile.setUser(user);
            userProfileRepository.save(userProfile);
            user.setUserProfile(userProfile);
        }

        // Ensure UserStats exists
        if (user.getUserStats() == null) {
            UserStats userStats = new UserStats();
            userStats.setUser(user);
            userStatsRepository.save(userStats);
            user.setUserStats(userStats);
        }

        return userMapper.toProfileViewResponse(user);
    }

    @Override
    @Transactional
    public UserProfileViewResponse updateUserProfile(String keycloakUserId, UpdateUserProfileRequest request) {
        log.info("Updating profile for keycloakUserId: {}", keycloakUserId);

        User user = userService.getUserByKeycloakId(keycloakUserId);

        // Update user fields
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }

        // Update or create profile
        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getTitle() != null) {
            profile.setTitle(request.getTitle());
        }
        if (request.getSocialLinks() != null) {
            profile.setSocialLinks(request.getSocialLinks());
        }

        userProfileRepository.save(profile);
        user.setUserProfile(profile);

        return userMapper.toProfileViewResponse(user);
    }

    @Override
    @Transactional
    public UserStatsDto updateUserStats(String keycloakUserId, UpdateStatsRequest request) {
        log.info("Updating stats for keycloakUserId: {}", keycloakUserId);

        User user = userService.getUserByKeycloakId(keycloakUserId);
        UserStats stats = user.getUserStats();

        if (stats == null) {
            stats = new UserStats();
            stats.setUser(user);
        }

        userMapper.updateUserStatsFromDto(request, stats);
        userStatsRepository.save(stats);

        return userMapper.toStatsDto(stats);
    }

    @Override
    @Transactional
    public UserStatsDto updateMonthlyGoal(String keycloakUserId, Integer monthlyGoal) {
        log.info("Updating monthly goal for keycloakUserId: {} to {}", keycloakUserId, monthlyGoal);

        User user = userService.getUserByKeycloakId(keycloakUserId);
        UserStats stats = user.getUserStats();

        if (stats == null) {
            stats = new UserStats();
            stats.setUser(user);
        }

        stats.setMonthlyGoal(monthlyGoal);
        userStatsRepository.save(stats);

        return userMapper.toStatsDto(stats);
    }

    @Override
    @Transactional
    public LearningPathDto createLearningPath(String keycloakUserId, CreateLearningPathRequest request) {
        log.info("Creating learning path for keycloakUserId: {}", keycloakUserId);

        User user = userService.getUserByKeycloakId(keycloakUserId);

        LearningPath learningPath = userMapper.toLearningPathEntity(request);
        learningPath.setUser(user);

        LearningPath saved = learningPathRepository.save(learningPath);
        return userMapper.toLearningPathDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathDto> getUserLearningPaths(String keycloakUserId) {
        log.info("Fetching learning paths for keycloakUserId: {}", keycloakUserId);

        User user = userService.getUserByKeycloakId(keycloakUserId);
        List<LearningPath> paths = learningPathRepository.findByUserIdAndIsActiveTrue(user.getUserId());

        return userMapper.toLearningPathDtoList(paths);
    }

    @Override
    @Transactional
    public LearningPathDto updateLearningPathProgress(String keycloakUserId, Long pathId, Integer progress) {
        log.info("Updating learning path {} progress to {} for keycloakUserId: {}", pathId, progress, keycloakUserId);

        User user = userService.getUserByKeycloakId(keycloakUserId);

        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("id", pathId.toString()));

        // Verify ownership
        if (!learningPath.getUser().getUserId().equals(user.getUserId())) {
            throw new ResourceNotFoundException("id", pathId.toString());
        }

        learningPath.setProgress(progress);
        LearningPath updated = learningPathRepository.save(learningPath);

        return userMapper.toLearningPathDto(updated);
    }

    @Override
    @Transactional
    public void deleteLearningPath(String keycloakUserId, Long pathId) {
        log.info("Deleting learning path {} for keycloakUserId: {}", pathId, keycloakUserId);

        User user = userService.getUserByKeycloakId(keycloakUserId);

        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("id", pathId.toString()));

        // Verify ownership
        if (!learningPath.getUser().getUserId().equals(user.getUserId())) {
            throw new ResourceNotFoundException("id", pathId.toString());
        }

        // Soft delete
        learningPath.setIsActive(false);
        learningPathRepository.save(learningPath);
    }
}
