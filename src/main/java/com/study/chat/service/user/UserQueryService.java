package com.study.chat.service.user;

public interface UserQueryService {

    boolean existsById(Long userId);

    boolean isEmailDuplicated(String email);
}
