package com.study.chat.repository;

import com.study.chat.domain.UserEmoticon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEmoticonRepository extends JpaRepository<UserEmoticon, Long> {

    boolean existsByUserIdAndPackId(Long userId, Long packId);
}
