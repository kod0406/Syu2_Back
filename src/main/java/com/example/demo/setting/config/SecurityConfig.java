package com.example.demo.setting.config;

import com.example.demo.setting.jwt.JwtAuthenticationFilter;
import com.example.demo.setting.jwt.JwtTokenProvider;
import com.example.demo.customer.repository.CustomerRepository;
import com.example.demo.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, customerRepository, storeRepository);
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS 사전 요청 허용
                        .requestMatchers(
                                "/api/stores/login",
                                "/api/customer/login", // 고객 로그인 API
                                "/api/oauth2/kakao/login",
                                "/api/oauth2/naver/login",
                                "/OAuth2/login/kakao", // 카카오 로그인 콜백
                                "/login/naver", // 네이버 로그인 콜백
                                "/owner/login", //프론트엔드 상점 로그인 화면
                                "/customer/login", //프론트엔드 고객 로그인 화면
                                "/api/auth/refresh-token", // 리프레시 토큰 요청 허용
                                "/api/v1/kakao-pay/ready",
                                "/v3/api-docs/**", // Swagger API 문서
                                "/swagger-ui/**", // Swagger UI
                                "/error",
                                "/manifest.json",
                                "/robots.txt"
                        ).permitAll()
                        .requestMatchers("/api/**").authenticated() // /api/** 경로는 인증 필요
                        .anyRequest().permitAll() // 그 외 요청은 모두 허용 (프론트엔드 라우팅)
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}