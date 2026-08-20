package com.study.chat.service.chat;

import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.apiPayload.exception.handler.ChatRoomHandler;
import com.study.chat.apiPayload.exception.handler.UserHandler;
import com.study.chat.domain.ChatParticipant;
import com.study.chat.domain.ChatRoom;
import com.study.chat.domain.User;
import com.study.chat.domain.enums.RoomType;
import com.study.chat.repository.ChatParticipantRepository;
import com.study.chat.repository.ChatRoomRepository;
import com.study.chat.repository.UserRepository;
import com.study.chat.web.dto.ChatRoomRequestDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomCommandServiceImpl implements ChatRoomCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;

    @Override
    public ChatRoom createRoom(Long userId, ChatRoomRequestDTO.CreateDTO request) {
        if (request.getType() == RoomType.OPEN) {
            // 오픈채팅은 방장·정원 등 별도 정보가 필요해 이 API로는 만들지 않는다.
            throw new ChatRoomHandler(ErrorStatus.ROOM_TYPE_NOT_SUPPORTED);
        }

        // 본인 + 초대 대상. 중복 입력이 있어도 한 번만 들어가도록 Set으로 모은다.
        Set<Long> memberIds = new LinkedHashSet<>();
        memberIds.add(userId);
        memberIds.addAll(request.getParticipantIds());

        if (memberIds.size() < 2) {
            throw new ChatRoomHandler(ErrorStatus.ROOM_SELF_ONLY);
        }
        if (request.getType() == RoomType.SINGLE && memberIds.size() != 2) {
            throw new ChatRoomHandler(ErrorStatus.ROOM_SINGLE_NEEDS_ONE);
        }

        List<User> members = new ArrayList<>();
        for (Long memberId : memberIds) {
            members.add(userRepository.findById(memberId)
                    .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND)));
        }

        // 명세서 정책: 이미 두 사람의 1:1 방이 있으면 새로 만들지 않고 기존 방을 돌려준다.
        if (request.getType() == RoomType.SINGLE) {
            Long otherId = members.get(1).getId();
            var existing = chatRoomRepository.findSingleRoomBetween(RoomType.SINGLE, userId, otherId);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        ChatRoom room = chatRoomRepository.save(ChatRoom.builder()
                .type(request.getType())
                .title(request.getType() == RoomType.SINGLE ? null : request.getTitle())
                .build());

        for (User member : members) {
            chatParticipantRepository.save(ChatParticipant.builder()
                    .room(room)
                    .user(member)
                    .joinedAt(LocalDateTime.now())
                    .build());
        }

        return room;
    }

    @Override
    public void leaveRoom(Long userId, Long roomId) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new ChatRoomHandler(ErrorStatus.ROOM_NOT_FOUND);
        }

        ChatParticipant participant = chatParticipantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus.ROOM_NOT_PARTICIPANT));

        // 메시지는 지우지 않는다. 남은 사람들의 대화 맥락이 끊기면 안 되기 때문이다.
        chatParticipantRepository.delete(participant);
    }
}
