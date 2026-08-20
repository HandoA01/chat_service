package com.study.chat.config;

import com.study.chat.apiPayload.ApiResponse;
import com.study.chat.apiPayload.code.status.ErrorStatus;
import com.study.chat.config.jwt.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 열어두는 경로
                        .requestMatchers("/api/auth/signup", "/api/auth/login").permitAll()
                        .requestMatchers("/h2-console/**", "/temp/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // STOMP는 CONNECT 프레임에서 별도로 JWT를 검증한다 (StompAuthChannelInterceptor)
                        .requestMatchers("/ws-stomp/**").permitAll()
                        .anyRequest().authenticated()
                )
                // 인증 실패도 공통 응답 규격으로 내려준다.
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, ex) ->
                                writeError(response, ErrorStatus._UNAUTHORIZED))
                        .accessDeniedHandler((request, response, ex) ->
                                writeError(response, ErrorStatus._FORBIDDEN))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void writeError(HttpServletResponse response, ErrorStatus status) throws java.io.IOException {
        response.setStatus(status.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(),
                ApiResponse.onFailure(status.getCode(), status.getMessage(), null));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
