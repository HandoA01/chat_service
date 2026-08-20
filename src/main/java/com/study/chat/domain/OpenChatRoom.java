package com.study.chat.domain;

import com.study.chat.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오픈채팅방에만 필요한 추가 정보. 1:1·그룹 방에는 방장·정원 개념이 없어
 * chat_rooms에 두면 전부 빈 값이 되므로 자식 테이블로 분리했다.
 * chat_room_id를 PK이자 FK로 써서 부모와 잇는다.
 */
@Entity
@Table(name = "open_chat_rooms")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OpenChatRoom extends BaseEntity {

    @Id
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "max_member_count")
    private Integer maxMemberCount;
}
