package com.example.demo.recommendation.enums;

import lombok.Getter;

@Getter
public enum WeatherType {
    CLEAR("맑음", "clear"),
    CLOUDS("흐림", "clouds"),
    RAIN("비", "rain"),
    DRIZZLE("이슬비", "drizzle"),
    THUNDERSTORM("뇌우", "thunderstorm"),
    SNOW("눈", "snow"),
    MIST("안개", "mist"),
    FOG("짙은안개", "fog"),
    HAZE("실안개", "haze"),
    DUST("황사", "dust"),
    SMOKE("연기", "smoke"),
    HOT("더위", "hot"),
    COLD("추위", "cold"),
    HUMID("습함", "humid"),
    DRY("건조함", "dry");

    private final String korean;
    private final String code;

    WeatherType(String korean, String code) {
        this.korean = korean;
        this.code = code;
    }

    public static WeatherType fromCode(String code) {
        for (WeatherType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return CLEAR; // 기본값
    }
}
