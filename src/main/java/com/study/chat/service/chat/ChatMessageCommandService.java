package com.study.chat.service.chat;

import com.study.chat.domain.ChatMessage;
import com.study.chat.web.dto.ChatMessageRequestDTO;

public interface ChatMessageCommandService {

    ChatMessage sendMessage(Long roomId, Long senderId, ChatMessageRequestDTO.SendDTO request);
}
