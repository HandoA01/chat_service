package com.study.chat.repository;

import com.study.chat.domain.ChatMessage;
import com.study.chat.domain.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 응답에 발신자 닉네임이 필요한데 sender가 LAZY라, 그냥 두면 메시지 수만큼
     * users 조회가 추가로 나간다(N+1). EntityGraph로 한 번에 같이 가져온다.
     */
    @EntityGraph(attributePaths = {"sender"})
    Page<ChatMessage> findAllByRoom(ChatRoom room, Pageable pageable);

    /** 커서 미지정: 최신부터 */
    @EntityGraph(attributePaths = {"sender"})
    List<ChatMessage> findByRoomIdOrderByIdDesc(Long roomId, Pageable pageable);

    /** 커서 지정: 해당 id보다 오래된 메시지 */
    @EntityGraph(attributePaths = {"sender"})
    List<ChatMessage> findByRoomIdAndIdLessThanOrderByIdDesc(Long roomId, Long cursor, Pageable pageable);

    @EntityGraph(attributePaths = {"sender"})
    Optional<ChatMessage> findFirstByRoomIdOrderByIdDesc(Long roomId);

    /** 안읽음 수: 마지막으로 읽은 메시지 이후로 남이 보낸 것 */
    @Query("""
            select count(m) from ChatMessage m
            where m.room.id = :roomId
              and m.id > :lastReadMessageId
              and m.sender.id <> :userId
            """)
    long countUnread(@Param("roomId") Long roomId,
                     @Param("lastReadMessageId") Long lastReadMessageId,
                     @Param("userId") Long userId);
}
