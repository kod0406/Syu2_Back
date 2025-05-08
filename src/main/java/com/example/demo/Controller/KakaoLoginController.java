package com.example.demo.Controller;

import com.example.demo.Service.KakaoService;
import com.example.demo.dto.KakaoUserInfoResponseDto;
import com.example.demo.entity.customer.Customer;
import com.example.demo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
public class KakaoLoginController {

    private final KakaoService kakaoService;
    private final CustomerRepository customerRepository;

    @GetMapping("OAuth2/login/kakao")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        String accessToken = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);
        String kakaoId = userInfo.getId().toString();

        Optional<Customer> customer = Optional.of(customerRepository.findByEmail(kakaoId)
                .orElseGet(() -> {
                    Customer newCustomer = Customer.builder()
                            .email(kakaoId)
                            .provider("KAKAO")
                            .build();
                    // JWT 발급 후에 main으로 이동
                    return customerRepository.save(newCustomer);
                }));
        //회원가입 되어있으면 JWT토큰 발급 로직 후에 main으로 이동

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "http://localhost:8080/menu-test")
                .build();
    }
}