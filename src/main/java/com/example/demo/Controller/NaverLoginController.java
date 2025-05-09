package com.example.demo.Controller;

import com.example.demo.Service.NaverLoginService;
import com.example.demo.entity.customer.Customer;
import com.example.demo.jwt.JwtTokenProvider;
import com.example.demo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class NaverLoginController {
    private final NaverLoginService naverLoginService;
    private final CustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    @GetMapping("/login/naver")
    public ResponseEntity<?> naverCallback(@RequestParam String code, @RequestParam String state) {
        String tokenResponse = naverLoginService.getNaverAccessToken(code, state); // 네이버 토큰 요청 메서드 호출

        Optional<Customer> optionalCustomer = customerRepository.findByEmail(tokenResponse);

        if (optionalCustomer.isEmpty()) {
            Customer newCustomer = Customer.builder()
                    .email(tokenResponse)
                    .provider("NAVER")
                    .build();
            customerRepository.save(newCustomer);
            log.info("신규 회원 등록 완료");
        } else {
            log.info("기존 회원입니다.");
        }
        String jwt = jwtTokenProvider.createToken(tokenResponse);

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
