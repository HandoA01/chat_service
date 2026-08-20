package com.study.chat.apiPayload.paging;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * offset 기반 페이징 공통 응답. 목록 API마다 페이징 필드를 따로 만들지 않기 위해 분리했다.
 * 클라이언트에는 1부터 시작하는 page를 돌려준다. (Pageable은 0부터 세므로 +1)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "offset 기반 페이징 공통 응답")
public class PageResponse<T> {

    @Schema(description = "조회된 데이터 목록")
    private List<T> content;

    @Schema(description = "현재 페이지 번호 (1부터 시작)", example = "1")
    private Integer page;

    @Schema(description = "페이지 크기", example = "20")
    private Integer size;

    @Schema(description = "전체 페이지 수", example = "3")
    private Integer totalPages;

    @Schema(description = "전체 데이터 수", example = "57")
    private Long totalElements;

    @Schema(description = "첫 페이지 여부", example = "true")
    private Boolean isFirst;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private Boolean isLast;

    /**
     * 엔티티 Page를 DTO Page 응답으로 변환한다.
     */
    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> converter) {
        return PageResponse.<T>builder()
                .content(page.getContent().stream().map(converter).toList())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}
