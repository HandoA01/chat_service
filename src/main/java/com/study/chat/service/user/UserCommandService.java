package com.study.chat.service.user;

import com.study.chat.domain.User;
import com.study.chat.web.dto.UserRequestDTO;

public interface UserCommandService {

    User joinUser(UserRequestDTO.JoinDTO request);

    User login(UserRequestDTO.LoginDTO request);

    User updateProfile(Long userId, UserRequestDTO.UpdateProfileDTO request);
}
