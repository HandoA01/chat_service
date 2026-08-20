package com.study.chat.service.user;

import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.apiPayload.exception.handler.UserHandler;
import com.study.chat.domain.User;
import com.study.chat.domain.enums.UserStatus;
import com.study.chat.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    @Override
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public boolean isEmailDuplicated(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
    }

    @Override
    public List<User> searchUsers(String keyword) {
        // 탈퇴한 회원은 검색 결과에 노출하지 않는다.
        return userRepository
                .findByStatusAndNicknameContainingIgnoreCaseOrStatusAndEmailContainingIgnoreCase(
                        UserStatus.ACTIVE, keyword, UserStatus.ACTIVE, keyword);
    }
}
