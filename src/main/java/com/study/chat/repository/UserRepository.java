package com.study.chat.repository;

import com.study.chat.domain.User;
import com.study.chat.domain.enums.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findByStatusAndNicknameContainingIgnoreCaseOrStatusAndEmailContainingIgnoreCase(
            UserStatus nicknameStatus, String nickname, UserStatus emailStatus, String email);
}
