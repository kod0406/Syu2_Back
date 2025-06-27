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
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucketName;

    // 타임아웃 설정 (nginx 설정과 일치)
    private static final int UPLOAD_TIMEOUT_SECONDS = 90;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public String uploadFile(MultipartFile file) {
        // 파일 검증
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        String extension = getExtension(originalFileName);
        String fileName = createFileNameWithStoreInfo(extension);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // 타임아웃을 적용한 업로드 작업
        Future<String> uploadTask = executorService.submit(() -> {
            try {
                PutObjectRequest putObjectRequest = new PutObjectRequest(
                        bucketName,
                        fileName,
                        file.getInputStream(),
                        metadata
                ).withCannedAcl(CannedAccessControlList.PublicRead);

                log.info("S3 파일 업로드 시작: {} (크기: {}bytes)", fileName, file.getSize());
                amazonS3.putObject(putObjectRequest);
                String fileUrl = amazonS3.getUrl(bucketName, fileName).toString();
                log.info("S3 파일 업로드 완료: {}", fileName);
                return fileUrl;

            } catch (IOException e) {
                log.error("파일 업로드 중 IO 오류 발생: {}", e.getMessage());
                throw new RuntimeException("파일 업로드 중 IO 오류가 발생했습니다: " + e.getMessage(), e);
            } catch (Exception e) {
                log.error("파일 업로드 중 예상치 못한 오류 발생: {}", e.getMessage());
                throw new RuntimeException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        });

        try {
            // 타임아웃 적용하여 업로드 결과 대기
            return uploadTask.get(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            uploadTask.cancel(true);
            log.error("파일 업로드 타임아웃 ({}초 초과): {}", UPLOAD_TIMEOUT_SECONDS, fileName);
            throw new RuntimeException("파일 업로드 시간이 초과되었습니다. 파일 크기를 확인하거나 다시 시도해주세요.");
        } catch (InterruptedException e) {
            uploadTask.cancel(true);
            Thread.currentThread().interrupt();
            log.error("파일 업로드가 중단되었습니다: {}", fileName);
            throw new RuntimeException("파일 업로드가 중단되었습니다.");
        } catch (ExecutionException e) {
            log.error("파일 업로드 실행 중 오류 발생: {}", e.getCause().getMessage());
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다: " + cause.getMessage(), cause);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("파일 크기가 너무 큽니다. 최대 크기: %dMB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        // 허용된 파일 확장자 검증
        String extension = getExtension(originalFileName).toLowerCase();
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif만 허용)");
        }

        log.info("파일 검증 완료: {} (크기: {}bytes)", originalFileName, file.getSize());
    }

    private boolean isAllowedExtension(String extension) {
        return extension.equals(".jpg") || extension.equals(".jpeg") ||
               extension.equals(".png") || extension.equals(".gif");
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
            return URLDecoder.decode(path.substring(1), StandardCharsets.UTF_8);
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