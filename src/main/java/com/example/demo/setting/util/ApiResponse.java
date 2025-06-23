package com.example.demo.setting.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 공통 API 응답 포맷을 정의하는 클래스입니다.
 *
 * 목적:
 * - 모든 API 응답을 동일한 구조로 반환하여 프론트엔드나 클라이언트 측에서 처리 일관성을 높입니다.
 * - 성공/실패 여부와 관련된 메시지와 함께 필요한 데이터를 담아서 보냅니다.
 *
 * 사용 예시:
 * - 성공: {"message": "회원가입 성공", "data": {id: 1, email: "user@fitform.com"}}
 * - 실패: {"message": "이메일이 중복되었습니다.", "data": null}
 *
 * @param <T> 실제 응답 데이터의 타입
 */
@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 포함
@AllArgsConstructor
public class ApiResponse<T> {

    /** 사용자에게 전달할 메시지 (예: "성공", "오류 발생", "인증 실패" 등) */
    private String message;

    /** 실제 응답 데이터 (실패 시 null 가능) */
    private T data;

    /**
     * 성공 응답을 생성하는 정적 메서드 (데이터 포함)
     * @param message 사용자에게 전달할 메시지
     * @param data 실제 응답 데이터
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data);
    }

    /**
     * 성공 응답을 생성하는 정적 메서드 (데이터 없음)
     * @param message 사용자에게 전달할 메시지
     * @return ApiResponse 객체
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(message, null);
    }

    /**
     * 실패 또는 오류 응답을 생성하는 정적 메서드
     * @param message 오류 메시지
     * @return ApiResponse 객체 (data는 null)
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null);
    }
}
