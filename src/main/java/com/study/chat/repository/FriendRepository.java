package com.study.chat.repository;

import com.study.chat.domain.Friend;
import com.study.chat.domain.enums.FriendStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    /**
     * 내가 보낸 요청(user_id = 나)과 내가 받은 요청(friend_id = 나)을 함께 본다.
     * 친구 관계는 방향과 무관하게 "나와 엮인 행"이 전부 필요하기 때문이다.
     */
    @EntityGraph(attributePaths = {"user", "friend"})
    @Query("select f from Friend f where (f.user.id = :userId or f.friend.id = :userId) and f.status = :status")
    List<Friend> findAllByUserIdAndStatus(@Param("userId") Long userId, @Param("status") FriendStatus status);
}
