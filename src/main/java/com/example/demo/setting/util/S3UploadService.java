package com.example.demo.setting.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.demo.store.entity.Store;
import com.example.demo.store.repository.StoreMenuRepository;
import com.example.demo.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3 amazonS3;
    private final StoreMenuRepository storeMenuRepository;
    private final StoreRepository storeRepository;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucketName;

    public String uploadFile(MultipartFile file) {
        if(file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String originalFileName = file.getOriginalFilename();
        String extension = getExtension(originalFileName);
        String fileName = createFileNameWithStoreInfo(extension);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    fileName,
                    file.getInputStream(),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead);

            amazonS3.putObject(putObjectRequest);
            return amazonS3.getUrl(bucketName, fileName).toString();

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다." + e.getMessage());
        }
    }

    // 이미지 URL에서 S3 객체 키 추출
    public String extractKeyFromImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            URL url = new URL(imageUrl);
            String path = url.getPath();
            // URL 디코딩 (한글 등이 포함될 수 있으므로)
            return URLDecoder.decode(path.substring(1), StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            log.error("이미지 URL에서 키 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // S3에서 파일 삭제
    public void deleteFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        String key = extractKeyFromImageUrl(imageUrl);
        if (key != null) {
            try {
                amazonS3.deleteObject(bucketName, key);
                log.info("S3에서 파일 삭제 완료: {}", key);
            } catch (Exception e) {
                log.error("S3에서 파일 삭제 실패: {}", e.getMessage());
            }
        }
    }

    private String createFileNameWithStoreInfo(String extension) {
        // 현재 인증된 사용자(Store) 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Store) {
            Store store = (Store) authentication.getPrincipal();
            String storeName = store.getStoreName();
            Long storeId = store.getStoreId();

            // 타임스탬프를 추가하여 고유한 파일명 생성
            String timestamp = String.valueOf(System.currentTimeMillis());
            return storeName + "_" + storeId + "_" + timestamp + extension;
        }

        // 인증 정보가 없거나 Store가 아닌 경우 기본 파일명 사용
        return System.currentTimeMillis() + extension;
    }

    private String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}