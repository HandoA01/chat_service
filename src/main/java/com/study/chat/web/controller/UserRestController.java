package com.study.chat.web.controller;

import com.study.chat.apiPayload.ApiResponse;
import com.study.chat.apiPayload.code.status.SuccessStatus;
import com.study.chat.converter.UserConverter;
import com.study.chat.domain.User;
import com.study.chat.service.user.UserCommandService;
import com.study.chat.web.dto.UserRequestDTO;
import com.study.chat.web.dto.UserResponseDTO;
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
@RequestMapping("/api/auth")
@Tag(name = "회원", description = "회원 가입 및 인증 API")
public class UserRestController {

    private final UserCommandService userCommandService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    @Operation(summary = "회원가입 API", description = "이메일/비밀번호/닉네임으로 신규 회원을 등록합니다.")
    public ApiResponse<UserResponseDTO.JoinResultDTO> join(@RequestBody @Valid UserRequestDTO.JoinDTO request) {
        User user = userCommandService.joinUser(request);
        return ApiResponse.of(SuccessStatus.USER_JOINED, UserConverter.toJoinResultDTO(user));
    }
}
