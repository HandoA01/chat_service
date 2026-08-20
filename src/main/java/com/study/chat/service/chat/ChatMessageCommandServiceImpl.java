package com.study.chat.service.chat;

import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.apiPayload.exception.handler.ChatMessageHandler;
import com.study.chat.apiPayload.exception.handler.ChatRoomHandler;
import com.study.chat.apiPayload.exception.handler.UserHandler;
import com.study.chat.domain.ChatMessage;
import com.study.chat.domain.ChatParticipant;
import com.study.chat.domain.ChatRoom;
import com.study.chat.domain.EmojiMessage;
import com.study.chat.domain.Emoticon;
import com.study.chat.domain.MediaMessage;
import com.study.chat.domain.TextMessage;
import com.study.chat.domain.User;
import com.study.chat.repository.ChatMessageRepository;
import com.study.chat.repository.ChatParticipantRepository;
import com.study.chat.repository.ChatRoomRepository;
import com.study.chat.repository.EmoticonRepository;
import com.study.chat.repository.UserEmoticonRepository;
import com.study.chat.repository.UserRepository;
import com.study.chat.web.dto.ChatMessageRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메시지 저장. 조인 상속의 쓰기 경로로, type에 따라 어떤 자식 엔티티로 만들지 여기서 분기한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageCommandServiceImpl implements ChatMessageCommandService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;
    private final EmoticonRepository emoticonRepository;
    private final UserEmoticonRepository userEmoticonRepository;

    @Override
    public ChatMessage sendMessage(Long roomId, Long senderId, ChatMessageRequestDTO.SendDTO request) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus.ROOM_NOT_FOUND));

        ChatParticipant participant = chatParticipantRepository.findByRoomIdAndUserId(roomId, senderId)
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus.ROOM_NOT_PARTICIPANT));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        ChatMessage parent = resolveParent(request.getParentMessageId(), roomId);

        ChatMessage message = switch (request.typeOrDefault()) {
            case TEXT -> buildText(room, sender, parent, request);
            case EMOJI -> buildEmoji(room, sender, parent, request, senderId);
            case MEDIA -> buildMedia(room, sender, parent, request);
        };

        ChatMessage saved = chatMessageRepository.save(message);

        // 자기가 보낸 메시지는 읽은 것으로 본다. 안 하면 본인 메시지가 안읽음으로 잡힌다.
        participant.updateLastReadMessage(saved.getId());

        return saved;
    }

    private ChatMessage resolveParent(Long parentMessageId, Long roomId) {
        if (parentMessageId == null) {
            return null;
        }
        ChatMessage parent = chatMessageRepository.findById(parentMessageId)
                .orElseThrow(() -> new ChatMessageHandler(ErrorStatus.MESSAGE_NOT_FOUND));

        // 다른 방의 메시지에 답장하면 대화 맥락이 깨지므로 막는다.
        if (!parent.getRoom().getId().equals(roomId)) {
            throw new ChatMessageHandler(ErrorStatus.MESSAGE_PARENT_NOT_IN_ROOM);
        }
        return parent;
    }

    private ChatMessage buildText(ChatRoom room, User sender, ChatMessage parent,
                                  ChatMessageRequestDTO.SendDTO request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ChatMessageHandler(ErrorStatus.MESSAGE_CONTENT_REQUIRED);
        }
        return TextMessage.builder()
                .room(room).sender(sender).parentMessage(parent)
                .content(request.getContent())
                .build();
    }

    private ChatMessage buildEmoji(ChatRoom room, User sender, ChatMessage parent,
                                   ChatMessageRequestDTO.SendDTO request, Long senderId) {
        if (request.getEmoticonId() == null) {
            throw new ChatMessageHandler(ErrorStatus.MESSAGE_EMOTICON_REQUIRED);
        }
        Emoticon emoticon = emoticonRepository.findById(request.getEmoticonId())
                .orElseThrow(() -> new ChatMessageHandler(ErrorStatus.EMOTICON_NOT_FOUND));

        // user_emoticons가 소유 검증의 근거다. 사지 않은 이모티콘은 보낼 수 없다.
        if (!userEmoticonRepository.existsByUserIdAndPackId(senderId, emoticon.getPack().getId())) {
            throw new ChatMessageHandler(ErrorStatus.MESSAGE_EMOTICON_NOT_OWNED);
        }

        return EmojiMessage.builder()
                .room(room).sender(sender).parentMessage(parent)
                .emoticon(emoticon)
                .build();
    }

    private ChatMessage buildMedia(ChatRoom room, User sender, ChatMessage parent,
                                   ChatMessageRequestDTO.SendDTO request) {
        if (request.getFileUrl() == null || request.getFileUrl().isBlank() || request.getFileType() == null) {
            throw new ChatMessageHandler(ErrorStatus.MESSAGE_FILE_REQUIRED);
        }
        return MediaMessage.builder()
                .room(room).sender(sender).parentMessage(parent)
                .fileUrl(request.getFileUrl())
                .fileType(request.getFileType())
                .fileSize(request.getFileSize())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();
    }
}
