package com.study.chat.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.study.chat.domain.enums.FileType;
import com.study.chat.domain.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatMessageResponseDTO {

    /**
     * 메시지 종류마다 채워지는 필드가 다르다.
     * 해당 없는 필드는 응답에서 아예 빠지도록 NON_NULL로 둔다.
     */
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "채팅 메시지. type에 따라 채워지는 필드가 다르다")
    public static class MessagePreviewDTO {

        @Schema(description = "메시지 ID", example = "5021")
        private Long id;

        @Schema(description = "채팅방 ID", example = "100")
        private Long roomId;

        @Schema(description = "보낸 회원 ID", example = "2")
        private Long senderId;

        @Schema(description = "보낸 회원 닉네임", example = "김철수")
        private String senderNickname;

        @Schema(description = "메시지 종류", example = "TEXT")
        private MessageType type;

        @Schema(description = "본문. TEXT일 때만", example = "회의는 3시에 시작합니다.", nullable = true)
        private String content;

        @Schema(description = "이모티콘 ID. EMOJI일 때만", example = "20", nullable = true)
        private Long emoticonId;

        @Schema(description = "이모티콘 이미지 URL. EMOJI일 때만", nullable = true)
        private String emoticonImageUrl;

        @Schema(description = "파일 URL. MEDIA일 때만", nullable = true)
        private String fileUrl;

        @Schema(description = "파일 종류. MEDIA일 때만", example = "IMAGE", nullable = true)
        private FileType fileType;

        @Schema(description = "썸네일 URL. MEDIA일 때만", nullable = true)
        private String thumbnailUrl;

        @Schema(description = "답장 대상 메시지 ID. 일반 메시지면 생략", example = "5020", nullable = true)
        private Long parentMessageId;

        @Schema(description = "보낸 일시")
        private LocalDateTime createdAt;
    }
}
