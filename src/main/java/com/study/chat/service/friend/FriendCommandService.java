package com.study.chat.service.friend;

import com.study.chat.domain.Friend;
import com.study.chat.web.dto.FriendRequestDTO;

public interface FriendCommandService {

    Friend requestFriend(Long userId, FriendRequestDTO.CreateDTO request);

    Friend acceptFriend(Long userId, Long requestId);
}
