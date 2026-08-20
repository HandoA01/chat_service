package com.study.chat.web.dto;

import com.study.chat.validation.annotation.ExistUser;
import com.study.chat.validation.annotation.NotAlreadyFriend;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class FriendRequestDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateDTO {

        @NotNull(message = "친구 요청 대상 ID는 필수입니다.")
        @ExistUser
        @NotAlreadyFriend
        private Long friendId;
    }
}
