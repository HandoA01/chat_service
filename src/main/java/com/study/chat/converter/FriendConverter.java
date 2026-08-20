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

    /**
     * 친구 목록은 "나와 엮인 행"을 조회하므로, 내가 보낸 쪽인지 받은 쪽인지에 따라
     * 상대방이 user일 수도 friend일 수도 있다. 응답에는 항상 상대방을 담는다.
     */
    public static FriendResponseDTO.FriendPreviewDTO toFriendPreviewDTO(Friend friend, Long myId) {
        User counterpart = friend.getUser().getId().equals(myId) ? friend.getFriend() : friend.getUser();

        return FriendResponseDTO.FriendPreviewDTO.builder()
                .friendshipId(friend.getId())
                .user(UserConverter.toSearchResultDTO(counterpart))
                .status(friend.getStatus())
                .createdAt(friend.getCreatedAt())
                .build();
    }
}
