package com.study.chat.service.chat;

import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.apiPayload.exception.handler.ChatRoomHandler;
import com.study.chat.domain.ChatMessage;
import com.study.chat.domain.ChatRoom;
import com.study.chat.repository.ChatMessageRepository;
import com.study.chat.repository.ChatParticipantRepository;
import com.study.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatQueryServiceImpl implements ChatQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Override
    public Page<ChatMessage> getMessagePage(Long roomId, Long userId, Integer page, Integer size) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus.ROOM_NOT_FOUND));

        if (!chatParticipantRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new ChatRoomHandler(ErrorStatus.ROOM_NOT_PARTICIPANT);
        }

        // 채팅은 최신 메시지부터 보여주므로 createdAt 내림차순으로 고정한다.
        Sort latestFirst = Sort.by(Sort.Direction.DESC, "createdAt");
        int pageSize = (size == null) ? DEFAULT_PAGE_SIZE : size;

        return chatMessageRepository.findAllByRoom(room, PageRequest.of(page, pageSize, latestFirst));
    }
}
