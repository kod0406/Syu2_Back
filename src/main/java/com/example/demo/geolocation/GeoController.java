package com.example.demo.geolocation;

import com.example.demo.setting.util.IpExtraction;
import com.example.demo.socialLogin.controller.KakaoLoginController;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GeoController {
    private final GeoService geoService;
    @GetMapping("/location")
    public ResponseEntity<String> geolocation(GeoRequestDto geoRequestDto) {
        String geoResponseDto = geoService.requestGeolocation(geoRequestDto);
        return ResponseEntity.ok(geoResponseDto);
    }



}
