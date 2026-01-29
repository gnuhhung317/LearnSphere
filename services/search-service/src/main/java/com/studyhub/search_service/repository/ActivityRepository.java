package com.studyhub.search_service.repository;

import com.studyhub.search_service.domain.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, String> {
    List<Activity> findByUserIdOrderByTimestampDesc(String userId);

    List<Activity> findTop10ByUserIdOrderByTimestampDesc(String userId);
}
