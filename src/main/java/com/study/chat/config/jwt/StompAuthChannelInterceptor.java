package com.study.chat.config.jwt;

import com.study.chat.config.security.CustomUserDetails;
import com.study.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * STOMP CONNECT 프레임의 Authorization 헤더로 인증한다.
 * HTTP 요청이 아니라 WebSocket 프레임이라 JwtAuthenticationFilter가 관여하지 못하므로,
 * 메시지 채널 단계에서 따로 검증해 세션에 사용자를 붙인다.
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklist tokenBlacklist;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor);

            if (token == null || tokenBlacklist.contains(token) || !jwtTokenProvider.isValid(token)) {
                throw new IllegalArgumentException("유효한 인증 토큰이 필요합니다.");
            }

            // 한 번 인증해두면 이후 SEND/SUBSCRIBE 프레임에서 재사용된다.
            userRepository.findById(jwtTokenProvider.getUserId(token)).ifPresent(user -> {
                CustomUserDetails principal = new CustomUserDetails(user);
                accessor.setUser(new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));
            });
        }

        return message;
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            return header.substring(PREFIX.length());
        }
        return null;
    }
}
