package com.study.chat.service.chat;

import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.apiPayload.exception.handler.ChatRoomHandler;
import com.study.chat.domain.ChatMessage;
import com.study.chat.domain.ChatParticipant;
import com.study.chat.domain.ChatRoom;
import com.study.chat.repository.ChatMessageRepository;
import com.study.chat.repository.ChatParticipantRepository;
import com.study.chat.repository.ChatRoomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatQueryServiceImpl implements ChatQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Override
    public List<ChatMessage> getMessagesByCursor(Long roomId, Long userId, Long cursor, int size) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new ChatRoomHandler(ErrorStatus.ROOM_NOT_FOUND);
        }
        if (!chatParticipantRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new ChatRoomHandler(ErrorStatus.ROOM_NOT_PARTICIPANT);
        }

        // hasNext 판단을 위해 한 건 더 읽는다. 컨트롤러에서 잘라낸다.
        PageRequest limit = PageRequest.of(0, size + 1);

        return (cursor == null)
                ? chatMessageRepository.findByRoomIdOrderByIdDesc(roomId, limit)
                : chatMessageRepository.findByRoomIdAndIdLessThanOrderByIdDesc(roomId, cursor, limit);
    }

    @Override
    public List<ChatParticipant> getMyParticipations(Long userId) {
        return chatParticipantRepository.findAllByUserId(userId);
    }

    @Override
    public List<ChatParticipant> getParticipants(Long roomId) {
        return chatParticipantRepository.findAllByRoomId(roomId);
    }

    @Override
    public long countParticipants(Long roomId) {
        return chatParticipantRepository.countByRoomId(roomId);
    }

    @Override
    public ChatMessage getLastMessage(Long roomId) {
        return chatMessageRepository.findFirstByRoomIdOrderByIdDesc(roomId).orElse(null);
    }

    @Override
    public long countUnread(ChatParticipant participant) {
        // 한 번도 읽지 않았으면 0부터 센다.
        long lastRead = participant.getLastReadMessageId() == null ? 0L : participant.getLastReadMessageId();
        return chatMessageRepository.countUnread(
                participant.getRoom().getId(), lastRead, participant.getUser().getId());
    }

    @Override
    public ChatRoom getRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus.ROOM_NOT_FOUND));
    }
}
