package com.study.chat.web.controller;

import com.study.chat.apiPayload.ApiResponse;
import com.study.chat.apiPayload.code.status.SuccessStatus;
import com.study.chat.apiPayload.paging.CursorPageResponse;
import com.study.chat.config.security.CustomUserDetails;
import com.study.chat.converter.ChatMessageConverter;
import com.study.chat.converter.ChatRoomConverter;
import com.study.chat.domain.ChatMessage;
import com.study.chat.domain.ChatParticipant;
import com.study.chat.domain.ChatRoom;
import com.study.chat.service.chat.ChatQueryService;
import com.study.chat.service.chat.ChatRoomCommandService;
import com.study.chat.web.dto.ChatMessageResponseDTO;
import com.study.chat.web.dto.ChatRoomRequestDTO;
import com.study.chat.web.dto.ChatRoomResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/chat-rooms")
@Tag(name = "채팅방", description = "채팅방 생성 · 목록 · 나가기 및 채팅 내역")
public class ChatRoomRestController {

    private final ChatRoomCommandService chatRoomCommandService;
    private final ChatQueryService chatQueryService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "채팅방 생성 API",
            description = "1:1(SINGLE) 또는 그룹(GROUP) 채팅방을 만듭니다. 생성자는 자동으로 참여자에 포함되며, "
                    + "이미 존재하는 1:1 방이 있으면 기존 방을 반환합니다.")
    public ApiResponse<ChatRoomResponseDTO.CreateResultDTO> createRoom(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid ChatRoomRequestDTO.CreateDTO request) {

        ChatRoom room = chatRoomCommandService.createRoom(principal.getUserId(), request);
        List<ChatParticipant> participants = chatQueryService.getParticipants(room.getId());

        return ApiResponse.of(SuccessStatus.ROOM_CREATED,
                ChatRoomConverter.toCreateResultDTO(room, participants));
    }

    @GetMapping
    @Operation(summary = "내 채팅방 목록 조회 API",
            description = "참여 중인 채팅방을 최근 메시지·안읽음 수와 함께 조회합니다.")
    public ApiResponse<List<ChatRoomResponseDTO.RoomPreviewDTO>> getMyRooms(
            @AuthenticationPrincipal CustomUserDetails principal) {

        List<ChatRoomResponseDTO.RoomPreviewDTO> result = new ArrayList<>();

        for (ChatParticipant participant : chatQueryService.getMyParticipations(principal.getUserId())) {
            ChatRoom room = participant.getRoom();
            result.add(ChatRoomConverter.toRoomPreviewDTO(
                    room,
                    chatQueryService.countParticipants(room.getId()),
                    chatQueryService.getLastMessage(room.getId()),
                    chatQueryService.countUnread(participant)));
        }

        return ApiResponse.of(SuccessStatus.ROOM_LIST_OK, result);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{roomId}/participants/me")
    @Operation(summary = "채팅방 나가기 API",
            description = "채팅방에서 나갑니다. 이미 보낸 메시지는 남은 참여자를 위해 삭제하지 않습니다.")
    public void leaveRoom(@AuthenticationPrincipal CustomUserDetails principal,
                          @PathVariable Long roomId) {
        chatRoomCommandService.leaveRoom(principal.getUserId(), roomId);
    }

    @GetMapping("/{roomId}/messages")
    @Operation(summary = "채팅 내역 조회 API",
            description = "채팅방의 메시지를 최신순으로 커서 기반 페이징 조회합니다. "
                    + "cursor를 비우면 최신부터, 응답의 nextCursor를 다음 요청에 넣으면 더 과거를 읽습니다.")
    @Parameter(name = "cursor", description = "이 메시지 ID보다 오래된 메시지를 조회. 미지정 시 최신부터")
    @Parameter(name = "size", description = "페이지 크기 (기본 20, 최대 100)")
    public ApiResponse<CursorPageResponse<ChatMessageResponseDTO.MessagePreviewDTO>> getMessages(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "MESSAGE_SIZE_INVALID")
            @Max(value = 100, message = "MESSAGE_SIZE_INVALID") Integer size) {

        List<ChatMessage> found =
                chatQueryService.getMessagesByCursor(roomId, principal.getUserId(), cursor, size);

        // size + 1건을 읽어왔으므로, 초과분이 있으면 다음 페이지가 있다는 뜻이다.
        boolean hasNext = found.size() > size;
        List<ChatMessage> page = hasNext ? found.subList(0, size) : found;

        return ApiResponse.of(SuccessStatus.MESSAGE_PAGE_OK,
                ChatMessageConverter.toCursorPage(page, size, hasNext));
    }
}
