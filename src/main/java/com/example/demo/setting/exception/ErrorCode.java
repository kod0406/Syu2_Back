package com.example.demo.setting.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 쿠폰 관련
    INVALID_ABSOLUTE_EXPIRY_DATE("C001", "절대 만료 방식을 선택한 경우 만료 날짜를 입력해야 합니다."),
    EXPIRY_BEFORE_ISSUE("C002", "만료 날짜는 발급 시작 시간 이후여야 합니다."),
    INVALID_RELATIVE_EXPIRY_DAYS("C003", "상대 만료 방식을 선택한 경우 유효 기간(일)을 입력해야 합니다."),
    EXPIRY_DATE_IN_PAST("C004", "상대 만료 방식의 만료 날짜는 현재 시간 이후여야 합니다."),
    INVALID_EXPIRY_TYPE("C005", "유효하지 않은 만료 방식을 선택했습니다."),
    CHANGE_DENIED("C006", "변경 불가"),
    
    //가게 못찾음
    STORE_NOT_FOUND("S001", "가게가 존재하지 않습니다."),
    PERMISSION_DENIED("P001" ,"권한 없음"),
    STORE_MENU_EXCEPTION("S002", "가게 메뉴 오류입니다."),

    //손님 쿠폰 에러
    CUSTOMER_NOT_FOUND("U001", "고객을 찾을 수 없습니다."),
    COUPON_NOT_FOUND("U002", "쿠폰을 찾을 수 없습니다."),
    COUPON_NOT_ACTIVE("U003", "현재 발급 가능한 쿠폰이 아닙니다."),
    COUPON_EXHAUSTED("U004", "쿠폰이 모두 소진되었습니다."),
    COUPON_NOT_YET_AVAILABLE("U005", "아직 쿠폰을 발급받을 수 없습니다."),
    COUPON_DUPLICATE("U006", "이미 발급받은 쿠폰입니다."),
    CUSTOMER_COUPON_NOT_FOUND("U007", "해당 UUID의 쿠폰을 찾을 수 없습니다."),

    //비밀번호
    PASSWORD_EXCEPTION("USER001", "비밀번호 오류");
    private final String code;
    private final String message;
}
