package com.study.chat.apiPayload.code.status;

import com.study.chat.apiPayload.code.BaseErrorCode;
import com.study.chat.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 규칙: [도메인][HTTP 상태 앞자리]00[일련번호]
 * 예) USER4001 - 회원 도메인의 400번대 첫 번째 에러
 * 도메인을 code에 명시해서 프론트가 어느 영역의 에러인지 바로 알 수 있게 한다.
 */
@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 회원 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4001", "해당 회원을 찾을 수 없습니다."),
    USER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "USER4002", "이미 사용 중인 이메일입니다."),
    USER_NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "USER4003", "닉네임은 필수입니다."),
    USER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "USER4004", "이미 탈퇴한 회원입니다."),

    // 친구 관련 에러
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND4001", "해당 친구 관계를 찾을 수 없습니다."),
    FRIEND_ALREADY_REQUESTED(HttpStatus.CONFLICT, "FRIEND4002", "이미 친구 요청을 보냈거나 친구인 상대입니다."),
    FRIEND_SELF_REQUEST(HttpStatus.BAD_REQUEST, "FRIEND4003", "자기 자신에게는 친구 요청을 보낼 수 없습니다."),

    // 채팅방 관련 에러
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM4001", "해당 채팅방을 찾을 수 없습니다."),
    ROOM_NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "ROOM4002", "참여하지 않은 채팅방입니다."),
    ROOM_ALREADY_JOINED(HttpStatus.CONFLICT, "ROOM4003", "이미 참여 중인 채팅방입니다."),

    // 메시지 관련 에러
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE4001", "해당 메시지를 찾을 수 없습니다."),
    MESSAGE_PAGE_INVALID(HttpStatus.BAD_REQUEST, "MESSAGE4002", "페이지 번호는 1 이상이어야 합니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
