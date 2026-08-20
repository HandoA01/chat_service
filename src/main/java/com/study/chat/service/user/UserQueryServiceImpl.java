package com.study.chat.service.user;

import com.study.chat.repository.UserRepository;
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
}
