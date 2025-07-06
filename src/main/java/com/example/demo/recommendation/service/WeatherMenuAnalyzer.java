package com.example.demo.recommendation.service;

import com.example.demo.recommendation.dto.WeatherMenuSuggestion;
import com.example.demo.recommendation.enums.MenuCategory;
import com.example.demo.recommendation.enums.WeatherType;
import com.example.demo.recommendation.enums.SeasonType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

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

    // 온도와 습도를 고려한 고급 카테고리 추천
    public List<MenuCategory> suggestAdvancedMenuCategories(WeatherType weatherType, SeasonType season,
                                                           Double temperature, Integer humidity) {
        List<MenuCategory> suggestions = new ArrayList<>();

        // 기본 날씨/계절 추천
        suggestions.addAll(getWeatherBasedCategories(weatherType));
        suggestions.addAll(getSeasonBasedCategories(season));

        // 온도별 세밀한 추천
        if (temperature != null) {
            suggestions.addAll(getTemperatureBasedCategories(temperature));
        }

        // 습도별 추천
        if (humidity != null) {
            suggestions.addAll(getHumidityBasedCategories(humidity));
        }

        // 복합 날씨 조건 추천
        suggestions.addAll(getComplexWeatherCategories(weatherType, temperature, humidity));

        // 시간대별 추천 추가
        suggestions.addAll(getTimeBasedCategories());

        return suggestions.stream()
            .distinct()
            .sorted(this::compareMenuCategoryPriority)
            .limit(8)
            .toList();
    }

    // 시간대별 메뉴 카테고리 추천
    public List<MenuCategory> suggestCategoriesByTime(LocalTime currentTime) {
        return getTimeBasedCategories(currentTime);
    }

    // 복합 조건 추천 (날씨 + 온도 + 습도 + 시간)
    public List<MenuCategory> suggestComprehensiveCategories(WeatherType weatherType, SeasonType season,
                                                           Double temperature, Integer humidity, LocalTime time) {
        List<MenuCategory> suggestions = new ArrayList<>();

        // 모든 조건별 추천 수집
        suggestions.addAll(getWeatherBasedCategories(weatherType));
        suggestions.addAll(getSeasonBasedCategories(season));

        if (temperature != null) {
            suggestions.addAll(getTemperatureBasedCategories(temperature));
        }

        if (humidity != null) {
            suggestions.addAll(getHumidityBasedCategories(humidity));
        }

        if (time != null) {
            suggestions.addAll(getTimeBasedCategories(time));
        }

        // 복합 조건 추천
        suggestions.addAll(getComplexWeatherCategories(weatherType, temperature, humidity));
        suggestions.addAll(getSeasonTimeCategories(season, time));

        return suggestions.stream()
            .distinct()
            .sorted(this::compareMenuCategoryPriority)
            .limit(10)
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

    // 날씨별 메뉴 카테고리 매핑 - 확장된 버전
    private List<MenuCategory> getWeatherBasedCategories(WeatherType weatherType) {
        return switch (weatherType) {
            case RAIN, DRIZZLE -> Arrays.asList(
                MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.KOREAN, MenuCategory.NOODLE,
                MenuCategory.SPICY, MenuCategory.COFFEE, MenuCategory.CHINESE
            );
            case SNOW, COLD -> Arrays.asList(
                MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.SPICY, MenuCategory.MEAT,
                MenuCategory.KOREAN, MenuCategory.NOODLE, MenuCategory.COFFEE
            );
            case HOT -> Arrays.asList(
                MenuCategory.COLD, MenuCategory.LIGHT, MenuCategory.DESSERT, MenuCategory.COFFEE,
                MenuCategory.SEAFOOD, MenuCategory.VEGETARIAN, MenuCategory.JAPANESE
            );
            case CLEAR -> Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.KOREAN, MenuCategory.RICE, MenuCategory.VEGETARIAN,
                MenuCategory.JAPANESE, MenuCategory.WESTERN, MenuCategory.COFFEE
            );
            case THUNDERSTORM -> Arrays.asList(
                MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.KOREAN, MenuCategory.SPICY,
                MenuCategory.MEAT, MenuCategory.NOODLE, MenuCategory.COFFEE
            );
            case CLOUDS -> Arrays.asList(
                MenuCategory.COFFEE, MenuCategory.HOT, MenuCategory.LIGHT, MenuCategory.DESSERT,
                MenuCategory.KOREAN, MenuCategory.WESTERN, MenuCategory.RICE
            );
            case MIST, FOG -> Arrays.asList(
                MenuCategory.COFFEE, MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.KOREAN,
                MenuCategory.LIGHT, MenuCategory.DESSERT, MenuCategory.WESTERN
            );
            case HUMID -> Arrays.asList(
                MenuCategory.COLD, MenuCategory.LIGHT, MenuCategory.SEAFOOD, MenuCategory.VEGETARIAN,
                MenuCategory.JAPANESE, MenuCategory.DESSERT, MenuCategory.COFFEE
            );
            case HAZE, DUST -> Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.VEGETARIAN, MenuCategory.SOUP, MenuCategory.COFFEE,
                MenuCategory.COLD, MenuCategory.SEAFOOD, MenuCategory.JAPANESE
            );
            case SMOKE -> Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.COLD, MenuCategory.VEGETARIAN, MenuCategory.SEAFOOD,
                MenuCategory.COFFEE, MenuCategory.DESSERT, MenuCategory.JAPANESE
            );
            case DRY -> Arrays.asList(
                MenuCategory.SOUP, MenuCategory.HOT, MenuCategory.KOREAN, MenuCategory.COFFEE,
                MenuCategory.LIGHT, MenuCategory.SEAFOOD, MenuCategory.RICE
            );
            default -> Arrays.asList(
                MenuCategory.KOREAN, MenuCategory.RICE, MenuCategory.SOUP, MenuCategory.LIGHT,
                MenuCategory.COFFEE, MenuCategory.WESTERN, MenuCategory.JAPANESE
            );
        };
    }

    // 계절별 메뉴 카테고리 매핑 - 확장된 버전
    private List<MenuCategory> getSeasonBasedCategories(SeasonType season) {
        return switch (season) {
            case SPRING -> Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.VEGETARIAN, MenuCategory.KOREAN, MenuCategory.SEAFOOD,
                MenuCategory.JAPANESE, MenuCategory.RICE, MenuCategory.COFFEE, MenuCategory.WESTERN
            );
            case SUMMER -> Arrays.asList(
                MenuCategory.COLD, MenuCategory.LIGHT, MenuCategory.DESSERT, MenuCategory.SEAFOOD,
                MenuCategory.COFFEE, MenuCategory.JAPANESE, MenuCategory.VEGETARIAN, MenuCategory.WESTERN
            );
            case AUTUMN -> Arrays.asList(
                MenuCategory.MEAT, MenuCategory.KOREAN, MenuCategory.HOT, MenuCategory.SOUP,
                MenuCategory.SPICY, MenuCategory.RICE, MenuCategory.CHINESE, MenuCategory.WESTERN
            );
            case WINTER -> Arrays.asList(
                MenuCategory.HOT, MenuCategory.SPICY, MenuCategory.SOUP, MenuCategory.MEAT,
                MenuCategory.KOREAN, MenuCategory.NOODLE, MenuCategory.COFFEE, MenuCategory.CHINESE
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

    // 온도별 세밀한 카테고리 추천
    private List<MenuCategory> getTemperatureBasedCategories(Double temperature) {
        if (temperature == null) return List.of();

        if (temperature <= -10) {
            // 극한 추위 (-10도 이하)
            return Arrays.asList(
                MenuCategory.HOT, MenuCategory.SPICY, MenuCategory.SOUP, MenuCategory.MEAT,
                MenuCategory.KOREAN, MenuCategory.CHINESE, MenuCategory.COFFEE, MenuCategory.WARMING
            );
        } else if (temperature <= 0) {
            // 영하 (0도 ~ -10도)
            return Arrays.asList(
                MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.SPICY, MenuCategory.MEAT,
                MenuCategory.KOREAN, MenuCategory.NOODLE, MenuCategory.COFFEE, MenuCategory.WARMING
            );
        } else if (temperature <= 10) {
            // 쌀쌀함 (1도 ~ 10도)
            return Arrays.asList(
                MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.KOREAN, MenuCategory.RICE,
                MenuCategory.MEAT, MenuCategory.COFFEE, MenuCategory.WESTERN, MenuCategory.COMFORT
            );
        } else if (temperature <= 20) {
            // 서늘함 (11도 ~ 20도)
            return Arrays.asList(
                MenuCategory.KOREAN, MenuCategory.RICE, MenuCategory.SOUP, MenuCategory.WESTERN,
                MenuCategory.JAPANESE, MenuCategory.COFFEE, MenuCategory.LIGHT, MenuCategory.CASUAL
            );
        } else if (temperature <= 25) {
            // 적당함 (21도 ~ 25도)
            return Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.KOREAN, MenuCategory.JAPANESE, MenuCategory.WESTERN,
                MenuCategory.RICE, MenuCategory.VEGETARIAN, MenuCategory.COFFEE, MenuCategory.SEAFOOD,
                MenuCategory.HEALTHY, MenuCategory.REFRESHING
            );
        } else if (temperature <= 30) {
            // 따뜻함 (26도 ~ 30도)
            return Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.COLD, MenuCategory.SEAFOOD, MenuCategory.VEGETARIAN,
                MenuCategory.JAPANESE, MenuCategory.WESTERN, MenuCategory.COFFEE, MenuCategory.DESSERT,
                MenuCategory.REFRESHING, MenuCategory.COOLING
            );
        } else if (temperature <= 35) {
            // 더움 (31도 ~ 35도)
            return Arrays.asList(
                MenuCategory.COLD, MenuCategory.LIGHT, MenuCategory.DESSERT, MenuCategory.COFFEE,
                MenuCategory.SEAFOOD, MenuCategory.VEGETARIAN, MenuCategory.JAPANESE,
                MenuCategory.COOLING, MenuCategory.REFRESHING
            );
        } else {
            // 극한 더위 (36도 이상)
            return Arrays.asList(
                MenuCategory.COLD, MenuCategory.DESSERT, MenuCategory.COFFEE, MenuCategory.LIGHT,
                MenuCategory.VEGETARIAN, MenuCategory.SEAFOOD, MenuCategory.JAPANESE,
                MenuCategory.COOLING, MenuCategory.VEGAN
            );
        }
    }

    // 습도별 카테고리 추천
    private List<MenuCategory> getHumidityBasedCategories(Integer humidity) {
        if (humidity == null) return List.of();

        if (humidity <= 30) {
            // 건조함 (30% 이하)
            return Arrays.asList(
                MenuCategory.SOUP, MenuCategory.HOT, MenuCategory.KOREAN, MenuCategory.COFFEE,
                MenuCategory.SEAFOOD, MenuCategory.RICE, MenuCategory.NOODLE, MenuCategory.HEALTHY
            );
        } else if (humidity <= 50) {
            // 적당함 (31% ~ 50%)
            return Arrays.asList(
                MenuCategory.KOREAN, MenuCategory.WESTERN, MenuCategory.JAPANESE, MenuCategory.RICE,
                MenuCategory.MEAT, MenuCategory.SEAFOOD, MenuCategory.COFFEE, MenuCategory.CASUAL
            );
        } else if (humidity <= 70) {
            // 습함 (51% ~ 70%)
            return Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.VEGETARIAN, MenuCategory.SEAFOOD, MenuCategory.JAPANESE,
                MenuCategory.COFFEE, MenuCategory.COLD, MenuCategory.DESSERT, MenuCategory.REFRESHING
            );
        } else {
            // 매우 습함 (71% 이상)
            return Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.COLD, MenuCategory.VEGETARIAN, MenuCategory.SEAFOOD,
                MenuCategory.DESSERT, MenuCategory.COFFEE, MenuCategory.JAPANESE, MenuCategory.COOLING
            );
        }
    }

    // 시간대별 카테고리 추천 (현재 시간)
    private List<MenuCategory> getTimeBasedCategories() {
        return getTimeBasedCategories(LocalTime.now());
    }

    // 시간대별 카테고리 추천 (지정 시간)
    private List<MenuCategory> getTimeBasedCategories(LocalTime time) {
        if (time == null) return List.of();

        int hour = time.getHour();

        if (hour >= 6 && hour < 10) {
            // 아침 시간대 (6시 ~ 10시)
            return Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.COFFEE, MenuCategory.RICE, MenuCategory.KOREAN,
                MenuCategory.WESTERN, MenuCategory.DESSERT, MenuCategory.VEGETARIAN, MenuCategory.BRUNCH,
                MenuCategory.HEALTHY, MenuCategory.QUICK
            );
        } else if (hour >= 10 && hour < 14) {
            // 점심 시간대 (10시 ~ 14시)
            return Arrays.asList(
                MenuCategory.KOREAN, MenuCategory.RICE, MenuCategory.MEAT, MenuCategory.SOUP,
                MenuCategory.CHINESE, MenuCategory.WESTERN, MenuCategory.JAPANESE, MenuCategory.NOODLE,
                MenuCategory.COMFORT, MenuCategory.ENERGY
            );
        } else if (hour >= 14 && hour < 17) {
            // 오후 간식 시간대 (14시 ~ 17시)
            return Arrays.asList(
                MenuCategory.COFFEE, MenuCategory.DESSERT, MenuCategory.LIGHT, MenuCategory.VEGETARIAN,
                MenuCategory.JAPANESE, MenuCategory.WESTERN, MenuCategory.COLD, MenuCategory.BAKERY,
                MenuCategory.CASUAL, MenuCategory.QUICK
            );
        } else if (hour >= 17 && hour < 21) {
            // 저녁 시간대 (17시 ~ 21시)
            return Arrays.asList(
                MenuCategory.KOREAN, MenuCategory.MEAT, MenuCategory.SOUP, MenuCategory.SPICY,
                MenuCategory.CHINESE, MenuCategory.WESTERN, MenuCategory.JAPANESE, MenuCategory.RICE,
                MenuCategory.COMFORT, MenuCategory.PREMIUM
            );
        } else if (hour >= 21 && hour < 24) {
            // 야식 시간대 (21시 ~ 24시)
            return Arrays.asList(
                MenuCategory.SPICY, MenuCategory.HOT, MenuCategory.NOODLE, MenuCategory.KOREAN,
                MenuCategory.CHINESE, MenuCategory.MEAT, MenuCategory.SOUP, MenuCategory.MIDNIGHT,
                MenuCategory.DELIVERY, MenuCategory.COMFORT_FOOD
            );
        } else {
            // 심야/새벽 시간대 (0시 ~ 6시)
            return Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.COFFEE, MenuCategory.HOT, MenuCategory.SOUP,
                MenuCategory.NOODLE, MenuCategory.KOREAN, MenuCategory.DESSERT, MenuCategory.MIDNIGHT,
                MenuCategory.QUICK, MenuCategory.CASUAL
            );
        }
    }

    // 복합 날씨 조건별 카테고리 추천 (날씨 + 온도 + 습도)
    private List<MenuCategory> getComplexWeatherCategories(WeatherType weatherType, Double temperature, Integer humidity) {
        List<MenuCategory> suggestions = new ArrayList<>();

        // 비오는 추운 날 + 높은 습도
        if ((weatherType == WeatherType.RAIN || weatherType == WeatherType.DRIZZLE)
            && temperature != null && temperature < 15
            && humidity != null && humidity > 70) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.HOT, MenuCategory.SPICY, MenuCategory.SOUP, MenuCategory.KOREAN,
                MenuCategory.NOODLE, MenuCategory.COFFEE, MenuCategory.MEAT
            ));
        }

        // 더운 여름날 + 높은 습도 (무덥고 끈적한 날)
        if (weatherType == WeatherType.HOT
            && temperature != null && temperature > 28
            && humidity != null && humidity > 60) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.COLD, MenuCategory.LIGHT, MenuCategory.DESSERT, MenuCategory.COFFEE,
                MenuCategory.VEGETARIAN, MenuCategory.SEAFOOD, MenuCategory.JAPANESE
            ));
        }

        // 눈오는 극한 추위
        if (weatherType == WeatherType.SNOW
            && temperature != null && temperature < -5) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.HOT, MenuCategory.SPICY, MenuCategory.SOUP, MenuCategory.MEAT,
                MenuCategory.KOREAN, MenuCategory.CHINESE, MenuCategory.NOODLE
            ));
        }

        // 건조하고 차가운 날씨 (겨울 건조)
        if ((weatherType == WeatherType.COLD || weatherType == WeatherType.DRY)
            && temperature != null && temperature < 10
            && humidity != null && humidity < 40) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.SOUP, MenuCategory.HOT, MenuCategory.KOREAN, MenuCategory.NOODLE,
                MenuCategory.COFFEE, MenuCategory.RICE, MenuCategory.MEAT
            ));
        }

        // 맑고 따뜻한 날 (완벽한 날씨)
        if (weatherType == WeatherType.CLEAR
            && temperature != null && temperature >= 20 && temperature <= 25
            && humidity != null && humidity >= 40 && humidity <= 60) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.KOREAN, MenuCategory.JAPANESE, MenuCategory.WESTERN,
                MenuCategory.VEGETARIAN, MenuCategory.SEAFOOD, MenuCategory.RICE, MenuCategory.COFFEE
            ));
        }

        // 안개 낀 쌀쌀한 아침
        if ((weatherType == WeatherType.MIST || weatherType == WeatherType.FOG)
            && temperature != null && temperature < 15) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.HOT, MenuCategory.COFFEE, MenuCategory.SOUP, MenuCategory.KOREAN,
                MenuCategory.LIGHT, MenuCategory.WESTERN, MenuCategory.RICE
            ));
        }

        // 뇌우가 치는 날 (극한 날씨)
        if (weatherType == WeatherType.THUNDERSTORM) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.SPICY, MenuCategory.KOREAN,
                MenuCategory.MEAT, MenuCategory.NOODLE, MenuCategory.COFFEE, MenuCategory.CHINESE
            ));
        }

        // 황사/먼지가 많은 날
        if (weatherType == WeatherType.DUST || weatherType == WeatherType.HAZE) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.VEGETARIAN, MenuCategory.SOUP, MenuCategory.SEAFOOD,
                MenuCategory.JAPANESE, MenuCategory.COFFEE, MenuCategory.COLD
            ));
        }

        return suggestions;
    }

    // 계절 + 시간대 조합별 추천
    private List<MenuCategory> getSeasonTimeCategories(SeasonType season, LocalTime time) {
        if (time == null) return List.of();

        int hour = time.getHour();
        List<MenuCategory> suggestions = new ArrayList<>();

        // 봄 계절별 시간대 추천
        if (season == SeasonType.SPRING) {
            if (hour >= 6 && hour < 12) {
                // 봄 아침/점심: 신선하고 가벼운 메뉴
                suggestions.addAll(Arrays.asList(
                    MenuCategory.LIGHT, MenuCategory.VEGETARIAN, MenuCategory.SEAFOOD, MenuCategory.JAPANESE,
                    MenuCategory.COFFEE, MenuCategory.RICE, MenuCategory.KOREAN
                ));
            } else if (hour >= 12 && hour < 18) {
                // 봄 오후: 균형잡힌 메뉴
                suggestions.addAll(Arrays.asList(
                    MenuCategory.KOREAN, MenuCategory.WESTERN, MenuCategory.JAPANESE, MenuCategory.RICE,
                    MenuCategory.SEAFOOD, MenuCategory.VEGETARIAN, MenuCategory.COFFEE
                ));
            } else {
                // 봄 저녁: 가볍고 따뜻한 메뉴
                suggestions.addAll(Arrays.asList(
                    MenuCategory.LIGHT, MenuCategory.SOUP, MenuCategory.KOREAN, MenuCategory.SEAFOOD,
                    MenuCategory.JAPANESE, MenuCategory.WESTERN, MenuCategory.COFFEE
                ));
            }
        }

        // 여름 계절별 시간대 추천
        else if (season == SeasonType.SUMMER) {
            if (hour >= 6 && hour < 12) {
                // 여름 아침/점심: 시원하고 가벼운 메뉴
                suggestions.addAll(Arrays.asList(
                    MenuCategory.COLD, MenuCategory.LIGHT, MenuCategory.SEAFOOD, MenuCategory.VEGETARIAN,
                    MenuCategory.JAPANESE, MenuCategory.COFFEE, MenuCategory.DESSERT
                ));
            } else if (hour >= 12 && hour < 18) {
                // 여름 오후: 시원한 간식과 음료
                suggestions.addAll(Arrays.asList(
                    MenuCategory.COLD, MenuCategory.DESSERT, MenuCategory.COFFEE, MenuCategory.LIGHT,
                    MenuCategory.SEAFOOD, MenuCategory.JAPANESE, MenuCategory.VEGETARIAN
                ));
            } else {
                // 여름 저녁: 시원하면서도 든든한 메뉴
                suggestions.addAll(Arrays.asList(
                    MenuCategory.COLD, MenuCategory.SEAFOOD, MenuCategory.LIGHT, MenuCategory.KOREAN,
                    MenuCategory.JAPANESE, MenuCategory.WESTERN, MenuCategory.VEGETARIAN
                ));
            }
        }

        // 가을 계절별 시간대 추천
        else if (season == SeasonType.AUTUMN) {
            if (hour >= 6 && hour < 12) {
                // 가을 아침/점심: 든든하고 영양가 있는 메뉴
                suggestions.addAll(Arrays.asList(
                    MenuCategory.KOREAN, MenuCategory.RICE, MenuCategory.MEAT, MenuCategory.SOUP,
                    MenuCategory.WESTERN, MenuCategory.COFFEE, MenuCategory.HOT
                ));
            } else if (hour >= 12 && hour < 18) {
                // 가을 오후: 따뜻한 간식과 음료
                suggestions.addAll(Arrays.asList(
                    MenuCategory.COFFEE, MenuCategory.HOT, MenuCategory.DESSERT, MenuCategory.KOREAN,
                    MenuCategory.WESTERN, MenuCategory.RICE, MenuCategory.MEAT
                ));
            } else {
                // 가을 저녁: 따뜻하고 든든한 메뉴
                suggestions.addAll(Arrays.asList(
                    MenuCategory.MEAT, MenuCategory.KOREAN, MenuCategory.HOT, MenuCategory.SOUP,
                    MenuCategory.SPICY, MenuCategory.CHINESE, MenuCategory.WESTERN
                ));
            }
        }

        // 겨울 계절별 시간대 추천
        else if (season == SeasonType.WINTER) {
            if (hour >= 6 && hour < 12) {
                // 겨울 아침/점심: 따뜻하고 든든한 메뉴
                suggestions.addAll(Arrays.asList(
                    MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.KOREAN, MenuCategory.MEAT,
                    MenuCategory.SPICY, MenuCategory.COFFEE, MenuCategory.RICE, MenuCategory.WARMING,
                    MenuCategory.COMFORT, MenuCategory.ENERGY
                ));
            } else if (hour >= 12 && hour < 18) {
                // 겨울 오후: 따뜻한 음료와 간식
                suggestions.addAll(Arrays.asList(
                    MenuCategory.COFFEE, MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.KOREAN,
                    MenuCategory.DESSERT, MenuCategory.SPICY, MenuCategory.MEAT, MenuCategory.WARMING,
                    MenuCategory.COMFORT_FOOD, MenuCategory.BAKERY
                ));
            } else {
                // 겨울 저녁: 매우 따뜻하고 든든한 메뉴
                suggestions.addAll(Arrays.asList(
                    MenuCategory.HOT, MenuCategory.SPICY, MenuCategory.SOUP, MenuCategory.MEAT,
                    MenuCategory.KOREAN, MenuCategory.CHINESE, MenuCategory.NOODLE, MenuCategory.WARMING,
                    MenuCategory.COMFORT_FOOD, MenuCategory.PREMIUM
                ));
            }
        }

        return suggestions;
    }

    // 특별한 날씨 상황별 추천
    public List<MenuCategory> getSpecialWeatherCategories(WeatherType weatherType, Double temperature,
                                                         Integer humidity, String weatherDescription) {
        List<MenuCategory> suggestions = new ArrayList<>();

        // 태풍이나 강한 바람
        if (weatherDescription != null && (weatherDescription.contains("태풍") || weatherDescription.contains("강풍"))) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.KOREAN, MenuCategory.SPICY,
                MenuCategory.MEAT, MenuCategory.COFFEE, MenuCategory.NOODLE, MenuCategory.COMFORT_FOOD,
                MenuCategory.WARMING, MenuCategory.DELIVERY
            ));
        }

        // 폭우
        if (weatherDescription != null && weatherDescription.contains("폭우")) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.HOT, MenuCategory.SPICY, MenuCategory.SOUP, MenuCategory.KOREAN,
                MenuCategory.NOODLE, MenuCategory.COFFEE, MenuCategory.MEAT, MenuCategory.COMFORT_FOOD,
                MenuCategory.WARMING, MenuCategory.DELIVERY
            ));
        }

        // 폭설
        if (weatherDescription != null && weatherDescription.contains("폭설")) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.HOT, MenuCategory.SPICY, MenuCategory.SOUP, MenuCategory.MEAT,
                MenuCategory.KOREAN, MenuCategory.CHINESE, MenuCategory.COFFEE, MenuCategory.WARMING,
                MenuCategory.COMFORT_FOOD, MenuCategory.ENERGY
            ));
        }

        // 혹한
        if (temperature != null && temperature < -15) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.HOT, MenuCategory.SPICY, MenuCategory.SOUP, MenuCategory.MEAT,
                MenuCategory.KOREAN, MenuCategory.CHINESE, MenuCategory.COFFEE, MenuCategory.NOODLE,
                MenuCategory.WARMING, MenuCategory.ENERGY, MenuCategory.COMFORT_FOOD
            ));
        }

        // 폭염
        if (temperature != null && temperature > 38) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.COLD, MenuCategory.DESSERT, MenuCategory.COFFEE, MenuCategory.LIGHT,
                MenuCategory.VEGETARIAN, MenuCategory.SEAFOOD, MenuCategory.JAPANESE, MenuCategory.COOLING,
                MenuCategory.REFRESHING, MenuCategory.VEGAN
            ));
        }

        // 미세먼지/황사
        if (weatherDescription != null && (weatherDescription.contains("미세먼지") || weatherDescription.contains("황사"))) {
            suggestions.addAll(Arrays.asList(
                MenuCategory.HEALTHY, MenuCategory.VEGETARIAN, MenuCategory.SOUP, MenuCategory.KOREAN,
                MenuCategory.LIGHT, MenuCategory.SEAFOOD, MenuCategory.JAPANESE, MenuCategory.VEGAN,
                MenuCategory.DIET, MenuCategory.REFRESHING
            ));
        }

        return suggestions;
    }

    // 업종별 특화 추천
    public List<MenuCategory> getBusinessTypeCategories(String businessType) {
        if (businessType == null) return List.of();

        return switch (businessType.toLowerCase()) {
            case "카페", "cafe", "coffee" -> Arrays.asList(
                MenuCategory.COFFEE, MenuCategory.DESSERT, MenuCategory.BAKERY, MenuCategory.LIGHT,
                MenuCategory.BRUNCH, MenuCategory.CASUAL, MenuCategory.QUICK
            );
            case "패스트푸드", "fastfood" -> Arrays.asList(
                MenuCategory.FAST_FOOD, MenuCategory.QUICK, MenuCategory.CASUAL, MenuCategory.BUDGET,
                MenuCategory.DELIVERY, MenuCategory.TAKEOUT
            );
            case "고급레스토랑", "fine_dining" -> Arrays.asList(
                MenuCategory.PREMIUM, MenuCategory.GOURMET, MenuCategory.WESTERN, MenuCategory.SLOW,
                MenuCategory.ROMANTIC, MenuCategory.BUSINESS
            );
            case "패밀리레스토랑", "family" -> Arrays.asList(
                MenuCategory.CASUAL, MenuCategory.KIDS, MenuCategory.WESTERN, MenuCategory.KOREAN,
                MenuCategory.COMFORT, MenuCategory.PARTY
            );
            default -> Arrays.asList(
                MenuCategory.KOREAN, MenuCategory.CASUAL, MenuCategory.COMFORT, MenuCategory.BUDGET
            );
        };
    }

    // 고객 연령대별 추천
    public List<MenuCategory> getAgeGroupCategories(String ageGroup) {
        if (ageGroup == null) return List.of();

        return switch (ageGroup.toLowerCase()) {
            case "10대", "teens" -> Arrays.asList(
                MenuCategory.FAST_FOOD, MenuCategory.SPICY, MenuCategory.DESSERT, MenuCategory.QUICK,
                MenuCategory.BUDGET, MenuCategory.STREET_FOOD
            );
            case "20대", "twenties" -> Arrays.asList(
                MenuCategory.FUSION, MenuCategory.JAPANESE, MenuCategory.WESTERN, MenuCategory.SPICY,
                MenuCategory.COFFEE, MenuCategory.DELIVERY, MenuCategory.BUDGET
            );
            case "30대", "thirties" -> Arrays.asList(
                MenuCategory.HEALTHY, MenuCategory.PREMIUM, MenuCategory.KOREAN, MenuCategory.BUSINESS,
                MenuCategory.GOURMET, MenuCategory.PROTEIN
            );
            case "40대", "forties" -> Arrays.asList(
                MenuCategory.TRADITIONAL, MenuCategory.HEALTHY, MenuCategory.KOREAN, MenuCategory.PREMIUM,
                MenuCategory.COMFORT, MenuCategory.BUSINESS
            );
            case "50대이상", "seniors" -> Arrays.asList(
                MenuCategory.TRADITIONAL, MenuCategory.HEALTHY, MenuCategory.KOREAN, MenuCategory.SENIOR,
                MenuCategory.LIGHT, MenuCategory.COMFORT
            );
            default -> Arrays.asList(
                MenuCategory.KOREAN, MenuCategory.CASUAL, MenuCategory.COMFORT
            );
        };
    }
}
