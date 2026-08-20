package com.study.chat.web.dto;

import com.study.chat.domain.enums.FriendStatus;
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
    public static class CreateResultDTO {
        private Long id;
        private Long userId;
        private Long friendId;
        private FriendStatus status;
        private LocalDateTime createdAt;
    }
}
