package com.study.chat.service.friend;

import com.study.chat.domain.Friend;
import com.study.chat.domain.enums.FriendStatus;
import java.util.List;

public interface FriendQueryService {

    boolean isAlreadyRequested(Long userId, Long friendId);

    List<Friend> getFriends(Long userId, FriendStatus status);
}
