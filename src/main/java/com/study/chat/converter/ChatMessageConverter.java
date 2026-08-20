package com.study.chat.converter;

import com.study.chat.apiPayload.paging.CursorPageResponse;
import com.study.chat.domain.ChatMessage;
import com.study.chat.domain.EmojiMessage;
import com.study.chat.domain.MediaMessage;
import com.study.chat.domain.TextMessage;
import com.study.chat.domain.enums.MessageType;
import com.study.chat.web.dto.ChatMessageResponseDTO;
import java.util.List;
import org.hibernate.Hibernate;

public class ChatMessageConverter {

    public static ChatMessageResponseDTO.MessagePreviewDTO toMessagePreviewDTO(ChatMessage rawMessage) {
        // 답장 대상으로 먼저 참조된 메시지는 ChatMessage 프록시로 로딩되어 있을 수 있다.
        // 프록시는 자식 타입이 아니라 부모 타입을 상속하므로, 벗겨내지 않으면 아래 instanceof가 전부 빗나간다.
        ChatMessage message = unwrap(rawMessage);

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

    /**
     * 채팅방 목록의 "마지막 메시지" 자리에 쓸 한 줄 요약.
     * 이모티콘·미디어는 본문이 없으므로 종류 표시로 대체한다.
     */
    public static String toPreviewText(ChatMessage rawMessage) {
        ChatMessage message = unwrap(rawMessage);

        if (message instanceof TextMessage textMessage) {
            return textMessage.getContent();
        }
        if (message instanceof EmojiMessage) {
            return "(이모티콘)";
        }
        if (message instanceof MediaMessage mediaMessage) {
            return switch (mediaMessage.getFileType()) {
                case IMAGE -> "(사진)";
                case VIDEO -> "(동영상)";
                case AUDIO -> "(음성메시지)";
                case FILE -> "(파일)";
            };
        }
        return "";
    }

    public static CursorPageResponse<ChatMessageResponseDTO.MessagePreviewDTO> toCursorPage(
            List<ChatMessage> messages, int size, boolean hasNext) {

        List<ChatMessageResponseDTO.MessagePreviewDTO> content = messages.stream()
                .map(ChatMessageConverter::toMessagePreviewDTO)
                .toList();

        // 최신순이라 목록의 마지막 항목이 가장 오래된 메시지 = 다음 커서
        Long nextCursor = (hasNext && !content.isEmpty())
                ? content.get(content.size() - 1).getId() : null;

        return CursorPageResponse.of(content, size, hasNext, nextCursor);
    }

    private static ChatMessage unwrap(ChatMessage message) {
        return (ChatMessage) Hibernate.unproxy(message);
    }
}
