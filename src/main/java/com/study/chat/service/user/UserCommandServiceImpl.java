package com.study.chat.service.user;

import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.apiPayload.exception.handler.UserHandler;
import com.study.chat.converter.UserConverter;
import com.study.chat.domain.User;
import com.study.chat.repository.UserRepository;
import com.study.chat.web.dto.UserRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User joinUser(UserRequestDTO.JoinDTO request) {
        // @NotDuplicateEmail이 1차로 걸러주지만, 동시 요청으로 통과할 수 있어 저장 직전에 한 번 더 확인한다.
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserHandler(ErrorStatus.USER_EMAIL_DUPLICATED);
        }

        User newUser = UserConverter.toUser(request, passwordEncoder.encode(request.getPassword()));
        return userRepository.save(newUser);
    }
}
