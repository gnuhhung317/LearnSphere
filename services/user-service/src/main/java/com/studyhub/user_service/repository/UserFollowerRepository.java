package com.studyhub.user_service.repository;

import com.studyhub.user_service.entity.UserFollower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowerRepository extends JpaRepository<UserFollower, Long> {

    /**
     * Check if follower is following followed
     */
    boolean existsByFollowerUserIdAndFollowedUserId(Long followerId, Long followedId);

    /**
     * Find follow relationship
     */
    Optional<UserFollower> findByFollowerUserIdAndFollowedUserId(Long followerId, Long followedId);

    /**
     * Count followers of a user
     */
    @Query("SELECT COUNT(uf) FROM UserFollower uf WHERE uf.followed.userId = :userId")
    long countFollowersByUserId(@Param("userId") Long userId);

    /**
     * Count users that a user is following
     */
    @Query("SELECT COUNT(uf) FROM UserFollower uf WHERE uf.follower.userId = :userId")
    long countFollowingByUserId(@Param("userId") Long userId);

    /**
     * Get list of followers for a user
     */
    @Query("SELECT uf.follower FROM UserFollower uf WHERE uf.followed.userId = :userId")
    List<com.studyhub.user_service.entity.User> findFollowersByUserId(@Param("userId") Long userId);

    /**
     * Get list of users that a user is following
     */
    @Query("SELECT uf.followed FROM UserFollower uf WHERE uf.follower.userId = :userId")
    List<com.studyhub.user_service.entity.User> findFollowingByUserId(@Param("userId") Long userId);

    /**
     * Delete follow relationship
     */
    void deleteByFollowerUserIdAndFollowedUserId(Long followerId, Long followedId);
}
