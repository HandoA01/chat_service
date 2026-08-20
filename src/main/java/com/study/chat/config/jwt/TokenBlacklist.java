package com.study.chat.config.jwt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 로그아웃된 토큰을 무효화하기 위한 저장소.
 * JWT는 서버가 상태를 갖지 않는 게 원칙이라 발급 후에는 취소할 방법이 없다.
 * 그래서 "무효화된 토큰" 목록을 따로 들고 검사한다.
 *
 * 지금은 인메모리라 서버를 재시작하면 사라지고 여러 대로 늘리면 공유되지 않는다.
 * 운영에서는 Redis에 TTL(토큰 만료시각)을 걸어 저장하는 것이 맞다.
 */
@Component
public class TokenBlacklist {

    private final Map<String, Long> blacklisted = new ConcurrentHashMap<>();

    public void add(String token, long expiresAtMillis) {
        blacklisted.put(token, expiresAtMillis);
    }

    public boolean contains(String token) {
        Long expiresAt = blacklisted.get(token);
        if (expiresAt == null) {
            return false;
        }
        // 이미 만료된 토큰은 어차피 검증에서 걸리므로 목록에서 지운다.
        if (expiresAt < System.currentTimeMillis()) {
            blacklisted.remove(token);
            return false;
        }
        return true;
    }
}
