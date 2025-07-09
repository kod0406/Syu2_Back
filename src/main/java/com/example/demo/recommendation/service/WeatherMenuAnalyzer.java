package com.example.demo.recommendation.service;

import com.example.demo.recommendation.enums.MenuCategory;
import com.example.demo.recommendation.enums.WeatherType;
import com.example.demo.recommendation.enums.SeasonType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherMenuAnalyzer {

    // 모든 조건을 종합하여 최적의 메뉴 카테고리를 추천하는 통합 메서드
    public List<MenuCategory> suggestBestMenuCategories(
        WeatherType weatherType, SeasonType season, Double temperature, Integer humidity,
        LocalTime time, String weatherDescription, String businessType, String ageGroup) {

        List<MenuCategory> suggestions = new ArrayList<>();

        // 1. 기본 날씨 및 계절 기반 추천
        suggestions.addAll(getWeatherBasedCategories(weatherType));
        suggestions.addAll(getSeasonBasedCategories(season));

        // 2. 온도 및 습도 기반 추천
        if (temperature != null) {
            suggestions.addAll(getTemperatureBasedCategories(temperature));
        }
        if (humidity != null) {
            suggestions.addAll(getHumidityBasedCategories(humidity));
        }

        // 3. 시간대 기반 추천
        if (time != null) {
            suggestions.addAll(getTimeBasedCategories(time));
        }

        // 4. 복합 날씨 조건 기반 추천
        suggestions.addAll(getComplexWeatherCategories(weatherType, temperature, humidity));

        // 5. 계절 및 시간 조합 기반 추천
        suggestions.addAll(getSeasonTimeCategories(season, time));

        // 6. 특별 날씨 상황 기반 추천
        suggestions.addAll(getSpecialWeatherCategories(weatherType, temperature, humidity, weatherDescription));

        // 7. 업종 및 연령대 기반 추천
        if (businessType != null) {
            suggestions.addAll(getBusinessTypeCategories(businessType));
        }
        if (ageGroup != null) {
            suggestions.addAll(getAgeGroupCategories(ageGroup));
        }

        // 최종적으로 중복 제거, 우선순위 정렬 후 상위 10개 추천
        return suggestions.stream()
            .distinct()
            .sorted(this::compareMenuCategoryPriority)
            .limit(10)
            .toList();
    }


    // 날씨별 메뉴 카테고리 매핑 - 더 다양한 카테고리 포함
    private List<MenuCategory> getWeatherBasedCategories(WeatherType weatherType) {
        return switch (weatherType) {
            case RAIN, DRIZZLE -> Arrays.asList(
                MenuCategory.RAINY_DAY, MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.KOREAN,
                MenuCategory.NOODLE, MenuCategory.SPICY, MenuCategory.COFFEE, MenuCategory.CHINESE,
                MenuCategory.COMFORT_FOOD, MenuCategory.WARMING, MenuCategory.STEAMED, MenuCategory.BOILED,
                MenuCategory.TRADITIONAL, MenuCategory.DELIVERY, MenuCategory.ENERGY, MenuCategory.SLOW,
                MenuCategory.NOSTALGIC, MenuCategory.FERMENTED, MenuCategory.MILD, MenuCategory.HALAL,
                MenuCategory.VITAMIN_RICH, MenuCategory.IMMUNITY
            );
            case SNOW, COLD -> Arrays.asList(
                MenuCategory.SNOWY_DAY, MenuCategory.COLD_WAVE, MenuCategory.HOT, MenuCategory.SOUP,
                MenuCategory.SPICY, MenuCategory.MEAT, MenuCategory.KOREAN, MenuCategory.NOODLE,
                MenuCategory.COFFEE, MenuCategory.WINTER_WARM, MenuCategory.WARMING, MenuCategory.GRILLED,
                MenuCategory.CHINESE, MenuCategory.COMFORT_FOOD, MenuCategory.ENERGY, MenuCategory.IMMUNITY,
                MenuCategory.PROTEIN, MenuCategory.HIGH_FIBER, MenuCategory.TRADITIONAL, MenuCategory.CHEESE,
                MenuCategory.CLASSIC, MenuCategory.ANDONG, MenuCategory.JEONJU
            );
            case HOT -> Arrays.asList(
                MenuCategory.HEAT_WAVE, MenuCategory.COLD, MenuCategory.LIGHT, MenuCategory.DESSERT,
                MenuCategory.COFFEE, MenuCategory.SEAFOOD, MenuCategory.VEGETARIAN, MenuCategory.JAPANESE,
                MenuCategory.SUMMER_COOL, MenuCategory.COOLING, MenuCategory.REFRESHING, MenuCategory.RAW,
                MenuCategory.VEGAN, MenuCategory.DIET, MenuCategory.BAKERY, MenuCategory.BRUNCH,
                MenuCategory.SUGAR_FREE, MenuCategory.GLUTEN_FREE, MenuCategory.LOW_SODIUM, MenuCategory.KETO,
                MenuCategory.TRENDY, MenuCategory.BUSAN, MenuCategory.JEJU
            );
            case CLEAR -> Arrays.asList(
                MenuCategory.SUNNY_DAY, MenuCategory.LIGHT, MenuCategory.KOREAN, MenuCategory.RICE,
                MenuCategory.VEGETARIAN, MenuCategory.JAPANESE, MenuCategory.WESTERN, MenuCategory.COFFEE,
                MenuCategory.HEALTHY, MenuCategory.CASUAL, MenuCategory.OUTDOOR, MenuCategory.MILD_WEATHER,
                MenuCategory.FUSION, MenuCategory.GOURMET, MenuCategory.PREMIUM, MenuCategory.ROMANTIC,
                MenuCategory.INNOVATIVE, MenuCategory.VALUE, MenuCategory.ECONOMICAL, MenuCategory.SEOUL,
                MenuCategory.GYEONGGI, MenuCategory.WINE
            );
            case THUNDERSTORM -> Arrays.asList(
                MenuCategory.STORM, MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.KOREAN,
                MenuCategory.SPICY, MenuCategory.MEAT, MenuCategory.NOODLE, MenuCategory.COFFEE,
                MenuCategory.COMFORT_FOOD, MenuCategory.WARMING, MenuCategory.DELIVERY, MenuCategory.CHINESE,
                MenuCategory.COMFORT, MenuCategory.ENERGY, MenuCategory.TAKEOUT, MenuCategory.MIDNIGHT,
                MenuCategory.TYPHOON, MenuCategory.STRESS_RELIEF, MenuCategory.ALONE, MenuCategory.SIMPLE
            );
            case CLOUDS -> Arrays.asList(
                MenuCategory.CLOUDY_DAY, MenuCategory.COFFEE, MenuCategory.HOT, MenuCategory.LIGHT,
                MenuCategory.DESSERT, MenuCategory.KOREAN, MenuCategory.WESTERN, MenuCategory.RICE,
                MenuCategory.MILD_WEATHER, MenuCategory.CASUAL, MenuCategory.BAKERY, MenuCategory.BRUNCH,
                MenuCategory.ITALIAN, MenuCategory.FUSION, MenuCategory.COMFORT, MenuCategory.FAMILY,
                MenuCategory.MOOD_BOOST, MenuCategory.RELAXATION, MenuCategory.SWEET, MenuCategory.RETRO
            );
            case MIST, FOG -> Arrays.asList(
                MenuCategory.COFFEE, MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.KOREAN,
                MenuCategory.LIGHT, MenuCategory.DESSERT, MenuCategory.WESTERN, MenuCategory.WARMING,
                MenuCategory.COMFORT, MenuCategory.STEAMED, MenuCategory.BAKERY, MenuCategory.BRUNCH,
                MenuCategory.MOOD_BOOST, MenuCategory.RELAXATION, MenuCategory.NOSTALGIC, MenuCategory.SLOW,
                MenuCategory.CLASSIC, MenuCategory.TRADITIONAL, MenuCategory.CHUNCHEON, MenuCategory.DAEGU
            );
            case HUMID -> Arrays.asList(
                MenuCategory.HUMID_DAY, MenuCategory.VERY_HUMID, MenuCategory.COLD, MenuCategory.LIGHT,
                MenuCategory.SEAFOOD, MenuCategory.VEGETARIAN, MenuCategory.JAPANESE, MenuCategory.DESSERT,
                MenuCategory.COFFEE, MenuCategory.COOLING, MenuCategory.REFRESHING, MenuCategory.VEGAN,
                MenuCategory.DIET, MenuCategory.DIGESTIVE, MenuCategory.DETOX, MenuCategory.VITAMIN_RICH,
                MenuCategory.LACTOSE_FREE, MenuCategory.GLUTEN_FREE, MenuCategory.INCHEON, MenuCategory.ULSAN
            );
            case HAZE, DUST -> Arrays.asList(
                MenuCategory.FINE_DUST, MenuCategory.YELLOW_DUST, MenuCategory.LIGHT, MenuCategory.VEGETARIAN,
                MenuCategory.SOUP, MenuCategory.COFFEE, MenuCategory.COLD, MenuCategory.SEAFOOD,
                MenuCategory.JAPANESE, MenuCategory.HEALTHY, MenuCategory.DETOX, MenuCategory.IMMUNITY,
                MenuCategory.ANTI_INFLAMMATORY, MenuCategory.VITAMIN_RICH, MenuCategory.DIGESTIVE, MenuCategory.VEGAN,
                MenuCategory.HIGH_FIBER, MenuCategory.LOW_SODIUM, MenuCategory.GWANGJU, MenuCategory.DAEJEON
            );
            case SMOKE -> Arrays.asList(
                MenuCategory.LIGHT, MenuCategory.COLD, MenuCategory.VEGETARIAN, MenuCategory.SEAFOOD,
                MenuCategory.COFFEE, MenuCategory.DESSERT, MenuCategory.JAPANESE, MenuCategory.HEALTHY,
                MenuCategory.DETOX, MenuCategory.IMMUNITY, MenuCategory.ANTI_INFLAMMATORY, MenuCategory.VEGAN,
                MenuCategory.DIET, MenuCategory.DIGESTIVE, MenuCategory.VITAMIN_RICH, MenuCategory.REFRESHING,
                MenuCategory.SUGAR_FREE, MenuCategory.GLUTEN_FREE, MenuCategory.KETO, MenuCategory.HIGH_FIBER
            );
            case DRY -> Arrays.asList(
                MenuCategory.DRY_DAY, MenuCategory.VERY_DRY, MenuCategory.SOUP, MenuCategory.HOT,
                MenuCategory.KOREAN, MenuCategory.COFFEE, MenuCategory.LIGHT, MenuCategory.SEAFOOD,
                MenuCategory.RICE, MenuCategory.STEAMED, MenuCategory.BOILED, MenuCategory.NOODLE,
                MenuCategory.WARMING, MenuCategory.COMFORT, MenuCategory.TRADITIONAL, MenuCategory.HEALTHY,
                MenuCategory.MILD, MenuCategory.DIGESTIVE, MenuCategory.HIGH_CALCIUM, MenuCategory.CALCIUM
            );
        };
    }

    // 계절별 메뉴 카테고리 매핑 - 더 다양한 카테고리 포함
    private List<MenuCategory> getSeasonBasedCategories(SeasonType season) {
        return switch (season) {
            case SPRING -> Arrays.asList(
                MenuCategory.SPRING_FRESH, MenuCategory.LIGHT, MenuCategory.VEGETARIAN, MenuCategory.KOREAN,
                MenuCategory.SEAFOOD, MenuCategory.JAPANESE, MenuCategory.RICE, MenuCategory.COFFEE,
                MenuCategory.WESTERN, MenuCategory.HEALTHY, MenuCategory.VITAMIN_RICH, MenuCategory.DETOX,
                MenuCategory.BRUNCH, MenuCategory.OUTDOOR, MenuCategory.CASUAL, MenuCategory.REFRESHING,
                MenuCategory.FUSION, MenuCategory.DIGESTIVE, MenuCategory.MOOD_BOOST, MenuCategory.TRENDY,
                MenuCategory.GLUTEN_FREE, MenuCategory.VEGAN, MenuCategory.RAW, MenuCategory.WINE,
                MenuCategory.INNOVATIVE, MenuCategory.SEOUL, MenuCategory.GYEONGGI, MenuCategory.CHUNCHEON
            );
            case SUMMER -> Arrays.asList(
                MenuCategory.SUMMER_COOL, MenuCategory.COLD, MenuCategory.LIGHT, MenuCategory.DESSERT,
                MenuCategory.SEAFOOD, MenuCategory.COFFEE, MenuCategory.JAPANESE, MenuCategory.VEGETARIAN,
                MenuCategory.WESTERN, MenuCategory.COOLING, MenuCategory.REFRESHING, MenuCategory.RAW,
                MenuCategory.VEGAN, MenuCategory.DIET, MenuCategory.BAKERY, MenuCategory.BRUNCH,
                MenuCategory.OUTDOOR, MenuCategory.CASUAL, MenuCategory.ITALIAN, MenuCategory.ROMANTIC,
                MenuCategory.SUGAR_FREE, MenuCategory.LOW_SODIUM, MenuCategory.KETO, MenuCategory.LACTOSE_FREE,
                MenuCategory.BUSAN, MenuCategory.JEJU, MenuCategory.INCHEON, MenuCategory.ULSAN
            );
            case AUTUMN -> Arrays.asList(
                MenuCategory.AUTUMN_HEARTY, MenuCategory.MEAT, MenuCategory.KOREAN, MenuCategory.HOT,
                MenuCategory.SOUP, MenuCategory.SPICY, MenuCategory.RICE, MenuCategory.CHINESE,
                MenuCategory.WESTERN, MenuCategory.COMFORT, MenuCategory.ENERGY, MenuCategory.GRILLED,
                MenuCategory.TRADITIONAL, MenuCategory.PREMIUM, MenuCategory.GOURMET, MenuCategory.FERMENTED,
                MenuCategory.NOSTALGIC, MenuCategory.FAMILY, MenuCategory.BUSINESS, MenuCategory.CLASSIC,
                MenuCategory.PROTEIN, MenuCategory.HIGH_FIBER, MenuCategory.IMMUNITY, MenuCategory.CHEESE,
                MenuCategory.ANDONG, MenuCategory.JEONJU, MenuCategory.DAEGU, MenuCategory.GWANGJU
            );
            case WINTER -> Arrays.asList(
                MenuCategory.WINTER_WARM, MenuCategory.HOT, MenuCategory.SPICY, MenuCategory.SOUP,
                MenuCategory.MEAT, MenuCategory.KOREAN, MenuCategory.NOODLE, MenuCategory.COFFEE,
                MenuCategory.CHINESE, MenuCategory.WARMING, MenuCategory.COMFORT_FOOD, MenuCategory.IMMUNITY,
                MenuCategory.STEAMED, MenuCategory.BOILED, MenuCategory.TRADITIONAL, MenuCategory.ENERGY,
                MenuCategory.SLOW, MenuCategory.FAMILY, MenuCategory.LUXURY, MenuCategory.CELEBRATION,
                MenuCategory.HIGH_CALCIUM, MenuCategory.VITAMIN_RICH, MenuCategory.DIGESTIVE, MenuCategory.CALCIUM,
                MenuCategory.HALAL, MenuCategory.ANTI_INFLAMMATORY, MenuCategory.DAEJEON, MenuCategory.RETRO
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
    // 온도별 세밀한 카테고리 추천
    private List<MenuCategory> getTemperatureBasedCategories(Double temperature) {
        if (temperature == null) return List.of();

        if (temperature <= -10) {
            // 극한 추위 (-10도 이하)
            return Arrays.asList(
                MenuCategory.FREEZING, MenuCategory.HOT, MenuCategory.SPICY, MenuCategory.SOUP, MenuCategory.MEAT,
                MenuCategory.KOREAN, MenuCategory.CHINESE, MenuCategory.COFFEE, MenuCategory.WARMING,
                MenuCategory.STEAMED, MenuCategory.GRILLED, MenuCategory.COMFORT_FOOD, MenuCategory.ENERGY,
                MenuCategory.IMMUNITY, MenuCategory.TRADITIONAL, MenuCategory.SLOW, MenuCategory.DELIVERY
            );
        } else if (temperature <= 0) {
            // 영하 (0도 ~ -10도)
            return Arrays.asList(
                MenuCategory.VERY_COLD, MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.SPICY, MenuCategory.MEAT,
                MenuCategory.KOREAN, MenuCategory.NOODLE, MenuCategory.COFFEE, MenuCategory.WARMING,
                MenuCategory.CHINESE, MenuCategory.COMFORT_FOOD, MenuCategory.ENERGY, MenuCategory.IMMUNITY,
                MenuCategory.GRILLED, MenuCategory.STEAMED, MenuCategory.TRADITIONAL, MenuCategory.FAMILY
            );
        } else if (temperature <= 10) {
            // 쌀쌀함 (1도 ~ 10도)
            return Arrays.asList(
                MenuCategory.COLD_WEATHER, MenuCategory.HOT, MenuCategory.SOUP, MenuCategory.KOREAN, MenuCategory.RICE,
                MenuCategory.MEAT, MenuCategory.COFFEE, MenuCategory.WESTERN, MenuCategory.COMFORT,
                MenuCategory.NOODLE, MenuCategory.CHINESE, MenuCategory.WARMING, MenuCategory.ENERGY,
                MenuCategory.GRILLED, MenuCategory.BOILED, MenuCategory.TRADITIONAL, MenuCategory.CASUAL
            );
        } else if (temperature <= 20) {
            // 서늘함 (11도 ~ 20도)
            return Arrays.asList(
                MenuCategory.COOL_WEATHER, MenuCategory.KOREAN, MenuCategory.RICE, MenuCategory.SOUP, MenuCategory.WESTERN,
                MenuCategory.JAPANESE, MenuCategory.COFFEE, MenuCategory.LIGHT, MenuCategory.CASUAL,
                MenuCategory.MEAT, MenuCategory.VEGETARIAN, MenuCategory.COMFORT, MenuCategory.FUSION,
                MenuCategory.ITALIAN, MenuCategory.BRUNCH, MenuCategory.BAKERY, MenuCategory.HEALTHY
            );
        } else if (temperature <= 25) {
            // 적당함 (21도 ~ 25도)
            return Arrays.asList(
                MenuCategory.MILD_WEATHER, MenuCategory.LIGHT, MenuCategory.KOREAN, MenuCategory.JAPANESE, MenuCategory.WESTERN,
                MenuCategory.RICE, MenuCategory.VEGETARIAN, MenuCategory.COFFEE, MenuCategory.SEAFOOD,
                MenuCategory.HEALTHY, MenuCategory.REFRESHING, MenuCategory.CASUAL, MenuCategory.FUSION,
                MenuCategory.GOURMET, MenuCategory.PREMIUM, MenuCategory.OUTDOOR, MenuCategory.ROMANTIC
            );
        } else if (temperature <= 30) {
            // 따뜻함 (26도 ~ 30도)
            return Arrays.asList(
                MenuCategory.WARM_WEATHER, MenuCategory.LIGHT, MenuCategory.COLD, MenuCategory.SEAFOOD, MenuCategory.VEGETARIAN,
                MenuCategory.JAPANESE, MenuCategory.WESTERN, MenuCategory.COFFEE, MenuCategory.DESSERT,
                MenuCategory.REFRESHING, MenuCategory.COOLING, MenuCategory.DIET, MenuCategory.VEGAN,
                MenuCategory.RAW, MenuCategory.HEALTHY, MenuCategory.OUTDOOR, MenuCategory.CASUAL
            );
        } else if (temperature <= 35) {
            // 더움 (31도 ~ 35도)
            return Arrays.asList(
                MenuCategory.VERY_HOT, MenuCategory.COLD, MenuCategory.LIGHT, MenuCategory.DESSERT, MenuCategory.COFFEE,
                MenuCategory.SEAFOOD, MenuCategory.VEGETARIAN, MenuCategory.JAPANESE,
                MenuCategory.COOLING, MenuCategory.REFRESHING, MenuCategory.VEGAN, MenuCategory.DIET,
                MenuCategory.RAW, MenuCategory.BAKERY, MenuCategory.BRUNCH, MenuCategory.ITALIAN
            );
        } else {
            // 극한 더위 (36도 이상)
            return Arrays.asList(
                MenuCategory.EXTREME_HOT, MenuCategory.COLD, MenuCategory.DESSERT, MenuCategory.COFFEE, MenuCategory.LIGHT,
                MenuCategory.VEGETARIAN, MenuCategory.SEAFOOD, MenuCategory.JAPANESE,
                MenuCategory.COOLING, MenuCategory.VEGAN, MenuCategory.RAW, MenuCategory.DIET,
                MenuCategory.REFRESHING, MenuCategory.BAKERY, MenuCategory.SUGAR_FREE, MenuCategory.HEALTHY
            );
        }
    }

    // 습도별 카테고리 추천
    private List<MenuCategory> getHumidityBasedCategories(Integer humidity) {
        if (humidity == null) return List.of();

        if (humidity <= 30) {
            // 건조함 (30% 이하)
            return Arrays.asList(
                MenuCategory.VERY_DRY, MenuCategory.SOUP, MenuCategory.HOT, MenuCategory.KOREAN, MenuCategory.COFFEE,
                MenuCategory.SEAFOOD, MenuCategory.RICE, MenuCategory.NOODLE, MenuCategory.HEALTHY,
                MenuCategory.STEAMED, MenuCategory.BOILED, MenuCategory.WARMING, MenuCategory.COMFORT,
                MenuCategory.TRADITIONAL, MenuCategory.IMMUNITY, MenuCategory.VITAMIN_RICH, MenuCategory.DIGESTIVE
            );
        } else if (humidity <= 50) {
            // 적당함 (31% ~ 50%)
            return Arrays.asList(
                MenuCategory.MODERATE_HUMIDITY, MenuCategory.KOREAN, MenuCategory.WESTERN, MenuCategory.JAPANESE, MenuCategory.RICE,
                MenuCategory.MEAT, MenuCategory.SEAFOOD, MenuCategory.COFFEE, MenuCategory.CASUAL,
                MenuCategory.FUSION, MenuCategory.GOURMET, MenuCategory.PREMIUM, MenuCategory.HEALTHY,
                MenuCategory.COMFORT, MenuCategory.TRADITIONAL, MenuCategory.BUSINESS, MenuCategory.FAMILY
            );
        } else if (humidity <= 70) {
            // 습함 (51% ~ 70%)
            return Arrays.asList(
                MenuCategory.HUMID, MenuCategory.LIGHT, MenuCategory.VEGETARIAN, MenuCategory.SEAFOOD, MenuCategory.JAPANESE,
                MenuCategory.COFFEE, MenuCategory.COLD, MenuCategory.DESSERT, MenuCategory.REFRESHING,
                MenuCategory.DIET, MenuCategory.VEGAN, MenuCategory.RAW, MenuCategory.HEALTHY,
                MenuCategory.DIGESTIVE, MenuCategory.DETOX, MenuCategory.VITAMIN_RICH, MenuCategory.COOLING
            );
        } else {
            // 매우 습함 (71% 이상)
            return Arrays.asList(
                MenuCategory.VERY_HUMID, MenuCategory.LIGHT, MenuCategory.COLD, MenuCategory.VEGETARIAN, MenuCategory.SEAFOOD,
                MenuCategory.DESSERT, MenuCategory.COFFEE, MenuCategory.JAPANESE, MenuCategory.COOLING,
                MenuCategory.VEGAN, MenuCategory.RAW, MenuCategory.DIET, MenuCategory.REFRESHING,
                MenuCategory.ANTI_INFLAMMATORY, MenuCategory.DETOX, MenuCategory.DIGESTIVE, MenuCategory.SUGAR_FREE
            );
        }
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
                MenuCategory.BRUNCH, MenuCategory.CASUAL, MenuCategory.QUICK, MenuCategory.MOOD_BOOST,
                MenuCategory.RELAXATION, MenuCategory.STUDY, MenuCategory.WORK, MenuCategory.FRIENDS,
                MenuCategory.DATE, MenuCategory.ALONE, MenuCategory.TRENDY, MenuCategory.PREMIUM
            );
            case "패스트푸드", "fastfood" -> Arrays.asList(
                MenuCategory.FAST_FOOD, MenuCategory.QUICK, MenuCategory.CASUAL, MenuCategory.BUDGET,
                MenuCategory.DELIVERY, MenuCategory.TAKEOUT, MenuCategory.FRIED, MenuCategory.MEAT,
                MenuCategory.KIDS, MenuCategory.TEENS, MenuCategory.FRIENDS, MenuCategory.STREET_FOOD,
                MenuCategory.ECONOMICAL, MenuCategory.VALUE, MenuCategory.MIDNIGHT, MenuCategory.ENERGY
            );
            case "고급레스토랑", "fine_dining" -> Arrays.asList(
                MenuCategory.PREMIUM, MenuCategory.GOURMET, MenuCategory.WESTERN, MenuCategory.SLOW,
                MenuCategory.ROMANTIC, MenuCategory.BUSINESS, MenuCategory.LUXURY, MenuCategory.CELEBRATION,
                MenuCategory.DATE, MenuCategory.FAMILY, MenuCategory.INNOVATIVE, MenuCategory.CLASSIC,
                MenuCategory.SEAFOOD, MenuCategory.MEAT, MenuCategory.FUSION, MenuCategory.ITALIAN
            );
            case "패밀리레스토랑", "family" -> Arrays.asList(
                MenuCategory.CASUAL, MenuCategory.KIDS, MenuCategory.WESTERN, MenuCategory.KOREAN,
                MenuCategory.COMFORT, MenuCategory.PARTY, MenuCategory.FAMILY, MenuCategory.CELEBRATION,
                MenuCategory.RICE, MenuCategory.MEAT, MenuCategory.SOUP, MenuCategory.DESSERT,
                MenuCategory.BRUNCH, MenuCategory.VALUE, MenuCategory.TRADITIONAL, MenuCategory.HEALTHY
            );
            case "한식당", "korean" -> Arrays.asList(
                MenuCategory.KOREAN, MenuCategory.TRADITIONAL, MenuCategory.SOUP, MenuCategory.RICE,
                MenuCategory.MEAT, MenuCategory.SEAFOOD, MenuCategory.VEGETARIAN, MenuCategory.SPICY,
                MenuCategory.FERMENTED, MenuCategory.HEALTHY, MenuCategory.FAMILY, MenuCategory.BUSINESS,
                MenuCategory.COMFORT, MenuCategory.CLASSIC, MenuCategory.SENIOR, MenuCategory.NOSTALGIC
            );
            case "일식당", "japanese" -> Arrays.asList(
                MenuCategory.JAPANESE, MenuCategory.SEAFOOD, MenuCategory.RAW, MenuCategory.LIGHT,
                MenuCategory.HEALTHY, MenuCategory.PREMIUM, MenuCategory.GOURMET, MenuCategory.REFRESHING,
                MenuCategory.DIET, MenuCategory.ROMANTIC, MenuCategory.BUSINESS, MenuCategory.TRENDY,
                MenuCategory.CLASSIC, MenuCategory.COOLING, MenuCategory.DATE, MenuCategory.FUSION
            );
            case "중식당", "chinese" -> Arrays.asList(
                MenuCategory.CHINESE, MenuCategory.SPICY, MenuCategory.HOT, MenuCategory.MEAT,
                MenuCategory.NOODLE, MenuCategory.RICE, MenuCategory.SOUP, MenuCategory.COMFORT,
                MenuCategory.FAMILY, MenuCategory.ENERGY, MenuCategory.TRADITIONAL, MenuCategory.GRILLED,
                MenuCategory.STEAMED, MenuCategory.FRIED, MenuCategory.PARTY, MenuCategory.WARMING
            );
            case "양식당", "western" -> Arrays.asList(
                MenuCategory.WESTERN, MenuCategory.MEAT, MenuCategory.SEAFOOD, MenuCategory.PREMIUM,
                MenuCategory.GOURMET, MenuCategory.ROMANTIC, MenuCategory.DATE, MenuCategory.BUSINESS,
                MenuCategory.CASUAL, MenuCategory.BRUNCH, MenuCategory.DESSERT, MenuCategory.FUSION,
                MenuCategory.INNOVATIVE, MenuCategory.CLASSIC, MenuCategory.LUXURY, MenuCategory.TRENDY
            );
            case "이탈리안", "italian" -> Arrays.asList(
                MenuCategory.ITALIAN, MenuCategory.WESTERN, MenuCategory.NOODLE, MenuCategory.CHEESE,
                MenuCategory.ROMANTIC, MenuCategory.DATE, MenuCategory.PREMIUM, MenuCategory.CASUAL,
                MenuCategory.FAMILY, MenuCategory.FUSION, MenuCategory.CLASSIC, MenuCategory.TRENDY,
                MenuCategory.DESSERT, MenuCategory.COFFEE, MenuCategory.WINE, MenuCategory.GOURMET
            );
            case "분식집", "snack_bar" -> Arrays.asList(
                MenuCategory.STREET_FOOD, MenuCategory.SPICY, MenuCategory.NOODLE, MenuCategory.RICE,
                MenuCategory.FRIED, MenuCategory.BUDGET, MenuCategory.CASUAL, MenuCategory.QUICK,
                MenuCategory.COMFORT_FOOD, MenuCategory.NOSTALGIC, MenuCategory.KIDS, MenuCategory.TEENS,
                MenuCategory.FRIENDS, MenuCategory.ECONOMICAL, MenuCategory.VALUE, MenuCategory.TAKEOUT
            );
            case "치킨집", "chicken" -> Arrays.asList(
                MenuCategory.FRIED, MenuCategory.MEAT, MenuCategory.SPICY, MenuCategory.DELIVERY,
                MenuCategory.TAKEOUT, MenuCategory.FRIENDS, MenuCategory.PARTY, MenuCategory.BEER,
                MenuCategory.MIDNIGHT, MenuCategory.CASUAL, MenuCategory.COMFORT_FOOD, MenuCategory.ENERGY,
                MenuCategory.STRESS_RELIEF, MenuCategory.CELEBRATION, MenuCategory.ECONOMICAL, MenuCategory.QUICK
            );
            case "술집", "bar" -> Arrays.asList(
                MenuCategory.SPICY, MenuCategory.FRIED, MenuCategory.GRILLED, MenuCategory.SEAFOOD,
                MenuCategory.MEAT, MenuCategory.FRIENDS, MenuCategory.STRESS_RELIEF, MenuCategory.RELAXATION,
                MenuCategory.MIDNIGHT, MenuCategory.PARTY, MenuCategory.CELEBRATION, MenuCategory.COMFORT_FOOD,
                MenuCategory.NOSTALGIC, MenuCategory.TRADITIONAL, MenuCategory.CASUAL, MenuCategory.ALONE
            );
            case "베이커리", "bakery" -> Arrays.asList(
                MenuCategory.BAKERY, MenuCategory.DESSERT, MenuCategory.COFFEE, MenuCategory.LIGHT,
                MenuCategory.BRUNCH, MenuCategory.SWEET, MenuCategory.CASUAL, MenuCategory.QUICK,
                MenuCategory.MOOD_BOOST, MenuCategory.CELEBRATION, MenuCategory.KIDS, MenuCategory.FAMILY,
                MenuCategory.DATE, MenuCategory.TRENDY, MenuCategory.PREMIUM, MenuCategory.HEALTHY
            );
            default -> Arrays.asList(
                MenuCategory.KOREAN, MenuCategory.CASUAL, MenuCategory.COMFORT, MenuCategory.BUDGET,
                MenuCategory.FAMILY, MenuCategory.TRADITIONAL, MenuCategory.HEALTHY, MenuCategory.RICE,
                MenuCategory.SOUP, MenuCategory.MEAT, MenuCategory.VALUE, MenuCategory.BUSINESS
            );
        };
    }

    // 고객 연령대별 추천 - 균형잡힌 분배
    public List<MenuCategory> getAgeGroupCategories(String ageGroup) {
        if (ageGroup == null) return List.of();

        return switch (ageGroup.toLowerCase()) {
            case "10대", "teens" -> Arrays.asList(
                MenuCategory.FAST_FOOD, MenuCategory.SPICY, MenuCategory.DESSERT, MenuCategory.QUICK,
                MenuCategory.BUDGET, MenuCategory.STREET_FOOD, MenuCategory.FRIED, MenuCategory.SWEET,
                MenuCategory.TRENDY, MenuCategory.FRIENDS, MenuCategory.PARTY, MenuCategory.ENERGY,
                MenuCategory.MOOD_BOOST, MenuCategory.CASUAL, MenuCategory.DELIVERY, MenuCategory.TAKEOUT
            );
            case "20대", "twenties" -> Arrays.asList(
                MenuCategory.FUSION, MenuCategory.JAPANESE, MenuCategory.WESTERN, MenuCategory.SPICY,
                MenuCategory.COFFEE, MenuCategory.DELIVERY, MenuCategory.BUDGET, MenuCategory.TRENDY,
                MenuCategory.FRIENDS, MenuCategory.DATE, MenuCategory.STRESS_RELIEF, MenuCategory.MIDNIGHT,
                MenuCategory.ECONOMICAL, MenuCategory.INNOVATIVE, MenuCategory.CASUAL, MenuCategory.WORK
            );
            case "30대", "thirties" -> Arrays.asList(
                MenuCategory.HEALTHY, MenuCategory.PREMIUM, MenuCategory.KOREAN, MenuCategory.BUSINESS,
                MenuCategory.GOURMET, MenuCategory.PROTEIN, MenuCategory.WORK, MenuCategory.FAMILY,
                MenuCategory.DATE, MenuCategory.ROMANTIC, MenuCategory.FUSION, MenuCategory.ITALIAN,
                MenuCategory.SEAFOOD, MenuCategory.DIET, MenuCategory.VITAMIN_RICH, MenuCategory.DIGESTIVE
            );
            case "40대", "forties" -> Arrays.asList(
                MenuCategory.TRADITIONAL, MenuCategory.HEALTHY, MenuCategory.KOREAN, MenuCategory.PREMIUM,
                MenuCategory.COMFORT, MenuCategory.BUSINESS, MenuCategory.FAMILY, MenuCategory.CLASSIC,
                MenuCategory.GOURMET, MenuCategory.LUXURY, MenuCategory.MEAT, MenuCategory.SEAFOOD,
                MenuCategory.IMMUNITY, MenuCategory.ANTI_INFLAMMATORY, MenuCategory.CELEBRATION, MenuCategory.SLOW
            );
            case "50대이상", "seniors" -> Arrays.asList(
                MenuCategory.TRADITIONAL, MenuCategory.HEALTHY, MenuCategory.KOREAN, MenuCategory.SENIOR,
                MenuCategory.LIGHT, MenuCategory.COMFORT, MenuCategory.CLASSIC, MenuCategory.NOSTALGIC,
                MenuCategory.FAMILY, MenuCategory.SOUP, MenuCategory.STEAMED, MenuCategory.BOILED,
                MenuCategory.DIGESTIVE, MenuCategory.IMMUNITY, MenuCategory.LOW_SODIUM, MenuCategory.VITAMIN_RICH
            );
            case "어린이", "kids", "children" -> Arrays.asList(
                MenuCategory.KIDS, MenuCategory.LIGHT, MenuCategory.SWEET, MenuCategory.MILD,
                MenuCategory.HEALTHY, MenuCategory.SIMPLE, MenuCategory.FAMILY, MenuCategory.CASUAL,
                MenuCategory.QUICK, MenuCategory.RICE, MenuCategory.MEAT, MenuCategory.DESSERT,
                MenuCategory.BAKERY, MenuCategory.VITAMIN_RICH, MenuCategory.CALCIUM, MenuCategory.PROTEIN
            );
            default -> Arrays.asList(
                MenuCategory.KOREAN, MenuCategory.CASUAL, MenuCategory.COMFORT, MenuCategory.FAMILY,
                MenuCategory.HEALTHY, MenuCategory.TRADITIONAL, MenuCategory.RICE, MenuCategory.SOUP
            );
        };
    }
}
