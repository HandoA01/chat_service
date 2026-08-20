package com.study.chat.service.chat;

import com.study.chat.domain.ChatRoom;
import com.study.chat.web.dto.ChatRoomRequestDTO;

public interface ChatRoomCommandService {

    ChatRoom createRoom(Long userId, ChatRoomRequestDTO.CreateDTO request);

    void leaveRoom(Long userId, Long roomId);
}
