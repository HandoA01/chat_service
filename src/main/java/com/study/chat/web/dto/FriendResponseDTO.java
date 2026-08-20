package com.study.chat.web.dto;

import com.study.chat.domain.enums.FriendStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class FriendResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "친구 요청/수락 결과")
    public static class CreateResultDTO {
        @Schema(description = "친구관계 ID", example = "10")
        private Long id;
        @Schema(description = "요청을 보낸 회원 ID", example = "1")
        private Long userId;
        @Schema(description = "요청을 받은 회원 ID", example = "2")
        private Long friendId;
        @Schema(description = "상태", example = "PENDING")
        private FriendStatus status;
        @Schema(description = "생성 일시")
        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "친구 목록 항목")
    public static class FriendPreviewDTO {
        @Schema(description = "친구관계 ID", example = "10")
        private Long friendshipId;
        @Schema(description = "상대방 정보")
        private UserResponseDTO.SearchResultDTO user;
        @Schema(description = "상태", example = "ACCEPTED")
        private FriendStatus status;
        @Schema(description = "생성 일시")
        private LocalDateTime createdAt;
    }
}
