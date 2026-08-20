package com.study.chat.repository;

import com.study.chat.domain.ChatParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    Optional<ChatParticipant> findByRoomIdAndUserId(Long roomId, Long userId);

    long countByRoomId(Long roomId);

    List<ChatParticipant> findAllByRoomId(Long roomId);

    @EntityGraph(attributePaths = {"room"})
    List<ChatParticipant> findAllByUserId(Long userId);
}
