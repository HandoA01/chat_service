package com.study.chat.web.dto;

import com.study.chat.validation.annotation.NotDuplicateEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UserRequestDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "회원가입 요청")
    public static class JoinDTO {

        @Schema(description = "이메일", example = "hong@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotDuplicateEmail
        private String email;

        @Schema(description = "비밀번호 (8~20자)", example = "password1234")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
        private String password;

        @Schema(description = "닉네임", example = "홍길동")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 50, message = "닉네임은 50자를 넘을 수 없습니다.")
        private String nickname;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "로그인 요청")
    public static class LoginDTO {

        @Schema(description = "이메일", example = "hong@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @Schema(description = "비밀번호", example = "password1234")
        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "프로필 수정 요청 (부분 수정, 보낸 필드만 반영)")
    public static class UpdateProfileDTO {

        @Schema(description = "닉네임", example = "새로운닉네임", nullable = true)
        @Size(min = 1, max = 50, message = "닉네임은 1자 이상 50자 이하여야 합니다.")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", nullable = true)
        @Size(max = 255, message = "프로필 이미지 URL은 255자를 넘을 수 없습니다.")
        private String profileImageUrl;

        @Schema(description = "상태 메시지", example = "오늘도 화이팅", nullable = true)
        @Size(max = 255, message = "상태 메시지는 255자를 넘을 수 없습니다.")
        private String statusMessage;
    }
}
