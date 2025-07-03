package com.example.demo.recommendation.enums;

import lombok.Getter;

@Getter
public enum MenuCategory {
    KOREAN("한식", "전통 한국 요리"),
    CHINESE("중식", "중국 요리"),
    JAPANESE("일식", "일본 요리"),
    WESTERN("양식", "서양 요리"),
    ITALIAN("이탈리안", "이탈리아 요리"),
    FAST_FOOD("패스트푸드", "패스트푸드"),
    DESSERT("디저트", "디저트 및 음료"),
    COFFEE("카페", "커피 및 음료"),
    SOUP("국물요리", "국, 탕, 찌개류"),
    NOODLE("면류", "라면, 파스타, 우동 등"),
    RICE("밥류", "볶음밥, 덮밥 등"),
    MEAT("육류", "고기 요리"),
    SEAFOOD("해산물", "해산물 요리"),
    VEGETARIAN("채식", "채식 요리"),
    SPICY("매운맛", "매운 요리"),
    LIGHT("가벼운", "가벼운 요리"),
    HOT("따뜻한", "따뜻한 요리"),
    COLD("차가운", "시원한 요리");

    private final String korean;
    private final String description;

    MenuCategory(String korean, String description) {
        this.korean = korean;
        this.description = description;
    }
}
