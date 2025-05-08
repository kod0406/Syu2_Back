package com.example.demo.Controller;

import com.example.demo.Service.QrCodeTestService;
import com.google.zxing.WriterException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;

@Controller
public class QrCodeTestController {
    private final QrCodeTestService qrCodeTestService;

    public QrCodeTestController(QrCodeTestService qrCodeTestService) {
        this.qrCodeTestService = qrCodeTestService;
    }
    /**
     * QR코드 생성 폼 페이지 표시
     */
    @GetMapping("/")
    public String showQrForm() {
        return "qr-form";
    }

    /**
     * QR코드 생성 및 결과 페이지 표시
     */
    @PostMapping("/generate-qr")
    public String generateQrCode(@RequestParam("url") String url, Model model) {
        try {
            String qrCodeBase64 = qrCodeTestService.generateQrCodeBase64(url, 250, 250);
            model.addAttribute("qrCodeImage", "data:image/png;base64," + qrCodeBase64);
            model.addAttribute("url", url);

            return "qr-result";
        } catch (WriterException | IOException e) {
            model.addAttribute("errorMessage", "QR 코드 생성 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }

    /**
     * QR코드 이미지 다운로드
     */
    @GetMapping("/download-qr")
    public ResponseEntity<byte[]> downloadQrCode(@RequestParam("url") String url) {
        try {
            byte[] qrCodeBytes = qrCodeTestService.generateQrCodeBytes(url, 250, 250);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "qrcode.png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(qrCodeBytes);
        } catch (WriterException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * QR코드를 통해 접근할 테스트 페이지 (메뉴판 접근 테스트용)
     */
    @GetMapping("/menu-test")
    public String menuTestPage(@RequestParam(required = false) String id, Model model) {
        model.addAttribute("menuId", id != null ? id : "기본 메뉴");
        return "menu-test";
    }

}
