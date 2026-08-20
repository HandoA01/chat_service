package com.study.chat.web.dto;

import com.study.chat.domain.enums.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatRoomRequestDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "채팅방 생성 요청")
    public static class CreateDTO {

        @Schema(description = "방 종류 (SINGLE 또는 GROUP)", example = "GROUP")
        @NotNull(message = "방 종류는 필수입니다.")
        private RoomType type;

        @Schema(description = "방 제목. SINGLE이면 생략 가능", example = "프로젝트 팀방", nullable = true)
        @Size(max = 100, message = "방 제목은 100자를 넘을 수 없습니다.")
        private String title;

        @Schema(description = "초대할 회원 ID 목록 (본인 제외)", example = "[2, 3]")
        @NotEmpty(message = "참여자는 최소 1명 이상이어야 합니다.")
        private List<Long> participantIds;
    }
}
