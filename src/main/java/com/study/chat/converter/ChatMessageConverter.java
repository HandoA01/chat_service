package com.study.chat.converter;

import com.study.chat.domain.ChatMessage;
import com.study.chat.domain.EmojiMessage;
import com.study.chat.domain.MediaMessage;
import com.study.chat.domain.TextMessage;
import com.study.chat.domain.enums.MessageType;
import com.study.chat.web.dto.ChatMessageResponseDTO;
import java.util.List;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;

public class ChatMessageConverter {

    public static ChatMessageResponseDTO.MessagePreviewDTO toMessagePreviewDTO(ChatMessage rawMessage) {
        // 답장 대상으로 먼저 참조된 메시지는 ChatMessage 프록시로 로딩되어 있을 수 있다.
        // 프록시는 자식 타입이 아니라 부모 타입을 상속하므로, 벗겨내지 않으면 아래 instanceof가 전부 빗나간다.
        ChatMessage message = (ChatMessage) Hibernate.unproxy(rawMessage);

        ChatMessageResponseDTO.MessagePreviewDTO.MessagePreviewDTOBuilder builder =
                ChatMessageResponseDTO.MessagePreviewDTO.builder()
                        .id(message.getId())
                        .roomId(message.getRoom().getId())
                        .senderId(message.getSender().getId())
                        .senderNickname(message.getSender().getNickname())
                        .parentMessageId(message.getParentMessage() == null
                                ? null : message.getParentMessage().getId())
                        .createdAt(message.getCreatedAt());

        // 조인 상속이라 실제 타입에 따라 채워지는 필드가 달라진다.
        if (message instanceof TextMessage textMessage) {
            builder.type(MessageType.TEXT)
                    .content(textMessage.getContent());
        } else if (message instanceof EmojiMessage emojiMessage) {
            builder.type(MessageType.EMOJI)
                    .emoticonId(emojiMessage.getEmoticon().getId())
                    .emoticonImageUrl(emojiMessage.getEmoticon().getImageUrl());
        } else if (message instanceof MediaMessage mediaMessage) {
            builder.type(MessageType.MEDIA)
                    .fileUrl(mediaMessage.getFileUrl())
                    .fileType(mediaMessage.getFileType())
                    .thumbnailUrl(mediaMessage.getThumbnailUrl());
        }

        return builder.build();
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
