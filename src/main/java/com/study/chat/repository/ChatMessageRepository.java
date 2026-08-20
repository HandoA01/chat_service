package com.study.chat.repository;

import com.study.chat.domain.ChatMessage;
import com.study.chat.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 응답에 발신자 닉네임이 필요한데 sender가 LAZY라, 그냥 두면 메시지 수만큼
     * users 조회가 추가로 나간다(N+1). EntityGraph로 한 번에 같이 가져온다.
     */
    @EntityGraph(attributePaths = {"sender"})
    Page<ChatMessage> findAllByRoom(ChatRoom room, Pageable pageable);
}
