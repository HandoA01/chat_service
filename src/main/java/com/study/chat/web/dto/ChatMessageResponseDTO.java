package com.study.chat.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.study.chat.domain.enums.FileType;
import com.study.chat.domain.enums.MessageType;
import java.time.LocalDateTime;
import java.util.List;
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
    public static class MessagePreviewDTO {
        private Long id;
        private Long roomId;
        private Long senderId;
        private String senderNickname;
        private MessageType type;

        // TEXT
        private String content;

        // EMOJI
        private Long emoticonId;
        private String emoticonImageUrl;

        // MEDIA
        private String fileUrl;
        private FileType fileType;
        private String thumbnailUrl;

        // 답장인 경우에만 채워진다
        private Long parentMessageId;

        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessagePreviewListDTO {
        private List<MessagePreviewDTO> messages;
        private Integer listSize;
        private Integer totalPage;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }
}
