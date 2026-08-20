package com.study.chat.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI chatServiceAPI() {
        Info info = new Info()
                .title("Chat Service API")
                .description("카카오톡 기준 채팅 서비스 API 명세서")
                .version("0.0.1");

        return new OpenAPI().info(info);
    }
}
