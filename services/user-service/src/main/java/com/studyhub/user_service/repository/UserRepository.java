package com.studyhub.user_service.repository;

import com.studyhub.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findAllByKeycloakUserIdIn(List<String> keycloakIds);

    boolean existsByKeycloakUserIdOrEmail(String keycloakUserId, String email);

    Optional<User> findByKeycloakUserId(String keycloakUserId);

    /**
     * Search users by username, fullName, or location
     */
    @Query("SELECT u FROM User u WHERE "
            + "LOWER(u.username) LIKE :searchPattern OR "
            + "LOWER(u.fullName) LIKE :searchPattern OR "
            + "LOWER(u.location) LIKE :searchPattern")
    List<User> searchUsers(@org.springframework.data.repository.query.Param("searchPattern") String searchPattern);
}
