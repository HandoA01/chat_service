package com.study.chat.converter;

import com.study.chat.domain.Friend;
import com.study.chat.domain.User;
import com.study.chat.domain.enums.FriendStatus;
import com.study.chat.web.dto.FriendResponseDTO;

public class FriendConverter {

    public static Friend toFriend(User user, User friend) {
        return Friend.builder()
                .user(user)
                .friend(friend)
                .status(FriendStatus.PENDING)
                .build();
    }

    public static FriendResponseDTO.CreateResultDTO toCreateResultDTO(Friend friend) {
        return FriendResponseDTO.CreateResultDTO.builder()
                .id(friend.getId())
                .userId(friend.getUser().getId())
                .friendId(friend.getFriend().getId())
                .status(friend.getStatus())
                .createdAt(friend.getCreatedAt())
                .build();
    }
}
