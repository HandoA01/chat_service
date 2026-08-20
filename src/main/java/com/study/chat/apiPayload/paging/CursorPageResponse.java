package com.study.chat.apiPayload.paging;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커서 기반 페이징 공통 응답.
 * 채팅처럼 계속 새 데이터가 쌓이는 목록은 offset 방식이면 조회 도중 경계가 밀려
 * 같은 항목이 중복되거나 누락된다. 마지막으로 본 id를 커서로 넘겨 그 지점부터 읽는다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "커서 기반 페이징 공통 응답")
public class CursorPageResponse<T> {

    @Schema(description = "조회된 데이터 목록")
    private List<T> content;

    @Schema(description = "요청된 페이지 크기", example = "20")
    private Integer size;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private Boolean hasNext;

    @Schema(description = "다음 요청에 사용할 커서. 더 없으면 null", example = "5020", nullable = true)
    private Long nextCursor;

    public static <T> CursorPageResponse<T> of(List<T> content, int size, boolean hasNext, Long nextCursor) {
        return CursorPageResponse.<T>builder()
                .content(content)
                .size(size)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }
}
