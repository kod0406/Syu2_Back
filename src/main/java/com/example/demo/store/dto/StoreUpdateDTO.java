package com.example.demo.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "매장 정보 업데이트 DTO")
public class StoreUpdateDTO {

    @Schema(description = "새로운 매장명", example = "새로운 매장명")
    private String storeName;

    @Schema(description = "현재 비밀번호 (비밀번호 변경 시 필수)", example = "currentPassword123")
    private String currentPassword;

    @Schema(description = "새로운 비밀번호", example = "newPassword123")
    private String newPassword;

    @Schema(description = "새로운 비밀번호 확인", example = "newPassword123")
    private String confirmPassword;
}
