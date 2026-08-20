package com.study.chat.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.study.chat.domain.enums.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatRoomResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "채팅방 생성 결과")
    public static class CreateResultDTO {
        @Schema(description = "채팅방 ID", example = "100")
        private Long id;
        @Schema(description = "방 제목", nullable = true)
        private String title;
        @Schema(description = "방 종류", example = "GROUP")
        private RoomType type;
        @Schema(description = "참여자 목록")
        private List<ParticipantDTO> participants;
        @Schema(description = "생성 일시")
        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "채팅방 참여자")
    public static class ParticipantDTO {
        @Schema(description = "회원 ID", example = "1")
        private Long userId;
        @Schema(description = "닉네임", example = "홍길동")
        private String nickname;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "내 채팅방 목록 항목")
    public static class RoomPreviewDTO {
        @Schema(description = "채팅방 ID", example = "100")
        private Long id;
        @Schema(description = "방 제목", nullable = true)
        private String title;
        @Schema(description = "방 종류", example = "GROUP")
        private RoomType type;
        @Schema(description = "참여자 수", example = "4")
        private Long participantCount;
        @Schema(description = "가장 최근 메시지. 없으면 생략", nullable = true)
        private LastMessageDTO lastMessage;
        @Schema(description = "읽지 않은 메시지 수", example = "3")
        private Long unreadCount;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "채팅방의 마지막 메시지 요약")
    public static class LastMessageDTO {
        @Schema(description = "미리보기 문구. 이모티콘/미디어는 종류 표시로 대체된다", example = "회의는 3시에 시작합니다.")
        private String content;
        @Schema(description = "보낸 사람 ID", example = "2")
        private Long senderId;
        @Schema(description = "보낸 일시")
        private LocalDateTime createdAt;
    }
}
