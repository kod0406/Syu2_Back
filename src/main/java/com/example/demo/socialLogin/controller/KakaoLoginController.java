package com.example.demo.socialLogin.controller;

import com.example.demo.socialLogin.service.KakaoService;
import com.example.demo.socialLogin.dto.KakaoUserInfoResponseDto;
import com.example.demo.customer.entity.Customer;
import com.example.demo.setting.jwt.JwtTokenProvider;
import com.example.demo.customer.repository.CustomerRepository;
import com.example.demo.setting.util.JwtCookieUtil;
import com.example.demo.setting.util.TokenRedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("")
@Tag(name = "카카오 로그인", description = "카카오 소셜 로그인 관련 API")
public class KakaoLoginController {

    private final KakaoService kakaoService;
    private final CustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${kakao.client_id}")
    private String kakaoClientId;

    @Value("${kakao.redirect_uri}")
    private String kakaoRedirectUri;

    @Operation(summary = "카카오 로그인 콜백", description = "카카오 인증 코드를 받아 사용자 정보를 처리하고 JWT 토큰을 발급합니다.")
    @GetMapping("OAuth2/login/kakao")
    public ResponseEntity<?> callback(
            @Parameter(description = "카카오 인증 코드") @RequestParam("code") String code) {
        String accessToken = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);
        String kakaoId = userInfo.getId().toString();
        Optional<Customer> optionalCustomer = customerRepository.findByEmail(kakaoId);

        if (optionalCustomer.isEmpty()) {
            Customer newCustomer = Customer.builder()
                    .email(kakaoId)
                    .provider("KAKAO")
                    .build();
            customerRepository.save(newCustomer);
            log.info("신규 회원 등록 완료");
        } else {
            log.info("기존 회원입니다.");
        }
        // 토큰 생성
        String jwt = jwtTokenProvider.createToken(kakaoId, "ROLE_CUSTOMER");
        String refreshToken = jwtTokenProvider.createRefreshToken(kakaoId, "ROLE_CUSTOMER");

        // 리프레시 토큰 저장 (Redis)
        long refreshTokenExpirationMillis = jwtTokenProvider.getRefreshTokenExpirationMillis();
        tokenRedisService.saveRefreshToken(kakaoId, refreshToken, refreshTokenExpirationMillis);

        // 액세스 토큰 쿠키 설정
        ResponseCookie accessTokenCookie = JwtCookieUtil.createAccessTokenCookie(jwt);

        // 리프레시 토큰 쿠키 설정 (HttpOnly)
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서 필수
                .domain("igo.ai.kr") // 도메인 명시적 설정
                .path("/")
                .maxAge(refreshTokenExpirationMillis / 1000)
                .sameSite("Lax")
                .build();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .header("Location", frontendUrl + "/")
                .build();
    }

    @Operation(summary = "카카오 로그인 리다이렉트", description = "카카오 로그인 페이지로 리다이렉트합니다.")
    @GetMapping("/api/oauth2/kakao/login")
    public ResponseEntity<Void> redirectToKakao() {
        String kakaoUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri;

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, kakaoUrl)
                .build();
    }
}
