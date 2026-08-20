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
    USER_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "USER4005", "이메일 또는 비밀번호가 일치하지 않습니다."),

    // 친구 관련 에러
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND4001", "해당 친구 관계를 찾을 수 없습니다."),
    FRIEND_ALREADY_REQUESTED(HttpStatus.CONFLICT, "FRIEND4002", "이미 친구 요청을 보냈거나 친구인 상대입니다."),
    FRIEND_SELF_REQUEST(HttpStatus.BAD_REQUEST, "FRIEND4003", "자기 자신에게는 친구 요청을 보낼 수 없습니다."),
    FRIEND_NOT_RECEIVER(HttpStatus.FORBIDDEN, "FRIEND4004", "본인이 받은 친구 요청이 아닙니다."),
    FRIEND_ALREADY_ACCEPTED(HttpStatus.CONFLICT, "FRIEND4005", "이미 수락된 친구 요청입니다."),

    // 채팅방 관련 에러
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM4001", "해당 채팅방을 찾을 수 없습니다."),
    ROOM_NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "ROOM4002", "참여하지 않은 채팅방입니다."),
    ROOM_ALREADY_JOINED(HttpStatus.CONFLICT, "ROOM4003", "이미 참여 중인 채팅방입니다."),
    ROOM_SINGLE_NEEDS_ONE(HttpStatus.BAD_REQUEST, "ROOM4004", "1:1 채팅방은 상대방 1명만 지정할 수 있습니다."),
    ROOM_SELF_ONLY(HttpStatus.BAD_REQUEST, "ROOM4005", "본인만으로는 채팅방을 만들 수 없습니다."),
    ROOM_TYPE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "ROOM4006", "생성할 수 없는 방 종류입니다."),

    // 메시지 관련 에러
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE4001", "해당 메시지를 찾을 수 없습니다."),
    MESSAGE_PAGE_INVALID(HttpStatus.BAD_REQUEST, "MESSAGE4002", "페이지 번호는 1 이상이어야 합니다."),
    MESSAGE_SIZE_INVALID(HttpStatus.BAD_REQUEST, "MESSAGE4003", "페이지 크기는 1 이상 100 이하여야 합니다."),
    MESSAGE_KEYWORD_REQUIRED(HttpStatus.BAD_REQUEST, "USER4006", "검색어는 필수입니다."),
    MESSAGE_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "MESSAGE4004", "텍스트 메시지는 본문이 필요합니다."),
    MESSAGE_EMOTICON_REQUIRED(HttpStatus.BAD_REQUEST, "MESSAGE4005", "이모티콘 메시지는 emoticonId가 필요합니다."),
    MESSAGE_FILE_REQUIRED(HttpStatus.BAD_REQUEST, "MESSAGE4006", "미디어 메시지는 fileUrl과 fileType이 필요합니다."),
    MESSAGE_EMOTICON_NOT_OWNED(HttpStatus.FORBIDDEN, "MESSAGE4007", "보유하지 않은 이모티콘입니다."),
    MESSAGE_PARENT_NOT_IN_ROOM(HttpStatus.BAD_REQUEST, "MESSAGE4008", "답장 대상이 같은 채팅방의 메시지가 아닙니다."),
    EMOTICON_NOT_FOUND(HttpStatus.NOT_FOUND, "EMOTICON4001", "해당 이모티콘을 찾을 수 없습니다."),
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
