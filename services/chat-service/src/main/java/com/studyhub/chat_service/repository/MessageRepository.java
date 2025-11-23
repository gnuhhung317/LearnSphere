package com.studyhub.chat_service.repository;

import com.studyhub.chat_service.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE m.room.id = :roomId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Message> findByRoomIdOrderByCreatedAtDesc(@Param("roomId") Long roomId, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.room.id = :roomId AND m.isPinned = true AND m.isDeleted = false")
    List<Message> findPinnedMessagesByRoomId(@Param("roomId") Long roomId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.room.id = :roomId AND m.isPinned = true")
    long countPinnedMessagesByRoomId(@Param("roomId") Long roomId);
}
