package com.study.chat.repository;

import com.study.chat.domain.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
}
