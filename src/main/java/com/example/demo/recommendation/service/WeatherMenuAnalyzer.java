package com.example.demo.recommendation.service;

import com.example.demo.recommendation.dto.WeatherMenuSuggestion;
import com.example.demo.recommendation.enums.MenuCategory;
import com.example.demo.recommendation.enums.WeatherType;
import com.example.demo.recommendation.enums.SeasonType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherMenuAnalyzer {

    // 날씨와 계절에 따른 메뉴 카테고리 추천
    public List<MenuCategory> suggestMenuCategories(WeatherType weatherType, SeasonType season) {
        List<MenuCategory> suggestions = new ArrayList<>();

        // 날씨별 기본 추천
        suggestions.addAll(getWeatherBasedCategories(weatherType));

        // 계절별 추천 추가
        suggestions.addAll(getSeasonBasedCategories(season));

        // 중복 제거 및 우선순위 정렬
        return suggestions.stream()
            .distinct()
            .sorted(this::compareMenuCategoryPriority)
            .limit(5)
            .toList();
    }

    // 상세한 날씨 메뉴 제안 생성
    public WeatherMenuSuggestion generateDetailedSuggestion(WeatherType weatherType, SeasonType season,
                                                           Double temperature, Integer humidity) {
        List<MenuCategory> categories = suggestMenuCategories(weatherType, season);
        String reason = generateSuggestionReason(weatherType, season, temperature, humidity);
        String suggestion = generateSuggestionText(weatherType, season, temperature);
        List<String> keywords = generateKeywords(weatherType, season, temperature);

        return WeatherMenuSuggestion.builder()
            .weatherType(weatherType)
            .season(season)
            .recommendedCategories(categories)
            .reason(reason)
            .temperature(temperature)
            .humidity(humidity)
            .suggestion(suggestion)
            .priority(calculatePriority(weatherType, season))
            .keywords(keywords)
            .build();
    }

    // 날씨별 메뉴 카테고리 매핑
    private List<MenuCategory> getWeatherBasedCategories(WeatherType weatherType) {
        return switch (weatherType) {
            case RAIN, DRIZZLE -> Arrays.asList(
                MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.KOREAN, MenuCategory.NOODLE
            );
            case SNOW, COLD -> Arrays.asList(
                MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.SPICY, MenuCategory.MEAT
            );
            case HOT, CLEAR -> Arrays.asList(
                MenuCategory.COLD, MenuCategory.LIGHT, MenuCategory.DESSERT, MenuCategory.COFFEE
            );
            case THUNDERSTORM -> Arrays.asList(
                MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.KOREAN, MenuCategory.SPICY
            );
            case CLOUDS, MIST, FOG -> Arrays.asList(
                MenuCategory.COFFEE, MenuCategory.HOT, MenuCategory.LIGHT, MenuCategory.DESSERT
            );
            case HUMID -> Arrays.asList(
                MenuCategory.COLD, MenuCategory.LIGHT, MenuCategory.SEAFOOD, MenuCategory.VEGETARIAN
            );
            default -> Arrays.asList(
                MenuCategory.KOREAN, MenuCategory.RICE, MenuCategory.SOUP
            );
        };
    }

    // 계절별 메뉴 카테고리 매핑
    private List<MenuCategory> getSeasonBasedCategories(SeasonType season) {
        return switch (season) {
            case SPRING -> Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.VEGETARIAN, MenuCategory.KOREAN, MenuCategory.SEAFOOD
            );
            case SUMMER -> Arrays.asList(
                MenuCategory.COLD, MenuCategory.LIGHT, MenuCategory.DESSERT, MenuCategory.SEAFOOD
            );
            case AUTUMN -> Arrays.asList(
                MenuCategory.MEAT, MenuCategory.KOREAN, MenuCategory.HOT, MenuCategory.SOUP
            );
            case WINTER -> Arrays.asList(
                MenuCategory.HOT, MenuCategory.SPICY, MenuCategory.SOUP, MenuCategory.MEAT
            );
        };
    }

    // 메뉴 카테고리 우선순위 비교
    private int compareMenuCategoryPriority(MenuCategory a, MenuCategory b) {
        Map<MenuCategory, Integer> priority = Map.of(
            MenuCategory.HOT, 1,
            MenuCategory.COLD, 2,
            MenuCategory.SOUP, 3,
            MenuCategory.SPICY, 4,
            MenuCategory.KOREAN, 5,
            MenuCategory.LIGHT, 6,
            MenuCategory.MEAT, 7,
            MenuCategory.DESSERT, 8
        );

        return Integer.compare(
            priority.getOrDefault(a, 10),
            priority.getOrDefault(b, 10)
        );
    }

    // 추천 이유 생성
    private String generateSuggestionReason(WeatherType weatherType, SeasonType season,
                                          Double temperature, Integer humidity) {
        StringBuilder reason = new StringBuilder();

        // 날씨 기반 이유
        switch (weatherType) {
            case RAIN, DRIZZLE -> reason.append("비가 내리는 날씨에는 따뜻한 국물 요리가 좋습니다. ");
            case SNOW, COLD -> reason.append("추운 날씨에는 몸을 따뜻하게 해주는 음식이 인기입니다. ");
            case HOT -> reason.append("더운 날씨에는 시원하고 가벼운 음식이 선호됩니다. ");
            case HUMID -> reason.append("습한 날씨에는 담백하고 가벼운 음식이 좋습니다. ");
            default -> reason.append("현재 날씨에 적합한 메뉴를 추천합니다. ");
        }

        // 계절 기반 이유 추가
        switch (season) {
            case SPRING -> reason.append("봄철에는 신선한 재료의 음식이 인기입니다.");
            case SUMMER -> reason.append("여름철에는 시원한 음식과 디저트가 인기입니다.");
            case AUTUMN -> reason.append("가을철에는 든든한 음식이 선호됩니다.");
            case WINTER -> reason.append("겨울철에는 따뜻하고 매운 음식이 인기입니다.");
        }

        return reason.toString();
    }

    // 구체적인 제안 텍스트 생성
    private String generateSuggestionText(WeatherType weatherType, SeasonType season, Double temperature) {
        if (temperature != null) {
            if (temperature < 0) {
                return "영하의 추위에는 뜨거운 국물 요리나 매운 음식으로 몸을 따뜻하게 해보세요.";
            } else if (temperature > 30) {
                return "무더운 날씨에는 시원한 음료나 가벼운 디저트로 더위를 식혀보세요.";
            } else if (temperature > 25) {
                return "따뜻한 날씨에는 시원한 음식이나 가벼운 메뉴가 좋습니다.";
            } else if (temperature < 10) {
                return "쌀쌀한 날씨에는 따뜻한 음식으로 몸을 데워보세요.";
            }
        }

        return switch (weatherType) {
            case RAIN -> "비 오는 날에는 따뜻한 국물 요리로 기분을 따뜻하게 해보세요.";
            case SNOW -> "눈 내리는 날에는 매운 음식으로 몸을 따뜻하게 해보세요.";
            case HOT -> "더운 날씨에는 시원한 음료와 가벼운 음식이 최고입니다.";
            default -> "현재 날씨에 딱 맞는 메뉴로 고객을 맞이해보세요.";
        };
    }

    // 키워드 생성
    private List<String> generateKeywords(WeatherType weatherType, SeasonType season, Double temperature) {
        List<String> keywords = new ArrayList<>();

        // 날씨 키워드
        switch (weatherType) {
            case RAIN, DRIZZLE -> keywords.addAll(Arrays.asList("따뜻한", "국물", "우산", "실내"));
            case SNOW, COLD -> keywords.addAll(Arrays.asList("뜨거운", "매운", "겨울", "따뜻함"));
            case HOT -> keywords.addAll(Arrays.asList("시원한", "차가운", "여름", "더위"));
            case HUMID -> keywords.addAll(Arrays.asList("담백한", "가벼운", "습도", "상쾌한"));
            default -> keywords.addAll(Arrays.asList("맛있는", "신선한", "건강한"));
        }

        // 계절 키워드
        switch (season) {
            case SPRING -> keywords.addAll(Arrays.asList("봄", "신선한", "가벼운"));
            case SUMMER -> keywords.addAll(Arrays.asList("여름", "시원한", "상쾌한"));
            case AUTUMN -> keywords.addAll(Arrays.asList("가을", "든든한", "영양"));
            case WINTER -> keywords.addAll(Arrays.asList("겨울", "따뜻한", "매운"));
        }

        return keywords.stream().distinct().limit(8).toList();
    }

    // 우선순위 계산
    private Integer calculatePriority(WeatherType weatherType, SeasonType season) {
        int priority = 5; // 기본 우선순위

        // 극한 날씨일수록 높은 우선순위
        switch (weatherType) {
            case SNOW, THUNDERSTORM, HOT -> priority = 1;
            case RAIN, COLD -> priority = 2;
            case HUMID, DRIZZLE -> priority = 3;
            case CLOUDS, MIST -> priority = 4;
            default -> priority = 5;
        }

        return priority;
    }
}
