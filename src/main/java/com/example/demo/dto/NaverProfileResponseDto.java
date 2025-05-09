package com.example.demo.dto;

import lombok.Data;

@Data
public class NaverProfileResponseDto {
    private String resultcode;
    private String message;
    private NaverProfile response;

    @Data
    public static class NaverProfile {
        private String id;
        private String email;
        private String name;
        private String nickname;
        private String gender;
        private String age;
        private String birthday;
        private String birthyear;
        private String profile_image;
        private String mobile;
    }
}
