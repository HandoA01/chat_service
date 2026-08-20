package com.study.chat.domain.enums;

/**
 * 사진·카메라·파일·음성메시지는 모두 "파일을 담는다"는 점에서 같아
 * 테이블을 나누지 않고 이 값으로만 구분한다.
 */
public enum FileType {
    IMAGE, VIDEO, FILE, AUDIO
}
