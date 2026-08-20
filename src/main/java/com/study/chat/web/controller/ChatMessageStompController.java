package com.study.chat.web.controller;

import com.study.chat.apiPayload.ApiResponse;
import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.apiPayload.exception.GeneralException;
import com.study.chat.config.security.CustomUserDetails;
import com.study.chat.converter.ChatMessageConverter;
import com.study.chat.domain.ChatMessage;
import com.study.chat.service.chat.ChatMessageCommandService;
import com.study.chat.web.dto.ChatMessageRequestDTO;
import com.study.chat.web.dto.ChatMessageResponseDTO;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

/**
 * 명세서 5장. /pub으로 받은 메시지를 저장하고 /sub 구독자에게 브로드캐스트한다.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageStompController {

    private final ChatMessageCommandService chatMessageCommandService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat-rooms/{roomId}/messages")
    public void sendMessage(@DestinationVariable Long roomId,
                            @Payload ChatMessageRequestDTO.SendDTO request,
                            Principal principal) {

        Long senderId = extractUserId(principal);
        ChatMessage saved = chatMessageCommandService.sendMessage(roomId, senderId, request);

        ChatMessageResponseDTO.MessagePreviewDTO payload =
                ChatMessageConverter.toMessagePreviewDTO(saved);

        messagingTemplate.convertAndSend("/sub/chat-rooms/" + roomId, payload);
    }

    /**
     * STOMP에는 HTTP 상태 코드가 없다. 에러는 발신자 개인 큐로만 돌려주고
     * 구독자 전체에게는 브로드캐스트하지 않는다. 본문 형식은 REST와 동일하게 맞춘다.
     */
    @MessageExceptionHandler(GeneralException.class)
    @SendToUser("/sub/errors")
    public ApiResponse<Object> handleGeneralException(GeneralException e) {
        var reason = e.getErrorReasonHttpStatus();
        return ApiResponse.onFailure(reason.getCode(), reason.getMessage(), null);
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/sub/errors")
    public ApiResponse<Object> handleException(Exception e) {
        log.error("STOMP 처리 중 예상하지 못한 예외", e);
        return ApiResponse.onFailure(ErrorStatus._INTERNAL_SERVER_ERROR.getCode(),
                ErrorStatus._INTERNAL_SERVER_ERROR.getMessage(), null);
    }

    private Long extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken token
                && token.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUserId();
        }
        throw new GeneralException(ErrorStatus._UNAUTHORIZED);
    }
}
