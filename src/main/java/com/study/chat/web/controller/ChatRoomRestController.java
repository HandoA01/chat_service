package com.study.chat.web.controller;

import com.study.chat.apiPayload.ApiResponse;
import com.study.chat.apiPayload.code.status.SuccessStatus;
import com.study.chat.common.TempAuth;
import com.study.chat.converter.ChatMessageConverter;
import com.study.chat.domain.ChatMessage;
import com.study.chat.service.chat.ChatQueryService;
import com.study.chat.web.dto.ChatMessageResponseDTO;
import com.study.chat.validation.annotation.CheckPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/chat-rooms")
@Tag(name = "채팅방", description = "채팅방 및 채팅 내역 API")
public class ChatRoomRestController {

    private final ChatQueryService chatQueryService;

    @GetMapping("/{roomId}/messages")
    @Operation(summary = "채팅 내역 조회 API", description = "특정 채팅방의 메시지를 최신순으로 페이징 조회합니다.")
    @Parameter(name = "page", description = "조회할 페이지 번호, 1부터 시작합니다.")
    public ApiResponse<ChatMessageResponseDTO.MessagePreviewListDTO> getMessages(
            @PathVariable Long roomId,
            @CheckPage @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) Integer size) {

        // 클라이언트는 1부터, Pageable은 0부터 세므로 여기서 변환한다.
        Page<ChatMessage> messagePage =
                chatQueryService.getMessagePage(roomId, TempAuth.CURRENT_USER_ID, page - 1, size);

        return ApiResponse.of(SuccessStatus.MESSAGE_PAGE_OK,
                ChatMessageConverter.toMessagePreviewListDTO(messagePage));
    }
}
