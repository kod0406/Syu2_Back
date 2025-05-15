package com.example.demo.Controller;

import com.example.demo.Service.QrCodeTestService;
import com.google.zxing.WriterException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "QR코드", description = "QR코드 생성 및 테스트 관련 기능")
public class QrCodeTestController {
    private final QrCodeTestService qrCodeTestService;

    public QrCodeTestController(QrCodeTestService qrCodeTestService) {
        this.qrCodeTestService = qrCodeTestService;
    }

    @Operation(summary = "QR코드 생성 폼 페이지", description = "QR코드 생성 입력 폼을 제공합니다.")
    @GetMapping("/")
    public String showQrForm() {
        return "qr-form";
    }

    @Operation(summary = "QR코드 생성", description = "제공된 URL로 QR코드를 생성하고 결과 페이지를 보여줍니다.")
    @PostMapping("/generate-qr")
    public String generateQrCode(
            @Parameter(description = "QR코드에 인코딩할 URL") @RequestParam("url") String url,
            Model model) {
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

    @Operation(summary = "QR코드 다운로드", description = "생성된 QR코드 이미지를 다운로드합니다.")
    @GetMapping("/download-qr")
    public ResponseEntity<byte[]> downloadQrCode(
            @Parameter(description = "QR코드에 인코딩할 URL") @RequestParam("url") String url) {
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

    @Operation(summary = "메뉴 테스트 페이지", description = "QR코드로 접근할 메뉴 테스트 페이지를 제공합니다.")
    @GetMapping("/menu-test")
    public String menuTestPage(
            @Parameter(description = "메뉴 ID") @RequestParam(required = false) String id,
            Model model) {
        model.addAttribute("menuId", id != null ? id : "기본 메뉴");
        return "menu-test";
    }
}
