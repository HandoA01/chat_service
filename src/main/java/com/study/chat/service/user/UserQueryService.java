package com.study.chat.service.user;

import com.study.chat.domain.User;
import java.util.List;

public interface UserQueryService {

    boolean existsById(Long userId);

    boolean isEmailDuplicated(String email);

    User getUser(Long userId);

    List<User> searchUsers(String keyword);
}
