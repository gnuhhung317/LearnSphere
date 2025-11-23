package com.studyhub.user_service.repository;

import com.studyhub.user_service.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    
    /**
     * Find user profile by user ID Note: UserProfile uses @MapsId, so the ID is
     * the same as User's ID
    */

     
    Optional<UserProfile> findById(Long userId);
}
