package com.study.chat.repository;

import com.study.chat.domain.ChatRoom;
import com.study.chat.domain.enums.RoomType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 두 사람만 있는 1:1 방을 찾는다. 명세서상 이미 존재하면 새로 만들지 않고 기존 방을 돌려준다.
     * 참여자가 정확히 2명이고 그 둘이 나와 상대인 방을 찾는 방식.
     */
    @Query("""
            select r from ChatRoom r
            where r.type = :type
              and (select count(p) from ChatParticipant p where p.room = r) = 2
              and exists (select 1 from ChatParticipant p1 where p1.room = r and p1.user.id = :userId)
              and exists (select 1 from ChatParticipant p2 where p2.room = r and p2.user.id = :otherId)
            """)
    Optional<ChatRoom> findSingleRoomBetween(@Param("type") RoomType type,
                                             @Param("userId") Long userId,
                                             @Param("otherId") Long otherId);
}
