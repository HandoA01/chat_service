package com.study.chat.config;

import com.study.chat.config.jwt.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 명세서 5.1: ws://{host}/ws-stomp, SockJS 사용 시 http(s)://{host}/ws-stomp
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*");
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 구독은 /sub, 발행은 /pub. 명세서 5.2 / 5.3과 같은 경로다.
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // CONNECT 프레임의 JWT를 여기서 검증한다.
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
