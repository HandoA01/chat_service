package com.study.chat.web.controller;

import com.study.chat.apiPayload.ApiResponse;
import com.study.chat.apiPayload.code.status.SuccessStatus;
import com.study.chat.config.security.CustomUserDetails;
import com.study.chat.converter.UserConverter;
import com.study.chat.domain.User;
import com.study.chat.service.user.UserCommandService;
import com.study.chat.service.user.UserQueryService;
import com.study.chat.web.dto.UserRequestDTO;
import com.study.chat.web.dto.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/users")
@Tag(name = "회원", description = "프로필 조회·수정 및 유저 검색")
public class UserRestController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회 API", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    public ApiResponse<UserResponseDTO.ProfileDTO> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal) {

        User user = userQueryService.getUser(principal.getUserId());
        return ApiResponse.of(SuccessStatus.USER_PROFILE_OK, UserConverter.toProfileDTO(user));
    }

    @PatchMapping("/me")
    @Operation(summary = "내 프로필 수정 API",
            description = "닉네임, 프로필 이미지, 상태 메시지를 부분 수정합니다. 보낸 필드만 반영됩니다.")
    public ApiResponse<UserResponseDTO.ProfileDTO> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid UserRequestDTO.UpdateProfileDTO request) {

        User user = userCommandService.updateProfile(principal.getUserId(), request);
        return ApiResponse.of(SuccessStatus.USER_PROFILE_UPDATED, UserConverter.toProfileDTO(user));
    }

    @GetMapping("/search")
    @Operation(summary = "유저 검색 API", description = "닉네임 또는 이메일로 유저를 검색합니다. 결과가 없어도 빈 배열을 반환합니다.")
    @Parameter(name = "keyword", description = "검색어 (닉네임 또는 이메일)")
    public ApiResponse<List<UserResponseDTO.SearchResultDTO>> search(
            @RequestParam @NotBlank(message = "검색어는 필수입니다.") String keyword) {

        List<UserResponseDTO.SearchResultDTO> result = userQueryService.searchUsers(keyword).stream()
                .map(UserConverter::toSearchResultDTO)
                .toList();

        return ApiResponse.of(SuccessStatus.USER_SEARCH_OK, result);
    }
}
