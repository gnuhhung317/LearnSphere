package com.studyhub.chat_service.repository;

import com.studyhub.chat_service.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    @Query("SELECT r FROM Room r JOIN r.members m WHERE m.userId = :userId")
    List<Room> findRoomsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Room r WHERE r.isPublic = true")
    List<Room> findPublicRooms();
    
    Optional<Room> findByInviteCode(String inviteCode);
    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM RoomMember m WHERE m.room.id = :roomId AND m.userId = :userId")
    boolean existsMemberInRoom(@Param("roomId") Long roomId, @Param("userId") Long userId);
    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM RoomMember m WHERE m.room.id = :roomId AND m.userId = :userId AND m.isOwner = true")
    boolean isOwnerOfRoom(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
