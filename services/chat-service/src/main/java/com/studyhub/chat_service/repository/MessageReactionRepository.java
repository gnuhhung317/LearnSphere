package com.studyhub.chat_service.repository;

import com.studyhub.chat_service.entity.MessageReaction;
import com.studyhub.chat_service.entity.MessageReactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, MessageReactionId> {
    
    @Query("SELECT r FROM MessageReaction r WHERE r.message.id = :messageId")
    List<MessageReaction> findByMessageId(@Param("messageId") Long messageId);
    
    @Query("SELECT mr.id.emoji, COUNT(mr) FROM MessageReaction mr WHERE mr.id.messageId = :messageId GROUP BY mr.id.emoji")
    List<Object[]> countReactionsByMessageId(@Param("messageId") Long messageId);
    
    List<MessageReaction> findByIdMessageIdAndIdUserId(Long messageId, Long userId);
}
