package com.example.demo.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ CORS 설정 예시 (필요 시 활성화)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOrigins("http://localhost:3000") // 프론트엔드 주소
                .allowedMethods("GET", "POST")
                .allowCredentials(true);
    }

    // 🔧 메시지 컨버터, 인터셉터 등도 여기에서 설정 가능
}
