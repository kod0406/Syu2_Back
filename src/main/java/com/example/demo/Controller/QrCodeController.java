package com.example.demo.Controller;

import com.example.demo.Service.QrCodeService;
import com.example.demo.dto.QrCodeResponseDto;
import com.google.zxing.WriterException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Tag(name = "QR코드", description = "QR코드 생성 및 테스트 관련 기능")
public class QrCodeController {
    private final QrCodeService qrCodeService;



    @Operation(
            summary = "QR코드 생성",
            description = "제공된 URL로 QR코드를 생성하고 결과 페이지를 보여줍니다. URL은 `application/x-www-form-urlencoded` 형식으로 전달됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "QR 코드 생성을 위한 URL을 포함하는 폼 데이터입니다. 'url' 파라미터로 전달해야 합니다.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(
                                    type = "object",
                                    properties = {
                                            @io.swagger.v3.oas.annotations.StringToClassMapItem( // 수정된 부분
                                                    key = "url",
                                                    value = String.class
                                            )
                                    },
                                    requiredProperties = {"url"}
                            )
                    )
            )
    )

    @PostMapping("/generate-qr")
    public ResponseEntity<QrCodeResponseDto> generateQrCode(@RequestParam String url) throws IOException, WriterException {
        String base64 = qrCodeService.generateQrCodeBase64(url, 250, 250);
        return ResponseEntity.ok(new QrCodeResponseDto("data:image/png;base64," + base64, url));
    }

    @Operation(summary = "QR코드 다운로드", description = "생성된 QR코드 이미지를 다운로드합니다.")
    @GetMapping("/download-qr")
    public ResponseEntity<byte[]> downloadQrCode(
            @Parameter(description = "QR코드에 인코딩할 URL (쿼리 파라미터 'url')") @RequestParam("url") String url) {
        try {
            byte[] qrCodeBytes = qrCodeService.generateQrCodeBytes(url, 250, 250);

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
}