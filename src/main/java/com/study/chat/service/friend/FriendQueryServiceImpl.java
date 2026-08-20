package com.study.chat.service.friend;

import com.study.chat.domain.Friend;
import com.study.chat.domain.enums.FriendStatus;
import com.study.chat.repository.FriendRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendQueryServiceImpl implements FriendQueryService {

    private final FriendRepository friendRepository;

    @Override
    public boolean isAlreadyRequested(Long userId, Long friendId) {
        // 방향과 무관하게 이미 엮여 있으면 중복 요청으로 본다.
        return friendRepository.existsByUserIdAndFriendId(userId, friendId)
                || friendRepository.existsByUserIdAndFriendId(friendId, userId);
    }

    @Override
    public List<Friend> getFriends(Long userId, FriendStatus status) {
        return friendRepository.findAllByUserIdAndStatus(userId, status);
    }
}
