package com.study.chat.web.controller;

import com.study.chat.apiPayload.ApiResponse;
import com.study.chat.apiPayload.code.status.SuccessStatus;
import com.study.chat.common.TempAuth;
import com.study.chat.converter.FriendConverter;
import com.study.chat.domain.Friend;
import com.study.chat.service.friend.FriendCommandService;
import com.study.chat.web.dto.FriendRequestDTO;
import com.study.chat.web.dto.FriendResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/friends")
@Tag(name = "친구", description = "친구 요청 및 목록 API")
public class FriendRestController {

    private final FriendCommandService friendCommandService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/requests")
    @Operation(summary = "친구 요청 API",
            description = "특정 회원에게 친구 요청(PENDING)을 보냅니다. 대상 존재 여부와 중복 요청 여부는 커스텀 어노테이션으로 검증합니다.")
    public ApiResponse<FriendResponseDTO.CreateResultDTO> request(
            @RequestBody @Valid FriendRequestDTO.CreateDTO request) {

        Friend friend = friendCommandService.requestFriend(TempAuth.CURRENT_USER_ID, request);
        return ApiResponse.of(SuccessStatus.FRIEND_REQUESTED, FriendConverter.toCreateResultDTO(friend));
    }
}
