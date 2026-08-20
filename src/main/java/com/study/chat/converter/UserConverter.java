package com.study.chat.converter;

import com.study.chat.domain.User;
import com.study.chat.domain.enums.UserStatus;
import com.study.chat.web.dto.UserRequestDTO;
import com.study.chat.web.dto.UserResponseDTO;

public class UserConverter {

    public static User toUser(UserRequestDTO.JoinDTO request, String encodedPassword) {
        return User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .status(UserStatus.ACTIVE)
                .build();
    }

    public static UserResponseDTO.JoinResultDTO toJoinResultDTO(User user) {
        return UserResponseDTO.JoinResultDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
