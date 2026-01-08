package com.studyhub.chat_service.repository;

import com.studyhub.chat_service.entity.Room;
import com.studyhub.chat_service.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r JOIN r.members m WHERE m.userId = :userId")
    List<Room> findRoomsByUserId(@Param("userId") String userId);

    @Query("SELECT r FROM Room r WHERE r.isPublic = true")
    List<Room> findPublicRooms();

    Optional<Room> findByInviteCode(String inviteCode);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM RoomMember m WHERE m.room.id = :roomId AND m.userId = :userId")
    boolean existsMemberInRoom(@Param("roomId") Long roomId, @Param("userId") String userId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM RoomMember m WHERE m.room.id = :roomId AND m.userId = :userId AND m.isOwner = true")
    boolean isOwnerOfRoom(@Param("roomId") Long roomId, @Param("userId") String userId);

    /**
     * Find existing DM room between two users DM rooms always have exactly 2
     * members
     */
    @Query("SELECT r FROM Room r "
            + "WHERE r.roomType = :roomType "
            + "AND (SELECT COUNT(m) FROM RoomMember m WHERE m.room.id = r.id) = 2 "
            + "AND EXISTS (SELECT 1 FROM RoomMember m1 WHERE m1.room.id = r.id AND m1.userId = :userId1) "
            + "AND EXISTS (SELECT 1 FROM RoomMember m2 WHERE m2.room.id = r.id AND m2.userId = :userId2)")
    Optional<Room> findDirectMessageRoom(
            @Param("roomType") RoomType roomType,
            @Param("userId1") String userId1,
            @Param("userId2") String userId2
    );
}
