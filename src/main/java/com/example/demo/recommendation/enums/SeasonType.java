package com.example.demo.recommendation.enums;

import lombok.Getter;

import java.time.LocalDate;
import java.time.Month;

@Getter
public enum SeasonType {
    SPRING("봄", "따뜻하고 상쾌한 계절"),
    SUMMER("여름", "덥고 습한 계절"),
    AUTUMN("가을", "시원하고 건조한 계절"),
    WINTER("겨울", "춥고 건조한 계절");

    private final String korean;
    private final String description;

    SeasonType(String korean, String description) {
        this.korean = korean;
        this.description = description;
    }

    public static SeasonType getCurrentSeason() {
        return getSeasonByDate(LocalDate.now());
    }

    public static SeasonType getSeasonByDate(LocalDate date) {
        Month month = date.getMonth();
        switch (month) {
            case MARCH:
            case APRIL:
            case MAY:
                return SPRING;
            case JUNE:
            case JULY:
            case AUGUST:
                return SUMMER;
            case SEPTEMBER:
            case OCTOBER:
            case NOVEMBER:
                return AUTUMN;
            case DECEMBER:
            case JANUARY:
            case FEBRUARY:
                return WINTER;
            default:
                return SPRING;
        }
    }
}
