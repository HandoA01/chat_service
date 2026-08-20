package com.study.chat.web.controller;

import com.study.chat.apiPayload.ApiResponse;
import com.study.chat.apiPayload.code.status.SuccessStatus;
import com.study.chat.config.jwt.JwtTokenProvider;
import com.study.chat.config.jwt.TokenBlacklist;
import com.study.chat.converter.UserConverter;
import com.study.chat.domain.User;
import com.study.chat.service.user.UserCommandService;
import com.study.chat.web.dto.UserRequestDTO;
import com.study.chat.web.dto.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "회원가입 · 로그인 · 로그아웃")
public class AuthRestController {

    private static final String PREFIX = "Bearer ";

    private final UserCommandService userCommandService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklist tokenBlacklist;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    @Operation(summary = "회원가입 API", description = "이메일/비밀번호/닉네임으로 신규 회원을 등록합니다.")
    public ApiResponse<UserResponseDTO.JoinResultDTO> join(@RequestBody @Valid UserRequestDTO.JoinDTO request) {
        User user = userCommandService.joinUser(request);
        return ApiResponse.of(SuccessStatus.USER_JOINED, UserConverter.toJoinResultDTO(user));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 API", description = "이메일/비밀번호로 로그인하고 JWT 액세스 토큰을 발급받습니다.")
    public ApiResponse<UserResponseDTO.LoginResultDTO> login(@RequestBody @Valid UserRequestDTO.LoginDTO request) {
        User user = userCommandService.login(request);
        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail());

        return ApiResponse.of(SuccessStatus.USER_LOGIN_OK,
                UserConverter.toLoginResultDTO(user, token, jwtTokenProvider.getExpirationSeconds()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API",
            description = "현재 토큰을 블랙리스트에 등록해 무효화합니다. 응답 본문은 없습니다.")
    public void logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length());
            tokenBlacklist.add(token, System.currentTimeMillis() + jwtTokenProvider.getExpirationSeconds() * 1000);
        }
    }
}
