package com.example.demo.store.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Base64;


@Service
public class QrCodeService {
    public byte[] generateQrCodeBytes(String url, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        return pngOutputStream.toByteArray();
    }

    /**
     * URL에 대한 QR코드를 Base64 인코딩 문자열로 반환
     * HTML의 <img> 태그에서 바로 사용 가능
     */
    public String generateQrCodeBase64(String url, int width, int height) throws WriterException, IOException {
        byte[] qrCodeBytes = generateQrCodeBytes(url, width, height);
        return Base64.getEncoder().encodeToString(qrCodeBytes);
    }
}
