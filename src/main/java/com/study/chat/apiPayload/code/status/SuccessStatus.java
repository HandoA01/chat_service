package com.study.chat.apiPayload.code.status;

import com.study.chat.apiPayload.code.BaseCode;
import com.study.chat.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {

    // 가장 일반적인 응답
    _OK(HttpStatus.OK, "COMMON200", "성공입니다."),
    _CREATED(HttpStatus.CREATED, "COMMON201", "생성에 성공했습니다."),

    // 회원 관련 응답
    USER_JOINED(HttpStatus.CREATED, "USER201", "회원가입에 성공했습니다."),

    // 친구 관련 응답
    FRIEND_REQUESTED(HttpStatus.CREATED, "FRIEND201", "친구 요청을 보냈습니다."),

    // 메시지 관련 응답
    MESSAGE_PAGE_OK(HttpStatus.OK, "MESSAGE200", "채팅 내역 조회에 성공했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build();
    }
}
