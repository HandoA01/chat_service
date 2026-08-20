package com.study.chat.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.study.chat.domain.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "회원가입 결과")
    public static class JoinResultDTO {
        @Schema(description = "회원 ID", example = "1")
        private Long id;
        @Schema(description = "이메일", example = "hong@example.com")
        private String email;
        @Schema(description = "닉네임", example = "홍길동")
        private String nickname;
        @Schema(description = "회원 상태", example = "ACTIVE")
        private UserStatus status;
        @Schema(description = "가입 일시")
        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "로그인 결과")
    public static class LoginResultDTO {
        @Schema(description = "액세스 토큰")
        private String accessToken;
        @Schema(description = "토큰 타입", example = "Bearer")
        private String tokenType;
        @Schema(description = "만료까지 남은 초", example = "3600")
        private Long expiresIn;
        @Schema(description = "로그인한 회원 요약")
        private SummaryDTO user;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원 요약")
    public static class SummaryDTO {
        @Schema(description = "회원 ID", example = "1")
        private Long id;
        @Schema(description = "이메일", example = "hong@example.com")
        private String email;
        @Schema(description = "닉네임", example = "홍길동")
        private String nickname;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "프로필")
    public static class ProfileDTO {
        @Schema(description = "회원 ID", example = "1")
        private Long id;
        @Schema(description = "이메일", example = "hong@example.com")
        private String email;
        @Schema(description = "닉네임", example = "홍길동")
        private String nickname;
        @Schema(description = "프로필 이미지 URL", nullable = true)
        private String profileImageUrl;
        @Schema(description = "상태 메시지", nullable = true)
        private String statusMessage;
        @Schema(description = "회원 상태", example = "ACTIVE")
        private UserStatus status;
        @Schema(description = "가입 일시")
        private LocalDateTime createdAt;
        @Schema(description = "최종 수정 일시")
        private LocalDateTime updatedAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "유저 검색 결과 항목")
    public static class SearchResultDTO {
        @Schema(description = "회원 ID", example = "2")
        private Long id;
        @Schema(description = "이메일", example = "kim@example.com")
        private String email;
        @Schema(description = "닉네임", example = "김철수")
        private String nickname;
        @Schema(description = "프로필 이미지 URL", nullable = true)
        private String profileImageUrl;
        @Schema(description = "상태 메시지", nullable = true)
        private String statusMessage;
    }
}
