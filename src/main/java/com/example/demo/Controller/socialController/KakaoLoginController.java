package com.example.demo.Controller.socialController;

import com.example.demo.Service.KakaoService;
import com.example.demo.dto.socialDto.KakaoUserInfoResponseDto;
import com.example.demo.entity.customer.Customer;
import com.example.demo.jwt.JwtTokenProvider;
import com.example.demo.repository.CustomerRepository;
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

import java.time.Duration;
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
        String jwt = jwtTokenProvider.createToken(kakaoId);

        ResponseCookie cookie = ResponseCookie.from("access_token", jwt)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("Lax")
                .build();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Set-Cookie", cookie.toString())
                .header("Location", "http://localhost:3000/")
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
