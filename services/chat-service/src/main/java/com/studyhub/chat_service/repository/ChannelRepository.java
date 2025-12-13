package com.studyhub.chat_service.repository;

import com.studyhub.chat_service.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    
    List<Channel> findByRoomIdOrderByIdAsc(Long roomId);
    
    long countByRoomId(Long roomId);
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Channel c WHERE c.room.id = :roomId AND c.name = :name")
    boolean existsByRoomIdAndName(Long roomId, String name);
}
