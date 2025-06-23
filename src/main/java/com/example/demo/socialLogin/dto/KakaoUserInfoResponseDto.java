package com.example.demo.socialLogin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;

@Getter
@NoArgsConstructor //역직렬화를 위한 기본 생성자
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "카카오 사용자 정보 응답 DTO")
public class KakaoUserInfoResponseDto {

    //회원 번호
    @JsonProperty("id")
    @Schema(description = "카카오 회원 번호", example = "12345678")
    public Long id;

    //자동 연결 설정을 비활성화한 경우만 존재.
    //true : 연결 상태, false : 연결 대기 상태
    @JsonProperty("has_signed_up")
    @Schema(description = "연결 상태 여부", example = "true")
    public Boolean hasSignedUp;

    //서비스에 연결 완료된 시각. UTC
    @JsonProperty("connected_at")
    @Schema(description = "서비스 연결 완료 시각")
    public Date connectedAt;

    //카카오싱크 간편가입을 통해 로그인한 시각. UTC
    @JsonProperty("synched_at")
    @Schema(description = "카카오싱크 로그인 시각")
    public Date synchedAt;

    //사용자 프로퍼티
    @JsonProperty("properties")
    @Schema(description = "사용자 속성 정보")
    public HashMap<String, String> properties;

    //카카오 계정 정보
    @JsonProperty("kakao_account")
    @Schema(description = "카카오 계정 정보")
    public KakaoAccount kakaoAccount;

    //uuid 등 추가 정보
    @JsonProperty("for_partner")
    @Schema(description = "파트너용 추가 정보")
    public Partner partner;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "카카오 계정 상세 정보")
    public class KakaoAccount {

        //프로필 정보 제공 동의 여부
        @JsonProperty("profile_needs_agreement")
        @Schema(description = "프로필 정보 제공 동의 여부", example = "false")
        public Boolean isProfileAgree;

        //닉네임 제공 동의 여부
        @JsonProperty("profile_nickname_needs_agreement")
        @Schema(description = "닉네임 제공 동의 여부", example = "false")
        public Boolean isNickNameAgree;

        //프로필 사진 제공 동의 여부
        @JsonProperty("profile_image_needs_agreement")
        @Schema(description = "프로필 사진 제공 동의 여부", example = "false")
        public Boolean isProfileImageAgree;

        //사용자 프로필 정보
        @JsonProperty("profile")
        @Schema(description = "사용자 프로필 정보")
        public Profile profile;

        //이름 제공 동의 여부
        @JsonProperty("name_needs_agreement")
        @Schema(description = "이름 제공 동의 여부", example = "false")
        public Boolean isNameAgree;

        //카카오계정 이름
        @JsonProperty("name")
        @Schema(description = "카카오계정 이름", example = "홍길동")
        public String name;

        //이메일 제공 동의 여부
        @JsonProperty("email_needs_agreement")
        @Schema(description = "이메일 제공 동의 여부", example = "false")
        public Boolean isEmailAgree;

        //이메일이 유효 여부
        // true : 유효한 이메일, false : 이메일이 다른 카카오 계정에 사용돼 만료
        @JsonProperty("is_email_valid")
        @Schema(description = "이메일 유효 여부", example = "true")
        public Boolean isEmailValid;

        //이메일이 인증 여부
        //true : 인증된 이메일, false : 인증되지 않은 이메일
        @JsonProperty("is_email_verified")
        @Schema(description = "이메일 인증 여부", example = "true")
        public Boolean isEmailVerified;

        //카카오계정 대표 이메일
        @JsonProperty("email")
        @Schema(description = "카카오계정 이메일", example = "user@example.com")
        public String email;

        //연령대 제공 동의 여부
        @JsonProperty("age_range_needs_agreement")
        @Schema(description = "연령대 제공 동의 여부", example = "false")
        public Boolean isAgeAgree;

        //연령대
        //참고 https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#req-user-info
        @JsonProperty("age_range")
        @Schema(description = "연령대", example = "20~29")
        public String ageRange;

        //출생 연도 제공 동의 여부
        @JsonProperty("birthyear_needs_agreement")
        @Schema(description = "출생 연도 제공 동의 여부", example = "false")
        public Boolean isBirthYearAgree;

        //출생 연도 (YYYY 형식)
        @JsonProperty("birthyear")
        @Schema(description = "출생 연도(YYYY 형식)", example = "1995")
        public String birthYear;

        //생일 제공 동의 여부
        @JsonProperty("birthday_needs_agreement")
        @Schema(description = "생일 제공 동의 여부", example = "false")
        public Boolean isBirthDayAgree;

        //생일 (MMDD 형식)
        @JsonProperty("birthday")
        @Schema(description = "생일(MMDD 형식)", example = "0101")
        public String birthDay;

        //생일 타입
        // SOLAR(양력) 혹은 LUNAR(음력)
        @JsonProperty("birthday_type")
        @Schema(description = "생일 타입(SOLAR:양력, LUNAR:음력)", example = "SOLAR")
        public String birthDayType;

        //성별 제공 동의 여부
        @JsonProperty("gender_needs_agreement")
        @Schema(description = "성별 제공 동의 여부", example = "false")
        public Boolean isGenderAgree;

        //성별
        @JsonProperty("gender")
        @Schema(description = "성별(female/male)", example = "male")
        public String gender;

        //전화번호 제공 동의 여부
        @JsonProperty("phone_number_needs_agreement")
        @Schema(description = "전화번호 제공 동의 여부", example = "false")
        public Boolean isPhoneNumberAgree;

        //전화번호
        //국내 번호인 경우 +82 00-0000-0000 형식
        @JsonProperty("phone_number")
        @Schema(description = "전화번호(+82 00-0000-0000 형식)", example = "+82 10-1234-5678")
        public String phoneNumber;

        //CI 동의 여부
        @JsonProperty("ci_needs_agreement")
        @Schema(description = "CI 제공 동의 여부", example = "false")
        public Boolean isCIAgree;

        //CI, 연계 정보
        @JsonProperty("ci")
        @Schema(description = "CI 연계 정보")
        public String ci;

        //CI 발급 시각, UTC
        @JsonProperty("ci_authenticated_at")
        @Schema(description = "CI 발급 시각")
        public Date ciCreatedAt;

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Schema(description = "사용자 프로필 상세 정보")
        public class Profile {

            //닉네임
            @JsonProperty("nickname")
            @Schema(description = "닉네임", example = "길동이")
            public String nickName;

            //프로필 미리보기 이미지 URL
            @JsonProperty("thumbnail_image_url")
            @Schema(description = "프로필 미리보기 이미지 URL", example = "http://k.kakaocdn.net/dn/thumbnail.jpg")
            public String thumbnailImageUrl;

            //프로필 사진 URL
            @JsonProperty("profile_image_url")
            @Schema(description = "프로필 사진 URL", example = "http://k.kakaocdn.net/dn/profile.jpg")
            public String profileImageUrl;

            //프로필 사진 URL 기본 프로필인지 여부
            //true : 기본 프로필, false : 사용자 등록
            @JsonProperty("is_default_image")
            @Schema(description = "기본 프로필 사진 여부", example = "false")
            public String isDefaultImage;

            //닉네임이 기본 닉네임인지 여부
            //true : 기본 닉네임, false : 사용자 등록
            @JsonProperty("is_default_nickname")
            @Schema(description = "기본 닉네임 여부", example = "false")
            public Boolean isDefaultNickName;
        }
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "파트너 정보")
    public class Partner {
        //고유 ID
        @JsonProperty("uuid")
        @Schema(description = "고유 UUID", example = "12345678-abcd-1234-abcd-123456789abc")
        public String uuid;
    }
}
