package com.study.chat.service.chat;

import com.study.chat.domain.ChatMessage;
import com.study.chat.domain.ChatParticipant;
import com.study.chat.domain.ChatRoom;
import java.util.List;

public interface ChatQueryService {

    /** 커서 기반 조회. size보다 1건 더 읽어 다음 페이지 존재 여부를 판단한다. */
    List<ChatMessage> getMessagesByCursor(Long roomId, Long userId, Long cursor, int size);

    List<ChatParticipant> getMyParticipations(Long userId);

    List<ChatParticipant> getParticipants(Long roomId);

    long countParticipants(Long roomId);

    ChatMessage getLastMessage(Long roomId);

    long countUnread(ChatParticipant participant);

    ChatRoom getRoom(Long roomId);
}
