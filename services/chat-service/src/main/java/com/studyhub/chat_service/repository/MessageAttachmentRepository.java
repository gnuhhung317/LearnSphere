package com.studyhub.chat_service.repository;

import com.studyhub.chat_service.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {
    
    @Query("SELECT a FROM MessageAttachment a WHERE a.message.id = :messageId")
    List<MessageAttachment> findByMessageId(@Param("messageId") Long messageId);
}
