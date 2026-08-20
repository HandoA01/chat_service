package com.study.chat.service.user;

import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.apiPayload.exception.handler.UserHandler;
import com.study.chat.converter.UserConverter;
import com.study.chat.domain.User;
import com.study.chat.domain.enums.UserStatus;
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

    @Override
    @Transactional(readOnly = true)
    public User login(UserRequestDTO.LoginDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                // 이메일이 없는 경우와 비밀번호가 틀린 경우를 구분해서 알려주면
                // 가입된 이메일인지 확인하는 통로가 되므로 같은 에러로 응답한다.
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_LOGIN_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserHandler(ErrorStatus.USER_LOGIN_FAILED);
        }
        if (user.getStatus() == UserStatus.DELETED) {
            throw new UserHandler(ErrorStatus.USER_ALREADY_WITHDRAWN);
        }

        return user;
    }

    @Override
    public User updateProfile(Long userId, UserRequestDTO.UpdateProfileDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 영속 상태이므로 변경 감지로 UPDATE가 나간다. save() 호출이 필요 없다.
        user.updateProfile(request.getNickname(), request.getProfileImageUrl(), request.getStatusMessage());
        return user;
    }
}
