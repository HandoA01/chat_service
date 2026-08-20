package com.study.chat.service.friend;

public interface FriendQueryService {

    boolean isAlreadyRequested(Long userId, Long friendId);
}
