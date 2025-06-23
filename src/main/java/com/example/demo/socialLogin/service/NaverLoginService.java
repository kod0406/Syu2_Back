package com.example.demo.socialLogin.service;

import com.example.demo.socialLogin.dto.NaverProfileResponseDto;
import com.example.demo.socialLogin.dto.NaverTokenResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class NaverLoginService {

    @Value("${naver.client_id}")
    private String naverClientId;
    @Value("${naver.client_secret}")
    private String naverClientSecret;


    public String getNaverAccessToken(String code, String state) {
        NaverTokenResponseDto tokenResponse = WebClient.create("https://nid.naver.com")
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/oauth2.0/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", naverClientId)
                        .queryParam("client_secret", naverClientSecret)
                        .queryParam("code", code)
                        .queryParam("state", state)
                        .build(true)
                )
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(NaverTokenResponseDto.class)
                .block();

        String accessToken = tokenResponse.getAccess_token();

        // 2. 프로필 API 호출
        NaverProfileResponseDto profile = WebClient.create("https://openapi.naver.com")
                .get()
                .uri("/v1/nid/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(NaverProfileResponseDto.class)
                .block();

        return profile.getResponse().getId(); // ✅ Naver ID 추출
    }
}
