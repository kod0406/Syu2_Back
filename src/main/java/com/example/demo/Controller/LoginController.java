package com.example.demo.Controller;

import com.example.demo.Service.KakaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@Tag(name = "��그인", description = "로그인 페이지 관련 API")
public class LoginController {

    private final KakaoService kakaoService;

    @Value("${kakao.client_id}")
    private String kakaoClientId;

    @Value("${kakao.redirect_uri}")
    private String kakaoRedirectUri;

    @Value("${naver.redirect_uri}")
    private String naverRedirectUri;

    @Value("${naver.client_id}")
    private String naverClientId;

    @Operation(summary = "카카오 로그인 페이지", description = "카카오 로그인 페이지를 제공합니다.")
    @GetMapping("/kakao")
    public String kakaoLogin(Model model) {

        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code"
                + "&client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri;
        model.addAttribute("kakaoAuthUrl", kakaoAuthUrl);

        String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" +
                naverClientId +
                "&state=1234" +
                "&redirect_uri=" +
                naverRedirectUri;

        model.addAttribute("naverAuthUrl", naverAuthUrl);

        return "login";
    }

    @Operation(summary = "점주 로그인 페이지", description = "점주 로그인 페이지를 제공합니다.")
    @GetMapping("/login/owner")
    public String ownerLogin() {
        return "owner-login";
    }

    @Operation(summary = "점주 회원가입 페이지", description = "점주 회원가입 페이지를 제공합니다.")
    @GetMapping("/login/owner/register")
    public String ownerRegister() {
        return "Owner_register";
    }
}
