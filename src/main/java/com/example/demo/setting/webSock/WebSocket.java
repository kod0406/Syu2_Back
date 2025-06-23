package com.example.demo.setting.webSock;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocket implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 클라이언트가 연결할 경로
                .setAllowedOriginPatterns("*") // CORS 허용
                .withSockJS(); // SockJS fallback 사용 (브라우저 호환성)
    }

    // 메시지 브로커 구성
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // 브로드캐스트용 경로
        registry.setApplicationDestinationPrefixes("/app"); // 메시지 보낼 때 사용할 접두어
    }
}
