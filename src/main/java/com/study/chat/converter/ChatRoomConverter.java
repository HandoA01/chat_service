package com.study.chat.converter;

import com.study.chat.domain.ChatMessage;
import com.study.chat.domain.ChatParticipant;
import com.study.chat.domain.ChatRoom;
import com.study.chat.web.dto.ChatRoomResponseDTO;
import java.util.List;

public class ChatRoomConverter {

    public static ChatRoomResponseDTO.CreateResultDTO toCreateResultDTO(ChatRoom room,
                                                                       List<ChatParticipant> participants) {
        List<ChatRoomResponseDTO.ParticipantDTO> participantDTOs = participants.stream()
                .map(p -> ChatRoomResponseDTO.ParticipantDTO.builder()
                        .userId(p.getUser().getId())
                        .nickname(p.getUser().getNickname())
                        .build())
                .toList();

        return ChatRoomResponseDTO.CreateResultDTO.builder()
                .id(room.getId())
                .title(room.getTitle())
                .type(room.getType())
                .participants(participantDTOs)
                .createdAt(room.getCreatedAt())
                .build();
    }

    public static ChatRoomResponseDTO.RoomPreviewDTO toRoomPreviewDTO(ChatRoom room,
                                                                     long participantCount,
                                                                     ChatMessage lastMessage,
                                                                     long unreadCount) {
        return ChatRoomResponseDTO.RoomPreviewDTO.builder()
                .id(room.getId())
                .title(room.getTitle())
                .type(room.getType())
                .participantCount(participantCount)
                .lastMessage(lastMessage == null ? null : ChatRoomResponseDTO.LastMessageDTO.builder()
                        .content(ChatMessageConverter.toPreviewText(lastMessage))
                        .senderId(lastMessage.getSender().getId())
                        .createdAt(lastMessage.getCreatedAt())
                        .build())
                .unreadCount(unreadCount)
                .build();
    }
}
