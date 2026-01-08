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
    
    @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Message> findByChannelIdOrderByCreatedAtDesc(@Param("channelId") Long channelId, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId AND m.isPinned = true AND m.isDeleted = false")
    List<Message> findPinnedMessagesByChannelId(@Param("channelId") Long channelId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.channel.id = :channelId AND m.isPinned = true")
    long countPinnedMessagesByChannelId(@Param("channelId") Long channelId);
    
    // Thread/Reply support
    @Query("SELECT m FROM Message m WHERE m.parentMessageId = :parentMessageId AND m.isDeleted = false ORDER BY m.createdAt ASC")
    List<Message> findRepliesByParentMessageId(@Param("parentMessageId") Long parentMessageId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.parentMessageId = :parentMessageId AND m.isDeleted = false")
    long countRepliesByParentMessageId(@Param("parentMessageId") Long parentMessageId);
}
