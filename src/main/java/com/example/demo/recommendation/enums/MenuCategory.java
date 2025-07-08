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
    COLD("차가운", "시원한 요리"),

    // 새로운 다양한 카테고리들 추가
    FUSION("퓨전", "퓨전 요리"),
    TRADITIONAL("전통", "전통 향토 요리"),
    HEALTHY("건강식", "건강하고 영양가 있는 요리"),
    COMFORT("든든한", "포만감 있는 든든한 요리"),
    GOURMET("고급", "고급스러운 요리"),
    CASUAL("간편한", "간편하고 캐주얼한 요리"),
    SEASONAL("계절", "계절 특별 요리"),
    STREET_FOOD("길거리", "길거리 음식"),
    BAKERY("베이커리", "빵, 제과류"),
    BRUNCH("브런치", "브런치 메뉴"),
    MIDNIGHT("심야", "심야 간식"),
    DELIVERY("배달", "배달 특화 메뉴"),
    TAKEOUT("포장", "포장 전용 메뉴"),
    DIET("다이어트", "다이어트 요리"),
    PROTEIN("고단백", "고단백 요리"),
    VEGAN("비건", "비건 요리"),
    HALAL("할랄", "할랄 요리"),
    KIDS("아이", "아이들을 위한 요리"),
    SENIOR("시니어", "시니어를 위한 요리"),
    ROMANTIC("로맨틱", "로맨틱한 분위기의 요리"),
    PARTY("파티", "파티용 요리"),
    BUSINESS("비즈니스", "비즈니스 미팅용 요리"),
    COMFORT_FOOD("위로", "위로가 되는 음식"),
    ENERGY("에너지", "에너지 충전 요리"),
    REFRESHING("상쾌한", "상쾌한 요리"),
    WARMING("몸보신", "몸을 따뜻하게 하는 요리"),
    COOLING("시원한", "몸을 시원하게 하는 요리"),
    PREMIUM("프리미엄", "프리미엄 요리"),
    BUDGET("가성비", "가성비 좋은 요리"),
    QUICK("빠른", "빨리 나오는 요리"),
    SLOW("정성", "정성스럽게 조리된 요리");

    private final String korean;
    private final String description;

    MenuCategory(String korean, String description) {
        this.korean = korean;
        this.description = description;
    }
}
