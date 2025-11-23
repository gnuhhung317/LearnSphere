package com.studyhub.chat_service.repository;

import com.studyhub.chat_service.entity.RoomMember;
import com.studyhub.chat_service.entity.RoomMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMember, RoomMemberId> {
    
    @Query("SELECT m FROM RoomMember m WHERE m.room.id = :roomId")
    List<RoomMember> findByRoomId(@Param("roomId") Long roomId);
    
    @Query("SELECT COUNT(m) FROM RoomMember m WHERE m.room.id = :roomId")
    long countByRoomId(@Param("roomId") Long roomId);
    
    @Query("SELECT m FROM RoomMember m WHERE m.userId = :userId")
    List<RoomMember> findByUserId(@Param("userId") Long userId);
}
