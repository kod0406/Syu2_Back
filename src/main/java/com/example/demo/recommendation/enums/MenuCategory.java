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

    // 기존 다양한 카테고리들
    FUSION("퓨전", "퓨전 요리"),
    TRADITIONAL("전통", "전통 향토 요리"),
    HEALTHY("건강식", "건강하고 영양가 있는 요리"),
    COMFORT("든든한", "포만감 있는 든든한 요리"),
    GOURMET("고급", "고급스러운 요리"),
    CASUAL("간편한", "간편하고 캐주얼한 요리"),
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
    SLOW("정성", "정성스럽게 조리된 요리"),

    // 날씨 기반 추가 카테고리들
    RAINY_DAY("비오는날", "비오는 날에 어울리는 음식"),
    SNOWY_DAY("눈오는날", "눈 오는 날에 어울리는 음식"),
    SUNNY_DAY("맑은날", "맑은 날에 어울리는 음식"),
    CLOUDY_DAY("흐린날", "흐린 날에 어울리는 음식"),
    HUMID_DAY("습한날", "습한 날에 어울리는 음식"),
    DRY_DAY("건조한날", "건조한 날에 어울리는 음식"),
    WINDY_DAY("바람부는날", "바람 부는 날에 어울리는 음식"),

    // 온도별 세부 카테고리
    FREEZING("극한추위", "영하 10도 이하 극한 추위용"),
    VERY_COLD("매우추운", "영하 0~10도 매우 추운 날용"),
    COLD_WEATHER("쌀쌀한", "1~10도 쌀쌀한 날용"),
    COOL_WEATHER("서늘한", "11~20도 서늘한 날용"),
    MILD_WEATHER("적당한", "21~25도 적당한 날용"),
    WARM_WEATHER("따뜻한", "26~30도 따뜻한 날용"),
    VERY_HOT("매우더운", "31~35도 매우 더운 날용"),
    EXTREME_HOT("극한더위", "36도 이상 극한 더위용"),

    // 시간대별 특화 카테고리
    EARLY_MORNING("이른아침", "새벽 5~7시 이른 아침용"),
    MORNING("아침", "아침 7~10시용"),
    LATE_MORNING("늦은아침", "늦은 아침 10~12시용"),
    LUNCH("점심", "점심 12~14시용"),
    AFTERNOON("오후", "오후 14~17시용"),
    EARLY_DINNER("이른저녁", "이른 저녁 17~19시용"),
    DINNER("저녁", "저녁 19~21시용"),
    LATE_NIGHT("늦은저녁", "늦은 저녁 21~24시용"),
    DAWN("새벽", "새벽 0~5시용"),

    // 계절별 특화 카테고리
    SPRING_FRESH("봄신선", "봄철 신선한 요리"),
    SUMMER_COOL("여름시원", "여름철 시원한 요리"),
    AUTUMN_HEARTY("가을든든", "가을철 든든한 요리"),
    WINTER_WARM("겨울따뜻", "겨울철 따뜻한 요리"),

    // 특별 상황별 카테고리
    STORM("폭풍우", "폭풍우 날씨용"),
    TYPHOON("태풍", "태풍 날씨용"),
    FINE_DUST("미세먼지", "미세먼지 많은 날용"),
    YELLOW_DUST("황사", "황사 날씨용"),
    HEAT_WAVE("폭염", "폭염 날씨용"),
    COLD_WAVE("한파", "한파 날씨용"),

    // 습도별 카테고리
    VERY_DRY("매우건조", "습도 30% 이하 매우 건조한 날용"),
    MODERATE_HUMIDITY("적당습도", "습도 31~50% 적당한 날용"),
    HUMID("습함", "습도 51~70% 습한 날용"),
    VERY_HUMID("매우습함", "습도 71% 이상 매우 습한 날용"),

    // 기분/감정별 카테고리
    MOOD_BOOST("기분전환", "기분을 좋게 하는 음식"),
    STRESS_RELIEF("스트레스해소", "스트레스 해소에 도움되는 음식"),
    NOSTALGIC("추억", "추억을 떠올리게 하는 음식"),
    CELEBRATION("축하", "축하할 때 먹는 음식"),

    // 건강 상태별 카테고리
    IMMUNITY("면역력", "면역력 증진에 도움되는 음식"),
    DIGESTIVE("소화", "소화에 좋은 음식"),
    ANTI_INFLAMMATORY("항염", "항염 효과가 있는 음식"),
    DETOX("해독", "해독에 도움되는 음식"),

    // 활동별 카테고리
    WORK("업무", "업무 중 먹기 좋은 음식"),
    STUDY("공부", "공부할 때 먹기 좋은 음식"),
    RELAXATION("휴식", "휴식할 때 먹기 좋은 음식"),
    OUTDOOR("야외", "야외 활동에 좋은 음식"),

    // 사회적 상황별 카테고리
    FAMILY("가족", "가족과 함께 먹기 좋은 음식"),
    FRIENDS("친구", "친구와 함께 먹기 좋은 음식"),
    DATE("데이트", "데이트할 때 먹기 좋은 음식"),
    ALONE("혼자", "혼자 먹기 좋은 음식"),

    // 경제적 상황별 카테고리
    LUXURY("럭셔리", "특별한 날 먹는 고급 음식"),
    ECONOMICAL("경제적", "경제적인 가격의 음식"),
    VALUE("가치", "가격 대비 가치가 높은 음식"),

    // 조리 방식별 카테고리
    GRILLED("구이", "구워서 만든 음식"),
    STEAMED("찜", "찐 음식"),
    FRIED("튀김", "튀긴 음식"),
    BOILED("삶은", "삶은 음식"),
    RAW("생", "생으로 먹는 음식"),
    FERMENTED("발효", "발효 음식"),

    // 영양소별 카테고리
    HIGH_FIBER("고섬유", "섬유질이 많은 음식"),
    LOW_SODIUM("저나트륨", "나트륨이 적은 음식"),
    HIGH_CALCIUM("고칼슘", "칼슘이 많은 음식"),
    VITAMIN_RICH("비타민", "비타민이 풍부한 음식"),

    // 특별한 식이 요구사항
    GLUTEN_FREE("글루텐프리", "글루텐이 없는 음식"),
    SUGAR_FREE("무설탕", "설탕이 없는 음식"),
    LACTOSE_FREE("유당불내증", "유당이 없는 음식"),
    KETO("케토", "케토 다이어트용 음식"),

    // 지역별 특색 카테고리
    SEOUL("서울", "서울 지역 특색 음식"),
    BUSAN("부산", "부산 지역 특색 음식"),
    JEJU("제주", "제주 지역 특색 음식"),
    GYEONGGI("경기", "경기 지역 특색 음식"),
    INCHEON("인천", "인천 지역 특색 음식"),
    DAEGU("대구", "대구 지역 특색 음식"),
    GWANGJU("광주", "광주 지역 특색 음식"),
    DAEJEON("대전", "대전 지역 특색 음식"),
    ULSAN("울산", "울산 지역 특색 음식"),
    CHUNCHEON("춘천", "춘천 지역 특색 음식"),
    ANDONG("안동", "안동 지역 특색 음식"),
    JEONJU("전주", "전주 지역 특색 음식"),

    // 트렌드별 카테고리
    TRENDY("트렌디", "최신 트렌드 음식"),
    CLASSIC("클래식", "클래식한 음식"),
    INNOVATIVE("혁신적", "혁신적인 음식"),
    RETRO("레트로", "레트로 감성 음식"),

    // 추가 누락 카테고리들
    TEENS("10대", "10대를 위한 음식"),
    CHEESE("치즈", "치즈가 들어간 음식"),
    WINE("와인", "와인과 어울리는 음식"),
    BEER("맥주", "맥주와 어울리는 음식"),
    SWEET("달콤한", "달콤한 음식"),
    MILD("순한", "순한 맛의 음식"),
    SIMPLE("단순한", "단순하고 간단한 음식"),
    CALCIUM("칼슘", "칼슘이 풍부한 음식");

    private final String korean;
    private final String description;

    MenuCategory(String korean, String description) {
        this.korean = korean;
        this.description = description;
    }
}
