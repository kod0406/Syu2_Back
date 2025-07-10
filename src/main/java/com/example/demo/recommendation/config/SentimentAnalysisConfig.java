package com.example.demo.recommendation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@ConfigurationProperties(prefix = "sentiment")
@Data
public class SentimentAnalysisConfig {

    private Positive positive = new Positive();
    private Negative negative = new Negative();
    private Negation negation = new Negation();
    private Threshold threshold = new Threshold();
    private Weight weight = new Weight();

    @Data
    public static class Positive {
        private List<String> basic = new ArrayList<>();
        private List<String> strong = new ArrayList<>();
        private List<String> revisit = new ArrayList<>();
        private List<String> service = new ArrayList<>();
    }

    @Data
    public static class Negative {
        private List<String> basic = new ArrayList<>();
        private List<String> strong = new ArrayList<>();
        private List<String> revisit = new ArrayList<>();
        private List<String> taste = new ArrayList<>();
    }

    @Data
    public static class Negation {
        private List<String> patterns = new ArrayList<>();
        private Search search = new Search();

        @Data
        public static class Search {
            private int range = 5;
        }
    }

    @Data
    public static class Threshold {
        private double positive = 0.3;
        private double negative = -0.3;
    }

    @Data
    public static class Weight {
        private double basic = 1.0;
        private double strong = 2.0;
        private double special = 2.0;
        private Negation negation = new Negation();

        @Data
        public static class Negation {
            private double strong = 1.0;
            private double basic = 0.5;
        }
    }
}