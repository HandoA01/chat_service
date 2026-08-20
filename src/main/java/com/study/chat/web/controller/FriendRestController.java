package com.study.chat.web.controller;

import com.study.chat.apiPayload.ApiResponse;
import com.study.chat.apiPayload.code.status.SuccessStatus;
import com.study.chat.config.security.CustomUserDetails;
import com.study.chat.converter.FriendConverter;
import com.study.chat.domain.Friend;
import com.study.chat.domain.enums.FriendStatus;
import com.study.chat.service.friend.FriendCommandService;
import com.study.chat.service.friend.FriendQueryService;
import com.study.chat.web.dto.FriendRequestDTO;
import com.study.chat.web.dto.FriendResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/friends")
@Tag(name = "친구", description = "친구 요청 · 수락 · 목록")
public class FriendRestController {

    private final FriendCommandService friendCommandService;
    private final FriendQueryService friendQueryService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/requests")
    @Operation(summary = "친구 요청 API",
            description = "특정 회원에게 친구 요청(PENDING)을 보냅니다. 대상 존재 여부와 중복 요청 여부는 커스텀 어노테이션으로 검증합니다.")
    public ApiResponse<FriendResponseDTO.CreateResultDTO> request(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid FriendRequestDTO.CreateDTO request) {

        Friend friend = friendCommandService.requestFriend(principal.getUserId(), request);
        return ApiResponse.of(SuccessStatus.FRIEND_REQUESTED, FriendConverter.toCreateResultDTO(friend));
    }

    @PatchMapping("/requests/{requestId}/accept")
    @Operation(summary = "친구 요청 수락 API", description = "받은 친구 요청을 수락합니다. (PENDING → ACCEPTED)")
    public ApiResponse<FriendResponseDTO.CreateResultDTO> accept(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long requestId) {

        Friend friend = friendCommandService.acceptFriend(principal.getUserId(), requestId);
        return ApiResponse.of(SuccessStatus.FRIEND_ACCEPTED, FriendConverter.toCreateResultDTO(friend));
    }

    @GetMapping
    @Operation(summary = "친구 목록 조회 API", description = "수락된(ACCEPTED) 친구 목록을 조회합니다. status로 대기 중 요청도 볼 수 있습니다.")
    @Parameter(name = "status", description = "ACCEPTED(기본) 또는 PENDING")
    public ApiResponse<List<FriendResponseDTO.FriendPreviewDTO>> getFriends(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "ACCEPTED") FriendStatus status) {

        Long myId = principal.getUserId();
        List<FriendResponseDTO.FriendPreviewDTO> result =
                friendQueryService.getFriends(myId, status).stream()
                        .map(friend -> FriendConverter.toFriendPreviewDTO(friend, myId))
                        .toList();

        return ApiResponse.of(SuccessStatus.FRIEND_LIST_OK, result);
    }
}
