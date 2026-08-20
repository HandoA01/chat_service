package com.study.chat.common;

/**
 * 로그인(JWT) 기능이 아직 없어서, 현재 사용자를 하드코딩으로 대체한다.
 * 12주차 Spring Security 적용 시 SecurityContext에서 꺼내는 방식으로 교체할 것.
 */
public class TempAuth {

    public static final Long CURRENT_USER_ID = 1L;

    private TempAuth() {
    }
}
