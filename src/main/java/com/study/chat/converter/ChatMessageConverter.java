package com.study.chat.converter;

import com.study.chat.domain.ChatMessage;
import com.study.chat.web.dto.ChatMessageResponseDTO;
import java.util.List;
import org.springframework.data.domain.Page;

public class ChatMessageConverter {

    public static ChatMessageResponseDTO.MessagePreviewDTO toMessagePreviewDTO(ChatMessage message) {
        return ChatMessageResponseDTO.MessagePreviewDTO.builder()
                .id(message.getId())
                .roomId(message.getRoom().getId())
                .senderId(message.getSender().getId())
                .senderNickname(message.getSender().getNickname())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

    public static ChatMessageResponseDTO.MessagePreviewListDTO toMessagePreviewListDTO(Page<ChatMessage> messagePage) {
        List<ChatMessageResponseDTO.MessagePreviewDTO> messages = messagePage.stream()
                .map(ChatMessageConverter::toMessagePreviewDTO)
                .toList();

        return ChatMessageResponseDTO.MessagePreviewListDTO.builder()
                .messages(messages)
                .listSize(messages.size())
                .totalPage(messagePage.getTotalPages())
                .totalElements(messagePage.getTotalElements())
                .isFirst(messagePage.isFirst())
                .isLast(messagePage.isLast())
                .build();
    }
}
