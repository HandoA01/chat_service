package com.study.chat.repository;

import com.study.chat.domain.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
}
