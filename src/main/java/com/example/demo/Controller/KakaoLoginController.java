package com.example.demo.Controller;

import com.example.demo.Service.KakaoService;
import com.example.demo.dto.KakaoUserInfoResponseDto;
import com.example.demo.entity.customer.Customer;
import com.example.demo.jwt.JwtTokenProvider;
import com.example.demo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class KakaoLoginController {

    private final KakaoService kakaoService;
    private final CustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    @GetMapping("OAuth2/login/kakao")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
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
                .header("Location", "http://localhost:8080/menu-test")
                .build();
    }
}