package com.study.chat.web.dto;

import com.study.chat.domain.enums.UserStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinResultDTO {
        private Long id;
        private String email;
        private String nickname;
        private UserStatus status;
        private LocalDateTime createdAt;
    }
}
