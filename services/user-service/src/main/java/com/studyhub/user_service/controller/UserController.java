package com.studyhub.user_service.controller;

import com.studyhub.user_service.dto.*;
import com.studyhub.user_service.dto.request.KeycloakUserIdList;
import com.studyhub.user_service.service.UserProfileService;
import com.studyhub.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserProfileService userProfileService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is running!");
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello from User Service on port 8081!");
    }

    /**
     * Get comprehensive user profile (replaces old /me endpoint) Fetches full
     * profile from database instead of just JWT claims
     */
    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileViewResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        String keycloakUserId = requireSubject(jwt);
        log.info("Fetching profile for keycloakUserId: {}", keycloakUserId);
        UserProfileViewResponse profile = userProfileService.getUserProfileView(keycloakUserId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Get user profile by user ID (for service-to-service calls) This endpoint
     * is used by other services (e.g., chat-service) to fetch user info
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileViewResponse> getUserById(@PathVariable String userId) {
        log.info("Fetching profile for userId: {}", userId);
        UserProfileViewResponse profile = userProfileService.getUserProfileByKeycloakId(userId);
        return ResponseEntity.ok(profile);
    }
    @PostMapping("/basic/bulk")
    public ResponseEntity<List<UserInfo>> getBasicBulk(@RequestBody KeycloakUserIdList keycloakIds) {
        log.info("Fetching basic profile for keycloakIds: {}", keycloakIds);
        List<UserInfo> profiles = userService.getBasicBulk(keycloakIds.getUserIds());
        return ResponseEntity.ok(profiles);
    }
    @GetMapping("/basic/{userId}")
    public ResponseEntity<UserInfo> getBasic(@PathVariable String userId) {
        log.info("Fetching basic profile for userId: {}", userId);
        UserInfo profile = userService.getBasic(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Get user profile by Keycloak ID (for JWT-based service calls) This
     * endpoint maps Keycloak UUID to StudyHub user profile
     */
    @GetMapping("/keycloak/{keycloakId}")
    public ResponseEntity<UserProfileViewResponse> getUserByKeycloakId(@PathVariable String keycloakId) {
        log.info("Fetching profile for keycloakId: {}", keycloakId);
        UserProfileViewResponse profile = userProfileService.getUserProfileByKeycloakId(keycloakId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update user profile information
     */
    @PutMapping("/me/profile")
    public ResponseEntity<UserProfileViewResponse> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        String keycloakUserId = requireSubject(jwt);
        log.info("Updating profile for keycloakUserId: {}", keycloakUserId);
        UserProfileViewResponse updatedProfile = userProfileService.updateUserProfile(keycloakUserId, request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Update user statistics
     */
    @PatchMapping("/me/stats")
    public ResponseEntity<UserStatsDto> updateMyStats(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateStatsRequest request) {
        String keycloakUserId = requireSubject(jwt);
        log.info("Updating stats for keycloakUserId: {}", keycloakUserId);
        UserStatsDto stats = userProfileService.updateUserStats(keycloakUserId, request);
        return ResponseEntity.ok(stats);
    }

    /**
     * Update monthly learning goal
     */
    @PatchMapping("/me/stats/goal")
    public ResponseEntity<UserStatsDto> updateMonthlyGoal(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("goal") Integer monthlyGoal) {
        String keycloakUserId = requireSubject(jwt);
        log.info("Updating monthly goal for keycloakUserId: {} to {}", keycloakUserId, monthlyGoal);
        UserStatsDto stats = userProfileService.updateMonthlyGoal(keycloakUserId, monthlyGoal);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all learning paths for authenticated user
     */
    @GetMapping("/me/learning-paths")
    public ResponseEntity<List<LearningPathDto>> getMyLearningPaths(@AuthenticationPrincipal Jwt jwt) {
        String keycloakUserId = requireSubject(jwt);
        log.info("Fetching learning paths for keycloakUserId: {}", keycloakUserId);
        List<LearningPathDto> paths = userProfileService.getUserLearningPaths(keycloakUserId);
        return ResponseEntity.ok(paths);
    }

    /**
     * Create a new learning path
     */
    @PostMapping("/me/learning-paths")
    public ResponseEntity<LearningPathDto> createLearningPath(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateLearningPathRequest request) {
        String keycloakUserId = requireSubject(jwt);
        log.info("Creating learning path for keycloakUserId: {}", keycloakUserId);
        LearningPathDto path = userProfileService.createLearningPath(keycloakUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(path);
    }

    /**
     * Update learning path progress
     */
    @PatchMapping("/me/learning-paths/{pathId}/progress")
    public ResponseEntity<LearningPathDto> updateLearningPathProgress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long pathId,
            @RequestParam("progress") Integer progress) {
        String keycloakUserId = requireSubject(jwt);
        log.info("Updating learning path {} progress for keycloakUserId: {}", pathId, keycloakUserId);
        LearningPathDto path = userProfileService.updateLearningPathProgress(keycloakUserId, pathId, progress);
        return ResponseEntity.ok(path);
    }

    /**
     * Delete a learning path (soft delete)
     */
    @DeleteMapping("/me/learning-paths/{pathId}")
    public ResponseEntity<Void> deleteLearningPath(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long pathId) {
        String keycloakUserId = jwt.getSubject();
        log.info("Deleting learning path {} for keycloakUserId: {}", pathId, keycloakUserId);
        userProfileService.deleteLearningPath(keycloakUserId, pathId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Legacy endpoint - Get JWT claims Keep for backward compatibility
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        log.info("Getting current user info from JWT token");
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing JWT token");
        }
        return ResponseEntity.ok(jwt.getClaims());
    }

    // helper to validate JWT and extract subject, throws 401 if missing
    private String requireSubject(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid JWT token");
        }
        return jwt.getSubject();
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Search users
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserSummaryDto>> searchUsers(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("q") String query) {
        String keycloakUserId = requireSubject(jwt);
        log.info("Searching users with query: {}", query);
        List<UserSummaryDto> users = userService.searchUsers(query, keycloakUserId);
        return ResponseEntity.ok(users);
    }

    /**
     * Follow a user
     */
    @PostMapping("/{userId}/follow")
    public ResponseEntity<Void> followUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId) {
        String keycloakUserId = requireSubject(jwt);
        log.info("User {} following user {}", keycloakUserId, userId);
        userService.followUser(keycloakUserId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Unfollow a user
     */
    @DeleteMapping("/{userId}/unfollow")
    public ResponseEntity<Void> unfollowUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId) {
        String keycloakUserId = requireSubject(jwt);
        log.info("User {} unfollowing user {}", keycloakUserId, userId);
        userService.unfollowUser(keycloakUserId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get followers list for a user
     */
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserSummaryDto>> getFollowers(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId) {
        String keycloakUserId = requireSubject(jwt);
        log.info("Getting followers for user {}", userId);
        List<UserSummaryDto> followers = userService.getFollowers(userId, keycloakUserId);
        return ResponseEntity.ok(followers);
    }

    /**
     * Get following list for a user
     */
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserSummaryDto>> getFollowing(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId) {
        String keycloakUserId = requireSubject(jwt);
        log.info("Getting following for user {}", userId);
        List<UserSummaryDto> following = userService.getFollowing(userId, keycloakUserId);
        return ResponseEntity.ok(following);
    }
}
