package com.study.chat.service.friend;

import com.study.chat.repository.FriendRepository;
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
        return friendRepository.existsByUserIdAndFriendId(userId, friendId);
    }
}
