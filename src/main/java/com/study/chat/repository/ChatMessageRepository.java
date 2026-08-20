package com.study.chat.repository;

import com.study.chat.domain.ChatMessage;
import com.study.chat.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findAllByRoom(ChatRoom room, Pageable pageable);
}
