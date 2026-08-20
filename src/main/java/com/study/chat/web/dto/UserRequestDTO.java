package com.study.chat.web.dto;

import com.study.chat.validation.annotation.NotDuplicateEmail;
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
    public static class JoinDTO {

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotDuplicateEmail
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
        private String password;

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 50, message = "닉네임은 50자를 넘을 수 없습니다.")
        private String nickname;
    }
}
