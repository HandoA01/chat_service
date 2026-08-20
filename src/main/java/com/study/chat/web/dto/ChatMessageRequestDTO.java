package com.study.chat.web.dto;

import com.study.chat.domain.enums.FileType;
import com.study.chat.domain.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatMessageRequestDTO {

    /**
     * 발신자(senderId)는 서버가 STOMP 세션의 인증 정보에서 꺼내므로 payload에 없다.
     * type을 생략하면 TEXT로 본다. (명세서 5.3의 기본 형태)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "메시지 발행 payload")
    public static class SendDTO {

        @Schema(description = "메시지 종류. 생략 시 TEXT", example = "TEXT", nullable = true)
        private MessageType type;

        @Schema(description = "본문. TEXT일 때 필수", example = "안녕하세요! 회의 준비 되셨나요?", nullable = true)
        private String content;

        @Schema(description = "이모티콘 ID. EMOJI일 때 필수", example = "20", nullable = true)
        private Long emoticonId;

        @Schema(description = "파일 URL. MEDIA일 때 필수", nullable = true)
        private String fileUrl;

        @Schema(description = "파일 종류. MEDIA일 때 필수", example = "IMAGE", nullable = true)
        private FileType fileType;

        @Schema(description = "파일 크기(byte). MEDIA일 때 선택", example = "20480", nullable = true)
        private Long fileSize;

        @Schema(description = "썸네일 URL. MEDIA일 때 선택", nullable = true)
        private String thumbnailUrl;

        @Schema(description = "답장 대상 메시지 ID. 일반 메시지면 생략", example = "5020", nullable = true)
        private Long parentMessageId;

        public MessageType typeOrDefault() {
            return type == null ? MessageType.TEXT : type;
        }
    }
}
