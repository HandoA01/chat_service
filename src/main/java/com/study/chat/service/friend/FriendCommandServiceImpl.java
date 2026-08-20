package com.study.chat.service.friend;

import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.apiPayload.exception.handler.FriendHandler;
import com.study.chat.apiPayload.exception.handler.UserHandler;
import com.study.chat.converter.FriendConverter;
import com.study.chat.domain.Friend;
import com.study.chat.domain.User;
import com.study.chat.repository.FriendRepository;
import com.study.chat.repository.UserRepository;
import com.study.chat.web.dto.FriendRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendCommandServiceImpl implements FriendCommandService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    @Override
    public Friend requestFriend(Long userId, FriendRequestDTO.CreateDTO request) {
        Long friendId = request.getFriendId();

        if (userId.equals(friendId)) {
            throw new FriendHandler(ErrorStatus.FRIEND_SELF_REQUEST);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        return friendRepository.save(FriendConverter.toFriend(user, friend));
    }
}
