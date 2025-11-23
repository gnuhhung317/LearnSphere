package com.studyhub.user_service.service.impl;

import com.studyhub.user_service.dto.*;
import com.studyhub.user_service.entity.*;
import com.studyhub.user_service.mapper.UserMapper;
import com.studyhub.user_service.repository.LearningPathRepository;
import com.studyhub.user_service.repository.UserProfileRepository;
import com.studyhub.user_service.repository.UserStatsRepository;
import com.studyhub.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserStatsRepository userStatsRepository;

    @Mock
    private LearningPathRepository learningPathRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserProfileServiceImpl service;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId(42L);
        sampleUser.setKeycloakUserId("kc-42");
        sampleUser.setFullName("Test User");
        sampleUser.setEmail("test@example.com");
        sampleUser.setIsActive(true);
    }

    @Test
    void getUserProfileView_createsProfileAndStatsIfMissing() {
        // user has no profile or stats initially
        sampleUser.setUserProfile(null);
        sampleUser.setUserStats(null);

        when(userService.getUserByKeycloakId("kc-42")).thenReturn(sampleUser);

        UserProfile up = new UserProfile();
        up.setUser(sampleUser);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(up);

        UserStats stats = new UserStats();
        stats.setUser(sampleUser);
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(stats);

        UserProfileViewResponse expected = new UserProfileViewResponse();
        expected.setId(42L);
        when(userMapper.toProfileViewResponse(sampleUser)).thenReturn(expected);

        UserProfileViewResponse res = service.getUserProfileView("kc-42");

        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(42L);

        // verify repository saves happened
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        verify(userStatsRepository, times(1)).save(any(UserStats.class));
        verify(userMapper, times(1)).toProfileViewResponse(sampleUser);
    }

    @Test
    void updateMyProfile_updatesUserAndProfile() {
        // user with no profile
        sampleUser.setUserProfile(null);
        when(userService.getUserByKeycloakId("kc-42")).thenReturn(sampleUser);

        UserProfile savedProfile = new UserProfile();
        savedProfile.setUser(sampleUser);
        savedProfile.setBio("new bio");
        savedProfile.setAvatarUrl("http://localhost:9000/media/file-123");
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(savedProfile);

        UserProfileViewResponse expected = new UserProfileViewResponse();
        expected.setId(42L);
        when(userMapper.toProfileViewResponse(sampleUser)).thenReturn(expected);

        UpdateUserProfileRequest req = new UpdateUserProfileRequest();
        req.setFullName("Updated Name");
        req.setBio("new bio");
        req.setAvatarUrl("http://localhost:9000/media/file-123");

        UserProfileViewResponse out = service.updateUserProfile("kc-42", req);

        assertThat(out).isNotNull();
        assertThat(out.getId()).isEqualTo(42L);

        // verify profile saved and mapper called
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        verify(userMapper, times(1)).toProfileViewResponse(sampleUser);
        assertThat(sampleUser.getFullName()).isEqualTo("Updated Name");
        assertThat(sampleUser.getUserProfile().getAvatarUrl()).isEqualTo("http://localhost:9000/media/file-123");
    }

    @Test
    void createLearningPath_savesAndReturnsDto() {
        when(userService.getUserByKeycloakId("kc-42")).thenReturn(sampleUser);

        CreateLearningPathRequest req = new CreateLearningPathRequest();
        req.setName("Path A");
        req.setDescription("desc");
        req.setProgress(10);

        LearningPath entityFromMapper = new LearningPath();
        entityFromMapper.setName("Path A");
        when(userMapper.toLearningPathEntity(req)).thenReturn(entityFromMapper);

        LearningPath saved = new LearningPath();
        saved.setId(100L);
        saved.setName("Path A");
        saved.setProgress(10);
        saved.setUser(sampleUser);

        when(learningPathRepository.save(any(LearningPath.class))).thenReturn(saved);

        LearningPathDto dto = new LearningPathDto();
        dto.setId(100L);
        dto.setName("Path A");
        when(userMapper.toLearningPathDto(saved)).thenReturn(dto);

        LearningPathDto out = service.createLearningPath("kc-42", req);

        assertThat(out).isNotNull();
        assertThat(out.getId()).isEqualTo(100L);
        verify(learningPathRepository, times(1)).save(any(LearningPath.class));
        verify(userMapper, times(1)).toLearningPathDto(saved);
    }

    @Test
    void updateUserStats_updatesAndReturnsDto() {
        when(userService.getUserByKeycloakId("kc-42")).thenReturn(sampleUser);

        UserStats existing = new UserStats();
        existing.setUser(sampleUser);
        existing.setStudyHours(10);
        sampleUser.setUserStats(existing);

        UpdateStatsRequest req = new UpdateStatsRequest();
        req.setStudyHours(20);
        when(userMapper.toStatsDto(any(UserStats.class))).thenReturn(new UserStatsDto(20, 0, 1, 0, 0, 0, 40, 0));

        service.updateUserStats("kc-42", req);

        // verify update path and save
        verify(userStatsRepository, times(1)).save(any(UserStats.class));
        verify(userMapper, times(1)).toStatsDto(any(UserStats.class));
    }
}
