package com.study.chat.service.chat;

import com.study.chat.domain.ChatMessage;
import org.springframework.data.domain.Page;

public interface ChatQueryService {

    Page<ChatMessage> getMessagePage(Long roomId, Long userId, Integer page, Integer size);
}
